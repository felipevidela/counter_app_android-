# Counter APP - IoT People Counter Application

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)

Una aplicación móvil Android para monitoreo en tiempo real de aforo (ocupación) mediante dispositivos IoT simulados, diseñada para gestión de espacios comerciales con enfoque en análisis de datos y visualización profesional.

## 📋 Descripción del Proyecto

Counter APP es una solución IoT completa que permite:
- **Monitoreo en tiempo real** del flujo de personas (entradas/salidas)
- **Gestión de múltiples dispositivos** de conteo simulados
- **Registro de eventos** con actualización automática en vivo
- **Reportes y gráficos profesionales** con YCharts
- **Sistema de alertas configurables** con notificaciones personalizadas
- **Exportación de datos** a formatos PDF y CSV
- **Notificaciones de desconexión** de dispositivos
- **Autenticación segura** de usuarios

### 🎯 Problema que Resuelve

La aplicación aborda la necesidad de monitorear el aforo en espacios comerciales (tiendas, malls, eventos) de manera:
- **Automatizada**: Sensores simulados que generan eventos realistas
- **En tiempo real**: Actualizaciones instantáneas mediante Flow reactivos
- **Analítica**: Estadísticas y gráficos de ocupación, tiempo promedio de visita, picos de aforo
- **Escalable**: Soporte para múltiples dispositivos simultáneos
- **Segura**: Autenticación de usuarios y notificaciones de eventos críticos

## ✨ Nuevas Funcionalidades (Última Actualización)

### **1. Edición Completa de Dispositivos**
- ✅ Editar nombre, tipo, ubicación y capacidad de dispositivos existentes
- ✅ Botón de edición (✏️) en cada tarjeta del Dashboard
- ✅ Pantalla reutilizada con modo dual (crear/editar)
- ✅ Preservación de datos críticos (MAC address, estado activo, fecha de creación)
- ✅ Navegación fluida con parámetros dinámicos

**Implementación:**
```kotlin
// Navegación con parámetros
navController.navigate("device_registration/$deviceId")

// ViewModel con función de actualización
fun updateDevice(deviceId: Long, name: String, type: String,
                location: String, capacity: Int, ...)
```

### **2. Dark Mode con Toggle Manual**
- ✅ Switch en Configuración → Preferencias → "Modo Oscuro"
- ✅ Persistencia con DataStore Preferences (sobrevive reinicios)
- ✅ Cambio en tiempo real sin reiniciar app
- ✅ Material Design 3 con esquemas de color light/dark completos
- ✅ Status bar adaptativo según tema

**Tecnología:**
```kotlin
// ThemePreferences con DataStore
class ThemePreferences(context: Context) {
    val darkModeFlow: Flow<Boolean>
    suspend fun saveDarkMode(enabled: Boolean)
}

// MainActivity observa el Flow
val isDarkMode by themePreferences.darkModeFlow.collectAsState(initial = false)
Counter_APPTheme(darkTheme = isDarkMode) { ... }
```

### **3. Fixes Críticos de UI/UX**
- ✅ **Crash de Ajustes resuelto**: Corregido loop infinito en SettingsViewModel
- ✅ **Visualización de entradas/salidas**: Dashboard actualizado para usar SensorEvent en tiempo real
- ✅ **Gráfico en modo oscuro**: Colores adaptativos (números blancos en dark mode)
- ✅ **Navbar en registro**: TopAppBar con botón de retroceso al login
- ✅ **Flows reactivos**: Actualización automática de estadísticas en Dashboard

### **4. Mejoras de Arquitectura**
- ✅ **SensorEventDao**: Agregadas funciones Flow para entradas/salidas
- ✅ **DashboardViewModel**: Rediseñado para combinar Flows reactivos
- ✅ **StateFlow en SettingsViewModel**: Uso correcto de `stateIn()` en lugar de `collect()`
- ✅ **DataStore integration**: Persistencia moderna para preferencias de tema

## 🚀 Características Principales

### 1. **Sistema de Eventos Basado en SensorEvent**
Arquitectura innovadora basada en eventos individuales:
- **ENTRY**: Eventos de entrada de personas (1 persona por evento - simulación realista de Arduino)
- **EXIT**: Eventos de salida de personas (1 persona por evento)
- **DISCONNECTION**: Eventos de desconexión de dispositivos (10% probabilidad)

