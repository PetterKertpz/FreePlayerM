package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.freeplayerm.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var mediaSession: MediaSession

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "media_playback_channel"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Creamos el canal de notificación (necesario para Android 8.0+)
        createNotificationChannel()

        // 2. Creamos el proveedor de notificaciones por defecto.
        //    La personalización del icono se hace aquí, directamente.
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID)
            .setChannelId(CHANNEL_ID)
            .build()

        // Asignamos el proveedor de notificaciones al servicio.
        setMediaNotificationProvider(notificationProvider)

        // Configuramos la intención para abrir la app al pulsar la notificación.
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSession.setSessionActivity(pendingIntent)
    }

    private fun createNotificationChannel() {
        // La comprobación de versión ya no es necesaria si tu minSdk es 26+,
        // pero la dejamos por compatibilidad y buenas prácticas.
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}