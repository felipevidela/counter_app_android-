package com.example.counter_app.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.counter_app.data.CounterData
import com.example.counter_app.data.CounterEvent
import java.util.UUID

// Security: ISO/IEC 27001 (A.10.1.1) - Cryptographic controls
// In a real-world scenario, data transmitted over Bluetooth should be encrypted to protect against eavesdropping.
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothService(private val context: Context): BluetoothServiceInterface {

    private val bluetoothManager: BluetoothManager? = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null

    // Use StateFlow to emit counter data and events in real-time
    private val _counterDataFlow = MutableStateFlow(CounterData(0, 0, 0))
    private val counterDataFlow: StateFlow<CounterData> = _counterDataFlow.asStateFlow()

    private val _eventLogFlow = MutableStateFlow<List<CounterEvent>>(emptyList())
    private val eventLogFlow: StateFlow<List<CounterEvent>> = _eventLogFlow.asStateFlow()

    private val _connectionStateFlow = MutableStateFlow("Desconectado")
    private val connectionStateFlow: StateFlow<String> = _connectionStateFlow.asStateFlow()

    override var onDeviceScan: ((List<ScanResult>) -> Unit)? = null
    override var onConnectionStateChange: ((String) -> Unit)? = null
    override var onServicesDiscovered: ((List<UUID>) -> Unit)? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // In a real app, you would filter by service UUID
                onDeviceScan?.invoke(listOf(it))
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let {
                onDeviceScan?.invoke(it)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
        }
    }

    override fun startBleScan() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        bleScanner?.startScan(null, scanSettings, scanCallback)
    }

    override fun stopBleScan() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bleScanner?.stopScan(scanCallback)
    }

    override fun connectToDevice(deviceAddress: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionStateFlow.value = "Conectado"
                onConnectionStateChange?.invoke("Connected")
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionStateFlow.value = "Desconectado"
                onConnectionStateChange?.invoke("Disconnected")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val serviceUuids = gatt?.services?.map { it.uuid } ?: emptyList()
            onServicesDiscovered?.invoke(serviceUuids)
        }
    }

    override fun getCounterDataFlow(): Flow<CounterData> = counterDataFlow

    override fun getEventLogFlow(): Flow<List<CounterEvent>> = eventLogFlow

    override fun getConnectionStateFlow(): Flow<String> = connectionStateFlow

    override fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        val service = bluetoothGatt?.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        characteristic?.let { 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeCharacteristic(it, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                @Suppress("DEPRECATION")
                it.value = data
                @Suppress("DEPRECATION")
                bluetoothGatt?.writeCharacteristic(it)
            }
        }
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    override fun getEventLog(): List<CounterEvent> {
        return _eventLogFlow.value
    }

    override fun disconnect() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
    }

    override fun resetAndDisconnect() {
        // Disconnect from device
        disconnect()

        // Reset all StateFlows
        _counterDataFlow.value = CounterData(0, 0, 0)
        _eventLogFlow.value = emptyList()
        _connectionStateFlow.value = "Desconectado"
    }
}