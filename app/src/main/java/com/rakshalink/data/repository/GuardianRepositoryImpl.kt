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
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuardianRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao,
    private val supabaseProvider: SupabaseClientProvider
) : GuardianRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getLinkedWearers(): Flow<List<WearerModel>> = channelFlow {
        val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        
        suspend fun fetchWearers(): List<WearerModel> {
            if (currentUserId.isEmpty()) return emptyList()
            return try {
                val links = supabaseProvider.db.from("guardian_links")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("guardian_id", currentUserId)
                            eq("status", "accepted")
                        }
                    }.decodeList<GuardianLinkDto>()

                if (links.isEmpty()) return emptyList()

                val wearers = mutableListOf<WearerModel>()
                for (link in links) {
                    val profile = try {
                        supabaseProvider.db.from("profiles")
                            .select(columns = Columns.ALL) {
                                filter { eq("id", link.wearerId) }
                            }.decodeSingleOrNull<ProfileDto>()
                    } catch (e: Exception) { null }

                    val latestLoc = try {
                        supabaseProvider.db.from("live_locations")
                            .select(columns = Columns.ALL) {
                                filter { eq("user_id", link.wearerId) }
                                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                                limit(1)
                            }.decodeSingleOrNull<LiveLocationDto>()
                    } catch (e: Exception) { null }

                    val device = try {
                        supabaseProvider.db.from("devices")
                            .select(columns = Columns.ALL) {
                                filter { eq("user_id", link.wearerId) }
                                limit(1)
                            }.decodeSingleOrNull<DeviceDto>()
                    } catch (e: Exception) { null }

                    val activeAlert = try {
                        supabaseProvider.db.from("emergency_alerts")
                            .select(columns = Columns.ALL) {
                                filter {
                                    eq("wearer_id", link.wearerId)
                                    eq("is_resolved", false)
                                }
                                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                                limit(1)
                            }.decodeSingleOrNull<EmergencyAlertDto>()
                    } catch (e: Exception) { null }

                    val parsedTimestamp = parseIsoTimestamp(latestLoc?.createdAt)
                    val isRecent = System.currentTimeMillis() - parsedTimestamp < 300000L

                    wearers.add(
                        WearerModel(
                            id = link.wearerId,
                            name = profile?.fullName?.ifBlank { null } ?: profile?.email?.substringBefore("@") ?: "Wearer",
                            email = profile?.email ?: "",
                            batteryLevel = device?.batteryLevel ?: 85,
                            isGpsActive = latestLoc != null && isRecent,
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
                emptyList()
            }
        }

        // Initial fetch
        send(fetchWearers())

        // Realtime updates subscription
        try {
            val channel = supabaseProvider.realtime.channel("guardian_wearers_updates")
            val locationChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "live_locations"
            }

            scope.launch {
                locationChanges.collect {
                    send(fetchWearers())
                }
            }
            channel.subscribe()
        } catch (e: Exception) {
            // Realtime fallback
        }
    }

    override fun getAlertInbox(): Flow<List<AlertModel>> = channelFlow {
        val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""

        suspend fun fetchAlerts(): List<AlertModel> {
            if (currentUserId.isEmpty()) return emptyList()
            return try {
                val links = supabaseProvider.db.from("guardian_links")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("guardian_id", currentUserId)
                            eq("status", "accepted")
                        }
                    }.decodeList<GuardianLinkDto>()

                val wearerIds = links.map { it.wearerId }
                if (wearerIds.isEmpty()) return emptyList()

                val remoteAlerts = supabaseProvider.db.from("emergency_alerts")
                    .select(columns = Columns.ALL) {
                        filter {
                            isIn("wearer_id", wearerIds)
                        }
                        order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }.decodeList<EmergencyAlertDto>()

                val models = remoteAlerts.map { alertDto ->
                    val profile = try {
                        supabaseProvider.db.from("profiles")
                            .select(columns = Columns.ALL) {
                                filter { eq("id", alertDto.wearerId) }
                            }.decodeSingleOrNull<ProfileDto>()
                    } catch (e: Exception) { null }

                    val wearerName = profile?.fullName?.ifBlank { null } ?: "Wearer"
                    val timestamp = parseIsoTimestamp(alertDto.createdAt)

                    // Cache in Room
                    try {
                        alertDao.insertAlert(
                            CachedAlertEntity(
                                id = alertDto.id,
                                wearerId = alertDto.wearerId,
                                wearerName = wearerName,
                                type = alertDto.type,
                                title = alertDto.title,
                                message = alertDto.message,
                                latitude = alertDto.latitude ?: 0.0,
                                longitude = alertDto.longitude ?: 0.0,
                                timestamp = timestamp,
                                isRead = false,
                                isResolved = alertDto.isResolved
                            )
                        )
                    } catch (e: Exception) {}

                    AlertModel(
                        id = alertDto.id,
                        wearerId = alertDto.wearerId,
                        wearerName = wearerName,
                        type = try { AlertType.valueOf(alertDto.type) } catch (e: Exception) { AlertType.SOS },
                        title = alertDto.title,
                        message = alertDto.message,
                        latitude = alertDto.latitude ?: 0.0,
                        longitude = alertDto.longitude ?: 0.0,
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

        // Realtime subscription for alerts
        try {
            val alertChannel = supabaseProvider.realtime.channel("guardian_alerts_updates")
            val alertChanges = alertChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "emergency_alerts"
            }

            scope.launch {
                alertChanges.collect {
                    send(fetchAlerts())
                }
            }
            alertChannel.subscribe()
        } catch (e: Exception) {
            // Fallback
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

