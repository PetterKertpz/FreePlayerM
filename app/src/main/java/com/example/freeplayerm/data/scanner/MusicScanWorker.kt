// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicScanWorker.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.freeplayerm.data.repository.LocalMusicRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * 🔧 MUSIC SCAN WORKER - Escaneos en Segundo Plano
 *
 * Worker para ejecutar escaneos de música usando WorkManager. Soporta escaneos únicos, periódicos y
 * con reintentos.
 *
 * Características:
 * - Escaneos periódicos configurables (por defecto cada 6 horas)
 * - Escaneos inmediatos bajo demanda
 * - Reintentos automáticos con backoff exponencial
 * - Constraints configurables (batería, WiFi, etc.)
 * - Notificación de progreso vía WorkInfo
 * - Integración con Hilt para inyección de dependencias
 *
 * @author Scanner System v2.0
 */
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

        // Nombres únicos para los trabajos
        const val WORK_NAME_PERIODIC = "music_scan_periodic"
        const val WORK_NAME_IMMEDIATE = "music_scan_immediate"
        const val WORK_NAME_BOOT = "music_scan_boot"

        // Tags para agrupar trabajos
        const val TAG_MUSIC_SCAN = "music_scan"
        const val TAG_IMMEDIATE = "immediate"
        const val TAG_PERIODIC = "periodic"

        // Keys para input/output data
        const val KEY_SCAN_TYPE = "scan_type"
        const val KEY_FORCE_FULL_SCAN = "force_full_scan"
        const val KEY_RESULT_NEW = "result_new"
        const val KEY_RESULT_DELETED = "result_deleted"
        const val KEY_RESULT_UPDATED = "result_updated"
        const val KEY_RESULT_TIME_MS = "result_time_ms"
        const val KEY_ERROR_MESSAGE = "error_message"

        // Tipos de escaneo
        const val SCAN_TYPE_PERIODIC = "periodic"
        const val SCAN_TYPE_IMMEDIATE = "immediate"
        const val SCAN_TYPE_BOOT = "boot"

        // Configuración por defecto
        private const val DEFAULT_PERIODIC_HOURS = 6L
        private const val DEFAULT_FLEX_MINUTES = 30L
        private const val MAX_RETRY_ATTEMPTS = 3

        // ==================== PROGRAMACIÓN DE TRABAJOS ====================

        /**
         * Programa escaneos periódicos.
         *
         * @param context Contexto de la aplicación
         * @param intervalHours Intervalo entre escaneos (default: 6 horas)
         * @param requiresCharging Si debe estar cargando (default: false)
         * @param requiresIdle Si debe estar en reposo (default: true en API 23+)
         */
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
                        if (requiresIdle && android.os.Build.VERSION.SDK_INT >= 23) {
                            setRequiresDeviceIdle(true)
                        }
                    }
                    .build()

            val inputData = Data.Builder().putString(KEY_SCAN_TYPE, SCAN_TYPE_PERIODIC).build()

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
                    ExistingPeriodicWorkPolicy.KEEP, // No reemplazar si ya existe
                    workRequest,
                )

            Log.d(TAG, "📅 Escaneos periódicos programados cada ${intervalHours}h")
        }

        /**
         * Ejecuta un escaneo inmediato en segundo plano.
         *
         * @param context Contexto de la aplicación
         * @param forceFullScan Si debe forzar escaneo completo ignorando caché
         */
        fun ejecutarEscaneoInmediato(context: Context, forceFullScan: Boolean = false) {
            val inputData =
                Data.Builder()
                    .putString(KEY_SCAN_TYPE, SCAN_TYPE_IMMEDIATE)
                    .putBoolean(KEY_FORCE_FULL_SCAN, forceFullScan)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<MusicScanWorker>()
                    .setInputData(inputData)
                    .addTag(TAG_MUSIC_SCAN)
                    .addTag(TAG_IMMEDIATE)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME_IMMEDIATE,
                    ExistingWorkPolicy.REPLACE, // Reemplazar si ya existe uno pendiente
                    workRequest,
                )

            Log.d(TAG, "🚀 Escaneo inmediato encolado${if (forceFullScan) " (forzado)" else ""}")
        }

        /** Programa un escaneo para ejecutarse después del arranque del dispositivo. */
        fun programarEscaneoPorArranque(context: Context) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

            val inputData = Data.Builder().putString(KEY_SCAN_TYPE, SCAN_TYPE_BOOT).build()

            val workRequest =
                OneTimeWorkRequestBuilder<MusicScanWorker>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .setInitialDelay(30, TimeUnit.SECONDS) // Esperar 30s después del boot
                    .addTag(TAG_MUSIC_SCAN)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME_BOOT, ExistingWorkPolicy.REPLACE, workRequest)

            Log.d(TAG, "🔄 Escaneo post-arranque programado")
        }

        /** Cancela todos los trabajos de escaneo pendientes. */
        fun cancelarTodos(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG_MUSIC_SCAN)
            Log.d(TAG, "🛑 Todos los escaneos cancelados")
        }

        /** Cancela solo los escaneos periódicos. */
        fun cancelarPeriodicos(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
            Log.d(TAG, "🛑 Escaneos periódicos cancelados")
        }

        /**
         * Observa el estado de los trabajos de escaneo.
         *
         * @return Flow con lista de WorkInfo
         */
        fun observarEstado(context: Context): Flow<List<WorkInfo>> {
            return WorkManager.getInstance(context).getWorkInfosByTagFlow(TAG_MUSIC_SCAN)
        }

        /** Observa si hay un escaneo en progreso. */
        fun observarEscaneoEnProgreso(context: Context): Flow<Boolean> {
            return observarEstado(context).map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }
        }

        /** Verifica si los escaneos periódicos están programados. */
        suspend fun estanProgramadosLosPeriodicos(context: Context): Boolean {
            val workInfos =
                WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME_PERIODIC).get()

            return workInfos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }
    }

    // ==================== EJECUCIÓN DEL WORKER ====================

    override suspend fun doWork(): Result {
        val scanType = inputData.getString(KEY_SCAN_TYPE) ?: SCAN_TYPE_IMMEDIATE
        val forceFullScan = inputData.getBoolean(KEY_FORCE_FULL_SCAN, false)

        Log.d(
            TAG,
            "🎵 Iniciando escaneo [$scanType] (intento ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS)",
        )

        return try {
            val resultado = musicRepository.escanearYGuardarMusica()

            if (resultado != null) {
                Log.d(
                    TAG,
                    "✅ Escaneo completado: +${resultado.nuevas}, -${resultado.eliminadas}, ~${resultado.actualizadas} (${resultado.tiempoMs}ms)",
                )

                val outputData =
                    Data.Builder()
                        .putInt(KEY_RESULT_NEW, resultado.nuevas)
                        .putInt(KEY_RESULT_DELETED, resultado.eliminadas)
                        .putInt(KEY_RESULT_UPDATED, resultado.actualizadas)
                        .putLong(KEY_RESULT_TIME_MS, resultado.tiempoMs)
                        .build()

                Result.success(outputData)
            } else {
                // Escaneo omitido porque ya había uno en progreso
                Log.d(TAG, "⚠️ Escaneo omitido - ya en progreso")
                Result.success()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Sin permisos de almacenamiento", e)
            val outputData =
                Data.Builder()
                    .putString(KEY_ERROR_MESSAGE, "Sin permisos de almacenamiento")
                    .build()
            Result.failure(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error durante el escaneo", e)

            if (runAttemptCount < MAX_RETRY_ATTEMPTS - 1) {
                Log.d(TAG, "🔄 Programando reintento...")
                Result.retry()
            } else {
                val outputData =
                    Data.Builder()
                        .putString(KEY_ERROR_MESSAGE, e.localizedMessage ?: "Error desconocido")
                        .build()
                Result.failure(outputData)
            }
        }
    }
}
