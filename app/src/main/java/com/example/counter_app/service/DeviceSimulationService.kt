package com.example.counter_app.service

import android.app.Application
import com.example.counter_app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class DeviceSimulationService(private val application: Application) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val deviceRepository: DeviceRepository
    private val sensorReadingRepository: SensorReadingRepository

    private var simulationJob: Job? = null
    private var isRunning = false

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    fun startSimulation(intervalSeconds: Int = 5) {
        if (isRunning) return

        isRunning = true
        simulationJob = scope.launch {
            while (isActive) {
                try {
                    simulateReadingsForActiveDevices()
                    delay(intervalSeconds * 1000L)
                } catch (e: Exception) {
                    // Log error but continue simulation
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

    private suspend fun simulateReadingsForActiveDevices() {
        val devices = deviceRepository.getAllDevices().first()

        devices.filter { it.isActive }.forEach { device ->
            val lastReading = sensorReadingRepository.getLatestReading(device.id).first()

            val (newEntered, newLeft) = if (lastReading != null) {
                generateIncrementalData(lastReading.entered, lastReading.left)
            } else {
                generateInitialData()
            }

            val reading = SensorReading(
                deviceId = device.id,
                entered = newEntered,
                left = newLeft,
                capacity = device.capacity
            )

            sensorReadingRepository.insertReading(reading)
        }
    }

    private fun generateInitialData(): Pair<Int, Int> {
        // Start from zero for new devices
        return Pair(0, 0)
    }

    private fun generateIncrementalData(currentEntered: Int, currentLeft: Int): Pair<Int, Int> {
        val currentInside = currentEntered - currentLeft

        // High traffic simulation for mall store
        // 70% chance of activity (high foot traffic)
        val hasActivity = Random.nextInt(0, 100) < 70

        if (!hasActivity) {
            return Pair(currentEntered, currentLeft)
        }

        // Determine activity type with bias towards entries (mall attracts people)
        val activityType = Random.nextInt(0, 100)

        val newEntered = when {
            // 50% chance of people entering (groups of 1-4)
            activityType < 50 -> {
                val groupSize = when (Random.nextInt(0, 100)) {
                    in 0..40 -> 1  // 40% solo
                    in 41..70 -> 2  // 30% couples
                    in 71..90 -> 3  // 20% small groups
                    else -> Random.nextInt(4, 6)  // 10% larger groups
                }
                currentEntered + groupSize
            }
            // 35% chance of people leaving
            activityType < 85 -> currentEntered
            // 15% chance of simultaneous entry and exit (rush)
            else -> currentEntered + Random.nextInt(1, 4)
        }

        val newLeft = when {
            // If entries happened, some might leave
            newEntered > currentEntered && currentInside > 0 -> {
                if (Random.nextInt(0, 100) < 30) {  // 30% chance exits happen with entries
                    currentLeft + Random.nextInt(1, minOf(currentInside, 3))
                } else {
                    currentLeft
                }
            }
            // If no entries, people might still leave
            activityType in 50..84 && currentInside > 0 -> {
                val exitGroupSize = minOf(Random.nextInt(1, 4), currentInside)
                currentLeft + exitGroupSize
            }
            // Simultaneous rush (last case from above)
            activityType >= 85 && currentInside > 0 -> {
                currentLeft + Random.nextInt(1, minOf(currentInside + 1, 3))
            }
            else -> currentLeft
        }

        return Pair(newEntered, newLeft)
    }

    fun cleanup() {
        stopSimulation()
        scope.cancel()
    }
}
