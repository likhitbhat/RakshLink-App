package com.rakshalink.data.repository

import com.rakshalink.data.preferences.UserPreferencesManager
import com.rakshalink.data.remote.api.TwilioAuthApi
import com.rakshalink.data.remote.dto.SendOtpResponse
import com.rakshalink.data.remote.dto.UserProfileDto
import com.rakshalink.data.remote.dto.VerifyOtpResponse
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.UserRole
import com.rakshalink.domain.repository.TwilioAuthRepository
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwilioAuthRepositoryImpl @Inject constructor(
    private val twilioAuthApi: TwilioAuthApi,
    private val userPreferencesManager: UserPreferencesManager,
    private val supabaseProvider: SupabaseClientProvider
) : TwilioAuthRepository {

    override suspend fun requestOtp(phone: String): SendOtpResponse {
        return twilioAuthApi.sendOtp(phone)
    }

    override suspend fun verifyOtp(phone: String, otp: String, role: UserRole): VerifyOtpResponse {
        val response = twilioAuthApi.verifyOtp(phone, otp)
        if (response.verified) {
            val userId = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()
            val randomDigits = (1000..9999).random()
            val wearerCode = "RL-$randomDigits-WK"
            val roleStr = role.name.lowercase()

            // 1. Store session locally in DataStore
            userPreferencesManager.saveAuthSession(
                userId = userId,
                phone = phone,
                role = roleStr
            )

            // 2. Insert or update user profile row in Supabase
            try {
                val profile = UserProfileDto(
                    id = userId,
                    phone = phone,
                    full_name = if (role == UserRole.GUARDIAN) "Guardian User" else "Wearer User",
                    role = roleStr,
                    wearer_code = wearerCode
                )
                supabaseProvider.db.from("users").upsert(profile)
            } catch (e: Exception) {
                // If Supabase network insert fails (e.g. offline/placeholder credentials), local session persists cleanly
            }
        }
        return response
    }

    override suspend fun resendOtp(phone: String): SendOtpResponse {
        return twilioAuthApi.resendOtp(phone)
    }
}
