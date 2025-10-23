# Architecture Documentation - Counter APP

## 📋 Resumen Ejecutivo

Counter APP implementa una arquitectura moderna de Android basada en **MVVM (Model-View-ViewModel)** con patrones avanzados de reactive programming usando **Kotlin Coroutines** y **StateFlow**. La innovación clave es el uso de **hot flows** para mantener un servicio Bluetooth persistente que actualiza datos en tiempo real sin necesidad de observadores activos.

## 🏗️ Arquitectura General

### Diagrama de Capas

```
┌────────────────────────────────────────────────────────────────┐
│                        UI Layer (Jetpack Compose)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ LoginScreen  │  │ MainScreen   │  │ EventLogS    │        │
│  │              │  │  ├─Monitoring│  │              │        │
│  │              │  │  └─Activator │  │              │        │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘        │
│         │                  │                  │                │
├─────────┼──────────────────┼──────────────────┼────────────────┤
│         │    ViewModel Layer                  │                │
│  ┌──────▼───────┐                    ┌────────▼─────────┐     │
│  │LoginViewModel│                    │ (stateless UIs)  │     │
│  └──────┬───────┘                    └──────────────────┘     │
│         │                                                      │
├─────────┼──────────────────────────────────────────────────────┤
│         │        Repository Layer                              │
│  ┌──────▼───────────┐                                          │
│  │  UserRepository  │                                          │
│  └──────┬───────────┘                                          │
│         │                                                      │
├─────────┼──────────────────────────────────────────────────────┤
│         │         Data Layer                                   │
│  ┌──────▼──────┐                  ┌───────────────────────┐   │
│  │ AppDatabase │                  │ BluetoothService      │   │
│  │  (Room)     │                  │  (Singleton via       │   │
│  │  - User DAO │                  │   CompositionLocal)   │   │
│  └─────────────┘                  └───────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

## 🎯 Patrones de Diseño Implementados

### 1. MVVM (Model-View-ViewModel)

**Ubicación**: Todo el proyecto

**Implementación**:
- **Model**: Entidades de Room (`User`, `CounterData`, `CounterEvent`)
- **View**: Composables (`LoginScreen`, `MonitoringScreen`, etc.)
- **ViewModel**: `LoginViewModel` para lógica de negocio

**Ejemplo**:
```kotlin
// View (LoginScreen.kt)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    // UI consume datos del ViewModel
}

// ViewModel (LoginViewModel.kt)
class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Lógica de negocio
        }
    }
}

