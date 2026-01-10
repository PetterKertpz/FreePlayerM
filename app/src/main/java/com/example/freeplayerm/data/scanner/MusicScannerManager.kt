// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicScannerManager.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
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
   
   data class EstadoUnificado(
      val estaEscaneando: Boolean = false,
      val tipoEscaneoActivo: TipoEscaneo? = null,
      val progreso: Pair<Int, Int>? = null,
      val ultimoResultado: ResultadoEscaneo? = null,
      val observerRegistrado: Boolean = false,
      val escaneosPeriodicos: Boolean = false,
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
   
   private val _ultimoResultado = MutableStateFlow<ResultadoEscaneo?>(null)
   val ultimoResultado: StateFlow<ResultadoEscaneo?> = _ultimoResultado.asStateFlow()
   
   val estadoUnificado: Flow<EstadoUnificado> = combine(
      musicRepository.estadoEscaneo,
      MusicScanWorker.observarEscaneoEnProgreso(context),
      _ultimoResultado,
      _estaInicializado,
   ) { estadoRepo, workerEnProgreso, ultimoResultado, inicializado ->
      val estaEscaneando = when (estadoRepo) {
         is LocalMusicRepository.EstadoEscaneo.Escaneando -> true
         else -> workerEnProgreso
      }
      
      val progreso = when (estadoRepo) {
         is LocalMusicRepository.EstadoEscaneo.Escaneando ->
            estadoRepo.progreso to estadoRepo.total
         else -> null
      }
      
      val tipoActivo = when {
         estadoRepo is LocalMusicRepository.EstadoEscaneo.Escaneando -> {
            if (workerEnProgreso) TipoEscaneo.PERIODICO else TipoEscaneo.MANUAL
         }
         else -> null
      }
      
      EstadoUnificado(
         estaEscaneando = estaEscaneando,
         tipoEscaneoActivo = tipoActivo,
         progreso = progreso,
         ultimoResultado = ultimoResultado,
         observerRegistrado = inicializado,
         escaneosPeriodicos = inicializado,
      )
   }
   
   fun inicializar(ejecutarEscaneoInicial: Boolean = true) {
      if (_estaInicializado.value) {
         Log.d(TAG, "Ya inicializado, omitiendo")
         return
      }
      
      Log.d(TAG, "Inicializando sistema de escaneo...")
      
      scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
      
      observarResultadosDelRepositorio()
      
      // Configurar callback con rate limiting
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
      
      contentObserver.registrar()
      MusicScanWorker.programarEscaneosPeriodicos(context)
      _estaInicializado.value = true
      Log.d(TAG, "Sistema de escaneo inicializado")
      
      if (ejecutarEscaneoInicial) {
         Log.d(TAG, "Ejecutando escaneo inicial...")
         ultimoEscaneoTimestamp = System.currentTimeMillis()
         escanearAhora()
      }
   }
   
   fun detener() {
      Log.d(TAG, "Deteniendo sistema de escaneo...")
      
      observerJob?.cancel()
      contentObserver.desregistrar()
      MusicScanWorker.cancelarTodos(context)
      
      _estaInicializado.value = false
   }
   
   fun destroy() {
      Log.d(TAG, "Destruyendo sistema de escaneo...")
      detener()
      contentObserver.destroy()
      scope?.cancel()
      scope = null
   }
   
   fun escanearAhora(forzarCompleto: Boolean = false) {
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
   
   fun escanearEnSegundoPlano(forzarCompleto: Boolean = false) {
      MusicScanWorker.ejecutarEscaneoInmediato(context, forzarCompleto)
   }
   
   fun setDeteccionAutomatica(habilitada: Boolean) {
      if (habilitada) {
         contentObserver.registrar()
      } else {
         contentObserver.desregistrar()
      }
   }
   
   fun configurarIntervaloPeriodico(horas: Long) {
      val horasValidas = horas.coerceIn(1, 24)
      MusicScanWorker.cancelarPeriodicos(context)
      MusicScanWorker.programarEscaneosPeriodicos(context, horasValidas)
   }
   
   fun observarTrabajosEnCola(): Flow<List<WorkInfo>> {
      return MusicScanWorker.observarEstado(context)
   }
   
   fun limpiarUltimoResultado() {
      _ultimoResultado.value = null
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