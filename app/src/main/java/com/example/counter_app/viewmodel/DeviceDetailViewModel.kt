package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorReadingRepository: SensorReadingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    private val _currentDeviceId = MutableStateFlow<Long?>(null)

    val device: Flow<Device?> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            deviceRepository.getDeviceById(deviceId)
        } else {
            flowOf(null)
        }
    }

    val latestReading: Flow<SensorReading?> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            sensorReadingRepository.getLatestReading(deviceId)
        } else {
            flowOf(null)
        }
    }

    val recentReadings: Flow<List<SensorReading>> = _currentDeviceId.flatMapLatest { deviceId ->
        if (deviceId != null) {
            sensorReadingRepository.getReadingsByDevice(deviceId, limit = 50)
        } else {
            flowOf(emptyList())
        }
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

    fun clearReadings() {
        viewModelScope.launch {
            _currentDeviceId.value?.let { deviceId ->
                sensorReadingRepository.deleteReadingsByDevice(deviceId)
            }
        }
    }
}
