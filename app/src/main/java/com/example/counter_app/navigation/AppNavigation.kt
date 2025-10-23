package com.example.counter_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.counter_app.data.CounterEvent
import com.example.counter_app.ui.EventLogScreen
import com.example.counter_app.ui.LoginScreen
import com.example.counter_app.ui.MainScreen
import com.example.counter_app.ui.RegistrationScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
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
            MainScreen(navController = navController)
        }
        composable(
            "event_log/{events}",
            arguments = listOf(navArgument("events") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventsJson = backStackEntry.arguments?.getString("events")
            val type = object : TypeToken<List<CounterEvent>>() {}.type
            val events: List<CounterEvent> = if (eventsJson != null) {
                Gson().fromJson(eventsJson, type) ?: emptyList()
            } else {
                emptyList()
            }
            EventLogScreen(events = events)
        }
    }
}
