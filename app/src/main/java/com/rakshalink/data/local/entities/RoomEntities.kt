package com.rakshalink.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
/ 1. Cached Users Entity (Mirrors Supabase 'users' / 'profiles' table)
 */
@Entity(tableName = "cached_users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val fullName: String,
    val role: String, // "wearer" or "guardian"
    val avatarUrl: String?,
    val wearerCode: String?,
    val createdAt: Long = System.currentTimeMillis()
)

/**
/ 2. Cached Wearer-Guardian Link Entity (Mirrors Supabase 'wearer_guardian_links' table)
 */
@Entity(tableName = "cached_wearer_guardian_links")
data class WearerGuardianLinkEntity(
    @PrimaryKey val id: String,
    val wearerId: String,
    val guardianId: String,
    val relationshipLabel: String,
    val isPrimary: Boolean,
    val status: String // "active", "pending"
)

/**
/ 3. Cached Location Entity (Mirrors Supabase 'locations' table)
 */
@Entity(tableName = "cached_locations")
data class CachedLocationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val streetAddress: String? = null,
    val speed: Float = 0f,
    val heading: Float = 0f,
    val isOnline: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

/**
/ 4. Cached Alert Entity (Mirrors Supabase 'alerts' table)
 */
@Entity(tableName = "cached_alerts")
data class CachedAlertEntity(
    @PrimaryKey val id: String,
    val wearerId: String,
    val wearerName: String,
    val type: String, // "SOS", "FALL", "ZONE_EXIT", "BATTERY_LOW"
    val title: String,
    val message: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isResolved: Boolean = false
)

/**
/ 5. Cached Safe Zone Entity (Mirrors Supabase 'safe_zones' table)
 */
@Entity(tableName = "cached_safe_zones")
data class CachedSafeZoneEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val entryNotification: Boolean = true,
    val exitNotification: Boolean = true,
    val isEnabled: Boolean = true
)

/**
/ 6. Cached Emergency Contact Entity (Mirrors Supabase 'emergency_contacts' table)
 */
@Entity(tableName = "cached_emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val wearerId: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val isVerified: Boolean = false
)

/**
/ 7. Pending Sync Queue Entity (Offline Sync Queue)
 */
@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true) val syncId: Long = 0,
    val itemType: String, // "location", "sos_attempt", "zone_event"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