```kotlin
data class SensorEvent(
    val id: Long = 0,
    val deviceId: Long,
    val eventType: EventType,
    val peopleCount: Int,
    val timestamp: Long
)

enum class EventType {
    ENTRY,
    EXIT,
    DISCONNECTION
}
```

### 2. **Simulación Realista de Dispositivos IoT**
Servicio de simulación que genera eventos automáticos con patrones realistas:
- **Eventos individuales**: Simulación precisa de sensores Arduino (1 persona por evento)
- **Balance inteligente** entre entradas/salidas según ocupación actual
- **Eventos de desconexión** aleatorios (10% probabilidad)
- **Notificaciones push** cuando se detecta desconexión
- **Reset automático de IDs**: Al borrar datos, los contadores se reinician desde 1

### 3. **Gráficos Profesionales con YCharts**
Visualización de datos de aforo con:
- **Eje Y dinámico**: Se ajusta automáticamente al rango de datos
- **Tooltips interactivos**: Muestra hora y aforo exacto al tocar
- **Sombra bajo la línea**: Gradiente visual para mejor lectura
- **Actualización en tiempo real**: Flow reactivo desde Room Database

### 4. **Sistema de Notificaciones y Alertas Configurables**
Notificaciones push inteligentes para eventos críticos:
- **Alerta de Desconexión**: Notifica cuando un dispositivo pierde conexión (configurable on/off)
- **Alerta de Aforo Bajo**: Notifica cuando la ocupación cae bajo un umbral configurable (5-30%)
- **Alerta de Aforo Alto**: Notifica cuando se acerca a capacidad máxima (70-100%)
- **Alerta de Peak de Tráfico**: Detecta muchas entradas en corto tiempo (5-20 entradas en 5 min)
- **Throttling inteligente**: Evita spam de notificaciones (máx. 1 alerta/tipo cada 10 min)
- **Canales separados**: Alertas de dispositivos (alta prioridad) vs alertas de aforo (normal)
- **Permiso runtime** para Android 13+ (POST_NOTIFICATIONS)
- **Configuración total** desde Settings: todas las alertas con toggle on/off personalizable

### 5. **Exportación de Datos**
Sistema completo de exportación de historial de eventos:
- **Formato PDF**:
  - Documento profesional con encabezado, tabla y pie de página
  - Eventos codificados por color (verde/rojo/naranja)
  - Incluye fecha de exportación y nombre del dispositivo
  - Soporte multi-página para grandes volúmenes de datos
- **Formato CSV**:
  - Compatible con Excel y Google Sheets
  - Columnas: Event ID, Device, Type, People Count, Date, Time, Timestamp
  - Perfecto para análisis de datos externos
- **Guardado en Downloads**: Uso de MediaStore (sin permisos en Android 10+)
- **Selector de app**: Intent chooser para abrir archivos exportados
- **Feedback visual**: Snackbar con botón "ABRIR" para acceso rápido

### 6. **Reportes y Estadísticas**
Pantalla de reportes con:
- **Gráfico de aforo**: Visualización temporal de ocupación
- **Estadísticas clave**:
  - Total de entradas
  - Total de salidas
  - Aforo actual
  - Peak de aforo
  - Tiempo promedio de visita (calculado con área bajo la curva)
- **Filtros de tiempo**: Hoy, Últimos 7 días, Últimos 30 días
- **Selector de dispositivo**: Análisis individual por dispositivo

## 🏗️ Arquitectura

### Patrón MVVM con Flow Reactivos

