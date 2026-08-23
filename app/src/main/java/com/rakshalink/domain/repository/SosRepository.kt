package com.rakshalink.domain.repository

import com.rakshalink.domain.model.SosState
import kotlinx.coroutines.flow.StateFlow

interface SosRepository {
    val sosState: StateFlow<SosState>
    fun startPressing()
    fun cancelPressing()
    fun armSos()
    fun showConfirmation()
    suspend fun triggerActiveSos(latitude: Double?, longitude: Double?): String
    suspend fun resolveSos(alertId: String, wasFalseAlarm: Boolean)
    fun startCooldown(seconds: Int = 30)
}
