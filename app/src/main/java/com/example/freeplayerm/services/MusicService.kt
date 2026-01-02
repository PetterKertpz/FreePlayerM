package com.example.freeplayerm.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.freeplayerm.R
import com.example.freeplayerm.utils.MediaItemHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ✅ VERSIÓN FINAL - SIN DefaultActionFactory
 *
 * Esta versión crea una notificación básica inicialmente para cumplir con startForeground(), y
 * luego Media3 la actualiza automáticamente.
 */
@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject lateinit var player: Player

    private lateinit var mediaSession: MediaSession

    @Inject lateinit var songSyncService: SongSyncService

    @Inject lateinit var mediaItemHelper: MediaItemHelper

    private var notificationProvider: CustomNotificationProvider? = null

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "media_playback_channel"
        private const val TAG = "MusicService"
    }

    private var syncJob: Job? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🎵 ========== INICIANDO MusicService ==========")

        mediaSession = MediaSession.Builder(this, player).setId("FreePlayerSession").build()
        // 1. Crear el Provider
        notificationProvider = CustomNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider!!)
        Log.d(TAG, "🔥 Provider asignado: CustomNotificationProvider")

        // 2. Configurar Listeners
        setupPlayerListeners()
        Log.d(TAG, "✅ Listeners del Player configurados")

        // 3. Configurar Session Activity (Click en notificación)
        try {
            val sessionIntent =
                packageManager?.getLaunchIntentForPackage(packageName)?.let { intent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                }
            if (sessionIntent != null) {
                mediaSession.setSessionActivity(sessionIntent)
                Log.d(TAG, "✅ SessionActivity configurada")
            } else {
                Log.w(TAG, "⚠️ No se pudo crear SessionActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error configurando SessionActivity: ${e.message}", e)
        }

        // 4. ✅ CRÍTICO: INICIAR COMO FOREGROUND SERVICE
        iniciarComoForegroundService()

        Log.d(TAG, "✅ MusicService iniciado correctamente")
    }

    /**
     * ✅ MÉTODO CORREGIDO - Sin usar DefaultActionFactory
     *
     * Crea una notificación básica inicialmente, luego Media3 la actualiza automáticamente con el
     * CustomNotificationProvider cuando sea necesario.
     */
    private fun iniciarComoForegroundService() {
        try {
            Log.d(TAG, "🚀 Iniciando servicio en Foreground...")

            // Crear una notificación básica inicial
            val notificacionInicial = crearNotificacionBasica()

            // ✅ AQUÍ ESTÁ LA MAGIA: startForeground()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notificacionInicial,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                )
                Log.d(TAG, "✅ Foreground iniciado con FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK")
            } else {
                startForeground(NOTIFICATION_ID, notificacionInicial)
                Log.d(TAG, "✅ Foreground iniciado (pre-Android Q)")
            }

            Log.d(TAG, "🔔 Servicio en Foreground con notificación ID: $NOTIFICATION_ID")
            Log.d(TAG, "📢 Media3 actualizará la notificación automáticamente cuando reproduzcas")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO al iniciar foreground: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * ✅ Crea una notificación básica para cumplir con startForeground()
     *
     * Esta notificación es temporal - Media3 la reemplazará automáticamente con tu
     * CustomNotificationProvider cuando empieces a reproducir.
     */
    private fun crearNotificacionBasica(): Notification {
        // Intent para abrir la app al tocar la notificación
        val openAppIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val pendingIntent =
            if (openAppIntent != null) {
                PendingIntent.getActivity(
                    this,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            } else {
                null
            }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FreePlayer")
            .setContentText("Listo para reproducir música")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * 🕵️ MÉTODO DE DIAGNÓSTICO (mantenido de tu código)
     *
     * Este método es llamado por Media3 cuando actualiza la notificación. Si este log NO sale,
     * Media3 no sabe que estás reproduciendo.
     */
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        Log.d(TAG, "🔥 onUpdateNotification LLAMADO")
        Log.d(TAG, "   ├─ Foreground requerido: $startInForegroundRequired")
        Log.d(TAG, "   ├─ Player.isPlaying: ${player.isPlaying}")
        Log.d(TAG, "   ├─ MediaItem actual: ${player.currentMediaItem?.mediaMetadata?.title}")
        Log.d(TAG, "   └─ MediaItemCount: ${player.mediaItemCount}")

        // Delegamos al comportamiento normal
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        Log.d(TAG, "📱 Cliente conectado: ${controllerInfo.packageName}")
        return mediaSession
    }

    private fun setupPlayerListeners() {
        player.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val reasonText =
                        when (reason) {
                            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "AUTO"
                            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "SEEK"
                            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED ->
                                "PLAYLIST_CHANGED"
                            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "REPEAT"
                            else -> "UNKNOWN($reason)"
                        }

                    Log.d(TAG, "🎵 Transición de canción")
                    Log.d(TAG, "   ├─ Razón: $reasonText")
                    Log.d(TAG, "   └─ Canción: ${mediaItem?.mediaMetadata?.title}")

                    mediaItem?.let {
                        if (
                            reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                                reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
                        ) {
                            iniciarSincronizacionCancion(it)
                        }
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateText =
                        when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN($playbackState)"
                        }

                    Log.d(TAG, "🎬 Estado Playback cambió")
                    Log.d(TAG, "   ├─ Nuevo estado: $stateText")
                    Log.d(TAG, "   └─ IsPlaying: ${player.isPlaying}")

                    when (playbackState) {
                        Player.STATE_ENDED,
                        Player.STATE_IDLE -> {
                            songSyncService.cancelarSincronizacion()
                        }
                        Player.STATE_READY -> {
                            if (player.isPlaying) {
                                player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
                            }
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(TAG, "⏯️ IsPlaying cambió a: $isPlaying")

                    if (!isPlaying) {
                        songSyncService.cancelarSincronizacion()
                    } else {
                        player.currentMediaItem?.let { iniciarSincronizacionCancion(it) }
                    }
                }
            }
        )
    }

    private fun iniciarSincronizacionCancion(mediaItem: MediaItem) {
        syncJob?.cancel()
        syncJob =
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    Log.d(TAG, "🔄 Iniciando sincronización: ${mediaItem.mediaMetadata.title}")
                    val cancionConArtista = mediaItemHelper.obtenerConResiliencia(mediaItem)
                    if (cancionConArtista != null) {
                        songSyncService.sincronizarCancionAlReproducir(cancionConArtista)
                    } else {
                        Log.w(TAG, "⚠️ No se pudo obtener datos para sincronizar")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "💥 Error en sincronización: ${e.message}", e)
                }
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "⚡ onStartCommand recibido")
        Log.d(TAG, "   ├─ Action: ${intent?.action ?: "null"}")
        Log.d(TAG, "   ├─ Flags: $flags")
        Log.d(TAG, "   └─ StartId: $startId")

        when (intent?.action) {
            "ACTION_STOP" -> {
                Log.d(TAG, "🛑 Deteniendo servicio por ACTION_STOP")
                player.stop()
                stopSelf()
                return START_NOT_STICKY
            }
            "ACTION_PLAY" -> {
                Log.d(TAG, "▶️ Reproducir solicitado")
                if (player.mediaItemCount > 0) {
                    player.prepare()
                    player.play()
                }
            }
        }

        // Asegurar que el player esté listo si es necesario
        if (
            player.playWhenReady &&
                player.mediaItemCount > 0 &&
                player.playbackState == Player.STATE_IDLE
        ) {
            Log.d(TAG, "🔧 Preparando player automáticamente")
            player.prepare()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val shouldKeepRunning = player.isPlaying && player.mediaItemCount > 0

        if (!shouldKeepRunning) {
            player.stop()
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "MusicService onDestroy iniciado")

        syncJob?.cancel()
        songSyncService.limpiar()

        // 1. Detener reproducción primero
        player.stop()
        player.clearMediaItems()

        // 2. Liberar MediaSession (internamente desvincula el player)
        mediaSession.release()

        // 3. Liberar Player AL FINAL
        player.release()

        super.onDestroy()
        Log.d(TAG, "MusicService destruido")
    }
}
