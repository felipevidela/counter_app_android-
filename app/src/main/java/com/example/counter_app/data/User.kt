package com.example.counter_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un usuario del sistema con autenticación segura.
 *
 * Los usuarios se almacenan en la tabla "users" con contraseñas hasheadas
 * usando SHA-256 para garantizar la seguridad de las credenciales.
 *
 * **Seguridad:**
 * - Las contraseñas NUNCA se almacenan en texto plano
 * - Se utiliza SHA-256 para el hashing de contraseñas
 * - Validación de requisitos: mínimo 8 caracteres, mayúscula y carácter especial
 *
 * @property username Nombre de usuario único (Primary Key)
 * @property passwordHash Hash SHA-256 de la contraseña del usuario
 *
 * @see UserDao para operaciones de autenticación
 * @see com.example.counter_app.auth.LoginViewModel para lógica de login/registro
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String
)
