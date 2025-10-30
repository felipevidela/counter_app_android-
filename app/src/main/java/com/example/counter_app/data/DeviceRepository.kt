package com.example.counter_app.data

import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getAllDevices(): Flow<List<Device>> = deviceDao.getAllDevices()

    fun getDeviceById(deviceId: Long): Flow<Device?> = deviceDao.getDeviceByIdFlow(deviceId)

    suspend fun getDeviceByIdSuspend(deviceId: Long): Device? = deviceDao.getDeviceById(deviceId)

    suspend fun insertDevice(device: Device): Long = deviceDao.insertDevice(device)

    suspend fun updateDevice(device: Device) = deviceDao.updateDevice(device)

    suspend fun deleteDevice(device: Device) = deviceDao.deleteDevice(device)

    suspend fun updateDeviceActiveStatus(deviceId: Long, isActive: Boolean) =
        deviceDao.updateDeviceActiveStatus(deviceId, isActive)
}
