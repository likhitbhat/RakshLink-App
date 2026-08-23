package com.rakshalink.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.rakshalink.domain.model.FallState
import com.rakshalink.domain.repository.LocationRepository
import com.rakshalink.domain.repository.SosRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class FallDetectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sosRepository: SosRepository,
    private val locationRepository: LocationRepository
) : SensorEventListener {

    private val sensorManager: SensorManager? by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    private val _fallState = MutableStateFlow<FallState>(FallState.Monitoring)
    val fallState: StateFlow<FallState> = _fallState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null
    private var lastFreefallTime: Long = 0L

    companion object {
        const val FREEFALL_THRESHOLD = 3.0f // m/s^2
        const val IMPACT_THRESHOLD = 25.0f // m/s^2
        const val FREEFALL_IMPACT_MAX_DELAY_MS = 1000L
    }

    fun startMonitoring() {
        try {
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyro = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            if (accel != null) {
                sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
            }
            if (gyro != null) {
                sensorManager?.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
            }
        } catch (e: Exception) {
            // Sensor fallback
        }
        _fallState.value = FallState.Monitoring
    }

    fun stopMonitoring() {
        sensorManager?.unregisterListener(this)
        countdownJob?.cancel()
        _fallState.value = FallState.Monitoring
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (_fallState.value is FallState.PossibleFallDetected || _fallState.value is FallState.Countdown) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt(x * x + y * y + z * z)

            val now = System.currentTimeMillis()
            if (magnitude < FREEFALL_THRESHOLD) {
                lastFreefallTime = now
            } else if (magnitude > IMPACT_THRESHOLD) {
                if (now - lastFreefallTime <= FREEFALL_IMPACT_MAX_DELAY_MS) {
                    triggerFallAlertSequence()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun triggerFallAlertSequence() {
        countdownJob?.cancel()
        _fallState.value = FallState.PossibleFallDetected

        countdownJob = scope.launch {
            for (i in 15 downTo 1) {
                if (_fallState.value is FallState.Cancelled) break
                _fallState.value = FallState.Countdown(secondsRemaining = i)
                delay(1000L)
            }

            if (_fallState.value is FallState.Countdown) {
                _fallState.value = FallState.SosTriggered
                val loc = locationRepository.getLatestLocation().firstOrNull()
                sosRepository.armSos()
                sosRepository.showConfirmation()
                sosRepository.triggerActiveSos(loc?.latitude, loc?.longitude)
            }
        }
    }

    fun cancelFall() {
        countdownJob?.cancel()
        _fallState.value = FallState.Cancelled
        scope.launch {
            delay(1500L)
            _fallState.value = FallState.Monitoring
        }
    }
}
