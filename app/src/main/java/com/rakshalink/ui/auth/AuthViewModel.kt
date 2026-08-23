package com.rakshalink.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshalink.domain.model.UserRole
import com.rakshalink.domain.repository.AuthRepository
import com.rakshalink.domain.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: UserRole) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object VerificationSent : AuthUiState()
    object PasswordResetSent : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _selectedRole = MutableStateFlow(UserRole.WEARER)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    suspend fun restoreUserRole(): UserRole {
        val role = authRepository.fetchOrRestoreUserRole()
        _selectedRole.value = role
        return role
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signIn(email, pass)) {
                is AuthResult.Success -> {
                    _selectedRole.value = result.data
                    _uiState.value = AuthUiState.Success(result.data)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signUp(email, pass, _selectedRole.value)) {
                is AuthResult.Success -> _uiState.value = AuthUiState.VerificationSent
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> _uiState.value = AuthUiState.PasswordResetSent
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
