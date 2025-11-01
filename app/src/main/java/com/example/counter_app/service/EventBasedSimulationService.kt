package com.example.counter_app.service

import android.app.Application
import com.example.counter_app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * Servicio de simulación basado en eventos individuales.
 *
 * En lugar de capturar el estado cada X segundos, este servicio genera
 * eventos individuales realistas de entrada y salida, como lo haría
 * un sistema real con sensores ultrasónicos en un mall.
 *
 * ## Eventos Generados:
 * - ENTRY: Persona o grupo detectado entrando al local
 * - EXIT: Persona o grupo detectado saliendo del local
 * - DISCONNECTION: Pérdida de conexión con el dispositivo (1% probabilidad)
 *
 * ## Patrones Simulados:
 * - Grupos de tamaño variable (1-6 personas)
 * - Frecuencia ajustable (más eventos en horas pico)
 * - Balance realista entre entradas y salidas
 * - Desconexiones aleatorias poco frecuentes
 */
class EventBasedSimulationService(private val application: Application) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val deviceRepository: DeviceRepository
    private val sensorEventRepository: SensorEventRepository
    private val settingsRepository: SettingsRepository
    private val notificationHandler: NotificationHandler

    private var simulationJob: Job? = null
    private var isRunning = false

    // Tracking para alertas
    private val recentEntriesMap = mutableMapOf<Long, MutableList<Long>>() // deviceId -> lista de timestamps de entradas
    private val lastAlertTimeMap = mutableMapOf<String, Long>() // "deviceId_alertType" -> timestamp

    companion object {
        private const val ALERT_THROTTLE_INTERVAL = 10 * 60 * 1000L // 10 minutos entre alertas del mismo tipo
        private const val TRAFFIC_PEAK_WINDOW = 5 * 60 * 1000L // Ventana de 5 minutos para pico de tráfico
    }

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
        settingsRepository = SettingsRepository(database.alertSettingsDao())
        notificationHandler = NotificationHandler(application)
    }

    /**
     * Inicia la simulación de eventos.
     *
     * @param eventIntervalMillis Intervalo promedio entre eventos (por defecto 2-8 segundos)
     */
    fun startSimulation(eventIntervalMillis: IntRange = 2000..8000) {
        if (isRunning) return

        isRunning = true
        simulationJob = scope.launch {
            while (isActive) {
                try {
                    generateEventsForActiveDevices()

                    // Esperar un tiempo aleatorio antes del próximo evento
                    val nextEventDelay = eventIntervalMillis.random().toLong()
                    delay(nextEventDelay)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopSimulation() {
        isRunning = false
        simulationJob?.cancel()
        simulationJob = null
    }

    private suspend fun generateEventsForActiveDevices() {
        val devices = deviceRepository.getAllDevices().first()

        devices.filter { it.isActive }.forEach { device ->
            // Probabilidad de que ocurra un evento en este ciclo (95% - aumentado para facilitar testing de alertas)
            if (Random.nextInt(100) < 95) {
                generateEventForDevice(device)
            }
        }
    }

    private suspend fun generateEventForDevice(device: Device) {
        // 10% de probabilidad de evento de desconexión
        if (Random.nextInt(100) < 10) {
            // Crear evento de desconexión
            val disconnectionEvent = SensorEvent(
                deviceId = device.id,
                eventType = EventType.DISCONNECTION,
                peopleCount = 0, // No aplicable para desconexiones
                timestamp = System.currentTimeMillis()
            )
            sensorEventRepository.insertEvent(disconnectionEvent)

            // Verificar si la alerta de desconexión está habilitada
            val alertSettings = settingsRepository.getAlertSettings().first()
            if (alertSettings.disconnectionAlertEnabled) {
                if (shouldSendAlert(device.id, "disconnection")) {
                    notificationHandler.showDisconnectionNotification(device.name)
                    updateLastAlertTime(device.id, "disconnection")
                }
            }
            return
        }

        // Obtener ocupación actual
        val currentOccupancy = sensorEventRepository.getCurrentOccupancy(device.id)

        // Decidir tipo de evento basado en ocupación
        val eventType = decideEventType(currentOccupancy, device.capacity)

        // Determinar tamaño del grupo
        val peopleCount = generateGroupSize()

        // Si es salida y no hay gente, no generar evento
        if (eventType == EventType.EXIT && currentOccupancy == 0) {
            return
        }

        // Asegurar que las salidas no excedan la ocupación actual
        val actualPeopleCount = if (eventType == EventType.EXIT) {
            minOf(peopleCount, currentOccupancy)
        } else {
            peopleCount
        }

        // Crear y guardar el evento
        val event = SensorEvent(
            deviceId = device.id,
            eventType = eventType,
            peopleCount = actualPeopleCount,
            timestamp = System.currentTimeMillis()
        )

        sensorEventRepository.insertEvent(event)

        // Verificar condiciones de alerta después de cada evento
        checkAlertConditions(device, event)
    }

    /**
     * Decide si el próximo evento será entrada o salida.
     *
     * Lógica modificada para facilitar testing de alertas:
     * - Crea patrones oscilantes entre ocupación baja y alta
     * - Cuando está alto (>75%): forzar bajada para trigger low occupancy alerts
     * - Cuando está bajo (<25%): forzar subida para trigger high occupancy + traffic peak alerts
     * - En medio: favorecer entradas para alcanzar umbrales altos
     */
    private fun decideEventType(currentOccupancy: Int, capacity: Int): EventType {
        if (currentOccupancy == 0) {
            return EventType.ENTRY
        }

        val occupancyPercentage = currentOccupancy.toFloat() / capacity.toFloat()

        return when {
            // Alta ocupación (>75%): Bajar rápidamente para trigger alertas de aforo bajo
            occupancyPercentage > 0.75f -> {
                if (Random.nextInt(100) < 80) EventType.EXIT else EventType.ENTRY
            }
            // Baja ocupación (<25%): Subir rápidamente para trigger alertas de aforo alto y peak
            occupancyPercentage < 0.25f -> {
                if (Random.nextInt(100) < 85) EventType.ENTRY else EventType.EXIT
            }
            // Ocupación media: Favorecer entradas para alcanzar umbrales altos
            else -> {
                if (Random.nextInt(100) < 70) EventType.ENTRY else EventType.EXIT
            }
        }
    }

    /**
     * Genera un tamaño de grupo para dispositivos Arduino.
     *
     * Los sensores Arduino solo pueden detectar una persona a la vez,
     * por lo que siempre retorna 1.
     *
     * NOTA: Código anterior soportaba grupos de 1-6 personas.
     * Se cambió para simular comportamiento de hardware real.
     */
    private fun generateGroupSize(): Int {
        // Arduino: solo eventos de 1 persona a la vez
        return 1

        /* Código anterior (grupos variables):
        return when (Random.nextInt(100)) {
            in 0..39 -> 1       // 40% solo
            in 40..69 -> 2      // 30% parejas
            in 70..89 -> 3      // 20% grupos pequeños
            else -> Random.nextInt(4, 7)  // 10% grupos grandes (4-6)
        }
        */
    }

    /**
     * Verifica las condiciones de alerta después de cada evento.
     *
     * @param device Dispositivo que generó el evento
     * @param event Evento recién insertado
     */
    private suspend fun checkAlertConditions(device: Device, event: SensorEvent) {
        // Obtener configuración de alertas
        val alertSettings = settingsRepository.getAlertSettings().first()

        // Obtener ocupación actual después del evento
        val currentOccupancy = sensorEventRepository.getCurrentOccupancy(device.id)

        // Trackear entradas para detección de pico de tráfico
        if (event.eventType == EventType.ENTRY) {
            val recentEntries = recentEntriesMap.getOrPut(device.id) { mutableListOf() }
            recentEntries.add(event.timestamp)

            // Limpiar entradas más antiguas que la ventana de 5 minutos
            val windowStart = event.timestamp - TRAFFIC_PEAK_WINDOW
            recentEntries.removeAll { it < windowStart }
        }

        // 1. Verificar Alerta de Aforo Bajo
        if (alertSettings.lowOccupancyEnabled) {
            val occupancyPercentage = if (device.capacity > 0) {
                (currentOccupancy.toFloat() / device.capacity) * 100
            } else {
                0f
            }

            if (occupancyPercentage < alertSettings.lowOccupancyThreshold) {
                if (shouldSendAlert(device.id, "low_occupancy")) {
                    notificationHandler.showLowOccupancyAlert(
                        deviceName = device.name,
                        currentOccupancy = currentOccupancy,
                        threshold = alertSettings.lowOccupancyThreshold
                    )
                    updateLastAlertTime(device.id, "low_occupancy")
                }
            }
        }

        // 2. Verificar Alerta de Aforo Alto
        if (alertSettings.highOccupancyEnabled) {
            val occupancyPercentage = if (device.capacity > 0) {
                (currentOccupancy.toFloat() / device.capacity) * 100
            } else {
                0f
            }

            if (occupancyPercentage >= alertSettings.highOccupancyThreshold) {
                if (shouldSendAlert(device.id, "high_occupancy")) {
                    notificationHandler.showHighOccupancyAlert(
                        deviceName = device.name,
                        currentOccupancy = currentOccupancy,
                        capacity = device.capacity
                    )
                    updateLastAlertTime(device.id, "high_occupancy")
                }
            }
        }

        // 3. Verificar Alerta de Peak de Tráfico
        if (alertSettings.trafficPeakEnabled && event.eventType == EventType.ENTRY) {
            val recentEntries = recentEntriesMap[device.id] ?: emptyList()

            if (recentEntries.size >= alertSettings.trafficPeakThreshold) {
                if (shouldSendAlert(device.id, "traffic_peak")) {
                    notificationHandler.showTrafficPeakAlert(
                        deviceName = device.name,
                        entriesCount = recentEntries.size
                    )
                    updateLastAlertTime(device.id, "traffic_peak")
                }
            }
        }
    }

    /**
     * Determina si se debe enviar una alerta basándose en el throttling.
     *
     * Evita spam de notificaciones del mismo tipo en corto tiempo.
     *
     * @param deviceId ID del dispositivo
     * @param alertType Tipo de alerta ("low_occupancy", "high_occupancy", "traffic_peak")
     * @return true si han pasado suficiente tiempo desde la última alerta de este tipo
     */
    private fun shouldSendAlert(deviceId: Long, alertType: String): Boolean {
        val key = "${deviceId}_${alertType}"
        val lastAlertTime = lastAlertTimeMap[key] ?: 0L
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastAlertTime) >= ALERT_THROTTLE_INTERVAL
    }

    /**
     * Actualiza el timestamp de la última alerta enviada.
     *
     * @param deviceId ID del dispositivo
     * @param alertType Tipo de alerta
     */
    private fun updateLastAlertTime(deviceId: Long, alertType: String) {
        val key = "${deviceId}_${alertType}"
        lastAlertTimeMap[key] = System.currentTimeMillis()
    }

    fun cleanup() {
        stopSimulation()
        scope.cancel()
    }
}
