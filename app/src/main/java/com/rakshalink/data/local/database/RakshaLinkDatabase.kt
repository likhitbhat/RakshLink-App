package com.rakshalink.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rakshalink.data.local.dao.AlertDao
import com.rakshalink.data.local.dao.LocationDao
import com.rakshalink.data.local.dao.PendingSyncDao
import com.rakshalink.data.local.dao.SafeZoneDao
import com.rakshalink.data.local.entities.CachedAlertEntity
import com.rakshalink.data.local.entities.CachedLocationEntity
import com.rakshalink.data.local.entities.CachedSafeZoneEntity
import com.rakshalink.data.local.entities.PendingSyncEntity

@Database(
    entities = [
        CachedLocationEntity::class,
        CachedAlertEntity::class,
        CachedSafeZoneEntity::class,
        PendingSyncEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RakshaLinkDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun alertDao(): AlertDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun pendingSyncDao(): PendingSyncDao
}
