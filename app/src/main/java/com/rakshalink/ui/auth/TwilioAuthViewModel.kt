package com.rakshalink.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshalink.domain.model.UserRole
import com.rakshalink.domain.repository.TwilioAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TwilioAuthUiState {
    object Idle : TwilioAuthUiState()
    object SendingOtp : TwilioAuthUiState()
    object OtpSent : TwilioAuthUiState()
    object Verifying : TwilioAuthUiState()
    data class Verified(val role: UserRole) : TwilioAuthUiState()
    data class Error(val message: String) : TwilioAuthUiState()
}

@HiltViewModel
class TwilioAuthViewModel @Inject constructor(
    private val twilioAuthRepository: TwilioAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwilioAuthUiState>(TwilioAuthUiState.Idle)
    val uiState: StateFlow<TwilioAuthUiState> = _uiState.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _countryCode = MutableStateFlow("+91")
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _otpDigits = MutableStateFlow(listOf("", "", "", ""))
    val otpDigits: StateFlow<List<String>> = _otpDigits.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(30)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    fun updatePhone(newPhone: String) {
        // Strip non-numeric characters for user phone input
        _phone.value = newPhone.filter { it.isDigit() }
    }

    fun updateCountryCode(code: String) {
        _countryCode.value = code
    }

    fun updateOtpDigit(index: Int, value: String) {
        if (index in 0..3) {
            val current = _otpDigits.value.toMutableList()
            val cleanVal = value.filter { it.isDigit() }

            if (cleanVal.length > 1) {
                // If user pasted a multi-digit code into a single box
                pasteOtp(cleanVal)
                return
            }

            current[index] = cleanVal.take(1)
            _otpDigits.value = current
        }
    }

    fun pasteOtp(pastedString: String) {
        val digitsOnly = pastedString.filter { it.isDigit() }.take(4)
        val newDigits = MutableList(4) { "" }
        for (i in digitsOnly.indices) {
            newDigits[i] = digitsOnly[i].toString()
        }
        _otpDigits.value = newDigits
    }

    fun fullPhoneNumber(): String {
        val code = _countryCode.value.trim()
        val num = _phone.value.trim()
        return if (num.startsWith("+")) num else "$code$num"
    }

    fun fullOtpString(): String {
        return _otpDigits.value.joinToString("")
    }

    fun requestOtp() {
        val num = _phone.value.trim()
        if (num.length < 7) {
            _uiState.value = TwilioAuthUiState.Error("Please enter a valid phone number.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TwilioAuthUiState.SendingOtp
            val fullPhone = fullPhoneNumber()
            val response = twilioAuthRepository.requestOtp(fullPhone)
            if (response.success) {
                _uiState.value = TwilioAuthUiState.OtpSent
                startCooldownTimer()
            } else {
                _uiState.value = TwilioAuthUiState.Error(response.message)
            }
        }
    }

    fun verifyOtp(role: UserRole) {
        val otp = fullOtpString()
        if (otp.length != 4) {
            _uiState.value = TwilioAuthUiState.Error("Please enter the complete 4-digit code.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TwilioAuthUiState.Verifying
            val fullPhone = fullPhoneNumber()
            val response = twilioAuthRepository.verifyOtp(fullPhone, otp, role)
            if (response.verified) {
                _uiState.value = TwilioAuthUiState.Verified(role)
            } else {
                _uiState.value = TwilioAuthUiState.Error(response.message)
            }
        }
    }

    fun resendOtp() {
        if (_isTimerRunning.value) return

        viewModelScope.launch {
            _uiState.value = TwilioAuthUiState.SendingOtp
            val fullPhone = fullPhoneNumber()
            val response = twilioAuthRepository.resendOtp(fullPhone)
            if (response.success) {
                _uiState.value = TwilioAuthUiState.OtpSent
                _otpDigits.value = listOf("", "", "", "")
                startCooldownTimer()
            } else {
                _uiState.value = TwilioAuthUiState.Error(response.message)
            }
        }
    }

    private fun startCooldownTimer() {
        timerJob?.cancel()
        _cooldownSeconds.value = 30
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_cooldownSeconds.value > 0) {
                delay(1000L)
                _cooldownSeconds.value -= 1
            }
            _isTimerRunning.value = false
        }
    }

    fun clearError() {
        if (_uiState.value is TwilioAuthUiState.Error) {
            _uiState.value = TwilioAuthUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
