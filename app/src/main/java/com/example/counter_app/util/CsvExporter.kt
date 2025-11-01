package com.example.counter_app.util

import com.example.counter_app.data.SensorEvent
import com.example.counter_app.data.EventType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para exportar eventos de sensores a formato CSV.
 *
 * Genera un archivo CSV con las siguientes columnas:
 * - Event ID
 * - Device Name
 * - Event Type
 * - People Count
 * - Date
 * - Time
 * - Timestamp
 */
object CsvExporter {

    /**
     * Convierte una lista de eventos a formato CSV.
     *
     * @param events Lista de eventos a exportar
     * @param deviceName Nombre del dispositivo
     * @return String con el contenido CSV
     */
    fun exportToCsv(events: List<SensorEvent>, deviceName: String): String {
        val csvBuilder = StringBuilder()

        // Header
        csvBuilder.appendLine("Event ID,Device Name,Event Type,People Count,Date,Time,Timestamp")

        // Data rows
        events.forEach { event ->
            val eventType = when (event.eventType) {
                EventType.ENTRY -> "ENTRADA"
                EventType.EXIT -> "SALIDA"
                EventType.DISCONNECTION -> "DESCONEXION"
            }

            val date = formatDate(event.timestamp)
            val time = formatTime(event.timestamp)

            // Escapar nombre del dispositivo si contiene comas
            val escapedDeviceName = if (deviceName.contains(",")) {
                "\"$deviceName\""
            } else {
                deviceName
            }

            csvBuilder.appendLine(
                "${event.id}," +
                "$escapedDeviceName," +
                "$eventType," +
                "${event.peopleCount}," +
                "$date," +
                "$time," +
                "${event.timestamp}"
            )
        }

        return csvBuilder.toString()
    }

    /**
     * Formatea un timestamp a fecha (dd/MM/yyyy).
     */
    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Formatea un timestamp a hora (HH:mm:ss).
     */
    private fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}
