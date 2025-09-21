// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/BibliotecaViewModel.kt
package com.example.freeplayerm.ui.features.biblioteca

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.DetalleListaReproduccionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.FavoritoEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.ImageRepository
import com.example.freeplayerm.data.repository.RepositorioDeMusicaLocal
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

// --- SIN CAMBIOS EN TUS ENUMS Y DATA CLASSES ---
enum class TipoDeCuerpoBiblioteca {
    CANCIONES,
    LISTAS,
    CANCIONES_DE_LISTA,
    ALBUMES,
    CANCIONES_DE_ALBUM,
    ARTISTAS,
    GENEROS,
    FAVORITOS,
    CANCIONES_DE_GENERO,
    CANCIONES_DE_ARTISTA
}
enum class CriterioDeOrdenamiento(val etiqueta: String) {
    NINGUNO("Por defecto"),
    POR_TITULO("Título"),
    POR_ARTISTA("Artista"),
    POR_ALBUM("Álbum"),
    POR_GENERO("Género"),
    MAS_RECIENTE("Más reciente")
}
enum class DireccionDeOrdenamiento {
    ASCENDENTE,
    DESCENDENTE
}

data class BibliotecaEstado(
    val usuarioActual: UsuarioEntity? = null,
    val cuerpoActual: TipoDeCuerpoBiblioteca = TipoDeCuerpoBiblioteca.CANCIONES,
    val canciones: List<CancionConArtista> = emptyList(),
    val albumes: List<AlbumEntity> = emptyList(),
    val artistas: List<ArtistaEntity> = emptyList(),
    val generos: List<GeneroEntity> = emptyList(),
    val listas: List<ListaReproduccionEntity> = emptyList(),
    val tituloDelCuerpo: String = "Canciones",
    val textoDeBusqueda: String = "",
    val criterioDeOrdenamiento: CriterioDeOrdenamiento = CriterioDeOrdenamiento.NINGUNO,
    val direccionDeOrdenamiento: DireccionDeOrdenamiento = DireccionDeOrdenamiento.ASCENDENTE,
    val estaEscaneando: Boolean = false,
    val escaneoManualEnProgreso: Boolean = false,
    val errorDeEscaneo: String? = null,
    val mostrarDialogoPlaylist: Boolean = false,
    val cancionParaAnadirALista: CancionConArtista? = null,
    val esModoSeleccion: Boolean = false,
    val cancionesSeleccionadas: Set<Int> = emptySet(), // Usamos un Set de IDs para eficiencia
    val listaActual: ListaReproduccionEntity? = null, // Para saber en qué lista estamos
    val mostrandoDialogoEditarLista: Boolean = false

)
sealed class BibliotecaEvento {
    data object ForzarReescaneo : BibliotecaEvento()
    data object PermisoConcedido : BibliotecaEvento()
    data class CambiarCuerpo(val nuevoCuerpo: TipoDeCuerpoBiblioteca) : BibliotecaEvento()
    data class TextoDeBusquedaCambiado(val texto: String) : BibliotecaEvento()
    data class CriterioDeOrdenamientoCambiado(val criterio: CriterioDeOrdenamiento) : BibliotecaEvento()
    data object DireccionDeOrdenamientoCambiada : BibliotecaEvento()
    data class AlbumSeleccionado(val album: AlbumEntity) : BibliotecaEvento()
    data class ArtistaSeleccionado(val artista: ArtistaEntity) : BibliotecaEvento()
    data class GeneroSeleccionado(val genero: GeneroEntity) : BibliotecaEvento()
    data class ListaSeleccionada(val lista: ListaReproduccionEntity) : BibliotecaEvento()
    data class AlternarFavorito(val cancionConArtista: CancionConArtista) : BibliotecaEvento()
    data class AbrirDialogoPlaylist(val cancion: CancionConArtista) : BibliotecaEvento()
    data object CerrarDialogoPlaylist : BibliotecaEvento()
    data class CrearNuevaListaYAnadirCancion(val nombre: String, val descripcion: String?, val portadaUri: String?) : BibliotecaEvento()
    data class AnadirCancionAListasExistentes(val idListas: List<Int>) : BibliotecaEvento()
    data class ActivarModoSeleccion(val cancion: CancionConArtista) : BibliotecaEvento()
    data object DesactivarModoSeleccion : BibliotecaEvento()
    data class AlternarSeleccionCancion(val cancionId: Int) : BibliotecaEvento()
    data object SeleccionarTodo : BibliotecaEvento()
    data object QuitarCancionesSeleccionadasDeLista : BibliotecaEvento()
    data object EliminarListaDeReproduccionActual : BibliotecaEvento()
    data object AbrirDialogoAnadirSeleccionALista : BibliotecaEvento()
    data class AnadirCancionesSeleccionadasAListas(val idListas: List<Int>) : BibliotecaEvento()
    data class CrearListaYAnadirCancionesSeleccionadas(val nombre: String, val descripcion: String?, val portadaUri: String?) : BibliotecaEvento()
    data class EditarCancion(val cancion: CancionConArtista) : BibliotecaEvento()
    data object VolverAListas : BibliotecaEvento()
    data object AbrirDialogoEditarLista : BibliotecaEvento()
    data object CerrarDialogoEditarLista : BibliotecaEvento()
    data class GuardarCambiosLista(val nombre: String, val descripcion: String?, val portadaUri: String?) : BibliotecaEvento()
}


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cancionDao: CancionDao,
    private val repositorioDeMusicaLocal: RepositorioDeMusicaLocal,
    private val imageRepository: ImageRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val listScrollStates = mutableMapOf<TipoDeCuerpoBiblioteca, LazyListState>()
    val gridScrollStates = mutableMapOf<TipoDeCuerpoBiblioteca, LazyGridState>()
    private val _estadoUi = MutableStateFlow(BibliotecaEstado())
    val estadoUi = _estadoUi.asStateFlow()
    private val fuenteDeCancionesActiva =
        MutableStateFlow<Flow<List<CancionConArtista>>>(flowOf(emptyList()))
    private val usuarioIdFlow = _estadoUi.map { it.usuarioActual?.id }.distinctUntilChanged().filterNotNull()
    // Mantenemos este Job para las cargas que no son de canciones (álbumes, artistas, etc.)

    init {
        observarYFiltrarCancionesActuales()
        observarYFiltrarAlbumes()
        observarYFiltrarArtistas()
        observarYFiltrarGeneros()
        observarYFiltrarListas()
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                _estadoUi.update {
                    it.copy(
                        criterioDeOrdenamiento = preferences.sortCriterion,
                        direccionDeOrdenamiento = preferences.sortDirection
                    )
                }
            }
        }

    }

    private fun observarYFiltrarArtistas() {
        viewModelScope.launch {
            val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
            cancionDao.obtenerTodosLosArtistas()
                .combine(textoDeBusqueda) { artistas, busqueda ->
                    if (busqueda.isBlank()) artistas
                    else {
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        artistas.filter { normalizarTexto(it.nombre).contains(busquedaNormalizada) }
                    }
                }
                .collectLatest { _estadoUi.update { s -> s.copy(artistas = it) } }
        }
    }

    private fun observarYFiltrarGeneros() {
        viewModelScope.launch {
            val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
            cancionDao.obtenerTodosLosGeneros()
                .combine(textoDeBusqueda) { generos, busqueda ->
                    if (busqueda.isBlank()) generos
                    else {
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        generos.filter { normalizarTexto(it.nombre).contains(busquedaNormalizada) }
                    }
                }
                .collectLatest { _estadoUi.update { s -> s.copy(generos = it) } }
        }
    }

    private fun observarYFiltrarListas() {
        viewModelScope.launch {
            _estadoUi.map { it.usuarioActual?.id }.distinctUntilChanged().flatMapLatest { usuarioId ->
                if (usuarioId != null) cancionDao.obtenerListasPorUsuarioId(usuarioId) else flowOf(emptyList())
            }.combine(_estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()) { listas, busqueda ->
                if (busqueda.isBlank()) listas
                else {
                    val busquedaNormalizada = normalizarTexto(busqueda)
                    listas.filter { normalizarTexto(it.nombre).contains(busquedaNormalizada) }
                }
            }.collectLatest { _estadoUi.update { s -> s.copy(listas = it) } }
        }
    }

    @SuppressLint("UseKtx")
    @RequiresApi(Build.VERSION_CODES.R)
    fun enEvento(evento: BibliotecaEvento) {
        when (evento) {
            is BibliotecaEvento.CriterioDeOrdenamientoCambiado -> {
                // Ya no actualizamos el estado aquí, porque el Flow lo hará automáticamente.
                // Solo guardamos la nueva preferencia.
                viewModelScope.launch {
                    userPreferencesRepository.updateSortCriterion(evento.criterio)
                }
            }
            is BibliotecaEvento.DireccionDeOrdenamientoCambiada -> {
                // Invertimos la dirección actual para guardarla
                val nuevaDireccion = if (_estadoUi.value.direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    DireccionDeOrdenamiento.DESCENDENTE
                } else {
                    DireccionDeOrdenamiento.ASCENDENTE
                }
                viewModelScope.launch {
                    userPreferencesRepository.updateSortDirection(nuevaDireccion)
                }
            }
            is BibliotecaEvento.AbrirDialogoEditarLista -> {
                _estadoUi.update { it.copy(mostrandoDialogoEditarLista = true) }
            }
            is BibliotecaEvento.CerrarDialogoEditarLista -> {
                _estadoUi.update { it.copy(mostrandoDialogoEditarLista = false) }
            }
            is BibliotecaEvento.GuardarCambiosLista -> {
                viewModelScope.launch {
                    val listaAEditar = _estadoUi.value.listaActual ?: return@launch
                    var nuevaPortadaUrl = listaAEditar.portadaUrl

                    if (evento.portadaUri != null && evento.portadaUri.startsWith("content://")) {
                        nuevaPortadaUrl = imageRepository.copyImageToInternalStorage(Uri.parse(evento.portadaUri))?.toString()
                    } else if (evento.portadaUri == null) {
                        // Si el usuario eliminó la portada
                        nuevaPortadaUrl = null
                    }

                    val listaActualizada = listaAEditar.copy(
                        nombre = evento.nombre,
                        descripcion = evento.descripcion,
                        portadaUrl = nuevaPortadaUrl // <-- Usamos la nueva URI
                    )

                    cancionDao.actualizarLista(listaActualizada)

                    // Actualizamos el estado y cerramos el diálogo
                    _estadoUi.update {
                        it.copy(
                            mostrandoDialogoEditarLista = false,
                            listaActual = listaActualizada // Actualizamos la lista en la UI al instante
                        )
                    }
                }
            }
            is BibliotecaEvento.VolverAListas -> {
                // Simplemente cambiamos el cuerpo para volver a la vista de todas las listas.
                cambiarCuerpo(TipoDeCuerpoBiblioteca.LISTAS)
            }
            is BibliotecaEvento.EditarCancion -> {
                // TODO: Aquí iría la lógica para abrir la pantalla de edición
                // para la 'evento.cancion' específica que se pasó.
                // Ejemplo: _estadoUi.update { it.copy(cancionParaEditar = evento.cancion) }
                // Al ser una acción directa, salimos del modo selección.
                _estadoUi.update { it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet()) }
            }
            is BibliotecaEvento.AbrirDialogoAnadirSeleccionALista -> {
                _estadoUi.update { it.copy(mostrarDialogoPlaylist = true) }
            }
            is BibliotecaEvento.AnadirCancionesSeleccionadasAListas -> {
                viewModelScope.launch {
                    val estado = _estadoUi.value
                    val cancionIds = estado.cancionesSeleccionadas

                    // Iteramos sobre cada lista elegida
                    evento.idListas.forEach { listaId ->
                        // Iteramos sobre cada canción seleccionada
                        cancionIds.forEach { cancionId ->
                            val detalle = DetalleListaReproduccionEntity(idLista = listaId, idCancion = cancionId)
                            cancionDao.insertarDetalleLista(detalle)
                        }
                    }

                    // Limpiamos y salimos del modo selección
                    _estadoUi.update {
                        it.copy(
                            esModoSeleccion = false,
                            cancionesSeleccionadas = emptySet(),
                            mostrarDialogoPlaylist = false
                        )
                    }
                }
            }
            is BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas -> {
                viewModelScope.launch {
                    val estado = _estadoUi.value
                    val usuarioId = estado.usuarioActual?.id ?: return@launch
                    val cancionIds = estado.cancionesSeleccionadas
                    val portadaUriString = evento.portadaUri?.let {
                        imageRepository.copyImageToInternalStorage(Uri.parse(it))?.toString()
                    }

                    // 1. Creamos la nueva lista
                    val nuevaLista = ListaReproduccionEntity(
                        idUsuario = usuarioId,
                        nombre = evento.nombre,
                        descripcion = evento.descripcion,
                        portadaUrl = portadaUriString
                    )
                    val nuevaListaId = cancionDao.insertarListaReproduccion(nuevaLista)

                    // 2. Añadimos las canciones a la nueva lista
                    if (nuevaListaId != -1L) {
                        cancionIds.forEach { cancionId ->
                            val detalle = DetalleListaReproduccionEntity(
                                idLista = nuevaListaId.toInt(),
                                idCancion = cancionId
                            )
                            cancionDao.insertarDetalleLista(detalle)
                        }
                    }

                    // 3. Limpiamos y salimos del modo selección
                    _estadoUi.update {
                        it.copy(
                            esModoSeleccion = false,
                            cancionesSeleccionadas = emptySet(),
                            mostrarDialogoPlaylist = false
                        )
                    }
                }
            }

            is BibliotecaEvento.ActivarModoSeleccion -> {
                _estadoUi.update {
                    it.copy(
                        esModoSeleccion = true,
                        // Al activar, seleccionamos la primera canción
                        cancionesSeleccionadas = setOf(evento.cancion.cancion.idCancion)
                    )
                }
            }
            is BibliotecaEvento.DesactivarModoSeleccion -> {
                _estadoUi.update {
                    it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet())
                }
            }
            is BibliotecaEvento.AlternarSeleccionCancion -> {
                _estadoUi.update {
                    val seleccionActual = it.cancionesSeleccionadas.toMutableSet()
                    if (evento.cancionId in seleccionActual) {
                        seleccionActual.remove(evento.cancionId)
                    } else {
                        seleccionActual.add(evento.cancionId)
                    }
                    // Si no queda ninguna canción seleccionada, salimos del modo selección
                    val nuevoModoSeleccion = seleccionActual.isNotEmpty()
                    it.copy(cancionesSeleccionadas = seleccionActual, esModoSeleccion = nuevoModoSeleccion)
                }
            }
            is BibliotecaEvento.SeleccionarTodo -> {
                _estadoUi.update {
                    val todosLosIds = it.canciones.map { cancion -> cancion.cancion.idCancion }.toSet()
                    // Si ya están todos seleccionados, los deseleccionamos. Si no, los seleccionamos todos.
                    val nuevaSeleccion = if (it.cancionesSeleccionadas == todosLosIds) {
                        emptySet()
                    } else {
                        todosLosIds
                    }
                    it.copy(cancionesSeleccionadas = nuevaSeleccion)
                }
            }
            is BibliotecaEvento.QuitarCancionesSeleccionadasDeLista -> {
                viewModelScope.launch {
                    val estado = _estadoUi.value
                    val listaId = estado.listaActual?.idLista ?: return@launch
                    val cancionIds = estado.cancionesSeleccionadas.toList()

                    cancionDao.quitarCancionesDeLista(listaId, cancionIds)

                    // Salimos del modo selección
                    _estadoUi.update { it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet()) }
                }
            }
            is BibliotecaEvento.EliminarListaDeReproduccionActual -> {
                viewModelScope.launch {
                    val listaId = _estadoUi.value.listaActual?.idLista ?: return@launch
                    cancionDao.eliminarListaPorId(listaId)
                    // Volvemos a la pantalla de listas
                    cambiarCuerpo(TipoDeCuerpoBiblioteca.LISTAS)
                }
            }

            is BibliotecaEvento.AbrirDialogoPlaylist -> {
                _estadoUi.update {
                    it.copy(
                        mostrarDialogoPlaylist = true,
                        cancionParaAnadirALista = evento.cancion
                    )
                }
            }
            is BibliotecaEvento.CerrarDialogoPlaylist -> {
                _estadoUi.update {
                    it.copy(
                        mostrarDialogoPlaylist = false,
                        cancionParaAnadirALista = null
                    )
                }
            }
            is BibliotecaEvento.AnadirCancionAListasExistentes -> {
                viewModelScope.launch {
                    val cancionId = _estadoUi.value.cancionParaAnadirALista?.cancion?.idCancion ?: return@launch
                    evento.idListas.forEach { listaId ->
                        val detalle = DetalleListaReproduccionEntity(idLista = listaId, idCancion = cancionId)
                        cancionDao.insertarDetalleLista(detalle)
                    }
                    // Cerramos el diálogo después de añadir
                    _estadoUi.update { it.copy(mostrarDialogoPlaylist = false, cancionParaAnadirALista = null) }
                }
            }
            is BibliotecaEvento.CrearNuevaListaYAnadirCancion -> {
                viewModelScope.launch {
                    val usuarioId = _estadoUi.value.usuarioActual?.id ?: return@launch
                    val cancionId = _estadoUi.value.cancionParaAnadirALista?.cancion?.idCancion ?: return@launch
                    val portadaUriString = evento.portadaUri?.let {
                        imageRepository.copyImageToInternalStorage(it.toUri())?.toString()
                    }
                    // 1. Crear la nueva lista
                    val nuevaLista = ListaReproduccionEntity(
                        idUsuario = usuarioId,
                        nombre = evento.nombre,
                        descripcion = evento.descripcion,
                        portadaUrl = portadaUriString
                    )
                    val nuevaListaId = cancionDao.insertarListaReproduccion(nuevaLista)

                    // 2. Añadir la canción a la nueva lista
                    if (nuevaListaId != -1L) { // -1L indica que la inserción falló
                        val detalle = DetalleListaReproduccionEntity(idLista = nuevaListaId.toInt(), idCancion = cancionId)
                        cancionDao.insertarDetalleLista(detalle)
                    }

                    // 3. Cerramos el diálogo
                    _estadoUi.update { it.copy(mostrarDialogoPlaylist = false, cancionParaAnadirALista = null) }
                }
            }

            is BibliotecaEvento.AlternarFavorito -> {
                viewModelScope.launch {
                    val usuarioId = _estadoUi.value.usuarioActual?.id ?: return@launch
                    val cancionId = evento.cancionConArtista.cancion.idCancion

                    if (evento.cancionConArtista.esFavorita) {
                        // Si ya es favorita, la quitamos
                        cancionDao.quitarDeFavoritos(usuarioId, cancionId)
                    } else {
                        // Si no es favorita, la añadimos
                        val nuevoFavorito = FavoritoEntity(idUsuario = usuarioId, idCancion = cancionId)
                        cancionDao.anadirAFavoritos(nuevoFavorito)
                    }
                }
            }
            is BibliotecaEvento.ForzarReescaneo -> {
                // El escaneo forzado es iniciado por el usuario.
                escanearAlmacenamientoLocal(esIniciadoPorUsuario = true)
            }

            is BibliotecaEvento.CambiarCuerpo -> cambiarCuerpo(evento.nuevoCuerpo)
            is BibliotecaEvento.TextoDeBusquedaCambiado -> {
                // Actualizamos el texto de búsqueda directamente en el estado principal.
                _estadoUi.update { it.copy(textoDeBusqueda = evento.texto) }
            }
            is BibliotecaEvento.CriterioDeOrdenamientoCambiado -> {
                _estadoUi.update { it.copy(criterioDeOrdenamiento = evento.criterio) }
            }
            is BibliotecaEvento.DireccionDeOrdenamientoCambiada -> {
                // Esta lógica invierte la dirección actual.
                val nuevaDireccion = if (_estadoUi.value.direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    DireccionDeOrdenamiento.DESCENDENTE
                } else {
                    DireccionDeOrdenamiento.ASCENDENTE
                }
                _estadoUi.update { it.copy(direccionDeOrdenamiento = nuevaDireccion) }
            }
            is BibliotecaEvento.AlbumSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM, album = evento.album)
            is BibliotecaEvento.ArtistaSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA, artista = evento.artista)
            is BibliotecaEvento.GeneroSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO, genero = evento.genero)
            is BibliotecaEvento.ListaSeleccionada -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA, lista = evento.lista)
            // El escaneo por permiso concedido es automático y debe ser silencioso.
            is BibliotecaEvento.PermisoConcedido -> { escanearAlmacenamientoLocal(esIniciadoPorUsuario = false)


            }

        }
    }
    private fun observarYFiltrarAlbumes() {
        viewModelScope.launch {
            val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
            cancionDao.obtenerTodosLosAlbumes()
                .combine(textoDeBusqueda) { albumes, busqueda ->
                    if (busqueda.isBlank()) albumes
                    else {
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        albumes.filter { normalizarTexto(it.titulo).contains(busquedaNormalizada) }
                    }
                }
                .collectLatest { _estadoUi.update { s -> s.copy(albumes = it) } }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun escanearAlmacenamientoLocal(esIniciadoPorUsuario: Boolean) {
        // Evita iniciar un nuevo escaneo manual si ya hay uno en progreso.
        if (esIniciadoPorUsuario && _estadoUi.value.escaneoManualEnProgreso) return

        viewModelScope.launch {
            // Solo mostramos el indicador de carga si el escaneo fue manual.
            if (esIniciadoPorUsuario) {
                _estadoUi.update { it.copy(escaneoManualEnProgreso = true, errorDeEscaneo = null) }
            }

            try {
                // La lógica de escaneo es la misma para ambos casos.
                repositorioDeMusicaLocal.escanearYGuardarMusica()
            } catch (e: Exception) {
                // Mostramos el error en la UI independientemente de cómo se inició.
                _estadoUi.update { it.copy(errorDeEscaneo = "Error al escanear: ${e.message}") }
            } finally {
                // Al finalizar, solo ocultamos el indicador si fue un escaneo manual.
                if (esIniciadoPorUsuario) {
                    _estadoUi.update { it.copy(escaneoManualEnProgreso = false) }
                }
            }
        }
    }

    // --- CAMBIO #4: EL OBSERVADOR AHORA ES DINÁMICO ---
    // Esta función ahora es mucho más potente.
    private fun observarYFiltrarCancionesActuales() {
        viewModelScope.launch {
            val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
            // Ahora observamos los dos nuevos estados de ordenamiento.
            val criterioDeOrdenamiento = _estadoUi.map { it.criterioDeOrdenamiento }.distinctUntilChanged()
            val direccionDeOrdenamiento = _estadoUi.map { it.direccionDeOrdenamiento }.distinctUntilChanged()

            fuenteDeCancionesActiva.flatMapLatest { flowDeCanciones ->
                // El 'combine' ahora incluye los nuevos flows.
                combine(flowDeCanciones, textoDeBusqueda, criterioDeOrdenamiento, direccionDeOrdenamiento) { canciones, busqueda, criterio, direccion ->
                    // 1. La Búsqueda (filtro) se mantiene igual.
                    val cancionesBuscadas = if (busqueda.isBlank()) {
                        canciones
                    } else {
                        // 1. Normalizamos y dividimos la búsqueda en palabras (tokens)
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        val tokensDeBusqueda = busquedaNormalizada.split(" ").filter { it.isNotBlank() }

                        canciones.filter { cancionConArtista ->
                            // 2. Normalizamos el texto de la canción y el artista
                            val textoDeCancionNormalizado = normalizarTexto(
                                "${cancionConArtista.cancion.titulo} ${cancionConArtista.artistaNombre}"
                            )
                            // 3. Verificamos que TODOS los tokens de búsqueda estén presentes
                            tokensDeBusqueda.all { token ->
                                textoDeCancionNormalizado.contains(token)
                            }
                        }
                    }

                    // --- CAMBIO #2: CORRECCIÓN DE NULABILIDAD EN ORDENAMIENTO ---
                    val cancionesOrdenadas = when (criterio) {
                        CriterioDeOrdenamiento.NINGUNO -> cancionesBuscadas
                        CriterioDeOrdenamiento.POR_TITULO -> cancionesBuscadas.sortedBy { it.cancion.titulo }
                        CriterioDeOrdenamiento.POR_ARTISTA -> cancionesBuscadas.sortedBy { it.artistaNombre ?: "" }
                        CriterioDeOrdenamiento.POR_ALBUM -> cancionesBuscadas.sortedBy { it.albumNombre ?: "" }
                        CriterioDeOrdenamiento.POR_GENERO -> cancionesBuscadas.sortedBy { it.generoNombre ?: "" }
                        // Aseguramos que idCancion no sea nulo para ordenar
                        CriterioDeOrdenamiento.MAS_RECIENTE -> cancionesBuscadas.sortedByDescending { it.cancion.idCancion }
                    }

                    if (direccion == DireccionDeOrdenamiento.DESCENDENTE) {
                        cancionesOrdenadas.reversed()
                    } else {
                        cancionesOrdenadas
                    }
                }
            }.collectLatest { cancionesFinales ->
                _estadoUi.update { it.copy(canciones = cancionesFinales) }
            }
        }
    }

    private fun normalizarTexto(texto: String): String {
        val textoSinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return textoSinAcentos.lowercase()
    }

    // --- CAMBIO #5: 'cambiarCuerpo' REFACTORIZADO PARA SER MÁS ROBUSTO ---
    private fun cambiarCuerpo(
        nuevoCuerpo: TipoDeCuerpoBiblioteca,
        album: AlbumEntity? = null,
        artista: ArtistaEntity? = null,
        genero: GeneroEntity? = null,
        lista: ListaReproduccionEntity? = null
    ) {
        var nuevaFuente: Flow<List<CancionConArtista>>? = null
        val esVistaDeLista = nuevoCuerpo == TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA
        // 1. Actualizamos el estado de la UI para la nueva vista y limpiamos la búsqueda.
        _estadoUi.update {
            it.copy(
                cuerpoActual = nuevoCuerpo,
                textoDeBusqueda = "",
                esModoSeleccion = false,
                cancionesSeleccionadas = emptySet(),
                listaActual = if (esVistaDeLista) lista else null // <-- Reinicio clave
            )
        }



        // 2. Simplemente actualizamos el título y, si es una vista de canciones,
        //    le decimos al observador de canciones cuál es su nueva fuente de datos.
        when (nuevoCuerpo) {

            // CASOS QUE NO SON LISTAS DE CANCIONES (SOLO CAMBIAN EL TÍTULO)
            TipoDeCuerpoBiblioteca.ALBUMES -> _estadoUi.update { it.copy(tituloDelCuerpo = "Álbumes") }
            TipoDeCuerpoBiblioteca.ARTISTAS -> _estadoUi.update { it.copy(tituloDelCuerpo = "Artistas") }
            TipoDeCuerpoBiblioteca.GENEROS -> _estadoUi.update { it.copy(tituloDelCuerpo = "Géneros") }
            TipoDeCuerpoBiblioteca.LISTAS -> _estadoUi.update { it.copy(tituloDelCuerpo = "Mis Listas") }

            // CASOS QUE SÍ SON LISTAS DE CANCIONES (CAMBIAN EL TÍTULO Y LA FUENTE)
            TipoDeCuerpoBiblioteca.CANCIONES -> {
                _estadoUi.update { it.copy(tituloDelCuerpo = "Canciones") }
                nuevaFuente = usuarioIdFlow.flatMapLatest { userId ->
                    cancionDao.obtenerCancionesConArtista(userId)
                }
            }
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM -> {
                album?.let {
                    _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.titulo) }
                    val usuarioId = _estadoUi.value.usuarioActual?.id

                    // Verificamos que el ID no sea nulo antes de llamar a la función.
                    if (usuarioId != null) {
                        nuevaFuente =
                            cancionDao.obtenerCancionesDeAlbumConArtista(it.idAlbum, usuarioId)
                    }
                }
            }
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA -> {
                artista?.let {
                    _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
                    val usuarioId = _estadoUi.value.usuarioActual?.id

                    // Verificamos que el ID no sea nulo antes de llamar a la función.
                    if (usuarioId != null) {
                        nuevaFuente = cancionDao.obtenerCancionesDeArtistaConArtista(it.idArtista, usuarioId)
                    }
                }
            }
            TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO -> {
                genero?.let {
                    _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
                    val usuarioId = _estadoUi.value.usuarioActual?.id

                    // Verificamos que el ID no sea nulo antes de llamar a la función.
                    if (usuarioId != null) {
                        nuevaFuente = cancionDao.obtenerCancionesDeGeneroConArtista(it.idGenero, usuarioId)
                    }
                }
            }
            TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                lista?.let {
                    // Actualizamos el título y nos aseguramos de que la lista esté en el estado
                    _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre, listaActual = it) }

                    val usuarioId = _estadoUi.value.usuarioActual?.id
                    if (usuarioId != null) {
                        nuevaFuente =
                            cancionDao.obtenerCancionesDeListaConArtista(it.idLista, usuarioId)
                    }
                }
            }
            TipoDeCuerpoBiblioteca.FAVORITOS -> {
                _estadoUi.update { it.copy(tituloDelCuerpo = "Favoritos") }
                val usuarioId = _estadoUi.value.usuarioActual?.id ?: -1
                if (usuarioId != -1) {
                    nuevaFuente = cancionDao.obtenerCancionesFavoritasConArtista(usuarioId)
                }
            }
        }

        // 3. Actualizamos el 'interruptor' de la fuente de canciones.
        // Si la nueva vista no es de canciones, le pasamos un flow vacío para limpiar la lista.
        fuenteDeCancionesActiva.value = nuevaFuente ?: flowOf(emptyList())
    }

    fun cargarDatosDeUsuario(usuarioId: Int) {
        if (_estadoUi.value.usuarioActual?.id == usuarioId) return
        viewModelScope.launch {
            if (usuarioId != -1) {
                val usuario = usuarioRepository.obtenerUsuarioPorId(usuarioId)
                _estadoUi.update { it.copy(usuarioActual = usuario) }
            }
        }
    }
}