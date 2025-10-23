package com.example.counter_app.bluetooth

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for providing BluetoothService throughout the app
 * This allows the service to persist across navigation without passing it manually
 */
val LocalBluetoothService = staticCompositionLocalOf<BluetoothServiceInterface> {
    error("No BluetoothService provided")
}
