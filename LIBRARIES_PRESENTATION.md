# Librerías Externas - Resumen para Presentación

## 📊 Tabla Resumen Ejecutivo

| # | Nombre | Versión | Función | Fuente | Licencia |
|---|--------|---------|---------|--------|----------|
| 1 | **Jetpack Compose** | 2024.09.00 | Framework UI declarativo moderno | [Google](https://developer.android.com/jetpack/compose) | Apache 2.0 |
| 2 | **Material Design 3** | 2024.09.00 | Componentes UI (Cards, Buttons, etc.) | [Google](https://m3.material.io/) | Apache 2.0 |
| 3 | **Room Database** | 2.8.3 | Persistencia SQLite local | [Google](https://developer.android.com/training/data-storage/room) | Apache 2.0 |
| 4 | **Navigation Compose** | 2.7.7 | Navegación entre pantallas | [Google](https://developer.android.com/jetpack/compose/navigation) | Apache 2.0 |
| 5 | **Canvas Compose** | Built-in | Gráficos personalizados | [Google](https://developer.android.com/jetpack/compose) | Apache 2.0 |
| 6 | **Material Icons** | 1.6.7 | +2000 iconos vectoriales | [Google](https://fonts.google.com/icons) | Apache 2.0 |
| 7 | **Core KTX** | 1.10.1 | Extensiones Kotlin para Android | [Google](https://developer.android.com/kotlin/ktx) | Apache 2.0 |
| 8 | **Lifecycle Runtime** | 2.6.1 | Manejo ciclo de vida + Coroutines | [Google](https://developer.android.com/topic/libraries/architecture/lifecycle) | Apache 2.0 |
| 9 | **Gson** | 2.10.1 | Parser JSON (serialización) | [Google](https://github.com/google/gson) | Apache 2.0 |

---

## 🎯 Descripción por Categoría

### 1️⃣ UI y Frontend (4 librerías)

#### Jetpack Compose
- **Qué es**: Framework UI declarativo que reemplaza XML
- **Para qué**: Construir toda la interfaz (pantallas, componentes)
- **Por qué**: Menos código, más moderno, recomendado por Google

#### Material Design 3
- **Qué es**: Sistema de diseño de Google
- **Para qué**: Cards, botones, text fields, navegación, progress bars
- **Por qué**: UI profesional y consistente con Android

#### Material Icons Extended
- **Qué es**: Colección de +2000 iconos
- **Para qué**: Sensores, flechas, settings, gráficos, etc.
- **Por qué**: Vectoriales, escalables, sin peso extra

---

### 2️⃣ Persistencia (1 librería)

#### Room Database
- **Qué es**: Capa de abstracción sobre SQLite
- **Para qué**: Guardar usuarios, dispositivos y lecturas localmente
- **Por qué**: Type-safe, queries verificadas en compilación, integración con Flow

**Tablas creadas:**
- `users` - Usuarios con contraseñas hasheadas
- `devices` - Dispositivos Arduino
- `sensor_readings` - Historial de lecturas

---

### 3️⃣ Navegación (1 librería)

#### Navigation Compose
- **Qué es**: Sistema de navegación para Compose
- **Para qué**: Navegar entre Login → Dashboard → Detalle → Reportes
- **Por qué**: Manejo automático del back stack, deep linking

**Rutas implementadas:**
```
login → main
    ├─ dashboard (lista dispositivos)
    ├─ reports (gráficos)
    ├─ settings (configuración)
    ├─ device_detail/{id} (detalle con parámetro)
    └─ device_registration (crear nuevo)
```

---

### 4️⃣ Visualización de Datos (1 librería)

#### Canvas Compose
- **Qué es**: API de dibujo 2D nativa de Jetpack Compose
- **Para qué**: Mostrar gráficos de línea temporal (entradas/salidas)
- **Por qué**: Built-in, sin dependencias externas, totalmente customizable

**Gráficos implementados:**
- Línea temporal de entradas (verde)
- Línea temporal de salidas (rojo)
- Puntos de datos circulares
- Escalado automático según valores máximos

---

### 5️⃣ Utilidades (2 librerías)

#### Core KTX & Lifecycle Runtime
- **Qué es**: Extensiones de Kotlin para Android
- **Para qué**: Código más conciso, manejo de lifecycle, coroutines
- **Por qué**: Estándar en proyectos Kotlin

#### Gson
- **Qué es**: Parser JSON de Google
- **Para qué**: Serialización de objetos (logging, debugging)
- **Por qué**: Confiable, ampliamente usado

---

## 📈 Estadísticas

### Por Proveedor
- **Google/Android**: 9 librerías (100%)
- **Terceros**: 0 librerías (0%)

### Por Tipo
- **UI/Frontend**: 44%
- **Datos/Persistencia**: 22%
- **Navegación**: 11%
- **Visualización**: 11%
- **Utilidades**: 22%

### Licencias
- **100% Apache License 2.0**
- **100% Open Source**
- **0 restricciones comerciales**

---

## 🎤 Script para Presentación

### Slide 1: Introducción
> "La aplicación Counter App utiliza **9 librerías principales** de producción, todas de fuentes oficiales. El 89% proviene directamente de Google como parte de Android Jetpack."

### Slide 2: UI y Frontend
> "Para la interfaz usamos **Jetpack Compose**, el framework UI moderno de Google que reemplaza XML. Junto con **Material Design 3**, implementamos cards, botones y navegación con un diseño profesional y consistente."

### Slide 3: Persistencia
> "La persistencia local se maneja con **Room Database versión 2.8.3**, una capa sobre SQLite que nos da type-safety y queries verificadas en compilación. Almacenamos 3 entidades: usuarios, dispositivos y lecturas."

### Slide 4: Navegación
> "La navegación entre pantallas usa **Navigation Compose 2.7.7**, que maneja automáticamente el back stack y permite pasar parámetros entre pantallas de forma type-safe."

### Slide 5: Visualización
> "Para los gráficos utilizamos **Canvas de Jetpack Compose**, la API de dibujo 2D nativa que permite crear visualizaciones personalizadas sin dependencias externas. Dibujamos gráficos de línea temporal para entradas y salidas con escalado automático."

### Slide 6: Conclusión
> "Todas las librerías son Apache 2.0, open source y activamente mantenidas. La elección se basó en: oficialidad de Google, modernidad, performance y amplia adopción en la industria."

---

## 🔗 Links Rápidos

### Para mostrar en presentación:
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Material Design 3**: https://m3.material.io/
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Canvas Compose**: https://developer.android.com/jetpack/compose/graphics/draw/overview

---

## ✅ Checklist de Presentación

Durante la presentación mencionar:
- [x] Nombre completo de cada librería
- [x] Versión utilizada
- [x] Función específica en la app
- [x] Fuente oficial (URL o proveedor)
- [x] Por qué se eligió cada una
- [x] Licencia (Apache 2.0 para todas)

---

**Preparado para**: Presentación Counter App
**Tiempo estimado**: 3-5 minutos
**Slides sugeridos**: 6 (intro + 4 categorías + conclusión)
