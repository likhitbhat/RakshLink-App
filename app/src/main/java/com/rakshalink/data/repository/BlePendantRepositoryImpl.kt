package com.rakshalink.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.rakshalink.domain.model.PendantConnectionState
import com.rakshalink.domain.repository.BlePendantRepository
import com.rakshalink.domain.repository.SosRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlePendantRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sosRepository: SosRepository
) : BlePendantRepository {

    companion object {
        val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
        val BATTERY_LEVEL_CHAR_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
        val SOS_BUTTON_SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805f9b34fb")
        val SOS_BUTTON_CHAR_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private var currentGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectionState = MutableStateFlow(PendantConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<PendantConnectionState> = _connectionState.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    override val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device ?: return
            if (device.name?.contains("Raksha", ignoreCase = true) == true ||
                device.name?.contains("Pendant", ignoreCase = true) == true ||
                device.name?.contains("SOS", ignoreCase = true) == true) {
                stopScan()
                connectDevice(device.address)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = PendantConnectionState.CONNECTED
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = PendantConnectionState.DISCONNECTED
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                // Subscribe to SOS button characteristic
                val sosChar = gatt.getService(SOS_BUTTON_SERVICE_UUID)?.getCharacteristic(SOS_BUTTON_CHAR_UUID)
                if (sosChar != null) {
                    gatt.setCharacteristicNotification(sosChar, true)
                    val descriptor = sosChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }

                // Read Battery Level
                val batteryChar = gatt.getService(BATTERY_SERVICE_UUID)?.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)
                if (batteryChar != null) {
                    gatt.readCharacteristic(batteryChar)
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic?.uuid == BATTERY_LEVEL_CHAR_UUID) {
                val value = characteristic?.value?.getOrNull(0)?.toInt() ?: 100
                _batteryLevel.value = value.coerceIn(0, 100)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic?.uuid == SOS_BUTTON_CHAR_UUID) {
                scope.launch {
                    sosRepository.armSos()
                    sosRepository.showConfirmation()
                    sosRepository.triggerActiveSos(null, null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScan() {
        try {
            val scanner = bluetoothAdapter?.bluetoothLeScanner
            if (scanner != null && bluetoothAdapter?.isEnabled == true) {
                _connectionState.value = PendantConnectionState.SCANNING
                scanner.startScan(scanCallback)
            } else {
                _connectionState.value = PendantConnectionState.DISCONNECTED
            }
        } catch (e: Exception) {
            _connectionState.value = PendantConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {}
    }

    @SuppressLint("MissingPermission")
    override fun connectDevice(deviceAddress: String) {
        if (deviceAddress.isBlank()) return
        try {
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return
            _connectionState.value = PendantConnectionState.CONNECTING
            currentGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: Exception) {
            _connectionState.value = PendantConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        stopScan()
        currentGatt?.disconnect()
        currentGatt?.close()
        currentGatt = null
        _connectionState.value = PendantConnectionState.DISCONNECTED
    }
}

