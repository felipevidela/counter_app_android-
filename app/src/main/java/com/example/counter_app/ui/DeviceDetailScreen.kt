package com.example.counter_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter_app.data.Device
import com.example.counter_app.data.SensorEvent
import com.example.counter_app.data.EventType
import com.example.counter_app.ui.components.BreadcrumbNavigation
import com.example.counter_app.viewmodel.DeviceDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DeviceDetailViewModel = viewModel()
) {
    LaunchedEffect(deviceId) {
        viewModel.setDeviceId(deviceId)
    }

    val device by viewModel.device.collectAsState(initial = null)
    val latestEvent by viewModel.latestEvent.collectAsState(initial = null)
    val recentEvents by viewModel.recentEvents.collectAsState(initial = emptyList())
    val totalEntered by viewModel.totalEntered.collectAsState(initial = 0)
    val totalLeft by viewModel.totalLeft.collectAsState(initial = 0)
    val currentOccupancy by viewModel.currentOccupancy.collectAsState(initial = 0)
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Borrar historial") },
            text = { Text("¿Estás seguro de que deseas eliminar todo el historial de eventos de este dispositivo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearEvents()
                        showClearDialog = false
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (device == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BreadcrumbNavigation(
                        items = listOf("Dispositivos", device!!.name),
                        onItemClick = { index ->
                            if (index == 0) onNavigateBack()
                        }
                    )
                }

                item {
                    DeviceInfoCard(
                        device = device!!,
                        totalEntered = totalEntered,
                        totalLeft = totalLeft,
                        currentOccupancy = currentOccupancy,
                        onToggleStatus = viewModel::toggleDeviceStatus
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Historial de Eventos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (recentEvents.isNotEmpty()) {
                            IconButton(onClick = { showClearDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Borrar historial",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                if (recentEvents.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay eventos registrados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(recentEvents, key = { it.id }) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(
    device: Device,
    totalEntered: Int,
    totalLeft: Int,
    currentOccupancy: Int,
    onToggleStatus: (Boolean) -> Unit
) {
    val occupancyPercentage = (currentOccupancy.toFloat() / device.capacity.toFloat()).coerceIn(0f, 1f)
    val isOverCapacity = currentOccupancy > device.capacity
    val isNearCapacity = occupancyPercentage >= 0.9f

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Sensors,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.type,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (device.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = device.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Sensor status section
            Text(
                text = "Estado de Sensores Ultrasónicos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Entry sensor
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (device.isActive)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (device.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sensor Entrada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            totalEntered.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (device.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "detecciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Exit sensor
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (device.isActive)
                            Color(0xFFF44336).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (device.isActive) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sensor Salida",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            totalLeft.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (device.isActive) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "detecciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Occupancy alert
            if (isNearCapacity || isOverCapacity) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOverCapacity)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            Color(0xFFFFEB3B).copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isOverCapacity)
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isOverCapacity)
                                "¡Capacidad excedida! $currentOccupancy de ${device.capacity}"
                            else
                                "Cerca del límite de capacidad",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Occupancy bar
            Text(
                "Aforo Actual",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$currentOccupancy personas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "de ${device.capacity}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { occupancyPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when {
                    isOverCapacity -> MaterialTheme.colorScheme.error
                    isNearCapacity -> Color(0xFFFFA726)
                    else -> MaterialTheme.colorScheme.primary
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Technical info
            Text(
                text = "Información Técnica",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("MAC Address", device.macAddress)
            InfoRow("Capacidad Máxima", "${device.capacity} personas")
            InfoRow("Estado Simulación", if (device.isActive) "Activa" else "Detenida")

            Spacer(modifier = Modifier.height(16.dp))

            // Control switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Controlar Simulación",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = device.isActive,
                    onCheckedChange = onToggleStatus
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatCard(label: String, value: String, backgroundColor: androidx.compose.ui.graphics.Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EventCard(event: SensorEvent) {
    var expanded by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = timeFormat.format(Date(event.timestamp))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(event.timestamp))
    val fullDateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val fullDateString = fullDateFormat.format(Date(event.timestamp))

    val isEntry = event.eventType == EventType.ENTRY
    val isDisconnection = event.eventType == EventType.DISCONNECTION

    val eventColor = when (event.eventType) {
        EventType.ENTRY -> Color(0xFF4CAF50)  // Verde para entrada
        EventType.EXIT -> Color(0xFFF44336)   // Rojo para salida
        EventType.DISCONNECTION -> Color(0xFFF57C00)  // Naranja para desconexión
    }

    val eventIcon = when (event.eventType) {
        EventType.ENTRY -> Icons.Default.Login
        EventType.EXIT -> Icons.Default.Logout
        EventType.DISCONNECTION -> Icons.Default.Warning
    }

    val eventText = when (event.eventType) {
        EventType.ENTRY -> "Entrada"
        EventType.EXIT -> "Salida"
        EventType.DISCONNECTION -> "Desconexión detectada"
    }

    val peopleText = if (event.peopleCount == 1) "persona" else "personas"

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = eventIcon,
                        contentDescription = eventText,
                        tint = eventColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = if (isDisconnection) {
                                eventText
                            } else {
                                "${event.peopleCount} $peopleText"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDisconnection) {
                                timeString
                            } else {
                                "$eventText • $timeString"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Mostrar menos" else "Mostrar más",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Divider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Información Detallada",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    DetailRow(label = "Fecha", value = fullDateString)
                    DetailRow(label = "Hora exacta", value = timeString)

                    Divider()

                    DetailRow(label = "Tipo de evento", value = eventText)

                    if (!isDisconnection) {
                        DetailRow(
                            label = "Personas detectadas",
                            value = "${event.peopleCount} $peopleText"
                        )
                    } else {
                        DetailRow(
                            label = "Estado",
                            value = "Conexión perdida con el dispositivo"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "ID de evento: #${event.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