```
┌─────────────────────────────────────────────────────┐
│           AppNavigation (Root)                      │
│  ┌─────────────────────────────────────────────┐   │
│  │  Room Database (Persistent Storage)         │   │
│  │    ├── DeviceDao                            │   │
│  │    ├── SensorEventDao                       │   │
│  │    └── UserDao                              │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  Repositories (Data Layer)                  │   │
│  │    ├── DeviceRepository                     │   │
│  │    ├── SensorEventRepository                │   │
│  │    └── SettingsRepository                   │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  Services (Background Processing)           │   │
│  │    ├── EventBasedSimulationService          │   │
│  │    └── NotificationHandler                  │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  ViewModels (Business Logic)                │   │
│  │    ├── DashboardViewModel                   │   │
│  │    ├── DeviceDetailViewModel                │   │
│  │    ├── DeviceRegistrationViewModel          │   │
│  │    ├── ReportsViewModel                     │   │
│  │    └── SettingsViewModel                    │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  UI Layer (Jetpack Compose)                 │   │
│  │    ├── LoginScreen                          │   │
│  │    ├── RegistrationScreen                   │   │
│  │    ├── DashboardScreen                      │   │
│  │    ├── DeviceDetailScreen                   │   │
│  │    ├── DeviceRegistrationScreen             │   │
│  │    ├── ReportsScreen                        │   │
│  │    └── SettingsScreen                       │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### Flow de Datos en Tiempo Real

```kotlin
// Room Database emite eventos automáticamente
sensorEventRepository.getEventsByDevice(deviceId, limit)
    .map { events -> transformToOccupancyData(events) }
    .collect { chartData ->
        // UI se actualiza automáticamente
    }
```

## 🛠️ Tecnologías Utilizadas

### Core
- **Kotlin** - Lenguaje de programación principal
- **Jetpack Compose** - UI moderna y declarativa
- **Material Design 3** - Sistema de diseño

### Arquitectura
- **MVVM** - Patrón de arquitectura
- **StateFlow** - Manejo de estado reactivo
- **Coroutines** - Programación asíncrona
- **Room Database** - Persistencia local con Flow reactivos
- **DataStore Preferences 1.0.0** - Almacenamiento de preferencias (dark mode)

### Visualización de Datos
- **YCharts 2.1.0** - Gráficos profesionales para Jetpack Compose
- **Eje Y dinámico** - Normalización automática de valores

### Notificaciones
- **NotificationManager** - Sistema de notificaciones de Android
- **Accompanist Permissions 0.34.0** - Manejo de permisos runtime
- **POST_NOTIFICATIONS** - Permiso para Android 13+

### Seguridad
- **SHA-256** - Hashing de contraseñas
- **Room Database** - Almacenamiento cifrado de credenciales

## 📱 Pantallas

### 1. Login/Registro
- Autenticación segura con SHA-256
- Validación de contraseñas
- Registro de nuevos usuarios

### 2. Dashboard
- Lista de dispositivos registrados
- Indicadores de estado (activo/inactivo)
- Acceso rápido a detalles de cada dispositivo
- Botón de registro de nuevos dispositivos

### 3. Device Detail
- **Métricas en tiempo real**:
  - Total de personas que han entrado
  - Total de personas que han salido
  - Aforo actual (ocupación)
- **Registro de eventos expandibles**:
  - Eventos de entrada (verde)
  - Eventos de salida (rojo)
  - Eventos de desconexión (naranja con ícono de advertencia)
- **Detalles por evento**:
  - Timestamp preciso
  - Número de personas
  - Tipo de evento
- **Exportación de datos**:
  - Botón "Exportar Historial"
  - Selector de formato (PDF/CSV)
  - Descarga automática a carpeta Downloads

### 4. Device Registration
- Registro de nuevos dispositivos
- Configuración de nombre y capacidad
- Activación automática al registrar

### 5. Reports & Charts
- **Gráfico de aforo** (OccupancyChart):
  - Línea temporal de ocupación
  - Eje Y dinámico que se ajusta a los datos
  - Tooltips interactivos con hora y aforo
  - Sombra bajo la línea para mejor visualización
- **Estadísticas**:
  - Total de entradas/salidas
  - Aforo actual y pico
  - Tiempo promedio de visita (en minutos)
- **Filtros**:
  - Selector de dispositivo
  - Rangos de tiempo: Hoy, Últimos 7 días, Últimos 30 días

### 6. Settings
- **Preferencias**:
  - Toggle de notificaciones (solicitud automática de permiso en Android 13+)
  - Slider de intervalo de simulación (1-30 segundos)
- **Alertas Configurables**:
  - **Alerta de Desconexión**:
    - Toggle on/off
    - Notifica cuando un dispositivo pierde conexión
  - **Alerta de Aforo Bajo**:
    - Toggle on/off
    - Slider de umbral (5-30% de capacidad)
  - **Alerta de Aforo Alto**:
    - Toggle on/off
    - Slider de umbral (70-100% de capacidad)
  - **Alerta de Peak de Tráfico**:
    - Toggle on/off
    - Slider de umbral (5-20 entradas en 5 minutos)
- **Gestión de datos**:
  - Borrar todas las lecturas (con reset de IDs)
- **Información de la app**
- **Cerrar sesión**

## 🔔 Sistema de Notificaciones

### Flujo de Permiso en Android 13+

```kotlin
// 1. Usuario activa notificaciones en Settings
viewModel.toggleNotifications(true)

