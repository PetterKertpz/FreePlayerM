// settings.gradle.kts
@file:Suppress("UnstableApiUsage") // <--- ESTA LÍNEA ELIMINA LA ADVERTENCIA AMARILLA

// ✅ 1. Gestión de Plugins (SIEMPRE PRIMERO)
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}

// ✅ 2. Plugins del Sistema (Toolchains)
plugins {
    // Esta herramienta ayuda a descargar Java automáticamente si falta.
    // En 2025, la versión recomendada suele ser más alta, pero 0.8.0+ funciona.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// ✅ 3. Gestión Centralizada de Dependencias (El estándar moderno)
dependencyResolutionManagement {
    // Esto OBLIGA a todos los módulos a usar estos repositorios (evita conflictos)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FreePlayerM"
include(":app")