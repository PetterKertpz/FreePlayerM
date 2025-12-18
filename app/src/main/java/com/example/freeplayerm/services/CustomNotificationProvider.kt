package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
    private val TAG = "CustomNotification"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Solo necesario para Android 8.0 (Oreo) en adelante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }
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

        // 2. Obtener la portada (Artwork)
        val albumArt = metadata.artworkData?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
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

        // 5. Configurar el estilo Media3 (Reemplaza al estilo Legacy)
        // Esto vincula automáticamente el token de sesión y habilita controles en Lockscreen
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowActionsInCompactView(0, 1, 2) // Índices de botones: [Prev, Play, Next]

        // 6. Construir la notificación
        val builder = NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            // Datos básicos
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(albumArt)
            .setContentTitle(titulo)
            .setContentText(artista)
            .setSubText("FreePlayer") // Opcional

            // Personalización visual
            .setColorized(true)
            .setColor(getNotificationColor())

            // Configuración Media
            .setStyle(mediaStyle)
            .setContentIntent(openAppPendingIntent)

            // Botón de cierre (deslizar o X en algunas versiones)
            .setDeleteIntent(
                actionFactory.createMediaActionPendingIntent(mediaSession,
                    Player.COMMAND_STOP.toLong()
                )
            )

            // Comportamiento del sistema
            .setOngoing(isPlaying) // No se puede quitar si está sonando
            .setOnlyAlertOnce(true) // Evita vibración constante al actualizar
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en pantalla de bloqueo
            .setPriority(NotificationCompat.PRIORITY_LOW) // Baja prioridad para evitar sonido de notificación
            .setAutoCancel(false)

        // Agregar las acciones al builder en orden (Prev -> Play -> Next)
        prevAction?.let { builder.addAction(it) }
        playPauseAction?.let { builder.addAction(it) }
        nextAction?.let { builder.addAction(it) }

        // Retornar la notificación construida
        return MediaNotification(currentNotificationId, builder.build())
    }

    /**
     * Obtiene el color de marca o un fallback
     */
    private fun getNotificationColor(): Int {
        return try {
            ContextCompat.getColor(context, R.color.purple_500)
        } catch (e: Exception) {
            // Color de respaldo (Morado)
            0xFF6200EE.toInt()
        }
    }

    /**
     * Obtiene una imagen por defecto si no hay portada
     */
    private fun getDefaultArtwork(): Bitmap {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification)
        } catch (e: Exception) {
            // Bitmap vacío 1x1 para evitar crash
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        // Aquí puedes manejar comandos extra si los necesitas
        return false
    }
}