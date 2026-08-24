package com.rakshalink.data.repository

import com.rakshalink.data.local.dao.SafeZoneDao
import com.rakshalink.data.local.entities.CachedSafeZoneEntity
import com.rakshalink.data.remote.dto.SafeZoneDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.SafeZoneModel
import com.rakshalink.domain.repository.SafeZoneRepository
import com.rakshalink.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeZoneRepositoryImpl @Inject constructor(
    private val safeZoneDao: SafeZoneDao,
    private val supabaseProvider: SupabaseClientProvider,
    private val userPreferencesManager: UserPreferencesManager
) : SafeZoneRepository {

    private suspend fun resolveCurrentUserId(): String {
        val supabaseUid = try { supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: "" } catch (e: Exception) { "" }
        if (supabaseUid.isNotEmpty()) return supabaseUid
        val storedUid = try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }
        if (storedUid.isNotEmpty()) return storedUid
        return ""
    }

    override fun getActiveSafeZones(): Flow<List<SafeZoneModel>> {
        return safeZoneDao.getActiveSafeZones().map { list ->
            list.map {
                SafeZoneModel(
                    id = it.id,
                    userId = it.userId,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radiusMeters = it.radiusMeters,
                    entryNotification = it.entryNotification,
                    exitNotification = it.exitNotification,
                    isEnabled = it.isEnabled
                )
            }
        }
    }

    override suspend fun addSafeZone(zone: SafeZoneModel) {
        val currentUserId = resolveCurrentUserId()
        val id = if (zone.id.isEmpty()) UUID.randomUUID().toString() else zone.id
        val resolvedUserId = if (zone.userId.isBlank()) currentUserId else zone.userId

        val entity = CachedSafeZoneEntity(
            id = id,
            userId = resolvedUserId,
            name = zone.name,
            latitude = zone.latitude,
            longitude = zone.longitude,
            radiusMeters = zone.radiusMeters,
            entryNotification = zone.entryNotification,
            exitNotification = zone.exitNotification,
            isEnabled = zone.isEnabled
        )
        safeZoneDao.insertSafeZones(listOf(entity))

        if (resolvedUserId.isNotEmpty()) {
            try {
                val dto = SafeZoneDto(
                    id = id,
                    userId = resolvedUserId,
                    name = zone.name,
                    latitude = zone.latitude,
                    longitude = zone.longitude,
                    radiusMeters = zone.radiusMeters,
                    entryNotification = zone.entryNotification,
                    exitNotification = zone.exitNotification,
                    isEnabled = zone.isEnabled
                )
                supabaseProvider.db.from("safe_zones").insert(dto)
            } catch (e: Exception) {
                // Error handling fallback
            }
        }
    }

    override suspend fun updateSafeZone(zone: SafeZoneModel) {
        addSafeZone(zone)
    }

    override suspend fun deleteSafeZone(id: String) {
        safeZoneDao.deleteSafeZone(id)
        try {
            supabaseProvider.db.from("safe_zones").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            // Delete fallback
        }
    }
}
