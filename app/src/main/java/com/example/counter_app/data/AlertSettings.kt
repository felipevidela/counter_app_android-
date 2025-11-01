package com.example.counter_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Configuración de alertas personalizadas para notificaciones.
 *
 * Permite configurar diferentes tipos de alertas que se dispararán
 * automáticamente según las condiciones del aforo.
 */
@Entity(tableName = "alert_settings")
data class AlertSettings(
    @PrimaryKey
    val id: Long = 1, // Solo una configuración global

    // Alerta de desconexión de dispositivos
    val disconnectionAlertEnabled: Boolean = false,

    // Alerta de aforo bajo
    val lowOccupancyEnabled: Boolean = false,
    val lowOccupancyThreshold: Int = 5, // Porcentaje de capacidad

    // Alerta de aforo alto/capacidad máxima
    val highOccupancyEnabled: Boolean = false,
    val highOccupancyThreshold: Int = 90, // Porcentaje de capacidad

    // Alerta de pico de tráfico (muchas entradas en poco tiempo)
    val trafficPeakEnabled: Boolean = false,
    val trafficPeakThreshold: Int = 10 // Entradas en 5 minutos
)
