package com.rakshalink.data.repository

import com.rakshalink.data.remote.dto.EmergencyAlertDto
import com.rakshalink.data.remote.dto.SosAttemptDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.SosState
import com.rakshalink.domain.repository.SosRepository
import com.rakshalink.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val userPreferencesManager: UserPreferencesManager
) : SosRepository {

    private val _sosState = MutableStateFlow<SosState>(SosState.Idle)
    override val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private suspend fun resolveCurrentUserId(): String {
        val supabaseUid = try { supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: "" } catch (e: Exception) { "" }
        if (supabaseUid.isNotEmpty()) return supabaseUid
        val storedUid = try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }
        if (storedUid.isNotEmpty()) return storedUid
        return ""
    }

    override fun startPressing() {
        if (_sosState.value is SosState.Idle) {
            _sosState.value = SosState.Pressing
        }
    }

    override fun cancelPressing() {
        if (_sosState.value is SosState.Pressing) {
            _sosState.value = SosState.Idle
        }
    }

    override fun armSos() {
        _sosState.value = SosState.Armed
    }

    override fun showConfirmation() {
        _sosState.value = SosState.Confirmation
    }

    override suspend fun triggerActiveSos(latitude: Double?, longitude: Double?): String {
        val alertId = UUID.randomUUID().toString()
        val userId = resolveCurrentUserId()

        _sosState.value = SosState.Active(alertId = alertId, timestampMs = System.currentTimeMillis())

        try {
            val dto = EmergencyAlertDto(
                id = alertId,
                wearerId = userId,
                type = "SOS",
                title = "EMERGENCY SOS ALERT",
                message = "Emergency SOS triggered by wearer at current coordinates.",
                latitude = latitude,
                longitude = longitude
            )
            supabaseProvider.db.from("emergency_alerts").insert(dto)
        } catch (e: Exception) {
            // Store locally / pending sync fallback
        }

        return alertId
    }

    override suspend fun resolveSos(alertId: String, wasFalseAlarm: Boolean) {
        val userId = resolveCurrentUserId()

        try {
            if (alertId.isNotEmpty()) {
                supabaseProvider.db.from("emergency_alerts").update(
                    mapOf("is_resolved" to true)
                ) {
                    filter { eq("id", alertId) }
                }
            }

            val attemptDto = SosAttemptDto(
                id = UUID.randomUUID().toString(),
                userId = userId,
                wasFalseAlarm = wasFalseAlarm,
                durationSeconds = 30
            )
            supabaseProvider.db.from("sos_attempts").insert(attemptDto)
        } catch (e: Exception) {
            // Failure fallback
        }

        _sosState.value = SosState.Resolved(wasFalseAlarm = wasFalseAlarm)
        startCooldown(30)
    }

    override fun startCooldown(seconds: Int) {
        scope.launch {
            for (i in seconds downTo 1) {
                _sosState.value = SosState.Cooldown(secondsRemaining = i)
                delay(1000L)
            }
            _sosState.value = SosState.Idle
        }
    }
}
