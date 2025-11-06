package com.example.counter_app.repository

import com.example.counter_app.data.Device
import com.example.counter_app.data.DeviceDao
import com.example.counter_app.data.DeviceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Tests unitarios para DeviceRepository.
 *
 * Verifica:
 * - Inserción de dispositivos
 * - Obtención de dispositivos por ID
 * - Listado de todos los dispositivos
 * - Actualización de dispositivos
 * - Eliminación de dispositivos
 */
class DeviceRepositoryTest {

    @Mock
    private lateinit var deviceDao: DeviceDao

    private lateinit var repository: DeviceRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = DeviceRepository(deviceDao)
    }

    @Test
    fun `insert device calls DAO insertDevice`() = runTest {
        // Arrange
        val device = Device(
            id = 1L,
            name = "Test Device",
            type = "Arduino Uno",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 100,
            location = "Store 1",
            isActive = true
        )
        `when`(deviceDao.insertDevice(device)).thenReturn(1L)

        // Act
        val result = repository.insertDevice(device)

        // Assert
        assertEquals(1L, result)
        verify(deviceDao, times(1)).insertDevice(device)
    }

    @Test
    fun `getDeviceById returns device from DAO`() = runTest {
        // Arrange
        val deviceId = 1L
        val expectedDevice = Device(
            id = deviceId,
            name = "Test Device",
            type = "Arduino Nano",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 50,
            location = "Store 2"
        )
        `when`(deviceDao.getDeviceByIdFlow(deviceId)).thenReturn(flowOf(expectedDevice))

        // Act
        val result = repository.getDeviceById(deviceId).first()

        // Assert
        assertEquals(expectedDevice, result)
        verify(deviceDao, times(1)).getDeviceByIdFlow(deviceId)
    }

    @Test
    fun `getAllDevices returns all devices from DAO`() = runTest {
        // Arrange
        val devices = listOf(
            Device(1L, "Device 1", "Arduino Uno", "AA:BB:CC:DD:EE:01", 100, "Store 1"),
            Device(2L, "Device 2", "ESP32", "AA:BB:CC:DD:EE:02", 150, "Store 2"),
            Device(3L, "Device 3", "NodeMCU", "AA:BB:CC:DD:EE:03", 80, "Store 3")
        )
        `when`(deviceDao.getAllDevices()).thenReturn(flowOf(devices))

        // Act
        val result = repository.getAllDevices().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals(devices, result)
        verify(deviceDao, times(1)).getAllDevices()
    }

    @Test
    fun `update device calls DAO updateDevice`() = runTest {
        // Arrange
        val device = Device(
            id = 1L,
            name = "Updated Device",
            type = "ESP32",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 200,
            location = "Updated Location",
            isActive = false
        )

        // Act
        repository.updateDevice(device)

        // Assert
        verify(deviceDao, times(1)).updateDevice(device)
    }

    @Test
    fun `delete device calls DAO deleteDevice`() = runTest {
        // Arrange
        val device = Device(
            id = 1L,
            name = "Device to Delete",
            type = "Arduino Nano",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 100,
            location = "Store 1"
        )

        // Act
        repository.deleteDevice(device)

        // Assert
        verify(deviceDao, times(1)).deleteDevice(device)
    }

    @Test
    fun `updateDeviceActiveStatus calls DAO updateDeviceActiveStatus`() = runTest {
        // Arrange
        val deviceId = 1L
        val isActive = true

        // Act
        repository.updateDeviceActiveStatus(deviceId, isActive)

        // Assert
        verify(deviceDao, times(1)).updateDeviceActiveStatus(deviceId, isActive)
    }
}
