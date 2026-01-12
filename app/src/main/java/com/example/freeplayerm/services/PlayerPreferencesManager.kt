package com.example.freeplayerm.services

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import com.example.freeplayerm.utils.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

// Estado del streaming
sealed class StreamingState {
   data object Allowed : StreamingState()

   data class Blocked(val reason: String) : StreamingState()
}

@Singleton
class PlayerPreferencesManager
@Inject
constructor(
   @ApplicationContext private val context: Context,
   private val sessionRepository: SessionRepository,
   private val preferencesRepository: UserPreferencesRepository,
   private val networkMonitor: NetworkMonitor,
) {
   private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

   private var player: ExoPlayer? = null
   private var loudnessEnhancer: LoudnessEnhancer? = null
   private var crossfadeJob: Job? = null

   // Estado actual de las preferencias
   private val _currentPreferences = MutableStateFlow<UserPreferencesEntity?>(null)
   val currentPreferences: StateFlow<UserPreferencesEntity?> = _currentPreferences.asStateFlow()

   // Estado del streaming
   private val _streamingState = MutableStateFlow<StreamingState>(StreamingState.Allowed)
   val streamingState: StateFlow<StreamingState> = _streamingState.asStateFlow()

   // Crossfade en progreso
   private val _crossfadeProgress = MutableStateFlow(0f)
   val crossfadeProgress: StateFlow<Float> = _crossfadeProgress.asStateFlow()

   init {
      observePreferences()
      observeNetwork()
   }

   // Vincular el player existente
   fun attachPlayer(exoPlayer: ExoPlayer) {
      player = exoPlayer
      setupAudioSession(exoPlayer)
      applyCurrentPreferences()
      Log.d(TAG, "Player vinculado a PlayerPreferencesManager")
   }

   fun detachPlayer() {
      releaseAudioEffects()
      player = null
      Log.d(TAG, "Player desvinculado")
   }

   private fun observePreferences() {
      scope.launch {
         sessionRepository.idDeUsuarioActivo
            .filterNotNull()
            .flatMapLatest { userId -> preferencesRepository.obtenerPreferenciasPorIdFlow(userId) }
            .collectLatest { prefs ->
               prefs?.let {
                  _currentPreferences.value = it
                  applyPreferences(it)
               }
            }
      }
   }

   private fun observeNetwork() {
      scope.launch {
         networkMonitor.networkStatus.collectLatest { status -> checkStreamingAllowed() }
      }
   }

   private fun applyCurrentPreferences() {
      _currentPreferences.value?.let { applyPreferences(it) }
   }

   private fun applyPreferences(prefs: UserPreferencesEntity) {
      Log.d(
         TAG,
         "Aplicando preferencias: crossfade=${prefs.crossfadeMs}ms, " +
            "normalizar=${prefs.normalizarVolumen}, calidad=${prefs.calidadPreferida}",
      )

      // Aplicar normalización de volumen
      applyVolumeNormalization(prefs.normalizarVolumen)

      // Verificar streaming permitido
      checkStreamingAllowed()

      // El crossfade se maneja en la transición de canciones
   }

   private fun setupAudioSession(exoPlayer: ExoPlayer) {
      val audioAttributes =
         AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

      exoPlayer.setAudioAttributes(audioAttributes, true)
   }

   // Normalización de volumen usando LoudnessEnhancer
   @OptIn(UnstableApi::class)
   private fun applyVolumeNormalization(isEnabled: Boolean) {
      val currentPlayer = player ?: return

      try {
         if (isEnabled) {
            if (loudnessEnhancer == null) {
               val audioSessionId = currentPlayer.audioSessionId
               if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                  loudnessEnhancer =
                     LoudnessEnhancer(audioSessionId).apply {
                        setTargetGain(300) // +3dB target (en millibels)
                        setEnabled(true)
                     }
                  Log.d(TAG, "Normalización de volumen ACTIVADA")
               }
            } else {
               loudnessEnhancer?.setEnabled(true)
            }
         } else {
            loudnessEnhancer?.setEnabled(false)
            Log.d(TAG, "Normalización de volumen DESACTIVADA")
         }
      } catch (e: Exception) {
         Log.e(TAG, "Error configurando normalización: ${e.message}")
      }
   }

   // Crossfade entre canciones
   fun performCrossfade(fromVolume: Float = 1f, toVolume: Float = 1f, onComplete: () -> Unit = {}) {
      val crossfadeMs = _currentPreferences.value?.crossfadeMs ?: 0
      if (crossfadeMs <= 0) {
         onComplete()
         return
      }

      crossfadeJob?.cancel()
      crossfadeJob =
         scope.launch {
            val steps = 20
            val stepDuration = crossfadeMs.toLong() / steps

            for (i in 0..steps) {
               val progress = i.toFloat() / steps
               _crossfadeProgress.value = progress

               // Fade out de la canción actual
               val currentVolume = fromVolume * (1 - progress)
               player?.volume = currentVolume

               delay(stepDuration)
            }

            // Restaurar volumen para la siguiente canción
            player?.volume = toVolume
            _crossfadeProgress.value = 0f
            onComplete()
         }
   }

   // Verificar si el streaming está permitido
   private fun checkStreamingAllowed() {
      val prefs = _currentPreferences.value
      val soloWifi = prefs?.soloWifiStreaming ?: false

      _streamingState.value =
         when {
            !networkMonitor.isConnected() -> StreamingState.Blocked("Sin conexión a internet")
            soloWifi && !networkMonitor.isWifiConnected() ->
               StreamingState.Blocked("Streaming solo permitido en WiFi")
            else -> StreamingState.Allowed
         }
   }

   // Obtener calidad según conexión actual
   fun getQualityForCurrentNetwork(): String {
      val prefs = _currentPreferences.value ?: return UserPreferencesEntity.CALIDAD_MEDIA
      val isWifi = networkMonitor.isWifiConnected()
      return prefs.obtenerCalidadSegunConexion(isWifi)
   }

   // Verificar si puede reproducir
   fun canPlay(): Boolean = _streamingState.value is StreamingState.Allowed

   // Obtener mensaje de bloqueo si existe
   fun getBlockedReason(): String? {
      return (_streamingState.value as? StreamingState.Blocked)?.reason
   }

   private fun releaseAudioEffects() {
      try {
         loudnessEnhancer?.release()
         loudnessEnhancer = null
      } catch (e: Exception) {
         Log.e(TAG, "Error liberando efectos de audio: ${e.message}")
      }
      crossfadeJob?.cancel()
   }

   companion object {
      private const val TAG = "PlayerPrefsManager"
   }
}
