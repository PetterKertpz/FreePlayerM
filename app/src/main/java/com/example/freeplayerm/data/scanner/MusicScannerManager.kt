// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicScannerManager.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import com.example.freeplayerm.data.repository.LocalMusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎛️ MUSIC SCANNER MANAGER - Coordinador Central del Sistema de Escaneo
 *
 * Punto de entrada único para gestionar todo el sistema de escaneo:
 * - ContentObserver (detección automática en tiempo real)
 * - WorkManager (escaneos periódicos en segundo plano)
 * - Escaneos manuales bajo demanda
 *
 * Uso:
 * ```kotlin
 * // Inicializar cuando se concedan permisos
 * scannerManager.inicializar()
 *
 * // Observar estado unificado
 * scannerManager.estadoUnificado.collect { estado -> ... }
 *
 * // Escaneo manual
 * scannerManager.escanearAhora()
 * ```
 *
 * @author Scanner System v2.0
 */
@Singleton
class MusicScannerManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: LocalMusicRepository,
    private val contentObserver: MusicContentObserver,
) {
    companion object {
        private const val TAG = "MusicScannerManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ==================== ESTADO UNIFICADO ====================

    private val _estaInicializado = MutableStateFlow(false)
    val estaInicializado: StateFlow<Boolean> = _estaInicializado.asStateFlow()

    /** Estado unificado que combina todas las fuentes de escaneo. */
    data class EstadoUnificado(
        val estaEscaneando: Boolean = false,
        val tipoEscaneoActivo: TipoEscaneo? = null,
        val progreso: Pair<Int, Int>? = null, // (actual, total)
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

    val estadoUnificado: Flow<EstadoUnificado> =
        combine(
            musicRepository.estadoEscaneo,
            MusicScanWorker.observarEscaneoEnProgreso(context),
            _ultimoResultado,
        ) { estadoRepo, workerEnProgreso, ultimoResultado ->
            val estaEscaneando =
                when (estadoRepo) {
                    is LocalMusicRepository.EstadoEscaneo.Escaneando -> true
                    else -> workerEnProgreso
                }

            val progreso =
                when (estadoRepo) {
                    is LocalMusicRepository.EstadoEscaneo.Escaneando ->
                        estadoRepo.progreso to estadoRepo.total
                    else -> null
                }

            val tipoActivo =
                when {
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
                observerRegistrado = _estaInicializado.value,
                escaneosPeriodicos = _estaInicializado.value,
            )
        }

    // ==================== API PÚBLICA ====================

    /**
     * Inicializa el sistema completo de escaneo. Llamar cuando se confirmen los permisos de
     * almacenamiento.
     */
    fun inicializar(ejecutarEscaneoInicial: Boolean = true) {
        if (_estaInicializado.value) {
            Log.d(TAG, "⚠️ Ya inicializado, omitiendo")
            return
        }

        Log.d(TAG, "🚀 Inicializando sistema de escaneo...")

        // 1. Observar resultados del repositorio PRIMERO (antes de cualquier escaneo)
        observarResultadosDelRepositorio()

        // 2. Registrar ContentObserver para detección en tiempo real
        contentObserver.registrar()

        // 3. Programar escaneos periódicos con WorkManager
        MusicScanWorker.programarEscaneosPeriodicos(context)

        _estaInicializado.value = true
        Log.d(TAG, "✅ Sistema de escaneo inicializado")

        // 4. Ejecutar escaneo inicial si se solicita
        if (ejecutarEscaneoInicial) {
            Log.d(TAG, "▶️ Ejecutando escaneo inicial...")
            escanearAhora()
        }
    }

    /** Detiene todo el sistema de escaneo. */
    fun detener() {
        Log.d(TAG, "🛑 Deteniendo sistema de escaneo...")

        contentObserver.desregistrar()
        MusicScanWorker.cancelarTodos(context)

        _estaInicializado.value = false
    }

    /**
     * Ejecuta un escaneo manual inmediato. Usa el repositorio directamente para máxima velocidad.
     */
    fun escanearAhora() {
        scope.launch {
            Log.d(TAG, "▶️ Iniciando escaneo manual")
            try {
                val resultado = musicRepository.escanearYGuardarMusica()
                if (resultado != null) {
                    _ultimoResultado.value =
                        ResultadoEscaneo(
                            nuevas = resultado.nuevas,
                            eliminadas = resultado.eliminadas,
                            actualizadas = resultado.actualizadas,
                            tiempoMs = resultado.tiempoMs,
                            exitoso = true,
                        )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en escaneo manual", e)
                _ultimoResultado.value =
                    ResultadoEscaneo(
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
     * Ejecuta un escaneo en segundo plano usando WorkManager. Útil cuando no necesitas el resultado
     * inmediato.
     */
    fun escanearEnSegundoPlano() {
        MusicScanWorker.ejecutarEscaneoInmediato(context)
    }

    /** Habilita o deshabilita la detección automática de nuevos archivos. */
    fun setDeteccionAutomatica(habilitada: Boolean) {
        if (habilitada) {
            contentObserver.registrar()
        } else {
            contentObserver.desregistrar()
        }
    }

    /**
     * Configura el intervalo de escaneos periódicos.
     *
     * @param horas Intervalo en horas (mínimo 1, máximo 24)
     */
    fun configurarIntervaloPerodico(horas: Long) {
        val horasValidas = horas.coerceIn(1, 24)
        MusicScanWorker.cancelarPeriodicos(context)
        MusicScanWorker.programarEscaneosPeriodicos(context, horasValidas)
    }

    /** Observa el estado de los trabajos de WorkManager. */
    fun observarTrabajosEnCola(): Flow<List<WorkInfo>> {
        return MusicScanWorker.observarEstado(context)
    }

    /** Limpia el último resultado (útil para resetear UI). */
    fun limpiarUltimoResultado() {
        _ultimoResultado.value = null
        musicRepository.reiniciarEstado()
    }

    // ==================== INTERNOS ====================

    private fun observarResultadosDelRepositorio() {
        scope.launch {
            musicRepository.estadoEscaneo.collect { estado ->
                when (estado) {
                    is LocalMusicRepository.EstadoEscaneo.Completado -> {
                        _ultimoResultado.value =
                            ResultadoEscaneo(
                                nuevas = estado.nuevas,
                                eliminadas = estado.eliminadas,
                                actualizadas = estado.actualizadas,
                                tiempoMs = estado.tiempoMs,
                                exitoso = true,
                            )
                    }
                    is LocalMusicRepository.EstadoEscaneo.Error -> {
                        _ultimoResultado.value =
                            ResultadoEscaneo(
                                nuevas = 0,
                                eliminadas = 0,
                                actualizadas = 0,
                                tiempoMs = 0,
                                exitoso = false,
                                error = estado.mensaje,
                            )
                    }
                    else -> {
                        /* Ignorar otros estados */
                    }
                }
            }
        }
    }
}
