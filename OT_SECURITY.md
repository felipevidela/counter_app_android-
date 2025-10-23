# OT Security - Operational Technology Security Guidelines

## 📋 Resumen

Este documento detalla el cumplimiento de Counter APP con lineamientos de seguridad para Tecnología Operacional (OT), específicamente el estándar **IEC 62443** - Seguridad en Sistemas de Control Industrial e IoT.

**Estándar Aplicable**: IEC 62443-4-2 (Requisitos técnicos de seguridad para componentes IACS)
**Contexto**: Aplicación móvil IoT para monitoreo de sensores de conteo
**Última Revisión**: 2025-01-23

## 🎯 Contexto de Aplicación

Counter APP es una aplicación de **Tecnología de la Información (IT)** que interactúa con **Tecnología Operacional (OT)**:

```
┌─────────────────────────────────────────────────────────┐
│                    IT Domain                            │
│  ┌──────────────────────────────────────────────┐      │
│  │   Counter APP (Android Mobile)               │      │
│  │   - User Authentication                       │      │
│  │   - Data Visualization                        │      │
│  │   - Event Logging                             │      │
│  └───────────────┬──────────────────────────────┘      │
│                  │ Bluetooth BLE                        │
│ ═════════════════╪════════════════════════════════════ │
│                  │ IT/OT Boundary                       │
│ ═════════════════╪════════════════════════════════════ │
│                  │                                      │
│  ┌───────────────▼──────────────────────────────┐      │
│  │         OT Domain                             │      │
│  │  ┌─────────────────────────────────────┐     │      │
│  │  │  IoT Counter Device (BLE)           │     │      │
│  │  │  - Infrared Sensors                 │     │      │
│  │  │  - Entry/Exit Detection             │     │      │
│  │  │  - BLE Communication Module         │     │      │
│  │  └─────────────────────────────────────┘     │      │
│  └───────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────┘
```

## 🔐 IEC 62443 - Cumplimiento

### Nivel de Seguridad Objetivo

**Security Level (SL)**: SL-2 (Protección contra ataques intencionales con recursos limitados)

| Nivel | Descripción | Aplicable |
|-------|-------------|-----------|
| SL-1  | Protección contra acceso casual/accidental | ✅ Cumple |
| SL-2  | Protección contra ataques intencionales simples | ⚠️ Parcial |
| SL-3  | Protección contra ataques sofisticados | ❌ No |
| SL-4  | Protección contra ataques con recursos extensos | ❌ No |

### Requisitos Fundamentales (FR - Foundational Requirements)

#### FR 1 - Identificación y Control de Autenticación

##### FR 1.1 - Identificación y Autenticación Humana
**Estado**: ✅ Implementado

**Implementación**:
- Sistema de login con usuario y contraseña
- Autenticación obligatoria antes de acceso
- SHA-256 para verificación de contraseñas

**Evidencia**:
```kotlin
// LoginViewModel.kt:21-30
fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
        val user = repository.getUser(username)
        if (user != null && user.passwordHash == hashPassword(password)) {
            onResult(true)
        }
    }
}
```

**Cumplimiento IEC 62443-4-2**:
- ✅ Identificación única por usuario
- ✅ Autenticación antes de acceso
- ✅ Contraseñas hasheadas (nunca en texto plano)

---

##### FR 1.2 - Identificación y Autenticación de Software y Dispositivos
**Estado**: ⚠️ Parcial

**Implementación Actual**:
- Usuario selecciona manualmente dispositivo BLE
- Dirección MAC del dispositivo como identificador
- No hay autenticación mutua implementada

**Evidencia**:
```kotlin
// MonitoringScreen.kt:133-134
bluetoothService.connectToDevice("00:11:22:33:44:55")
// o
bluetoothService.connectToDevice(result.device.address)
```

**Cumplimiento**:
- ✅ Identificación de dispositivo por MAC
- ⚠️ No hay certificados de dispositivo
- ⚠️ No hay challenge-response

