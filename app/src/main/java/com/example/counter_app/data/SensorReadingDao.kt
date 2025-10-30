package com.example.counter_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    fun getReadingsByDevice(deviceId: Long, limit: Int = 100): Flow<List<SensorReading>>

    @Query("SELECT * FROM sensor_readings WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(deviceId: Long): Flow<SensorReading?>

    @Query("SELECT * FROM sensor_readings WHERE deviceId = :deviceId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getReadingsByDateRange(deviceId: Long, startTime: Long, endTime: Long): Flow<List<SensorReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReading): Long

    @Query("DELETE FROM sensor_readings WHERE deviceId = :deviceId")
    suspend fun deleteReadingsByDevice(deviceId: Long)

    @Query("DELETE FROM sensor_readings WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldReadings(beforeTimestamp: Long)
}
