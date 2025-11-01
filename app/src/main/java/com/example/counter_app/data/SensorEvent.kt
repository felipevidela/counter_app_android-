package com.example.counter_app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un evento individual de sensor ultrasónico.
 *
 * A diferencia de SensorReading (que almacena capturas de estado acumulado),
 * SensorEvent representa un evento real y específico:
 * - Una persona o grupo entrando al local
 * - Una persona o grupo saliendo del local
 *
 * ## Ejemplo de Eventos Reales:
 * ```
 * 15:30:15 - Entraron 2 personas (pareja)
 * 15:30:42 - Entró 1 persona (solo)
 * 15:31:05 - Salieron 3 personas (familia)
 * ```
 *
 * @property id ID único del evento (auto-generado)
 * @property deviceId ID del dispositivo Arduino que detectó el evento
 * @property eventType Tipo de evento: "ENTRY" (entrada) o "EXIT" (salida)
 * @property peopleCount Cantidad de personas detectadas (1-6 usualmente)
 * @property timestamp Timestamp exacto cuando ocurrió el evento (millis)
 */
@Entity(
    tableName = "sensor_events",
    foreignKeys = [ForeignKey(
        entity = Device::class,
        parentColumns = ["id"],
        childColumns = ["deviceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["deviceId"]), Index(value = ["timestamp"])]
)
data class SensorEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceId: Long,
    val eventType: EventType,
    val peopleCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tipo de evento detectado por el sensor ultrasónico.
 */
enum class EventType {
    /** Evento de entrada: sensor de entrada detectó personas entrando */
    ENTRY,

    /** Evento de salida: sensor de salida detectó personas saliendo */
    EXIT,

    /** Evento de desconexión: se perdió la conexión con el dispositivo */
    DISCONNECTION
}
