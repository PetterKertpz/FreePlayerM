package com.example.freeplayerm.ui.features.reproductor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReproductorViewModel @Inject constructor(
    private val player: Player
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(ReproductorEstado(cancionActual = null))
    val estadoUi = _estadoUi.asStateFlow()

    private var colaDeReproduccion: List<CancionConArtista> = emptyList()
    private var actualizadorDeProgresoJob: Job? = null

    init {
        escucharEventosDelReproductor()
    }

    private fun escucharEventosDelReproductor() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _estadoUi.update { it.copy(estaReproduciendo = isPlaying) }
                if (isPlaying) iniciarActualizadorDeProgreso() else detenerActualizadorDeProgreso()
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
                // pero lo mantenemos por si se usa en otro sitio.
            }
            is ReproductorEvento.EstablecerColaYReproducir -> {
                colaDeReproduccion = evento.cola
                val mediaItems = evento.cola.map { MediaItem.fromUri(it.cancion.archivoPath ?: "") }
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
                // 1. Obtenemos el estado actual de la UI.
                val estadoActual = _estadoUi.value

                // 2. **Actualizamos la UI inmediatamente (Optimismo)**.
                //    El icono en la pantalla cambiará al instante.
                _estadoUi.update { it.copy(estaReproduciendo = !estadoActual.estaReproduciendo) }

                // 3. Enviamos el comando al reproductor basándonos en el estado *anterior*.
                //    Esto asegura que enviamos la acción correcta (si estaba sonando, pausar, y viceversa).
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
                        // Actualizamos el progreso visualmente de forma instantánea
                        progresoActualMs = evento.position.toLong()
                    )
                }
            }
            is ReproductorEvento.OnScrubFinished -> {
                // Cuando el usuario suelta el dedo, le decimos al reproductor que salte a esa posición
                player.seekTo(evento.position.toLong())
                _estadoUi.update { it.copy(isScrubbing = false) }
            }
            ReproductorEvento.CambiarModoReproduccion -> toggleModoReproduccion()
            ReproductorEvento.CambiarModoRepeticion -> toggleModoRepeticion()
            else -> {} // Otros eventos como Detener o AlternarFavorito
        }
    }

    private fun actualizarCancionActual() {
        val indiceActual = player.currentMediaItemIndex
        if (indiceActual >= 0 && indiceActual < colaDeReproduccion.size) {
            _estadoUi.update { it.copy(cancionActual = colaDeReproduccion[indiceActual]) }
        } else {
            _estadoUi.update { it.copy(cancionActual = null, estaReproduciendo = false) }
        }
    }

    private fun iniciarActualizadorDeProgreso() {
        detenerActualizadorDeProgreso()
        actualizadorDeProgresoJob = viewModelScope.launch {
            while (true) {
                // --- ✅ CAMBIO CLAVE AQUÍ ---
                // Solo actualizamos el progreso si el usuario NO está deslizando
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
        // No liberamos el player aquí, porque el servicio lo gestiona.
        super.onCleared()
    }
}