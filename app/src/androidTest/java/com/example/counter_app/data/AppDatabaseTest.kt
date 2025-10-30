package com.example.counter_app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Test de integración para Room Database.
 *
 * Verifica:
 * - Creación de base de datos en memoria
 * - Operaciones CRUD en todas las entidades
 * - Relaciones entre tablas (Foreign Keys)
 * - Cascade delete functionality
 * - Queries complejas con múltiples tablas
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var deviceDao: DeviceDao
    private lateinit var sensorReadingDao: SensorReadingDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        userDao = database.userDao()
        deviceDao = database.deviceDao()
        sensorReadingDao = database.sensorReadingDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    // ==================== User Tests ====================

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        // Arrange
        val user = User(
            username = "testuser",
            passwordHash = "hashedpassword123",
            email = "test@example.com"
        )

        // Act
        userDao.insert(user)
        val retrievedUser = userDao.getUserByUsername("testuser").first()

        // Assert
        assertNotNull(retrievedUser)
        assertEquals("testuser", retrievedUser?.username)
        assertEquals("hashedpassword123", retrievedUser?.passwordHash)
        assertEquals("test@example.com", retrievedUser?.email)
    }

    @Test
    fun updateUser() = runBlocking {
        // Arrange
        val user = User(username = "user1", passwordHash = "hash1", email = "old@example.com")
        userDao.insert(user)
        val insertedUser = userDao.getUserByUsername("user1").first()!!

        // Act
        val updatedUser = insertedUser.copy(email = "new@example.com")
        userDao.update(updatedUser)
        val result = userDao.getUserByUsername("user1").first()

        // Assert
        assertEquals("new@example.com", result?.email)
    }

    @Test
    fun deleteUser() = runBlocking {
        // Arrange
        val user = User(username = "deleteMe", passwordHash = "hash", email = "delete@example.com")
        userDao.insert(user)

        // Act
        val insertedUser = userDao.getUserByUsername("deleteMe").first()!!
        userDao.delete(insertedUser)
        val result = userDao.getUserByUsername("deleteMe").first()

        // Assert
        assertNull(result)
    }

    // ==================== Device Tests ====================

    @Test
    fun insertAndGetDevice() = runBlocking {
        // Arrange
        val device = Device(
            name = "Arduino Store 1",
            type = "Arduino Uno",
            macAddress = "AA:BB:CC:DD:EE:FF",
            capacity = 100,
            location = "Mall Floor 1",
            isActive = true
        )

        // Act
        val deviceId = deviceDao.insert(device)
        val retrievedDevice = deviceDao.getDeviceById(deviceId).first()

        // Assert
        assertNotNull(retrievedDevice)
        assertEquals("Arduino Store 1", retrievedDevice?.name)
        assertEquals("Arduino Uno", retrievedDevice?.type)
        assertEquals(100, retrievedDevice?.capacity)
        assertEquals("Mall Floor 1", retrievedDevice?.location)
    }

    @Test
    fun getAllDevices() = runBlocking {
        // Arrange
        val device1 = Device(name = "Device 1", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:01", capacity = 100, location = "Store 1")
        val device2 = Device(name = "Device 2", type = "ESP32", macAddress = "AA:BB:CC:DD:EE:02", capacity = 150, location = "Store 2")
        val device3 = Device(name = "Device 3", type = "NodeMCU", macAddress = "AA:BB:CC:DD:EE:03", capacity = 80, location = "Store 3")

        // Act
        deviceDao.insert(device1)
        deviceDao.insert(device2)
        deviceDao.insert(device3)
        val devices = deviceDao.getAllDevices().first()

        // Assert
        assertEquals(3, devices.size)
    }

    @Test
    fun getActiveDevicesOnly() = runBlocking {
        // Arrange
        val activeDevice = Device(name = "Active", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:01", capacity = 100, location = "Store 1", isActive = true)
        val inactiveDevice = Device(name = "Inactive", type = "ESP32", macAddress = "AA:BB:CC:DD:EE:02", capacity = 150, location = "Store 2", isActive = false)

        // Act
        deviceDao.insert(activeDevice)
        deviceDao.insert(inactiveDevice)
        val activeDevices = deviceDao.getActiveDevices().first()

        // Assert
        assertEquals(1, activeDevices.size)
        assertEquals("Active", activeDevices[0].name)
        assertTrue(activeDevices[0].isActive)
    }

    @Test
    fun updateDevice() = runBlocking {
        // Arrange
        val device = Device(name = "Original", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Original Location")
        val deviceId = deviceDao.insert(device)

        // Act
        val insertedDevice = deviceDao.getDeviceById(deviceId).first()!!
        val updatedDevice = insertedDevice.copy(name = "Updated", location = "New Location", isActive = false)
        deviceDao.update(updatedDevice)
        val result = deviceDao.getDeviceById(deviceId).first()

        // Assert
        assertEquals("Updated", result?.name)
        assertEquals("New Location", result?.location)
        assertFalse(result?.isActive ?: true)
    }

    // ==================== SensorReading Tests ====================

    @Test
    fun insertAndGetSensorReading() = runBlocking {
        // Arrange - First create a device
        val device = Device(name = "Test Device", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Store 1")
        val deviceId = deviceDao.insert(device)

        val reading = SensorReading(
            deviceId = deviceId,
            entered = 50,
            left = 20,
            capacity = 100,
            timestamp = System.currentTimeMillis()
        )

        // Act
        sensorReadingDao.insert(reading)
        val latestReading = sensorReadingDao.getLatestReadingForDevice(deviceId).first()

        // Assert
        assertNotNull(latestReading)
        assertEquals(deviceId, latestReading?.deviceId)
        assertEquals(50, latestReading?.entered)
        assertEquals(20, latestReading?.left)
        assertEquals(100, latestReading?.capacity)
    }

    @Test
    fun getRecentReadingsLimited() = runBlocking {
        // Arrange
        val device = Device(name = "Test Device", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Store 1")
        val deviceId = deviceDao.insert(device)

        // Insert 15 readings
        repeat(15) { index ->
            val reading = SensorReading(
                deviceId = deviceId,
                entered = 10 + index,
                left = 5 + index,
                capacity = 100,
                timestamp = System.currentTimeMillis() + index
            )
            sensorReadingDao.insert(reading)
        }

        // Act
        val recentReadings = sensorReadingDao.getRecentReadingsForDevice(deviceId, limit = 10).first()

        // Assert
        assertEquals(10, recentReadings.size)
    }

    @Test
    fun clearReadingsForDevice() = runBlocking {
        // Arrange
        val device = Device(name = "Test Device", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Store 1")
        val deviceId = deviceDao.insert(device)

        repeat(5) {
            val reading = SensorReading(deviceId = deviceId, entered = 10, left = 5, capacity = 100)
            sensorReadingDao.insert(reading)
        }

        // Act
        sensorReadingDao.clearReadingsForDevice(deviceId)
        val readings = sensorReadingDao.getRecentReadingsForDevice(deviceId).first()

        // Assert
        assertTrue(readings.isEmpty())
    }

    // ==================== Foreign Key & Cascade Tests ====================

    @Test
    fun cascadeDeleteDeviceRemovesReadings() = runBlocking {
        // Arrange
        val device = Device(name = "Test Device", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Store 1")
        val deviceId = deviceDao.insert(device)

        // Insert multiple readings
        repeat(5) {
            val reading = SensorReading(deviceId = deviceId, entered = 10, left = 5, capacity = 100)
            sensorReadingDao.insert(reading)
        }

        // Verify readings exist
        val readingsBefore = sensorReadingDao.getRecentReadingsForDevice(deviceId).first()
        assertEquals(5, readingsBefore.size)

        // Act - Delete the device (should cascade to readings)
        val deviceToDelete = deviceDao.getDeviceById(deviceId).first()!!
        deviceDao.delete(deviceToDelete)

        // Assert - Readings should be gone due to CASCADE
        val readingsAfter = sensorReadingDao.getRecentReadingsForDevice(deviceId).first()
        assertTrue(readingsAfter.isEmpty())
    }

    // ==================== Complex Query Tests ====================

    @Test
    fun multipleDevicesWithReadings() = runBlocking {
        // Arrange
        val device1 = Device(name = "Device 1", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:01", capacity = 100, location = "Store 1")
        val device2 = Device(name = "Device 2", type = "ESP32", macAddress = "AA:BB:CC:DD:EE:02", capacity = 150, location = "Store 2")

        val deviceId1 = deviceDao.insert(device1)
        val deviceId2 = deviceDao.insert(device2)

        // Insert readings for both devices
        repeat(3) {
            sensorReadingDao.insert(SensorReading(deviceId = deviceId1, entered = 10 + it, left = 5, capacity = 100))
            sensorReadingDao.insert(SensorReading(deviceId = deviceId2, entered = 20 + it, left = 10, capacity = 150))
        }

        // Act
        val readingsDevice1 = sensorReadingDao.getRecentReadingsForDevice(deviceId1).first()
        val readingsDevice2 = sensorReadingDao.getRecentReadingsForDevice(deviceId2).first()

        // Assert
        assertEquals(3, readingsDevice1.size)
        assertEquals(3, readingsDevice2.size)
        assertTrue(readingsDevice1.all { it.deviceId == deviceId1 })
        assertTrue(readingsDevice2.all { it.deviceId == deviceId2 })
    }

    @Test
    fun latestReadingIsActuallyLatest() = runBlocking {
        // Arrange
        val device = Device(name = "Test Device", type = "Arduino Uno", macAddress = "AA:BB:CC:DD:EE:FF", capacity = 100, location = "Store 1")
        val deviceId = deviceDao.insert(device)

        val timestamp1 = System.currentTimeMillis()
        val timestamp2 = timestamp1 + 1000
        val timestamp3 = timestamp2 + 1000

        // Insert readings out of order
        sensorReadingDao.insert(SensorReading(deviceId = deviceId, entered = 10, left = 5, capacity = 100, timestamp = timestamp2))
        sensorReadingDao.insert(SensorReading(deviceId = deviceId, entered = 30, left = 15, capacity = 100, timestamp = timestamp3))
        sensorReadingDao.insert(SensorReading(deviceId = deviceId, entered = 5, left = 2, capacity = 100, timestamp = timestamp1))

        // Act
        val latestReading = sensorReadingDao.getLatestReadingForDevice(deviceId).first()

        // Assert
        assertNotNull(latestReading)
        assertEquals(30, latestReading?.entered)
        assertEquals(timestamp3, latestReading?.timestamp)
    }
}
