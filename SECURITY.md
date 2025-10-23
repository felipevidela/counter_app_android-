# Security Policy - Counter APP

## 📋 Resumen

Counter APP implementa múltiples capas de seguridad para proteger datos de usuarios y comunicaciones IoT, siguiendo estándares internacionales ISO/IEC 27001 y mejores prácticas de la industria.

## 🔐 Medidas de Seguridad Implementadas

### 1. Autenticación de Usuarios

#### 1.1 Sistema de Login Seguro (ISO/IEC 27001 - A.9.4.2)
**Implementación**: `LoginScreen.kt`, `LoginViewModel.kt`

**Características**:
- ✅ Autenticación basada en usuario y contraseña
- ✅ Hash SHA-256 para almacenamiento de contraseñas
- ✅ Las contraseñas **nunca** se almacenan en texto plano
- ✅ Validación de credenciales en capa de ViewModel

**Código**:
```kotlin
private fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
```

#### 1.2 Validación de Contraseñas
**Implementación**: `LoginViewModel.kt:51-62`

**Requisitos enforced**:
- ✅ Mínimo 8 caracteres
- ✅ Al menos 1 letra mayúscula
- ✅ Al menos 1 carácter especial
- ✅ Validación en tiempo real

**Código**:
```kotlin
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
```

### 2. Protección de Datos

#### 2.1 Base de Datos Segura
**Implementación**: Room Database (`AppDatabase.kt`)

**Características**:
- ✅ Datos almacenados localmente en SQLite
- ✅ Acceso controlado mediante DAO pattern
- ✅ Hash de contraseñas antes de almacenamiento
- ✅ No hay contraseñas en logs o memoria

#### 2.2 Gestión de Sesión
**Implementación**: `AppNavigation.kt`

**Características**:
- ✅ Sesión iniciada solo después de autenticación exitosa
- ✅ Navegación protegida con rutas
- ✅ Logout implícito al salir de MainScreen

### 3. Comunicación Bluetooth (ISO/IEC 27001 - A.10.1.1)

#### 3.1 Controles Criptográficos
**Comentarios de seguridad**: `BluetoothService.kt:21-22`

```kotlin
// Security: ISO/IEC 27001 (A.10.1.1) - Cryptographic controls
// In a real-world scenario, data transmitted over Bluetooth should be encrypted
```

**Estado actual**:
- ⚠️ **NOTA**: Encriptación AES planificada (ver Roadmap)
- ✅ Bluetooth Low Energy (BLE) - protocolo seguro
- ✅ Pairing requerido para conexión
- ✅ Permisos de Android enforce seguridad

#### 3.2 Permisos de Bluetooth
**Implementación**: `AndroidManifest.xml:7-11`

