package com.example.counter_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter_app.viewmodel.ReportsViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel()
) {
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val chartData by viewModel.chartData.collectAsState(initial = emptyList())

    var selectedDeviceId by remember { mutableStateOf<Long?>(null) }
    var selectedRange by remember { mutableStateOf(ReportsViewModel.DateRange.TODAY) }
    var expandedDevice by remember { mutableStateOf(false) }
    var expandedRange by remember { mutableStateOf(false) }

    // Auto-select first device
    LaunchedEffect(devices) {
        if (selectedDeviceId == null && devices.isNotEmpty()) {
            selectedDeviceId = devices.first().id
            viewModel.setSelectedDevice(devices.first().id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes y Gráficos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No hay dispositivos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Crea dispositivos para ver sus reportes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDevice,
                    onExpandedChange = { expandedDevice = !expandedDevice }
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
                        onDismissRequest = { expandedDevice = false }
                    ) {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text(device.name) },
                                onClick = {
                                    selectedDeviceId = device.id
                                    viewModel.setSelectedDevice(device.id)
                                    expandedDevice = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedRange,
                    onExpandedChange = { expandedRange = !expandedRange }
                ) {
                    @Suppress("DEPRECATION")
                    OutlinedTextField(
                        value = when (selectedRange) {
                            ReportsViewModel.DateRange.TODAY -> "Hoy"
                            ReportsViewModel.DateRange.LAST_7_DAYS -> "Últimos 7 días"
                            ReportsViewModel.DateRange.LAST_30_DAYS -> "Últimos 30 días"
                        },
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
                        onDismissRequest = { expandedRange = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hoy") },
                            onClick = {
                                selectedRange = ReportsViewModel.DateRange.TODAY
                                viewModel.setDateRange(selectedRange)
                                expandedRange = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Últimos 7 días") },
                            onClick = {
                                selectedRange = ReportsViewModel.DateRange.LAST_7_DAYS
                                viewModel.setDateRange(selectedRange)
                                expandedRange = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Últimos 30 días") },
                            onClick = {
                                selectedRange = ReportsViewModel.DateRange.LAST_30_DAYS
                                viewModel.setDateRange(selectedRange)
                                expandedRange = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Entradas y Salidas en el Tiempo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (chartData.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay datos para mostrar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    ChartCard(chartData = chartData)
                }

                StatsCard(chartData = chartData)
            }
        }
    }
}

@Composable
fun ChartCard(chartData: List<com.example.counter_app.viewmodel.ChartData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (chartData.isNotEmpty()) {
                SimpleLineChart(
                    chartData = chartData,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF4CAF50)
            ) {}
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("Entradas", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF44336)
            ) {}
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("Salidas", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SimpleLineChart(
    chartData: List<com.example.counter_app.viewmodel.ChartData>,
    modifier: Modifier = Modifier
) {
    val enteredColor = Color(0xFF4CAF50)
    val leftColor = Color(0xFFF44336)

    Canvas(modifier = modifier) {
        if (chartData.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40f

        // Find max value for scaling
        val maxEntered = chartData.maxOf { it.entered }.toFloat()
        val maxLeft = chartData.maxOf { it.left }.toFloat()
        val maxValue = maxOf(maxEntered, maxLeft, 1f)

        // Calculate step sizes
        val xStep = (width - padding * 2) / (chartData.size - 1).coerceAtLeast(1)
        val yScale = (height - padding * 2) / maxValue

        // Draw entered line (green)
        val enteredPath = Path()
        chartData.forEachIndexed { index, data ->
            val x = padding + index * xStep
            val y = height - padding - (data.entered.toFloat() * yScale)

            if (index == 0) {
                enteredPath.moveTo(x, y)
            } else {
                enteredPath.lineTo(x, y)
            }
        }
        drawPath(
            path = enteredPath,
            color = enteredColor,
            style = Stroke(width = 3f)
        )

        // Draw left line (red)
        val leftPath = Path()
        chartData.forEachIndexed { index, data ->
            val x = padding + index * xStep
            val y = height - padding - (data.left.toFloat() * yScale)

            if (index == 0) {
                leftPath.moveTo(x, y)
            } else {
                leftPath.lineTo(x, y)
            }
        }
        drawPath(
            path = leftPath,
            color = leftColor,
            style = Stroke(width = 3f)
        )

        // Draw data points for entered
        chartData.forEachIndexed { index, data ->
            val x = padding + index * xStep
            val y = height - padding - (data.entered.toFloat() * yScale)
            drawCircle(
                color = enteredColor,
                radius = 5f,
                center = Offset(x, y)
            )
        }

        // Draw data points for left
        chartData.forEachIndexed { index, data ->
            val x = padding + index * xStep
            val y = height - padding - (data.left.toFloat() * yScale)
            drawCircle(
                color = leftColor,
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun StatsCard(chartData: List<com.example.counter_app.viewmodel.ChartData>) {
    val totalEntered = chartData.sumOf { it.entered }
    val totalLeft = chartData.sumOf { it.left }
    val avgEntered = if (chartData.isNotEmpty()) totalEntered / chartData.size else 0
    val avgLeft = if (chartData.isNotEmpty()) totalLeft / chartData.size else 0

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Estadísticas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Total Entradas", totalEntered.toString())
                StatItem("Total Salidas", totalLeft.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Promedio Entradas", avgEntered.toString())
                StatItem("Promedio Salidas", avgLeft.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Registros", chartData.size.toString())
                StatItem("Diferencia", "${totalEntered - totalLeft}")
            }
        }
    }
}
