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

            // Security check: Check if phone is already registered as a different role
            val existingProfile = try {
                supabaseProvider.db.from("users")
                    .select(columns = Columns.ALL) {
                        filter {
                            or {
                                eq("id", userId)
                                eq("phone", phone)
                            }
                        }
                        limit(1)
                    }.decodeSingleOrNull<UserProfileDto>()
            } catch (e: Exception) { null }

            if (existingProfile != null) {
                val registeredRole = UserRole.fromString(existingProfile.role)
                if (registeredRole != role) {
                    return VerifyOtpResponse(
                        success = false,
                        verified = false,
                        message = "Security Error: Phone $phone is registered as a ${registeredRole.name.uppercase()} account. You cannot log in as a ${role.name.uppercase()}."
                    )
                }
            }

            val permanentCode = when {
                !existingProfile?.wearer_code.isNullOrBlank() -> existingProfile!!.wearer_code!!
                else -> com.rakshalink.data.remote.dto.generatePermanentWearerCode(phone)
            }
            val roleStr = role.name.lowercase()
            val sessionToken = UUID.randomUUID().toString()

            // 1. Store session locally in DataStore
            userPreferencesManager.saveAuthSession(
                userId = userId,
                phone = phone,
                role = roleStr,
                sessionToken = sessionToken
            )

            // 2. Insert or update user profile row in Supabase
            try {
                val profile = UserProfileDto(
                    id = userId,
                    phone = phone,
                    full_name = if (role == UserRole.GUARDIAN) "Guardian User" else "Wearer User",
                    role = roleStr,
                    wearer_code = permanentCode,
                    session_device_token = sessionToken
                )
                supabaseProvider.db.from("users").upsert(profile)
            } catch (e: Exception) {}
        }
        return response
    }

    override suspend fun resendOtp(phone: String): SendOtpResponse {
        return twilioAuthApi.resendOtp(phone)
    }
}
