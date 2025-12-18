package com.example.freeplayerm.ui.features.reproductor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.GeniusRepository
import com.example.freeplayerm.utils.MediaItemHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReproductorViewModel @Inject constructor(
    private val player: Player,
    private val geniusRepository: GeniusRepository,
    private val letraDao: LetraDao,
    private val cancionDao: CancionDao,
    private val mediaItemHelper: MediaItemHelper // <-- INYECTADO
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(ReproductorEstado(cancionActual = null))
    val estadoUi = _estadoUi.asStateFlow()

    private val _letra = MutableStateFlow<String?>(null)
    val letra = _letra.asStateFlow()

    private val _infoArtista = MutableStateFlow<String?>("Cargando información...")
    val infoArtista = _infoArtista.asStateFlow()

    private var colaDeReproduccion: List<CancionConArtista> = emptyList()
    private var actualizadorDeProgresoJob: Job? = null
    private var trabajoDeRotacion: Job? = null
    private var trabajoDeLetra: Job? = null
    private var trabajoDeArtista: Job? = null

    init {
        escucharEventosDelReproductor()
    }

    private fun escucharEventosDelReproductor() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
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
                actualizarCancionActual()
            }
        })
    }

    fun enEvento(evento: ReproductorEvento) {
        when (evento) {
            is ReproductorEvento.SeleccionarCancion -> {
                // Este evento ahora es manejado por el nuevo 'EstablecerColaYReproducir'
            }
            is ReproductorEvento.EstablecerColaYReproducir -> {
                colaDeReproduccion = evento.cola

                // --- CAMBIO PRINCIPAL: Usamos el helper para crear MediaItems con METADATOS ---
                val mediaItems = evento.cola.map { cancion ->
                    mediaItemHelper.crearMediaItemDesdeEntidad(cancion)
                }

                player.setMediaItems(mediaItems)

                val indiceInicial = evento.cola.indexOf(evento.cancionInicial)
                if (indiceInicial != -1) {
                    player.seekToDefaultPosition(indiceInicial)
                    player.prepare()
                    player.play()
                    actualizarCancionActual()
                }
            }

            ReproductorEvento.ReproducirPausar -> {
                val estadoActual = _estadoUi.value
                _estadoUi.update { it.copy(estaReproduciendo = !estadoActual.estaReproduciendo) }

                if (estadoActual.estaReproduciendo) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            ReproductorEvento.SiguienteCancion -> player.seekToNextMediaItem()
            ReproductorEvento.CancionAnterior -> player.seekToPreviousMediaItem()
            is ReproductorEvento.OnScrub -> {
                _estadoUi.update {
                    it.copy(
                        isScrubbing = true,
                        progresoActualMs = evento.position.toLong()
                    )
                }
            }
            is ReproductorEvento.OnScrubFinished -> {
                player.seekTo(evento.position.toLong())
                _estadoUi.update { it.copy(isScrubbing = false) }
            }
            ReproductorEvento.CambiarModoReproduccion -> toggleModoReproduccion()
            ReproductorEvento.CambiarModoRepeticion -> toggleModoRepeticion()
            else -> {}
        }
    }

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

    private fun actualizarCancionActual() {
        val indiceActual = player.currentMediaItemIndex

        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()

        if (indiceActual >= 0 && indiceActual < colaDeReproduccion.size) {
            val cancionActual = colaDeReproduccion[indiceActual]

            _estadoUi.update {
                it.copy(
                    cancionActual = cancionActual,
                    anguloRotacionVinilo = 0f
                )
            }

            _letra.value = "Cargando letra..."
            _infoArtista.value = "Cargando información..."

            viewModelScope.launch {
                geniusRepository.sincronizarCancionAlReproducir(cancionActual)
            }

            trabajoDeLetra = viewModelScope.launch {
                letraDao.obtenerLetraPorIdCancion(cancionActual.cancion.idCancion)
                    .distinctUntilChanged()
                    .collect { letraEntity ->
                        if (letraEntity != null && letraEntity.letra.isNotBlank()) {
                            _letra.value = letraEntity.letra
                        } else {
                            delay(3000)
                            if (_letra.value == "Cargando letra...") {
                                _letra.value = "Letra no disponible."
                            }
                        }
                    }
            }

            val artistaId = cancionActual.cancion.idArtista
            if (artistaId != null) {
                trabajoDeArtista = viewModelScope.launch {
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
                            }
                        }
                }
            } else {
                _infoArtista.value = "Información del artista no disponible."
            }

        } else {
            _estadoUi.update { it.copy(cancionActual = null, estaReproduciendo = false) }
            _letra.value = null
            _infoArtista.value = null
        }
    }

    private fun iniciarActualizadorDeProgreso() {
        detenerActualizadorDeProgreso()
        actualizadorDeProgresoJob = viewModelScope.launch {
            while (true) {
                if (!_estadoUi.value.isScrubbing) {
                    _estadoUi.update { it.copy(progresoActualMs = player.currentPosition) }
                }
                delay(1000L)
            }
        }
    }

    private fun detenerActualizadorDeProgreso() {
        actualizadorDeProgresoJob?.cancel()
    }

    private fun toggleModoReproduccion() {
        val nuevoModo = if (_estadoUi.value.modoReproduccion == ModoReproduccion.EN_ORDEN) {
            ModoReproduccion.ALEATORIO
        } else {
            ModoReproduccion.EN_ORDEN
        }
        player.shuffleModeEnabled = nuevoModo == ModoReproduccion.ALEATORIO
        _estadoUi.update { it.copy(modoReproduccion = nuevoModo) }
    }

    private fun toggleModoRepeticion() {
        val nuevoModo = when (_estadoUi.value.modoRepeticion) {
            ModoRepeticion.NO_REPETIR -> ModoRepeticion.REPETIR_LISTA
            ModoRepeticion.REPETIR_LISTA -> ModoRepeticion.REPETIR_CANCION
            ModoRepeticion.REPETIR_CANCION -> ModoRepeticion.NO_REPETIR
        }
        player.repeatMode = when (nuevoModo) {
            ModoRepeticion.NO_REPETIR -> Player.REPEAT_MODE_OFF
            ModoRepeticion.REPETIR_LISTA -> Player.REPEAT_MODE_ALL
            ModoRepeticion.REPETIR_CANCION -> Player.REPEAT_MODE_ONE
        }
        _estadoUi.update { it.copy(modoRepeticion = nuevoModo) }
    }

    override fun onCleared() {
        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()
        super.onCleared()
    }
}