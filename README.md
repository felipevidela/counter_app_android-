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
- **Notificaciones de desconexión** de dispositivos
- **Autenticación segura** de usuarios

### 🎯 Problema que Resuelve

La aplicación aborda la necesidad de monitorear el aforo en espacios comerciales (tiendas, malls, eventos) de manera:
- **Automatizada**: Sensores simulados que generan eventos realistas
- **En tiempo real**: Actualizaciones instantáneas mediante Flow reactivos
- **Analítica**: Estadísticas y gráficos de ocupación, tiempo promedio de visita, picos de aforo
- **Escalable**: Soporte para múltiples dispositivos simultáneos
- **Segura**: Autenticación de usuarios y notificaciones de eventos críticos

## 🚀 Características Principales

### 1. **Sistema de Eventos Basado en SensorEvent**
Arquitectura innovadora basada en eventos individuales:
- **ENTRY**: Eventos de entrada de personas (grupos de 1-6 personas)
- **EXIT**: Eventos de salida de personas
- **DISCONNECTION**: Eventos de desconexión de dispositivos (5% probabilidad)

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
- Grupos de tamaño variable (40% solo, 30% parejas, 20% grupos pequeños, 10% grupos grandes)
- Balance inteligente entre entradas/salidas según ocupación actual
- Eventos de desconexión aleatorios (5%)
- Notificaciones push cuando se detecta desconexión

### 3. **Gráficos Profesionales con YCharts**
Visualización de datos de aforo con:
- **Eje Y dinámico**: Se ajusta automáticamente al rango de datos
- **Tooltips interactivos**: Muestra hora y aforo exacto al tocar
- **Sombra bajo la línea**: Gradiente visual para mejor lectura
- **Actualización en tiempo real**: Flow reactivo desde Room Database

### 4. **Sistema de Notificaciones**
Notificaciones push para eventos críticos:
- Alertas de desconexión de dispositivos
- Permiso runtime para Android 13+ (POST_NOTIFICATIONS)
- Diálogo explicativo cuando se solicita permiso
- Control total desde Settings

### 5. **Reportes y Estadísticas**
Pantalla de reportes con:
- **Gráfico de aforo**: Visualización temporal de ocupación
- **Estadísticas clave**:
  - Total de entradas
  - Total de salidas
  - Aforo actual
  - Pico de aforo
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
- **Notificaciones**:
  - Toggle para activar/desactivar
  - Solicitud automática de permiso en Android 13+
  - Diálogo explicativo cuando se necesita
- **Intervalo de simulación**:
  - Slider para ajustar frecuencia (1-30 segundos)
- **Gestión de datos**:
  - Borrar todas las lecturas
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

```kotlin
// Crear notificación de desconexión
fun showDisconnectionNotification(deviceName: String) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_warning)
        .setContentTitle("⚠️ Dispositivo desconectado")
        .setContentText("El dispositivo \"$deviceName\" ha perdido conexión")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(notificationId++, notification)
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
│   ├── AppDatabase.kt
│   ├── Device.kt
│   ├── DeviceDao.kt
│   ├── DeviceRepository.kt
│   ├── SensorEvent.kt
│   ├── SensorEventDao.kt
│   ├── SensorEventRepository.kt
│   ├── SettingsRepository.kt
│   ├── User.kt
│   └── UserDao.kt
├── domain/
│   ├── ChartPoint.kt
│   ├── OccupancyChartData.kt
│   └── ReportStats.kt
├── navigation/
│   └── AppNavigation.kt
├── security/
│   └── NotificationHandler.kt
├── service/
│   ├── DeviceSimulationService.kt
│   └── EventBasedSimulationService.kt
├── ui/
│   ├── DashboardScreen.kt
│   ├── DeviceDetailScreen.kt
│   ├── DeviceRegistrationScreen.kt
│   ├── LoginScreen.kt
│   ├── RegistrationScreen.kt
│   ├── ReportsScreen.kt
│   ├── SettingsScreen.kt
│   └── components/
│       └── OccupancyChart.kt
├── util/
│   └── PasswordUtil.kt
└── viewmodel/
    ├── DashboardViewModel.kt
    ├── DeviceDetailViewModel.kt
    ├── DeviceRegistrationViewModel.kt
    ├── ReportsViewModel.kt
    └── SettingsViewModel.kt
```

## 🔄 Roadmap

- [x] Sistema de eventos basado en SensorEvent
- [x] Gráficos profesionales con YCharts
- [x] Notificaciones de desconexión
- [x] Eje Y dinámico en gráficos
- [x] Permiso runtime para Android 13+
- [x] Estadísticas avanzadas (tiempo promedio de visita)
- [ ] Exportación de reportes a PDF/CSV
- [ ] Dashboard web complementario
- [ ] Integración con dispositivos IoT reales
- [ ] Sistema de alertas configurables
- [ ] Predicción de aforo con ML

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
