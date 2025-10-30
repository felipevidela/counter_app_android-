package com.example.counter_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos principal de la aplicación usando Room Database (SQLite).
 *
 * Esta clase define la configuración de la base de datos local que persiste toda
 * la información de la aplicación IoT: usuarios, dispositivos Arduino y lecturas
 * de sensores ultrasónicos.
 *
 * ## Arquitectura de Persistencia:
 *
 * ```
 * App ← ViewModel ← Repository ← DAO ← AppDatabase ← SQLite
 * ```
 *
 * ## Entidades:
 * - **User**: Usuarios autenticados con contraseñas hasheadas
 * - **Device**: Dispositivos Arduino con sensores ultrasónicos
 * - **SensorReading**: Lecturas históricas de entrada/salida de personas
 *
 * ## Relaciones:
 * - Device 1:N SensorReading (Un dispositivo tiene múltiples lecturas)
 * - Foreign Key CASCADE: Eliminar dispositivo → elimina sus lecturas
 *
 * ## Versión de Base de Datos:
 * - **Versión actual: 3**
 * - v1: Users
 * - v2: + Devices, SensorReadings
 * - v3: + location field en Device
 *
 * ## Patrón Singleton:
 * Se implementa un singleton thread-safe con doble verificación para
 * garantizar una única instancia de la base de datos en toda la app.
 *
 * @property userDao DAO para operaciones sobre usuarios
 * @property deviceDao DAO para operaciones sobre dispositivos Arduino
 * @property sensorReadingDao DAO para operaciones sobre lecturas de sensores
 *
 * @see User entidad de usuarios
 * @see Device entidad de dispositivos
 * @see SensorReading entidad de lecturas
 */
@Database(
    entities = [User::class, Device::class, SensorReading::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun sensorReadingDao(): SensorReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia singleton de la base de datos.
         *
         * Implementa el patrón Singleton con doble verificación (Double-Check Locking)
         * para garantizar thread-safety y eficiencia.
         *
         * **Configuración:**
         * - Nombre de BD: "app_database"
         * - Migración destructiva habilitada (para desarrollo)
         * - Context de aplicación usado para evitar memory leaks
         *
         * @param context Contexto de Android (se usa applicationContext)
         * @return Instancia única de AppDatabase
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // For development - recreates DB on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
