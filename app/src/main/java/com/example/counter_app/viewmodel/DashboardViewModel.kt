package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeviceStats(
    val entered: Int,
    val left: Int,
    val capacity: Int
)

data class DeviceWithLatestReading(
    val device: Device,
    val latestReading: DeviceStats?
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorEventRepository: SensorEventRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
    }

    val devices: Flow<List<Device>> = deviceRepository.getAllDevices()

    val devicesWithReadings: Flow<List<DeviceWithLatestReading>> = devices.flatMapLatest { deviceList ->
        if (deviceList.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                deviceList.map { device ->
                    // Combinar flows de entradas y salidas para cada dispositivo
                    combine(
                        sensorEventRepository.getTotalEnteredFlow(device.id),
                        sensorEventRepository.getTotalLeftFlow(device.id)
                    ) { entered, left ->
                        val stats = DeviceStats(entered, left, device.capacity)
                        DeviceWithLatestReading(device, stats)
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
