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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.example.freeplayerm.MainActivity
import com.example.freeplayerm.R
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.*

/**
 * Proveedor de notificaciones personalizado usando MediaStyle.
 *
 * ✅ Compatible con Android 13+ lockscreen
 * ✅ Integración nativa con Media3
 * ✅ Diseño personalizado con colores de tu marca
 * ✅ Actualización automática de metadatos
 * ✅ Funciona con Bluetooth, Android Auto, WearOS
 */
@UnstableApi
class CustomNotificationProvider(
    private val context: Context
) : MediaNotification.Provider {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var currentNotificationId = MusicService.NOTIFICATION_ID
    private val TAG = "CustomNotification"

    // Scope para actualizaciones en tiempo real
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var updateJob: Job? = null

    init {
        createNotificationChannel()
        Log.d(TAG, "✅ CustomNotificationProvider inicializado")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MusicService.CHANNEL_ID,
            "Reproducción de Música",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Control de reproducción musical"
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null) // Sin sonido para notificación de media
        }
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "📢 Canal de notificación creado")
    }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {

        val player = mediaSession.player
        val metadata = player.mediaMetadata

        // Obtener información de la canción
        val titulo = metadata.title?.toString() ?: "Canción desconocida"
        val artista = metadata.artist?.toString() ?: "Artista desconocido"

        Log.d(TAG, "🎵 Creando notificación para: $titulo - $artista")

        // Obtener artwork (portada del álbum)
        val albumArt = metadata.artworkData?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error decodificando artwork: ${e.message}")
                null
            }
        } ?: getDefaultArtwork()

        // Intent para abrir la app al tocar la notificación
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear acciones usando el actionFactory de Media3
        // Esto asegura que los comandos se envíen correctamente al player

        val playPauseAction = if (player.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pausar",
                actionFactory.createMediaAction(
                    mediaSession,
                    androidx.media3.session.MediaSession.ControllerInfo.LEGACY_CONTROLLER,
                    Player.COMMAND_PLAY_PAUSE,
                    Bundle.EMPTY
                )
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Reproducir",
                actionFactory.createMediaAction(
                    mediaSession,
                    androidx.media3.session.MediaSession.ControllerInfo.LEGACY_CONTROLLER,
                    Player.COMMAND_PLAY_PAUSE,
                    Bundle.EMPTY
                )
            )
        }

        val previousAction = NotificationCompat.Action(
            R.drawable.ic_previous,
            "Anterior",
            actionFactory.createMediaAction(
                mediaSession,
                androidx.media3.session.MediaSession.ControllerInfo.LEGACY_CONTROLLER,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Bundle.EMPTY
            )
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_next,
            "Siguiente",
            actionFactory.createMediaAction(
                mediaSession,
                androidx.media3.session.MediaSession.ControllerInfo.LEGACY_CONTROLLER,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Bundle.EMPTY
            )
        )

        // Construir notificación con MediaStyle
        val notification = NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            // --- CONTENIDO BÁSICO ---
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(albumArt)
            .setContentTitle(titulo)
            .setContentText(artista)
            .setSubText("FreePlayer") // Opcional: muestra el nombre de tu app

            // --- PERSONALIZACIÓN DE COLOR (TU MARCA) ---
            // Esto hace que la notificación use el color de tu app
            .setColorized(true)
            .setColor(getNotificationColor())

            // --- MEDIASTYLE (CRÍTICO PARA LOCKSCREEN Y ANDROID 13+) ---
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken.legacyToken)
                    .setShowActionsInCompactView(0, 1, 2) // Muestra 3 botones en vista compacta
                    .setShowCancelButton(false)
            )

            // --- ACCIONES (BOTONES) ---
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)

            // --- COMPORTAMIENTO ---
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(createStopPendingIntent()) // Al deslizar para quitar
            .setOngoing(player.isPlaying) // No se puede quitar si está reproduciendo
            .setOnlyAlertOnce(true) // No vibra/suena cada vez que se actualiza
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en lockscreen
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .build()

        Log.d(TAG, "✅ Notificación creada (Playing: ${player.isPlaying})")

        // Iniciar actualizaciones automáticas si está reproduciendo
        startProgressUpdates(player, onNotificationChangedCallback)

        return MediaNotification(currentNotificationId, notification)
    }

    /**
     * Obtiene el color de la notificación desde los recursos
     * Prioriza colores personalizados, fallback a colores del sistema
     */
    private fun getNotificationColor(): Int {
        return try {
            // Intenta obtener un color personalizado
            ContextCompat.getColor(context, R.color.purple_500)
        } catch (e: Exception) {
            // Fallback a un color por defecto
            try {
                ContextCompat.getColor(context, android.R.color.holo_purple)
            } catch (e: Exception) {
                // Último fallback
                0xFF6B4EFF.toInt() // Morado en hex
            }
        }
    }

    /**
     * Crea un PendingIntent para detener la reproducción
     * cuando el usuario desliza la notificación
     */
    private fun createStopPendingIntent(): PendingIntent {
        val stopIntent = Intent(context, MusicService::class.java).apply {
            action = "ACTION_STOP"
        }
        return PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Obtiene un artwork por defecto cuando la canción no tiene portada
     */
    private fun getDefaultArtwork(): Bitmap {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error cargando artwork por defecto: ${e.message}")
            // Crear un bitmap simple de 1x1 como fallback
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    /**
     * Inicia actualizaciones periódicas de la notificación
     * para reflejar el progreso de reproducción en tiempo real
     */
    private fun startProgressUpdates(
        player: Player,
        callback: MediaNotification.Provider.Callback
    ) {
        // Cancelar job anterior si existe
        updateJob?.cancel()

        if (player.isPlaying) {
            Log.d(TAG, "⏱️ Iniciando actualizaciones de progreso")
            updateJob = scope.launch {
                while (player.isPlaying) {
                    delay(1000) // Actualizar cada segundo
                    try {
                        callback.onNotificationChanged(this@CustomNotificationProvider)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error actualizando notificación: ${e.message}")
                    }
                }
                Log.d(TAG, "⏸️ Deteniendo actualizaciones de progreso")
            }
        }
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        Log.d(TAG, "🎮 Comando recibido: $action")
        return false // Retornar true si manejas comandos personalizados
    }

    /**
     * Limpieza de recursos cuando se destruye el provider
     */
    fun release() {
        updateJob?.cancel()
        scope.cancel()
        Log.d(TAG, "🔚 CustomNotificationProvider liberado")
    }
}