package com.example.counter_app.security

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Security Event Logger
 *
 * Sistema de logging de eventos de seguridad para auditoría y compliance.
 *
 * Cumplimiento:
 * - ISO/IEC 27001 (A.12.4.1) - Registro de eventos
 * - IEC 62443-4-2 (FR 6.1) - Auditoría de eventos
 * - IEC 62443-4-2 (FR 6.2) - Monitoreo continuo
 *
 * Eventos registrados:
 * - Intentos de autenticación (exitosos y fallidos)
 * - Conexiones a dispositivos Bluetooth
 * - Desconexiones y errores
 * - Cambios de configuración
 * - Eventos de seguridad críticos
 */
class SecurityLogger {

    companion object {
        private const val TAG = "SecurityLogger"
        private const val MAX_LOG_SIZE = 1000 // Máximo de eventos en memoria

        // Singleton instance
        @Volatile
        private var INSTANCE: SecurityLogger? = null

        fun getInstance(): SecurityLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecurityLogger().also { INSTANCE = it }
            }
        }
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    // StateFlow para observar logs en UI
    private val _securityEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val securityEvents: StateFlow<List<SecurityEvent>> = _securityEvents.asStateFlow()

    /**
     * Registra intento de login exitoso
     */
    fun logLoginSuccess(username: String) {
        val event = SecurityEvent(
            type = SecurityEventType.LOGIN_SUCCESS,
            severity = SecuritySeverity.INFO,
            username = username,
            message = "Login exitoso",
            timestamp = Date()
        )
        addEvent(event)
        Log.i(TAG, "LOGIN SUCCESS: $username at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra intento de login fallido
     *
     * IMPORTANTE: Estos eventos deben ser monitoreados para detectar ataques de fuerza bruta
     */
    fun logLoginFailure(username: String, reason: String = "Credenciales inválidas") {
        val event = SecurityEvent(
            type = SecurityEventType.LOGIN_FAILURE,
            severity = SecuritySeverity.WARNING,
            username = username,
            message = reason,
            timestamp = Date()
        )
        addEvent(event)
        Log.w(TAG, "LOGIN FAILURE: $username - $reason at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra logout de usuario
     */
    fun logLogout(username: String) {
        val event = SecurityEvent(
            type = SecurityEventType.LOGOUT,
            severity = SecuritySeverity.INFO,
            username = username,
            message = "Logout exitoso",
            timestamp = Date()
        )
        addEvent(event)
        Log.i(TAG, "LOGOUT: $username at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra conexión exitosa a dispositivo Bluetooth
     */
    fun logBluetoothConnect(deviceAddress: String, username: String?) {
        val event = SecurityEvent(
            type = SecurityEventType.BLUETOOTH_CONNECT,
            severity = SecuritySeverity.INFO,
            username = username,
            deviceAddress = deviceAddress,
            message = "Conexión Bluetooth establecida",
            timestamp = Date()
        )
        addEvent(event)
        Log.i(TAG, "BLUETOOTH CONNECT: $deviceAddress by $username at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra desconexión de dispositivo Bluetooth
     */
    fun logBluetoothDisconnect(deviceAddress: String?, username: String?, reason: String = "Normal") {
        val event = SecurityEvent(
            type = SecurityEventType.BLUETOOTH_DISCONNECT,
            severity = if (reason == "Normal") SecuritySeverity.INFO else SecuritySeverity.WARNING,
            username = username,
            deviceAddress = deviceAddress,
            message = "Desconexión Bluetooth: $reason",
            timestamp = Date()
        )
        addEvent(event)
        Log.i(TAG, "BLUETOOTH DISCONNECT: $deviceAddress - $reason at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra error de comunicación Bluetooth
     */
    fun logBluetoothError(deviceAddress: String?, errorMessage: String, username: String?) {
        val event = SecurityEvent(
            type = SecurityEventType.BLUETOOTH_ERROR,
            severity = SecuritySeverity.ERROR,
            username = username,
            deviceAddress = deviceAddress,
            message = "Error Bluetooth: $errorMessage",
            timestamp = Date()
        )
        addEvent(event)
        Log.e(TAG, "BLUETOOTH ERROR: $deviceAddress - $errorMessage at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra cambio de configuración de seguridad
     */
    fun logSecurityConfigChange(action: String, username: String?) {
        val event = SecurityEvent(
            type = SecurityEventType.CONFIG_CHANGE,
            severity = SecuritySeverity.WARNING,
            username = username,
            message = "Cambio de configuración: $action",
            timestamp = Date()
        )
        addEvent(event)
        Log.w(TAG, "CONFIG CHANGE: $action by $username at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra evento de seguridad crítico
     */
    fun logCriticalEvent(message: String, username: String? = null) {
        val event = SecurityEvent(
            type = SecurityEventType.CRITICAL,
            severity = SecuritySeverity.CRITICAL,
            username = username,
            message = message,
            timestamp = Date()
        )
        addEvent(event)
        Log.e(TAG, "CRITICAL EVENT: $message at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Registra intento de acceso no autorizado
     */
    fun logUnauthorizedAccess(resource: String, username: String?) {
        val event = SecurityEvent(
            type = SecurityEventType.UNAUTHORIZED_ACCESS,
            severity = SecuritySeverity.ERROR,
            username = username,
            message = "Intento de acceso no autorizado a: $resource",
            timestamp = Date()
        )
        addEvent(event)
        Log.e(TAG, "UNAUTHORIZED ACCESS: $resource by $username at ${formatTimestamp(event.timestamp)}")
    }

    /**
     * Obtiene conteo de intentos de login fallidos por usuario en ventana de tiempo
     *
     * Útil para detectar ataques de fuerza bruta
     */
    fun getFailedLoginCount(username: String, windowMinutes: Int = 5): Int {
        val cutoffTime = Date(System.currentTimeMillis() - (windowMinutes * 60 * 1000))
        return _securityEvents.value.count {
            it.type == SecurityEventType.LOGIN_FAILURE &&
                    it.username == username &&
                    it.timestamp.after(cutoffTime)
        }
    }

    /**
     * Obtiene eventos por severidad
     */
    fun getEventsBySeverity(severity: SecuritySeverity): List<SecurityEvent> {
        return _securityEvents.value.filter { it.severity == severity }
    }

    /**
     * Obtiene eventos por tipo
     */
    fun getEventsByType(type: SecurityEventType): List<SecurityEvent> {
        return _securityEvents.value.filter { it.type == type }
    }

    /**
     * Obtiene eventos de un usuario específico
     */
    fun getEventsByUser(username: String): List<SecurityEvent> {
        return _securityEvents.value.filter { it.username == username }
    }

    /**
     * Limpia logs antiguos (mantiene solo los últimos MAX_LOG_SIZE)
     */
    private fun addEvent(event: SecurityEvent) {
        val currentEvents = _securityEvents.value.toMutableList()
        currentEvents.add(event)

        // Mantener solo los últimos MAX_LOG_SIZE eventos
        if (currentEvents.size > MAX_LOG_SIZE) {
            currentEvents.removeAt(0)
        }

        _securityEvents.value = currentEvents
    }

    /**
     * Limpia todos los logs (útil para testing)
     */
    fun clearLogs() {
        _securityEvents.value = emptyList()
        Log.i(TAG, "Security logs cleared")
    }

    /**
     * Exporta logs a string para persistencia o envío
     */
    fun exportLogs(): String {
        val sb = StringBuilder()
        sb.appendLine("Security Event Log Export")
        sb.appendLine("Generated: ${formatTimestamp(Date())}")
        sb.appendLine("Total Events: ${_securityEvents.value.size}")
        sb.appendLine("=" .repeat(80))
        sb.appendLine()

        _securityEvents.value.forEach { event ->
            sb.appendLine("[${formatTimestamp(event.timestamp)}] [${event.severity}] [${event.type}]")
            sb.appendLine("  User: ${event.username ?: "N/A"}")
            if (event.deviceAddress != null) {
                sb.appendLine("  Device: ${event.deviceAddress}")
            }
            sb.appendLine("  Message: ${event.message}")
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Genera reporte de seguridad
     */
    fun generateSecurityReport(): SecurityReport {
        val events = _securityEvents.value
        return SecurityReport(
            totalEvents = events.size,
            criticalEvents = events.count { it.severity == SecuritySeverity.CRITICAL },
            errorEvents = events.count { it.severity == SecuritySeverity.ERROR },
            warningEvents = events.count { it.severity == SecuritySeverity.WARNING },
            infoEvents = events.count { it.severity == SecuritySeverity.INFO },
            loginSuccesses = events.count { it.type == SecurityEventType.LOGIN_SUCCESS },
            loginFailures = events.count { it.type == SecurityEventType.LOGIN_FAILURE },
            bluetoothConnections = events.count { it.type == SecurityEventType.BLUETOOTH_CONNECT },
            bluetoothErrors = events.count { it.type == SecurityEventType.BLUETOOTH_ERROR },
            firstEventTime = events.firstOrNull()?.timestamp,
            lastEventTime = events.lastOrNull()?.timestamp
        )
    }

    private fun formatTimestamp(date: Date): String {
        return dateFormatter.format(date)
    }
}

/**
 * Tipos de eventos de seguridad
 */
enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    BLUETOOTH_CONNECT,
    BLUETOOTH_DISCONNECT,
    BLUETOOTH_ERROR,
    CONFIG_CHANGE,
    UNAUTHORIZED_ACCESS,
    CRITICAL
}

/**
 * Severidad de eventos
 */
enum class SecuritySeverity {
    INFO,       // Eventos normales
    WARNING,    // Eventos sospechosos
    ERROR,      // Errores de seguridad
    CRITICAL    // Eventos críticos que requieren atención inmediata
}

/**
 * Modelo de evento de seguridad
 */
data class SecurityEvent(
    val type: SecurityEventType,
    val severity: SecuritySeverity,
    val username: String? = null,
    val deviceAddress: String? = null,
    val message: String,
    val timestamp: Date
)

/**
 * Reporte de seguridad
 */
data class SecurityReport(
    val totalEvents: Int,
    val criticalEvents: Int,
    val errorEvents: Int,
    val warningEvents: Int,
    val infoEvents: Int,
    val loginSuccesses: Int,
    val loginFailures: Int,
    val bluetoothConnections: Int,
    val bluetoothErrors: Int,
    val firstEventTime: Date?,
    val lastEventTime: Date?
)
