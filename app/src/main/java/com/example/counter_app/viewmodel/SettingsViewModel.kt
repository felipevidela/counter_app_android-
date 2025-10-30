package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.AppDatabase
import com.example.counter_app.data.SensorReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class SettingsState(
    val simulationIntervalSeconds: Int = 5,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorReadingRepository: SensorReadingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings

    fun updateSimulationInterval(seconds: Int) {
        _settings.value = _settings.value.copy(simulationIntervalSeconds = seconds)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkModeEnabled = enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Delete readings older than 1 millisecond (essentially all)
                val currentTime = Calendar.getInstance().timeInMillis
                sensorReadingRepository.deleteOldReadings(currentTime)
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
