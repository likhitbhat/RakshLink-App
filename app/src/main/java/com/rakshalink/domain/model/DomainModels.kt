package com.rakshalink.domain.model

data class LocationModel(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

data class SafeZoneModel(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 100f,
    val entryNotification: Boolean = true,
    val exitNotification: Boolean = true,
    val isEnabled: Boolean = true
)

data class EmergencyContactModel(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val isVerified: Boolean = false
)

enum class AlertType {
    SOS,
    FALL,
    ZONE_ENTRY,
    ZONE_EXIT,
    BATTERY_LOW,
    DISCONNECTED
}

data class AlertModel(
    val id: String = "",
    val wearerId: String = "",
    val wearerName: String = "Wearer",
    val type: AlertType = AlertType.SOS,
    val title: String = "",
    val message: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isResolved: Boolean = false
)

data class WearerModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val statusText: String = "Protected",
    val batteryLevel: Int = 100,
    val isGpsActive: Boolean = true,
    val isPendantConnected: Boolean = true,
    val lastLocation: LocationModel? = null,
    val guardianCount: Int = 1
)
