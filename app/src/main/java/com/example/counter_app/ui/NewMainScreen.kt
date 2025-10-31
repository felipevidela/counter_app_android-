@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.counter_app.ui

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.counter_app.service.EventBasedSimulationService

sealed class AppScreen(val route: String, val name: String, val icon: @Composable () -> Unit) {
    object Dashboard : AppScreen("dashboard", "Dispositivos", { Icon(Icons.Default.Dashboard, contentDescription = null) })
    object Reports : AppScreen("reports", "Reportes", { Icon(Icons.Default.BarChart, contentDescription = null) })
    object Settings : AppScreen("settings", "Ajustes", { Icon(Icons.Default.Settings, contentDescription = null) })
}

@Composable
fun NewMainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize event-based simulation service
    val simulationService = remember {
        EventBasedSimulationService(context.applicationContext as Application)
    }

    // Start simulation when entering main screen
    DisposableEffect(Unit) {
        simulationService.startSimulation(eventIntervalMillis = 2000..8000)
        onDispose {
            simulationService.stopSimulation()
            simulationService.cleanup()
        }
    }

    val items = listOf(
        AppScreen.Dashboard,
        AppScreen.Reports,
        AppScreen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Routes where bottom bar should be hidden
    val routesWithoutBottomBar = listOf("device_detail", "device_registration")
    val showBottomBar = currentRoute !in routesWithoutBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon() },
                            label = { Text(screen.name) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = AppScreen.Dashboard.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                DashboardScreen(
                    onDeviceClick = { deviceId ->
                        navController.navigate("device_detail/$deviceId")
                    },
                    onAddDeviceClick = {
                        navController.navigate("device_registration")
                    }
                )
            }

            composable(
                route = AppScreen.Reports.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                ReportsScreen()
            }

            composable(
                route = AppScreen.Settings.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                SettingsScreen(onLogout = onLogout)
            }

            composable(
                route = "device_detail/{deviceId}",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId")?.toLongOrNull() ?: 0L
                DeviceDetailScreen(
                    deviceId = deviceId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "device_registration",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                DeviceRegistrationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onDeviceCreated = { deviceId ->
                        navController.navigate("device_detail/$deviceId") {
                            popUpTo("device_registration") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
