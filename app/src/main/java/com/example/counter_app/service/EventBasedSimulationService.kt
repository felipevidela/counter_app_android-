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
 *
 * ## Patrones Simulados:
 * - Grupos de tamaño variable (1-6 personas)
 * - Frecuencia ajustable (más eventos en horas pico)
 * - Balance realista entre entradas y salidas
 */
class EventBasedSimulationService(private val application: Application) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val deviceRepository: DeviceRepository
    private val sensorEventRepository: SensorEventRepository

    private var simulationJob: Job? = null
    private var isRunning = false

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
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
            // Probabilidad de que ocurra un evento en este ciclo (70%)
            if (Random.nextInt(100) < 70) {
                generateEventForDevice(device)
            }
        }
    }

    private suspend fun generateEventForDevice(device: Device) {
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
    }

    /**
     * Decide si el próximo evento será entrada o salida.
     *
     * Lógica:
     * - Si está vacío: solo entradas
     * - Si está lleno/cerca: más salidas
     * - Normal: 60% entradas, 40% salidas (mall atrae gente)
     */
    private fun decideEventType(currentOccupancy: Int, capacity: Int): EventType {
        if (currentOccupancy == 0) {
            return EventType.ENTRY
        }

        val occupancyPercentage = currentOccupancy.toFloat() / capacity.toFloat()

        return when {
            // Si está sobre el 90%, favorecer salidas
            occupancyPercentage > 0.9f -> {
                if (Random.nextInt(100) < 70) EventType.EXIT else EventType.ENTRY
            }
            // Si está muy vacío, favorecer entradas
            occupancyPercentage < 0.2f -> {
                if (Random.nextInt(100) < 80) EventType.ENTRY else EventType.EXIT
            }
            // Normal: 60% entradas, 40% salidas
            else -> {
                if (Random.nextInt(100) < 60) EventType.ENTRY else EventType.EXIT
            }
        }
    }

    /**
     * Genera un tamaño de grupo realista para un mall.
     *
     * Distribución:
     * - 40% personas solas (1)
     * - 30% parejas (2)
     * - 20% grupos pequeños (3)
     * - 10% grupos grandes (4-6)
     */
    private fun generateGroupSize(): Int {
        return when (Random.nextInt(100)) {
            in 0..39 -> 1       // 40% solo
            in 40..69 -> 2      // 30% parejas
            in 70..89 -> 3      // 20% grupos pequeños
            else -> Random.nextInt(4, 7)  // 10% grupos grandes (4-6)
        }
    }

    fun cleanup() {
        stopSimulation()
        scope.cancel()
    }
}
