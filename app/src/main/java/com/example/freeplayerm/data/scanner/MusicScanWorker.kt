// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicScanWorker.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.freeplayerm.data.repository.LocalMusicRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltWorker
class MusicScanWorker
@AssistedInject
constructor(
   @Assisted context: Context,
   @Assisted params: WorkerParameters,
   private val musicRepository: LocalMusicRepository,
) : CoroutineWorker(context, params) {

   companion object {
      private const val TAG = "MusicScanWorker"

      const val WORK_NAME_PERIODIC = "music_scan_periodic"
      const val WORK_NAME_IMMEDIATE = "music_scan_immediate"
      const val WORK_NAME_BOOT = "music_scan_boot"

      const val TAG_MUSIC_SCAN = "music_scan"
      const val TAG_IMMEDIATE = "immediate"
      const val TAG_PERIODIC = "periodic"

      // Input keys
      const val KEY_SCAN_TYPE = "scan_type"
      const val KEY_FORCE_FULL_SCAN = "force_full_scan"

      // Output keys
      const val KEY_RESULT_NEW = "result_new"
      const val KEY_RESULT_DELETED = "result_deleted"
      const val KEY_RESULT_UPDATED = "result_updated"
      const val KEY_RESULT_TIME_MS = "result_time_ms"
      const val KEY_ERROR_MESSAGE = "error_message"

      // Progress keys
      const val KEY_PROGRESS_CURRENT = "progress_current"
      const val KEY_PROGRESS_TOTAL = "progress_total"
      const val KEY_PROGRESS_MESSAGE = "progress_message"

      const val SCAN_TYPE_PERIODIC = "periodic"
      const val SCAN_TYPE_IMMEDIATE = "immediate"
      const val SCAN_TYPE_BOOT = "boot"

      private const val DEFAULT_PERIODIC_HOURS = 6L
      private const val DEFAULT_FLEX_MINUTES = 30L
      private const val MAX_RETRY_ATTEMPTS = 3

      private const val INITIAL_BACKOFF_SECONDS = 30L

      private const val MAX_BACKOFF_SECONDS = 300L

      fun programarEscaneosPeriodicos(
         context: Context,
         intervalHours: Long = DEFAULT_PERIODIC_HOURS,
         requiresCharging: Boolean = false,
         requiresIdle: Boolean = true,
      ) {
         val constraints =
            Constraints.Builder()
               .setRequiresBatteryNotLow(true)
               .apply {
                  if (requiresCharging) setRequiresCharging(true)
                  if (requiresIdle) {
                     setRequiresDeviceIdle(true)
                  }
               }
               .build()

         val inputData = workDataOf(KEY_SCAN_TYPE to SCAN_TYPE_PERIODIC)

         val workRequest =
            PeriodicWorkRequestBuilder<MusicScanWorker>(
                  intervalHours,
                  TimeUnit.HOURS,
                  DEFAULT_FLEX_MINUTES,
                  TimeUnit.MINUTES,
               )
               .setConstraints(constraints)
               .setInputData(inputData)
               .addTag(TAG_MUSIC_SCAN)
               .addTag(TAG_PERIODIC)
               .build()

         WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
               WORK_NAME_PERIODIC,
               ExistingPeriodicWorkPolicy.KEEP,
               workRequest,
            )

         Log.d(TAG, "Escaneos periodicos programados cada ${intervalHours}h")
      }

      fun ejecutarEscaneoInmediato(context: Context, forceFullScan: Boolean = false) {
         val inputData =
            workDataOf(KEY_SCAN_TYPE to SCAN_TYPE_IMMEDIATE, KEY_FORCE_FULL_SCAN to forceFullScan)

         val workRequest =
            OneTimeWorkRequestBuilder<MusicScanWorker>()
               .setInputData(inputData)
               .setBackoffCriteria(
                  BackoffPolicy.EXPONENTIAL,
                  INITIAL_BACKOFF_SECONDS,
                  TimeUnit.SECONDS,
               )
               .addTag(TAG_MUSIC_SCAN)
               .addTag(TAG_IMMEDIATE)
               .build()

         WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME_IMMEDIATE, ExistingWorkPolicy.REPLACE, workRequest)

         Log.d(TAG, "Escaneo inmediato encolado (forzado=$forceFullScan)")
      }

      fun programarEscaneoPorArranque(context: Context) {
         val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

         val inputData = workDataOf(KEY_SCAN_TYPE to SCAN_TYPE_BOOT)

         val workRequest =
            OneTimeWorkRequestBuilder<MusicScanWorker>()
               .setConstraints(constraints)
               .setInputData(inputData)
               .setInitialDelay(30, TimeUnit.SECONDS)
               .addTag(TAG_MUSIC_SCAN)
               .build()

         WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME_BOOT, ExistingWorkPolicy.REPLACE, workRequest)

         Log.d(TAG, "Escaneo post-arranque programado")
      }

      fun cancelarTodos(context: Context) {
         WorkManager.getInstance(context).cancelAllWorkByTag(TAG_MUSIC_SCAN)
         Log.d(TAG, "Todos los escaneos cancelados")
      }

      fun cancelarPeriodicos(context: Context) {
         WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
         Log.d(TAG, "Escaneos periodicos cancelados")
      }

      fun observarEstado(context: Context): Flow<List<WorkInfo>> {
         return WorkManager.getInstance(context).getWorkInfosByTagFlow(TAG_MUSIC_SCAN)
      }

      fun observarEscaneoEnProgreso(context: Context): Flow<Boolean> {
         return observarEstado(context).map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
         }
      }

      suspend fun estanProgramadosLosPeriodicos(context: Context): Boolean {
         return try {
            val workInfos =
               WorkManager.getInstance(context)
                  .getWorkInfosForUniqueWorkFlow(WORK_NAME_PERIODIC)
                  .first()
            workInfos.any {
               it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
         } catch (e: Exception) {
            false
         }
      }
   }
   
   override suspend fun doWork(): Result {
      val scanType = inputData.getString(KEY_SCAN_TYPE) ?: SCAN_TYPE_IMMEDIATE
      val forceFullScan = inputData.getBoolean(KEY_FORCE_FULL_SCAN, false)

      Log.d(
         TAG,
         "Iniciando escaneo [$scanType] (intento ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS)",
      )
      reportarProgreso(0, 0, "Iniciando escaneo...")

      return try {
         withContext(Dispatchers.IO) {
            // ✅ Usar coroutineScope para cancelación estructurada
            coroutineScope {
               val progressJob = launch { observarProgresoRepositorio() }

               try {
                  val resultado = musicRepository.escanearYGuardarMusica(forceFullScan)

                  if (resultado != null) {
                     Log.d(
                        TAG,
                        "Escaneo completado: +${resultado.nuevas}, -${resultado.eliminadas}, ~${resultado.actualizadas}",
                     )

                     val outputData =
                        workDataOf(
                           KEY_RESULT_NEW to resultado.nuevas,
                           KEY_RESULT_DELETED to resultado.eliminadas,
                           KEY_RESULT_UPDATED to resultado.actualizadas,
                           KEY_RESULT_TIME_MS to resultado.tiempoMs,
                        )

                     Result.success(outputData)
                  } else {
                     Log.d(TAG, "Escaneo omitido - ya en progreso")
                     Result.success()
                  }
               } finally {
                  progressJob.cancelAndJoin() // ✅ Asegurar cancelación
               }
            }
         }
      } catch (e: SecurityException) {
         Log.e(TAG, "Sin permisos de almacenamiento", e)
         Result.failure(workDataOf(KEY_ERROR_MESSAGE to "Sin permisos de almacenamiento"))
      } catch (e: Exception) {
         Log.e(TAG, "Error durante el escaneo (intento ${runAttemptCount + 1})", e)

         val esErrorRecuperable =
            when (e) {
               is java.io.IOException,
               is android.database.sqlite.SQLiteException,
               is kotlinx.coroutines.TimeoutCancellationException -> true
               else -> false
            }

         if (esErrorRecuperable && runAttemptCount < MAX_RETRY_ATTEMPTS - 1) {
            val backoffSeconds = calculateBackoff(runAttemptCount)
            Log.d(TAG, "Programando reintento en ${backoffSeconds}s...")
            Result.retry()
         } else {
            Log.e(TAG, "Máximo de reintentos alcanzado o error no recuperable")
            Result.failure(
               workDataOf(
                  KEY_ERROR_MESSAGE to (e.localizedMessage ?: "Error desconocido"),
                  "retry_count" to runAttemptCount,
               )
            )
         }
      }
   }

   // ✅ Cambiar observarProgresoRepositorio para no retornar Job
   private suspend fun observarProgresoRepositorio() {
      musicRepository.estadoEscaneo.collect { estado ->
         when (estado) {
            is LocalMusicRepository.EstadoEscaneo.Escaneando -> {
               reportarProgreso(estado.progreso, estado.total, estado.mensaje)
            }
            else -> {
               /* Ignorar */
            }
         }
      }
   }

   private fun calculateBackoff(attemptCount: Int): Long {
      val backoff = INITIAL_BACKOFF_SECONDS * (1 shl attemptCount) // 2^attemptCount
      return backoff.coerceAtMost(MAX_BACKOFF_SECONDS)
   }

   

   private suspend fun reportarProgreso(current: Int, total: Int, message: String) {
      setProgress(
         workDataOf(
            KEY_PROGRESS_CURRENT to current,
            KEY_PROGRESS_TOTAL to total,
            KEY_PROGRESS_MESSAGE to message,
         )
      )
   }
}
