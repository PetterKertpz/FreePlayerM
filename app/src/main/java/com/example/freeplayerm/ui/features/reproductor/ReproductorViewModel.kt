package com.example.freeplayerm.ui.features.reproductor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.GeniusRepository
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
    private val geniusRepository: GeniusRepository, // <-- AÑADIDO
    private val letraDao: LetraDao, // <-- AÑADIDO
    private val cancionDao: CancionDao
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
                // <-- MODIFICADO: Llamamos a la nueva función aquí
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
    private fun gestionarRotacionVinilo(estaReproduciendo: Boolean) {
        trabajoDeRotacion?.cancel()
        if (estaReproduciendo) {
            trabajoDeRotacion = viewModelScope.launch {
                while (true) {
                    val anguloActual = _estadoUi.value.anguloRotacionVinilo
                    // Incremento para una rotación completa cada 10 segundos
                    val incremento = 360f / (10000f / 16f)
                    _estadoUi.update {
                        it.copy(
                            anguloRotacionVinilo = (anguloActual + incremento) % 360f
                        )
                    }
                    delay(16) // Actualiza a ~60 FPS
                }
            }
        }
    }
    private fun actualizarCancionActual() {
        val indiceActual = player.currentMediaItemIndex

        // 1. Cancelamos los observadores de la canción anterior
        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()

        if (indiceActual >= 0 && indiceActual < colaDeReproduccion.size) {
            val cancionActual = colaDeReproduccion[indiceActual]

            // 2. Actualizamos la UI con la nueva canción
            _estadoUi.update {
                it.copy(
                    cancionActual = cancionActual,
                    anguloRotacionVinilo = 0f
                )
            }

            // 3. Ponemos los estados de texto en "Cargando"
            _letra.value = "Cargando letra..."
            _infoArtista.value = "Cargando información..."

            // 4. Lanzamos la sincronización en segundo plano (para API, Jsoup, etc.)
            viewModelScope.launch {
                geniusRepository.sincronizarCancionAlReproducir(cancionActual)
            }

            // 5. Observamos la LETRA desde la base de datos
            trabajoDeLetra = viewModelScope.launch {
                letraDao.obtenerLetraPorIdCancion(cancionActual.cancion.idCancion)
                    .distinctUntilChanged()
                    .collect { letraEntity ->
                        // Cuando Room nos da la letra (ahora o después), actualizamos
                        if (letraEntity != null && letraEntity.letra.isNotBlank()) {
                            _letra.value = letraEntity.letra
                        } else {
                            // Damos un tiempo de gracia por si la red tarda
                            delay(3000)
                            if (_letra.value == "Cargando letra...") {
                                _letra.value = "Letra no disponible."
                            }
                        }
                    }
            }

            // 6. Observamos la INFO DEL ARTISTA desde la base de datos
            val artistaId = cancionActual.cancion.idArtista
            if (artistaId != null) {
                trabajoDeArtista = viewModelScope.launch {
                    cancionDao.obtenerArtistaPorIdFlow(artistaId)
                        .distinctUntilChanged()
                        .collect { artistaEntity ->
                            // Cuando el repo actualice al artista, esto se disparará
                            if (artistaEntity?.descripcion.isNullOrBlank()) {
                                // Damos tiempo de gracia
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
            // No hay canción sonando
            _estadoUi.update { it.copy(cancionActual = null, estaReproduciendo = false) }
            _letra.value = null
            _infoArtista.value = null
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
        trabajoDeLetra?.cancel()
        trabajoDeArtista?.cancel()
        // No liberamos el player aquí, porque el servicio lo gestiona.
        super.onCleared()
    }
}