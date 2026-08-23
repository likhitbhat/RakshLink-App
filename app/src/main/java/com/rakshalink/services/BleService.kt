package com.rakshalink.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.rakshalink.domain.repository.BlePendantRepository
import com.rakshalink.domain.repository.SosRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BleService : Service() {

    @Inject
    lateinit var bleRepository: BlePendantRepository

    @Inject
    lateinit var sosRepository: SosRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "START_SCAN" -> bleRepository.startScan()
            "CONNECT" -> {
                val address = intent.getStringExtra("DEVICE_ADDRESS") ?: ""
                if (address.isNotEmpty()) bleRepository.connectDevice(address)
            }
            "DISCONNECT" -> bleRepository.disconnect()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