// Model (User.kt)
@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String
)
```

---

### 2. Repository Pattern

**Ubicación**: `UserRepository.kt`

**Propósito**: Abstracción de fuente de datos

**Implementación**:
```kotlin
class UserRepository(private val userDao: UserDao) {
    suspend fun getUser(username: String): User? {
        return userDao.getUser(username)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}
```

**Beneficios**:
- Desacoplamiento de UI y base de datos
- Facilita testing con mocks
- Single source of truth

---

### 3. Dependency Injection via CompositionLocal

**Ubicación**: `LocalBluetoothService.kt`, `AppNavigation.kt`

**Innovación clave**: Singleton service compartido globalmente

**Implementación**:
```kotlin
// Definición del CompositionLocal
val LocalBluetoothService = staticCompositionLocalOf<BluetoothServiceInterface> {
    error("No BluetoothService provided")
}

// Provisión del servicio (AppNavigation.kt)
composable("main") {
    val bluetoothService = remember {
        if (USE_SIMULATED_SERVICE) SimulatedBluetoothService()
        else BluetoothService(context)
    }

    DisposableEffect(Unit) {
        onDispose { bluetoothService.disconnect() }
    }

    CompositionLocalProvider(LocalBluetoothService provides bluetoothService) {
        MainScreen()
    }
}

// Consumo del servicio (MonitoringScreen.kt)
@Composable
fun MonitoringScreen(navController: NavHostController) {
    val bluetoothService = LocalBluetoothService.current
    // Usar el servicio
}
```

**Ventajas**:
- Evita prop drilling (pasar parámetros manualmente)
- Service persiste durante toda la sesión
- Lifecycle management automático con DisposableEffect

---

### 4. Hot Flows Architecture (Innovación Principal)

**Ubicación**: `SimulatedBluetoothService.kt`

**Problema Resuelto**: Los cold flows dejan de emitir cuando no hay observadores

**Solución**: StateFlow con CoroutineScope persistente

**Arquitectura**:
```
┌─────────────────────────────────────────────────────────────┐
│           SimulatedBluetoothService                         │
│  ┌───────────────────────────────────────────────────┐     │
│  │  serviceScope (CoroutineScope)                    │     │
│  │  └── counterJob (Job)                             │     │
│  │       └── while(isConnected) {                    │     │
│  │            delay(3000)                             │     │
│  │            // Genera eventos automáticamente       │     │
│  │            _counterDataFlow.emit(newData)          │     │
│  │            _eventLogFlow.emit(newEvent)            │     │
│  │          }                                         │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
│  ┌───────────────────────────────────────────────────┐     │
│  │  StateFlows (Hot Flows - Siempre activos)        │     │
│  │  ├── _counterDataFlow: MutableStateFlow           │     │
│  │  ├── _eventLogFlow: MutableStateFlow              │     │
│  │  └── _connectionStateFlow: MutableStateFlow       │     │
│  └───────────────────────────────────────────────────┘     │
│                         ▲                                   │
│                         │ collectAsState()                  │
│  ┌──────────────────────┴──────────────────────────┐       │
│  │  Multiple Observers (UI Screens)                │       │
│  │  ├── MonitoringScreen                           │       │
│  │  ├── EventLogScreen                             │       │
│  │  └── ActivatorScreen                            │       │
│  └─────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

**Código**:
```kotlin
class SimulatedBluetoothService : BluetoothServiceInterface {
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var counterJob: Job? = null

    // Hot flows - emiten continuamente
    private val _counterDataFlow = MutableStateFlow(CounterData(0, 0, 0))
    private val _eventLogFlow = MutableStateFlow<List<CounterEvent>>(emptyList())

    override fun connectToDevice(deviceAddress: String) {
        isConnected = true
        _connectionStateFlow.value = "Conectado"
        startCounterGeneration()
    }

    private fun startCounterGeneration() {
        counterJob = serviceScope.launch {
            while (isConnected) {
                delay(3000)
                // Genera eventos SIN necesidad de observadores
                val newEvent = generateEvent()
                _eventLogFlow.value = currentEvents + newEvent
                _counterDataFlow.value = newCounterData
            }
        }
    }
}
```

**Flujo de Datos**:
1. Usuario conecta a dispositivo
2. `startCounterGeneration()` lanza coroutine en background
3. Coroutine emite datos cada 3 segundos **independientemente de observadores**
4. UI screens observan StateFlow con `collectAsState()`
5. Navegación entre pantallas **no interrumpe** el flujo de datos
6. Eventos aparecen en EventLogScreen **sin refrescar**

---

### 5. Navigation Architecture

**Ubicación**: `AppNavigation.kt`, `MainScreen.kt`

**Arquitectura de dos niveles**:

```
┌─────────────────────────────────────────────────────────────┐
│  AppNavigation (Root NavHost)                               │
│  ┌───────────────────────────────────────────────────┐     │
│  │  NavHost(startDestination = "login")              │     │
│  │  ├── Route: "login" → LoginScreen                 │     │
│  │  └── Route: "main" → MainScreen                   │     │
│  │       ├── Create BluetoothService (singleton)     │     │
│  │       └── Provide via CompositionLocal            │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  MainScreen (Internal NavHost)                              │
│  ┌───────────────────────────────────────────────────┐     │
│  │  val internalNavController = rememberNavController()  │  │
│  │  NavHost(internalNavController, "monitoring")     │     │
│  │  ├── Route: "monitoring" → MonitoringScreen       │     │
│  │  ├── Route: "activator" → ActivatorScreen         │     │
│  │  └── Route: "event_log" → EventLogScreen          │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

**Por qué dos NavHosts**:
- **Root NavHost**: Maneja autenticación (login → main)
- **Internal NavHost**: Navegación dentro de la app (tabs + event log)
- Evita conflictos de ViewModelStore
- BluetoothService persiste durante toda la sesión en "main"

**Código**:
```kotlin
// AppNavigation.kt - Root level
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { navController.navigate("main") })
        }
        composable("main") {
            val bluetoothService = remember { SimulatedBluetoothService() }
            CompositionLocalProvider(LocalBluetoothService provides bluetoothService) {
                MainScreen()
            }
        }
    }
}

