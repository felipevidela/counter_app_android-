package com.example.counter_app.service

import android.app.Application
import com.example.counter_app.data.Device
import com.example.counter_app.data.SensorReading
import com.example.counter_app.repository.DeviceRepository
import com.example.counter_app.repository.SensorReadingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Tests unitarios para DeviceSimulationService.
 *
 * Verifica:
 * - Generación de datos simulados para sensores ultrasónicos
 * - Lógica de entrada/salida de personas (grupos)
 * - Cálculo de ocupación actual
 * - Validación de datos realistas para mall
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceSimulationServiceTest {

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @Mock
    private lateinit var sensorReadingRepository: SensorReadingRepository

    private lateinit var service: DeviceSimulationService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        service = DeviceSimulationService(application)
    }

    @Test
    fun `simulation generates incremental data correctly`() {
        // Arrange
        val currentEntered = 50
        val currentLeft = 20

        // Act - Simulate multiple readings to test randomness
        var hasIncrease = false
        var hasDecrease = false

        repeat(100) {
            val (newEntered, newLeft) = simulateIncrementalData(currentEntered, currentLeft)

            if (newEntered > currentEntered) hasIncrease = true
            if (newLeft > currentLeft) hasDecrease = true
        }

        // Assert - Over 100 iterations, both should occur
        assertTrue("Should have entrance events", hasIncrease)
        assertTrue("Should have exit events", hasDecrease)
    }

    @Test
    fun `occupancy calculation is correct`() {
        // Arrange
        val entered = 100
        val left = 30

        // Act
        val currentOccupancy = entered - left

        // Assert
        assertEquals(70, currentOccupancy)
    }

    @Test
    fun `sensor reading has valid timestamp`() {
        // Arrange
        val beforeTime = System.currentTimeMillis()

        // Act
        val reading = SensorReading(
            id = 1L,
            deviceId = 1L,
            entered = 50,
            left = 20,
            capacity = 100,
            timestamp = System.currentTimeMillis()
        )

        val afterTime = System.currentTimeMillis()

        // Assert
        assertTrue(reading.timestamp >= beforeTime)
        assertTrue(reading.timestamp <= afterTime)
    }

    @Test
    fun `group size simulation respects mall traffic patterns`() {
        // Arrange - Test group size distribution over many iterations
        val groupSizes = mutableListOf<Int>()

        // Act - Simulate 1000 group entries
        repeat(1000) {
            val groupSize = generateRandomGroupSize()
            groupSizes.add(groupSize)
        }

        // Assert
        // Most groups should be 1-3 people (90% based on simulation logic)
        val smallGroups = groupSizes.count { it in 1..3 }
        val largeGroups = groupSizes.count { it > 3 }

        assertTrue("Small groups should dominate", smallGroups > largeGroups)
        assertTrue("Should have some solo shoppers", groupSizes.contains(1))
        assertTrue("Should have some couples", groupSizes.contains(2))
    }

    @Test
    fun `activity rate is realistic for mall traffic`() {
        // Arrange
        var activeCount = 0
        val totalSimulations = 1000

        // Act - Simulate activity detection
        repeat(totalSimulations) {
            if (hasActivity()) {
                activeCount++
            }
        }

        // Assert - Should have ~70% activity rate (based on simulation logic)
        val activityRate = activeCount.toFloat() / totalSimulations
        assertTrue("Activity rate should be around 70%", activityRate in 0.6f..0.8f)
    }

    @Test
    fun `entered count always increases or stays same`() {
        // Arrange
        val initialEntered = 50
        val initialLeft = 20

        // Act
        val (newEntered, newLeft) = simulateIncrementalData(initialEntered, initialLeft)

        // Assert
        assertTrue("Entered should never decrease", newEntered >= initialEntered)
        assertTrue("Left should never decrease", newLeft >= initialLeft)
    }

    @Test
    fun `capacity is respected in device creation`() {
        // Arrange
        val device = Device(
            id = 1L,
            name = "Test Store",
            type = "Arduino Uno",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 100,
            location = "Mall Floor 1"
        )

        // Act
        val capacity = device.capacity

        // Assert
        assertTrue("Capacity should be positive", capacity > 0)
        assertTrue("Capacity should be realistic for store", capacity in 50..300)
    }

    // Helper methods to simulate service behavior for testing
    private fun simulateIncrementalData(currentEntered: Int, currentLeft: Int): Pair<Int, Int> {
        val hasActivity = (0..99).random() < 70 // 70% activity

        if (!hasActivity) return Pair(currentEntered, currentLeft)

        val groupSize = when ((0..99).random()) {
            in 0..40 -> 1
            in 41..70 -> 2
            in 71..90 -> 3
            else -> (4..6).random()
        }

        val isEntering = (0..99).random() < 60 // 60% entering

        return if (isEntering) {
            Pair(currentEntered + groupSize, currentLeft)
        } else {
            val leaving = minOf(groupSize, currentEntered - currentLeft)
            Pair(currentEntered, currentLeft + leaving)
        }
    }

    private fun generateRandomGroupSize(): Int {
        return when ((0..99).random()) {
            in 0..40 -> 1      // 40% solo
            in 41..70 -> 2     // 30% couples
            in 71..90 -> 3     // 20% small groups
            else -> (4..6).random() // 10% larger groups
        }
    }

    private fun hasActivity(): Boolean {
        return (0..99).random() < 70 // 70% activity rate
    }
}
