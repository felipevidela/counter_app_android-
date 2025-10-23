package com.example.counter_app.ui

import android.Manifest
import android.bluetooth.le.ScanResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.counter_app.bluetooth.LocalBluetoothService
import com.example.counter_app.bluetooth.USE_SIMULATED_SERVICE
import java.util.UUID

import androidx.navigation.NavController
import com.example.counter_app.data.CounterData

@Composable
fun MonitoringScreen(navController: NavController) {
    // Get BluetoothService from CompositionLocal
    val bluetoothService = LocalBluetoothService.current

    var scannedDevices by remember { mutableStateOf(emptyList<ScanResult>()) }
    var isScanning by remember { mutableStateOf(false) }

    // Observe state from the service flows - these persist across navigation
    val counterData by bluetoothService.getCounterDataFlow().collectAsState(initial = CounterData(0, 0, 0))
    val connectionStatus by bluetoothService.getConnectionStateFlow().collectAsState(initial = "Desconectado")

    var services by remember { mutableStateOf(emptyList<UUID>()) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            isScanning = true
            bluetoothService.startBleScan()
        }
    }

    bluetoothService.onDeviceScan = {
        scannedDevices = it
    }

    bluetoothService.onServicesDiscovered = {
        services = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Contador de Personas", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Entradas", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${counterData.entered}", style = MaterialTheme.typography.displayMedium)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Salidas", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${counterData.left}", style = MaterialTheme.typography.displayMedium)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Aforo", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${counterData.capacity}", style = MaterialTheme.typography.displayMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Estado: $connectionStatus", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(
                    onClick = {
                        if (isScanning) {
                            isScanning = false
                            bluetoothService.stopBleScan()
                        } else {
                            scannedDevices = emptyList()
                            if (USE_SIMULATED_SERVICE) {
                                isScanning = true
                                bluetoothService.startBleScan()
                            } else {
                                requestPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isScanning) "Detener" else "Buscar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = {
                        navController.navigate("event_log")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registro")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = {
                    bluetoothService.resetAndDisconnect()
                    scannedDevices = emptyList()
                },
                enabled = connectionStatus == "Conectado",
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Desconectar y Reiniciar")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Dispositivos Encontrados", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                if (USE_SIMULATED_SERVICE && isScanning) {
                    Text("Dispositivo: Contador Simulado - 00:11:22:33:44:55",
                        modifier = Modifier.clickable { 
                            bluetoothService.connectToDevice("00:11:22:33:44:55")
                        }.padding(16.dp).fillMaxWidth()
                    )
                } else {
                    LazyColumn {
                        items(scannedDevices) { result ->
                            Text("Dispositivo: ${result.device.name ?: "Desconocido"} - ${result.device.address}",
                                modifier = Modifier.clickable { 
                                    bluetoothService.connectToDevice(result.device.address)
                                }.padding(16.dp).fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}




