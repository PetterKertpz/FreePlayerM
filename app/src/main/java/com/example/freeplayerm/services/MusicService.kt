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
import com.example.freeplayerm.com.example.freeplayerm.services.CancionSyncService
import com.example.freeplayerm.utils.MediaItemHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val syncScope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "media_playback_channel"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Configurar el listener para detectar cambios de canción
        setupPlayerListeners()

        // --- ✅ USAMOS NUESTRO NUEVO Y SIMPLE PROVEEDOR ---
        val notificationProvider = CustomNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)

        // El resto de la configuración se mantiene igual
        val pendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSession.setSessionActivity(pendingIntent!!)

        Log.d(TAG, "🎵 MusicService creado - Listener de sincronización activado")
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                mediaItem?.let { item ->
                    // Solo sincronizar en transiciones automáticas o por seek
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {

                        Log.d(TAG, "🔄 Transición de canción detectada - Razón: $reason")
                        iniciarSincronizacionCancion(item)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                Log.d(TAG, "🎛️ Estado de reproducción cambiado: $playbackState")

                when (playbackState) {
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        // Cuando termina la reproducción, cancelar sincronización
                        Log.d(TAG, "⏹️ Reproducción terminada - Cancelando sincronización")
                        cancionSyncService.cancelarSincronizacion()
                    }
                    Player.STATE_READY -> {
                        // Cuando está listo para reproducir, verificar si necesitamos sincronizar
                        if (player.isPlaying) {
                            Log.d(TAG, "▶️ Reproducción lista y en curso - Verificando sincronización")
                            player.currentMediaItem?.let { item ->
                                iniciarSincronizacionCancion(item)
                            }
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "⏳ Buffering - Manteniendo sincronización activa")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                Log.d(TAG, "🎭 Estado de reproducción cambiado: ${if (isPlaying) "PLAYING" else "PAUSED"}")

                if (!isPlaying) {
                    // Cuando se pausa, cancelar sincronización
                    Log.d(TAG, "⏸️ Reproducción pausada - Cancelando sincronización")
                    cancionSyncService.cancelarSincronizacion()
                } else {
                    // Cuando se reanuda, sincronizar si es necesario
                    Log.d(TAG, "▶️ Reproducción reanudada - Verificando sincronización")
                    player.currentMediaItem?.let { item ->
                        iniciarSincronizacionCancion(item)
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)

                // Manejar cambios de posición (skips, seeks)
                if (reason == Player.DISCONTINUITY_REASON_SEEK ||
                    reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                    Log.d(TAG, "⏩ Seek detectado - Re-evaluando sincronización")
                    player.currentMediaItem?.let { item ->
                        iniciarSincronizacionCancion(item)
                    }
                }
            }
        })
    }

    /**
     * Inicia la sincronización de una canción de manera robusta
     */
    private fun iniciarSincronizacionCancion(mediaItem: MediaItem) {
        // Cancelar cualquier sincronización anterior
        syncJob?.cancel()

        // Iniciar nueva sincronización
        syncJob = syncScope.launch {
            try {
                Log.d(TAG, "🔄 Iniciando proceso de sincronización...")

                // Obtener datos de la canción usando el helper robusto
                val cancionConArtista = mediaItemHelper.obtenerDatosCancionConResiliencia(mediaItem)

                if (cancionConArtista != null) {
                    Log.d(TAG, "✅ Datos obtenidos - Sincronizando: ${cancionConArtista.cancion.titulo}")
                    cancionSyncService.sincronizarCancionAlReproducir(cancionConArtista)
                    Log.d(TAG, "🚀 Sincronización iniciada para: ${cancionConArtista.cancion.titulo}")
                } else {
                    Log.w(TAG, "⚠️ No se pudieron obtener datos para sincronización")

                    // Debug detallado para entender el problema
                    val debugInfo = mediaItemHelper.debugMediaItem(mediaItem)
                    Log.d(TAG, "🔍 Debug Info:\n$debugInfo")

                    // Intentar con datos mínimos como último recurso
                    val datosMinimos = mediaItemHelper.extraerDatosBusquedaBasicos(mediaItem)
                    if (datosMinimos != null) {
                        Log.d(TAG, "🆘 Usando datos mínimos para búsqueda: '${datosMinimos.first}' - '${datosMinimos.second}'")
                        // Aquí podrías llamar a un método alternativo si lo tienes
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "💥 Error en proceso de sincronización: ${e.message}", e)
            }
        }
    }

    /**
     * Obtiene información de la canción actual para logging
     */
    private fun obtenerInfoCancionActual(): String {
        return try {
            val currentItem = player.currentMediaItem
            val metadata = currentItem?.mediaMetadata

            """
            🎵 Canción Actual:
            ID: ${currentItem?.mediaId ?: "N/A"}
            Título: ${metadata?.title ?: "N/A"}
            Artista: ${metadata?.artist ?: "N/A"}
            Álbum: ${metadata?.albumTitle ?: "N/A"}
            Estado: ${player.playbackState}
            Reproduciendo: ${player.isPlaying}
            """.trimIndent()
        } catch (e: Exception) {
            "❌ Error obteniendo info: ${e.message}"
        }
    }

    private fun createNotificationChannel() {
        try {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para notificaciones de reproducción de música"
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "📱 Canal de notificación creado: $CHANNEL_ID")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando canal de notificación: ${e.message}")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        Log.d(TAG, "🎭 Sesión media solicitada por: ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "📱 Task removida - Verificando si detener servicio")

        if (!player.playWhenReady || player.mediaItemCount == 0) {
            Log.d(TAG, "🛑 No hay reproducción activa - Deteniendo servicio")
            cancionSyncService.cancelarSincronizacion()
            stopSelf()
        } else {
            Log.d(TAG, "🎵 Reproducción activa - Manteniendo servicio")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🔚 Destruyendo MusicService")

        // Limpieza ordenada
        cancionSyncService.cancelarSincronizacion()
        syncJob?.cancel()

        mediaSession.release()
        player.release()

        Log.d(TAG, "✅ MusicService destruido correctamente")
        super.onDestroy()
    }

    /**
     * Método para debugging del estado actual del servicio
     */
    fun debugEstadoActual(): String {
        return """
        🎵 MusicService Debug:
        ✅ Servicio activo: ${isRunning()}
        🔊 Player estado: ${player.playbackState}
        ▶️ Reproduciendo: ${player.isPlaying}
        📊 Media items: ${player.mediaItemCount}
        🔄 Sincronizando: ${cancionSyncService.estaSincronizando()}
        ${obtenerInfoCancionActual()}
        """.trimIndent()
    }

    private fun isRunning(): Boolean {
        return try {
            // Verificar si el servicio está corriendo
            true // Simplificado - podrías usar un flag real
        } catch (e: Exception) {
            false
        }
    }
}