package com.example.freeplayerm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
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
 * SOLUCIÓN AL ERROR DE HILT:
 * MediaSessionService SÍ extiende de Service, pero Hilt a veces tiene problemas
 * reconociendo subclases de Service que no están en AndroidX directamente.
 *
 * OPCIONES DE SOLUCIÓN:
 * 1. Asegúrate de que tu versión de Hilt es compatible con Media3
 * 2. Verifica que tengas las dependencias correctas en build.gradle
 * 3. Si el error persiste, puede ser un problema de versiones
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
        const val CHANNEL_ID = "media_playback_channel" // Ahora es accesible públicamente
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        setupPlayerListeners()

        // Configuramos el proveedor de notificaciones personalizado
        // IMPORTANTE: Usar el constructor simple, NO .create()
        val notificationProvider = CustomNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)

        try {
            val pendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { intent ->
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            if (pendingIntent != null) {
                mediaSession.setSessionActivity(pendingIntent)
            } else {
                Log.w(TAG, "⚠️ No se pudo crear el PendingIntent para la sesión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error configurando SessionActivity: ${e.message}")
        }

        Log.d(TAG, "🎵 MusicService creado - Listener de sincronización activado")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "🎭 Sesión media solicitada por: ${controllerInfo.packageName}")
        return mediaSession
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                mediaItem?.let { item ->
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                        Log.d(TAG, "🔄 Transición detectada ($reason) -> Sincronizando")
                        iniciarSincronizacionCancion(item)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        cancionSyncService.cancelarSincronizacion()
                    }
                    Player.STATE_READY -> {
                        if (player.isPlaying) {
                            player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (!isPlaying) {
                    cancionSyncService.cancelarSincronizacion()
                } else {
                    player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
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
                    player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
                }
            }
        })
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
                Log.e(TAG, "💥 Error en sincronización: ${e.message}")
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
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Canal de notificación creado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando canal: ${e.message}")
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 Task removida - Evaluando detención")

        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🔚 Destruyendo MusicService")
        cancionSyncService.cancelarSincronizacion()
        syncJob?.cancel()
        mediaSession.release()

        if (player.playbackState == Player.STATE_IDLE) {
            player.release()
        }

        super.onDestroy()
    }
}