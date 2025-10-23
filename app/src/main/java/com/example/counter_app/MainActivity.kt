package com.example.counter_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.counter_app.navigation.AppNavigation
import com.example.counter_app.ui.theme.Counter_APPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Counter_APPTheme {
                AppNavigation()
            }
        }
    }
}