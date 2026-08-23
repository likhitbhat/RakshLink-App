package com.rakshalink.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class UserRoleDto(
    @SerialName("user_id") val userId: String = "",
    val role: String = "wearer"
)

@Serializable
data class DeviceDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("device_name") val deviceName: String = "RakshaLink Pendant",
    @SerialName("battery_level") val batteryLevel: Int = 100,
    @SerialName("is_connected") val isConnected: Boolean = true
)

@Serializable
data class LiveLocationDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class SafeZoneDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("radius_meters") val radiusMeters: Float = 100f,
    @SerialName("entry_notification") val entryNotification: Boolean = true,
    @SerialName("exit_notification") val exitNotification: Boolean = true,
    @SerialName("is_enabled") val isEnabled: Boolean = true
)

@Serializable
data class EmergencyContactDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    val relationship: String = "",
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class EmergencyAlertDto(
    val id: String = "",
    @SerialName("wearer_id") val wearerId: String = "",
    val type: String = "SOS",
    val title: String = "",
    val message: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("is_resolved") val isResolved: Boolean = false
)

@Serializable
data class SosAttemptDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("was_false_alarm") val wasFalseAlarm: Boolean = false,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class PushSubscriptionDto(
    @SerialName("user_id") val userId: String = "",
    @SerialName("fcm_token") val fcmToken: String = ""
)

@Serializable
data class GuardianLinkDto(
    val id: String = "",
    @SerialName("wearer_id") val wearerId: String = "",
    @SerialName("guardian_id") val guardianId: String = "",
    val role: String = "secondary",
    val status: String = "active",
    @SerialName("linked_at") val linkedAt: String = ""
)

@Serializable
data class GuardianInviteDto(
    val id: String = "",
    @SerialName("wearer_id") val wearerId: String = "",
    @SerialName("invitee_contact") val inviteeContact: String = "",
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("expires_at") val expiresAt: String = ""
)

@Serializable
data class WearerGuardianLinkDto(
    val id: String = "",
    @SerialName("wearer_id") val wearerId: String = "",
    @SerialName("guardian_id") val guardianId: String = "",
    val role: String = "secondary",
    val status: String = "active",
    @SerialName("linked_at") val linkedAt: String = ""
)

@Serializable
data class UserPreferencesDto(
    @SerialName("user_id") val userId: String = "",
    @SerialName("push_enabled") val pushEnabled: Boolean = true,
    @SerialName("share_location_enabled") val shareLocationEnabled: Boolean = true,
    val theme: String = "dark",
    val language: String = "en",
    @SerialName("alert_sound_enabled") val alertSoundEnabled: Boolean = true,
    @SerialName("vibration_enabled") val vibrationEnabled: Boolean = true,
    @SerialName("alert_volume") val alertVolume: Int = 80
)

@Serializable
data class ZoneEventDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("zone_id") val zoneId: String = "",
    @SerialName("event_type") val eventType: String = "enter",
    @SerialName("created_at") val createdAt: String = ""
)

