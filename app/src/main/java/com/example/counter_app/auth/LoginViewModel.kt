package com.example.counter_app.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.counter_app.data.AppDatabase
import com.example.counter_app.data.User
import com.example.counter_app.data.UserRepository
import com.example.counter_app.security.SecurityLogger
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val securityLogger = SecurityLogger.getInstance()

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUser(username)
            if (user != null && user.passwordHash == hashPassword(password)) {
                // Security: Log successful authentication
                securityLogger.logLoginSuccess(username)
                onResult(true)
            } else {
                // Security: Log failed authentication attempt
                securityLogger.logLoginFailure(username, "Credenciales inválidas")
                onResult(false)
            }
        }
    }

    fun register(username: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            if (repository.getUser(username) != null) {
                // Security: Log failed registration (user already exists)
                securityLogger.logLoginFailure(username, "Intento de registro con usuario existente")
                onResult("El usuario ya existe")
                return@launch
            }

            val passwordError = validatePassword(password)
            if (passwordError != null) {
                onResult(passwordError)
                return@launch
            }

            val user = User(username, hashPassword(password))
            repository.insertUser(user)

            // Security: Log successful registration
            securityLogger.logLoginSuccess(username)
            onResult("Success")
        }
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }
        if (!password.any { it.isUpperCase() }) {
            return "La contraseña debe tener al menos una mayúscula"
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return "La contraseña debe tener al menos un caracter especial"
        }
        return null
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
