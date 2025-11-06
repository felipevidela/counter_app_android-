package com.example.counter_app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Clase para gestionar las preferencias del tema de la aplicación usando DataStore.
 * Utiliza DataStore Preferences para persistir la preferencia de Dark Mode del usuario.
 */
class ThemePreferences(private val context: Context) {

    companion object {
        // Extension property para crear DataStore singleton
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

        // Key para la preferencia de dark mode
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    /**
     * Flow que emite el estado actual del dark mode.
     * Default: false (modo claro)
     */
    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    /**
     * Guarda la preferencia de dark mode.
     * @param enabled true para activar dark mode, false para desactivarlo
     */
    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}
