package com.example.counter_app.bluetooth

import android.bluetooth.le.ScanResult
import com.example.counter_app.data.CounterData
import com.example.counter_app.data.CounterEvent
import java.util.UUID

import kotlinx.coroutines.flow.Flow

interface BluetoothServiceInterface {
    var onDeviceScan: ((List<ScanResult>) -> Unit)?
    var onConnectionStateChange: ((String) -> Unit)?
    var onServicesDiscovered: ((List<UUID>) -> Unit)?

    fun startBleScan()
    fun stopBleScan()
    fun connectToDevice(deviceAddress: String)
    fun getCounterDataFlow(): Flow<CounterData>
    fun getEventLogFlow(): Flow<List<CounterEvent>>
    fun getConnectionStateFlow(): Flow<String>
    fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray)
    fun isBluetoothEnabled(): Boolean
    fun getEventLog(): List<CounterEvent>
    fun disconnect()
    fun resetAndDisconnect()
}
