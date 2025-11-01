package com.example.counter_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestionar la configuración de alertas.
 */
@Dao
interface AlertSettingsDao {

    /**
     * Obtiene la configuración actual de alertas.
     */
    @Query("SELECT * FROM alert_settings WHERE id = 1")
    fun getAlertSettings(): Flow<AlertSettings?>

    /**
     * Guarda o actualiza la configuración de alertas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlertSettings(settings: AlertSettings)

    /**
     * Actualiza solo el estado de alerta de desconexión.
     */
    @Query("UPDATE alert_settings SET disconnectionAlertEnabled = :enabled WHERE id = 1")
    suspend fun updateDisconnectionAlert(enabled: Boolean)

    /**
     * Actualiza solo el estado de alerta de aforo bajo.
     */
    @Query("UPDATE alert_settings SET lowOccupancyEnabled = :enabled, lowOccupancyThreshold = :threshold WHERE id = 1")
    suspend fun updateLowOccupancyAlert(enabled: Boolean, threshold: Int)

    /**
     * Actualiza solo el estado de alerta de aforo alto.
     */
    @Query("UPDATE alert_settings SET highOccupancyEnabled = :enabled, highOccupancyThreshold = :threshold WHERE id = 1")
    suspend fun updateHighOccupancyAlert(enabled: Boolean, threshold: Int)

    /**
     * Actualiza solo el estado de alerta de pico de tráfico.
     */
    @Query("UPDATE alert_settings SET trafficPeakEnabled = :enabled, trafficPeakThreshold = :threshold WHERE id = 1")
    suspend fun updateTrafficPeakAlert(enabled: Boolean, threshold: Int)
}
