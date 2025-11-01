package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorEventRepository: SensorEventRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
    }

    private val _currentDeviceId = MutableStateFlow<Long?>(null)

    val device: Flow<Device?> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            deviceRepository.getDeviceById(deviceId)
        } else {
            flowOf(null)
        }
    }

    val latestEvent: Flow<SensorEvent?> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            sensorEventRepository.getLatestEvent(deviceId)
        } else {
            flowOf(null)
        }
    }

    val recentEvents: Flow<List<SensorEvent>> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            sensorEventRepository.getEventsByDevice(deviceId, limit = 50)
        } else {
            flowOf(emptyList())
        }
    }

    // Calculate total entered based on events
    val totalEntered: Flow<Int> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            recentEvents.map { events ->
                events.filter { it.eventType == EventType.ENTRY }
                    .sumOf { it.peopleCount }
            }
        } else {
            flowOf(0)
        }
    }

    // Calculate total left based on events
    val totalLeft: Flow<Int> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            recentEvents.map { events ->
                events.filter { it.eventType == EventType.EXIT }
                    .sumOf { it.peopleCount }
            }
        } else {
            flowOf(0)
        }
    }

    // Calculate current occupancy (entered - left)
    val currentOccupancy: Flow<Int> = combine(totalEntered, totalLeft) { entered, left ->
        (entered - left).coerceAtLeast(0)
    }

    fun setDeviceId(deviceId: Long) {
        _currentDeviceId.value = deviceId
    }

    fun toggleDeviceStatus(isActive: Boolean) {
        viewModelScope.launch {
            _currentDeviceId.value?.let { deviceId ->
                deviceRepository.updateDeviceActiveStatus(deviceId, isActive)
            }
        }
    }

    fun clearEvents() {
        viewModelScope.launch {
            _currentDeviceId.value?.let { deviceId ->
                sensorEventRepository.clearEvents(deviceId)
            }
        }
    }
}
