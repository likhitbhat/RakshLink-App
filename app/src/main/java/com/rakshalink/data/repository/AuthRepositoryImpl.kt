package com.rakshalink.data.repository

import com.rakshalink.data.preferences.UserPreferencesManager
import com.rakshalink.data.remote.dto.UserProfileDto
import com.rakshalink.data.remote.dto.UserRoleDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.UserRole
import com.rakshalink.domain.repository.AuthRepository
import com.rakshalink.domain.repository.AuthResult
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val userPreferencesManager: UserPreferencesManager
) : AuthRepository {

    override fun currentUserRole(): Flow<UserRole> {
        return userPreferencesManager.userRoleFlow.map { roleStr ->
            UserRole.fromString(roleStr)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return try {
            supabaseProvider.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return try {
            supabaseProvider.auth.currentSessionOrNull()?.user?.id
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun fetchOrRestoreUserRole(): UserRole {
        val userId = getCurrentUserId() ?: return UserRole.WEARER
        return try {
            val roleDto = supabaseProvider.db.from("user_roles")
                .select(columns = Columns.ALL) {
                    filter { eq("user_id", userId) }
                }.decodeSingleOrNull<UserRoleDto>()
            val role = UserRole.fromString(roleDto?.role)
            userPreferencesManager.setUserRole(role.name.lowercase())
            role
        } catch (e: Exception) {
            UserRole.WEARER
        }
    }

    override suspend fun signUp(email: String, password: String, role: UserRole): AuthResult<Unit> {
        return try {
            // Security check: Check if email is already registered as a different role
            val existingProfile = try {
                supabaseProvider.db.from("users")
                    .select(columns = Columns.ALL) {
                        filter { eq("email", email) }
                        limit(1)
                    }.decodeSingleOrNull<UserProfileDto>()
            } catch (e: Exception) { null }

            if (existingProfile != null) {
                val registeredRole = UserRole.fromString(existingProfile.role)
                if (registeredRole != role) {
                    return AuthResult.Error(
                        "Security Violation: This email (${email}) is registered as a ${registeredRole.name.uppercase()} account. You cannot sign up as a ${role.name.uppercase()} using the same email."
                    )
                }
            }

            supabaseProvider.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id
            if (userId != null) {
                val roleStr = role.name.lowercase()
                try {
                    val roleDto = UserRoleDto(userId = userId, role = roleStr)
                    supabaseProvider.db.from("user_roles").insert(roleDto)
                } catch (e: Exception) {}

                try {
                    val profile = UserProfileDto(
                        id = userId,
                        email = email,
                        full_name = email.substringBefore("@").split(".", "_", "-").joinToString(" ") { word -> word.lowercase().replaceFirstChar { char -> char.uppercase() } },
                        role = roleStr,
                        wearer_code = "RL-${userId.take(4).uppercase()}-WK"
                    )
                    supabaseProvider.db.from("users").upsert(profile)
                } catch (e: Exception) {}
                userPreferencesManager.saveAuthSession(userId = userId, phone = email, role = roleStr)
            }
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign up failed")
        }
    }

    override suspend fun signIn(email: String, password: String, expectedRole: UserRole?): AuthResult<UserRole> {
        return try {
            supabaseProvider.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id
            var role = UserRole.WEARER

            if (userId != null) {
                // Fetch existing profile to determine registered role
                val existingProfile = try {
                    supabaseProvider.db.from("users")
                        .select(columns = Columns.ALL) {
                            filter { eq("id", userId) }
                            limit(1)
                        }.decodeSingleOrNull<UserProfileDto>()
                } catch (e: Exception) { null }

                val registeredRoleStr = existingProfile?.role ?: try {
                    val roleDto = supabaseProvider.db.from("user_roles")
                        .select(columns = Columns.ALL) {
                            filter { eq("user_id", userId) }
                        }.decodeSingleOrNull<UserRoleDto>()
                    roleDto?.role
                } catch (e: Exception) { null }

                if (registeredRoleStr != null) {
                    role = UserRole.fromString(registeredRoleStr)
                }

                // STRICT SECURITY ROLE LOCK ENFORCEMENT
                if (expectedRole != null && role != expectedRole) {
                    // Sign out immediately to protect user session
                    supabaseProvider.auth.signOut()
                    userPreferencesManager.clearAuthSession()
                    return AuthResult.Error(
                        "Security Violation: This email (${email}) is registered as a ${role.name.uppercase()} account. You cannot log in as a ${expectedRole.name.uppercase()} using this email."
                    )
                }

                val roleStr = role.name.lowercase()
                val resolvedName = when {
                    !existingProfile?.full_name.isNullOrBlank() -> existingProfile!!.full_name
                    email.contains("@") -> email.substringBefore("@")
                        .split(".", "_", "-")
                        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { char -> char.uppercase() } }
                    else -> email
                }

                try {
                    val profile = UserProfileDto(
                        id = userId,
                        email = email,
                        full_name = resolvedName,
                        role = roleStr,
                        wearer_code = existingProfile?.wearer_code ?: "RL-${userId.take(4).uppercase()}-WK"
                    )
                    supabaseProvider.db.from("users").upsert(profile)
                } catch (e: Exception) {}

                userPreferencesManager.saveAuthSession(userId = userId, phone = email, role = roleStr)
            }
            AuthResult.Success(role)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign in failed")
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            supabaseProvider.auth.signOut()
            userPreferencesManager.clearAuthSession()
            userPreferencesManager.setUserRole("wearer")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            try { userPreferencesManager.clearAuthSession() } catch (e2: Exception) {}
            userPreferencesManager.setUserRole("wearer")
            AuthResult.Success(Unit)
        }
    }


    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            supabaseProvider.auth.resetPasswordForEmail(email)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Failed to send reset email")
        }
    }

    override suspend fun resendEmailVerification(email: String): AuthResult<Unit> {
        return try {
            supabaseProvider.auth.resendEmail(io.github.jan.supabase.auth.OtpType.Email.SIGNUP, email)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Failed to resend verification")
        }
    }
}
