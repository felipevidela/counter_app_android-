package com.example.counter_app.calculations

import com.example.counter_app.domain.ChartPoint
import com.example.counter_app.domain.OccupancyChartData
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para validar el cálculo de tiempo promedio de visita.
 *
 * Simula la lógica de ReportsViewModel.calculateStatsFromEvents()
 * para verificar que el cálculo del área bajo la curva sea correcto.
 */
class AvgDwellTimeCalculationTest {

    /**
     * Simula el cálculo de tiempo promedio del ViewModel
     */
    private fun calculateAvgDwellMinutes(
        occupancyData: OccupancyChartData,
        totalExits: Int
    ): Int {
        if (occupancyData.occupancyPoints.size > 1 && totalExits > 0) {
            val points = occupancyData.occupancyPoints
            var totalPersonMinutes = 0.0

            // Calcular área bajo la curva (personas-minuto acumulados)
            for (i in 0 until points.size - 1) {
                val currentOccupancy = points[i].value
                val nextTimestamp = points[i + 1].timestamp
                val currentTimestamp = points[i].timestamp
                val durationMinutes = (nextTimestamp - currentTimestamp) / (1000.0 * 60.0)

                // Área del trapecio: aforo actual × duración
                totalPersonMinutes += currentOccupancy * durationMinutes
            }

            // Promedio: personas-minuto totales / número de salidas
            return (totalPersonMinutes / totalExits).toInt()
        }
        return 0
    }

    @Test
    fun `test caso simple - 1 persona por 30 minutos`() {
        // Arrange: 1 persona entra y sale después de 30 minutos
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime, 0f),                    // 0 min: 0 personas
            ChartPoint(baseTime + 0 * 60000, 1f),        // 0 min: 1 persona
            ChartPoint(baseTime + 30 * 60000, 1f),       // 30 min: 1 persona
            ChartPoint(baseTime + 30 * 60000, 0f)        // 30 min: 0 personas
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 1)

        // Assert: Debería ser ~30 minutos
        assertTrue("Esperado ~30 min, obtenido: $result", result in 29..31)
        println("✓ TEST 1 PASADO: $result minutos (esperado: 30)")
    }

    @Test
    fun `test múltiples personas con diferentes tiempos`() {
        // Arrange: 3 personas con tiempos de visita diferentes
        // P1: 0-60 min (60 min), P2: 10-40 min (30 min), P3: 20-50 min (30 min)
        // Promedio esperado: (60+30+30)/3 = 40 minutos
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime + 0 * 60000, 0f),
            ChartPoint(baseTime + 0 * 60000, 1f),        // P1 entra
            ChartPoint(baseTime + 10 * 60000, 2f),       // P2 entra
            ChartPoint(baseTime + 20 * 60000, 3f),       // P3 entra
            ChartPoint(baseTime + 40 * 60000, 3f),
            ChartPoint(baseTime + 40 * 60000, 2f),       // P2 sale
            ChartPoint(baseTime + 50 * 60000, 2f),
            ChartPoint(baseTime + 50 * 60000, 1f),       // P3 sale
            ChartPoint(baseTime + 60 * 60000, 1f),
            ChartPoint(baseTime + 60 * 60000, 0f)        // P1 sale
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 3)

        // Assert
        // Cálculo: 1×10 + 2×10 + 3×20 + 2×10 + 1×10 = 120 personas-min
        // Promedio: 120 / 3 = 40 minutos
        assertTrue("Esperado ~40 min, obtenido: $result", result in 38..42)
        println("✓ TEST 2 PASADO: $result minutos (esperado: 40)")
    }

    @Test
    fun `test caso realista de tienda con aforo variable`() {
        // Arrange: Tienda con 10 personas en 2 horas
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime + 0 * 60000, 0f),
            ChartPoint(baseTime + 5 * 60000, 2f),
            ChartPoint(baseTime + 15 * 60000, 5f),
            ChartPoint(baseTime + 30 * 60000, 8f),       // Pico: 8 personas
            ChartPoint(baseTime + 45 * 60000, 6f),
            ChartPoint(baseTime + 60 * 60000, 4f),
            ChartPoint(baseTime + 75 * 60000, 3f),
            ChartPoint(baseTime + 90 * 60000, 1f),
            ChartPoint(baseTime + 120 * 60000, 0f)
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 10)

        // Assert
        // Área: 2×10 + 5×15 + 8×15 + 6×15 + 4×15 + 3×15 + 1×30 = 485 personas-min
        // Promedio: 485 / 10 ≈ 48 minutos
        assertTrue("Esperado ~48 min, obtenido: $result", result in 45..50)
        println("✓ TEST 3 PASADO: $result minutos (esperado: ~48)")
    }

    @Test
    fun `test sin salidas debe retornar 0`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime, 5f),
            ChartPoint(baseTime + 60000, 5f)
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 0)

        // Assert
        assertEquals("Sin salidas debería retornar 0", 0, result)
        println("✓ TEST 4 PASADO: Sin salidas = 0 minutos")
    }

    @Test
    fun `test sin datos debe retornar 0`() {
        // Arrange
        val data = OccupancyChartData(occupancyPoints = emptyList())

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 5)

        // Assert
        assertEquals("Sin datos debería retornar 0", 0, result)
        println("✓ TEST 5 PASADO: Sin datos = 0 minutos")
    }

    @Test
    fun `test solo un punto debe retornar 0`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val points = listOf(ChartPoint(baseTime, 5f))
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 5)

        // Assert
        assertEquals("Un solo punto debería retornar 0", 0, result)
        println("✓ TEST 6 PASADO: Un punto = 0 minutos")
    }

    @Test
    fun `test visitas muy cortas - menos de 5 minutos`() {
        // Arrange: Personas que entran y salen rápidamente
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime + 0 * 60000, 0f),
            ChartPoint(baseTime + 0 * 60000, 3f),        // 3 personas entran
            ChartPoint(baseTime + 3 * 60000, 3f),
            ChartPoint(baseTime + 3 * 60000, 0f)         // Todas salen a los 3 min
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 3)

        // Assert: 3 personas × 3 minutos = 9 personas-min / 3 salidas = 3 min
        assertTrue("Esperado ~3 min, obtenido: $result", result in 2..4)
        println("✓ TEST 7 PASADO: $result minutos (esperado: 3)")
    }

    @Test
    fun `test visitas muy largas - más de 2 horas`() {
        // Arrange: Personas que se quedan mucho tiempo (ej: coworking, restaurante)
        val baseTime = System.currentTimeMillis()
        val points = listOf(
            ChartPoint(baseTime + 0 * 60000, 0f),
            ChartPoint(baseTime + 0 * 60000, 4f),        // 4 personas
            ChartPoint(baseTime + 150 * 60000, 4f),      // 2.5 horas después
            ChartPoint(baseTime + 150 * 60000, 0f)       // Todas salen
        )
        val data = OccupancyChartData(occupancyPoints = points)

        // Act
        val result = calculateAvgDwellMinutes(data, totalExits = 4)

        // Assert: 4 personas × 150 min = 600 personas-min / 4 = 150 min
        assertTrue("Esperado ~150 min, obtenido: $result", result in 148..152)
        println("✓ TEST 8 PASADO: $result minutos (esperado: 150)")
    }
}
