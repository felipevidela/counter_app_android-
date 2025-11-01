package com.example.counter_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.counter_app.domain.OccupancyChartData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gráfico de aforo (ocupación) usando YCharts.
 *
 * Muestra una sola línea que representa cuántas personas
 * hay dentro del establecimiento en cada momento.
 *
 * Simple, claro y directo al objetivo.
 */
@Composable
fun OccupancyChart(
    data: OccupancyChartData,
    modifier: Modifier = Modifier
) {
    // Color para la línea de aforo (azul/verde profesional)
    val occupancyColor = Color(0xFF2196F3)  // Azul Material

    if (!data.hasData) {
        // Estado vacío
        EmptyChartState(modifier)
        return
    }

    Column(modifier = modifier) {
        // El data.maxValue ya incluye 20% de padding del ViewModel
        // Usamos ese valor para el rango del gráfico
        val chartMaxY = data.maxValue.toFloat().coerceAtLeast(5f)
        val yAxisSteps = 5

        // Construir datos para YCharts
        // Normalizar valores Y al rango 0-yAxisSteps para que coincidan con el eje Y
        val occupancyLine = Line(
            dataPoints = data.occupancyPoints.mapIndexed { index, point ->
                val normalizedY = if (chartMaxY > 0) (point.value / chartMaxY) * yAxisSteps else 0f
                Point(x = index.toFloat(), y = normalizedY)
            },
            lineStyle = LineStyle(color = occupancyColor),
            intersectionPoint = IntersectionPoint(
                color = occupancyColor,
                radius = 3.dp
            ),
            selectionHighlightPoint = SelectionHighlightPoint(
                color = occupancyColor
            ),
            shadowUnderLine = ShadowUnderLine(
                alpha = 0.2f,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        occupancyColor.copy(alpha = 0.4f),
                        occupancyColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            ),
            selectionHighlightPopUp = SelectionHighlightPopUp(
                popUpLabel = { x, y ->
                    // Tooltip personalizado: "HH:mm\nAforo: X personas"
                    val pointIndex = x.toInt()
                    val point = data.occupancyPoints.getOrNull(pointIndex)
                    val aforo = point?.value?.toInt() ?: 0  // Usar valor original, no normalizado
                    val hora = point?.let { formatTimestamp(it.timestamp) } ?: ""
                    "$hora\nAforo: $aforo personas"
                }
            )
        )

        // Determinar el número de pasos para el eje X
        val steps = maxOf(0, data.occupancyPoints.size - 1)

        // Eje X (tiempo)
        val xAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .backgroundColor(Color.Transparent)
            .steps(steps)
            .labelData { index ->
                // Mostrar etiquetas cada N puntos para evitar sobrecarga
                val pointIndex = index.toInt()
                if (pointIndex % maxOf(1, steps / 5) == 0) {
                    if (pointIndex < data.occupancyPoints.size) {
                        formatTimestamp(data.occupancyPoints[pointIndex].timestamp)
                    } else ""
                } else ""
            }
            .labelAndAxisLinePadding(15.dp)
            .build()

        // Eje Y (aforo - número de personas) - dinámico según datos
        val yAxisData = AxisData.Builder()
            .steps(yAxisSteps)
            .backgroundColor(Color.Transparent)
            .labelAndAxisLinePadding(20.dp)
            .labelData { value ->
                // Escalar etiquetas: value va de 0-yAxisSteps, mapear a 0-chartMaxY
                val scaledValue = (value * chartMaxY / yAxisSteps).toInt()
                "$scaledValue"
            }
            .build()

        // Configuración del gráfico
        val lineChartData = LineChartData(
            linePlotData = LinePlotData(
                lines = listOf(occupancyLine)
            ),
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
            backgroundColor = MaterialTheme.colorScheme.surface
        )

        // Renderizar el gráfico
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            lineChartData = lineChartData
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda simple
        OccupancyLegend(occupancyColor = occupancyColor)
    }
}

@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sin datos de aforo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Selecciona un dispositivo y rango de tiempo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Los datos se actualizarán automáticamente",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OccupancyLegend(occupancyColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.size(12.dp),
            shape = MaterialTheme.shapes.small,
            color = occupancyColor
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Aforo (personas dentro)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
