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
