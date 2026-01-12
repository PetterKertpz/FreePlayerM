// en: app/src/main/java/com/example/freeplayerm/data/purification/PlaybackEnrichmentHelper.kt
package com.example.freeplayerm.data.purification

import android.util.Log
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_API_NOT_FOUND
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_CLEANED_LOCAL
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_DIRTY
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_ENRICHED
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_FAILED
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_PARTIAL_VERIFIED
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_REFINED
import com.example.freeplayerm.data.local.entity.SongEntity.Companion.STATUS_VERIFIED
import com.example.freeplayerm.data.repository.GeniusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 PLAYBACK ENRICHMENT HELPER
 *
 * Helper para enriquecer canciones durante la reproducción.
 * Se integra con el reproductor para mejorar metadata on-demand.
 *
 * Características:
 * - Enriquecimiento no bloqueante (no interrumpe reproducción)
 * - Rate limiting para evitar spam de requests
 * - Estado observable para UI
 * - Respeta configuración del pipeline
 *
 * Uso en ViewModel:
 * ```kotlin
 * class PlayerViewModel @Inject constructor(
 *     private val enrichmentHelper: PlaybackEnrichmentHelper,
 *     ...
 * ) {
 *     fun onSongStarted(song: SongEntity) {
 *         enrichmentHelper.onSongPlay(song)
 *     }
 * }
 * ```
 *
 * @version 1.0
 */
