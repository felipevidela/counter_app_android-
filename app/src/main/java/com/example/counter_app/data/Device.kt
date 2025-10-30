package com.example.counter_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un dispositivo Arduino IoT con sensores ultrasónicos.
 *
 * Esta entidad se almacena en la tabla "devices" de Room Database (SQLite local).
 * Cada dispositivo simula un Arduino real con 2 sensores ultrasónicos para
 * conteo de personas (entrada y salida) en tiendas de mall.
 *
 * @property id Identificador único autogenerado del dispositivo
 * @property name Nombre descriptivo del dispositivo (ej: "Sensor Entrada Principal")
 * @property type Tipo de microcontrolador Arduino ("Arduino Nano", "Arduino Uno", "ESP32", "NodeMCU")
 * @property macAddress Dirección MAC simulada única generada automáticamente
 * @property capacity Capacidad máxima de personas permitidas (para alertas de aforo)
 * @property location Ubicación física del dispositivo en la tienda (ej: "Entrada Principal", "Probadores")
 * @property isActive Indica si la simulación está activa (true) o detenida (false)
 * @property createdAt Timestamp de creación del dispositivo en milisegundos
 *
 * @see SensorReading para las lecturas asociadas a este dispositivo
 * @see DeviceDao para operaciones CRUD sobre esta entidad
 */
@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Arduino Nano", "Arduino Uno", "ESP32", "NodeMCU"
    val macAddress: String, // Simulated MAC address
    val capacity: Int = 100, // Maximum capacity for counter devices
    val location: String = "", // Location in store (e.g., "Entrada Principal", "Probadores")
    val isActive: Boolean = true, // Whether simulation is running
    val createdAt: Long = System.currentTimeMillis()
)
