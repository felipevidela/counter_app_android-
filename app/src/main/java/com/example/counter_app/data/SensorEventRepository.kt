package com.example.counter_app.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar eventos individuales de sensores ultrasónicos.
 *
 * Proporciona una capa de abstracción entre ViewModels y el DAO,
 * siguiendo el patrón Repository del architecture pattern de Android.
 */
class SensorEventRepository(private val sensorEventDao: SensorEventDao) {

    /**
     * Obtiene los eventos más recientes de un dispositivo.
     */
    fun getEventsByDevice(deviceId: Long, limit: Int = 100): Flow<List<SensorEvent>> {
        return sensorEventDao.getEventsByDevice(deviceId, limit)
    }

    /**
     * Obtiene eventos de un dispositivo en un rango de fechas.
     */
    fun getEventsByDateRange(deviceId: Long, startTime: Long, endTime: Long): Flow<List<SensorEvent>> {
        return sensorEventDao.getEventsByDateRange(deviceId, startTime, endTime)
    }

    /**
     * Obtiene el evento más reciente de un dispositivo.
     */
    fun getLatestEvent(deviceId: Long): Flow<SensorEvent?> {
        return sensorEventDao.getLatestEvent(deviceId)
    }

    /**
     * Calcula el total de personas que han entrado.
     */
    suspend fun getTotalEntered(deviceId: Long): Int {
        return sensorEventDao.getTotalEntered(deviceId)
    }

    /**
     * Calcula el total de personas que han salido.
     */
    suspend fun getTotalLeft(deviceId: Long): Int {
        return sensorEventDao.getTotalLeft(deviceId)
    }

    /**
     * Calcula la ocupación actual (entradas - salidas).
     */
    suspend fun getCurrentOccupancy(deviceId: Long): Int {
        val entered = getTotalEntered(deviceId)
        val left = getTotalLeft(deviceId)
        return (entered - left).coerceAtLeast(0)
    }

    /**
     * Obtiene el conteo total de eventos.
     */
    suspend fun getEventCount(deviceId: Long): Int {
        return sensorEventDao.getEventCount(deviceId)
    }

    /**
     * Inserta un nuevo evento.
     */
    suspend fun insertEvent(event: SensorEvent): Long {
        return sensorEventDao.insertEvent(event)
    }

    /**
     * Inserta múltiples eventos.
     */
    suspend fun insertEvents(events: List<SensorEvent>) {
        sensorEventDao.insertEvents(events)
    }

    /**
     * Elimina todos los eventos de un dispositivo.
     */
    suspend fun clearEvents(deviceId: Long) {
        sensorEventDao.deleteEventsByDevice(deviceId)
    }

    /**
     * Elimina eventos antiguos.
     */
    suspend fun deleteOldEvents(beforeTimestamp: Long) {
        sensorEventDao.deleteOldEvents(beforeTimestamp)
    }
}
