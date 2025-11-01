package com.example.counter_app.domain

/**
 * Modelos simples para datos de gráficos.
 *
 * Enfoque minimalista para evitar complejidad innecesaria.
 */

/**
 * Punto simple para el gráfico.
 * Representa un valor en un momento específico del tiempo.
 */
data class ChartPoint(
    val timestamp: Long,      // Timestamp en milisegundos
    val value: Float,         // Valor Y del punto
    val label: String = ""    // Etiqueta opcional (ej: "10:00")
)

/**
 * Datos para el gráfico de aforo (ocupación) en el tiempo.
 */
data class OccupancyChartData(
    val occupancyPoints: List<ChartPoint> = emptyList(),  // Puntos de aforo en el tiempo
    val maxValue: Float = 0f,                             // Valor máximo para escala Y
    val minValue: Float = 0f                              // Valor mínimo (siempre 0)
) {
    companion object {
        val EMPTY = OccupancyChartData()
    }

    /**
     * Indica si hay datos para mostrar.
     */
    val hasData: Boolean
        get() = occupancyPoints.isNotEmpty()
}

/**
 * Estadísticas simples calculadas de los datos.
 */
data class ReportStats(
    val totalEntries: Int = 0,
    val totalExits: Int = 0,
    val currentOccupancy: Int = 0,
    val peakOccupancy: Int = 0,
    val avgDwellTimeMinutes: Int = 0  // Tiempo promedio de visita en minutos
) {
    companion object {
        val EMPTY = ReportStats()
    }
}
