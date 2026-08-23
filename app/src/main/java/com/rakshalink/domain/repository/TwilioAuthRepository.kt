package com.rakshalink.domain.repository

import com.rakshalink.data.remote.dto.SendOtpResponse
import com.rakshalink.data.remote.dto.VerifyOtpResponse
import com.rakshalink.domain.model.UserRole

interface TwilioAuthRepository {
    suspend fun requestOtp(phone: String): SendOtpResponse
    suspend fun verifyOtp(phone: String, otp: String, role: UserRole): VerifyOtpResponse
    suspend fun resendOtp(phone: String): SendOtpResponse
}
