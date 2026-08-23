package com.rakshalink.domain.model

sealed class SosState {
    object Idle : SosState()
    object Pressing : SosState()
    object Armed : SosState()
    object Confirmation : SosState()
    data class Active(val alertId: String, val timestampMs: Long) : SosState()
    data class Resolved(val wasFalseAlarm: Boolean) : SosState()
    data class Cooldown(val secondsRemaining: Int) : SosState()
}
