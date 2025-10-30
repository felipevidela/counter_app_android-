# Librerías y APIs Externas - Counter App

## 📋 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Librerías de UI y Frontend](#librerías-de-ui-y-frontend)
3. [Persistencia de Datos](#persistencia-de-datos)
4. [Navegación](#navegación)
5. [Visualización de Datos](#visualización-de-datos)
6. [Utilidades](#utilidades)
7. [Testing](#testing)
8. [Plugins y Herramientas](#plugins-y-herramientas)
9. [Diagrama de Dependencias](#diagrama-de-dependencias)

---

## Resumen Ejecutivo

La aplicación Counter App utiliza **11 librerías principales** de producción, todas de fuentes oficiales y confiables.

### Tabla Resumen

| Nombre | Versión | Categoría | Proveedor | Licencia |
|--------|---------|-----------|-----------|----------|
| Jetpack Compose | 2024.09.00 | UI Framework | Google | Apache 2.0 |
| Material Design 3 | 2024.09.00 | UI Components | Google | Apache 2.0 |
| Room Database | 2.8.3 | Persistencia | Google | Apache 2.0 |
| Navigation Compose | 2.7.7 | Navegación | Google | Apache 2.0 |
| Lifecycle Runtime KTX | 2.6.1 | Architecture | Google | Apache 2.0 |
| Core KTX | 1.10.1 | Kotlin Extensions | Google | Apache 2.0 |
| Material Icons Extended | 1.6.7 | Iconografía | Google | Apache 2.0 |
| Vico Charts | 1.13.1 | Gráficos | Patrykandpatrick | Apache 2.0 |
| Gson | 2.10.1 | JSON Parsing | Google | Apache 2.0 |
| Activity Compose | 1.8.0 | Integration | Google | Apache 2.0 |
| KSP | 2.0.21-1.0.28 | Code Generation | Google | Apache 2.0 |

---

## Librerías de UI y Frontend

### 1. Jetpack Compose

**Información General:**
- **Nombre Completo**: Android Jetpack Compose
- **Versión**: BOM 2024.09.00 (Bill of Materials)
- **Grupo**: `androidx.compose`
- **Licencia**: Apache License 2.0

**Función en la App:**
Jetpack Compose es el framework UI declarativo moderno de Android que reemplaza el sistema tradicional de XML. Toda la interfaz de usuario de Counter App está construida con Compose.

**Componentes Utilizados:**
```kotlin
// UI Foundation
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.ui.graphics)
implementation(libs.androidx.compose.ui.tooling.preview)
```

**Uso en el Proyecto:**
- Todas las pantallas (Dashboard, DeviceDetail, Reports, Settings)
- Componentes personalizados (DeviceCard, SensorStatusChip)
- Layouts reactivos y animaciones
- Estados con `remember` y `mutableStateOf`

**Fuente Oficial:**
- Website: https://developer.android.com/jetpack/compose
- GitHub: https://github.com/androidx/androidx
- Documentación: https://developer.android.com/jetpack/compose/documentation

**Por qué se eligió:**
- ✅ Recomendación oficial de Google para nuevos proyectos
- ✅ UI declarativa más intuitiva que XML
- ✅ Menos código boilerplate
- ✅ Actualizaciones reactivas automáticas
- ✅ Mejor rendimiento y menos bugs

---

### 2. Material Design 3

**Información General:**
- **Nombre Completo**: Compose Material Design 3
- **Versión**: 2024.09.00 (via BOM)
- **Grupo**: `androidx.compose.material3`
- **Licencia**: Apache License 2.0

**Función en la App:**
Biblioteca de componentes que implementa Material Design 3 (Material You), el sistema de diseño más reciente de Google con soporte para temas dinámicos y mayor personalización.

**Componentes Utilizados:**
```kotlin
implementation(libs.androidx.compose.material3)
```

**Elementos de Material 3 en la App:**
- **Cards**: `ElevatedCard`, `Card`, `OutlinedCard`
- **Buttons**: `Button`, `FilledTonalButton`, `TextButton`, `IconButton`
- **Inputs**: `OutlinedTextField`, `Switch`, `Slider`
- **Navigation**: `NavigationBar`, `NavigationBarItem`, `TopAppBar`
- **Chips**: `AssistChip` para estados
- **Progress**: `LinearProgressIndicator` para aforo
- **Dialogs**: `AlertDialog`
- **FAB**: `FloatingActionButton`

**Fuente Oficial:**
- Website: https://m3.material.io/
- Compose M3: https://developer.android.com/jetpack/compose/designsystems/material3
- Guidelines: https://m3.material.io/components

**Por qué se eligió:**
- ✅ Diseño moderno y profesional
- ✅ Componentes pre-construidos y testeados
- ✅ Accesibilidad incorporada
- ✅ Temas dinámicos basados en wallpaper (Android 12+)
- ✅ Consistencia con otras apps Android

---

### 3. Material Icons Extended

**Información General:**
- **Nombre Completo**: Compose Material Icons Extended
- **Versión**: 1.6.7
- **Grupo**: `androidx.compose.material`
- **Licencia**: Apache License 2.0

**Función en la App:**
Colección extendida de +2000 iconos de Material Design vectoriales para Compose.

**Dependencia:**
```kotlin
implementation("androidx.compose.material:material-icons-extended:1.6.7")
```

**Iconos Utilizados en la App:**
- `Icons.Filled.Sensors` - Ícono principal de dispositivos
- `Icons.Filled.Login` - Sensor de entrada
- `Icons.Filled.Logout` - Sensor de salida
- `Icons.Filled.Dashboard` - Tab de dashboard
- `Icons.Filled.BarChart` - Tab de reportes
- `Icons.Filled.Settings` - Tab de configuración
- `Icons.Filled.LocationOn` - Ubicación de dispositivos
- `Icons.Filled.Warning` - Alertas de capacidad
- `Icons.Filled.Delete` - Eliminar dispositivos
- `Icons.Filled.Add` - Agregar dispositivo
- `Icons.Filled.ArrowBack` - Navegación
- `Icons.Filled.Visibility/VisibilityOff` - Mostrar/ocultar contraseña
- `Icons.Filled.ExitToApp` - Cerrar sesión

**Fuente Oficial:**
- Icons Gallery: https://fonts.google.com/icons
- Compose Usage: https://developer.android.com/jetpack/compose/resources/icons

**Por qué se eligió:**
- ✅ Iconos vectoriales (escalables sin pérdida de calidad)
- ✅ Integración nativa con Compose
- ✅ Consistencia visual con Material Design
- ✅ Sin necesidad de assets adicionales
- ✅ Peso mínimo en APK

---

## Persistencia de Datos

### 4. Room Database

**Información General:**
- **Nombre Completo**: Android Room Persistence Library
- **Versión**: 2.8.3
- **Grupo**: `androidx.room`
- **Licencia**: Apache License 2.0

**Función en la App:**
Biblioteca de persistencia que proporciona una capa de abstracción sobre SQLite, permitiendo un acceso fluido a la base de datos mientras aprovecha la potencia completa de SQL.

**Dependencias:**
```kotlin
implementation(libs.androidx.room.runtime)  // Runtime
implementation(libs.androidx.room.ktx)      // Kotlin Extensions + Coroutines
ksp(libs.androidx.room.compiler)            // Annotation Processor (KSP)
```

**Uso en el Proyecto:**
- **Entidades**: `User`, `Device`, `SensorReading`
- **DAOs**: `UserDao`, `DeviceDao`, `SensorReadingDao`
- **Database**: `AppDatabase` (singleton)
- **Repositories**: Capa de abstracción sobre DAOs
- **Queries Reactivas**: Flow para actualizaciones en tiempo real
- **Foreign Keys**: Relación Device → SensorReading con CASCADE
- **Type Converters**: Para conversión de tipos (Date ↔ Long)

**Características Utilizadas:**
```kotlin
@Entity(tableName = "devices")
@PrimaryKey(autoGenerate = true)
@ForeignKey(onDelete = ForeignKey.CASCADE)
@Index(value = ["deviceId"])
@Query("SELECT * FROM devices WHERE ...")
suspend fun insertDevice(...): Long
fun getAllDevices(): Flow<List<Device>>
```

**Fuente Oficial:**
- Website: https://developer.android.com/training/data-storage/room
- GitHub: https://github.com/androidx/androidx/tree/androidx-main/room
- Codelab: https://developer.android.com/codelabs/android-room-with-a-view-kotlin

**Por qué se eligió:**
- ✅ Recomendación oficial de Google para SQLite
- ✅ Compile-time verification de queries SQL
- ✅ Integración perfecta con Coroutines y Flow
- ✅ Migrations automáticas
- ✅ Menos boilerplate que SQLite directo
- ✅ Type-safe
- ✅ Soporte para relaciones complejas

---

## Navegación

### 5. Navigation Compose

**Información General:**
- **Nombre Completo**: Android Navigation Component for Compose
- **Versión**: 2.7.7
- **Grupo**: `androidx.navigation`
- **Licencia**: Apache License 2.0

**Función en la App:**
Componente de navegación que gestiona la navegación entre pantallas en aplicaciones Jetpack Compose con soporte para deep links, transiciones y back stack management.

**Dependencia:**
```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
```

**Uso en el Proyecto:**
- **AppNavigation.kt**: Navegación principal (Login → Main)
- **NewMainScreen.kt**: Navegación interna con bottom nav
- **Rutas definidas**:
  ```
  "login" → LoginScreen
  "register" → RegistrationScreen
  "main" → NewMainScreen
    ├─ "dashboard" → DashboardScreen
    ├─ "reports" → ReportsScreen
    ├─ "settings" → SettingsScreen
    ├─ "device_detail/{deviceId}" → DeviceDetailScreen
    └─ "device_registration" → DeviceRegistrationScreen
  ```

**Características Utilizadas:**
```kotlin
val navController = rememberNavController()
NavHost(navController = navController, startDestination = "login") {
    composable("main") { MainScreen(...) }
    composable("device_detail/{deviceId}") { backStackEntry ->
        val deviceId = backStackEntry.arguments?.getString("deviceId")
        DeviceDetailScreen(deviceId = deviceId)
    }
}
navController.navigate("main")
navController.popBackStack()
```

**Fuente Oficial:**
- Website: https://developer.android.com/jetpack/compose/navigation
- Guide: https://developer.android.com/guide/navigation
- GitHub: https://github.com/androidx/androidx/tree/androidx-main/navigation

**Por qué se eligió:**
- ✅ Solución oficial de Google
- ✅ Type-safe navigation
- ✅ Manejo automático del back stack
- ✅ Soporte para argumentos entre pantallas
- ✅ Integración con bottom navigation
- ✅ Deep linking support

---

## Visualización de Datos

### 6. Vico Charts

**Información General:**
- **Nombre Completo**: Vico - Compose Charts Library
- **Versión**: 1.13.1
- **Autor**: Patryk Goworowski & Patrick Michalik
- **Licencia**: Apache License 2.0

**Función en la App:**
Biblioteca moderna de gráficos para Jetpack Compose que permite crear visualizaciones de datos interactivas y animadas con soporte nativo para Material Design 3.

**Dependencias:**
```kotlin
implementation("com.patrykandpatrick.vico:compose:1.13.1")      // Core Compose
implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")   // Material 3 theming
implementation("com.patrykandpatrick.vico:core:1.13.1")         // Core logic
```

**Uso en el Proyecto:**
- **ReportsScreen.kt**: Gráficos de línea temporal
- **Tipos de gráficos**: Line charts para entrada/salida de personas
- **Características**:
  - Gráficos de línea con múltiples series
  - Ejes personalizados (X: tiempo, Y: cantidad)
  - Colores diferenciados por sensor (verde/rojo)
  - Animaciones suaves
  - Integración con Material 3

**Código de Ejemplo:**
```kotlin
CartesianChartHost(
    chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lines = listOf(
                rememberLineSpec(lineColor = Color(0xFF4CAF50)), // Entradas
                rememberLineSpec(lineColor = Color(0xFFF44336))  // Salidas
            )
        ),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis()
    ),
    modelProducer = modelProducer
)
```

**Fuente Oficial:**
- GitHub: https://github.com/patrykandpatrick/vico
- Documentación: https://patrykandpatrick.com/vico/wiki/
- Maven Central: https://central.sonatype.com/artifact/com.patrykandpatrick.vico/core

**Por qué se eligió:**
- ✅ Diseñado específicamente para Jetpack Compose
- ✅ Soporte nativo para Material Design 3
- ✅ Performance optimizado
- ✅ API moderna y declarativa
- ✅ Amplia variedad de tipos de gráficos
- ✅ Customizable y extensible
- ✅ Animaciones suaves out-of-the-box
- ✅ Activamente mantenida

**Alternativas consideradas:**
- MPAndroidChart: Diseñado para Views (XML), no Compose
- Charts by Philipp Jahoda: Legacy, menos mantenimiento

---

## Utilidades

### 7. Core KTX

**Información General:**
- **Nombre Completo**: Android Core KTX
- **Versión**: 1.10.1
- **Grupo**: `androidx.core`
- **Licencia**: Apache License 2.0

**Función en la App:**
Extensiones de Kotlin que proporcionan APIs concisas e idiomáticas para las APIs comunes de Android Framework.

**Dependencia:**
```kotlin
implementation(libs.androidx.core.ktx)
```

**Uso en el Proyecto:**
- Extensiones para Context
- Helpers para Collections
- Utilities para recursos
- Simplificación de APIs de Android

**Fuente Oficial:**
- Website: https://developer.android.com/kotlin/ktx
- GitHub: https://github.com/androidx/androidx/tree/androidx-main/core/core-ktx

**Por qué se eligió:**
- ✅ Estándar en proyectos Kotlin Android
- ✅ Código más conciso y legible
- ✅ Menos boilerplate

---

### 8. Lifecycle Runtime KTX

**Información General:**
- **Nombre Completo**: Android Lifecycle Runtime KTX
- **Versión**: 2.6.1
- **Grupo**: `androidx.lifecycle`
- **Licencia**: Apache License 2.0

**Función en la App:**
Extensiones de Kotlin para los componentes de Lifecycle que facilitan el manejo del ciclo de vida de Android con Coroutines.

**Dependencia:**
```kotlin
implementation(libs.androidx.lifecycle.runtime.ktx)
```

**Uso en el Proyecto:**
- ViewModels lifecycle-aware
- `viewModelScope` para coroutines
- `collectAsState()` para Flows en Compose
- Manejo seguro del ciclo de vida

**Fuente Oficial:**
- Website: https://developer.android.com/topic/libraries/architecture/lifecycle
- GitHub: https://github.com/androidx/androidx/tree/androidx-main/lifecycle

**Por qué se eligió:**
- ✅ Manejo automático de lifecycle
- ✅ Prevención de memory leaks
- ✅ Integración con Coroutines

---

### 9. Activity Compose

**Información General:**
- **Nombre Completo**: Activity Compose Integration
- **Versión**: 1.8.0
- **Grupo**: `androidx.activity`
- **Licencia**: Apache License 2.0

**Función en la App:**
Proporciona la integración entre Activities de Android y Jetpack Compose.

**Dependencia:**
```kotlin
implementation(libs.androidx.activity.compose)
```

**Uso en el Proyecto:**
```kotlin
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
```

**Fuente Oficial:**
- Website: https://developer.android.com/jetpack/androidx/releases/activity

**Por qué se eligió:**
- ✅ Necesario para usar Compose en Activities
- ✅ Manejo del back press
- ✅ Integración con ViewModels

---

### 10. Gson

**Información General:**
- **Nombre Completo**: Gson - JSON Parser
- **Versión**: 2.10.1
- **Proveedor**: Google
- **Licencia**: Apache License 2.0

**Función en la App:**
Biblioteca de Google para serialización/deserialización de objetos Java/Kotlin a JSON y viceversa.

**Dependencia:**
```kotlin
implementation("com.google.code.gson:gson:2.10.1")
```

**Uso en el Proyecto:**
- Serialización de datos para logging
- Conversión de objetos complejos
- Debugging y desarrollo

**Fuente Oficial:**
- GitHub: https://github.com/google/gson
- User Guide: https://github.com/google/gson/blob/master/UserGuide.md
- Maven: https://mvnrepository.com/artifact/com.google.code.gson/gson

**Por qué se eligió:**
- ✅ Ampliamente utilizada en Android
- ✅ Confiable y estable
- ✅ Buen rendimiento
- ✅ Mantenida por Google

---

## Testing

### 11. JUnit

**Información General:**
- **Nombre Completo**: JUnit Testing Framework
- **Versión**: 4.13.2
- **Licencia**: Eclipse Public License 1.0

**Función:**
Framework estándar para unit testing en Java/Kotlin.

**Dependencia:**
```kotlin
testImplementation(libs.junit)
```

**Fuente Oficial:**
- Website: https://junit.org/junit4/

---

### 12. AndroidX Test

**Información General:**
- **AndroidX JUnit**: v1.1.5
- **Espresso Core**: v3.5.1

**Función:**
Frameworks de Google para testing de UI en Android.

**Dependencias:**
```kotlin
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
```

**Fuente Oficial:**
- Website: https://developer.android.com/training/testing

---

## Plugins y Herramientas

### 13. KSP (Kotlin Symbol Processing)

**Información General:**
- **Nombre Completo**: Kotlin Symbol Processing API
- **Versión**: 2.0.21-1.0.28
- **Licencia**: Apache License 2.0

**Función en la App:**
API de procesamiento de anotaciones para Kotlin, más rápida que KAPT. Utilizada por Room para generar código.

**Plugin:**
```kotlin
id("com.google.devtools.ksp") version "2.0.21-1.0.28"
```

**Uso:**
- Generación de DAOs de Room
- Procesamiento de `@Entity`, `@Dao`, `@Database`
- 2x más rápido que KAPT

**Fuente Oficial:**
- GitHub: https://github.com/google/ksp
- Docs: https://kotlinlang.org/docs/ksp-overview.html

**Por qué se eligió:**
- ✅ Recomendación de Google para Room
- ✅ Build times más rápidos que KAPT
- ✅ Mejor integración con Kotlin

---

## Diagrama de Dependencias

```
Counter App
├── UI Layer
│   ├── Jetpack Compose (2024.09.00)
│   ├── Material Design 3 (2024.09.00)
│   ├── Material Icons Extended (1.6.7)
│   └── Activity Compose (1.8.0)
│
├── Navigation
│   └── Navigation Compose (2.7.7)
│
├── Architecture & Lifecycle
│   ├── Lifecycle Runtime KTX (2.6.1)
│   └── Core KTX (1.10.1)
│
├── Data & Persistence
│   ├── Room Database (2.8.3)
│   │   ├── room-runtime
│   │   ├── room-ktx
│   │   └── room-compiler (KSP)
│   └── Gson (2.10.1)
│
├── Data Visualization
│   └── Vico Charts (1.13.1)
│       ├── vico:compose
│       ├── vico:compose-m3
│       └── vico:core
│
└── Testing
    ├── JUnit (4.13.2)
    ├── AndroidX JUnit (1.1.5)
    └── Espresso Core (3.5.1)
```

---

## Resumen para Presentación

### Librerías Principales (Top 5)

1. **Jetpack Compose** - Framework UI moderno de Google
2. **Room Database** - Persistencia local SQLite
3. **Navigation Compose** - Sistema de navegación
4. **Material Design 3** - Componentes UI y diseño
5. **Vico Charts** - Visualización de datos en gráficos

### Proveedor por Categoría

- **Google/Android**: 9 librerías (82%)
  - Compose, Room, Navigation, Material 3, Core KTX, Lifecycle, Activity, Icons

- **Terceros (Open Source)**: 2 librerías (18%)
  - Vico Charts (Patrykandpatrick)
  - Gson (Google pero open source independiente)

### Licencias

- **Todas las librerías**: Apache License 2.0
- **100% Open Source**
- **Sin restricciones comerciales**

---

## Justificación Técnica

### ¿Por qué estas librerías?

1. **Oficiales de Google**: La mayoría son parte de Android Jetpack
2. **Modernas**: Todas soportan las últimas versiones de Android
3. **Mantenidas activamente**: Updates regulares y soporte
4. **Amplia adopción**: Usadas por millones de apps
5. **Bien documentadas**: Documentación oficial exhaustiva
6. **Performance**: Optimizadas para Android
7. **Compatibilidad**: Funcionan bien juntas (ecosystem)

---

## Enlaces de Referencia

### Documentación Oficial
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Architecture](https://developer.android.com/topic/architecture)
- [Material Design 3](https://m3.material.io/)

### Repositorios GitHub
- [AndroidX](https://github.com/androidx/androidx)
- [Vico Charts](https://github.com/patrykandpatrick/vico)
- [Gson](https://github.com/google/gson)

### Versiones y Releases
- [Maven Central](https://central.sonatype.com/)
- [Google Maven Repository](https://maven.google.com/)

---

**Documento preparado para**: Presentación del proyecto Counter App
**Fecha**: Enero 2025
**Versión**: 1.0
