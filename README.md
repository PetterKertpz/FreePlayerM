<div align="center">

# 🎵 FreePlayerM

### Reproductor de Música Local para Android

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![License](https://img.shields.io/badge/License-Academic-blue.svg)](#licencia)

<img src="app/src/main/res/drawable/free_player.webp" width="192" alt="FreePlayerM Logo"/>

*Una aplicación Android nativa para reproducir música almacenada localmente con enriquecimiento automático de metadatos*

[Características](#-características) •
[Arquitectura](#-arquitectura) •
[Instalación](#-instalación) •
[Uso](#-uso) •
[Tecnologías](#-tecnologías)

</div>

---

## 📋 Descripción

**FreePlayerM** es una aplicación móvil Android desarrollada como proyecto académico que permite reproducir archivos de música almacenados localmente en el dispositivo. A diferencia de las aplicaciones de streaming, FreePlayerM se enfoca en la biblioteca musical personal del usuario, enriqueciéndola automáticamente con letras, información de artistas y portadas obtenidas de fuentes externas.

### 🎯 Objetivos del Proyecto

- Implementar una arquitectura limpia y escalable siguiendo las mejores prácticas de desarrollo Android
- Demostrar el uso de tecnologías modernas del ecosistema Android (Jetpack Compose, Kotlin Coroutines, Room)
- Crear una experiencia de usuario fluida e intuitiva para la reproducción de música local
- Integrar servicios externos para enriquecer la experiencia musical

---

## ✨ Características

### Reproducción de Música
- 🎵 Reproducción de archivos locales (MP3, AAC, FLAC, OGG)
- advancement Controles completos: Play/Pausa, Anterior, Siguiente
- 🔀 Modo aleatorio (Shuffle)
- 🔁 Modos de repetición: Ninguno, Una canción, Todas
- 🎚️ Barra de progreso interactiva con seeking
- 🔊 Normalización de volumen entre canciones
- 🎛️ Crossfade para transiciones suaves

### Biblioteca Musical
- 📚 Organización por Canciones, Álbumes, Artistas y Géneros
- 🔍 Búsqueda en tiempo real
- 📑 Ordenamiento múltiple (Título, Artista, Álbum, Duración, Fecha)
- ❤️ Sistema de favoritos
- 📝 Listas de reproducción personalizadas

### Enriquecimiento Automático
- 📜 Obtención de letras desde Genius API
- 🖼️ Descarga automática de portadas de álbumes
- 👤 Información y biografías de artistas
- 🔗 Enlaces a redes sociales de artistas

### Experiencia de Usuario
- 🌙 Interfaz moderna con Jetpack Compose
- 📱 Reproductor expandible con animaciones fluidas
- 💿 Visualización de vinilo giratorio
- 🔔 Controles desde notificaciones del sistema
- 🎧 Compatibilidad con controles de auriculares
- 🚀 Reproducción en segundo plano

### Autenticación
- 👤 Registro e inicio de sesión local
- 🔐 Autenticación con Google (Firebase Auth)
- 🔑 Recuperación de contraseña

---

## 🏗️ Arquitectura

FreePlayerM implementa **Clean Architecture** con el patrón de presentación **MVVM**, garantizando separación de responsabilidades, testabilidad y mantenibilidad del código.

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                      │
│         (UI Components, ViewModels, States)                  │
│                    Jetpack Compose + MVVM                    │
├─────────────────────────────────────────────────────────────┤
│                      CAPA DE DOMINIO                         │
│            (Use Cases, Entities, Repositories)               │
│                    Lógica de Negocio                         │
├─────────────────────────────────────────────────────────────┤
│                       CAPA DE DATOS                          │
│              (Room DB, Retrofit, DataSources)                │
│              Persistencia y Fuentes Remotas                  │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de Datos

```
UI (Compose) ←→ ViewModel (State) ←→ Repository ←→ DataSource (DAO/API)
     ↑              ↑                    ↑              ↑
 Recompose      Collect              Emit           Query
     └── StateFlow ──┴── suspend fun ──┴── Flow<T> ──┘
```

---

## 📁 Estructura del Proyecto

```
com.example.freeplayerm/
├── core/                    # Componentes transversales
│   ├── auth/               # Autenticación (Google Auth)
│   └── security/           # Seguridad y encriptación
├── data/                    # Capa de datos
│   ├── local/              # Persistencia local
│   │   ├── dao/            # Data Access Objects (Room)
│   │   └── entity/         # Entidades de Room
│   ├── remote/             # Fuentes remotas
│   │   └── genius/         # Integración Genius API
│   ├── repository/         # Implementación de repositorios
│   ├── purification/       # Pipeline de enriquecimiento
│   └── scanner/            # Escaneo de archivos musicales
├── di/                      # Módulos de inyección (Hilt)
├── receiver/               # Broadcast Receivers
├── services/               # Servicios Android (MusicService)
├── ui/                      # Capa de presentación
│   ├── features/           # Pantallas por funcionalidad
│   │   ├── auth/           # Login, Registro, Recuperación
│   │   ├── library/        # Biblioteca musical
│   │   ├── player/         # Reproductor de música
│   │   ├── profile/        # Perfil de usuario
│   │   └── settings/       # Configuraciones
│   ├── theme/              # Sistema de diseño y temas
│   └── nav/                # Navegación
├── utils/                   # Utilidades generales
├── FreePlayerApplication.kt # Application class
└── MainActivity.kt          # Activity principal
```

---

## 🛠️ Tecnologías

### Lenguaje y Plataforma
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Kotlin | 2.0.0 | Lenguaje principal con compilador K2 |
| Android SDK | API 36 | Nivel de compilación (Android 16) |
| Min SDK | API 26 | Android 8.0 Oreo mínimo |

### Frameworks y Librerías Principales

#### Interfaz de Usuario
- **Jetpack Compose** - UI declarativa moderna
- **Material Design 3** - Sistema de diseño
- **Coil** - Carga asíncrona de imágenes
- **Navigation Compose** - Navegación entre pantallas

#### Arquitectura y DI
- **Dagger Hilt** - Inyección de dependencias
- **ViewModel** - Gestión de estado de UI
- **StateFlow/Flow** - Programación reactiva

#### Persistencia
- **Room Database** - Base de datos local SQLite
- **DataStore** - Preferencias de usuario

#### Networking
- **Retrofit 2** - Cliente HTTP
- **OkHttp** - Cliente HTTP de bajo nivel
- **Moshi** - Serialización JSON

#### Reproducción de Audio
- **Media3 ExoPlayer** - Motor de reproducción
- **MediaSession** - Integración con sistema

#### Autenticación
- **Firebase Auth** - Autenticación con Google
- **Google Sign-In** - OAuth 2.0

#### Procesamiento en Background
- **WorkManager** - Tareas programadas
- **Kotlin Coroutines** - Concurrencia

---

## 📋 Requisitos Previos

### Para Desarrollo
- **Android Studio** Ladybug (2024.2.1) o superior
- **JDK 17** (OpenJDK, Oracle JDK o Amazon Corretto)
- **Git** para control de versiones

### Para Ejecución
- Dispositivo Android con **API 26+** (Android 8.0 Oreo)
- Permisos de almacenamiento para acceder a archivos de música
- Conexión a internet (opcional, para enriquecimiento de metadatos)

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/[tu-usuario]/FreePlayerM.git
cd FreePlayerM
```

### 2. Configurar Variables de Entorno

Crear archivo `local.properties` en la raíz del proyecto:

```properties
# Ruta al SDK de Android
sdk.dir=/Users/[usuario]/Library/Android/sdk

# API Key de Genius (requerida para letras)
GENIUS_API_KEY=your_genius_api_key_here

# Debug de Firebase (opcional)
FIREBASE_DEBUG_ENABLED=false
```

### 3. Configurar Firebase

1. Crear proyecto en [Firebase Console](https://console.firebase.google.com)
2. Registrar aplicación Android con package name: `com.example.freeplayerm`
3. Descargar `google-services.json`
4. Colocar el archivo en el directorio `app/`

### 4. Compilar el Proyecto

```bash
# Limpiar proyecto
./gradlew clean

# Compilar variante debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug
```

### 5. Ejecutar Tests

```bash
# Tests unitarios
./gradlew testDebugUnitTest

# Tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedDebugAndroidTest
```

---

## 📱 Uso

### Primer Inicio

1. **Instalar** la aplicación en el dispositivo
2. **Conceder permisos** de acceso a archivos de música cuando se solicite
3. **Crear cuenta** o iniciar sesión con Google
4. Esperar el **escaneo automático** de la biblioteca musical

### Navegación Principal

| Sección | Descripción |
|---------|-------------|
| **Canciones** | Lista completa de música escaneada |
| **Álbumes** | Agrupación por álbum con portadas |
| **Artistas** | Navegación por artista |
| **Géneros** | Clasificación por género musical |
| **Listas** | Playlists personalizadas |

### Controles del Reproductor

| Control | Función |
|---------|---------|
| advancement/⏸️ | Reproducir / Pausar |
| ⏮️ | Canción anterior |
| ⏭️ | Canción siguiente |
| 🔁 | Ciclar modo repetición |
| 🔀 | Activar/desactivar aleatorio |
| ❤️ | Agregar/quitar de favoritos |

### Gestos

- **Deslizar hacia arriba** en el mini reproductor para expandir
- **Deslizar hacia abajo** en reproductor expandido para colapsar
- **Deslizar horizontal** en portada para cambiar canción

---

## ⚙️ Configuración

### Opciones Disponibles

| Categoría | Opción | Valores |
|-----------|--------|---------|
| **Audio** | Calidad preferida | Baja / Media / Alta / Lossless |
| **Audio** | Normalizar volumen | On / Off |
| **Audio** | Crossfade | 0-12 segundos |
| **Red** | Solo WiFi streaming | On / Off |
| **Red** | Tamaño de caché | 100-2000 MB |
| **Notificaciones** | Mostrar controles | On / Off |

---

## 🔧 Solución de Problemas

### Biblioteca Vacía
- Verificar permisos: Settings → Apps → FreePlayerM → Permissions
- Habilitar permiso "Music and Audio" o "Files and Media"

### Letras No Disponibles
- Verificar conexión a internet
- La canción puede no estar en la base de datos de Genius
- Verificar que GENIUS_API_KEY esté configurada

### Error de Reproducción
- Verificar formato de archivo (MP3, AAC, FLAC, OGG soportados)
- El archivo puede estar corrupto

---

## 📊 Diagrama de Base de Datos

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│ USUARIO  │    │ ARTISTA  │    │  GÉNERO  │
├──────────┤    ├──────────┤    ├──────────┤
│ PK id    │    │ PK id    │    │ PK id    │
│ username │    │ name     │    │ name     │
│ email    │    │ image_url│    │ desc     │
│ password │    │ biography│    └────┬─────┘
└────┬─────┘    └────┬─────┘         │
     │ 1:N           │ 1:N           │ 1:N
     ↓               ↓               ↓
┌──────────┐    ┌──────────────────────────┐
│ PLAYLIST │    │         CANCIÓN          │
├──────────┤    ├──────────────────────────┤
│ PK id    │    │ PK id                    │
│ FK user  │    │ title, FK artist/album   │
│ name     │    │ FK genre, duration       │
│ created  │    │ file_path, conf_score    │
└────┬─────┘    └────────────┬─────────────┘
     │ N:M                   │ 1:1
     ↓                       ↓
┌──────────────┐        ┌──────────┐
│PLAYLIST_ITEM │        │  LETRAS  │
├──────────────┤        ├──────────┤
│ FK playlist  │        │ FK song  │
│ FK song      │        │ content  │
│ position     │        │ source   │
└──────────────┘        └──────────┘
```

---

## 🧪 Testing

### Estructura de Tests

```
app/
├── src/
│   ├── test/                    # Tests unitarios
│   │   └── java/
│   │       └── com.example.freeplayerm/
│   │           ├── repository/  # Tests de repositorios
│   │           ├── viewmodel/   # Tests de ViewModels
│   │           └── util/        # Tests de utilidades
│   └── androidTest/             # Tests instrumentados
│       └── java/
│           └── com.example.freeplayerm/
│               ├── dao/         # Tests de DAOs
│               └── ui/          # Tests de UI
```

### Ejecutar Tests

```bash
# Tests unitarios con reporte
./gradlew testDebugUnitTest jacocoTestReport

# Verificar cobertura
open app/build/reports/jacoco/testDebugUnitTest/html/index.html
```

---

## 📄 Licencia

Este proyecto fue desarrollado con fines **académicos** como parte de un programa educativo.

```
© 2026 FreePlayerM - Todos los derechos reservados
Desarrollado por PetterMullerKertpz (David Lopez)
```

---

## 👨‍💻 Autor

**David Lopez** (PetterMullerKertpz)

---

## 🙏 Agradecimientos

- [Genius API](https://genius.com/developers) - Letras y metadatos musicales
- [Android Developers](https://developer.android.com) - Documentación oficial
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Framework de UI
- [Material Design](https://material.io) - Sistema de diseño

---

<div align="center">

**⭐ Si este proyecto te fue útil, considera darle una estrella ⭐**

Hecho con ❤️ para la comunidad académica

</div>