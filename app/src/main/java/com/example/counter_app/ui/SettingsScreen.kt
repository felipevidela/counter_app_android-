package com.example.counter_app.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter_app.viewmodel.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val alertSettings by viewModel.alertSettings.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    // Permission state para Android 13+
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    // Observar cuando se conceda el permiso y activar notificaciones
    LaunchedEffect(notificationPermissionState?.status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationPermissionState?.status?.isGranted == true && !settings.notificationsEnabled) {
                viewModel.toggleNotifications(true)
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Borrar todos los datos") },
            text = { Text("¿Estás seguro de que deseas eliminar todas las lecturas de sensores? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData {
                            showClearDataDialog = false
                        }
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para explicar por qué se necesita el permiso
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Permiso de notificaciones necesario") },
            text = { Text("Para recibir alertas de desconexión de dispositivos, necesitamos tu permiso para enviar notificaciones. Esto te ayudará a estar informado de problemas con los sensores en tiempo real.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        notificationPermissionState?.launchPermissionRequest()
                    }
                ) {
                    Text("Conceder permiso")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        viewModel.toggleNotifications(false)
                    }
                ) {
                    Text("No permitir")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Recibir alertas de dispositivos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // Usuario quiere activar notificaciones
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        // Android 13+: verificar/solicitar permiso
                                        val permissionState = notificationPermissionState
                                        if (permissionState != null) {
                                            when {
                                                permissionState.status.isGranted -> {
                                                    // Permiso ya concedido, activar notificaciones
                                                    viewModel.toggleNotifications(true)
                                                }
                                                permissionState.status.shouldShowRationale -> {
                                                    // Mostrar explicación y luego pedir permiso
                                                    showPermissionRationaleDialog = true
                                                }
                                                else -> {
                                                    // Primera vez, solicitar directamente
                                                    permissionState.launchPermissionRequest()
                                                }
                                            }
                                        }
                                    } else {
                                        // Android < 13: no se necesita permiso runtime
                                        viewModel.toggleNotifications(true)
                                    }
                                } else {
                                    // Usuario quiere desactivar notificaciones
                                    viewModel.toggleNotifications(false)
                                }
                            }
                        )
                    }
                }
            }
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Intervalo de simulación", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Cada ${settings.simulationIntervalSeconds} segundos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.simulationIntervalSeconds.toFloat(),
                        onValueChange = { viewModel.updateSimulationInterval(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                }
            }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Alertas Configurables",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                // Alerta de Desconexión
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerta de Desconexión", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Notificar cuando un dispositivo pierda conexión",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertSettings.disconnectionAlertEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateDisconnectionAlert(enabled)
                            }
                        )
                    }
                }
            }
            }

            item {
                // Alerta de Aforo Bajo
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerta de Aforo Bajo", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Notificar cuando la ocupación sea baja",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertSettings.lowOccupancyEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateLowOccupancyAlert(enabled, alertSettings.lowOccupancyThreshold)
                            }
                        )
                    }
                    if (alertSettings.lowOccupancyEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Umbral: ${alertSettings.lowOccupancyThreshold}% de capacidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = alertSettings.lowOccupancyThreshold.toFloat(),
                            onValueChange = { value ->
                                viewModel.updateLowOccupancyAlert(true, value.toInt())
                            },
                            valueRange = 5f..30f,
                            steps = 24
                        )
                    }
                }
            }
            }

            item {
                // Alerta de Aforo Alto
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerta de Aforo Alto", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Notificar cuando se acerque a capacidad máxima",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertSettings.highOccupancyEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateHighOccupancyAlert(enabled, alertSettings.highOccupancyThreshold)
                            }
                        )
                    }
                    if (alertSettings.highOccupancyEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Umbral: ${alertSettings.highOccupancyThreshold}% de capacidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = alertSettings.highOccupancyThreshold.toFloat(),
                            onValueChange = { value ->
                                viewModel.updateHighOccupancyAlert(true, value.toInt())
                            },
                            valueRange = 70f..100f,
                            steps = 29
                        )
                    }
                }
            }
            }

            item {
                // Alerta de Peak de Tráfico
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alerta de Peak de Tráfico", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Notificar cuando haya muchas entradas en poco tiempo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertSettings.trafficPeakEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateTrafficPeakAlert(enabled, alertSettings.trafficPeakThreshold)
                            }
                        )
                    }
                    if (alertSettings.trafficPeakEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Umbral: ${alertSettings.trafficPeakThreshold} entradas en 5 minutos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = alertSettings.trafficPeakThreshold.toFloat(),
                            onValueChange = { value ->
                                viewModel.updateTrafficPeakAlert(true, value.toInt())
                            },
                            valueRange = 5f..20f,
                            steps = 14
                        )
                    }
                }
            }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Datos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                FilledTonalButton(
                    onClick = { showClearDataDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar todas las lecturas")
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Divider()
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Información",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Counter App", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Versión 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aplicación de monitoreo IoT para dispositivos de conteo simulados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}
