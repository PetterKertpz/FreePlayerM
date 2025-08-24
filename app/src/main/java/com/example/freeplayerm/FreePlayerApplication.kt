package com.example.freeplayerm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * @HiltAndroidApp: Esta anotación es crucial. Activa la generación de código de Hilt
 * y crea el contenedor de dependencias que se adjuntará al ciclo de vida de la aplicación.
 */
@HiltAndroidApp
class FreePlayerApplication : Application() {
    // Por ahora, el cuerpo de la clase puede estar vacío.
}