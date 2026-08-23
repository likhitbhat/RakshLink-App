package com.rakshalink.data.repository

import com.rakshalink.data.preferences.UserPreferencesManager
import com.rakshalink.data.remote.api.TwilioAuthApi
import com.rakshalink.data.remote.dto.SendOtpResponse
import com.rakshalink.data.remote.dto.VerifyOtpResponse
import com.rakshalink.domain.model.UserRole
import com.rakshalink.domain.repository.TwilioAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwilioAuthRepositoryImpl @Inject constructor(
    private val twilioAuthApi: TwilioAuthApi,
    private val userPreferencesManager: UserPreferencesManager
) : TwilioAuthRepository {

    override suspend fun requestOtp(phone: String): SendOtpResponse {
        return twilioAuthApi.sendOtp(phone)
    }

    override suspend fun verifyOtp(phone: String, otp: String, role: UserRole): VerifyOtpResponse {
        val response = twilioAuthApi.verifyOtp(phone, otp)
        if (response.verified) {
            // Save authenticated user role in local encrypted preferences
            userPreferencesManager.setUserRole(role.name.lowercase())
        }
        return response
    }

    override suspend fun resendOtp(phone: String): SendOtpResponse {
        return twilioAuthApi.resendOtp(phone)
    }
}
