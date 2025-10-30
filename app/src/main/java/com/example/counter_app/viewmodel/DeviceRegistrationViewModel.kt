package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class DeviceRegistrationViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
    }

    fun createDevice(
        name: String,
        type: String,
        location: String,
        capacity: Int,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("El nombre del dispositivo no puede estar vacío")
            return
        }

        if (capacity <= 0) {
            onError("La capacidad debe ser mayor a 0")
            return
        }

        viewModelScope.launch {
            try {
                val device = Device(
                    name = name,
                    type = type,
                    location = location,
                    macAddress = generateSimulatedMacAddress(),
                    capacity = capacity,
                    isActive = true
                )
                val deviceId = deviceRepository.insertDevice(device)
                onSuccess(deviceId)
            } catch (e: Exception) {
                onError("Error al crear el dispositivo: ${e.message}")
            }
        }
    }

    private fun generateSimulatedMacAddress(): String {
        val bytes = ByteArray(6) { Random.nextInt(256).toByte() }
        return bytes.joinToString(":") { "%02X".format(it) }
    }
}
