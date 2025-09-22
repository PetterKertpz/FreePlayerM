package com.example.freeplayerm.services

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import com.example.freeplayerm.R

/**
 * Heredamos del proveedor por defecto de Media3 para reutilizar toda su lógica.
 * Lo único que cambiamos (override) es la función que decide qué icono pequeño usar.
 */
@UnstableApi
class CustomNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {

    /**
     * Esta función es llamada por el sistema para obtener el icono de la notificación.
     * Al sobreescribirla, forzamos a que siempre use nuestro icono personalizado.
     */
    fun getSmallIconResourceId(): Int {
        return R.drawable.ic_notification
    }
}