package com.example.freeplayerm.ui.features.reproductor

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.GeniusRepository
import com.example.freeplayerm.utils.MediaItemHelper
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ✅ VERSIÓN MEJORADA - Conectada al MusicService
 *
 * Mantiene toda la lógica original (letras, artista, rotación) pero ahora
 * se comunica correctamente con el servicio para mostrar notificaciones.
 */
@HiltViewModel
class ReproductorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceComponentName: ComponentName,
    private val geniusRepository: GeniusRepository,
    private val letraDao: LetraDao,
    private val cancionDao: CancionDao,
    private val mediaItemHelper: MediaItemHelper
) : ViewModel() {

    companion object {
        private const val TAG = "ReproductorViewModel"
    }

    // ==================== ESTADO UI ====================
    private val _estadoUi = MutableStateFlow(ReproductorEstado(cancionActual = null))
    val estadoUi = _estadoUi.asStateFlow()

    private val _letra = MutableStateFlow<String?>(null)
    val letra = _letra.asStateFlow()

    private val _infoArtista = MutableStateFlow<String?>("Cargando información...")
    val infoArtista = _infoArtista.asStateFlow()

    // ==================== REPRODUCTOR ====================
    // ✅ CAMBIO PRINCIPAL: MediaController en lugar de Player directo
    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    // Helper para acceder al player de forma segura
    private val player: Player?
        get() = mediaController

    // ==================== ESTADO INTERNO ====================
    private var colaDeReproduccion: List<CancionConArtista> = emptyList()
    private var actualizadorDeProgresoJob: Job? = null
    private var trabajoDeRotacion: Job? = null
    private var trabajoDeLetra: Job? = null
    private var trabajoDeArtista: Job? = null

    init {
        Log.d(TAG, "🎵 Inicializando ReproductorViewModel")
        conectarAlServicio()
    }

    // ==================== CONEXIÓN AL SERVICIO ====================

    /**
     * ✅ Conecta al MusicService usando MediaController
     * Esta es la pieza clave que faltaba para que funcionen las notificaciones
     */
    private fun conectarAlServicio() {
        try {
            Log.d(TAG, "🔌 Conectando al MusicService...")

            val sessionToken = SessionToken(context, serviceComponentName)
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            controllerFuture.addListener({
                try {
                    mediaController = controllerFuture.get()
                    Log.d(TAG, "✅ MediaController conectado exitosamente")

                    // Configurar listeners del player (tu lógica original)
                    escucharEventosDelReproductor()

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error obteniendo MediaController: ${e.message}", e)
                }
            }, MoreExecutors.directExecutor())

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error conectando al servicio: ${e.message}", e)
        }
    }

    // ==================== LISTENERS DEL PLAYER ====================

    /**
     * ✅ Tu lógica original, ahora conectada al servicio
     */
    private fun escucharEventosDelReproductor() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "▶️ Estado reproducción: $isPlaying")
                _estadoUi.update { it.copy(estaReproduciendo = isPlaying) }

                if (isPlaying) {
                    iniciarActualizadorDeProgreso()
                } else {
                    detenerActualizadorDeProgreso()
                }
                gestionarRotacionVinilo(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log.d(TAG, "🎵 Transición de canción: ${mediaItem?.mediaMetadata?.title}")
                actualizarCancionActual()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val nuevoModo = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> ModoRepeticion.NO_REPETIR
                    Player.REPEAT_MODE_ALL -> ModoRepeticion.REPETIR_LISTA
                    Player.REPEAT_MODE_ONE -> ModoRepeticion.REPETIR_CANCION
                    else -> ModoRepeticion.NO_REPETIR
                }
                _estadoUi.update { it.copy(modoRepeticion = nuevoModo) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                val nuevoModo = if (shuffleModeEnabled) {
                    ModoReproduccion.ALEATORIO
                } else {
                    ModoReproduccion.EN_ORDEN
                }
                _estadoUi.update { it.copy(modoReproduccion = nuevoModo) }
            }
        })
    }

    // ==================== MANEJO DE EVENTOS ====================

    /**
     * ✅ Tu lógica original de eventos, con mejoras en logs
     */
    fun enEvento(evento: ReproductorEvento) {
        when (evento) {
            is ReproductorEvento.SeleccionarCancion -> {
                // Deprecated - usar EstablecerColaYReproducir
                Log.w(TAG, "⚠️ SeleccionarCancion está deprecated, usa EstablecerColaYReproducir")
            }

            is ReproductorEvento.EstablecerColaYReproducir -> {
                establecerColaYReproducir(evento.cola, evento.cancionInicial)
            }

            ReproductorEvento.ReproducirPausar -> {
                togglePlayPause()
            }

            ReproductorEvento.SiguienteCancion -> {
                player?.seekToNextMediaItem()
                Log.d(TAG, "⏭️ Siguiente canción")
            }

            ReproductorEvento.CancionAnterior -> {
                player?.seekToPreviousMediaItem()
                Log.d(TAG, "⏮️ Canción anterior")
            }

            is ReproductorEvento.OnScrub -> {
                _estadoUi.update {
                    it.copy(
                        isScrubbing = true,
                        progresoActualMs = evento.position.toLong()
                    )
                }
            }

            is ReproductorEvento.OnScrubFinished -> {
                player?.seekTo(evento.position.toLong())
                _estadoUi.update { it.copy(isScrubbing = false) }
                Log.d(TAG, "🎯 Seeking a: ${evento.position}ms")
            }

            ReproductorEvento.CambiarModoReproduccion -> {
                toggleModoReproduccion()
            }

            ReproductorEvento.CambiarModoRepeticion -> {
                toggleModoRepeticion()
            }

            else -> {
                Log.w(TAG, "⚠️ Evento no manejado: $evento")
            }
        }
    }

    // ==================== LÓGICA DE REPRODUCCIÓN ====================

    /**
     * ✅ MÉTODO MEJORADO - Ahora con mejor logging y manejo de errores
     */
    private fun establecerColaYReproducir(
        cola: List<CancionConArtista>,
        cancionInicial: CancionConArtista
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📋 Estableciendo cola de ${cola.size} canciones")
                colaDeReproduccion = cola

                // Crear MediaItems con metadatos completos (tu lógica original)
                val mediaItems = cola.map { cancion ->
                    mediaItemHelper.crearMediaItemDesdeEntidad(cancion)
                }

                player?.let { p ->
                    p.setMediaItems(mediaItems)

                    val indiceInicial = cola.indexOf(cancionInicial)
                    if (indiceInicial != -1) {
                        p.seekToDefaultPosition(indiceInicial)
                        p.prepare()
                        p.play()
                        Log.d(TAG, "✅ Reproducción iniciada desde índice $indiceInicial")
                        actualizarCancionActual()
                    } else {
                        Log.e(TAG, "❌ Canción inicial no encontrada en la cola")
                    }
                } ?: run {
                    Log.e(TAG, "❌ MediaController no disponible")
                }

            } catch (e: Exception) {
                Log.e(TAG, "💥 Error estableciendo cola: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Toggle play/pause mejorado
     */
    private fun togglePlayPause() {
        player?.let { p ->
            val estaReproduciendo = p.isPlaying

            if (estaReproduciendo) {
                p.pause()
                Log.d(TAG, "⏸️ Pausado")
            } else {
                p.play()
                Log.d(TAG, "▶️ Reproduciendo")
            }

            _estadoUi.update { it.copy(estaReproduciendo = !estaReproduciendo) }
        } ?: run {
            Log.w(TAG, "⚠️ No se puede play/pause - MediaController no disponible")
        }
    }

    // ==================== ANIMACIÓN DE VINILO ====================

    /**
     * ✅ Tu lógica original de rotación - sin cambios
     */
    private fun gestionarRotacionVinilo(estaReproduciendo: Boolean) {
        trabajoDeRotacion?.cancel()
        if (estaReproduciendo) {
            trabajoDeRotacion = viewModelScope.launch {
                while (true) {
                    val anguloActual = _estadoUi.value.anguloRotacionVinilo
                    val incremento = 360f / (10000f / 16f)
                    _estadoUi.update {
                        it.copy(
                            anguloRotacionVinilo = (anguloActual + incremento) % 360f
                        )
                    }
                    delay(16)
                }
            }
        }
    }

    // ==================== ACTUALIZACIÓN DE CANCIÓN ====================

    /**
     * ✅ Tu lógica original de actualización - sin cambios
     */
    private fun actualizarCancionActual() {
        val indiceActual = player?.currentMediaItemIndex ?: -1

        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()

        if (indiceActual >= 0 && indiceActual < colaDeReproduccion.size) {
            val cancionActual = colaDeReproduccion[indiceActual]

            Log.d(TAG, "🎵 Canción actual: ${cancionActual.cancion.titulo}")

            _estadoUi.update {
                it.copy(
                    cancionActual = cancionActual,
                    anguloRotacionVinilo = 0f
                )
            }

            _letra.value = "Cargando letra..."
            _infoArtista.value = "Cargando información..."

            // Sincronizar con Genius (tu lógica original)
            viewModelScope.launch {
                try {
                    geniusRepository.sincronizarCancionAlReproducir(cancionActual)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sincronizando con Genius: ${e.message}")
                }
            }

            // Cargar letra desde base de datos
            trabajoDeLetra = viewModelScope.launch {
                try {
                    letraDao.obtenerLetraPorIdCancion(cancionActual.cancion.idCancion)
                        .distinctUntilChanged()
                        .collect { letraEntity ->
                            if (letraEntity != null && letraEntity.letra.isNotBlank()) {
                                _letra.value = letraEntity.letra
                                Log.d(TAG, "✅ Letra cargada")
                            } else {
                                delay(3000)
                                if (_letra.value == "Cargando letra...") {
                                    _letra.value = "Letra no disponible."
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error cargando letra: ${e.message}")
                    _letra.value = "Error cargando letra."
                }
            }

            // Cargar información del artista
            val artistaId = cancionActual.cancion.idArtista
            if (artistaId != null) {
                trabajoDeArtista = viewModelScope.launch {
                    try {
                        cancionDao.obtenerArtistaPorIdFlow(artistaId)
                            .distinctUntilChanged()
                            .collect { artistaEntity ->
                                if (artistaEntity?.descripcion.isNullOrBlank()) {
                                    delay(3000)
                                    if (_infoArtista.value == "Cargando información...") {
                                        _infoArtista.value = "Información del artista no disponible."
                                    }
                                } else {
                                    _infoArtista.value = artistaEntity.descripcion
                                    Log.d(TAG, "✅ Info artista cargada")
                                }
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error cargando info artista: ${e.message}")
                        _infoArtista.value = "Error cargando información."
                    }
                }
            } else {
                _infoArtista.value = "Información del artista no disponible."
            }

        } else {
            Log.d(TAG, "⚠️ Sin canción actual válida")
            _estadoUi.update { it.copy(cancionActual = null, estaReproduciendo = false) }
            _letra.value = null
            _infoArtista.value = null
        }
    }

    // ==================== ACTUALIZACIÓN DE PROGRESO ====================

    /**
     * ✅ Tu lógica original - sin cambios
     */
    private fun iniciarActualizadorDeProgreso() {
        detenerActualizadorDeProgreso()
        actualizadorDeProgresoJob = viewModelScope.launch {
            while (true) {
                if (!_estadoUi.value.isScrubbing) {
                    player?.let { p ->
                        _estadoUi.update { it.copy(progresoActualMs = p.currentPosition) }
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun detenerActualizadorDeProgreso() {
        actualizadorDeProgresoJob?.cancel()
    }

    // ==================== MODOS DE REPRODUCCIÓN ====================

    /**
     * ✅ Tu lógica original mejorada con logs
     */
    private fun toggleModoReproduccion() {
        player?.let { p ->
            val nuevoModo = if (_estadoUi.value.modoReproduccion == ModoReproduccion.EN_ORDEN) {
                ModoReproduccion.ALEATORIO
            } else {
                ModoReproduccion.EN_ORDEN
            }

            p.shuffleModeEnabled = nuevoModo == ModoReproduccion.ALEATORIO
            _estadoUi.update { it.copy(modoReproduccion = nuevoModo) }

            Log.d(TAG, "🔀 Modo reproducción: $nuevoModo")
        }
    }

    private fun toggleModoRepeticion() {
        player?.let { p ->
            val nuevoModo = when (_estadoUi.value.modoRepeticion) {
                ModoRepeticion.NO_REPETIR -> ModoRepeticion.REPETIR_LISTA
                ModoRepeticion.REPETIR_LISTA -> ModoRepeticion.REPETIR_CANCION
                ModoRepeticion.REPETIR_CANCION -> ModoRepeticion.NO_REPETIR
            }

            p.repeatMode = when (nuevoModo) {
                ModoRepeticion.NO_REPETIR -> Player.REPEAT_MODE_OFF
                ModoRepeticion.REPETIR_LISTA -> Player.REPEAT_MODE_ALL
                ModoRepeticion.REPETIR_CANCION -> Player.REPEAT_MODE_ONE
            }

            _estadoUi.update { it.copy(modoRepeticion = nuevoModo) }
            Log.d(TAG, "🔁 Modo repetición: $nuevoModo")
        }
    }

    // ==================== MÉTODOS PÚBLICOS ADICIONALES ====================

    /**
     * ✅ NUEVO: Para reproducir una sola canción (útil para tests)
     */
    fun reproducirCancion(cancion: CancionConArtista) {
        establecerColaYReproducir(listOf(cancion), cancion)
    }

    /**
     * ✅ NUEVO: Verificar si el reproductor está listo
     */
    fun estaConectado(): Boolean = mediaController != null

    // ==================== LIMPIEZA ====================

    override fun onCleared() {
        Log.d(TAG, "🧹 Limpiando ReproductorViewModel")

        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()
        trabajoDeRotacion?.cancel()
        actualizadorDeProgresoJob?.cancel()

        try {
            MediaController.releaseFuture(controllerFuture)
            Log.d(TAG, "✅ MediaController liberado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error liberando MediaController: ${e.message}")
        }

        super.onCleared()
    }
}