**Recomendaciones**:
- Implementar autenticación mutua (app ↔ dispositivo)
- Usar certificados digitales para dispositivos
- Implementar pairing seguro con PIN

---

##### FR 1.3 - Fortaleza de Contraseñas
**Estado**: ✅ Implementado

**Requisitos Implementados**:
```kotlin
// LoginViewModel.kt:51-62
private fun validatePassword(password: String): String? {
    if (password.length < 8)  // Mínimo 8 caracteres
    if (!password.any { it.isUpperCase() })  // Al menos una mayúscula
    if (!password.any { !it.isLetterOrDigit() })  // Al menos un especial
}
```

**Cumplimiento IEC 62443**:
| Requisito | IEC 62443 | Implementado |
|-----------|-----------|--------------|
| Longitud mínima | ≥ 8 | ✅ 8 chars |
| Caracteres mezclados | Sí | ✅ Mayúsculas + especiales |
| Cambio periódico | Recomendado | ❌ No |
| Historial | Sí | ❌ No |

---

#### FR 2 - Control de Uso

##### FR 2.1 - Autorización
**Estado**: ✅ Implementado

**Implementación**:
- Acceso a funcionalidades solo después de login
- Navegación protegida por autenticación

**Arquitectura**:
```
Login Screen → Authentication Success → Main Screen
     ↑                                        ↓
     └──────────── Logout ───────────────────┘
```

---

##### FR 2.2 - Gestión de Privilegios
**Estado**: ❌ No Implementado (No Aplicable)

**Razón**: Aplicación de usuario único, todos tienen mismos privilegios

**Recomendación Futura**:
- Roles: Admin, Operator, Viewer
- Admin: Configuración, ver todos los datos
- Operator: Conectar dispositivos, ver datos
- Viewer: Solo visualización

---

#### FR 3 - Integridad del Sistema

##### FR 3.1 - Integridad de Comunicación
**Estado**: ⚠️ Parcial

**Implementación Actual**:
- Bluetooth BLE con Link Layer Encryption
- No hay encriptación de capa de aplicación

**Cumplimiento**:
- ✅ Protocolo seguro (BLE)
- ⚠️ No hay HMAC/firma digital
- ⚠️ No hay detección de replay attacks

**Recomendaciones**:
- Implementar AES-256-GCM para datos
- Agregar timestamp + nonce para prevenir replay
- Implementar Message Authentication Code (MAC)

---

##### FR 3.2 - Protección contra Malware
**Estado**: ✅ Implementado

**Medidas**:
- Aplicación distribuida via Google Play (firma APK)
- Código fuente público para auditoría
- Sin ejecución de código dinámico
- Room Database previene SQL injection

---

#### FR 4 - Confidencialidad de Datos

##### FR 4.1 - Confidencialidad de Información
**Estado**: ⚠️ Parcial

**Datos Sensibles Identificados**:

| Dato | Almacenamiento | Transmisión | Protección |
|------|----------------|-------------|------------|
| Contraseñas | Room DB (SHA-256) | No transmitida | ✅ Hash |
| Datos de Contador | StateFlow (memoria) | BLE | ⚠️ BLE encryption |
| Eventos | StateFlow + Room | No | ✅ Local only |
| Username | Room DB | No | ✅ Local only |

**Cumplimiento**:
- ✅ Contraseñas protegidas
- ⚠️ Datos BLE sin encriptación adicional
- ✅ Sin transmisión a internet

**Recomendaciones**:
- AES-256 para datos Bluetooth
- Encriptación de base de datos (SQLCipher)

---

##### FR 4.2 - Protección de Información en Tránsito
**Estado**: ⚠️ Parcial

**Implementación**:
```kotlin
// BluetoothService.kt:21-22
// Security: ISO/IEC 27001 (A.10.1.1) - Cryptographic controls
// In a real-world scenario, data transmitted over Bluetooth should be encrypted
```

