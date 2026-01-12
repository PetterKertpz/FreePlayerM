// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicScannerManager.kt
// ✅ VERSIÓN ACTUALIZADA CON ENRIQUECIMIENTO INTEGRADO
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import com.example.freeplayerm.data.purification.MetadataPipelineConfig
import com.example.freeplayerm.data.repository.LocalMusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 MUSIC SCANNER MANAGER v2.0
 *
 * Gestor centralizado del sistema de escaneo y enriquecimiento.
 * Coordina:
 * - Escaneo de MediaStore (LocalMusicRepository)
 * - Enriquecimiento con Genius API (MetadataEnrichmentWorker)
 * - Detección automática de cambios (MusicContentObserver)
 * - Trabajos periódicos (WorkManager)
 *
 * Flujo completo:
 * MediaStore → CLEANED_LOCAL → Genius API → VERIFIED
 *
 * @version 2.0 - Integración con sistema de purificación
 */
@Singleton
class MusicScannerManager @Inject constructor(
   @ApplicationContext private val context: Context,
   private val musicRepository: LocalMusicRepository,
   private val contentObserver: MusicContentObserver,
) {
   companion object {
      private const val TAG = "MusicScannerManager"
      private const val MIN_INTERVALO_MANUAL_MS = 15_000L // 15 segundos
      private const val MIN_INTERVALO_AUTOMATICO_MS = 60_000L // 1 minuto
   }
   
   private var scope: CoroutineScope? = null
   private var observerJob: Job? = null
   
   private var ultimoEscaneoTimestamp = 0L
   private var ultimoEscaneoAutomaticoTimestamp = 0L
   
   private val _estaInicializado = MutableStateFlow(false)
   val estaInicializado: StateFlow<Boolean> = _estaInicializado.asStateFlow()
   
   // ==================== ESTADO UNIFICADO ====================
   
   data class EstadoUnificado(
      val estaEscaneando: Boolean = false,
      val estaEnriqueciendo: Boolean = false,
      val tipoEscaneoActivo: TipoEscaneo? = null,
      val progreso: Pair<Int, Int>? = null,
      val ultimoResultado: ResultadoEscaneo? = null,
      val ultimoEnriquecimiento: ResultadoEnriquecimiento? = null,
      val observerRegistrado: Boolean = false,
      val escaneosPeriodicos: Boolean = false,
      val enriquecimientoPeriodico: Boolean = false,
   )
   
   enum class TipoEscaneo {
      MANUAL,
      AUTOMATICO,
      PERIODICO,
   }
   
   data class ResultadoEscaneo(
      val nuevas: Int,
      val eliminadas: Int,
      val actualizadas: Int,
      val tiempoMs: Long,
      val exitoso: Boolean,
      val error: String? = null,
   )
   
   data class ResultadoEnriquecimiento(
      val enriquecidas: Int,
      val fallidas: Int,
      val saltadas: Int,
      val tiempoMs: Long,
      val exitoso: Boolean,
      val error: String? = null,
   ) {
      val total: Int get() = enriquecidas + fallidas + saltadas
      val tasaExito: Double get() = if (total > 0) enriquecidas.toDouble() / total else 0.0
   }
   
   private val _ultimoResultado = MutableStateFlow<ResultadoEscaneo?>(null)
   val ultimoResultado: StateFlow<ResultadoEscaneo?> = _ultimoResultado.asStateFlow()
   
   private val _ultimoEnriquecimiento = MutableStateFlow<ResultadoEnriquecimiento?>(null)
   val ultimoEnriquecimiento: StateFlow<ResultadoEnriquecimiento?> = _ultimoEnriquecimiento.asStateFlow()
   
   /**
    * Estado unificado que combina escaneo y enriquecimiento
    */
   val estadoUnificado: Flow<EstadoUnificado> = combine(
      musicRepository.estadoEscaneo,
      MusicScanWorker.observarEscaneoEnProgreso(context),
      MetadataEnrichmentWorker.observarEnProgreso(context),
      _ultimoResultado,
      _ultimoEnriquecimiento,
      _estaInicializado,
   ) { array ->
      val estadoRepo = array[0] as LocalMusicRepository.EstadoEscaneo
      val scanWorkerEnProgreso = array[1] as Boolean
      val enrichWorkerEnProgreso = array[2] as Boolean
      val ultimoResultado = array[3] as ResultadoEscaneo?
      val ultimoEnriquecimiento = array[4] as ResultadoEnriquecimiento?
      val inicializado = array[5] as Boolean
      
      val estaEscaneando = when (estadoRepo) {
         is LocalMusicRepository.EstadoEscaneo.Escaneando -> true
         else -> scanWorkerEnProgreso
      }
      
      val progreso = when (estadoRepo) {
         is LocalMusicRepository.EstadoEscaneo.Escaneando ->
            estadoRepo.progreso to estadoRepo.total
         else -> null
      }
      
      val tipoActivo = when {
         estadoRepo is LocalMusicRepository.EstadoEscaneo.Escaneando -> {
            if (scanWorkerEnProgreso) TipoEscaneo.PERIODICO else TipoEscaneo.MANUAL
         }
         else -> null
      }
      
      EstadoUnificado(
         estaEscaneando = estaEscaneando,
         estaEnriqueciendo = enrichWorkerEnProgreso,
         tipoEscaneoActivo = tipoActivo,
         progreso = progreso,
         ultimoResultado = ultimoResultado,
         ultimoEnriquecimiento = ultimoEnriquecimiento,
         observerRegistrado = inicializado,
         escaneosPeriodicos = inicializado,
         enriquecimientoPeriodico = MetadataPipelineConfig.ENRICH_IN_BACKGROUND,
      )
   }
   
   // ==================== INICIALIZACIÓN ====================
   
   /**
    * Inicializa el sistema completo de escaneo y enriquecimiento
    */
   fun inicializar(ejecutarEscaneoInicial: Boolean = true) {
      if (_estaInicializado.value) {
         Log.d(TAG, "Ya inicializado, omitiendo")
         return
      }
      
      Log.d(TAG, "🚀 Inicializando sistema de escaneo y enriquecimiento...")
      
      scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
      
      // Observar resultados del repositorio
      observarResultadosDelRepositorio()
      
      // Configurar callback con rate limiting y enriquecimiento
      contentObserver.setOnChangeListener {
         val ahora = System.currentTimeMillis()
         val tiempoDesdeUltimo = ahora - ultimoEscaneoAutomaticoTimestamp
         
         if (tiempoDesdeUltimo < MIN_INTERVALO_AUTOMATICO_MS) {
            Log.d(TAG, "Escaneo automático omitido por rate limiting")
            return@setOnChangeListener
         }
         
         ultimoEscaneoAutomaticoTimestamp = ahora
         Log.d(TAG, "Cambio detectado, ejecutando escaneo automático")
         
         try {
            val resultado = musicRepository.escanearYGuardarMusica(forceFullScan = false)
            if (resultado != null) {
               _ultimoResultado.value = resultado.toResultadoEscaneo()
               Log.d(TAG, "Escaneo automático completado: +${resultado.nuevas}, -${resultado.eliminadas}")
               
               // ✅ NUEVO: Encolar enriquecimiento si hay canciones nuevas
               if (resultado.nuevas > 0 && MetadataPipelineConfig.ENABLE_AUTO_ENRICHMENT) {
                  MetadataEnrichmentWorker.ejecutarPostEscaneo(
                     context = context,
                     cancionesNuevas = resultado.nuevas
                  )
               }
            }
         } catch (e: Exception) {
            Log.e(TAG, "Error en escaneo automático", e)
            _ultimoResultado.value = ResultadoEscaneo(
               nuevas = 0,
               eliminadas = 0,
               actualizadas = 0,
               tiempoMs = 0,
               exitoso = false,
               error = e.localizedMessage ?: "Error en escaneo automático",
            )
         }
      }
      
      // Registrar observer y programar trabajos periódicos
      contentObserver.registrar()
      MusicScanWorker.programarEscaneosPeriodicos(context)
      
      // ✅ NUEVO: Programar enriquecimiento periódico
      if (MetadataPipelineConfig.ENRICH_IN_BACKGROUND) {
         MetadataEnrichmentWorker.programarPeriodico(context)
      }
      
      _estaInicializado.value = true
      Log.d(TAG, "✅ Sistema de escaneo y enriquecimiento inicializado")
      
      if (ejecutarEscaneoInicial) {
         Log.d(TAG, "Ejecutando escaneo inicial...")
         ultimoEscaneoTimestamp = System.currentTimeMillis()
         escanearAhora()
      }
   }
   
   /**
    * Detiene el sistema (mantiene estado)
    */
   fun detener() {
      Log.d(TAG, "Deteniendo sistema de escaneo...")
      
      observerJob?.cancel()
      contentObserver.desregistrar()
      MusicScanWorker.cancelarTodos(context)
      MetadataEnrichmentWorker.cancelarTodos(context)
      
      _estaInicializado.value = false
   }
   
   /**
    * Destruye el sistema completamente
    */
   fun destroy() {
      Log.d(TAG, "Destruyendo sistema de escaneo...")
      detener()
      contentObserver.destroy()
      scope?.cancel()
      scope = null
   }
   
   // ==================== ESCANEO ====================
   
   /**
    * Ejecuta escaneo inmediato con opción de enriquecimiento automático
    */
   fun escanearAhora(forzarCompleto: Boolean = false, enriquecerDespues: Boolean = true) {
      val ahora = System.currentTimeMillis()
      val tiempoDesdeUltimo = ahora - ultimoEscaneoTimestamp
      
      if (!forzarCompleto && tiempoDesdeUltimo < MIN_INTERVALO_MANUAL_MS) {
         val segundosRestantes = ((MIN_INTERVALO_MANUAL_MS - tiempoDesdeUltimo) / 1000).toInt()
         Log.d(TAG, "Escaneo rechazado por rate limiting. Espera ${segundosRestantes}s")
         
         _ultimoResultado.value = ResultadoEscaneo(
            nuevas = 0,
            eliminadas = 0,
            actualizadas = 0,
            tiempoMs = 0,
            exitoso = false,
            error = "Por favor espera $segundosRestantes segundos antes de escanear nuevamente"
         )
         return
      }
      
      ultimoEscaneoTimestamp = ahora
      
      scope?.launch {
         Log.d(TAG, "Iniciando escaneo manual (forzado=$forzarCompleto)")
         try {
            val resultado = musicRepository.escanearYGuardarMusica(forzarCompleto)
            if (resultado != null) {
               _ultimoResultado.value = resultado.toResultadoEscaneo()
               
               // ✅ NUEVO: Encolar enriquecimiento si hay canciones nuevas
               if (enriquecerDespues && resultado.nuevas > 0 && MetadataPipelineConfig.ENABLE_AUTO_ENRICHMENT) {
                  Log.d(TAG, "📤 Encolando enriquecimiento para ${resultado.nuevas} canciones nuevas")
                  MetadataEnrichmentWorker.ejecutarPostEscaneo(
                     context = context,
                     cancionesNuevas = resultado.nuevas
                  )
               }
            }
         } catch (e: Exception) {
            Log.e(TAG, "Error en escaneo manual", e)
            _ultimoResultado.value = ResultadoEscaneo(
               nuevas = 0,
               eliminadas = 0,
               actualizadas = 0,
               tiempoMs = 0,
               exitoso = false,
               error = e.localizedMessage,
            )
         }
      }
   }
   
   /**
    * Ejecuta escaneo en segundo plano via WorkManager
    */
   fun escanearEnSegundoPlano(forzarCompleto: Boolean = false) {
      MusicScanWorker.ejecutarEscaneoInmediato(context, forzarCompleto)
   }
   
   // ==================== ENRIQUECIMIENTO ====================
   
   /**
    * Ejecuta enriquecimiento inmediato de canciones pendientes
    */
   fun enriquecerAhora(batchSize: Int = MetadataPipelineConfig.BACKGROUND_BATCH_SIZE) {
      if (!MetadataPipelineConfig.ENABLE_AUTO_ENRICHMENT) {
         Log.w(TAG, "Enriquecimiento deshabilitado en configuración")
         return
      }
      
      Log.d(TAG, "📤 Iniciando enriquecimiento manual (batch=$batchSize)")
      MetadataEnrichmentWorker.ejecutarInmediato(
         context = context,
         batchSize = batchSize,
         triggeredBy = MetadataEnrichmentWorker.TRIGGERED_MANUAL
      )
   }
   
   /**
    * Configura enriquecimiento periódico
    */
   fun configurarEnriquecimientoPeriodico(
      habilitado: Boolean,
      intervalHoras: Long = 12
   ) {
      if (habilitado) {
         MetadataEnrichmentWorker.programarPeriodico(
            context = context,
            intervalHours = intervalHoras
         )
         Log.d(TAG, "Enriquecimiento periódico activado cada ${intervalHoras}h")
      } else {
         MetadataEnrichmentWorker.cancelarPeriodico(context)
         Log.d(TAG, "Enriquecimiento periódico desactivado")
      }
   }
   
   /**
    * Obtiene estadísticas de la cola de enriquecimiento
    */
   suspend fun obtenerEstadisticasEnriquecimiento(): EnrichmentQueueStats {
      return MetadataEnrichmentWorker.obtenerEstadisticasCola(context)
   }
   
   // ==================== CONFIGURACIÓN ====================
   
   /**
    * Habilita/deshabilita detección automática de cambios
    */
   fun setDeteccionAutomatica(habilitada: Boolean) {
      if (habilitada) {
         contentObserver.registrar()
      } else {
         contentObserver.desregistrar()
      }
   }
   
   /**
    * Configura intervalo de escaneo periódico
    */
   fun configurarIntervaloPeriodico(horas: Long) {
      val horasValidas = horas.coerceIn(1, 24)
      MusicScanWorker.cancelarPeriodicos(context)
      MusicScanWorker.programarEscaneosPeriodicos(context, horasValidas)
   }
   
   // ==================== OBSERVADORES ====================
   
   /**
    * Observa trabajos de escaneo en cola
    */
   fun observarTrabajosEscaneo(): Flow<List<WorkInfo>> {
      return MusicScanWorker.observarEstado(context)
   }
   
   /**
    * Observa trabajos de enriquecimiento en cola
    */
   fun observarTrabajosEnriquecimiento(): Flow<List<WorkInfo>> {
      return MetadataEnrichmentWorker.observarEstado(context)
   }
   
   // ==================== UTILIDADES ====================
   
   fun limpiarUltimoResultado() {
      _ultimoResultado.value = null
      _ultimoEnriquecimiento.value = null
      musicRepository.reiniciarEstado()
   }
   
   fun tiempoHastaProximoEscaneoPermitido(): Long {
      val ahora = System.currentTimeMillis()
      val tiempoRestante = MIN_INTERVALO_MANUAL_MS - (ahora - ultimoEscaneoTimestamp)
      return maxOf(0, tiempoRestante)
   }
   
   fun puedeEscanearAhora(): Boolean {
      return tiempoHastaProximoEscaneoPermitido() == 0L
   }
   
   private fun observarResultadosDelRepositorio() {
      observerJob?.cancel()
      observerJob = scope?.launch {
         musicRepository.estadoEscaneo.collect { estado ->
            when (estado) {
               is LocalMusicRepository.EstadoEscaneo.Completado -> {
                  _ultimoResultado.value = estado.toResultadoEscaneo()
               }
               is LocalMusicRepository.EstadoEscaneo.Error -> {
                  _ultimoResultado.value = ResultadoEscaneo(
                     nuevas = 0,
                     eliminadas = 0,
                     actualizadas = 0,
                     tiempoMs = 0,
                     exitoso = false,
                     error = estado.mensaje,
                  )
               }
               else -> { /* Ignorar */ }
            }
         }
      }
   }
   
   // Extension para convertir estado a resultado
   private fun LocalMusicRepository.EstadoEscaneo.Completado.toResultadoEscaneo() = ResultadoEscaneo(
      nuevas = nuevas,
      eliminadas = eliminadas,
      actualizadas = actualizadas,
      tiempoMs = tiempoMs,
      exitoso = true,
   )
}