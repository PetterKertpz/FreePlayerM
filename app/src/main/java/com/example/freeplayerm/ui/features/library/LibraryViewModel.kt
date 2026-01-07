// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/LibraryViewModel.kt
package com.example.freeplayerm.ui.features.library

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.FavoriteEntity
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.data.local.entity.PlaylistItemEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.data.repository.ImageRepository
import com.example.freeplayerm.data.repository.LocalMusicRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import com.example.freeplayerm.data.repository.UserRepository
import com.example.freeplayerm.data.scanner.MusicScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel
@Inject
constructor(
   private val userRepository: UserRepository,
   private val songDao: SongDao,
   private val localMusicRepository: LocalMusicRepository,
   private val imageRepository: ImageRepository,
   private val userPreferencesRepository: UserPreferencesRepository,
   private val musicScannerManager: MusicScannerManager,
) : ViewModel() {
   private val _estadoUi = MutableStateFlow(BibliotecaEstado())
   val estadoUi = _estadoUi.asStateFlow()
   private val fuenteDeCancionesActiva =
      MutableStateFlow<Flow<List<SongWithArtist>>>(flowOf(emptyList()))
   private val usuarioIdFlow =
      _estadoUi.map { it.usuarioActual?.idUsuario }.distinctUntilChanged().filterNotNull()

   // Mantenemos este Job para las cargas que no son de canciones (álbumes, artistas, etc.)

   init {
      observarYFiltrarCancionesActuales()
      observarYFiltrarAlbumes()
      observarYFiltrarArtistas()
      observarYFiltrarGeneros()
      observarYFiltrarListas()
      observarEstadoEscaneo()
      viewModelScope.launch {
         userPreferencesRepository.userPreferences.collect { preferences ->
            _estadoUi.update {
               it.copy(
                  criterioDeOrdenamiento = preferences.sortCriterion,
                  direccionDeOrdenamiento = preferences.sortDirection,
                  nivelZoom = preferences.zoomLevel,
               )
            }
         }
      }
   }

   private fun observarYFiltrarArtistas() {
      viewModelScope.launch {
         val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
         songDao
            .obtenerTodosLosArtistas()
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
         songDao
            .obtenerTodosLosGeneros()
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
         _estadoUi
            .map { it.usuarioActual?.idUsuario }
            .distinctUntilChanged()
            .flatMapLatest { usuarioId ->
               if (usuarioId != null) songDao.obtenerListasPorUsuario(usuarioId)
               else flowOf(emptyList())
            }
            .combine(_estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()) { listas, busqueda
               ->
               if (busqueda.isBlank()) listas
               else {
                  val busquedaNormalizada = normalizarTexto(busqueda)
                  listas.filter { normalizarTexto(it.nombre).contains(busquedaNormalizada) }
               }
            }
            .collectLatest { _estadoUi.update { s -> s.copy(listas = it) } }
      }
   }

   fun enEvento(evento: BibliotecaEvento) {
      when (evento) {
         is BibliotecaEvento.ActivarModoSeleccionListas -> {
            _estadoUi.update {
               it.copy(
                  esModoSeleccionListas = true,
                  listasSeleccionadas = setOf(evento.lista.idLista),
               )
            }
         }
         
         is BibliotecaEvento.DesactivarModoSeleccionListas -> {
            _estadoUi.update {
               it.copy(
                  esModoSeleccionListas = false,
                  listasSeleccionadas = emptySet(),
                  listaParaEditar = null,
               )
            }
         }
         
         is BibliotecaEvento.AlternarSeleccionLista -> {
            _estadoUi.update {
               val seleccionActual = it.listasSeleccionadas.toMutableSet()
               if (evento.listaId in seleccionActual) {
                  seleccionActual.remove(evento.listaId)
               } else {
                  seleccionActual.add(evento.listaId)
               }
               val nuevoModoSeleccion = seleccionActual.isNotEmpty()
               it.copy(
                  listasSeleccionadas = seleccionActual,
                  esModoSeleccionListas = nuevoModoSeleccion,
               )
            }
         }
         
         is BibliotecaEvento.SeleccionarTodasLasListas -> {
            _estadoUi.update {
               val todosLosIds = it.listas.map { lista -> lista.idLista }.toSet()
               val nuevaSeleccion = if (it.listasSeleccionadas == todosLosIds) {
                  emptySet()
               } else {
                  todosLosIds
               }
               it.copy(listasSeleccionadas = nuevaSeleccion)
            }
         }
         
         is BibliotecaEvento.EliminarListasSeleccionadas -> {
            viewModelScope.launch {
               val idsAEliminar = _estadoUi.value.listasSeleccionadas.toList()
               idsAEliminar.forEach { listaId ->
                  songDao.eliminarListaPorId(listaId)
               }
               _estadoUi.update {
                  it.copy(
                     esModoSeleccionListas = false,
                     listasSeleccionadas = emptySet(),
                  )
               }
            }
         }
         
         is BibliotecaEvento.AbrirDialogoEditarListaSeleccionada -> {
            _estadoUi.update {
               it.copy(listaParaEditar = evento.lista)
            }
         }
         
         is BibliotecaEvento.CerrarDialogoEditarListaSeleccionada -> {
            _estadoUi.update {
               it.copy(listaParaEditar = null)
            }
         }
         
         is BibliotecaEvento.GuardarCambiosListaSeleccionada -> {
            viewModelScope.launch {
               val listaAEditar = _estadoUi.value.listaParaEditar ?: return@launch
               var nuevaPortadaUrl = listaAEditar.portadaUrl
               
               if (evento.portadaUri != null && evento.portadaUri.startsWith("content://")) {
                  nuevaPortadaUrl = imageRepository
                     .copyImageFromUri(Uri.parse(evento.portadaUri))
                     ?.toString()
               } else if (evento.portadaUri == null) {
                  nuevaPortadaUrl = null
               }
               
               val listaActualizada = listaAEditar.copy(
                  nombre = evento.nombre,
                  descripcion = evento.descripcion,
                  portadaUrl = nuevaPortadaUrl,
               )
               
               songDao.actualizarListaReproduccion(listaActualizada)
               
               _estadoUi.update {
                  it.copy(
                     listaParaEditar = null,
                     esModoSeleccionListas = false,
                     listasSeleccionadas = emptySet(),
                  )
               }
            }
         }
         // ─── Zoom ───
         is BibliotecaEvento.CambiarNivelZoom -> {
            println("🎬 [VIEWMODEL] Evento CambiarNivelZoom recibido: ${evento.nivel}")
            _estadoUi.update { it.copy(nivelZoom = evento.nivel) }
            viewModelScope.launch { userPreferencesRepository.updateZoomLevel(evento.nivel) }
         }
         is BibliotecaEvento.AumentarZoom -> {
            val nuevoNivel = _estadoUi.value.nivelZoom.siguiente()
            _estadoUi.update { it.copy(nivelZoom = nuevoNivel) }
            viewModelScope.launch { userPreferencesRepository.updateZoomLevel(nuevoNivel) }
         }
         is BibliotecaEvento.ReducirZoom -> {
            val nuevoNivel = _estadoUi.value.nivelZoom.anterior()
            _estadoUi.update { it.copy(nivelZoom = nuevoNivel) }
            viewModelScope.launch { userPreferencesRepository.updateZoomLevel(nuevoNivel) }
         }
         is BibliotecaEvento.QuitarSeleccionDeFavoritos -> {
            viewModelScope.launch {
               val estado = _estadoUi.value
               val usuarioId = estado.usuarioActual?.idUsuario ?: return@launch
               val cancionIds = estado.cancionesSeleccionadas.toList()

               // Iteramos sobre cada canción seleccionada y la quitamos de favoritos
               cancionIds.forEach { cancionId -> songDao.quitarDeFavoritos(usuarioId, cancionId) }

               // Limpiamos y salimos del modo selección
               _estadoUi.update {
                  it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet())
               }
            }
         }
         is BibliotecaEvento.AnadirSeleccionAFavoritos -> {
            viewModelScope.launch {
               val estado = _estadoUi.value
               val usuarioId = estado.usuarioActual?.idUsuario ?: return@launch

               // ✅ Filtrar solo las que NO son favoritas
               val idsNoFavoritos =
                  estado.canciones
                     .filter { it.cancion.idCancion in estado.cancionesSeleccionadas }
                     .filterNot { it.esFavorita }
                     .map { it.cancion.idCancion }

               if (idsNoFavoritos.isNotEmpty()) {
                  // ✅ Crear lista de entidades para inserción batch
                  val nuevosFavoritos =
                     idsNoFavoritos.map { cancionId ->
                        FavoriteEntity(idUsuario = usuarioId, idCancion = cancionId)
                     }

                  // ✅ Inserción batch (más eficiente)
                  songDao.agregarMultiplesAFavoritos(nuevosFavoritos)
               }

               _estadoUi.update {
                  it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet())
               }
            }
         }
         is BibliotecaEvento.LimpiarBusqueda -> {
            _estadoUi.update { it.copy(textoDeBusqueda = "") }
         }
         is BibliotecaEvento.CriterioDeOrdenamientoCambiado -> {
            // Ya no actualizamos el estado aquí, porque el Flow lo hará automáticamente.
            // Solo guardamos la nueva preferencia.
            viewModelScope.launch { userPreferencesRepository.updateSortCriterion(evento.criterio) }
         }
         is BibliotecaEvento.DireccionDeOrdenamientoCambiada -> {
            // Invertimos la dirección actual para guardarla
            val nuevaDireccion =
               if (_estadoUi.value.direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                  DireccionDeOrdenamiento.DESCENDENTE
               } else {
                  DireccionDeOrdenamiento.ASCENDENTE
               }
            viewModelScope.launch { userPreferencesRepository.updateSortDirection(nuevaDireccion) }
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
                  nuevaPortadaUrl =
                     imageRepository.copyImageFromUri(Uri.parse(evento.portadaUri))?.toString()
               } else if (evento.portadaUri == null) {
                  // Si el usuario eliminó la portada
                  nuevaPortadaUrl = null
               }

               val listaActualizada =
                  listaAEditar.copy(
                     nombre = evento.nombre,
                     descripcion = evento.descripcion,
                     portadaUrl = nuevaPortadaUrl, // <-- Usamos la nueva URI
                  )

               songDao.actualizarListaReproduccion(listaActualizada)

               // Actualizamos el estado y cerramos el diálogo
               _estadoUi.update {
                  it.copy(
                     mostrandoDialogoEditarLista = false,
                     listaActual = listaActualizada, // Actualizamos la lista en la UI al instante
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
            _estadoUi.update {
               it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet())
            }
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
                     val detalle = PlaylistItemEntity(idLista = listaId, idCancion = cancionId)
                     songDao.insertarDetalleLista(detalle)
                  }
               }

               // Limpiamos y salimos del modo selección
               _estadoUi.update {
                  it.copy(
                     esModoSeleccion = false,
                     cancionesSeleccionadas = emptySet(),
                     mostrarDialogoPlaylist = false,
                  )
               }
            }
         }
         is BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas -> {
            viewModelScope.launch {
               val estado = _estadoUi.value
               val usuarioId = estado.usuarioActual?.idUsuario ?: return@launch
               val cancionIds = estado.cancionesSeleccionadas
               val portadaUriString =
                  evento.portadaUri?.let {
                     imageRepository.copyImageFromUri(Uri.parse(it))?.toString()
                  }

               // 1. Creamos la nueva lista
               val nuevaLista =
                  PlaylistEntity(
                     idUsuario = usuarioId,
                     nombre = evento.nombre,
                     descripcion = evento.descripcion,
                     portadaUrl = portadaUriString,
                  )
               val nuevaListaId = songDao.insertarListaReproduccion(nuevaLista)

               // 2. Añadimos las canciones a la nueva lista
               if (nuevaListaId != -1L) {
                  cancionIds.forEach { cancionId ->
                     val detalle =
                        PlaylistItemEntity(idLista = nuevaListaId.toInt(), idCancion = cancionId)
                     songDao.insertarDetalleLista(detalle)
                  }
               }

               // 3. Limpiamos y salimos del modo selección
               _estadoUi.update {
                  it.copy(
                     esModoSeleccion = false,
                     cancionesSeleccionadas = emptySet(),
                     mostrarDialogoPlaylist = false,
                  )
               }
            }
         }
         is BibliotecaEvento.ActivarModoSeleccion -> {
            _estadoUi.update {
               it.copy(
                  esModoSeleccion = true,
                  // Al activar, seleccionamos la primera canción
                  cancionesSeleccionadas = setOf(evento.cancion.cancion.idCancion),
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
               it.copy(
                  cancionesSeleccionadas = seleccionActual,
                  esModoSeleccion = nuevoModoSeleccion,
               )
            }
         }
         is BibliotecaEvento.SeleccionarTodo -> {
            _estadoUi.update {
               val todosLosIds = it.canciones.map { cancion -> cancion.cancion.idCancion }.toSet()
               // Si ya están todos seleccionados, los deseleccionamos. Si no, los
               // seleccionamos todos.
               val nuevaSeleccion =
                  if (it.cancionesSeleccionadas == todosLosIds) {
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

               songDao.quitarCancionesDeLista(listaId, cancionIds.map { it })

               // Salimos del modo selección
               _estadoUi.update {
                  it.copy(esModoSeleccion = false, cancionesSeleccionadas = emptySet())
               }
            }
         }
         is BibliotecaEvento.EliminarListaDeReproduccionActual -> {
            viewModelScope.launch {
               val listaId = _estadoUi.value.listaActual?.idLista ?: return@launch
               songDao.eliminarListaPorId(listaId)
               // Volvemos a la pantalla de listas
               cambiarCuerpo(TipoDeCuerpoBiblioteca.LISTAS)
            }
         }
         is BibliotecaEvento.AbrirDialogoPlaylist -> {
            _estadoUi.update {
               it.copy(mostrarDialogoPlaylist = true, cancionParaAnadirALista = evento.cancion)
            }
         }
         is BibliotecaEvento.CerrarDialogoPlaylist -> {
            _estadoUi.update {
               it.copy(mostrarDialogoPlaylist = false, cancionParaAnadirALista = null)
            }
         }
         is BibliotecaEvento.AnadirCancionAListasExistentes -> {
            viewModelScope.launch {
               val cancionId =
                  _estadoUi.value.cancionParaAnadirALista?.cancion?.idCancion ?: return@launch
               evento.idListas.forEach { listaId ->
                  val detalle = PlaylistItemEntity(idLista = listaId, idCancion = cancionId)
                  songDao.insertarDetalleLista(detalle)
               }
               // Cerramos el diálogo después de añadir
               _estadoUi.update {
                  it.copy(mostrarDialogoPlaylist = false, cancionParaAnadirALista = null)
               }
            }
         }
         is BibliotecaEvento.CrearNuevaListaYAnadirCancion -> {
            viewModelScope.launch {
               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario ?: return@launch
               val cancionId =
                  _estadoUi.value.cancionParaAnadirALista?.cancion?.idCancion ?: return@launch
               val portadaUriString =
                  evento.portadaUri?.let {
                     imageRepository.copyImageFromUri(it.toUri())?.toString()
                  }
               // 1. Crear la nueva lista
               val nuevaLista =
                  PlaylistEntity(
                     idUsuario = usuarioId,
                     nombre = evento.nombre,
                     descripcion = evento.descripcion,
                     portadaUrl = portadaUriString,
                  )
               val nuevaListaId = songDao.insertarListaReproduccion(nuevaLista)

               // 2. Añadir la canción a la nueva lista
               if (nuevaListaId != -1L) { // -1L indica que la inserción falló
                  val detalle =
                     PlaylistItemEntity(idLista = nuevaListaId.toInt(), idCancion = cancionId)
                  songDao.insertarDetalleLista(detalle)
               }

               // 3. Cerramos el diálogo
               _estadoUi.update {
                  it.copy(mostrarDialogoPlaylist = false, cancionParaAnadirALista = null)
               }
            }
         }
         is BibliotecaEvento.AlternarFavorito -> {
            viewModelScope.launch {
               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario ?: return@launch
               val cancionId = evento.songWithArtist.cancion.idCancion

               if (evento.songWithArtist.esFavorita) {
                  // Si ya es favorita, la quitamos
                  songDao.quitarDeFavoritos(usuarioId, cancionId)
               } else {
                  // Si no es favorita, la añadimos
                  val nuevoFavorito = FavoriteEntity(idUsuario = usuarioId, idCancion = cancionId)
                  songDao.agregarAFavoritos(nuevoFavorito)
               }
            }
         }
         is BibliotecaEvento.ForzarReescaneo -> {
            musicScannerManager.escanearAhora()
         }
         is BibliotecaEvento.CambiarCuerpo -> cambiarCuerpo(evento.nuevoCuerpo)
         is BibliotecaEvento.TextoDeBusquedaCambiado -> {
            _estadoUi.update { it.copy(textoDeBusqueda = evento.texto) }
         }
         is BibliotecaEvento.AlbumSeleccionado ->
            cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM, album = evento.album)
         is BibliotecaEvento.ArtistaSeleccionado ->
            cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA, artista = evento.artista)
         is BibliotecaEvento.GeneroSeleccionado ->
            cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO, genero = evento.genero)
         is BibliotecaEvento.ListaSeleccionada ->
            cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA, lista = evento.lista)
         is BibliotecaEvento.PermisoConcedido -> {
            // Inicializar sistema de escaneo completo (solo la primera vez)
            if (!musicScannerManager.estaInicializado.value) {
               android.util.Log.d(
                  "BibliotecaVM",
                  "🚀 Inicializando MusicScannerManager con escaneo inicial",
               )
               musicScannerManager.inicializar(ejecutarEscaneoInicial = true)
            } else {
               android.util.Log.d("BibliotecaVM", "✅ MusicScannerManager ya inicializado")
            }
         }
      }
   }

   private fun observarYFiltrarAlbumes() {
      viewModelScope.launch {
         val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
         songDao
            .obtenerTodosLosAlbumes()
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

   private fun observarEstadoEscaneo() {
      viewModelScope.launch {
         musicScannerManager.estadoUnificado.collect { estadoUnificado ->
            _estadoUi.update { ui ->
               ui.copy(
                  estaEscaneando = estadoUnificado.estaEscaneando,
                  escaneoManualEnProgreso = estadoUnificado.estaEscaneando,
                  progresoEscaneo = estadoUnificado.progreso,
                  mensajeEscaneo =
                     estadoUnificado.ultimoResultado?.let {
                        if (it.exitoso) "Añadidas: ${it.nuevas}, Eliminadas: ${it.eliminadas}"
                        else it.error
                     },
                  errorDeEscaneo = estadoUnificado.ultimoResultado?.error,
               )
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
         val criterioDeOrdenamiento =
            _estadoUi.map { it.criterioDeOrdenamiento }.distinctUntilChanged()
         val direccionDeOrdenamiento =
            _estadoUi.map { it.direccionDeOrdenamiento }.distinctUntilChanged()

         fuenteDeCancionesActiva
            .flatMapLatest { flowDeCanciones ->
               // El 'combine' ahora incluye los nuevos flows.
               combine(
                  flowDeCanciones,
                  textoDeBusqueda,
                  criterioDeOrdenamiento,
                  direccionDeOrdenamiento,
               ) { canciones, busqueda, criterio, direccion ->
                  // 1. La Búsqueda (filtro) se mantiene igual.
                  val cancionesBuscadas =
                     if (busqueda.isBlank()) {
                        canciones
                     } else {
                        // 1. Normalizamos y dividimos la búsqueda en palabras (tokens)
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        val tokensDeBusqueda =
                           busquedaNormalizada.split(" ").filter { it.isNotBlank() }

                        canciones.filter { cancionConArtista ->
                           // 2. Normalizamos el texto de la canción y el artista
                           val textoDeCancionNormalizado =
                              normalizarTexto(
                                 "${cancionConArtista.cancion.titulo} ${cancionConArtista.artistaNombre}"
                              )
                           // 3. Verificamos que TODOS los tokens de búsqueda estén
                           // presentes
                           tokensDeBusqueda.all { token ->
                              textoDeCancionNormalizado.contains(token)
                           }
                        }
                     }

                  // --- CAMBIO #2: CORRECCIÓN DE NULABILIDAD EN ORDENAMIENTO ---
                  val cancionesOrdenadas =
                     when (criterio) {
                        CriterioDeOrdenamiento.NINGUNO -> cancionesBuscadas
                        CriterioDeOrdenamiento.POR_TITULO ->
                           cancionesBuscadas.sortedBy { it.cancion.titulo }
                        CriterioDeOrdenamiento.POR_ARTISTA ->
                           cancionesBuscadas.sortedBy { it.artistaNombre ?: "" }
                        CriterioDeOrdenamiento.POR_ALBUM ->
                           cancionesBuscadas.sortedBy { it.albumNombre ?: "" }
                        CriterioDeOrdenamiento.POR_GENERO ->
                           cancionesBuscadas.sortedBy { it.generoNombre ?: "" }
                        // Aseguramos que idCancion no sea nulo para ordenar
                        CriterioDeOrdenamiento.MAS_RECIENTE ->
                           cancionesBuscadas.sortedByDescending { it.cancion.idCancion }
                     }

                  if (direccion == DireccionDeOrdenamiento.DESCENDENTE) {
                     cancionesOrdenadas.reversed()
                  } else {
                     cancionesOrdenadas
                  }
               }
            }
            .collectLatest { cancionesFinales ->
               _estadoUi.update { it.copy(canciones = cancionesFinales) }
            }
      }
   }

   private fun normalizarTexto(texto: String): String {
      val textoSinAcentos =
         Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
      return textoSinAcentos.lowercase()
   }

   // --- CAMBIO #5: 'cambiarCuerpo' REFACTORIZADO PARA SER MÁS ROBUSTO ---
   private fun cambiarCuerpo(
      nuevoCuerpo: TipoDeCuerpoBiblioteca,
      album: AlbumEntity? = null,
      artista: ArtistEntity? = null,
      genero: GenreEntity? = null,
      lista: PlaylistEntity? = null,
   ) {
      var nuevaFuente: Flow<List<SongWithArtist>>? = null
      val esVistaDeLista = nuevoCuerpo == TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA
      // 1. Actualizamos el estado de la UI para la nueva vista y limpiamos la búsqueda.
      _estadoUi.update {
         it.copy(
            cuerpoActual = nuevoCuerpo,
            textoDeBusqueda = "",
            esModoSeleccion = false,
            cancionesSeleccionadas = emptySet(),
            listaActual = if (esVistaDeLista) lista else null, // <-- Reinicio clave
         )
      }

      // 2. Simplemente actualizamos el título y, si es una vista de canciones,
      //    le decimos al observador de canciones cuál es su nueva fuente de datos.
      when (nuevoCuerpo) {

         // CASOS QUE NO SON LISTAS DE CANCIONES (SOLO CAMBIAN EL TÍTULO)
         TipoDeCuerpoBiblioteca.ALBUMES -> _estadoUi.update { it.copy(tituloDelCuerpo = "Álbumes") }
         TipoDeCuerpoBiblioteca.ARTISTAS ->
            _estadoUi.update { it.copy(tituloDelCuerpo = "Artistas") }
         TipoDeCuerpoBiblioteca.GENEROS -> _estadoUi.update { it.copy(tituloDelCuerpo = "Géneros") }
         TipoDeCuerpoBiblioteca.LISTAS ->
            _estadoUi.update { it.copy(tituloDelCuerpo = "Mis Listas") }

         // CASOS QUE SÍ SON LISTAS DE CANCIONES (CAMBIAN EL TÍTULO Y LA FUENTE)
         TipoDeCuerpoBiblioteca.CANCIONES -> {
            _estadoUi.update { it.copy(tituloDelCuerpo = "Canciones") }
            nuevaFuente =
               usuarioIdFlow.flatMapLatest { userId -> songDao.obtenerCancionesConArtista(userId) }
         }
         TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM -> {
            album?.let {
               _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.titulo) }
               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario

               // Verificamos que el ID no sea nulo antes de llamar a la función.
               if (usuarioId != null) {
                  nuevaFuente = songDao.obtenerCancionesDeAlbumConArtista(it.idAlbum, usuarioId)
               }
            }
         }
         TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA -> {
            artista?.let {
               _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario

               // Verificamos que el ID no sea nulo antes de llamar a la función.
               if (usuarioId != null) {
                  nuevaFuente = songDao.obtenerCancionesDeArtistaConArtista(it.idArtista, usuarioId)
               }
            }
         }
         TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO -> {
            genero?.let {
               _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario

               // Verificamos que el ID no sea nulo antes de llamar a la función.
               if (usuarioId != null) {
                  nuevaFuente = songDao.obtenerCancionesDeGeneroConArtista(it.idGenero, usuarioId)
               }
            }
         }
         TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
            lista?.let {
               // Actualizamos el título y nos aseguramos de que la lista esté en el estado
               _estadoUi.update { estado ->
                  estado.copy(tituloDelCuerpo = it.nombre, listaActual = it)
               }

               val usuarioId = _estadoUi.value.usuarioActual?.idUsuario
               if (usuarioId != null) {
                  nuevaFuente = songDao.obtenerCancionesDeListaConArtista(it.idLista, usuarioId)
               }
            }
         }
         TipoDeCuerpoBiblioteca.FAVORITOS -> {
            _estadoUi.update { it.copy(tituloDelCuerpo = "Favoritos") }
            val usuarioId = _estadoUi.value.usuarioActual?.idUsuario ?: -1
            if (usuarioId != -1) {
               nuevaFuente = songDao.obtenerFavoritas(usuarioId)
            }
         }
      }

      // 3. Actualizamos el 'interruptor' de la fuente de canciones.
      // Si la nueva vista no es de canciones, le pasamos un flow vacío para limpiar la lista.
      fuenteDeCancionesActiva.value = nuevaFuente ?: flowOf(emptyList())
   }

   fun cargarDatosDeUsuario(usuarioId: Int) {
      if (_estadoUi.value.usuarioActual?.idUsuario == usuarioId) return
      viewModelScope.launch {
         if (usuarioId != -1) {
            val usuario = userRepository.obtenerUsuarioPorId(usuarioId)
            _estadoUi.update { it.copy(usuarioActual = usuario) }
         }
      }
   }
}
