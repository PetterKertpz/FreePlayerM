package com.example.freeplayerm.ui.features.reproductor

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.FavoritoEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.GeniusRepository
import com.example.freeplayerm.utils.MediaItemHelper
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * ⚡ REPRODUCTOR VIEWMODEL - v3.0
 *
 * Mejoras implementadas:
 * ✅ Soporte para 3 modos de panel (MINIMIZADO, NORMAL, EXPANDIDO)
 * ✅ Estado unificado (letra, info, enlaces en ReproductorEstado)
 * ✅ Detección de scroll para minimización automática
 * ✅ Manejo de enlaces externos (Genius, YouTube, Google)
 * ✅ Carga de datos expandidos bajo demanda
 *
 * @version 3.0 - Sistema de 3 Modos
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
        private const val TAG = "ReproductorVM"
        private const val PROGRESO_UPDATE_MS = 250L
        private const val PROGRESO_EMIT_THRESHOLD_MS = 1000L
        private const val TIMEOUT_DATOS_MS = 5000L
        private const val USUARIO_DEFAULT = 1
    }

    // ==================== ESTADO UI UNIFICADO ====================

    private val _estadoUi = MutableStateFlow(ReproductorEstado())
    val estadoUi: StateFlow<ReproductorEstado> = _estadoUi.asStateFlow()

    // ==================== EFECTOS (ONE-SHOT) ====================

    private val _efectos = Channel<ReproductorEfecto>(Channel.BUFFERED)
    val efectos = _efectos.receiveAsFlow()

    // ==================== MEDIA CONTROLLER ====================

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var playerListener: Player.Listener? = null

    private val player: Player? get() = mediaController

    // ==================== ESTADO INTERNO ====================

    private val colaCanciones = ConcurrentHashMap<String, CancionConArtista>()
    private var actualizadorDeProgresoJob: Job? = null
    private var datosAdicionalesJob: Job? = null
    private var ultimoSegundoEmitido: Long = -1L

    // ==================== INICIALIZACIÓN ====================

    init {
        conectarAlServicio()
        observarEstadoFavoritos()
    }

    private fun conectarAlServicio() {
        try {
            val sessionToken = SessionToken(context, serviceComponentName)
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            controllerFuture.addListener({
                try {
                    mediaController = controllerFuture.get()
                    viewModelScope.launch(Dispatchers.Main.immediate) {
                        configurarListenersDelPlayer()
                        sincronizarEstadoInicial()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error conectando al reproductor", e)
                    enviarEfecto(ReproductorEfecto.Error("Error conectando al reproductor"))
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            Log.e(TAG, "Error crítico en reproductor", e)
            enviarEfecto(ReproductorEfecto.Error("Error crítico en reproductor"))
        }
    }

    private fun configurarListenersDelPlayer() {
        playerListener?.let { player?.removeListener(it) }

        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _estadoUi.update { it.copy(estaReproduciendo = isPlaying) }
                if (isPlaying) iniciarActualizadorDeProgreso() else detenerActualizadorDeProgreso()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                actualizarCancionActualDesdePlayer(mediaItem)
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
                val nuevoModo = if (shuffleModeEnabled) ModoReproduccion.ALEATORIO else ModoReproduccion.EN_ORDEN
                _estadoUi.update { it.copy(modoReproduccion = nuevoModo) }
            }

            override fun onPlayerErrorChanged(error: androidx.media3.common.PlaybackException?) {
                error?.let {
                    Log.e(TAG, "Error del player: ${it.message}", it)
                    enviarEfecto(ReproductorEfecto.Error("Error: ${it.message}"))
                }
            }
        }

        player?.addListener(playerListener!!)
    }

    private fun sincronizarEstadoInicial() {
        player?.let { p ->
            _estadoUi.update {
                it.copy(
                    estaReproduciendo = p.isPlaying,
                    modoReproduccion = if (p.shuffleModeEnabled) ModoReproduccion.ALEATORIO else ModoReproduccion.EN_ORDEN,
                    modoRepeticion = when (p.repeatMode) {
                        Player.REPEAT_MODE_OFF -> ModoRepeticion.NO_REPETIR
                        Player.REPEAT_MODE_ALL -> ModoRepeticion.REPETIR_LISTA
                        Player.REPEAT_MODE_ONE -> ModoRepeticion.REPETIR_CANCION
                        else -> ModoRepeticion.NO_REPETIR
                    }
                )
            }
            p.currentMediaItem?.let { actualizarCancionActualDesdePlayer(it) }
        }
    }

    // ==================== MANEJO DE EVENTOS ====================

    fun onEvento(evento: ReproductorEvento) {
        when (evento) {
            is ReproductorEvento.Reproduccion -> manejarEventoReproduccion(evento)
            is ReproductorEvento.Navegacion -> manejarEventoNavegacion(evento)
            is ReproductorEvento.Configuracion -> manejarEventoConfiguracion(evento)
            is ReproductorEvento.Panel -> manejarEventoPanel(evento)
            is ReproductorEvento.Enlaces -> manejarEventoEnlaces(evento)
        }
    }

    // ==================== EVENTOS DE REPRODUCCIÓN ====================

    private fun manejarEventoReproduccion(evento: ReproductorEvento.Reproduccion) {
        val p = player ?: run {
            enviarEfecto(ReproductorEfecto.MostrarToast("Reproductor no disponible"))
            return
        }

        when (evento) {
            is ReproductorEvento.Reproduccion.EstablecerColaYReproducir -> {
                establecerColaYReproducir(evento.cola, evento.cancionInicial)
            }
            is ReproductorEvento.Reproduccion.ReproducirPausar -> togglePlayPause()
            is ReproductorEvento.Reproduccion.SiguienteCancion -> {
                if (p.hasNextMediaItem() || p.repeatMode != Player.REPEAT_MODE_OFF) {
                    p.seekToNextMediaItem()
                } else {
                    enviarEfecto(ReproductorEfecto.MostrarToast("No hay más canciones"))
                }
            }
            is ReproductorEvento.Reproduccion.CancionAnterior -> {
                when {
                    p.currentPosition > 3000 -> p.seekTo(0)
                    p.hasPreviousMediaItem() -> p.seekToPreviousMediaItem()
                }
            }
            is ReproductorEvento.Reproduccion.Detener -> detenerReproduccion()
        }
    }

    // ==================== EVENTOS DE NAVEGACIÓN ====================

    private fun manejarEventoNavegacion(evento: ReproductorEvento.Navegacion) {
        when (evento) {
            is ReproductorEvento.Navegacion.OnScrub -> {
                _estadoUi.update {
                    it.copy(
                        isScrubbing = true,
                        progresoTemporalMs = evento.positionMs.coerceAtLeast(0)
                    )
                }
            }
            is ReproductorEvento.Navegacion.OnScrubFinished -> {
                val posicion = evento.positionMs.coerceAtLeast(0)
                player?.seekTo(posicion)
                _estadoUi.update {
                    it.copy(
                        isScrubbing = false,
                        progresoTemporalMs = null,
                        progresoActualMs = posicion
                    )
                }
                ultimoSegundoEmitido = -1L
            }
        }
    }

    // ==================== EVENTOS DE CONFIGURACIÓN ====================

    private fun manejarEventoConfiguracion(evento: ReproductorEvento.Configuracion) {
        when (evento) {
            is ReproductorEvento.Configuracion.CambiarModoReproduccion -> toggleModoReproduccion()
            is ReproductorEvento.Configuracion.CambiarModoRepeticion -> toggleModoRepeticion()
            is ReproductorEvento.Configuracion.AlternarFavorito -> toggleFavorito()
        }
    }

    // ==================== EVENTOS DEL PANEL ====================

    private fun manejarEventoPanel(evento: ReproductorEvento.Panel) {
        when (evento) {
            is ReproductorEvento.Panel.CambiarModo -> {
                _estadoUi.update { it.copy(modoPanel = evento.nuevoModo) }
                if (evento.nuevoModo == ModoPanelReproductor.EXPANDIDO) {
                    cargarDatosExpandidosSiNecesario()
                }
            }
            is ReproductorEvento.Panel.Expandir -> {
                _estadoUi.update { it.copy(modoPanel = ModoPanelReproductor.EXPANDIDO) }
                cargarDatosExpandidosSiNecesario()
            }
            is ReproductorEvento.Panel.Colapsar -> {
                _estadoUi.update { it.copy(modoPanel = ModoPanelReproductor.NORMAL) }
            }
            is ReproductorEvento.Panel.NotificarScroll -> {
                _estadoUi.update { it.copy(isScrollActivo = evento.scrollActivo) }
            }
            is ReproductorEvento.Panel.CambiarTab -> {
                _estadoUi.update { it.copy(tabExpandidoActivo = evento.tab) }
            }
        }
    }

    // ==================== EVENTOS DE ENLACES ====================

    private fun manejarEventoEnlaces(evento: ReproductorEvento.Enlaces) {
        val estado = _estadoUi.value
        val url = when (evento) {
            is ReproductorEvento.Enlaces.AbrirGenius -> estado.enlaceGenius
            is ReproductorEvento.Enlaces.AbrirYoutube -> estado.enlaceYoutube
            is ReproductorEvento.Enlaces.AbrirGoogle -> estado.enlaceGoogle
        }

        if (url.isNullOrBlank()) {
            enviarEfecto(ReproductorEfecto.MostrarToast("Enlace no disponible"))
        } else {
            enviarEfecto(ReproductorEfecto.AbrirUrl(url))
        }
    }

    // ==================== LÓGICA DE REPRODUCCIÓN ====================

    private fun establecerColaYReproducir(cola: List<CancionConArtista>, cancionInicial: CancionConArtista) {
        val p = player ?: run {
            enviarEfecto(ReproductorEfecto.MostrarToast("Reproductor no disponible"))
            return
        }

        viewModelScope.launch {
            try {
                colaCanciones.clear()

                val (cancionesValidas, cancionesInvalidas) = cola.partition { cancion ->
                    !cancion.cancion.archivoPath.isNullOrBlank()
                }

                if (cancionesInvalidas.isNotEmpty()) {
                    Log.w(TAG, "⚠️ ${cancionesInvalidas.size} canciones sin archivo válido")
                }

                if (cancionesValidas.isEmpty()) {
                    enviarEfecto(ReproductorEfecto.Error("No hay canciones reproducibles"))
                    return@launch
                }

                val mediaItems = cancionesValidas.mapNotNull { cancion ->
                    try {
                        mediaItemHelper.crearMediaItem(cancion).also {
                            colaCanciones[it.mediaId] = cancion
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creando MediaItem para '${cancion.cancion.titulo}'", e)
                        null
                    }
                }

                if (mediaItems.isEmpty()) {
                    enviarEfecto(ReproductorEfecto.Error("Error al procesar las canciones"))
                    return@launch
                }

                val indiceInicial = mediaItems.indexOfFirst {
                    it.mediaId == cancionInicial.cancion.idCancion.toString()
                }.let { if (it >= 0) it else 0 }

                withContext(Dispatchers.Main) {
                    p.setMediaItems(mediaItems, indiceInicial, 0L)
                    p.prepare()
                    p.playWhenReady = true
                }

                if (cancionesInvalidas.isNotEmpty()) {
                    enviarEfecto(ReproductorEfecto.MostrarToast("${cancionesInvalidas.size} canciones omitidas"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error estableciendo cola", e)
                enviarEfecto(ReproductorEfecto.Error("Error al cargar canciones"))
            }
        }
    }

    private fun togglePlayPause() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    private fun detenerReproduccion() {
        player?.let { p ->
            p.stop()
            p.clearMediaItems()
            colaCanciones.clear()
            _estadoUi.update { ReproductorEstado() }
        }
    }

    // ==================== ACTUALIZACIÓN DE CANCIÓN ====================

    private fun actualizarCancionActualDesdePlayer(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            limpiarEstadoCancion()
            return
        }

        datosAdicionalesJob?.cancel()

        viewModelScope.launch {
            try {
                var cancion = colaCanciones[mediaItem.mediaId]

                if (cancion == null) {
                    cancion = withContext(Dispatchers.IO) {
                        mediaItemHelper.mediaItemACancionConArtista(mediaItem, USUARIO_DEFAULT)
                    }
                    cancion?.let { colaCanciones[mediaItem.mediaId] = it }
                }

                if (cancion != null) {
                    _estadoUi.update {
                        it.copy(
                            cancionActual = cancion,
                            letra = null,
                            infoArtista = null,
                            enlaceGenius = null,
                            enlaceYoutube = null,
                            enlaceGoogle = null,
                            cargandoLetra = false,
                            cargandoInfo = false
                        )
                    }

                    // Construir enlaces inmediatamente
                    construirEnlaces(cancion)

                    // Si estamos en modo expandido, cargar datos adicionales
                    if (_estadoUi.value.modoPanel == ModoPanelReproductor.EXPANDIDO) {
                        cargarDatosExpandidos(cancion)
                    }
                } else {
                    limpiarEstadoCancion()
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando canción", e)
                limpiarEstadoCancion()
                enviarEfecto(ReproductorEfecto.Error("Error al cargar canción"))
            }
        }
    }

    private fun limpiarEstadoCancion() {
        _estadoUi.update {
            it.copy(
                cancionActual = null,
                progresoActualMs = 0L,
                progresoTemporalMs = null,
                letra = null,
                infoArtista = null,
                enlaceGenius = null,
                enlaceYoutube = null,
                enlaceGoogle = null
            )
        }
    }

    // ==================== CARGA DE DATOS EXPANDIDOS ====================

    private fun cargarDatosExpandidosSiNecesario() {
        _estadoUi.value.cancionActual?.let { cancion ->
            if (_estadoUi.value.letra == null && !_estadoUi.value.cargandoLetra) {
                cargarDatosExpandidos(cancion)
            }
        }
    }

    private fun cargarDatosExpandidos(cancion: CancionConArtista) {
        datosAdicionalesJob?.cancel()

        datosAdicionalesJob = viewModelScope.launch {
            val idCancion = cancion.cancion.idCancion

            _estadoUi.update { it.copy(cargandoLetra = true, cargandoInfo = true) }

            coroutineScope {
                // Sincronizar con Genius
                launch(Dispatchers.IO) {
                    runCatching { geniusRepository.sincronizarCancionAlReproducir(cancion.cancion) }
                }

                // Cargar letra
                launch(Dispatchers.IO) { cargarLetra(idCancion) }

                // Cargar info artista
                launch(Dispatchers.IO) { cargarInfoArtista(cancion) }
            }
        }
    }

    private suspend fun cargarLetra(idCancion: Int) {
        try {
            withTimeout(TIMEOUT_DATOS_MS) {
                letraDao.obtenerLetraPorIdCancion(idCancion).firstOrNull()?.let { letraEntity ->
                    if (esCancionActual(idCancion)) {
                        _estadoUi.update {
                            it.copy(
                                letra = letraEntity.textoLetra.trim().ifBlank { "Letra no disponible" },
                                cargandoLetra = false
                            )
                        }
                    }
                } ?: run {
                    _estadoUi.update { it.copy(letra = "Letra no disponible", cargandoLetra = false) }
                }
            }
        } catch (e: TimeoutCancellationException) {
            _estadoUi.update { it.copy(letra = "Letra no disponible", cargandoLetra = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando letra", e)
            _estadoUi.update { it.copy(letra = "Error cargando letra", cargandoLetra = false) }
        }
    }

    private suspend fun cargarInfoArtista(cancion: CancionConArtista) {
        val artistaId = cancion.cancion.idArtista?.toLong()
        if (artistaId == null) {
            _estadoUi.update { it.copy(infoArtista = "Información no disponible", cargandoInfo = false) }
            return
        }

        try {
            withTimeout(TIMEOUT_DATOS_MS) {
                cancionDao.obtenerArtistaPorIdFlow(artistaId.toInt()).firstOrNull()?.let { artistaEntity ->
                    if (esCancionActual(cancion.cancion.idCancion)) {
                        val info = buildString {
                            appendLine("🎤 ${artistaEntity.nombre}")
                            artistaEntity.paisOrigen?.let { appendLine("📍 $it") }
                            artistaEntity.descripcion?.trim()?.takeIf { it.isNotBlank() }?.let {
                                appendLine()
                                append(it)
                            }
                        }.trim().ifBlank { "Información no disponible" }

                        _estadoUi.update { it.copy(infoArtista = info, cargandoInfo = false) }
                    }
                } ?: run {
                    _estadoUi.update { it.copy(infoArtista = "Información no disponible", cargandoInfo = false) }
                }
            }
        } catch (e: TimeoutCancellationException) {
            _estadoUi.update { it.copy(infoArtista = "Información no disponible", cargandoInfo = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando info artista", e)
            _estadoUi.update { it.copy(infoArtista = "Error cargando información", cargandoInfo = false) }
        }
    }

    private fun construirEnlaces(cancion: CancionConArtista) {
        val titulo = cancion.cancion.titulo
        val artista = cancion.artistaNombre ?: "Unknown"

        try {
            val query = URLEncoder.encode("$titulo $artista", "UTF-8")
            val artistaQuery = URLEncoder.encode("$artista artist", "UTF-8")

            _estadoUi.update {
                it.copy(
                    enlaceGenius = "https://genius.com/search?q=$query",
                    enlaceYoutube = "https://www.youtube.com/results?search_query=$query",
                    enlaceGoogle = "https://www.google.com/search?q=$artistaQuery"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error construyendo enlaces", e)
        }
    }

    private fun esCancionActual(idCancion: Int): Boolean {
        return _estadoUi.value.cancionActual?.cancion?.idCancion == idCancion
    }

    // ==================== PROGRESO ====================

    private fun iniciarActualizadorDeProgreso() {
        detenerActualizadorDeProgreso()
        ultimoSegundoEmitido = -1L

        actualizadorDeProgresoJob = viewModelScope.launch {
            while (isActive) {
                actualizarProgreso()
                delay(PROGRESO_UPDATE_MS)
            }
        }
    }

    private fun actualizarProgreso() {
        if (_estadoUi.value.isScrubbing) return

        player?.let { p ->
            val posicionActual = p.currentPosition
            val segundoActual = posicionActual / PROGRESO_EMIT_THRESHOLD_MS

            if (segundoActual != ultimoSegundoEmitido) {
                _estadoUi.update { it.copy(progresoActualMs = posicionActual) }
                ultimoSegundoEmitido = segundoActual
            }
        }
    }

    private fun detenerActualizadorDeProgreso() {
        actualizadorDeProgresoJob?.cancel()
        actualizadorDeProgresoJob = null
    }

    // ==================== MODO REPRODUCCIÓN/REPETICIÓN ====================

    private fun toggleModoReproduccion() {
        player?.let { p ->
            val nuevoModo = _estadoUi.value.modoReproduccion.toggle()
            p.shuffleModeEnabled = nuevoModo == ModoReproduccion.ALEATORIO
            _estadoUi.update { it.copy(modoReproduccion = nuevoModo) }

            val mensaje = if (nuevoModo == ModoReproduccion.ALEATORIO)
                "Modo aleatorio activado" else "Modo aleatorio desactivado"
            enviarEfecto(ReproductorEfecto.MostrarToast(mensaje))
        }
    }

    private fun toggleModoRepeticion() {
        player?.let { p ->
            val nuevoModo = _estadoUi.value.modoRepeticion.siguiente()

            p.repeatMode = when (nuevoModo) {
                ModoRepeticion.NO_REPETIR -> Player.REPEAT_MODE_OFF
                ModoRepeticion.REPETIR_LISTA -> Player.REPEAT_MODE_ALL
                ModoRepeticion.REPETIR_CANCION -> Player.REPEAT_MODE_ONE
            }

            _estadoUi.update { it.copy(modoRepeticion = nuevoModo) }

            val mensaje = when (nuevoModo) {
                ModoRepeticion.NO_REPETIR -> "Repetición desactivada"
                ModoRepeticion.REPETIR_LISTA -> "Repetir lista"
                ModoRepeticion.REPETIR_CANCION -> "Repetir canción"
            }
            enviarEfecto(ReproductorEfecto.MostrarToast(mensaje))
        }
    }

    // ==================== FAVORITOS ====================

    private fun observarEstadoFavoritos() {
        viewModelScope.launch {
            _estadoUi
                .map { it.cancionActual?.cancion?.idCancion }
                .distinctUntilChanged()
                .collect { idCancion ->
                    idCancion?.let { actualizarEstadoFavorito(it.toLong()) }
                }
        }
    }

    private suspend fun actualizarEstadoFavorito(idCancion: Long) {
        runCatching {
            cancionDao.obtenerCancionConArtistaPorId(idCancion.toInt(), USUARIO_DEFAULT)?.let { c ->
                _estadoUi.update { it.copy(esFavorita = c.esFavorita) }
            }
        }
    }

    private fun toggleFavorito() {
        viewModelScope.launch {
            val cancion = _estadoUi.value.cancionActual?.cancion ?: return@launch
            val nuevoEstado = !_estadoUi.value.esFavorita

            try {
                if (nuevoEstado) {
                    cancionDao.agregarAFavoritos(FavoritoEntity(USUARIO_DEFAULT, cancion.idCancion))
                    enviarEfecto(ReproductorEfecto.MostrarToast("Agregado a favoritos ❤️"))
                } else {
                    cancionDao.quitarDeFavoritos(USUARIO_DEFAULT, cancion.idCancion)
                    enviarEfecto(ReproductorEfecto.MostrarToast("Quitado de favoritos"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando favorito", e)
                enviarEfecto(ReproductorEfecto.Error("Error al actualizar favorito"))
            }
        }
    }

    // ==================== UTILIDADES PÚBLICAS ====================

    fun reproducirCancion(cancion: CancionConArtista) {
        establecerColaYReproducir(listOf(cancion), cancion)
    }

    fun estaConectado(): Boolean = mediaController != null

    fun tieneCancionActual(): Boolean = _estadoUi.value.tieneCancion

    // ==================== HELPER PARA EFECTOS ====================

    private fun enviarEfecto(efecto: ReproductorEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        datosAdicionalesJob?.cancel()
        actualizadorDeProgresoJob?.cancel()

        playerListener?.let { player?.removeListener(it) }
        playerListener = null

        runCatching { MediaController.releaseFuture(controllerFuture) }

        mediaController = null
        colaCanciones.clear()

        super.onCleared()
    }
}

// ==================== EFECTOS ====================

sealed interface ReproductorEfecto {
    data class MostrarToast(val mensaje: String) : ReproductorEfecto
    data class Error(val mensaje: String) : ReproductorEfecto
    data class AbrirUrl(val url: String) : ReproductorEfecto
}