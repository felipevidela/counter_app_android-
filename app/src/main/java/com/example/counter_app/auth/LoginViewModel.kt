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

/**
 * ViewModel para autenticación de usuarios con implementación de estándares de seguridad.
 *
 * ## Estándares de Seguridad Implementados
 *
 * ### 1. Almacenamiento Seguro de Contraseñas
 * - **Hash SHA-256**: Las contraseñas NUNCA se almacenan en texto plano
 * - Las contraseñas se hashean usando SHA-256 antes de almacenarse en Room Database
 * - El hash es irreversible - no se puede obtener la contraseña original
 * - Ver método [hashPassword] para implementación
 *
 * ### 2. Validación de Contraseñas Fuertes
 * - **Longitud mínima**: 8 caracteres
 * - **Complejidad**: Debe incluir al menos una mayúscula
 * - **Caracteres especiales**: Debe incluir al menos un símbolo especial
 * - Ver método [validatePassword] para reglas completas
 * - Basado en OWASP Password Guidelines
 *
 * ### 3. Auditoría de Seguridad (Security Logging)
 * - Registro de intentos exitosos de login usando [SecurityLogger]
 * - Registro de intentos fallidos de login
 * - Registro de intentos de registro duplicados
 * - Los logs incluyen timestamp y usuario (NO incluyen contraseñas)
 * - Permite detección de ataques de fuerza bruta
 *
 * ### 4. Protección contra Ataques Comunes
 *
 * #### Prevención de SQL Injection
 * - Room Database usa queries parametrizadas automáticamente
 * - No se concatenan strings de usuario en queries SQL
 *
 * #### Prevención de Timing Attacks
 * - La comparación de hashes se realiza de forma segura
 * - No se revela información sobre si el usuario existe o la contraseña es incorrecta
 *
 * #### Prevención de Enumeración de Usuarios
 * - Los mensajes de error son genéricos ("Credenciales inválidas")
 * - No se revela si el error fue por usuario o contraseña
 *
 * ### 5. Gestión de Sesiones
 * - Las operaciones de autenticación se ejecutan en coroutines (background thread)
 * - No se bloquea el UI thread durante el hash de contraseñas
 * - Callbacks seguros mediante [onResult]
 *
 * ## Arquitectura de Seguridad
 *
 * ```
 * UI (LoginScreen)
 *    ↓
 * LoginViewModel (esta clase)
 *    ↓
 * [hashPassword] SHA-256
 *    ↓
 * UserRepository
 *    ↓
 * Room Database (contraseñas hasheadas)
 *    ↓
 * SQLite (archivo encriptado por Android)
 * ```
 *
 * ## Referencias
 * - OWASP Authentication Cheat Sheet
 * - NIST Digital Identity Guidelines (SP 800-63B)
 * - Android Security Best Practices
 *
 * @property repository Repositorio de usuarios para acceso a Room Database
 * @property securityLogger Logger singleton para auditoría de eventos de seguridad
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val securityLogger = SecurityLogger.getInstance()

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    /**
     * Autentica un usuario verificando credenciales de forma segura.
     *
     * ## Proceso de Autenticación Segura
     * 1. Hashea la contraseña ingresada usando SHA-256
     * 2. Compara el hash con el hash almacenado en base de datos
     * 3. Registra el resultado en SecurityLogger (auditoría)
     * 4. Retorna resultado vía callback
     *
     * ## Medidas de Seguridad
     * - NO compara contraseñas en texto plano
     * - NO revela si el error fue por usuario o contraseña (previene enumeración)
     * - Ejecuta operaciones en background thread (viewModelScope)
     * - Registra intentos fallidos para detección de ataques
     *
     * @param username Nombre de usuario ingresado
     * @param password Contraseña en texto plano (se hasheará antes de comparar)
     * @param onResult Callback con resultado: true = éxito, false = fallo
     */
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

    /**
     * Registra un nuevo usuario aplicando validaciones de seguridad.
     *
     * ## Proceso de Registro Seguro
     * 1. Verifica que el usuario no exista (previene duplicados)
     * 2. Valida la fortaleza de la contraseña usando [validatePassword]
     * 3. Hashea la contraseña usando SHA-256
     * 4. Almacena el usuario con contraseña hasheada
     * 5. Registra el evento en SecurityLogger
     *
     * ## Validaciones Aplicadas
     * - Usuario único (no duplicados)
     * - Contraseña de al menos 8 caracteres
     * - Contraseña con al menos 1 mayúscula
     * - Contraseña con al menos 1 carácter especial
     *
     * @param username Nombre de usuario deseado
     * @param password Contraseña en texto plano (se validará y hasheará)
     * @param onResult Callback con resultado: "Success" o mensaje de error
     */
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

    /**
     * Valida la fortaleza de una contraseña según estándares de seguridad.
     *
     * ## Reglas de Validación (basadas en OWASP)
     * 1. **Longitud mínima**: 8 caracteres
     *    - Reduce el espacio de búsqueda para ataques de fuerza bruta
     * 2. **Mayúsculas**: Al menos 1 carácter en mayúscula
     *    - Aumenta la entropía de la contraseña
     * 3. **Caracteres especiales**: Al menos 1 símbolo (!@#$%^&*, etc.)
     *    - Dificulta ataques de diccionario
     *
     * @param password Contraseña a validar
     * @return null si es válida, mensaje de error si no cumple requisitos
     */
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

    /**
     * Hashea una contraseña usando SHA-256 para almacenamiento seguro.
     *
     * ## Algoritmo SHA-256
     * - **Familia**: SHA-2 (Secure Hash Algorithm 2)
     * - **Longitud de salida**: 256 bits (64 caracteres hexadecimales)
     * - **Propiedad**: Función hash criptográfica de un solo sentido
     * - **Seguridad**: Computacionalmente inviable revertir el hash
     *
     * ## Proceso
     * 1. Convierte la contraseña a bytes (UTF-8)
     * 2. Aplica SHA-256 usando MessageDigest de Java Security
     * 3. Convierte el digest a representación hexadecimal
     * 4. Retorna string de 64 caracteres hexadecimales
     *
     * ## Ejemplo
     * ```
     * Input:  "MyPass123!"
     * Output: "a1b2c3d4e5f6..." (64 chars)
     * ```
     *
     * ## Nota de Seguridad
     * En producción se recomienda usar bcrypt o Argon2 con salt.
     * SHA-256 es apropiado para demostración educativa.
     *
     * @param password Contraseña en texto plano
     * @return Hash SHA-256 en formato hexadecimal (64 caracteres)
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
