package com.example.counter_app.bluetooth

import android.bluetooth.le.ScanResult
import com.example.counter_app.data.CounterData
import com.example.counter_app.data.CounterEvent
import com.example.counter_app.data.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class SimulatedBluetoothService : BluetoothServiceInterface {

    private var entered = 0
    private var left = 0
    private var isScanning = false
    private var isConnected = false

    // CoroutineScope for running background tasks
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var counterJob: Job? = null

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

    override fun startBleScan() {
        isScanning = true
        serviceScope.launch {
            delay(1000)
            // onDeviceScan?.invoke(listOf())
        }
    }

    override fun stopBleScan() {
        isScanning = false
    }

    override fun connectToDevice(deviceAddress: String) {
        isConnected = true
        _connectionStateFlow.value = "Conectado"
        onConnectionStateChange?.invoke("Conectado")
        serviceScope.launch {
            delay(500)
            onServicesDiscovered?.invoke(listOf(COUNTER_SERVICE_UUID))
        }

        // Start generating counter data in background
        startCounterGeneration()
    }

    private fun startCounterGeneration() {
        // Cancel any existing job
        counterJob?.cancel()

        // Start a new coroutine that runs continuously
        counterJob = serviceScope.launch {
            while (isConnected) {
                delay(3000)

                // Generate a random event
                val newEvent = if (Random.nextBoolean()) {
                    entered++
                    CounterEvent(EventType.ENTRY, Date())
                } else {
                    if (entered > left) {
                        left++
                        CounterEvent(EventType.EXIT, Date())
                    } else null
                }

                // Add event to StateFlow if generated
                newEvent?.let {
                    val currentEvents = _eventLogFlow.value.toMutableList()
                    currentEvents.add(it)
                    _eventLogFlow.value = currentEvents
                }

                // Update counter data
                val capacity = entered - left
                val counterData = CounterData(entered, left, capacity)
                _counterDataFlow.value = counterData
            }
        }
    }

    override fun getCounterDataFlow(): Flow<CounterData> = counterDataFlow

    override fun getEventLogFlow(): Flow<List<CounterEvent>> = eventLogFlow

    override fun getConnectionStateFlow(): Flow<String> = connectionStateFlow

    override fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        // Simulate successful write
    }

    override fun isBluetoothEnabled(): Boolean {
        return true
    }

    override fun getEventLog(): List<CounterEvent> {
        return _eventLogFlow.value
    }

    override fun disconnect() {
        isConnected = false
        counterJob?.cancel()
        counterJob = null
        _connectionStateFlow.value = "Desconectado"
        onConnectionStateChange?.invoke("Desconectado")
    }

    override fun resetAndDisconnect() {
        // Stop counter generation
        isConnected = false
        counterJob?.cancel()
        counterJob = null

        // Reset counter variables
        entered = 0
        left = 0

        // Reset all StateFlows
        _counterDataFlow.value = CounterData(0, 0, 0)
        _eventLogFlow.value = emptyList()
        _connectionStateFlow.value = "Desconectado"

        // Notify listeners
        onConnectionStateChange?.invoke("Desconectado")
    }
}
