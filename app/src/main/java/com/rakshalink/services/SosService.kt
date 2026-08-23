package com.rakshalink.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.rakshalink.data.preferences.UserPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SosService : Service() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var alarmJob: Job? = null
    private var isMuted = false

    override fun onCreate() {
        super.onCreate()
        initAudioAndVibrator()
    }

    private fun initAudioAndVibrator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            // Tone generator fallback
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "START_ALARM" -> startEmergencyAlarm()
            "MUTE_ALARM" -> muteAlarmTemporarily()
            "STOP_ALARM" -> stopEmergencyAlarm()
        }
        return START_NOT_STICKY
    }

    private fun startEmergencyAlarm() {
        alarmJob?.cancel()
        alarmJob = CoroutineScope(Dispatchers.Default).launch {
            val soundEnabled = userPreferencesManager.soundEnabledFlow.first()
            val vibrationEnabled = userPreferencesManager.vibrationEnabledFlow.first()

            while (true) {
                if (!isMuted && soundEnabled) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
                }

                if (vibrationEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 500, 200, 500),
                                -1
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(longArrayOf(0, 500, 200, 500), -1)
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun muteAlarmTemporarily() {
        isMuted = true
        CoroutineScope(Dispatchers.Default).launch {
            delay(30000L) // Auto resume after 30 seconds
            isMuted = false
        }
    }

    private fun stopEmergencyAlarm() {
        alarmJob?.cancel()
        vibrator?.cancel()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmJob?.cancel()
        vibrator?.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
