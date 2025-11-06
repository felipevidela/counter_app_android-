package com.example.counter_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a eventos individuales de sensores ultrasónicos.
 *
 * Proporciona métodos para:
 * - Obtener eventos recientes de un dispositivo
 * - Obtener eventos por rango de fechas
 * - Calcular estadísticas (total entradas, salidas, ocupación actual)
 * - Insertar eventos individuales
 * - Limpiar historial de eventos
 */
@Dao
interface SensorEventDao {

    /**
     * Obtiene los eventos más recientes de un dispositivo.
     *
     * @param deviceId ID del dispositivo
     * @param limit Cantidad máxima de eventos a retornar (por defecto 100)
     * @return Flow de lista de eventos ordenados por timestamp DESC
     */
    @Query("SELECT * FROM sensor_events WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    fun getEventsByDevice(deviceId: Long, limit: Int = 100): Flow<List<SensorEvent>>

    /**
     * Obtiene eventos de un dispositivo en un rango de fechas específico.
     *
     * @param deviceId ID del dispositivo
     * @param startTime Timestamp de inicio (inclusive)
     * @param endTime Timestamp de fin (inclusive)
     * @return Flow de lista de eventos ordenados por timestamp ASC
     */
    @Query("SELECT * FROM sensor_events WHERE deviceId = :deviceId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getEventsByDateRange(deviceId: Long, startTime: Long, endTime: Long): Flow<List<SensorEvent>>

    /**
     * Obtiene el evento más reciente de un dispositivo.
     *
     * @param deviceId ID del dispositivo
     * @return Flow del evento más reciente o null si no hay eventos
     */
    @Query("SELECT * FROM sensor_events WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestEvent(deviceId: Long): Flow<SensorEvent?>

    /**
     * Calcula el total de personas que han entrado (suma de todos los eventos ENTRY).
     *
     * @param deviceId ID del dispositivo
     * @return Total acumulado de entradas
     */
    @Query("SELECT COALESCE(SUM(peopleCount), 0) FROM sensor_events WHERE deviceId = :deviceId AND eventType = 'ENTRY'")
    suspend fun getTotalEntered(deviceId: Long): Int

    /**
     * Calcula el total de personas que han entrado (Flow reactivo).
     *
     * @param deviceId ID del dispositivo
     * @return Flow que emite el total acumulado de entradas
     */
    @Query("SELECT COALESCE(SUM(peopleCount), 0) FROM sensor_events WHERE deviceId = :deviceId AND eventType = 'ENTRY'")
    fun getTotalEnteredFlow(deviceId: Long): Flow<Int>

    /**
     * Calcula el total de personas que han salido (suma de todos los eventos EXIT).
     *
     * @param deviceId ID del dispositivo
     * @return Total acumulado de salidas
     */
    @Query("SELECT COALESCE(SUM(peopleCount), 0) FROM sensor_events WHERE deviceId = :deviceId AND eventType = 'EXIT'")
    suspend fun getTotalLeft(deviceId: Long): Int

    /**
     * Calcula el total de personas que han salido (Flow reactivo).
     *
     * @param deviceId ID del dispositivo
     * @return Flow que emite el total acumulado de salidas
     */
    @Query("SELECT COALESCE(SUM(peopleCount), 0) FROM sensor_events WHERE deviceId = :deviceId AND eventType = 'EXIT'")
    fun getTotalLeftFlow(deviceId: Long): Flow<Int>

    /**
     * Cuenta cuántos eventos hay para un dispositivo.
     *
     * @param deviceId ID del dispositivo
     * @return Cantidad total de eventos
     */
    @Query("SELECT COUNT(*) FROM sensor_events WHERE deviceId = :deviceId")
    suspend fun getEventCount(deviceId: Long): Int

    /**
     * Inserta un nuevo evento de sensor.
     *
     * @param event Evento a insertar
     * @return ID del evento insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SensorEvent): Long

    /**
     * Inserta múltiples eventos de una vez.
     *
     * @param events Lista de eventos a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<SensorEvent>)

    /**
     * Elimina todos los eventos de un dispositivo específico.
     *
     * @param deviceId ID del dispositivo
     */
    @Query("DELETE FROM sensor_events WHERE deviceId = :deviceId")
    suspend fun deleteEventsByDevice(deviceId: Long)

    /**
     * Elimina eventos antiguos (útil para limpieza periódica).
     *
     * @param beforeTimestamp Eliminar eventos anteriores a este timestamp
     */
    @Query("DELETE FROM sensor_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldEvents(beforeTimestamp: Long)

    /**
     * Resetea el autoincrement de IDs de la tabla sensor_events.
     *
     * Esto hace que el próximo evento insertado tenga ID = 1.
     * IMPORTANTE: Solo debe ejecutarse después de eliminar todos los eventos.
     */
    @Query("DELETE FROM sqlite_sequence WHERE name = 'sensor_events'")
    suspend fun resetAutoIncrement()
}
