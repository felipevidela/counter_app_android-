package com.example.counter_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter_app.domain.OccupancyChartData
import com.example.counter_app.domain.ReportStats
import com.example.counter_app.ui.components.AppTopBar
import com.example.counter_app.ui.components.OccupancyChart
import com.example.counter_app.viewmodel.ReportsViewModel

/**
 * Pantalla de Reportes y Gráficos - REESCRITA DESDE CERO.
 *
 * Diseño simple y limpio:
 * 1. Filtros (dispositivo + rango)
 * 2. Gráfico de AFORO en tiempo real (YCharts)
 * 3. Estadísticas (entradas, salidas, aforo actual y máximo)
 *
 * Todo reactivo con actualización en tiempo real automática.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel()
) {
    // Observar estados reactivos
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val chartData by viewModel.chartData.collectAsState(initial = OccupancyChartData.EMPTY)
    val stats by viewModel.stats.collectAsState(initial = ReportStats.EMPTY)

    // Estados locales para UI
    var selectedDeviceId by remember { mutableStateOf<Long?>(null) }
    var selectedRange by remember { mutableStateOf(ReportsViewModel.DateRange.TODAY) }
    var expandedDevice by remember { mutableStateOf(false) }
    var expandedRange by remember { mutableStateOf(false) }

    // Auto-seleccionar primer dispositivo
    LaunchedEffect(devices) {
        if (selectedDeviceId == null && devices.isNotEmpty()) {
            val firstDeviceId = devices.first().id
            selectedDeviceId = firstDeviceId
            viewModel.setSelectedDevice(firstDeviceId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Reportes y Gráficos")
        }
    ) { padding ->
        if (devices.isEmpty()) {
            // Estado sin dispositivos
            EmptyDevicesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección de Filtros
                FiltersSection(
                    devices = devices,
                    selectedDeviceId = selectedDeviceId,
                    selectedRange = selectedRange,
                    expandedDevice = expandedDevice,
                    expandedRange = expandedRange,
                    onDeviceSelected = { deviceId ->
                        selectedDeviceId = deviceId
                        viewModel.setSelectedDevice(deviceId)
                        expandedDevice = false
                    },
                    onRangeSelected = { range ->
                        selectedRange = range
                        viewModel.setDateRange(range)
                        expandedRange = false
                    },
                    onDeviceExpandedChange = { expandedDevice = it },
                    onRangeExpandedChange = { expandedRange = it }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Sección de Gráfico
                ChartSection(chartData = chartData)

                // Sección de Estadísticas
                StatsSection(stats = stats)
            }
        }
    }
}

@Composable
private fun EmptyDevicesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "No hay dispositivos registrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Crea dispositivos en el Dashboard para ver reportes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection(
    devices: List<com.example.counter_app.data.Device>,
    selectedDeviceId: Long?,
    selectedRange: ReportsViewModel.DateRange,
    expandedDevice: Boolean,
    expandedRange: Boolean,
    onDeviceSelected: (Long) -> Unit,
    onRangeSelected: (ReportsViewModel.DateRange) -> Unit,
    onDeviceExpandedChange: (Boolean) -> Unit,
    onRangeExpandedChange: (Boolean) -> Unit
) {
    Text(
        "Filtros",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    // Selector de Dispositivo
    ExposedDropdownMenuBox(
        expanded = expandedDevice,
        onExpandedChange = onDeviceExpandedChange
    ) {
        @Suppress("DEPRECATION")
        OutlinedTextField(
            value = devices.find { it.id == selectedDeviceId }?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Dispositivo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDevice) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandedDevice,
            onDismissRequest = { onDeviceExpandedChange(false) }
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(device.name) },
                    onClick = { onDeviceSelected(device.id) }
                )
            }
        }
    }

    // Selector de Rango
    ExposedDropdownMenuBox(
        expanded = expandedRange,
        onExpandedChange = onRangeExpandedChange
    ) {
        @Suppress("DEPRECATION")
        OutlinedTextField(
            value = selectedRange.getDisplayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Rango de fechas") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRange) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandedRange,
            onDismissRequest = { onRangeExpandedChange(false) }
        ) {
            ReportsViewModel.DateRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.getDisplayName()) },
                    onClick = { onRangeSelected(range) }
                )
            }
        }
    }
}

@Composable
private fun ChartSection(chartData: OccupancyChartData) {
    Text(
        "Aforo en Tiempo Real",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Text(
        "Ocupación actual del establecimiento • Actualización automática",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OccupancyChart(
                data = chartData,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatsSection(stats: ReportStats) {
    Text(
        "Estadísticas",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Primera fila: Total Entradas y Total Salidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Total Entradas", stats.totalEntries.toString())
                StatItem("Total Salidas", stats.totalExits.toString())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segunda fila: Aforo Actual y Aforo Máximo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Aforo Actual", stats.currentOccupancy.toString())
                StatItem("Aforo Máximo", stats.peakOccupancy.toString())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tercera fila: Tiempo Promedio de Visita
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatItem(
                    "Tiempo Prom. Visita",
                    "${stats.avgDwellTimeMinutes} min"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
