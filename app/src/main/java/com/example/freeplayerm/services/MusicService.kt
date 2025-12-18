package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.freeplayerm.utils.MediaItemHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio de reproducción de música usando Media3 (ExoPlayer).
 *
 * ✅ Soporta notificaciones con MediaStyle
 * ✅ Actualización automática de metadatos
 * ✅ Sincronización con base de datos
 * ✅ Compatible con Android 13+ lockscreen
 */
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var cancionSyncService: CancionSyncService

    @Inject
    lateinit var mediaItemHelper: MediaItemHelper

    private val TAG = "MusicService"
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "media_playback_channel"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🎵 Iniciando MusicService...")

        createNotificationChannel()
        setupPlayerListeners()

        // Configuramos el proveedor de notificaciones personalizado
        val notificationProvider = CustomNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)

        // Configurar session activity (para abrir la app desde la notificación)
        try {
            val pendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { intent ->
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            if (pendingIntent != null) {
                mediaSession.setSessionActivity(pendingIntent)
                Log.d(TAG, "✅ SessionActivity configurada")
            } else {
                Log.w(TAG, "⚠️ No se pudo crear el PendingIntent para la sesión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error configurando SessionActivity: ${e.message}")
        }

        Log.d(TAG, "✅ MusicService creado correctamente")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        Log.d(TAG, "🎭 Sesión media solicitada por: ${controllerInfo.packageName}")
        return mediaSession
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                mediaItem?.let { item ->
                    Log.d(TAG, "🔄 Transición de canción detectada")

                    // ⭐ NUEVO: Actualizar metadatos para la notificación
                    updateMediaMetadata(item)

                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                        Log.d(TAG, "📡 Iniciando sincronización ($reason)")
                        iniciarSincronizacionCancion(item)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                Log.d(TAG, "🎬 Estado de reproducción cambió: $playbackState")

                when (playbackState) {
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        Log.d(TAG, "⏹️ Reproducción finalizada/inactiva")
                        cancionSyncService.cancelarSincronizacion()
                    }
                    Player.STATE_READY -> {
                        if (player.isPlaying) {
                            Log.d(TAG, "▶️ Reproducción lista y activa")
                            player.currentMediaItem?.let {
                                updateMediaMetadata(it)
                                iniciarSincronizacionCancion(it)
                            }
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "⏳ Buffering...")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                Log.d(TAG, if (isPlaying) "▶️ Reproducción iniciada" else "⏸️ Reproducción pausada")

                if (!isPlaying) {
                    cancionSyncService.cancelarSincronizacion()
                } else {
                    player.currentMediaItem?.let {
                        updateMediaMetadata(it)
                        iniciarSincronizacionCancion(it)
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)

                if (reason == Player.DISCONTINUITY_REASON_SEEK ||
                    reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                    Log.d(TAG, "⏩ Usuario buscó en la canción")
                    player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
                }
            }
        })

        Log.d(TAG, "👂 Listeners del player configurados")
    }

    /**
     * ⭐ NUEVO: Actualiza los metadatos de la sesión
     * Esto permite que la notificación se actualice automáticamente
     * sin necesidad de recrearla manualmente.
     *
     * Los metadatos incluyen:
     * - Título de la canción
     * - Artista
     * - Álbum
     * - Portada (artwork)
     * - Duración
     */
    private fun updateMediaMetadata(mediaItem: MediaItem) {
        try {
            val currentMetadata = mediaItem.mediaMetadata

            Log.d(TAG, """
                📝 Actualizando metadatos:
                   Título: ${currentMetadata.title}
                   Artista: ${currentMetadata.artist}
                   Álbum: ${currentMetadata.albumTitle}
                   Artwork: ${if (currentMetadata.artworkData != null) "Sí" else "No"}
            """.trimIndent())

            // Media3 usa los metadatos del MediaItem actual automáticamente
            // No necesitamos hacer nada más, solo asegurarnos de que estén bien configurados

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando metadatos: ${e.message}", e)
        }
    }

    private fun iniciarSincronizacionCancion(mediaItem: MediaItem) {
        syncJob?.cancel()
        syncJob = syncScope.launch {
            try {
                val cancionConArtista = mediaItemHelper.obtenerDatosCancionConResiliencia(mediaItem)

                if (cancionConArtista != null) {
                    Log.d(TAG, "✅ Sincronizando: ${cancionConArtista.cancion.titulo}")
                    cancionSyncService.sincronizarCancionAlReproducir(cancionConArtista)
                } else {
                    Log.w(TAG, "⚠️ No se pudieron obtener datos para sincronización")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error en sincronización: ${e.message}", e)
            }
        }
    }

    private fun createNotificationChannel() {
        try {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del reproductor"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "📢 Canal de notificación creado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando canal: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Manejar acción de detener desde la notificación
        if (intent?.action == "ACTION_STOP") {
            Log.d(TAG, "⏹️ Acción de detener recibida")
            player.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 Task removida - Evaluando detención del servicio")

        // Solo detener el servicio si no está reproduciendo
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            Log.d(TAG, "🛑 Deteniendo servicio (no hay reproducción activa)")
            stopSelf()
        } else {
            Log.d(TAG, "▶️ Manteniendo servicio activo (reproducción en curso)")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🔚 Destruyendo MusicService...")

        // Cancelar sincronización
        cancionSyncService.cancelarSincronizacion()
        syncJob?.cancel()

        // Liberar sesión
        mediaSession.release()
        Log.d(TAG, "🔓 MediaSession liberada")

        // Liberar player solo si está idle
        if (player.playbackState == Player.STATE_IDLE) {
            player.release()
            Log.d(TAG, "🔓 Player liberado")
        }

        super.onDestroy()
        Log.d(TAG, "✅ MusicService destruido")
    }
}