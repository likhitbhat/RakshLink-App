package com.rakshalink.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class InactivityState {
    object Active : InactivityState()
    data class Warning(val secondsRemaining: Int) : InactivityState()
    object TimedOut : InactivityState()
}

@Singleton
class InactivityTracker @Inject constructor() {

    private val _state = MutableStateFlow<InactivityState>(InactivityState.Active)
    val state: StateFlow<InactivityState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var idleJob: Job? = null

    companion object {
        const val IDLE_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
        const val WARNING_COUNTDOWN_SECONDS = 60
    }

    fun startTracking(onSignOutRequired: () -> Unit) {
        resetUserInteraction(onSignOutRequired)
    }

    fun resetUserInteraction(onSignOutRequired: () -> Unit) {
        if (_state.value is InactivityState.TimedOut) return
        _state.value = InactivityState.Active
        idleJob?.cancel()

        idleJob = scope.launch {
            delay(IDLE_TIMEOUT_MS)
            // Enter warning 60s countdown
            for (seconds in WARNING_COUNTDOWN_SECONDS downTo 1) {
                if (_state.value is InactivityState.Active) break
                _state.value = InactivityState.Warning(secondsRemaining = seconds)
                delay(1000L)
            }

            if (_state.value is InactivityState.Warning) {
                _state.value = InactivityState.TimedOut
                onSignOutRequired()
            }
        }
    }

    fun staySignedIn(onSignOutRequired: () -> Unit) {
        resetUserInteraction(onSignOutRequired)
    }
}