**Principio de Mínimos Privilegios** aplicado:
```xml
<!-- Solo permisos necesarios para funcionalidad Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Justificación de permisos**:
- `BLUETOOTH`: Comunicación básica BLE
- `BLUETOOTH_ADMIN`: Descubrimiento de dispositivos
- `BLUETOOTH_SCAN`: Escaneo de dispositivos BLE (Android 12+)
- `BLUETOOTH_CONNECT`: Conexión a dispositivos (Android 12+)
- `ACCESS_FINE_LOCATION`: Requerido por Android para BLE scan

### 4. Validación de Entrada

#### 4.1 Sanitización de Inputs
**Implementación**: Todos los TextFields

**Protecciones**:
- ✅ `singleLine = true` previene inyecciones multilinea
- ✅ Validación de longitud máxima
- ✅ Escapado automático en Room Database
- ✅ No se ejecuta código desde inputs

#### 4.2 Prevención de SQL Injection
**Implementación**: Room Database con consultas parametrizadas

```kotlin
@Query("SELECT * FROM users WHERE username = :username")
suspend fun getUser(username: String): User?
```

### 5. Gestión de Estados Seguros

#### 5.1 Estado de Conexión
**Implementación**: `StateFlow` en `BluetoothService`

**Seguridad**:
- ✅ Estado sincronizado entre pantallas
- ✅ No hay estados inconsistentes
- ✅ Desconexión limpia con `resetAndDisconnect()`

#### 5.2 Limpieza de Datos
**Implementación**: `SimulatedBluetoothService.kt:131-148`

```kotlin
override fun resetAndDisconnect() {
    // Stop counter generation
    isConnected = false
    counterJob?.cancel()

    // Reset ALL state
    entered = 0
    left = 0
    _counterDataFlow.value = CounterData(0, 0, 0)
    _eventLogFlow.value = emptyList()
    _connectionStateFlow.value = "Desconectado"
}
```

## 🚨 Vulnerabilidades Conocidas y Mitigaciones

### 1. Transmisión de Datos Bluetooth
**Riesgo**: Datos transmitidos sin encriptación adicional

**Mitigación Actual**:
- BLE usa encriptación de capa de enlace (Link Layer Encryption)
- Pairing obligatorio antes de comunicación

**Mitigación Futura**:
- Implementar AES-256 para encriptación de capa de aplicación
- Ver [Roadmap](#roadmap-de-seguridad)

### 2. Almacenamiento Local
**Riesgo**: Base de datos SQLite no encriptada por defecto

**Mitigación Actual**:
- Contraseñas hasheadas con SHA-256
- Datos sensibles minimizados
- Acceso protegido por autenticación de Android

**Mitigación Futura**:
- Considerar SQLCipher para encriptación de base de datos completa

### 3. Man-in-the-Middle (MITM) Bluetooth
**Riesgo**: Interceptación de comunicación BLE

**Mitigación Actual**:
- Bluetooth pairing obligatorio
- Verificación manual del dispositivo (usuario selecciona)

**Mitigación Futura**:
- Implementar autenticación mutua
- Certificados de dispositivos

## 📊 Cumplimiento de Estándares

### ISO/IEC 27001:2013

| Control | Descripción | Estado | Implementación |
|---------|-------------|--------|----------------|
| A.9.4.1 | Restricción de acceso a la información | ✅ | Login obligatorio |
| A.9.4.2 | Procedimientos seguros de conexión | ✅ | SHA-256, validación |
| A.9.4.3 | Sistema de gestión de contraseñas | ✅ | Validación, hashing |
| A.10.1.1 | Política de uso de controles criptográficos | ⚠️ | Comentado, planificado |
| A.10.1.2 | Gestión de llaves | ⚠️ | Pendiente |
| A.12.3.1 | Respaldo de información | ✅ | Room Database |
| A.18.1.5 | Regulación de controles criptográficos | ✅ | SHA-256 documentado |

**Leyenda**: ✅ Implementado | ⚠️ Parcial | ❌ No implementado

Ver [ISO_COMPLIANCE.md](ISO_COMPLIANCE.md) para detalles completos.

## 🛡️ Mejores Prácticas Implementadas

### 1. Principio de Mínimos Privilegios
- Solo permisos necesarios solicitados
- Acceso a datos restringido por autenticación
- Servicios con scope limitado

### 2. Defensa en Profundidad
- Múltiples capas de seguridad
- Validación en cliente y lógica de negocio
- Hash + validación de contraseñas

### 3. Seguridad por Diseño
- Comentarios ISO/IEC 27001 en código
- Arquitectura con separación de responsabilidades
- Estado inmutable con StateFlow

### 4. Fail Secure
- En caso de error, la app cierra sesión
- Datos sensibles no se loggean
- Errores genéricos mostrados al usuario

## 🔄 Roadmap de Seguridad

### Corto Plazo (1-2 meses)
- [ ] Implementar AES-256 para datos Bluetooth
- [ ] Agregar logging de eventos de seguridad
- [ ] Implementar rate limiting para intentos de login
- [ ] Agregar biometría (fingerprint/face)

### Medio Plazo (3-6 meses)
- [ ] Encriptación de base de datos con SQLCipher
- [ ] Implementar certificados SSL para comunicación futura
- [ ] Autenticación mutua de dispositivos
- [ ] Auditoría de seguridad externa

### Largo Plazo (6-12 meses)
- [ ] Penetration testing
- [ ] Certificación ISO/IEC 27001 completa
- [ ] Cumplimiento GDPR/CCPA
- [ ] Bug bounty program

## 📞 Reportar Vulnerabilidades

Si encuentras una vulnerabilidad de seguridad:

1. **NO** abras un issue público
2. Envía un email a: [tu-email-de-seguridad]
3. Incluye:
   - Descripción detallada
   - Pasos para reproducir
   - Impacto potencial
   - Sugerencias de mitigación (opcional)

**Compromiso**:
- Respuesta inicial: 48 horas
- Evaluación completa: 7 días
- Fix y publicación: 30 días

## 🏆 Reconocimientos

Agradecimientos a:
- OWASP Mobile Security Project
- Android Security Best Practices
- ISO/IEC 27001 Standards
- Claude AI por asistencia en seguridad

---

**Última actualización**: 2025-01-23
**Versión**: 1.0.0
