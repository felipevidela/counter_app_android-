package com.example.counter_app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar configuración de la aplicación y alertas.
 */
class SettingsRepository(private val alertSettingsDao: AlertSettingsDao) {

    /**
     * Obtiene la configuración de alertas como Flow.
     * Si no existe, retorna configuración por defecto.
     */
    fun getAlertSettings(): Flow<AlertSettings> {
        return alertSettingsDao.getAlertSettings().map { settings ->
            settings ?: AlertSettings() // Retornar defaults si no existe
        }
    }

    /**
     * Guarda la configuración completa de alertas.
     */
    suspend fun saveAlertSettings(settings: AlertSettings) {
        alertSettingsDao.saveAlertSettings(settings)
    }

    /**
     * Inicializa la configuración de alertas con valores por defecto.
     */
    suspend fun initializeAlertSettings() {
        alertSettingsDao.saveAlertSettings(AlertSettings())
    }

    /**
     * Actualiza solo la configuración de alerta de desconexión.
     */
    suspend fun updateDisconnectionAlert(enabled: Boolean) {
        alertSettingsDao.updateDisconnectionAlert(enabled)
    }

    /**
     * Actualiza solo la configuración de alerta de aforo bajo.
     */
    suspend fun updateLowOccupancyAlert(enabled: Boolean, threshold: Int) {
        alertSettingsDao.updateLowOccupancyAlert(enabled, threshold)
    }

    /**
     * Actualiza solo la configuración de alerta de aforo alto.
     */
    suspend fun updateHighOccupancyAlert(enabled: Boolean, threshold: Int) {
        alertSettingsDao.updateHighOccupancyAlert(enabled, threshold)
    }

    /**
     * Actualiza solo la configuración de alerta de pico de tráfico.
     */
    suspend fun updateTrafficPeakAlert(enabled: Boolean, threshold: Int) {
        alertSettingsDao.updateTrafficPeakAlert(enabled, threshold)
    }
}
