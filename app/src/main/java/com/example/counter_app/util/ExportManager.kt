package com.example.counter_app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.counter_app.data.SensorEvent
import java.io.IOException

/**
 * Gestor centralizado para exportación de eventos a archivos.
 *
 * Maneja:
 * - Creación de archivos CSV y PDF
 * - Guardado en carpeta Downloads usando MediaStore (Android 10+)
 * - Compatibilidad con Android 8-9
 */
object ExportManager {

    /**
     * Formato de exportación.
     */
    enum class ExportFormat {
        CSV,
        PDF
    }

    /**
     * Resultado de una operación de exportación.
     */
    sealed class ExportResult {
        data class Success(val uri: Uri, val filename: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    /**
     * Exporta eventos al formato especificado y guarda en Downloads.
     *
     * @param context Contexto de la aplicación
     * @param events Lista de eventos a exportar
     * @param deviceName Nombre del dispositivo
     * @param format Formato de exportación (CSV o PDF)
     * @return ExportResult con URI del archivo creado o error
     */
    fun exportEvents(
        context: Context,
        events: List<SensorEvent>,
        deviceName: String,
        format: ExportFormat
    ): ExportResult {
        if (events.isEmpty()) {
            return ExportResult.Error("No hay eventos para exportar")
        }

        return try {
            when (format) {
                ExportFormat.CSV -> exportAsCsv(context, events, deviceName)
                ExportFormat.PDF -> exportAsPdf(context, events, deviceName)
            }
        } catch (e: Exception) {
            ExportResult.Error("Error al exportar: ${e.message}")
        }
    }

    /**
     * Exporta eventos como archivo CSV.
     */
    private fun exportAsCsv(
        context: Context,
        events: List<SensorEvent>,
        deviceName: String
    ): ExportResult {
        val filename = generateFilename(deviceName, "csv")
        val csvContent = CsvExporter.exportToCsv(events, deviceName)

        return saveToDownloads(context, filename, "text/csv", csvContent.toByteArray())
    }

    /**
     * Exporta eventos como archivo PDF.
     */
    private fun exportAsPdf(
        context: Context,
        events: List<SensorEvent>,
        deviceName: String
    ): ExportResult {
        val filename = generateFilename(deviceName, "pdf")

        return try {
            val uri = createFileInDownloads(context, filename, "application/pdf")
                ?: return ExportResult.Error("No se pudo crear el archivo")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                PdfExporter.exportToPdf(events, deviceName, outputStream)
            } ?: return ExportResult.Error("No se pudo abrir el archivo para escritura")

            ExportResult.Success(uri, filename)
        } catch (e: IOException) {
            ExportResult.Error("Error al crear PDF: ${e.message}")
        }
    }

    /**
     * Guarda contenido de bytes en la carpeta Downloads.
     */
    private fun saveToDownloads(
        context: Context,
        filename: String,
        mimeType: String,
        content: ByteArray
    ): ExportResult {
        return try {
            val uri = createFileInDownloads(context, filename, mimeType)
                ?: return ExportResult.Error("No se pudo crear el archivo")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content)
            } ?: return ExportResult.Error("No se pudo abrir el archivo para escritura")

            ExportResult.Success(uri, filename)
        } catch (e: IOException) {
            ExportResult.Error("Error al guardar archivo: ${e.message}")
        }
    }

    /**
     * Crea un archivo en la carpeta Downloads usando MediaStore.
     */
    private fun createFileInDownloads(
        context: Context,
        filename: String,
        mimeType: String
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

            // Android 10+ usa RELATIVE_PATH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: usar MediaStore.Downloads
            context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } else {
            // Android 8-9: usar MediaStore.Files
            context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            )
        }
    }

    /**
     * Genera un nombre de archivo único basado en el dispositivo y timestamp.
     *
     * Formato: eventos_[DeviceName]_[YYYYMMDD_HHmmss].[extension]
     */
    private fun generateFilename(deviceName: String, extension: String): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())

        // Sanitizar nombre del dispositivo (remover caracteres especiales)
        val sanitizedDeviceName = deviceName
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .take(30) // Limitar longitud

        return "eventos_${sanitizedDeviceName}_$timestamp.$extension"
    }
}
