package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChartData(
    val timestamp: Long,
    val entered: Int,
    val left: Int
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorReadingRepository: SensorReadingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    private val _selectedDeviceId = MutableStateFlow<Long?>(null)
    private val _dateRange = MutableStateFlow(DateRange.TODAY)

    val devices: Flow<List<Device>> = deviceRepository.getAllDevices()

    val chartData: Flow<List<ChartData>> = combine(
        _selectedDeviceId,
        _dateRange
    ) { deviceId, range ->
        Pair(deviceId, range)
    }.flatMapLatest { (deviceId, range) ->
        if (deviceId != null) {
            val (startTime, endTime) = range.getTimeRange()
            sensorReadingRepository.getReadingsByDateRange(deviceId, startTime, endTime)
                .map { readings ->
                    readings.map { reading ->
                        ChartData(reading.timestamp, reading.entered, reading.left)
                    }
                }
        } else {
            flowOf(emptyList())
        }
    }

    fun setSelectedDevice(deviceId: Long) {
        _selectedDeviceId.value = deviceId
    }

    fun setDateRange(range: DateRange) {
        _dateRange.value = range
    }

    enum class DateRange {
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS;

        fun getTimeRange(): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = when (this) {
                TODAY -> calendar.timeInMillis
                LAST_7_DAYS -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.timeInMillis
                }
                LAST_30_DAYS -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    calendar.timeInMillis
                }
            }

            return Pair(startTime, endTime)
        }
    }
}
