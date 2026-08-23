package com.rakshalink.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakshalink.data.local.entities.CachedAlertEntity
import com.rakshalink.data.local.entities.CachedLocationEntity
import com.rakshalink.data.local.entities.CachedSafeZoneEntity
import com.rakshalink.data.local.entities.EmergencyContactEntity
import com.rakshalink.data.local.entities.PendingSyncEntity
import com.rakshalink.data.local.entities.UserEntity
import com.rakshalink.data.local.entities.WearerGuardianLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>
}

@Dao
interface WearerGuardianLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<WearerGuardianLinkEntity>)

    @Query("SELECT * FROM cached_wearer_guardian_links WHERE guardianId = :guardianId AND status = 'active'")
    fun getLinkedWearersForGuardian(guardianId: String): Flow<List<WearerGuardianLinkEntity>>
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: CachedLocationEntity)

    @Query("SELECT * FROM cached_locations ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocation(): Flow<CachedLocationEntity?>

    @Query("SELECT * FROM cached_locations WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocationForUser(userId: String): Flow<CachedLocationEntity?>

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
interface EmergencyContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Query("SELECT * FROM cached_emergency_contacts WHERE wearerId = :wearerId")
    fun getContactsForWearer(wearerId: String): Flow<List<EmergencyContactEntity>>

    @Query("DELETE FROM cached_emergency_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)
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
