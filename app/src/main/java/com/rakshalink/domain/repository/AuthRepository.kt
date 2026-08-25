package com.rakshalink.domain.repository

import com.rakshalink.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

interface AuthRepository {
    fun currentUserRole(): Flow<UserRole>
    fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUserId(): String?
    suspend fun fetchOrRestoreUserRole(): UserRole
    suspend fun signUp(email: String, password: String, role: UserRole): AuthResult<Unit>
    suspend fun signIn(email: String, password: String, expectedRole: UserRole? = null): AuthResult<UserRole>
    suspend fun signOut(): AuthResult<Unit>
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>
    suspend fun resendEmailVerification(email: String): AuthResult<Unit>
}

