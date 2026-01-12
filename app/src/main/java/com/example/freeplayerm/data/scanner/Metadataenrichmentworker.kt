// en: app/src/main/java/com/example/freeplayerm/data/scanner/MetadataEnrichmentWorker.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.data.purification.MetadataPipelineConfig
import com.example.freeplayerm.data.repository.GeniusRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 🎵 METADATA ENRICHMENT WORKER
 *
 * Worker de WorkManager para enriquecimiento de metadata en background.
 * Se ejecuta después del escaneo o de forma periódica para:
 * - Buscar canciones con metadata incompleta
 * - Enriquecer con datos de Genius API
 * - Calcular confidence scores
 * - Actualizar estados del pipeline
 *
 * Características:
 * - Rate limiting automático (respeta límites de API)
 * - Retry con backoff exponencial
 * - Constraints inteligentes (WiFi, batería, idle)
 * - Progreso reportado a UI
 *
 * @version 1.0
 */
@HiltWorker
class MetadataEnrichmentWorker
@AssistedInject
constructor(
   @Assisted context: Context,
   @Assisted params: WorkerParameters,
   private val songDao: SongDao,
   private val geniusRepository: GeniusRepository,
) : CoroutineWorker(context, params) {
   
   companion object {
      private const val TAG = "EnrichmentWorker"
      
      // Work names
      const val WORK_NAME_IMMEDIATE = "metadata_enrichment_immediate"
      const val WORK_NAME_PERIODIC = "metadata_enrichment_periodic"
      const val WORK_NAME_POST_SCAN = "metadata_enrichment_post_scan"
      
      // Tags
      const val TAG_ENRICHMENT = "metadata_enrichment"
      const val TAG_IMMEDIATE = "enrichment_immediate"
      const val TAG_PERIODIC = "enrichment_periodic"
      
      // Input keys
      const val KEY_BATCH_SIZE = "batch_size"
      const val KEY_PRIORITY_MODE = "priority_mode"
      const val KEY_MAX_ATTEMPTS = "max_attempts"
      const val KEY_TRIGGERED_BY = "triggered_by"
      
      // Output keys
      const val KEY_RESULT_ENRICHED = "result_enriched"
      const val KEY_RESULT_FAILED = "result_failed"
      const val KEY_RESULT_SKIPPED = "result_skipped"
      const val KEY_RESULT_TOTAL = "result_total"
      const val KEY_RESULT_TIME_MS = "result_time_ms"
      const val KEY_ERROR_MESSAGE = "error_message"
      
      // Progress keys
      const val KEY_PROGRESS_CURRENT = "progress_current"
      const val KEY_PROGRESS_TOTAL = "progress_total"
      const val KEY_PROGRESS_MESSAGE = "progress_message"
      const val KEY_CURRENT_SONG = "current_song"
      
      // Triggered by values
      const val TRIGGERED_SCAN = "scan"
      const val TRIGGERED_MANUAL = "manual"
      const val TRIGGERED_PERIODIC = "periodic"
      const val TRIGGERED_PLAY = "play"
      
      // Config
      private const val DEFAULT_BATCH_SIZE = 50
      private const val DEFAULT_PERIODIC_HOURS = 12L
      private const val FLEX_MINUTES = 60L
      private const val MAX_RETRY_ATTEMPTS = 2
      private const val INITIAL_BACKOFF_MINUTES = 5L
      
      /**
       * Ejecuta enriquecimiento inmediato
       * Ideal para después de un escaneo o petición manual
       */
      fun ejecutarInmediato(
         context: Context,
         batchSize: Int = DEFAULT_BATCH_SIZE,
         triggeredBy: String = TRIGGERED_MANUAL
      ) {
         if (!MetadataPipelineConfig.ENABLE_AUTO_ENRICHMENT) {
            Log.d(TAG, "Auto-enrichment deshabilitado en config")
            return
         }
         
         val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
         
         val inputData = workDataOf(
            KEY_BATCH_SIZE to batchSize,
            KEY_TRIGGERED_BY to triggeredBy,
            KEY_MAX_ATTEMPTS to MetadataPipelineConfig.MAX_ENRICHMENT_ATTEMPTS
         )
         
         val workRequest = OneTimeWorkRequestBuilder<MetadataEnrichmentWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
               BackoffPolicy.EXPONENTIAL,
               INITIAL_BACKOFF_MINUTES,
               TimeUnit.MINUTES
            )
            .addTag(TAG_ENRICHMENT)
            .addTag(TAG_IMMEDIATE)
            .build()
         
         WorkManager.getInstance(context)
            .enqueueUniqueWork(
               WORK_NAME_IMMEDIATE,
               ExistingWorkPolicy.REPLACE,
               workRequest
            )
         
         Log.d(TAG, "Enriquecimiento inmediato encolado (batch=$batchSize, by=$triggeredBy)")
      }
      
      /**
       * Ejecuta enriquecimiento después de escaneo
       * Se encola con delay para no saturar recursos
       */
      fun ejecutarPostEscaneo(
         context: Context,
         cancionesNuevas: Int,
         delayMinutes: Long = 1
      ) {
         if (!MetadataPipelineConfig.ENABLE_AUTO_ENRICHMENT) return
         if (cancionesNuevas == 0) {
            Log.d(TAG, "Sin canciones nuevas, omitiendo enrichment post-scan")
            return
         }
         
         val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
         
         // Calcular batch size según canciones nuevas
         val batchSize = minOf(cancionesNuevas, MetadataPipelineConfig.BACKGROUND_BATCH_SIZE)
         
         val inputData = workDataOf(
            KEY_BATCH_SIZE to batchSize,
            KEY_TRIGGERED_BY to TRIGGERED_SCAN,
            KEY_MAX_ATTEMPTS to MetadataPipelineConfig.MAX_ENRICHMENT_ATTEMPTS
         )
         
         val workRequest = OneTimeWorkRequestBuilder<MetadataEnrichmentWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag(TAG_ENRICHMENT)
            .build()
         
         WorkManager.getInstance(context)
            .enqueueUniqueWork(
               WORK_NAME_POST_SCAN,
               ExistingWorkPolicy.REPLACE,
               workRequest
            )
         
         Log.d(TAG, "Enriquecimiento post-scan programado en ${delayMinutes}min (batch=$batchSize)")
      }
      
      /**
       * Programa enriquecimiento periódico
       * Se ejecuta cada X horas cuando el dispositivo está idle
       */
      fun programarPeriodico(
         context: Context,
         intervalHours: Long = DEFAULT_PERIODIC_HOURS,
         requiresWifi: Boolean = true,
         requiresCharging: Boolean = false
      ) {
         if (!MetadataPipelineConfig.ENRICH_IN_BACKGROUND) {
            Log.d(TAG, "Background enrichment deshabilitado")
            return
         }
         
         val constraints = Constraints.Builder()
            .setRequiredNetworkType(
               if (requiresWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .apply {
               if (requiresCharging) setRequiresCharging(true)
            }
            .build()
         
         val inputData = workDataOf(
            KEY_BATCH_SIZE to MetadataPipelineConfig.BACKGROUND_BATCH_SIZE,
            KEY_TRIGGERED_BY to TRIGGERED_PERIODIC,
            KEY_MAX_ATTEMPTS to MetadataPipelineConfig.MAX_ENRICHMENT_ATTEMPTS
         )
         
         val workRequest = PeriodicWorkRequestBuilder<MetadataEnrichmentWorker>(
            intervalHours, TimeUnit.HOURS,
            FLEX_MINUTES, TimeUnit.MINUTES
         )
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(TAG_ENRICHMENT)
            .addTag(TAG_PERIODIC)
            .build()
         
         WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
               WORK_NAME_PERIODIC,
               ExistingPeriodicWorkPolicy.KEEP,
               workRequest
            )
         
         Log.d(TAG, "Enriquecimiento periódico programado cada ${intervalHours}h")
      }
      
      /**
       * Cancela todos los trabajos de enriquecimiento
       */
      fun cancelarTodos(context: Context) {
         WorkManager.getInstance(context).cancelAllWorkByTag(TAG_ENRICHMENT)
         Log.d(TAG, "Todos los trabajos de enriquecimiento cancelados")
      }
      
      /**
       * Cancela solo el periódico
       */
      fun cancelarPeriodico(context: Context) {
         WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
         Log.d(TAG, "Enriquecimiento periódico cancelado")
      }
      
      /**
       * Observa estado de trabajos de enriquecimiento
       */
      fun observarEstado(context: Context): Flow<List<WorkInfo>> {
         return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(TAG_ENRICHMENT)
      }
      
      /**
       * Verifica si hay enriquecimiento en progreso
       */
      fun observarEnProgreso(context: Context): Flow<Boolean> {
         return observarEstado(context).map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
         }
      }
      
      /**
       * Obtiene estadísticas de la cola de enriquecimiento
       */
      suspend fun obtenerEstadisticasCola(context: Context): EnrichmentQueueStats {
         return try {
            val workInfos = WorkManager.getInstance(context)
               .getWorkInfosByTagFlow(TAG_ENRICHMENT)
               .first()
            
            EnrichmentQueueStats(
               enProgreso = workInfos.count { it.state == WorkInfo.State.RUNNING },
               enCola = workInfos.count { it.state == WorkInfo.State.ENQUEUED },
               completados = workInfos.count { it.state == WorkInfo.State.SUCCEEDED },
               fallidos = workInfos.count { it.state == WorkInfo.State.FAILED }
            )
         } catch (e: Exception) {
            EnrichmentQueueStats()
         }
      }
   }
   
   override suspend fun doWork(): Result {
      val batchSize = inputData.getInt(KEY_BATCH_SIZE, DEFAULT_BATCH_SIZE)
      val triggeredBy = inputData.getString(KEY_TRIGGERED_BY) ?: TRIGGERED_MANUAL
      val maxAttempts = inputData.getInt(KEY_MAX_ATTEMPTS, MetadataPipelineConfig.MAX_ENRICHMENT_ATTEMPTS)
      
      Log.d(TAG, "🚀 Iniciando enriquecimiento (batch=$batchSize, by=$triggeredBy, attempt=${runAttemptCount + 1})")
      val startTime = System.currentTimeMillis()
      
      return try {
         withContext(Dispatchers.IO) {
            // 1. Obtener canciones pendientes de enriquecer
            reportarProgreso(0, batchSize, "Buscando canciones pendientes...")
            
            val cancionesPendientes = obtenerCancionesPendientes(batchSize, maxAttempts)
            
            if (cancionesPendientes.isEmpty()) {
               Log.d(TAG, "✅ No hay canciones pendientes de enriquecer")
               return@withContext Result.success(
                  workDataOf(
                     KEY_RESULT_ENRICHED to 0,
                     KEY_RESULT_TOTAL to 0,
                     KEY_RESULT_TIME_MS to (System.currentTimeMillis() - startTime)
                  )
               )
            }
            
            Log.d(TAG, "📊 Encontradas ${cancionesPendientes.size} canciones para enriquecer")
            
            // 2. Procesar batch
            var enriched = 0
            var failed = 0
            var skipped = 0
            
            cancionesPendientes.forEachIndexed { index, songWithArtist ->
               val cancion = songWithArtist.cancion
               
               reportarProgreso(
                  index + 1,
                  cancionesPendientes.size,
                  "Enriqueciendo: ${cancion.titulo}",
                  cancion.titulo
               )
               
               try {
                  // Verificar que sigue necesitando enriquecimiento
                  if (!cancion.necesitaEnriquecimiento()) {
                     skipped++
                     return@forEachIndexed
                  }
                  
                  // Intentar enriquecer
                  geniusRepository.sincronizarCancionAlReproducir(cancion)
                  
                  // Verificar resultado (reload from DB)
                  val actualizada = songDao.obtenerCancionPorId(cancion.idCancion)
                  if (actualizada != null && actualizada.metadataStatus != cancion.metadataStatus) {
                     if (actualizada.metadataStatus in listOf(
                           SongEntity.STATUS_ENRICHED,
                           SongEntity.STATUS_VERIFIED,
                           SongEntity.STATUS_PARTIAL_VERIFIED
                        )) {
                        enriched++
                        Log.d(TAG, "   ✅ ${cancion.titulo} → ${actualizada.metadataStatus}")
                     } else {
                        failed++
                        Log.d(TAG, "   ❌ ${cancion.titulo} → ${actualizada.metadataStatus}")
                     }
                  } else {
                     skipped++
                  }
                  
                  // Rate limiting - pequeña pausa entre requests
                  if (index < cancionesPendientes.size - 1) {
                     val baseDelay = 500L
                     val adaptiveDelay = when {
                        failed > enriched * 2 -> baseDelay * 4  // Muchos fallos = más lento
                        failed > enriched -> baseDelay * 2
                        else -> baseDelay
                     }
                     kotlinx.coroutines.delay(adaptiveDelay)
                  }
                  
               } catch (e: Exception) {
                  Log.e(TAG, "   ❌ Error enriqueciendo ${cancion.titulo}: ${e.message}")
                  failed++
               }
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, """
                    ✅ Enriquecimiento completado en ${totalTime}ms:
                       - Enriquecidas: $enriched
                       - Fallidas: $failed  
                       - Saltadas: $skipped
                       - Total: ${cancionesPendientes.size}
                """.trimIndent())
            
            Result.success(
               workDataOf(
                  KEY_RESULT_ENRICHED to enriched,
                  KEY_RESULT_FAILED to failed,
                  KEY_RESULT_SKIPPED to skipped,
                  KEY_RESULT_TOTAL to cancionesPendientes.size,
                  KEY_RESULT_TIME_MS to totalTime
               )
            )
         }
      } catch (e: Exception) {
         Log.e(TAG, "❌ Error en enriquecimiento: ${e.message}", e)
         
         // Decidir si reintentar
         val shouldRetry = runAttemptCount < MAX_RETRY_ATTEMPTS && isRetryableError(e)
         
         if (shouldRetry) {
            Log.d(TAG, "🔄 Programando reintento...")
            Result.retry()
         } else {
            Result.failure(
               workDataOf(
                  KEY_ERROR_MESSAGE to (e.localizedMessage ?: "Error desconocido"),
                  KEY_RESULT_TIME_MS to (System.currentTimeMillis() - startTime)
               )
            )
         }
      }
   }
   
   private suspend fun obtenerCancionesPendientes(
      limit: Int,
      maxAttempts: Int
   ): List<SongWithArtist> {
      // Obtener canciones que necesitan enriquecimiento
      val cancionesEntity = songDao.obtenerCancionesParaEnriquecer(maxAttempts, limit)
      
      // Convertir a SongWithArtist para tener nombre del artista
      return cancionesEntity.mapNotNull { cancion ->
         songDao.obtenerCancionConArtistaPorId(cancion.idCancion, 1) // usuarioId = 1
      }
   }
   
   private suspend fun reportarProgreso(
      current: Int,
      total: Int,
      message: String,
      currentSong: String? = null
   ) {
      setProgress(
         workDataOf(
            KEY_PROGRESS_CURRENT to current,
            KEY_PROGRESS_TOTAL to total,
            KEY_PROGRESS_MESSAGE to message,
            KEY_CURRENT_SONG to (currentSong ?: "")
         )
      )
   }
   
   private fun isRetryableError(e: Exception): Boolean {
      return when (e) {
         is java.io.IOException,
         is java.net.SocketTimeoutException,
         is java.net.UnknownHostException -> true
         else -> false
      }
   }
}

/**
 * Estadísticas de la cola de enriquecimiento
 */
data class EnrichmentQueueStats(
   val enProgreso: Int = 0,
   val enCola: Int = 0,
   val completados: Int = 0,
   val fallidos: Int = 0
) {
   val total: Int get() = enProgreso + enCola + completados + fallidos
   val hayTrabajosPendientes: Boolean get() = enProgreso > 0 || enCola > 0
}