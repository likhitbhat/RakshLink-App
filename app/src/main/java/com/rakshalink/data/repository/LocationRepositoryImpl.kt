package com.rakshalink.data.repository

import com.rakshalink.data.local.dao.LocationDao
import com.rakshalink.data.local.entities.CachedLocationEntity
import com.rakshalink.data.remote.dto.LiveLocationDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.LocationModel
import com.rakshalink.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val supabaseProvider: SupabaseClientProvider
) : LocationRepository {

    override fun getLatestLocation(): Flow<LocationModel?> {
        return locationDao.getLatestLocation().map { entity ->
            entity?.let {
                LocationModel(
                    id = it.id,
                    userId = it.userId,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    timestamp = it.timestamp,
                    isSynced = it.isSynced
                )
            }
        }
    }

    override suspend fun saveLocation(location: LocationModel) {
        val id = if (location.id.isEmpty()) UUID.randomUUID().toString() else location.id
        val resolvedUserId = if (location.userId.isBlank()) {
            supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        } else {
            location.userId
        }
        var isSynced = false

        try {
            if (resolvedUserId.isNotEmpty()) {
                val dto = LiveLocationDto(
                    id = id,
                    userId = resolvedUserId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                try { supabaseProvider.db.from("locations").insert(dto) } catch (e: Exception) {}
                try { supabaseProvider.db.from("live_locations").insert(dto) } catch (e: Exception) {}
                isSynced = true
            }
        } catch (e: Exception) {
            isSynced = false
        }

        val entity = CachedLocationEntity(
            id = id,
            userId = resolvedUserId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = location.timestamp,
            isSynced = isSynced
        )
        locationDao.insertLocation(entity)
    }

    override suspend fun syncPendingLocations() {
        val unsynced = locationDao.getUnsyncedLocations()
        if (unsynced.isEmpty()) return

        val syncedIds = mutableListOf<String>()
        for (item in unsynced) {
            try {
                val dto = LiveLocationDto(
                    id = item.id,
                    userId = item.userId,
                    latitude = item.latitude,
                    longitude = item.longitude,
                    accuracy = item.accuracy
                )
                supabaseProvider.db.from("live_locations").insert(dto)
                syncedIds.add(item.id)
            } catch (e: Exception) {
                // Continue trying remaining
            }
        }

        if (syncedIds.isNotEmpty()) {
            locationDao.markAsSynced(syncedIds)
        }
    }
}
