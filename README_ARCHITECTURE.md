# Arquitectura de la Aplicación Counter App

## 📋 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura General](#arquitectura-general)
3. [Capa de Datos (Data Layer)](#capa-de-datos-data-layer)
4. [Capa de Lógica (ViewModel Layer)](#capa-de-lógica-viewmodel-layer)
5. [Capa de Presentación (UI Layer)](#capa-de-presentación-ui-layer)
6. [Persistencia de Datos](#persistencia-de-datos)
7. [Flujo de Datos](#flujo-de-datos)
8. [Patrones de Diseño](#patrones-de-diseño)

---

## Resumen Ejecutivo

**Counter App** es una aplicación Android nativa construida con **Jetpack Compose** que simula dispositivos Arduino IoT con sensores ultrasónicos para conteo de personas en tiendas de mall.

### Tecnologías Clave:
- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Base de Datos**: Room Database (SQLite local)
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Persistencia**: Local (Room Database)
- **Concurrencia**: Kotlin Coroutines + Flow

---

## Arquitectura General

La aplicación sigue el patrón **MVVM** con separación clara de responsabilidades en capas:

```
┌─────────────────────────────────────────────────────────┐
│                   UI LAYER (Compose)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │  Dashboard  │  │   Device    │  │   Reports   │   │
│  │   Screen    │  │   Detail    │  │   Screen    │   │
│  └─────────────┘  └─────────────┘  └─────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │ Observa StateFlow
┌───────────────────────▼─────────────────────────────────┐
│              VIEWMODEL LAYER (Lógica)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │  Dashboard  │  │   Device    │  │   Reports   │   │
│  │  ViewModel  │  │   Detail    │  │  ViewModel  │   │
│  └─────────────┘  │  ViewModel  │  └─────────────┘   │
│                    └─────────────┘                     │
└───────────────────────┬─────────────────────────────────┘
                        │ Usa Repository
┌───────────────────────▼─────────────────────────────────┐
│            REPOSITORY LAYER (Abstracción)               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │    User     │  │   Device    │  │  Sensor     │   │
│  │ Repository  │  │ Repository  │  │  Reading    │   │
│  └─────────────┘  └─────────────┘  │ Repository  │   │
│                                     └─────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │ Llama a DAO
┌───────────────────────▼─────────────────────────────────┐
│                 DATA LAYER (Room)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │   UserDao   │  │  DeviceDao  │  │   Sensor    │   │
│  │             │  │             │  │ ReadingDao  │   │
│  └─────────────┘  └─────────────┘  └─────────────┘   │
│                                                         │
│                   ┌─────────────┐                      │
│                   │ AppDatabase │                      │
│                   └─────────────┘                      │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
              ┌──────────────────┐
              │  SQLite Database │
              │  (Local Storage) │
              └──────────────────┘
```

---

## Capa de Datos (Data Layer)

### 📦 Estructura de Paquetes

```
data/
├── AppDatabase.kt              # Room Database principal
├── Converters.kt               # Type Converters (Date ↔ Long)
│
├── User.kt                     # Entidad Usuario
├── UserDao.kt                  # DAO Usuario
├── UserRepository.kt           # Repositorio Usuario
│
├── Device.kt                   # Entidad Dispositivo Arduino
├── DeviceDao.kt                # DAO Dispositivo
├── DeviceRepository.kt         # Repositorio Dispositivo
│
├── SensorReading.kt            # Entidad Lectura de Sensor
├── SensorReadingDao.kt         # DAO Lectura
└── SensorReadingRepository.kt  # Repositorio Lectura
```

### 📊 Modelo de Datos (ERD)

```
┌──────────────────┐
│      User        │
├──────────────────┤
│ PK username      │
│    passwordHash  │
└──────────────────┘

┌──────────────────────────┐         ┌─────────────────────────┐
│        Device            │ 1     N │    SensorReading        │
├──────────────────────────┤─────────├─────────────────────────┤
│ PK id                    │         │ PK id                   │
│    name                  │         │ FK deviceId             │
│    type                  │         │    entered              │
│    macAddress            │         │    left                 │
│    capacity              │         │    capacity             │
│    location              │         │    timestamp            │
│    isActive              │         └─────────────────────────┘
│    createdAt             │
└──────────────────────────┘
```

### 🔑 Entidades Principales

#### 1. User
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String  // SHA-256
)
```
- **Propósito**: Autenticación de usuarios
- **Seguridad**: Contraseñas hasheadas con SHA-256
- **Validación**: Mínimo 8 caracteres, mayúscula y carácter especial

#### 2. Device
```kotlin
@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // "Sensor Entrada Principal"
    val type: String,           // "Arduino Uno", "ESP32", etc.
    val macAddress: String,     // MAC simulada única
    val capacity: Int = 100,    // Aforo máximo
    val location: String = "",  // "Entrada Principal"
    val isActive: Boolean = true,
    val createdAt: Long
)
```
- **Propósito**: Dispositivos Arduino con sensores ultrasónicos
- **Simulación**: 2 sensores (entrada/salida)
- **Estado**: Activo/Inactivo para control de simulación

#### 3. SensorReading
```kotlin
@Entity(
    tableName = "sensor_readings",
    foreignKeys = [ForeignKey(
        entity = Device::class,
        parentColumns = ["id"],
        childColumns = ["deviceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["deviceId"])]
)
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,         // FK → Device.id
    val entered: Int,           // Detecciones entrada
    val left: Int,              // Detecciones salida
    val capacity: Int,
    val timestamp: Long
)
```
- **Propósito**: Historial de lecturas de sensores
- **Relación**: Many-to-One con Device (CASCADE delete)
- **Índice**: Optimizado para consultas por deviceId
- **Cálculo aforo**: `entered - left`

---

## Capa de Lógica (ViewModel Layer)

### 📂 Estructura

```
viewmodel/
├── DashboardViewModel.kt          # Lista de dispositivos
├── DeviceDetailViewModel.kt       # Detalle + lecturas
├── DeviceRegistrationViewModel.kt # Crear dispositivo
├── ReportsViewModel.kt            # Gráficos temporales
└── SettingsViewModel.kt           # Configuración
```

### 🔄 Patrón ViewModel

Cada ViewModel:
1. **Extiende AndroidViewModel** para acceso al Application Context
2. **Inicializa Repositories** en el `init` block
3. **Expone StateFlow/Flow** para datos reactivos
4. **Maneja lógica de negocio** (validaciones, transformaciones)
5. **Ejecuta operaciones en viewModelScope** (coroutines)

#### Ejemplo: DashboardViewModel

```kotlin
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val deviceRepository: DeviceRepository
    private val sensorReadingRepository: SensorReadingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        deviceRepository = DeviceRepository(database.deviceDao())
        sensorReadingRepository = SensorReadingRepository(database.sensorReadingDao())
    }

    // Flow reactivo: UI se actualiza automáticamente
    val devices: Flow<List<Device>> = deviceRepository.getAllDevices()

    // Combina dispositivos con sus últimas lecturas
    val devicesWithReadings: Flow<List<DeviceWithLatestReading>> =
        devices.flatMapLatest { deviceList ->
            // Lógica de combinación...
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operación que modifica estado
    fun toggleDeviceStatus(deviceId: Long, isActive: Boolean) {
        viewModelScope.launch {
            deviceRepository.updateDeviceActiveStatus(deviceId, isActive)
        }
    }
}
```

---

## Capa de Presentación (UI Layer)

### 🎨 Jetpack Compose + Material Design 3

```
ui/
├── DashboardScreen.kt         # Lista de dispositivos
├── DeviceDetailScreen.kt      # Vista detallada
├── DeviceRegistrationScreen.kt # Formulario creación
├── ReportsScreen.kt           # Gráficos Vico
├── SettingsScreen.kt          # Configuración
├── LoginScreen.kt             # Autenticación
├── RegistrationScreen.kt      # Registro usuario
└── theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

### 📱 Componentes UI Principales

#### DashboardScreen
```kotlin
@Composable
fun DashboardScreen(
    onDeviceClick: (Long) -> Unit,
    onAddDeviceClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val devicesWithReadings by viewModel.devicesWithReadings.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Dispositivos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDeviceClick) {
                Icon(Icons.Default.Add, "Agregar")
            }
        }
    ) { padding ->
        LazyColumn {
            items(devicesWithReadings) { item ->
                DeviceCard(
                    deviceWithReading = item,
                    onClick = { onDeviceClick(item.device.id) }
                )
            }
        }
    }
}
```

#### Características UI:
- ✅ **Material Design 3** componentes
- ✅ **Iconos Material** (Sensors, Login, Logout, Warning)
- ✅ **Cards elevadas** con sombras
- ✅ **LinearProgressIndicator** para aforo
- ✅ **Chips de estado** (Activo/Inactivo)
- ✅ **Colores dinámicos** según nivel de ocupación

---

## Persistencia de Datos

### 💾 Room Database (SQLite Local)

#### Configuración

```kotlin
@Database(
    entities = [User::class, Device::class, SensorReading::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Singleton thread-safe
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                 .build()
            }
        }
    }
}
```

#### Características de Persistencia:

1. **Local Storage (SQLite)**
   - Base de datos en el dispositivo
   - No requiere conexión a internet
   - Datos persisten entre sesiones

2. **Room Annotations**
   - `@Entity`: Define tablas
   - `@PrimaryKey`: Clave primaria (autoGenerate)
   - `@ForeignKey`: Relaciones entre tablas
   - `@Index`: Optimización de consultas
   - `@TypeConverter`: Conversión de tipos

3. **Operaciones Asíncronas**
   - Todas las operaciones de BD son `suspend functions`
   - Se ejecutan en background threads (IO Dispatcher)
   - UI nunca se bloquea

4. **Reactive Queries (Flow)**
   ```kotlin
   @Query("SELECT * FROM devices ORDER BY createdAt DESC")
   fun getAllDevices(): Flow<List<Device>>
   ```
   - Retorna Flow que emite automáticamente cuando hay cambios
   - UI se actualiza reactivamente con `collectAsState()`

---

## Flujo de Datos

### 📊 Flujo Completo: Crear Dispositivo

```
┌──────────────────────────────────────────────────────────────┐
│ 1. USER INTERACTION (UI Layer)                              │
│    DeviceRegistrationScreen                                  │
│    ├─ Usuario llena formulario                              │
│    ├─ Click "Crear Dispositivo Arduino"                     │
│    └─ onClick: viewModel.createDevice(...)                  │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. BUSINESS LOGIC (ViewModel Layer)                         │
│    DeviceRegistrationViewModel                              │
│    ├─ Validación de campos                                  │
│    ├─ Generación de MAC address                            │
│    └─ viewModelScope.launch {                              │
│          repository.insertDevice(device)                    │
│        }                                                    │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. DATA ABSTRACTION (Repository Layer)                      │
│    DeviceRepository                                          │
│    └─ suspend fun insertDevice(device: Device): Long {     │
│          deviceDao.insertDevice(device)                     │
│        }                                                    │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. DATABASE ACCESS (DAO Layer)                              │
│    DeviceDao                                                 │
│    @Insert(onConflict = OnConflictStrategy.REPLACE)        │
│    suspend fun insertDevice(device: Device): Long           │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. PERSISTENCE (Room Database)                              │
│    AppDatabase → SQLite                                      │
│    ├─ INSERT INTO devices VALUES (...)                     │
│    └─ Retorna ID autogenerado                              │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. REACTIVE UPDATE (Flow emission)                          │
│    deviceDao.getAllDevices() emite nuevo valor              │
│    ├─ DashboardViewModel.devices recibe actualización      │
│    └─ UI se actualiza automáticamente (collectAsState)     │
└──────────────────────────────────────────────────────────────┘
```

### 🔄 Simulación en Tiempo Real

```
DeviceSimulationService (cada 5 segundos)
    ├─ getAllDevices().filter { it.isActive }
    ├─ Para cada dispositivo activo:
    │   ├─ Generar datos aleatorios (entrada/salida)
    │   ├─ Simular grupos de personas (mall traffic)
    │   └─ insertReading(SensorReading(...))
    └─ UI se actualiza automáticamente vía Flow
```

---

## Patrones de Diseño

### 1. **MVVM (Model-View-ViewModel)**
- **Separación de responsabilidades**
- **Testabilidad mejorada**
- **UI reactiva**

### 2. **Repository Pattern**
```kotlin
class DeviceRepository(private val deviceDao: DeviceDao) {
    fun getAllDevices(): Flow<List<Device>> = deviceDao.getAllDevices()
    suspend fun insertDevice(device: Device) = deviceDao.insertDevice(device)
}
```
- **Abstracción de fuente de datos**
- **Fácil cambio de implementación** (local ↔ remoto)
- **Single Source of Truth**

### 3. **Singleton (Database)**
```kotlin
companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            // Double-check locking
        }
    }
}
```
- **Thread-safe**
- **Única instancia en toda la app**
- **Evita memory leaks** (applicationContext)

### 4. **Observer Pattern (Flow)**
```kotlin
val devices: Flow<List<Device>>

// En UI:
val devices by viewModel.devices.collectAsState(initial = emptyList())
```
- **Actualizaciones automáticas**
- **Programación reactiva**
- **Desacoplamiento**

### 5. **Dependency Injection Manual**
```kotlin
init {
    val database = AppDatabase.getDatabase(application)
    deviceRepository = DeviceRepository(database.deviceDao())
}
```
- **Constructor injection** en ViewModels
- **No requiere framework** (Dagger/Hilt)
- **Simple y directo**

---

## 🚀 Resumen Técnico

| Aspecto | Implementación |
|---------|---------------|
| **Arquitectura** | MVVM + Repository Pattern |
| **UI** | Jetpack Compose + Material Design 3 |
| **Persistencia** | Room Database (SQLite) |
| **Concurrencia** | Kotlin Coroutines + Flow |
| **Navegación** | Jetpack Navigation Compose |
| **Inyección** | Manual (ViewModel init) |
| **Reactividad** | StateFlow / Flow |
| **Testing** | Room In-Memory DB, Unit Tests |

---

## 📚 Referencias

- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Material Design 3](https://m3.material.io/)
- [Android MVVM](https://developer.android.com/topic/architecture)

---

**Última actualización**: 2025-01-30
**Versión de Base de Datos**: 3
**Versión de App**: 1.0.0