// 2. App verifica versión de Android
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // 3. Verifica estado del permiso
    when {
        permissionState.status.isGranted -> {
            // Permiso concedido, activar notificaciones
        }
        permissionState.status.shouldShowRationale -> {
            // Mostrar diálogo explicativo
        }
        else -> {
            // Solicitar permiso directamente
            permissionState.launchPermissionRequest()
        }
    }
}

// 4. Observar cuando se concede el permiso
LaunchedEffect(permissionState?.status) {
    if (permissionState?.status?.isGranted == true) {
        viewModel.toggleNotifications(true)
    }
}
```

### NotificationHandler

Sistema de notificaciones con 2 canales separados:

```kotlin
// Canal de alertas de dispositivos (alta prioridad)
private const val CHANNEL_ID_DEVICES = "device_alerts"

// Canal de alertas de aforo (prioridad normal)
private const val CHANNEL_ID_OCCUPANCY = "occupancy_alerts"

// Notificación de desconexión
fun showDisconnectionNotification(deviceName: String) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEVICES)
        .setContentTitle("⚠️ Dispositivo desconectado")
        .setContentText(deviceName)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(longArrayOf(0, 250, 250, 250))
        .build()

    notificationManager.notify(NOTIFICATION_ID_DISCONNECTION, notification)
}

// Alerta de aforo bajo
fun showLowOccupancyAlert(deviceName: String, currentOccupancy: Int, threshold: Int) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID_OCCUPANCY)
        .setContentTitle("📉 Aforo Bajo")
        .setContentText("El aforo en '$deviceName' es bajo: $currentOccupancy personas")
        .build()

    notificationManager.notify(NOTIFICATION_ID_LOW_OCCUPANCY, notification)
}

// Alerta de aforo alto
fun showHighOccupancyAlert(deviceName: String, currentOccupancy: Int, capacity: Int) {
    // Similar con emoji 📈
}

// Alerta de pico de tráfico
fun showTrafficPeakAlert(deviceName: String, entriesCount: Int) {
    // Similar con emoji 🚶
}
```

## 🧪 Cálculos de Estadísticas

### Tiempo Promedio de Visita

El tiempo promedio de visita se calcula usando el **área bajo la curva** de ocupación:

```kotlin
// Calcular área bajo la curva (personas-minuto acumulados)
for (i in 0 until points.size - 1) {
    val currentOccupancy = points[i].value
    val nextTimestamp = points[i + 1].timestamp
    val currentTimestamp = points[i].timestamp
    val durationMinutes = (nextTimestamp - currentTimestamp) / (1000.0 * 60.0)

    // Área del trapecio: aforo actual × duración
    totalPersonMinutes += currentOccupancy * durationMinutes
}

// Promedio: personas-minuto totales / número de salidas
avgDwellMinutes = (totalPersonMinutes / totalExits).toInt()
```

### Normalización del Eje Y

Para el gráfico con eje Y dinámico:

```kotlin
// 1. Calcular valor máximo con 20% de padding
val maxY = (occupancyPoints.maxOfOrNull { it.value } ?: 0f) * 1.2f

// 2. Normalizar puntos al rango del eje (0-5)
val normalizedY = (point.value / maxY) * yAxisSteps

