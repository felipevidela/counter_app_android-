package com.example.counter_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY createdAt DESC")
    fun getAllDevices(): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: Long): Device?

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    fun getDeviceByIdFlow(deviceId: Long): Flow<Device?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    @Update
    suspend fun updateDevice(device: Device)

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("UPDATE devices SET isActive = :isActive WHERE id = :deviceId")
    suspend fun updateDeviceActiveStatus(deviceId: Long, isActive: Boolean)
}
