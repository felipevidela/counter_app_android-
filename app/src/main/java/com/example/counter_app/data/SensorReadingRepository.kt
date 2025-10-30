package com.example.counter_app.data

import kotlinx.coroutines.flow.Flow

class SensorReadingRepository(private val sensorReadingDao: SensorReadingDao) {

    fun getReadingsByDevice(deviceId: Long, limit: Int = 100): Flow<List<SensorReading>> =
        sensorReadingDao.getReadingsByDevice(deviceId, limit)

    fun getLatestReading(deviceId: Long): Flow<SensorReading?> =
        sensorReadingDao.getLatestReading(deviceId)

    fun getReadingsByDateRange(deviceId: Long, startTime: Long, endTime: Long): Flow<List<SensorReading>> =
        sensorReadingDao.getReadingsByDateRange(deviceId, startTime, endTime)

    suspend fun insertReading(reading: SensorReading): Long =
        sensorReadingDao.insertReading(reading)

    suspend fun deleteReadingsByDevice(deviceId: Long) =
        sensorReadingDao.deleteReadingsByDevice(deviceId)

    suspend fun deleteOldReadings(beforeTimestamp: Long) =
        sensorReadingDao.deleteOldReadings(beforeTimestamp)
}
