package com.example.counter_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.counter_app.bluetooth.LocalBluetoothService
import com.example.counter_app.bluetooth.COUNTER_SERVICE_UUID
import com.example.counter_app.bluetooth.ACTIVATOR_CHARACTERISTIC_UUID

@Composable
fun ActivatorScreen() {
    // Get BluetoothService from CompositionLocal
    val bluetoothService = LocalBluetoothService.current

    var switchState by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0f) }
    var sentData by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Activador", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Activar Característica", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = switchState,
                        onCheckedChange = { switchState = it }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Valor: ${sliderValue.toInt()}", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f
                )
                Spacer(modifier = Modifier.height(24.dp))

                FilledTonalButton(onClick = { 
                    val data = ByteArray(2)
                    data[0] = if (switchState) 1 else 0
                    data[1] = sliderValue.toInt().toByte()
                    bluetoothService.writeCharacteristic(COUNTER_SERVICE_UUID, ACTIVATOR_CHARACTERISTIC_UUID, data)
                    sentData = "Interruptor: ${if(switchState) "On" else "Off"}, Deslizador: ${sliderValue.toInt()}"
                }) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Datos")
                }

                if (sentData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Enviado: $sentData", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
