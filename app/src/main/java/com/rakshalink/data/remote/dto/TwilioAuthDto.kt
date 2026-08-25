package com.rakshalink.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val phone: String
)

@Serializable
data class SendOtpResponse(
    val success: Boolean = false,
    val message: String = ""
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

@Serializable
data class VerifyOtpResponse(
    val success: Boolean = false,
    val verified: Boolean = false,
    val message: String = ""
)

@Serializable
data class UserProfileDto(
    val id: String = "",
    val email: String? = null,
    val phone: String? = null,
    val full_name: String = "",
    val role: String = "wearer",
    val wearer_code: String? = null,
    val session_device_token: String? = null
)

fun generatePermanentWearerCode(identifier: String): String {
    val cleaned = identifier.trim().lowercase()
    if (cleaned.isBlank()) return "RL-000000-WK"
    return try {
        val digest = java.security.MessageDigest.getInstance("MD5").digest(cleaned.toByteArray())
        val hex = digest.joinToString("") { "%02X".format(it) }
        "RL-${hex.take(6)}-WK"
    } catch (e: Exception) {
        "RL-${cleaned.take(6).uppercase()}-WK"
    }
}

@Serializable
data class SendInviteRequest(
    val wearerId: String = "",
    val wearerName: String = "",
    val inviteeContact: String = "",
    val inviteId: String = ""
)