// MainScreen.kt - Internal navigation
@Composable
fun MainScreen() {
    val internalNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(internalNavController) }
    ) {
        NavHost(internalNavController, startDestination = Screen.Monitoring.route) {
            composable(Screen.Monitoring.route) { MonitoringScreen(internalNavController) }
            composable(Screen.Activator.route) { ActivatorScreen() }
            composable("event_log") { EventLogScreen(internalNavController) }
        }
    }
}
```

---

### 6. State Management con StateFlow

**Ubicación**: `SimulatedBluetoothService.kt`

**Patrón**: Unidirectional Data Flow (UDF)

**Arquitectura**:
```
┌────────────────────────────────────────────────────┐
│  Service Layer (State Source)                      │
│  ┌──────────────────────────────────────────┐     │
│  │  private val _stateFlow = MutableStateFlow│     │
│  │  val stateFlow: StateFlow = _stateFlow    │     │
│  └──────────────────────────────────────────┘     │
└──────────────────┬─────────────────────────────────┘
                   │ Read-only exposure
                   ▼
┌────────────────────────────────────────────────────┐
│  UI Layer (State Consumer)                         │
│  ┌──────────────────────────────────────────┐     │
│  │  val state by stateFlow.collectAsState()  │     │
│  │  // UI recompose automáticamente          │     │
│  └──────────────────────────────────────────┘     │
└────────────────────────────────────────────────────┘
```

**Tipos de StateFlow**:

1. **CounterData** (Datos del contador)
```kotlin
private val _counterDataFlow = MutableStateFlow(CounterData(0, 0, 0))
override fun getCounterDataFlow(): StateFlow<CounterData> = _counterDataFlow
```

2. **EventLog** (Lista de eventos)
```kotlin
private val _eventLogFlow = MutableStateFlow<List<CounterEvent>>(emptyList())
override fun getEventLogFlow(): StateFlow<List<CounterEvent>> = _eventLogFlow
```

3. **ConnectionState** (Estado de conexión)
```kotlin
private val _connectionStateFlow = MutableStateFlow("Desconectado")
override fun getConnectionStateFlow(): StateFlow<String> = _connectionStateFlow
```

**Beneficios**:
- Estado inmutable expuesto a UI
- Updates automáticos con `collectAsState()`
- Thread-safe por diseño
- No hay race conditions

---

## 🔄 Flujo de Datos Completo

### Escenario: Usuario conecta a dispositivo y navega entre pantallas

```
1. Usuario presiona "Buscar" en MonitoringScreen
   └── MonitoringScreen.kt:109
       └── bluetoothService.searchDevices()

2. Usuario selecciona dispositivo
   └── MonitoringScreen.kt:133
       └── bluetoothService.connectToDevice(address)

3. BluetoothService inicia hot flow
   └── SimulatedBluetoothService.kt:71
       └── startCounterGeneration()
           ├── Coroutine en background emite cada 3s
           ├── _counterDataFlow.value = newData
           └── _eventLogFlow.value = events

4. MonitoringScreen observa cambios
   └── MonitoringScreen.kt:43
       └── val counterData by getCounterDataFlow().collectAsState()
           └── UI recompone automáticamente

5. Usuario navega a EventLogScreen
   └── MonitoringScreen.kt:163
       └── navController.navigate("event_log")
           └── Hot flow SIGUE EMITIENDO en background

