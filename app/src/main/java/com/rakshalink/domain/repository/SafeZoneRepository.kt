package com.rakshalink.domain.repository

import com.rakshalink.domain.model.SafeZoneModel
import kotlinx.coroutines.flow.Flow

interface SafeZoneRepository {
    fun getActiveSafeZones(): Flow<List<SafeZoneModel>>
    suspend fun addSafeZone(zone: SafeZoneModel)
    suspend fun updateSafeZone(zone: SafeZoneModel)
    suspend fun deleteSafeZone(id: String)
}