**Estado Actual**:
- Bluetooth BLE usa AES-CCM en link layer
- No hay TLS (no aplicable para BLE)
- No hay encriptación de capa de aplicación

**Nivel de Protección**:
- SL-1: ✅ Link layer encryption
- SL-2: ⚠️ Falta app-layer encryption
- SL-3: ❌ No implementado

---

#### FR 5 - Flujo de Datos Restringido

##### FR 5.1 - Segmentación de Red
**Estado**: ✅ Implementado (Arquitectura)

**Segmentación**:
```
┌──────────────────────────────────────────┐
│  Internet / Cloud         (No conectado) │
└──────────────────────────────────────────┘
                    ✗ No connection
┌──────────────────────────────────────────┐
│  Mobile App (IT)         (Aislado)       │
│  - User data local only                  │
│  - No internet access                    │
└───────────────┬──────────────────────────┘
                │ Bluetooth BLE
                │ (Protocolo controlado)
┌───────────────▼──────────────────────────┐
│  IoT Device (OT)         (Edge device)   │
│  - Sensors only                          │
│  - No internet access                    │
└──────────────────────────────────────────┘
```

**Cumplimiento**:
- ✅ Sin acceso a internet
- ✅ Comunicación punto a punto (1:1)
- ✅ Protocolo específico (BLE)
- ✅ Sin puentes de red

---

##### FR 5.2 - Protección de Límites
**Estado**: ✅ Implementado

**Límites Definidos**:
- App ↔ Dispositivo: Solo Bluetooth BLE
- Permisos Android controlan acceso
- No hay puertos de red abiertos

---

#### FR 6 - Respuesta Oportuna a Eventos

##### FR 6.1 - Auditoría de Eventos
**Estado**: ⚠️ Parcial

**Eventos Registrados**:
```kotlin
// SimulatedBluetoothService.kt
events.add(CounterEvent(EventType.ENTRY, Date()))
events.add(CounterEvent(EventType.EXIT, Date()))
```

**Cumplimiento**:
- ✅ Eventos de negocio (entries/exits)
- ⚠️ No hay eventos de seguridad
- ❌ No hay logs de autenticación

**Pendiente**:
- Login attempts (exitosos y fallidos)
- Conexiones a dispositivos
- Desconexiones inesperadas
- Errores de comunicación

---

##### FR 6.2 - Monitoreo Continuo
**Estado**: ✅ Implementado

**Implementación**:
- Hot flows con StateFlow
- Monitoreo continuo mientras conectado
- Background service persistente

```kotlin
// SimulatedBluetoothService.kt:71-98
counterJob = serviceScope.launch {
    while (isConnected) {
        delay(3000)
        // Continuous monitoring
        _counterDataFlow.value = counterData
    }
}
```

---

#### FR 7 - Disponibilidad de Recursos

##### FR 7.1 - Denegación de Servicio (DoS)
**Estado**: ⚠️ Parcial

**Protecciones Actuales**:
- Android maneja recursos Bluetooth
- Timeout en operaciones BLE (framework)
- No hay rate limiting en login

**Vulnerabilidades**:
- ⚠️ Login sin rate limiting (brute force posible)
- ⚠️ No hay protección contra BLE flooding

**Recomendaciones**:
- Rate limiting: 5 intentos / minuto
- Lockout temporal después de 5 fallos
- Timeout configurable para conexiones BLE

---

## 🏭 Zonas y Conductos (Zones and Conduits)

### Zonas Definidas

#### Zona 1: Mobile Application (IT Zone)
**Nivel de Confianza**: Alto
**Componentes**:
- Counter APP (Android)
- Room Database
- User Authentication

**Controles de Seguridad**:
- Autenticación de usuario
- Encriptación de datos en reposo (SHA-256 passwords)
- Sandboxing de Android

---

#### Zona 2: IoT Device (OT Zone)
**Nivel de Confianza**: Medio
**Componentes**:
- Dispositivo contador BLE
- Sensores infrarrojos
- Módulo Bluetooth

