package com.example.counter_app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una lectura de los sensores ultrasónicos de un dispositivo.
 *
 * Almacena el historial completo de detecciones de entrada y salida de personas
 * con timestamps para análisis temporal y generación de reportes.
 *
 * **Relación con Device:**
 * - Relación Many-to-One: Múltiples lecturas pertenecen a un dispositivo
 * - Foreign Key con CASCADE: Si se elimina el dispositivo, se eliminan sus lecturas
 * - Índice en deviceId para optimizar consultas por dispositivo
 *
 * **Sensores Ultrasónicos:**
 * - `entered`: Detecciones del sensor de entrada (personas que entraron)
 * - `left`: Detecciones del sensor de salida (personas que salieron)
 * - Aforo actual se calcula como: `entered - left`
 *
 * @property id Identificador único autogenerado de la lectura
 * @property deviceId ID del dispositivo al que pertenece esta lectura (Foreign Key)
 * @property entered Total acumulado de detecciones del sensor de entrada
 * @property left Total acumulado de detecciones del sensor de salida
 * @property capacity Capacidad máxima del local (copiada desde Device para histórico)
 * @property timestamp Momento exacto de la lectura en milisegundos (Unix timestamp)
 *
 * @see Device entidad padre relacionada
 * @see SensorReadingDao para operaciones CRUD y consultas temporales
 */
@Entity(
    tableName = "sensor_readings",
    foreignKeys = [ForeignKey(
        entity = Device::class,
        parentColumns = ["id"],
        childColumns = ["deviceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["deviceId"])]
)
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,
    val entered: Int,
    val left: Int,
    val capacity: Int,
    val timestamp: Long = System.currentTimeMillis()
)