6. EventLogScreen muestra eventos en tiempo real
   └── EventLogScreen.kt:13
       └── val events by getEventLogFlow().collectAsState()
           └── Lista se actualiza automáticamente

7. Usuario regresa a MonitoringScreen
   └── EventLogScreen.kt:30
       └── navController.popBackStack()
           └── Estado de conexión PERSISTE (no se pierde)

8. Usuario desconecta
   └── MonitoringScreen.kt:185
       └── bluetoothService.resetAndDisconnect()
           ├── counterJob?.cancel() (detiene coroutine)
           ├── Reset de todos los StateFlow
           └── UI actualiza instantáneamente
```

---

## 🗂️ Estructura de Directorios

```
app/src/main/java/com/example/counter_app/
├── MainActivity.kt                    # Entry point
├── navigation/
│   └── AppNavigation.kt               # Root navigation
├── ui/
│   ├── MainScreen.kt                  # Internal navigation
│   ├── LoginScreen.kt                 # Autenticación
│   ├── RegistrationScreen.kt          # Registro de usuarios
│   ├── MonitoringScreen.kt            # Tab 1 - Monitoreo
│   ├── ActivatorScreen.kt             # Tab 2 - Control
│   ├── EventLogScreen.kt              # Pantalla de eventos
│   └── theme/
│       ├── Color.kt                   # Color scheme
│       ├── Theme.kt                   # Material3 theme
│       └── Type.kt                    # Typography
├── viewmodel/
│   └── LoginViewModel.kt              # Lógica de login
├── data/
│   ├── User.kt                        # Entity
│   ├── CounterData.kt                 # Data class
│   ├── CounterEvent.kt                # Data class
│   ├── AppDatabase.kt                 # Room DB
│   ├── UserDao.kt                     # DAO
│   └── UserRepository.kt              # Repository
└── bluetooth/
    ├── BluetoothServiceInterface.kt   # Interface
    ├── SimulatedBluetoothService.kt   # Implementación simulada
    ├── BluetoothService.kt            # Implementación real (placeholder)
    └── LocalBluetoothService.kt       # CompositionLocal
```

---

## 🔐 Capas de Seguridad

### 1. Authentication Layer
```
LoginScreen → LoginViewModel → UserRepository → Room DB
                    ├── SHA-256 hashing
                    ├── Validación de complejidad
                    └── No passwords en logs
```

### 2. Data Protection Layer
```
Room Database
├── Passwords: SHA-256 hash
├── User data: Android sandbox
└── Events: In-memory StateFlow
```

### 3. Communication Layer
```
Bluetooth BLE
├── Pairing required
├── Link layer encryption
└── Manual device selection
```

---

## 📊 Performance Considerations

### 1. Memory Management

**StateFlow vs Flow**:
- StateFlow mantiene último valor en memoria (overhead mínimo)
- Evita recrear Flow cada vez que UI observa
- Conflate por defecto (solo último valor importa)

**Cleanup**:
```kotlin
DisposableEffect(Unit) {
    onDispose {
        bluetoothService.disconnect()  // Limpia recursos
    }
}
```

### 2. Threading

**Dispatchers utilizados**:
- `Dispatchers.Default`: Background coroutine en BluetoothService
- `Dispatchers.Main`: UI updates (automático con StateFlow)
- `Dispatchers.IO`: Room Database operations

**Código**:
```kotlin
// Background worker
private val serviceScope = CoroutineScope(Dispatchers.Default)

