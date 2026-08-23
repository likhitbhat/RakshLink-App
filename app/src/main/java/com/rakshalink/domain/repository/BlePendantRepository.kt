package com.rakshalink.domain.repository

import com.rakshalink.domain.model.PendantConnectionState
import kotlinx.coroutines.flow.StateFlow

interface BlePendantRepository {
    val connectionState: StateFlow<PendantConnectionState>
    val batteryLevel: StateFlow<Int>
    fun startScan()
    fun connectDevice(deviceAddress: String)
    fun disconnect()
}
