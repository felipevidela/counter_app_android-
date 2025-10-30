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
                    // Convert cumulative values to incremental deltas
                    if (readings.isEmpty()) {
                        emptyList()
                    } else {
                        // Calculate deltas between consecutive readings
                        val chartDataList = mutableListOf<ChartData>()

                        // First reading shows its values as-is (starting point)
                        chartDataList.add(
                            ChartData(
                                timestamp = readings[0].timestamp,
                                entered = readings[0].entered,
                                left = readings[0].left
                            )
                        )

                        // For subsequent readings, calculate the delta from previous
                        for (i in 1 until readings.size) {
                            val current = readings[i]
                            val previous = readings[i - 1]

                            val enteredDelta = (current.entered - previous.entered).coerceAtLeast(0)
                            val leftDelta = (current.left - previous.left).coerceAtLeast(0)

                            chartDataList.add(
                                ChartData(
                                    timestamp = current.timestamp,
                                    entered = enteredDelta,
                                    left = leftDelta
                                )
                            )
                        }

                        chartDataList
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
