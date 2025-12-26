// en: app/src/main/java/com/example/freeplayerm/FreePlayerMApp.kt
package com.example.freeplayerm

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject // ← CORRECCIÓN: javax, no jakarta

/**
 * 🎵 FREE PLAYER M - Application Class
 *
 * Punto de entrada de la aplicación con integración de:
 * - Hilt (Inyección de dependencias)
 * - WorkManager (Tareas en segundo plano)
 *
 * @HiltAndroidApp activa la generación de código de Hilt y crea
 * el contenedor de dependencias raíz adjunto al ciclo de vida de la app.
 */
@HiltAndroidApp
class FreePlayerMApp : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "FreePlayerMApp"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // ==================== WORKMANAGER CONFIG ====================

    /**
     * Configuración personalizada de WorkManager.
     * Se usa HiltWorkerFactory para inyectar dependencias en los Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO
            )
            .build()

    // ==================== LIFECYCLE ====================

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Aplicación iniciada")

        // Inicializaciones adicionales si las necesitas
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        // Aquí puedes agregar inicializaciones que no dependan de Hilt
        // Las que dependen de Hilt deben ir en los ViewModels o donde se inyecten

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "📱 Modo DEBUG activo")
        }
    }
}