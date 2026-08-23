package com.rakshalink.domain.repository

import com.rakshalink.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLatestLocation(): Flow<LocationModel?>
    suspend fun saveLocation(location: LocationModel)
    suspend fun syncPendingLocations()
}