// 3. Escalar etiquetas del eje Y
val scaledValue = (value * maxY / yAxisSteps).toInt()
```

## 🚦 Cómo Usar

### Requisitos
- Android 8.0 (API 26) o superior
- Android 13+ para notificaciones (opcional)

### Instalación
1. Clona el repositorio
```bash
git clone https://github.com/felipevidela/counter_app_android-.git
```

2. Abre el proyecto en Android Studio
3. Sincroniza Gradle
4. Ejecuta en dispositivo o emulador

### Uso
1. **Registrarse**: Crea una cuenta con usuario y contraseña segura
2. **Login**: Ingresa con tus credenciales
3. **Registrar Dispositivo**: Agrega un nuevo dispositivo de conteo
4. **Dashboard**: Ve tus dispositivos y su estado
5. **Device Detail**: Monitorea en tiempo real los eventos
6. **Reports**: Analiza estadísticas y gráficos de aforo
7. **Settings**:
   - Activa notificaciones (se pedirá permiso en Android 13+)
   - Ajusta intervalo de simulación
   - Borra datos si es necesario

## 📈 Características Técnicas Avanzadas

### 1. Flow Reactivos desde Room
```kotlin
// Los datos se actualizan automáticamente sin polling
@Query("SELECT * FROM sensor_events WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
fun getEventsByDevice(deviceId: Long, limit: Int): Flow<List<SensorEvent>>
```

### 2. Simulación Inteligente de Eventos
```kotlin
// Patrón realista de entradas/salidas
private fun decideEventType(currentOccupancy: Int, capacity: Int): EventType {
    val occupancyPercentage = currentOccupancy.toFloat() / capacity.toFloat()

    return when {
        occupancyPercentage > 0.9f -> // Más salidas si está lleno
            if (Random.nextInt(100) < 70) EventType.EXIT else EventType.ENTRY
        occupancyPercentage < 0.2f -> // Más entradas si está vacío
            if (Random.nextInt(100) < 80) EventType.ENTRY else EventType.EXIT
        else -> // Normal: 60% entradas, 40% salidas
            if (Random.nextInt(100) < 60) EventType.ENTRY else EventType.EXIT
    }
}
```

### 3. Gráfico con Eje Y Dinámico
```kotlin
// Se ajusta automáticamente a cualquier rango de datos
val maxY = data.maxValue.toFloat().coerceAtLeast(5f)
val normalizedY = (point.value / maxY) * yAxisSteps
```

## 📊 Estructura del Proyecto

```
app/src/main/java/com/example/counter_app/
├── auth/
│   └── LoginViewModel.kt
├── data/
│   ├── AlertSettings.kt              # Entidad para configuración de alertas
│   ├── AlertSettingsDao.kt           # DAO de alertas
│   ├── AppDatabase.kt                # Base de datos (v6)
│   ├── Converters.kt                 # Type converters para Room
│   ├── Device.kt
│   ├── DeviceDao.kt
│   ├── DeviceRepository.kt
│   ├── SensorEvent.kt
│   ├── SensorEventDao.kt
│   ├── SensorEventRepository.kt
│   ├── SettingsRepository.kt         # Repositorio de configuración
│   ├── ThemePreferences.kt           # ✨ DataStore para dark mode
│   ├── User.kt
│   └── UserDao.kt
├── domain/
│   ├── ChartPoint.kt
│   ├── OccupancyChartData.kt
│   └── ReportStats.kt
├── navigation/
│   └── AppNavigation.kt
├── service/
│   ├── DeviceSimulationService.kt
│   ├── EventBasedSimulationService.kt # Con lógica de alertas
│   └── NotificationHandler.kt         # 4 tipos de notificaciones
├── ui/
│   ├── DashboardScreen.kt
│   ├── DeviceDetailScreen.kt          # Con exportación
│   ├── DeviceRegistrationScreen.kt
│   ├── LoginScreen.kt
│   ├── RegistrationScreen.kt
│   ├── ReportsScreen.kt
│   ├── SettingsScreen.kt              # Con alertas configurables
│   └── components/
│       └── OccupancyChart.kt
├── util/
│   ├── CsvExporter.kt                 # Exportación a CSV
│   ├── ExportManager.kt               # Gestor de archivos
│   ├── PasswordUtil.kt
│   └── PdfExporter.kt                 # Exportación a PDF
└── viewmodel/
    ├── DashboardViewModel.kt
    ├── DeviceDetailViewModel.kt
    ├── DeviceRegistrationViewModel.kt
    ├── ReportsViewModel.kt
    └── SettingsViewModel.kt           # Con gestión de alertas
```

## 🔔 Sistema de Alertas Configurables

### Arquitectura de Alertas

El sistema de alertas configurables permite al usuario personalizar completamente las notificaciones que desea recibir:

```kotlin
// Entidad de configuración persistida en Room Database
@Entity(tableName = "alert_settings")
data class AlertSettings(
    @PrimaryKey val id: Long = 1,
    val disconnectionAlertEnabled: Boolean = false,  // Alerta de desconexión
    val lowOccupancyEnabled: Boolean = false,
    val lowOccupancyThreshold: Int = 5,       // 5% por defecto
    val highOccupancyEnabled: Boolean = false,
    val highOccupancyThreshold: Int = 90,     // 90% por defecto
    val trafficPeakEnabled: Boolean = false,
    val trafficPeakThreshold: Int = 10        // 10 entradas en 5 min
)
```

### Lógica de Detección

EventBasedSimulationService verifica las condiciones después de cada evento:

```kotlin
private suspend fun checkAlertConditions(device: Device, event: SensorEvent) {
    val alertSettings = settingsRepository.getAlertSettings().first()
    val currentOccupancy = sensorEventRepository.getCurrentOccupancy(device.id)

    // 1. Verificar Alerta de Aforo Bajo
    if (alertSettings.lowOccupancyEnabled) {
        val occupancyPercentage = (currentOccupancy.toFloat() / device.capacity) * 100

        if (occupancyPercentage < alertSettings.lowOccupancyThreshold) {
            if (shouldSendAlert(device.id, "low_occupancy")) {
                notificationHandler.showLowOccupancyAlert(...)
                updateLastAlertTime(device.id, "low_occupancy")
            }
        }
    }

    // 2. Verificar Alerta de Aforo Alto
    // 3. Verificar Alerta de Peak de Tráfico
}
```

**Alerta de Desconexión:**

La alerta de desconexión se verifica cuando se genera un evento de desconexión (10% probabilidad):

```kotlin
private suspend fun generateEventForDevice(device: Device) {
    // 10% de probabilidad de evento de desconexión
    if (Random.nextInt(100) < 10) {
        val disconnectionEvent = SensorEvent(...)
        sensorEventRepository.insertEvent(disconnectionEvent)

        // Verificar si la alerta de desconexión está habilitada
        val alertSettings = settingsRepository.getAlertSettings().first()
        if (alertSettings.disconnectionAlertEnabled) {
            if (shouldSendAlert(device.id, "disconnection")) {
                notificationHandler.showDisconnectionNotification(device.name)
                updateLastAlertTime(device.id, "disconnection")
            }
        }
        return
    }
    // ... resto de la generación de eventos
}
```

### Throttling de Notificaciones

Para evitar spam de notificaciones:

```kotlin
// Mapa de últimas alertas por dispositivo y tipo
private val lastAlertTimeMap = mutableMapOf<String, Long>()

// Intervalo mínimo entre alertas del mismo tipo: 10 minutos
private const val ALERT_THROTTLE_INTERVAL = 10 * 60 * 1000L

private fun shouldSendAlert(deviceId: Long, alertType: String): Boolean {
    val key = "${deviceId}_${alertType}"
    val lastAlertTime = lastAlertTimeMap[key] ?: 0L
    val currentTime = System.currentTimeMillis()
    return (currentTime - lastAlertTime) >= ALERT_THROTTLE_INTERVAL
}
```

### Tracking de Peak de Tráfico

Ventana deslizante de 5 minutos:

```kotlin
// Mapa de entradas recientes por dispositivo
private val recentEntriesMap = mutableMapOf<Long, MutableList<Long>>()

// Agregar entrada al tracker
if (event.eventType == EventType.ENTRY) {
    val recentEntries = recentEntriesMap.getOrPut(device.id) { mutableListOf() }
    recentEntries.add(event.timestamp)

    // Limpiar entradas fuera de la ventana de 5 minutos
    val windowStart = event.timestamp - TRAFFIC_PEAK_WINDOW
    recentEntries.removeAll { it < windowStart }

    // Verificar si supera el umbral
    if (recentEntries.size >= alertSettings.trafficPeakThreshold) {
        // Enviar alerta
    }
}
```

### Canales de Notificación

Dos canales separados para mejor experiencia de usuario:

| Canal | ID | Prioridad | Vibración | Uso |
|-------|----|-----------|-----------|----|
| Alertas de Dispositivos | `device_alerts` | HIGH | ✅ | Desconexiones |
| Alertas de Aforo | `occupancy_alerts` | DEFAULT | ❌ | Aforo bajo/alto, tráfico |

## 📤 Sistema de Exportación

### Exportación a PDF

Utiliza `android.graphics.pdf.PdfDocument` (sin dependencias externas):

```kotlin
class PdfExporter(private val context: Context) {
    fun exportEvents(events: List<SensorEvent>, deviceName: String): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        val page = pdfDocument.startPage(pageInfo)

        // Dibujar encabezado
        canvas.drawText("Reporte de Eventos", x, y, titlePaint)
        canvas.drawText("Dispositivo: $deviceName", x, y, subtitlePaint)

        // Dibujar tabla de eventos con colores
        events.forEach { event ->
            val paint = when(event.eventType) {
                EventType.ENTRY -> greenPaint
                EventType.EXIT -> redPaint
                EventType.DISCONNECTION -> orangePaint
            }
            // Dibujar fila...
        }

        pdfDocument.finishPage(page)

        // Guardar usando MediaStore
        return ExportManager.saveFile(pdfDocument, "events_$deviceName.pdf", "application/pdf")
    }
}
```

### Exportación a CSV

Formato compatible con Excel y Google Sheets:

```kotlin
class CsvExporter {
    fun exportEvents(events: List<SensorEvent>, deviceName: String): Uri? {
        val csvContent = buildString {
            // Header
            appendLine("Event ID,Device Name,Event Type,People Count,Date,Time,Timestamp")

            // Datos
            events.forEach { event ->
                appendLine("${event.id},$deviceName,${event.eventType}," +
                          "${event.peopleCount},$date,$time,${event.timestamp}")
            }
        }

        return ExportManager.saveFile(csvContent, "events_$deviceName.csv", "text/csv")
    }
}
```

### Guardado con MediaStore (Android 10+)

Sin permisos de storage necesarios:

```kotlin
object ExportManager {
    fun saveFile(content: Any, fileName: String, mimeType: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        )

