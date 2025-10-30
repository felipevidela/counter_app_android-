package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeviceWithLatestReading(
    val device: Device,
    val latestReading: SensorReading?
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorReadingRepository: SensorReadingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    val devices: Flow<List<Device>> = deviceRepository.getAllDevices()

    val devicesWithReadings: Flow<List<DeviceWithLatestReading>> = devices.flatMapLatest { deviceList ->
        if (deviceList.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                deviceList.map { device ->
                    sensorReadingRepository.getLatestReading(device.id).map { reading ->
                        DeviceWithLatestReading(device, reading)
                    }
                }
            ) { array ->
                array.toList()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDeviceStatus(deviceId: Long, isActive: Boolean) {
        viewModelScope.launch {
            deviceRepository.updateDeviceActiveStatus(deviceId, isActive)
        }
    }

    fun deleteDevice(device: Device) {
        viewModelScope.launch {
            deviceRepository.deleteDevice(device)
        }
    }
}