// Database operations
viewModelScope.launch(Dispatchers.IO) {
    repository.getUser(username)
}
```

### 3. UI Performance

**Jetpack Compose optimizations**:
- `remember`: Evita recrear BluetoothService
- `derivedStateOf`: Evita recomposiciones innecesarias
- `LazyColumn`: Virtualización de lista de eventos

---

## 🧪 Testability

### Unit Testing Strategy

**ViewModel Testing**:
```kotlin
@Test
fun `login with valid credentials should succeed`() = runTest {
    val repository = FakeUserRepository()
    val viewModel = LoginViewModel(repository)

    var result: Boolean? = null
    viewModel.login("user", "Pass123!", { result = it })

    assertThat(result).isTrue()
}
```

**Repository Testing**:
```kotlin
@Test
fun `getUser should return user from database`() = runTest {
    val dao = FakeUserDao()
    val repository = UserRepository(dao)

    val user = repository.getUser("testuser")

    assertThat(user).isNotNull()
}
```

**Service Testing**:
```kotlin
@Test
fun `connectToDevice should emit connected state`() = runTest {
    val service = SimulatedBluetoothService()

    service.connectToDevice("00:11:22:33:44:55")

    assertThat(service.getConnectionStateFlow().value).isEqualTo("Conectado")
}
```

---

## 🔮 Arquitectura Futura

### Planificado

1. **Multi-device Support**
```
BluetoothService
├── Device 1 → StateFlow<Data1>
├── Device 2 → StateFlow<Data2>
└── Device 3 → StateFlow<Data3>
```

2. **Cloud Sync**
```
Local DB ←→ Sync Manager ←→ Firebase/Backend
           ├── Work Manager
           └── Conflict resolution
```

3. **Advanced Security**
```
AES Encryption Layer
├── Key Management (Keystore)
├── Certificate pinning
└── End-to-end encryption
```

---

## 📚 Decisiones de Arquitectura

### Por qué StateFlow en vez de LiveData?

| Aspecto | StateFlow | LiveData |
|---------|-----------|----------|
| Lifecycle-aware | No (manual) | Sí (automático) |
| Thread-safety | Sí | Sí |
| Coroutine support | Nativo | Limitado |
| Compose support | Excelente | Bueno |
| Background emission | Sí | No |

**Decisión**: StateFlow permite hot flows que emiten en background, esencial para este proyecto.

### Por qué CompositionLocal en vez de Hilt?

| Aspecto | CompositionLocal | Hilt |
|---------|------------------|------|
| Setup complexity | Bajo | Alto |
| Boilerplate | Mínimo | Moderado |
| Scope control | Composable-level | Application/Activity |
| Learning curve | Bajo | Alto |

**Decisión**: Para un servicio singleton simple, CompositionLocal es suficiente sin la complejidad de Hilt.

### Por qué Room en vez de DataStore?

| Aspecto | Room | DataStore |
|---------|------|-----------|
| Structured data | Excelente | Limitado |
| Queries | SQL | Key-value |
| Relations | Sí | No |
| Type safety | Sí | Parcial |

**Decisión**: Room permite queries complejas y relaciones, necesario para sistema de usuarios.

---

## 🎓 Principios SOLID Aplicados

### 1. Single Responsibility Principle (SRP)
- `LoginViewModel`: Solo lógica de autenticación
- `UserRepository`: Solo acceso a datos de usuarios
- `BluetoothService`: Solo comunicación BLE

### 2. Open/Closed Principle (OCP)
- `BluetoothServiceInterface`: Interface abierta para extensión
- `SimulatedBluetoothService` vs `BluetoothService`: Implementaciones cerradas para modificación

### 3. Liskov Substitution Principle (LSP)
- Cualquier `BluetoothServiceInterface` puede ser sustituida sin romper código

### 4. Interface Segregation Principle (ISP)
- Interface específica para Bluetooth, no "God interface"

### 5. Dependency Inversion Principle (DIP)
- UI depende de abstracción (`BluetoothServiceInterface`), no implementación concreta

---

## 📞 Conclusión

Counter APP implementa una arquitectura moderna, escalable y mantenible que:

✅ Separa responsabilidades en capas bien definidas
✅ Utiliza reactive programming para actualizaciones en tiempo real
✅ Implementa patrones de diseño probados (MVVM, Repository, DI)
✅ Innova con hot flows para background processing
✅ Facilita testing con interfaces y abstracciones
✅ Sigue principios SOLID para código limpio
✅ Documenta decisiones arquitectónicas

**Contacto**: Felipe Videla
**Última Actualización**: 2025-01-23