        // Escribir contenido...
        return uri
    }
}
```

## 🔄 Roadmap

### Completado ✅
- [x] Sistema de eventos basado en SensorEvent
- [x] Gráficos profesionales con YCharts
- [x] Notificaciones de desconexión
- [x] Eje Y dinámico en gráficos
- [x] Permiso runtime para Android 13+
- [x] Estadísticas avanzadas (tiempo promedio de visita)
- [x] Exportación de reportes a PDF/CSV
- [x] Sistema de alertas configurables
- [x] Simulación realista Arduino (1 persona/evento)
- [x] Reset automático de IDs
- [x] **Edición completa de dispositivos** ✨ v1.1
- [x] **Dark mode con toggle manual** ✨ v1.1
- [x] **Persistencia con DataStore** ✨ v1.1
- [x] **Gráficos adaptativos a tema** ✨ v1.1
- [x] **Navbar en pantalla de registro** ✨ v1.1
- [x] **Flows reactivos en Dashboard** ✨ v1.1

### En Desarrollo / Futuro 🚧
- [ ] Dashboard web complementario
- [ ] Integración con dispositivos IoT reales (Bluetooth)
- [ ] Predicción de aforo con ML
- [ ] Análisis de patrones de comportamiento
- [ ] Sincronización cloud multi-dispositivo
- [ ] Widgets de Android
- [ ] Modo offline avanzado

## 👥 Contribución

Este es un proyecto académico, pero las sugerencias son bienvenidas:
1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es de código abierto para fines educativos.

## 📧 Contacto

Felipe Videla - [GitHub](https://github.com/felipevidela)

Project Link: [https://github.com/felipevidela/counter_app_android-](https://github.com/felipevidela/counter_app_android-)

---

**Desarrollado con Claude Code** - AI-assisted development
