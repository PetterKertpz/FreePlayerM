package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList

/**
 * Proveedor de notificación personalizado para Media3.
 * Simplemente usa el DefaultMediaNotificationProvider y asegura que el canal esté creado.
 *
 * NOTA: Para cambiar el icono de notificación, crea un archivo drawable llamado
 * "media3_notification" y Media3 lo usará automáticamente.
 */
@UnstableApi
class CustomNotificationProvider(
    context: Context
) : MediaNotification.Provider {

    private val defaultProvider = DefaultMediaNotificationProvider(context)

    init {
        // Crear el canal de notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            MusicService.CHANNEL_ID,
            "Reproducción de Música",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones del reproductor de música"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        return defaultProvider.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            onNotificationChangedCallback
        )
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        return defaultProvider.handleCustomCommand(session, action, extras)
    }
}