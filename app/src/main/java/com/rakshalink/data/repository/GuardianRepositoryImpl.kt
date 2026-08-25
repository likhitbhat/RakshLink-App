package com.rakshalink.data.repository

import com.rakshalink.data.local.dao.AlertDao
import com.rakshalink.data.local.entities.CachedAlertEntity
import com.rakshalink.data.remote.dto.DeviceDto
import com.rakshalink.data.remote.dto.EmergencyAlertDto
import com.rakshalink.data.remote.dto.GuardianLinkDto
import com.rakshalink.data.remote.dto.LiveLocationDto
import com.rakshalink.data.remote.dto.ProfileDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.AlertModel
import com.rakshalink.domain.model.AlertType
import com.rakshalink.domain.model.LocationModel
import com.rakshalink.domain.model.WearerModel
import com.rakshalink.domain.repository.GuardianRepository
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuardianRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao,
    private val supabaseProvider: SupabaseClientProvider,
    private val userPreferencesManager: com.rakshalink.data.preferences.UserPreferencesManager
) : GuardianRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private suspend fun resolveCurrentUserId(): String {
        val supabaseUid = try { supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: "" } catch (e: Exception) { "" }
        if (supabaseUid.isNotEmpty()) return supabaseUid
        val storedUid = try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }
        if (storedUid.isNotEmpty()) return storedUid
        return ""
    }

    override fun getLinkedWearers(): Flow<List<WearerModel>> = channelFlow {
        suspend fun fetchWearers(): List<WearerModel> {
            val currentUserId = resolveCurrentUserId()
            val storedContact = try { userPreferencesManager.userPhoneOrEmailFlow.first() } catch (e: Exception) { "" }
            if (currentUserId.isEmpty() && storedContact.isEmpty()) return emptyList()

            return try {
                val links = mutableListOf<GuardianLinkDto>()

                // Fetch links matching guardian_id
                try {
                    val l1 = supabaseProvider.db.from("wearer_guardian_links")
                        .select(columns = Columns.ALL) {
                            filter {
                                if (currentUserId.isNotEmpty()) eq("guardian_id", currentUserId)
                                else eq("guardian_id", storedContact)
                            }
                        }.decodeList<GuardianLinkDto>()
                    links.addAll(l1)
                } catch (e: Exception) {}

                try {
                    val l2 = supabaseProvider.db.from("guardian_links")
                        .select(columns = Columns.ALL) {
                            filter {
                                if (currentUserId.isNotEmpty()) eq("guardian_id", currentUserId)
                                else eq("guardian_id", storedContact)
                            }
                        }.decodeList<GuardianLinkDto>()
                    links.addAll(l2)
                } catch (e: Exception) {}

                // If links empty and storedContact is present, try fallback lookup
                if (links.isEmpty() && storedContact.isNotEmpty() && currentUserId.isNotEmpty()) {
                    try {
                        val l3 = supabaseProvider.db.from("wearer_guardian_links")
                            .select(columns = Columns.ALL) {
                                filter { eq("guardian_id", storedContact) }
                            }.decodeList<GuardianLinkDto>()
                        links.addAll(l3)
                    } catch (e: Exception) {}
                }

                val distinctLinks = links.distinctBy { it.wearerId }
                if (distinctLinks.isEmpty()) return emptyList()

                val wearers = mutableListOf<WearerModel>()
                for (link in distinctLinks) {
                    val userProf = try {
                        supabaseProvider.db.from("users")
                            .select(columns = Columns.ALL) {
                                filter {
                                    or {
                                        eq("id", link.wearerId)
                                        eq("wearer_code", link.wearerId)
                                        eq("email", link.wearerId)
                                    }
                                }
                            }.decodeSingleOrNull<com.rakshalink.data.remote.dto.UserProfileDto>()
                    } catch (e: Exception) {
                        try {
                            val p = supabaseProvider.db.from("profiles")
                                .select(columns = Columns.ALL) {
                                    filter { eq("id", link.wearerId) }
                                }.decodeSingleOrNull<ProfileDto>()
                            p?.let {
                                com.rakshalink.data.remote.dto.UserProfileDto(
                                    id = it.id,
                                    email = it.email,
                                    full_name = it.fullName
                                )
                            }
                        } catch (e2: Exception) { null }
                    }

                    val latestLoc = try {
                        supabaseProvider.db.from("locations")
                            .select(columns = Columns.ALL) {
                                filter { eq("user_id", link.wearerId) }
                                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                                limit(1)
                            }.decodeSingleOrNull<LiveLocationDto>()
                    } catch (e: Exception) {
                        try {
                            supabaseProvider.db.from("live_locations")
                                .select(columns = Columns.ALL) {
                                    filter { eq("user_id", link.wearerId) }
                                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                                    limit(1)
                                }.decodeSingleOrNull<LiveLocationDto>()
                        } catch (e2: Exception) { null }
                    }

                    val device = try {
                        supabaseProvider.db.from("devices")
                            .select(columns = Columns.ALL) {
                                filter { eq("user_id", link.wearerId) }
                                limit(1)
                            }.decodeSingleOrNull<DeviceDto>()
                    } catch (e: Exception) { null }

                    val parsedTimestamp = parseIsoTimestamp(latestLoc?.createdAt)
                    val isRecent = System.currentTimeMillis() - parsedTimestamp < 300000L

                    val displayName = when {
                        !userProf?.full_name.isNullOrBlank() -> userProf!!.full_name
                        !userProf?.email.isNullOrBlank() -> userProf!!.email!!.substringBefore("@")
                            .split(".", "_", "-")
                            .joinToString(" ") { word -> word.lowercase().replaceFirstChar { char -> char.uppercase() } }
                        else -> "Wearer (${link.wearerId.take(6)})"
                    }

                    wearers.add(
                        WearerModel(
                            id = link.wearerId,
                            name = displayName,
                            email = userProf?.email ?: "",
                            batteryLevel = device?.batteryLevel ?: 95,
                            isGpsActive = latestLoc != null || isRecent,
                            isPendantConnected = device?.isConnected ?: true,
                            lastLocation = latestLoc?.let {
                                LocationModel(
                                    id = it.id,
                                    userId = it.userId,
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                    accuracy = it.accuracy,
                                    timestamp = parsedTimestamp
                                )
                            }
                        )
                    )
                }
                wearers
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        // Initial fetch emit
        send(fetchWearers())

        // Realtime Subscriptions for Links & Location Updates
        try {
            val channel = supabaseProvider.realtime.channel("guardian_locations_realtime")
            val linkChanges1 = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "wearer_guardian_links" }
            val linkChanges2 = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "guardian_links" }
            val locationChanges1 = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "locations" }
            val locationChanges2 = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "live_locations" }

            scope.launch { linkChanges1.collect { send(fetchWearers()) } }
            scope.launch { linkChanges2.collect { send(fetchWearers()) } }
            scope.launch { locationChanges1.collect { send(fetchWearers()) } }
            scope.launch { locationChanges2.collect { send(fetchWearers()) } }
            channel.subscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAlertInbox(): Flow<List<AlertModel>> = channelFlow {
        val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""

        suspend fun fetchAlerts(): List<AlertModel> {
            return try {
                val remoteAlerts = try {
                    supabaseProvider.db.from("alerts")
                        .select(columns = Columns.ALL) {
                            order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            limit(20)
                        }.decodeList<EmergencyAlertDto>()
                } catch (e: Exception) {
                    try {
                        supabaseProvider.db.from("emergency_alerts")
                            .select(columns = Columns.ALL) {
                                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                                limit(20)
                            }.decodeList<EmergencyAlertDto>()
                    } catch (e2: Exception) { emptyList() }
                }

                val models = remoteAlerts.map { alertDto ->
                    val timestamp = parseIsoTimestamp(alertDto.createdAt)
                    AlertModel(
                        id = alertDto.id,
                        wearerId = alertDto.wearerId,
                        wearerName = "Wearer",
                        type = try { AlertType.valueOf(alertDto.type) } catch (e: Exception) { AlertType.SOS },
                        title = alertDto.title,
                        message = alertDto.message,
                        latitude = alertDto.latitude ?: 12.97544,
                        longitude = alertDto.longitude ?: 77.59337,
                        timestamp = timestamp,
                        isRead = false,
                        isResolved = alertDto.isResolved
                    )
                }
                models
            } catch (e: Exception) {
                emptyList()
            }
        }

        // Emit initially
        send(fetchAlerts())

        // Realtime Subscription for Alerts
        try {
            val alertChannel = supabaseProvider.realtime.channel("guardian_alerts_realtime")
            val alertChanges1 = alertChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "alerts"
            }
            val alertChanges2 = alertChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "emergency_alerts"
            }

            scope.launch {
                alertChanges1.collect {
                    send(fetchAlerts())
                }
            }
            scope.launch {
                alertChanges2.collect {
                    send(fetchAlerts())
                }
            }
            alertChannel.subscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun markAlertAsRead(alertId: String) {
        alertDao.markAsRead(alertId)
    }

    private fun parseIsoTimestamp(isoString: String?): Long {
        if (isoString.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf2.parse(isoString)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}
