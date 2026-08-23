package com.rakshalink.domain.model

sealed class FallState {
    object Monitoring : FallState()
    object PossibleFallDetected : FallState()
    data class Countdown(val secondsRemaining: Int) : FallState()
    object Cancelled : FallState()
    object SosTriggered : FallState()
}

enum class PendantConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED
}
