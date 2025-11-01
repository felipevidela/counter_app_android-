package com.example.counter_app.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.counter_app.data.EventType
import com.example.counter_app.data.SensorEvent
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para exportar eventos de sensores a formato PDF.
 *
 * Genera un documento PDF profesional con:
 * - Header con nombre del dispositivo y fecha de exportación
 * - Tabla con eventos (tipo, cantidad, fecha y hora)
 * - Footer con resumen de estadísticas
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595  // A4 width in points (8.27 inches)
    private const val PAGE_HEIGHT = 842 // A4 height in points (11.69 inches)
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 20f

    /**
     * Exporta una lista de eventos a un PDF y lo escribe en un OutputStream.
     *
     * @param events Lista de eventos a exportar
     * @param deviceName Nombre del dispositivo
     * @param outputStream OutputStream donde escribir el PDF
     */
    fun exportToPdf(events: List<SensorEvent>, deviceName: String, outputStream: OutputStream) {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPosition = MARGIN + 90f  // Aumentado para evitar solapamiento con header
        var pageNumber = 1

        try {
            // Crear primera página
            currentPage = startNewPage(document, pageNumber)
            canvas = currentPage.canvas

            // Dibujar header
            drawHeader(canvas!!, deviceName)

            // Calcular estadísticas
            val totalEntries = events.count { it.eventType == EventType.ENTRY }
            val totalExits = events.count { it.eventType == EventType.EXIT }
            val totalDisconnections = events.count { it.eventType == EventType.DISCONNECTION }

            // Dibujar tabla de eventos
            events.forEachIndexed { index, event ->
                // Verificar si necesitamos una nueva página
                if (yPosition > PAGE_HEIGHT - 100) {
                    document.finishPage(currentPage)
                    pageNumber++
                    currentPage = startNewPage(document, pageNumber)
                    canvas = currentPage.canvas
                    yPosition = MARGIN + 40f
                }

                drawEventRow(canvas!!, event, yPosition)
                yPosition += LINE_HEIGHT + 5f
            }

            // Dibujar footer con estadísticas
            drawFooter(canvas!!, totalEntries, totalExits, totalDisconnections, yPosition + 20f)

            document.finishPage(currentPage)

            // Escribir el documento al OutputStream
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    /**
     * Crea una nueva página en el documento.
     */
    private fun startNewPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        return document.startPage(pageInfo)
    }

    /**
     * Dibuja el header del PDF con título y fecha.
     */
    private fun drawHeader(canvas: Canvas, deviceName: String) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        // Título
        canvas.drawText("Reporte de Eventos", MARGIN, MARGIN + 25f, titlePaint)

        // Dispositivo (con más espacio)
        canvas.drawText("Dispositivo: $deviceName", MARGIN, MARGIN + 50f, subtitlePaint)

        // Fecha de exportación (con más espacio)
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha de exportación: $currentDate", MARGIN, MARGIN + 68f, subtitlePaint)

        // Línea separadora (con más espacio)
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, MARGIN + 80f, PAGE_WIDTH - MARGIN, MARGIN + 80f, linePaint)
    }

    /**
     * Dibuja una fila de la tabla con un evento.
     */
    private fun drawEventRow(canvas: Canvas, event: SensorEvent, yPosition: Float) {
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }

        val iconPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
        }

        // Determinar color y texto según tipo de evento
        val (eventText, eventColor) = when (event.eventType) {
            EventType.ENTRY -> "ENTRADA" to Color.rgb(76, 175, 80)     // Verde
            EventType.EXIT -> "SALIDA" to Color.rgb(244, 67, 54)       // Rojo
            EventType.DISCONNECTION -> "DESCONEXIÓN" to Color.rgb(255, 152, 0) // Naranja
        }

        iconPaint.color = eventColor

        // Columna 1: Tipo de evento (con color)
        canvas.drawText("●", MARGIN, yPosition, iconPaint)
        canvas.drawText(eventText, MARGIN + 20f, yPosition, textPaint)

        // Columna 2: Cantidad de personas (solo si no es desconexión)
        if (event.eventType != EventType.DISCONNECTION) {
            val peopleText = "${event.peopleCount} persona${if (event.peopleCount != 1) "s" else ""}"
            canvas.drawText(peopleText, MARGIN + 150f, yPosition, textPaint)
        }

        // Columna 3: Fecha
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(event.timestamp))
        canvas.drawText(date, MARGIN + 280f, yPosition, textPaint)

        // Columna 4: Hora
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
        canvas.drawText(time, MARGIN + 400f, yPosition, textPaint)
    }

    /**
     * Dibuja el footer con resumen de estadísticas.
     */
    private fun drawFooter(
        canvas: Canvas,
        totalEntries: Int,
        totalExits: Int,
        totalDisconnections: Int,
        yPosition: Float
    ) {
        val footerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isFakeBoldText = true
        }

        // Línea separadora
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)

        // Resumen
        canvas.drawText("RESUMEN", MARGIN, yPosition + 20f, footerPaint)

        val summaryPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }

        canvas.drawText("Total de entradas: $totalEntries", MARGIN, yPosition + 40f, summaryPaint)
        canvas.drawText("Total de salidas: $totalExits", MARGIN, yPosition + 55f, summaryPaint)
        canvas.drawText("Eventos de desconexión: $totalDisconnections", MARGIN, yPosition + 70f, summaryPaint)

        val currentOccupancy = (totalEntries - totalExits).coerceAtLeast(0)
        canvas.drawText("Aforo final: $currentOccupancy personas", MARGIN, yPosition + 85f, footerPaint)
    }
}
