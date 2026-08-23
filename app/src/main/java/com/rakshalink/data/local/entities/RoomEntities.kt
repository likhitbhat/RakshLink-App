package com.rakshalink.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_locations")
data class CachedLocationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "cached_alerts")
data class CachedAlertEntity(
    @PrimaryKey val id: String,
    val wearerId: String,
    val wearerName: String,
    val type: String,
    val title: String,
    val message: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val isRead: Boolean,
    val isResolved: Boolean = false
)

@Entity(tableName = "cached_safe_zones")
data class CachedSafeZoneEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val entryNotification: Boolean,
    val exitNotification: Boolean,
    val isEnabled: Boolean
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true) val syncId: Long = 0,
    val itemType: String, // "location", "sos_attempt", "zone_event"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
