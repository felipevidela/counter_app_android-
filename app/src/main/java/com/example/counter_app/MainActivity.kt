package com.example.counter_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.counter_app.data.ThemePreferences
import com.example.counter_app.navigation.AppNavigation
import com.example.counter_app.ui.theme.Counter_APPTheme

class MainActivity : ComponentActivity() {
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreferences = ThemePreferences(applicationContext)

        setContent {
            // Observar preferencia de dark mode
            val isDarkMode by themePreferences.darkModeFlow.collectAsState(initial = false)

            Counter_APPTheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}