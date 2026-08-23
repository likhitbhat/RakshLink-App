package com.rakshalink.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rakshalink.data.local.dao.AlertDao
import com.rakshalink.data.local.dao.EmergencyContactDao
import com.rakshalink.data.local.dao.LocationDao
import com.rakshalink.data.local.dao.PendingSyncDao
import com.rakshalink.data.local.dao.SafeZoneDao
import com.rakshalink.data.local.dao.UserDao
import com.rakshalink.data.local.dao.WearerGuardianLinkDao
import com.rakshalink.data.local.entities.CachedAlertEntity
import com.rakshalink.data.local.entities.CachedLocationEntity
import com.rakshalink.data.local.entities.CachedSafeZoneEntity
import com.rakshalink.data.local.entities.EmergencyContactEntity
import com.rakshalink.data.local.entities.PendingSyncEntity
import com.rakshalink.data.local.entities.UserEntity
import com.rakshalink.data.local.entities.WearerGuardianLinkEntity

@Database(
    entities = [
        UserEntity::class,
        WearerGuardianLinkEntity::class,
        CachedLocationEntity::class,
        CachedAlertEntity::class,
        CachedSafeZoneEntity::class,
        EmergencyContactEntity::class,
        PendingSyncEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RakshaLinkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun wearerGuardianLinkDao(): WearerGuardianLinkDao
    abstract fun locationDao(): LocationDao
    abstract fun alertDao(): AlertDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun pendingSyncDao(): PendingSyncDao
}
