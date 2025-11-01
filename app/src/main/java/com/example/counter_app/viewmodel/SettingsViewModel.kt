package com.example.counter_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.AlertSettings
import com.example.counter_app.data.AppDatabase
import com.example.counter_app.data.SensorEventRepository
import com.example.counter_app.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SettingsState(
    val simulationIntervalSeconds: Int = 5,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorEventRepository: SensorEventRepository
    private val settingsRepository: SettingsRepository

    init {
        val database = AppDatabase.getDatabase(application)
        sensorEventRepository = SensorEventRepository(database.sensorEventDao())
        settingsRepository = SettingsRepository(database.alertSettingsDao())

        // Inicializar configuración de alertas si no existe
        viewModelScope.launch {
            settingsRepository.initializeAlertSettings()
        }
    }

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings

    // Flow para configuración de alertas
    val alertSettings: StateFlow<AlertSettings> = settingsRepository.getAlertSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlertSettings()
        )

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
                // Delete events older than 1 millisecond (essentially all)
                val currentTime = Calendar.getInstance().timeInMillis
                sensorEventRepository.deleteOldEvents(currentTime)
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Funciones para gestionar alertas configurables

    fun updateDisconnectionAlert(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDisconnectionAlert(enabled)
        }
    }

    fun updateLowOccupancyAlert(enabled: Boolean, threshold: Int) {
        viewModelScope.launch {
            settingsRepository.updateLowOccupancyAlert(enabled, threshold)
        }
    }

    fun updateHighOccupancyAlert(enabled: Boolean, threshold: Int) {
        viewModelScope.launch {
            settingsRepository.updateHighOccupancyAlert(enabled, threshold)
        }
    }

    fun updateTrafficPeakAlert(enabled: Boolean, threshold: Int) {
        viewModelScope.launch {
            settingsRepository.updateTrafficPeakAlert(enabled, threshold)
        }
    }
}