@Singleton
class PlaybackEnrichmentHelper @Inject constructor(
   private val geniusRepository: GeniusRepository,
) {
   companion object {
      private const val TAG = "PlaybackEnrichment"
      
      // Evitar enriquecer la misma canción muy seguido
      private const val MIN_INTERVAL_SAME_SONG_MS = 60_000L // 1 minuto
      
      // Evitar spam de requests generales
      private const val MIN_INTERVAL_ANY_MS = 5_000L // 5 segundos
   }
   
   private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
   private var currentJob: Job? = null
   
   // Tracking de última canción y tiempo
   private var lastEnrichedSongId: Int? = null
   private var lastEnrichmentTime: Long = 0L
   
   // ==================== ESTADO OBSERVABLE ====================
   
   sealed class EnrichmentState {
      data object Idle : EnrichmentState()
      data class Enriching(val songTitle: String) : EnrichmentState()
      data class Success(val songTitle: String, val newScore: Int) : EnrichmentState()
      data class Skipped(val reason: String) : EnrichmentState()
      data class Failed(val songTitle: String, val error: String) : EnrichmentState()
   }
   
   private val _state = MutableStateFlow<EnrichmentState>(EnrichmentState.Idle)
   val state: StateFlow<EnrichmentState> = _state.asStateFlow()
   
   private val _isEnriching = MutableStateFlow(false)
   val isEnriching: StateFlow<Boolean> = _isEnriching.asStateFlow()
   
   // Estadísticas de sesión
   private var sessionEnriched = 0
   private var sessionSkipped = 0
   private var sessionFailed = 0
   
   data class SessionStats(
      val enriched: Int,
      val skipped: Int,
      val failed: Int
   ) {
      val total: Int get() = enriched + skipped + failed
   }
   
   fun getSessionStats() = SessionStats(sessionEnriched, sessionSkipped, sessionFailed)
   
   // ==================== API PRINCIPAL ====================
   
   /**
    * Llamar cuando una canción empieza a reproducirse.
    * Verifica si necesita enriquecimiento y lo ejecuta en background.
    *
    * @param song La canción que está por reproducirse
    */
   fun onSongPlay(song: SongEntity) {
      // Verificar si el enriquecimiento está habilitado
      if (!MetadataPipelineConfig.ENRICH_ON_PLAY) {
         Log.d(TAG, "Enriquecimiento al reproducir deshabilitado")
         return
      }
      
      // Verificar rate limiting
      val now = System.currentTimeMillis()
      
      // Misma canción muy pronto
      if (song.idCancion == lastEnrichedSongId &&
         (now - lastEnrichmentTime) < MIN_INTERVAL_SAME_SONG_MS) {
         Log.d(TAG, "⏩ '${song.titulo}' - rate limited (misma canción)")
         return
      }
      
      // Cualquier canción muy pronto
      if ((now - lastEnrichmentTime) < MIN_INTERVAL_ANY_MS) {
         Log.d(TAG, "⏩ '${song.titulo}' - rate limited (general)")
         return
      }
      
      // Verificar si necesita enriquecimiento
      if (!song.necesitaEnriquecimiento()) {
         Log.d(TAG, "✅ '${song.titulo}' ya enriquecida (status=${song.metadataStatus}, score=${song.confidenceScore})")
         _state.value = EnrichmentState.Skipped("Ya enriquecida")
         sessionSkipped++
         return
      }
      
      // Verificar si puede reintentar
      if (!song.puedeReintentarEnriquecimiento()) {
         Log.d(TAG, "⏩ '${song.titulo}' - en período de espera para reintento")
         _state.value = EnrichmentState.Skipped("Esperando reintento")
         sessionSkipped++
         return
      }
      
      // Cancelar job anterior si existe
      currentJob?.cancel()
      
      // Iniciar enriquecimiento
      currentJob = scope.launch {
         enrichSong(song)
      }
   }
   
   /**
    * Fuerza el enriquecimiento de una canción (ignora rate limiting)
    */
   fun forceEnrich(song: SongEntity) {
      currentJob?.cancel()
      currentJob = scope.launch {
         enrichSong(song, force = true)
      }
   }
   
   /**
    * Cancela cualquier enriquecimiento en progreso
    */
   fun cancel() {
      currentJob?.cancel()
      _isEnriching.value = false
      _state.value = EnrichmentState.Idle
   }
   
   /**
    * Resetea estadísticas de sesión
    */
   fun resetSessionStats() {
      sessionEnriched = 0
      sessionSkipped = 0
      sessionFailed = 0
   }
   
   // ==================== IMPLEMENTACIÓN PRIVADA ====================
   
   private suspend fun enrichSong(song: SongEntity, force: Boolean = false) {
      _isEnriching.value = true
      _state.value = EnrichmentState.Enriching(song.titulo)
      
      val startTime = System.currentTimeMillis()
      
      try {
         Log.d(TAG, "🎵 Enriqueciendo: '${song.titulo}'")
         
         // Usar el método del repositorio
         geniusRepository.sincronizarCancionAlReproducir(song)
         
         // Actualizar tracking
         lastEnrichedSongId = song.idCancion
         lastEnrichmentTime = System.currentTimeMillis()
         
         val duration = System.currentTimeMillis() - startTime
         
         // Nota: No tenemos acceso al score actualizado aquí directamente
         // El repositorio ya actualizó la BD
         Log.d(TAG, "✅ '${song.titulo}' enriquecida en ${duration}ms")
         
         _state.value = EnrichmentState.Success(song.titulo, 0) // Score se obtiene de BD
         sessionEnriched++
         
      } catch (e: Exception) {
         Log.e(TAG, "❌ Error enriqueciendo '${song.titulo}': ${e.message}")
         _state.value = EnrichmentState.Failed(song.titulo, e.message ?: "Error desconocido")
         sessionFailed++
      } finally {
         _isEnriching.value = false
      }
   }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension para verificar rápidamente si una canción debería enriquecerse al reproducir
 */
fun SongEntity.shouldEnrichOnPlay(): Boolean {
   if (!MetadataPipelineConfig.ENRICH_ON_PLAY) return false
   if (!necesitaEnriquecimiento()) return false
   if (!puedeReintentarEnriquecimiento()) return false
   return true
}

/**
 * Extension para obtener descripción del estado de metadata
 */
fun SongEntity.getMetadataStatusDescription(): String {
   return when (metadataStatus) {
      STATUS_DIRTY -> "Sin procesar"
      STATUS_CLEANED_LOCAL -> "Limpieza local"
      STATUS_ENRICHED -> "Enriquecida"
      STATUS_REFINED -> "Refinada"
      STATUS_VERIFIED -> "Verificada ✓"
      STATUS_PARTIAL_VERIFIED -> "Parcialmente verificada"
      STATUS_API_NOT_FOUND -> "No encontrada en API"
      STATUS_FAILED -> "Error en procesamiento"
      else -> metadataStatus
   }
}

/**
 * Extension para obtener emoji del nivel de calidad
 */
fun SongEntity.getQualityEmoji(): String {
   return when (obtenerNivelCalidad()) {
      SongEntity.QualityLevel.EXCELLENT -> "⭐"
      SongEntity.QualityLevel.GOOD -> "✅"
      SongEntity.QualityLevel.FAIR -> "📝"
      SongEntity.QualityLevel.POOR -> "⚠️"
      SongEntity.QualityLevel.BAD -> "❌"
   }
}