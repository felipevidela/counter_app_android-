package com.example.counter_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.counter_app.bluetooth.BluetoothService
import com.example.counter_app.bluetooth.LocalBluetoothService
import com.example.counter_app.bluetooth.SimulatedBluetoothService
import com.example.counter_app.bluetooth.USE_SIMULATED_SERVICE
import com.example.counter_app.ui.EventLogScreen
import com.example.counter_app.ui.LoginScreen
import com.example.counter_app.ui.MainScreen
import com.example.counter_app.ui.RegistrationScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("main") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegistrationScreen(onRegistrationSuccess = { navController.navigate("login") })
        }
        composable("main") {
            // Create BluetoothService once when entering main screen
            val bluetoothService = remember {
                if (USE_SIMULATED_SERVICE) {
                    SimulatedBluetoothService()
                } else {
                    BluetoothService(context)
                }
            }

            // Cleanup when leaving main screen completely (back to login)
            DisposableEffect(Unit) {
                onDispose {
                    bluetoothService.disconnect()
                }
            }

            // Provide BluetoothService to MainScreen and its nested navigation
            CompositionLocalProvider(LocalBluetoothService provides bluetoothService) {
                MainScreen()
            }
        }
    }
}
