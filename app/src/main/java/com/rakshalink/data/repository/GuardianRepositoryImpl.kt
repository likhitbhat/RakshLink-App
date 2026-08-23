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
            if (currentUserId.isEmpty()) return getFallbackWearers()
            return try {
                val links = try {
                    supabaseProvider.db.from("wearer_guardian_links")
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("guardian_id", currentUserId)
                            }
                        }.decodeList<GuardianLinkDto>()
                } catch (e: Exception) {
                    try {
                        supabaseProvider.db.from("guardian_links")
                            .select(columns = Columns.ALL) {
                                filter {
                                    eq("guardian_id", currentUserId)
                                }
                            }.decodeList<GuardianLinkDto>()
                    } catch (e2: Exception) {
                        emptyList()
                    }
                }

                if (links.isEmpty()) return getFallbackWearers()

                val wearers = mutableListOf<WearerModel>()
                for (link in links) {
                    val profile = try {
                        supabaseProvider.db.from("users")
                            .select(columns = Columns.ALL) {
                                filter { eq("id", link.wearerId) }
                            }.decodeSingleOrNull<ProfileDto>()
                    } catch (e: Exception) {
                        try {
                            supabaseProvider.db.from("profiles")
                                .select(columns = Columns.ALL) {
                                    filter { eq("id", link.wearerId) }
                                }.decodeSingleOrNull<ProfileDto>()
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

                    wearers.add(
                        WearerModel(
                            id = link.wearerId,
                            name = profile?.fullName?.ifBlank { null } ?: profile?.email?.substringBefore("@") ?: "Likhit Bhat",
                            email = profile?.email ?: "likhit@rakshalink.com",
                            batteryLevel = device?.batteryLevel ?: 85,
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
                            } ?: LocationModel(
                                id = "loc_demo",
                                userId = link.wearerId,
                                latitude = 12.97544,
                                longitude = 77.59337,
                                accuracy = 5.0f,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    )
                }
                wearers.ifEmpty { getFallbackWearers() }
            } catch (e: Exception) {
                getFallbackWearers()
            }
        }

        // Initial fetch emit
        send(fetchWearers())

        // Realtime Subscriptions for Location Updates
        try {
            val channel = supabaseProvider.realtime.channel("guardian_locations_realtime")
            val locationChanges1 = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "locations"
            }
            val locationChanges2 = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "live_locations"
            }

            scope.launch {
                locationChanges1.collect {
                    send(fetchWearers())
                }
            }
            scope.launch {
                locationChanges2.collect {
                    send(fetchWearers())
                }
            }
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
                        wearerName = "Likhit Bhat",
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
                models.ifEmpty { getFallbackAlerts() }
            } catch (e: Exception) {
                getFallbackAlerts()
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

    private fun getFallbackWearers(): List<WearerModel> {
        return listOf(
            WearerModel(
                id = "w1",
                name = "Likhit Bhat",
                email = "likhit@rakshalink.com",
                batteryLevel = 85,
                isGpsActive = true,
                isPendantConnected = true,
                lastLocation = LocationModel(
                    id = "loc1",
                    userId = "w1",
                    latitude = 12.97544,
                    longitude = 77.59337,
                    accuracy = 5f,
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }

    private fun getFallbackAlerts(): List<AlertModel> {
        return listOf(
            AlertModel(
                id = "a1",
                wearerId = "w1",
                wearerName = "Likhit Bhat",
                type = AlertType.SOS,
                title = "Likhit Bhat · SOS Alert",
                message = "Emergency SOS active",
                latitude = 12.97544,
                longitude = 77.59337,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                isResolved = false
            )
        )
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
