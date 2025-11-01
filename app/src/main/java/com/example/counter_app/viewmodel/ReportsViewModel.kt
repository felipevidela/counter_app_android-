package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.*
import com.example.counter_app.domain.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

/**
 * ViewModel SIMPLIFICADO para la pantalla de Reportes.
 *
 * Enfoque minimalista:
 * - Flow reactivo directo desde Room
 * - Transformación simple SensorEvent → ChartPoint
 * - Sin procesadores complejos
 * - Sin bucketing ni downsampling
 * - Actualización automática en tiempo real
 */
class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository: DeviceRepository
    private val sensorEventRepository: SensorEventRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
    }

    // Estado de selección
    private val _selectedDeviceId = MutableStateFlow<Long?>(null)
    private val _dateRange = MutableStateFlow(DateRange.TODAY)

    // Dispositivos disponibles
    val devices: Flow<List<Device>> = deviceRepository.getAllDevices()

    /**
     * Datos del gráfico - Flow reactivo que se actualiza automáticamente.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chartData: Flow<OccupancyChartData> = combine(
        _selectedDeviceId,
        _dateRange
    ) { deviceId, range ->
        Pair(deviceId, range)
    }.flatMapLatest { (deviceId, range) ->
        if (deviceId != null) {
            val (startTime, endTime) = range.getTimeRange()

            // Flow reactivo desde Room
            sensorEventRepository.getEventsByDateRange(deviceId, startTime, endTime)
                .map { events ->
                    transformEventsToOccupancyData(events)
                }
        } else {
            flowOf(OccupancyChartData.EMPTY)
        }
    }

    /**
     * Estadísticas - calculadas automáticamente desde eventos.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val stats: Flow<ReportStats> = combine(
        _selectedDeviceId,
        _dateRange
    ) { deviceId, range ->
        Pair(deviceId, range)
    }.flatMapLatest { (deviceId, range) ->
        if (deviceId != null) {
            val (startTime, endTime) = range.getTimeRange()
            sensorEventRepository.getEventsByDateRange(deviceId, startTime, endTime)
                .combine(chartData) { events, occupancyData ->
                    calculateStatsFromEvents(events, occupancyData)
                }
        } else {
            flowOf(ReportStats.EMPTY)
        }
    }

    // Funciones públicas para actualizar estado
    fun setSelectedDevice(deviceId: Long) {
        _selectedDeviceId.value = deviceId
    }

    fun setDateRange(range: DateRange) {
        _dateRange.value = range
    }

    /**
     * Transformación SIMPLE de eventos a datos de aforo.
     * Calcula el aforo (ocupación) en cada punto del tiempo.
     */
    private fun transformEventsToOccupancyData(events: List<SensorEvent>): OccupancyChartData {
        if (events.isEmpty()) {
            return OccupancyChartData.EMPTY
        }

        // Ordenar por timestamp
        val sortedEvents = events.sortedBy { it.timestamp }

        // Calcular aforo en cada punto
        var currentOccupancy = 0
        val occupancyPoints = mutableListOf<ChartPoint>()

        sortedEvents.forEach { event ->
            when (event.eventType) {
                EventType.ENTRY -> {
                    currentOccupancy += event.peopleCount
                }
                EventType.EXIT -> {
                    currentOccupancy -= event.peopleCount
                }
            }

            // El aforo nunca puede ser negativo
            currentOccupancy = maxOf(0, currentOccupancy)

            occupancyPoints.add(
                ChartPoint(
                    timestamp = event.timestamp,
                    value = currentOccupancy.toFloat()
                )
            )
        }

        val maxValue = (occupancyPoints.maxOfOrNull { it.value } ?: 0f) * 1.2f // 20% padding

        return OccupancyChartData(
            occupancyPoints = occupancyPoints,
            maxValue = maxValue,
            minValue = 0f
        )
    }

    /**
     * Cálculo SIMPLE de estadísticas desde eventos y datos de aforo.
     */
    private fun calculateStatsFromEvents(
        events: List<SensorEvent>,
        occupancyData: OccupancyChartData
    ): ReportStats {
        if (events.isEmpty()) {
            return ReportStats.EMPTY
        }

        // Calcular totales de entradas y salidas
        val totalEntries = events
            .filter { it.eventType == EventType.ENTRY }
            .sumOf { it.peopleCount }

        val totalExits = events
            .filter { it.eventType == EventType.EXIT }
            .sumOf { it.peopleCount }

        // Aforo actual y máximo desde los datos del gráfico
        val currentOccupancy = occupancyData.occupancyPoints.lastOrNull()?.value?.toInt() ?: 0
        val peakOccupancy = occupancyData.occupancyPoints.maxOfOrNull { it.value }?.toInt() ?: 0

        // Calcular tiempo promedio de visita usando área bajo la curva
        val avgDwellMinutes = if (occupancyData.occupancyPoints.size > 1 && totalExits > 0) {
            val points = occupancyData.occupancyPoints
            var totalPersonMinutes = 0.0

            // Calcular área bajo la curva (personas-minuto acumulados)
            for (i in 0 until points.size - 1) {
                val currentOccupancy = points[i].value
                val nextTimestamp = points[i + 1].timestamp
                val currentTimestamp = points[i].timestamp
                val durationMinutes = (nextTimestamp - currentTimestamp) / (1000.0 * 60.0)

                // Área del trapecio (aproximación): aforo actual × duración
                totalPersonMinutes += currentOccupancy * durationMinutes
            }

            // Promedio: personas-minuto totales / número de salidas
            (totalPersonMinutes / totalExits).toInt()
        } else {
            0
        }

        return ReportStats(
            totalEntries = totalEntries,
            totalExits = totalExits,
            currentOccupancy = currentOccupancy,
            peakOccupancy = peakOccupancy,
            avgDwellTimeMinutes = avgDwellMinutes
        )
    }

    /**
     * Rangos de fechas disponibles.
     */
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

        fun getDisplayName(): String = when (this) {
            TODAY -> "Hoy"
            LAST_7_DAYS -> "Últimos 7 días"
            LAST_30_DAYS -> "Últimos 30 días"
        }
    }
}
