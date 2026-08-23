package com.rakshalink.data.remote.api

import com.rakshalink.data.remote.dto.SendOtpRequest
import com.rakshalink.data.remote.dto.SendOtpResponse
import com.rakshalink.data.remote.dto.VerifyOtpRequest
import com.rakshalink.data.remote.dto.VerifyOtpResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwilioAuthApi @Inject constructor() {

    private var baseUrl: String = com.rakshalink.BuildConfig.TWILIO_BACKEND_URL.ifBlank { "https://rakshlink-app.onrender.com" }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    fun setBaseUrl(url: String) {
        if (url.isNotBlank()) {
            baseUrl = url.removeSuffix("/")
        }
    }

    suspend fun sendOtp(phone: String): SendOtpResponse {
        return try {
            val response = client.post("$baseUrl/api/auth/send-otp") {
                contentType(ContentType.Application.Json)
                setBody(SendOtpRequest(phone = phone))
            }
            response.body<SendOtpResponse>()
        } catch (e: Exception) {
            SendOtpResponse(
                success = false,
                message = e.localizedMessage ?: "Unable to connect to server. Please check your internet connection."
            )
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): VerifyOtpResponse {
        return try {
            val response = client.post("$baseUrl/api/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(VerifyOtpRequest(phone = phone, otp = otp))
            }
            response.body<VerifyOtpResponse>()
        } catch (e: Exception) {
            VerifyOtpResponse(
                success = false,
                verified = false,
                message = e.localizedMessage ?: "Unable to connect to server. Please try again."
            )
        }
    }

    suspend fun resendOtp(phone: String): SendOtpResponse {
        return try {
            val response = client.post("$baseUrl/api/auth/resend-otp") {
                contentType(ContentType.Application.Json)
                setBody(SendOtpRequest(phone = phone))
            }
            response.body<SendOtpResponse>()
        } catch (e: Exception) {
            SendOtpResponse(
                success = false,
                message = e.localizedMessage ?: "Unable to resend OTP. Please try again."
            )
        }
    }

    suspend fun sendGuardianInvite(
        wearerId: String,
        wearerName: String,
        inviteeContact: String,
        inviteId: String
    ): SendOtpResponse {
        return try {
            val response = client.post("$baseUrl/api/guardian/send-invite") {
                contentType(ContentType.Application.Json)
                setBody(
                    com.rakshalink.data.remote.dto.SendInviteRequest(
                        wearerId = wearerId,
                        wearerName = wearerName,
                        inviteeContact = inviteeContact,
                        inviteId = inviteId
                    )
                )
            }
            response.body<SendOtpResponse>()
        } catch (e: Exception) {
            SendOtpResponse(
                success = false,
                message = e.localizedMessage ?: "Unable to dispatch SMS invite."
            )
        }
    }
}
