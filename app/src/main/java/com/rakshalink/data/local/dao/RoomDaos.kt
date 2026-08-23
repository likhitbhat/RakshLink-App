package com.rakshalink.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakshalink.data.local.entities.CachedAlertEntity
import com.rakshalink.data.local.entities.CachedLocationEntity
import com.rakshalink.data.local.entities.CachedSafeZoneEntity
import com.rakshalink.data.local.entities.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: CachedLocationEntity)

    @Query("SELECT * FROM cached_locations ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocation(): Flow<CachedLocationEntity?>

    @Query("SELECT * FROM cached_locations WHERE isSynced = 0")
    suspend fun getUnsyncedLocations(): List<CachedLocationEntity>

    @Query("UPDATE cached_locations SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<CachedAlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CachedAlertEntity)

    @Query("SELECT * FROM cached_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<CachedAlertEntity>>

    @Query("UPDATE cached_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: String)
}

@Dao
interface SafeZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeZones(zones: List<CachedSafeZoneEntity>)

    @Query("SELECT * FROM cached_safe_zones WHERE isEnabled = 1")
    fun getActiveSafeZones(): Flow<List<CachedSafeZoneEntity>>

    @Query("DELETE FROM cached_safe_zones WHERE id = :id")
    suspend fun deleteSafeZone(id: String)
}

@Dao
interface PendingSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(item: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getAllPendingSync(): List<PendingSyncEntity>

    @Query("DELETE FROM pending_sync WHERE syncId IN (:ids)")
    suspend fun deletePendingSync(ids: List<Long>)
}
