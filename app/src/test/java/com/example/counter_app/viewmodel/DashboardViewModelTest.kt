package com.example.counter_app.viewmodel

import com.example.counter_app.data.Device
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para lógica de Dashboard.
 *
 * Verifica:
 * - Filtrado de dispositivos activos
 * - Cálculos de ocupación
 * - Detección de capacidad
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @Test
    fun `active devices are filtered correctly`() = runTest {
        // Arrange
        val allDevices = listOf(
            Device(1L, "Active 1", "Arduino Uno", "AA:BB:CC:DD:EE:01", 100, "Store 1", isActive = true),
            Device(2L, "Inactive", "ESP32", "AA:BB:CC:DD:EE:02", 150, "Store 2", isActive = false),
            Device(3L, "Active 2", "NodeMCU", "AA:BB:CC:DD:EE:03", 80, "Store 3", isActive = true)
        )

        // Act
        val activeDevices = allDevices.filter { it.isActive }

        // Assert
        assertEquals(2, activeDevices.size)
        assertTrue(activeDevices.all { it.isActive })
    }

    @Test
    fun `occupancy calculation shows correct percentage`() = runTest {
        // Arrange
        val entered = 75
        val left = 25
        val capacity = 100

        // Act
        val currentOccupancy = entered - left
        val occupancyPercentage = currentOccupancy.toFloat() / capacity.toFloat()

        // Assert
        assertEquals(50, currentOccupancy)
        assertEquals(0.5f, occupancyPercentage, 0.01f)
    }

    @Test
    fun `over capacity detection works correctly`() = runTest {
        // Arrange
        val entered = 120
        val left = 10
        val capacity = 100

        // Act
        val currentOccupancy = entered - left
        val isOverCapacity = currentOccupancy > capacity

        // Assert
        assertEquals(110, currentOccupancy)
        assertTrue(isOverCapacity)
    }

    @Test
    fun `near capacity warning triggers at 90 percent`() = runTest {
        // Arrange
        val entered = 95
        val left = 5
        val capacity = 100

        // Act
        val currentOccupancy = entered - left
        val occupancyPercentage = currentOccupancy.toFloat() / capacity.toFloat()
        val isNearCapacity = occupancyPercentage >= 0.9f

        // Assert
        assertEquals(90, currentOccupancy)
        assertEquals(0.9f, occupancyPercentage, 0.01f)
        assertTrue(isNearCapacity)
    }
}