**Controles de Seguridad**:
- Pairing Bluetooth
- Link layer encryption
- Sin acceso a internet

---

### Conducto: Bluetooth BLE

**Protocolo**: Bluetooth Low Energy 4.0+
**Encriptación**: AES-CCM (link layer)
**Autenticación**: Pairing (Numeric Comparison)

**Seguridad del Conducto**:
| Control | Estado |
|---------|--------|
| Encriptación en tránsito | ✅ BLE native |
| Autenticación de endpoints | ⚠️ Pairing básico |
| Integridad de mensajes | ✅ BLE CRC |
| Anti-replay | ❌ No |
| Rate limiting | ❌ No |

---

## 📊 Matriz de Riesgos OT

| Amenaza | Probabilidad | Impacto | Riesgo | Mitigación |
|---------|--------------|---------|--------|------------|
| Acceso no autorizado a app | Media | Alto | Alto | ✅ Login + SHA-256 |
| Interceptación BLE | Alta | Medio | Alto | ⚠️ BLE encryption (necesita AES app-layer) |
| Malware en dispositivo móvil | Baja | Alto | Medio | ✅ Android sandbox |
| Brute force de contraseñas | Media | Alto | Alto | ⚠️ Validación (necesita rate limiting) |
| Replay attack BLE | Media | Medio | Medio | ❌ No hay protección |
| Suplantación de dispositivo | Baja | Alto | Medio | ⚠️ Pairing (necesita certificados) |
| DoS en login | Baja | Bajo | Bajo | ❌ No hay rate limiting |

**Leyenda Riesgo**: Alto (Acción inmediata) | Medio (Planificar) | Bajo (Monitorear)

---

## 🔧 Recomendaciones de Mejora

### Prioridad Crítica (1-2 meses)

1. **Encriptación de Capa de Aplicación**
   ```kotlin
   class BluetoothEncryption {
       fun encryptData(data: ByteArray, key: SecretKey): ByteArray {
           val cipher = Cipher.getInstance("AES/GCM/NoPadding")
           cipher.init(Cipher.ENCRYPT_MODE, key)
           return cipher.doFinal(data)
       }
   }
   ```

2. **Rate Limiting en Login**
   ```kotlin
   class LoginRateLimiter {
       private val attempts = mutableMapOf<String, AttemptInfo>()
       fun checkRateLimit(username: String): Boolean {
           // Max 5 attempts per 5 minutes
       }
   }
   ```

### Prioridad Alta (2-4 meses)

3. **Auditoría de Eventos de Seguridad**
   - Log de autenticaciones
   - Log de conexiones BLE
   - Timestamps precisos

4. **Autenticación Mutua de Dispositivos**
   - Certificados para dispositivos IoT
   - Challenge-response protocol

### Prioridad Media (4-6 meses)

5. **Protección Anti-Replay**
   - Nonce en cada mensaje
   - Timestamp validation

6. **Gestión de Certificados**
   - Android Keystore
   - Rotación de llaves

---

## 📈 Roadmap de Cumplimiento IEC 62443

| Trimestre | Objetivo | Controles Implementados |
|-----------|----------|------------------------|
| Q1 2025 | SL-1 Completo | FR 1.1, FR 1.3, FR 2.1, FR 5.1 |
| Q2 2025 | SL-2 Parcial | + FR 3.1, FR 4.1, FR 6.1 |
| Q3 2025 | SL-2 Completo | + FR 1.2, FR 4.2, FR 7.1 |
| Q4 2025 | SL-3 Inicio | Evaluación de requisitos |

---

## 📞 Contacto

**Responsable de Seguridad OT**: Felipe Videla
**Email**: [tu-email]
**Última Actualización**: 2025-01-23

---

**Referencias**:
- IEC 62443-4-2: Technical security requirements for IACS components
- IEC 62443-3-3: System security requirements and security levels
- NIST SP 800-82: Guide to Industrial Control Systems Security
