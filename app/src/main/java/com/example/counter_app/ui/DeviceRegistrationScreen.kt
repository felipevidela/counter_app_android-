package com.example.counter_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter_app.viewmodel.DeviceRegistrationViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegistrationScreen(
    deviceId: Long = 0L, // 0L = crear nuevo, >0 = editar existente
    onNavigateBack: () -> Unit,
    onDeviceCreated: (Long) -> Unit,
    viewModel: DeviceRegistrationViewModel = viewModel()
) {
    val isEditMode = deviceId > 0L
    val editingDevice by viewModel.editingDevice.collectAsState()

    var deviceName by remember { mutableStateOf("") }
    var deviceType by remember { mutableStateOf("Arduino Uno") }
    var deviceLocation by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("100") }
    var error by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val deviceTypes = listOf("Arduino Uno", "Arduino Nano", "ESP32", "NodeMCU", "Arduino Mega")

    // Cargar dispositivo si estamos en modo edición
    LaunchedEffect(deviceId) {
        if (isEditMode) {
            viewModel.loadDevice(deviceId)
        } else {
            viewModel.clearEditingDevice()
        }
    }

    // Pre-rellenar campos cuando se carga el dispositivo
    LaunchedEffect(editingDevice) {
        editingDevice?.let { device ->
            deviceName = device.name
            deviceType = device.type
            deviceLocation = device.location
            capacity = device.capacity.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Dispositivo" else "Nuevo Dispositivo") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información del Dispositivo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = deviceName,
                onValueChange = {
                    deviceName = it
                    error = ""
                },
                label = { Text("Nombre del dispositivo") },
                placeholder = { Text("Ej: Sensor Entrada Principal") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = deviceLocation,
                onValueChange = {
                    deviceLocation = it
                    error = ""
                },
                label = { Text("Ubicación en tienda (opcional)") },
                placeholder = { Text("Ej: Entrada Principal, Probadores, Caja") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = deviceType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de dispositivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    deviceTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                deviceType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = capacity,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        capacity = it
                        error = ""
                    }
                },
                label = { Text("Capacidad máxima") },
                placeholder = { Text("100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Información",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Este dispositivo Arduino simulado incluirá:\n" +
                                "• 2 sensores ultrasónicos (Entrada y Salida)\n" +
                                "• Dirección MAC única generada automáticamente\n" +
                                "• Generación aleatoria de datos simulando flujo de personas en mall\n" +
                                "• Capacidad configurable para alertas de aforo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val capacityInt = capacity.toIntOrNull() ?: 0
                    if (isEditMode && editingDevice != null) {
                        // Modo edición
                        viewModel.updateDevice(
                            deviceId = editingDevice!!.id,
                            name = deviceName,
                            type = deviceType,
                            location = deviceLocation,
                            capacity = capacityInt,
                            macAddress = editingDevice!!.macAddress,
                            isActive = editingDevice!!.isActive,
                            createdAt = editingDevice!!.createdAt,
                            onSuccess = {
                                onNavigateBack()
                            },
                            onError = { errorMsg ->
                                error = errorMsg
                            }
                        )
                    } else {
                        // Modo creación
                        viewModel.createDevice(
                            name = deviceName,
                            type = deviceType,
                            location = deviceLocation,
                            capacity = capacityInt,
                            onSuccess = { newDeviceId ->
                                onDeviceCreated(newDeviceId)
                            },
                            onError = { errorMsg ->
                                error = errorMsg
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = deviceName.isNotBlank() && capacity.isNotBlank()
            ) {
                Text(if (isEditMode) "Actualizar Dispositivo" else "Crear Dispositivo Arduino")
            }
        }
    }
}
