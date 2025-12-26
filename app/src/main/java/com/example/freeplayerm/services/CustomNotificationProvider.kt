package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.freeplayerm.MainActivity

import com.example.freeplayerm.R
import com.google.common.collect.ImmutableList

/**
 * Proveedor de notificaciones personalizado para Media3.
 *
 * NOTA: Asegúrate de tener la dependencia 'androidx.media3:media3-session'
 * en tu build.gradle para usar MediaStyleNotificationHelper.
 */
@UnstableApi
class CustomNotificationProvider(
    private val context: Context
) : MediaNotification.Provider {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val currentNotificationId = MusicService.NOTIFICATION_ID

    // ✅ Corregido: TAG dentro de companion object y marcado como privado
    companion object {
        private const val TAG = "CustomNotification"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Solo necesario para Android 8.0 (Oreo) en adelante
        val channel = NotificationChannel(
            MusicService.CHANNEL_ID,
            "Reproducción de Música",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Control de reproducción musical"
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null) // Importante para que no interrumpa el audio
        }
        notificationManager.createNotificationChannel(channel)

        // ✅ USO DEL TAG: Confirmación de creación del canal
        Log.d(TAG, "📢 Canal de notificaciones configurado")
    }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {

        val player = mediaSession.player
        val metadata = player.mediaMetadata

        // 1. Obtener datos de la canción
        val titulo = metadata.title?.toString() ?: "FreePlayer"
        val artista = metadata.artist?.toString() ?: "Reproduciendo música"

        // ✅ USO DEL TAG: Depuración de metadatos
        Log.d(TAG, "🎵 Creando notificación para: $titulo - $artista (Playing: ${player.isPlaying})")

        // 2. Obtener la portada (Artwork)
        val albumArt = metadata.artworkData?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error decodificando artwork: ${e.message}")
                getDefaultArtwork()
            }
        } ?: getDefaultArtwork()

        // 3. Crear Acciones (Botones) usando la API de Media3 correctamente

        // Acción Anterior
        val prevAction = actionFactory.createMediaAction(
            mediaSession,
            IconCompat.createWithResource(context, R.drawable.ic_previous),
            "Anterior",
            Player.COMMAND_SEEK_TO_PREVIOUS
        )

        // Acción Play/Pause (Dinámica)
        val isPlaying = player.isPlaying
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pausar" else "Reproducir"

        val playPauseAction = actionFactory.createMediaAction(
            mediaSession,
            IconCompat.createWithResource(context, playPauseIcon),
            playPauseTitle,
            Player.COMMAND_PLAY_PAUSE
        )

        // Acción Siguiente
        val nextAction = actionFactory.createMediaAction(
            mediaSession,
            IconCompat.createWithResource(context, R.drawable.ic_next),
            "Siguiente",
            Player.COMMAND_SEEK_TO_NEXT
        )

        // 4. Configurar el Intent para abrir la App al tocar la notificación
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 5. Configurar el estilo Media3
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowActionsInCompactView(0, 1, 2) // Índices de botones: [Prev, Play, Next]

        // 6. Construir la notificación
        val builder = NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            // Datos básicos
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(albumArt)
            .setContentTitle(titulo)
            .setContentText(artista)
            .setSubText("FreePlayer")

            // Personalización visual
            .setColorized(true)
            .setColor(getNotificationColor())

            // Configuración Media
            .setStyle(mediaStyle)
            .setContentIntent(openAppPendingIntent)

            // Botón de cierre
            .setDeleteIntent(
                actionFactory.createMediaActionPendingIntent(
                    mediaSession,
                    Player.COMMAND_STOP.toLong()
                )
            )

            // Comportamiento del sistema
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)

        // Agregar las acciones al builder en orden
        prevAction.let { builder.addAction(it) }
        playPauseAction.let { builder.addAction(it) }
        nextAction.let { builder.addAction(it) }

        return MediaNotification(currentNotificationId, builder.build())
    }

    private fun getNotificationColor(): Int {
        return try {
            ContextCompat.getColor(context, R.color.purple_500)
        } catch (_: Exception) {
            0xFF6200EE.toInt()
        }
    }

    private fun getDefaultArtwork(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification)
            ?: createBitmap(64, 64).apply {
                eraseColor(0xFF6200EE.toInt())
            }
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        Log.d(TAG, "🎮 Comando personalizado recibido: $action")
        return false
    }
}