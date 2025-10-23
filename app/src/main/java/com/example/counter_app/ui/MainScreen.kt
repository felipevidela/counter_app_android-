@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.counter_app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val name: String, val icon: @Composable () -> Unit) {
    object Monitoring : Screen("monitoring", "Monitoreo", { Icon(Icons.Default.Home, contentDescription = null) })
    object Activator : Screen("activator", "Activador", { Icon(Icons.Default.Create, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    // Create a separate NavController for the bottom navigation tabs
    val tabNavController = rememberNavController()

    val items = listOf(
        Screen.Monitoring,
        Screen.Activator,
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Contador de Personas") }) },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon() },
                        label = { Text(screen.name) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            tabNavController.navigate(screen.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
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
    ) { innerPadding ->
        NavHost(tabNavController, startDestination = Screen.Monitoring.route, Modifier.padding(innerPadding)) {
            composable(Screen.Monitoring.route) { MonitoringScreen(navController = navController) }
            composable(Screen.Activator.route) { ActivatorScreen() }
        }
    }
}
