package com.example.freeplayerm.ui.features.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.features.library.components.DialogoCrearLista
import com.example.freeplayerm.ui.features.library.components.FabSeleccionBiblioteca
import com.example.freeplayerm.ui.features.library.components.PermissionRequestScreen
import com.example.freeplayerm.ui.features.library.components.PlaylistDialog
import com.example.freeplayerm.ui.features.library.components.PlaylistSelectionFab
import com.example.freeplayerm.ui.features.library.components.SearchBarWithFilters
import com.example.freeplayerm.ui.features.library.components.SeccionEncabezadoConEstado
import com.example.freeplayerm.ui.features.library.components.TransicionDeContenidoBiblioteca
import com.example.freeplayerm.ui.features.library.components.contents.AlbumsContent
import com.example.freeplayerm.ui.features.library.components.contents.ArtistsContent
import com.example.freeplayerm.ui.features.library.components.contents.GenresContent
import com.example.freeplayerm.ui.features.library.components.contents.PlaylistsContent
import com.example.freeplayerm.ui.features.library.components.contents.SongsContent
import com.example.freeplayerm.ui.features.player.gesture.PlayerGestureConstants
import com.example.freeplayerm.ui.features.player.layouts.PlayerPanel
import com.example.freeplayerm.ui.features.player.layouts.PlayerScreen
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.viewmodel.PlayerViewModel
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
   usuarioId: Int,
   libraryViewModel: LibraryViewModel = hiltViewModel(),
   onPermisosConfirmados: () -> Unit = {},
   onNavigateToPerfil: () -> Unit = {},
) {
   val context = LocalContext.current
   val focusManager = LocalFocusManager.current

   val permisoRequerido =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         Manifest.permission.READ_MEDIA_AUDIO
      } else {
         Manifest.permission.READ_EXTERNAL_STORAGE
      }
   val estadoPermiso = rememberPermissionState(permission = permisoRequerido)

   if (estadoPermiso.status.isGranted) {
      val estadoBiblioteca by libraryViewModel.estadoUi.collectAsStateWithLifecycle()

      val cuerpoActual = estadoBiblioteca.cuerpoActual
      val listScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyListState>() }
      val gridScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyGridState>() }

      val lazyListState =
         remember(cuerpoActual) { listScrollStates.getOrPut(cuerpoActual) { LazyListState() } }
      val lazyGridState =
         remember(cuerpoActual) { gridScrollStates.getOrPut(cuerpoActual) { LazyGridState() } }

      LaunchedEffect(Unit) {
         onPermisosConfirmados()
         libraryViewModel.enEvento(BibliotecaEvento.PermisoConcedido)
         if (estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES) {
            libraryViewModel.enEvento(
               BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES)
            )
         }
      }
      LaunchedEffect(usuarioId) { libraryViewModel.cargarDatosDeUsuario(usuarioId) }

      LaunchedEffect(Unit) {
         libraryViewModel.efectosNavegacion.collect { efecto ->
            when (efecto) {
               EfectoNavegacion.AbrirPerfil -> onNavigateToPerfil()
               EfectoNavegacion.AbrirConfiguraciones -> {
                  // TODO: Implementar cuando creemos la pantalla de configuraciones
               }
            }
         }
      }

      CuerpoBibliotecaGalactico(
         estadoBiblioteca = estadoBiblioteca,
         lazyListState = lazyListState,
         lazyGridState = lazyGridState,
         onBibliotecaEvento = { evento ->
            libraryViewModel.enEvento(evento)
            if (evento is BibliotecaEvento.LimpiarBusqueda) focusManager.clearFocus()
         },
      )
   } else {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground()
         PermissionRequestScreen(estadoPermiso = estadoPermiso)
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoBibliotecaGalactico(
   estadoBiblioteca: BibliotecaEstado,
   lazyListState: LazyListState,
   lazyGridState: LazyGridState,
   onBibliotecaEvento: (BibliotecaEvento) -> Unit,
   playerViewModel: PlayerViewModel = hiltViewModel(),
) {
   val playerState by playerViewModel.state.collectAsStateWithLifecycle()

   BackHandler(enabled = playerState.panelMode == PlayerPanelMode.EXPANDED) {
      playerViewModel.onEvent(PlayerEvent.Panel.Collapse)
   }

   BackHandler(
      enabled =
         playerState.panelMode != PlayerPanelMode.EXPANDED &&
            (estadoBiblioteca.esModoSeleccion ||
               estadoBiblioteca.esModoSeleccionListas || // <- Agregar esta línea
               estadoBiblioteca.cuerpoActual != TipoDeCuerpoBiblioteca.CANCIONES)
   ) {
      when {
         estadoBiblioteca.esModoSeleccion -> {
            onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion)
         }
         estadoBiblioteca.esModoSeleccionListas -> { // <- Agregar este caso
            onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccionListas)
         }
         estadoBiblioteca.cuerpoActual != TipoDeCuerpoBiblioteca.CANCIONES -> {
            onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES))
         }
      }
   }

   LaunchedEffect(lazyListState.isScrollInProgress, playerState.panelMode) {
      if (playerState.panelMode != PlayerPanelMode.EXPANDED) {
         playerViewModel.onEvent(PlayerEvent.Panel.NotifyScroll(lazyListState.isScrollInProgress))
      }
   }

   LaunchedEffect(lazyGridState.isScrollInProgress, playerState.panelMode) {
      if (playerState.panelMode != PlayerPanelMode.EXPANDED) {
         playerViewModel.onEvent(PlayerEvent.Panel.NotifyScroll(lazyGridState.isScrollInProgress))
      }
   }

   ManejadorDeDialogos(estadoBiblioteca, onBibliotecaEvento)

   Box(modifier = Modifier.fillMaxSize()) {
      GalaxyBackground()

      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
         val screenHeightDp = maxHeight
         val density = LocalDensity.current

         val alturaPanelActual =
            remember(playerState.panelMode, playerState.currentSong, screenHeightDp) {
               with(density) {
                  when {
                     playerState.currentSong == null -> 0.dp
                     playerState.panelMode == PlayerPanelMode.EXPANDED -> 0.dp
                     else -> {
                        screenHeightDp * PlayerGestureConstants.HEIGHT_FRACTION_NORMAL
                     }
                  }
               }
            }

         if (playerState.panelMode == PlayerPanelMode.EXPANDED) {
            PlayerScreen(viewModel = playerViewModel, modifier = Modifier.fillMaxSize())
         } else {
            Column(modifier = Modifier.fillMaxSize()) {
               SeccionEncabezadoConEstado(
                  estadoBiblioteca = estadoBiblioteca,
                  onBibliotecaEvento = onBibliotecaEvento,
               )

               Box(modifier = Modifier.weight(1f)) {
                  Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                     if (debeMostrarBusqueda(estadoBiblioteca.cuerpoActual)) {
                        SearchBarWithFilters(
                           textoDeBusqueda = estadoBiblioteca.textoDeBusqueda,
                           criterioDeOrdenamiento = estadoBiblioteca.criterioDeOrdenamiento,
                           direccionDeOrdenamiento = estadoBiblioteca.direccionDeOrdenamiento,
                           enEvento = onBibliotecaEvento,
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                     }

                     Box(
                        modifier =
                           Modifier.weight(1f).pointerInput(estadoBiblioteca.cuerpoActual) {
                              if (puedeNavegarConGestos(estadoBiblioteca.cuerpoActual)) {
                                 var dragDistanciaTotal = 0f
                                 var yaEjecuto = false

                                 detectHorizontalDragGestures(
                                    onDragStart = {
                                       dragDistanciaTotal = 0f
                                       yaEjecuto = false
                                    },
                                    onDragEnd = {
                                       dragDistanciaTotal = 0f
                                       yaEjecuto = false
                                    },
                                    onDragCancel = {
                                       dragDistanciaTotal = 0f
                                       yaEjecuto = false
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                       if (!yaEjecuto) {
                                          dragDistanciaTotal += dragAmount
                                          when {
                                             dragDistanciaTotal < -100f -> {
                                                change.consume()
                                                obtenerSiguienteSeccion(
                                                      estadoBiblioteca.cuerpoActual
                                                   )
                                                   ?.let {
                                                      onBibliotecaEvento(
                                                         BibliotecaEvento.CambiarCuerpo(it)
                                                      )
                                                   }
                                                yaEjecuto = true
                                             }
                                             dragDistanciaTotal > 100f -> {
                                                change.consume()
                                                obtenerSeccionAnterior(
                                                      estadoBiblioteca.cuerpoActual
                                                   )
                                                   ?.let {
                                                      onBibliotecaEvento(
                                                         BibliotecaEvento.CambiarCuerpo(it)
                                                      )
                                                   }
                                                yaEjecuto = true
                                             }
                                          }
                                       }
                                    },
                                 )
                              }
                           }
                     ) {
                        ContenidoPrincipalBiblioteca(
                           estadoBiblioteca = estadoBiblioteca,
                           lazyListState = lazyListState,
                           lazyGridState = lazyGridState,
                           onBibliotecaEvento = onBibliotecaEvento,
                           onPlayerEvent = playerViewModel::onEvent,
                        )
                     }

                     if (playerState.currentSong != null) {
                        Spacer(modifier = Modifier.height(alturaPanelActual))
                     }
                  }
               }
            }

            if (playerState.currentSong != null) {
               Box(
                  modifier =
                     Modifier.fillMaxWidth().height(alturaPanelActual).align(Alignment.BottomCenter)
               ) {
                  PlayerPanel(state = playerState, onEvent = playerViewModel::onEvent)
               }
            }

            FabSeleccionBiblioteca(
               modifier =
                  Modifier.align(Alignment.BottomEnd)
                     .padding(end = 16.dp, bottom = alturaPanelActual + 16.dp),
               estadoBiblioteca = estadoBiblioteca,
               onBibliotecaEvento = onBibliotecaEvento,
            )
            PlaylistSelectionFab(
               modifier =
                  Modifier.align(Alignment.BottomEnd)
                     .padding(end = 16.dp, bottom = alturaPanelActual + 16.dp),
               estadoBiblioteca = estadoBiblioteca,
               onBibliotecaEvento = onBibliotecaEvento,
            )
         }
      }
   }
}

// ==========================================
// SUB-COMPONENTES (sin cambios)
// ==========================================

@Composable
private fun ContenidoPrincipalBiblioteca(
   estadoBiblioteca: BibliotecaEstado,
   lazyListState: LazyListState,
   lazyGridState: LazyGridState,
   onBibliotecaEvento: (BibliotecaEvento) -> Unit,
   onPlayerEvent: (PlayerEvent) -> Unit, // ✅ Renombrado
) {
   if (estadoBiblioteca.estaEscaneando) {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
         CircularProgressIndicator(color = Color(0xFFD500F9))
      }
      return
   }

   val onZoomChange: (NivelZoom) -> Unit = { nuevoNivel ->
      onBibliotecaEvento(BibliotecaEvento.CambiarNivelZoom(nuevoNivel))
   }

   TransicionDeContenidoBiblioteca(targetState = estadoBiblioteca.cuerpoActual) { cuerpo ->
      when (cuerpo) {
         TipoDeCuerpoBiblioteca.CANCIONES,
         TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA,
         TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
         TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
         TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
         TipoDeCuerpoBiblioteca.FAVORITOS -> {
            SongsContent(
               canciones = estadoBiblioteca.canciones,
               estado = estadoBiblioteca,
               lazyListState = lazyListState,
               onBibliotecaEvento = onBibliotecaEvento,
               onPlayerEvent = onPlayerEvent,
            )
         }

         TipoDeCuerpoBiblioteca.ALBUMES ->
            AlbumsContent(
               albumes = estadoBiblioteca.albumes,
               lazyGridState = lazyGridState,
               nivelZoom = estadoBiblioteca.nivelZoom,
               onZoomChange = onZoomChange,
               onAlbumClick = { onBibliotecaEvento(BibliotecaEvento.AlbumSeleccionado(it)) },
            )

         TipoDeCuerpoBiblioteca.ARTISTAS ->
            ArtistsContent(
               artistas = estadoBiblioteca.artistas,
               lazyGridState = lazyGridState,
               nivelZoom = estadoBiblioteca.nivelZoom,
               onZoomChange = onZoomChange,
               onArtistaClick = { onBibliotecaEvento(BibliotecaEvento.ArtistaSeleccionado(it)) },
            )

         TipoDeCuerpoBiblioteca.GENEROS ->
            GenresContent(
               generos = estadoBiblioteca.generos,
               lazyGridState = lazyGridState,
               nivelZoom = estadoBiblioteca.nivelZoom,
               onZoomChange = onZoomChange,
               onGeneroClick = { onBibliotecaEvento(BibliotecaEvento.GeneroSeleccionado(it)) },
            )

         TipoDeCuerpoBiblioteca.LISTAS ->
            PlaylistsContent(
               listas = estadoBiblioteca.listas,
               lazyListState = lazyListState,
               nivelZoom = estadoBiblioteca.nivelZoom,
               onZoomChange = onZoomChange,
               onListaClick = { onBibliotecaEvento(BibliotecaEvento.ListaSeleccionada(it)) },
               esModoSeleccion = estadoBiblioteca.esModoSeleccionListas,
               listasSeleccionadas = estadoBiblioteca.listasSeleccionadas,
               onActivarModoSeleccion = {
                  onBibliotecaEvento(BibliotecaEvento.ActivarModoSeleccionListas(it))
               },
               onAlternarSeleccion = {
                  onBibliotecaEvento(BibliotecaEvento.AlternarSeleccionLista(it))
               },
            )
      }
   }
}

@Composable
private fun ManejadorDeDialogos(estado: BibliotecaEstado, onEvento: (BibliotecaEvento) -> Unit) {
   // Diálogo existente de playlist
   if (estado.mostrarDialogoPlaylist) {
      PlaylistDialog(
         listasExistentes = estado.listas,
         onDismiss = { onEvento(BibliotecaEvento.CerrarDialogoPlaylist) },
         onCrearLista = { n, d, p ->
            if (estado.esModoSeleccion)
               onEvento(BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas(n, d, p))
            else onEvento(BibliotecaEvento.CrearNuevaListaYAnadirCancion(n, d, p))
         },
         onAnadirAListas = { ids ->
            if (estado.esModoSeleccion)
               onEvento(BibliotecaEvento.AnadirCancionesSeleccionadasAListas(ids))
            else onEvento(BibliotecaEvento.AnadirCancionAListasExistentes(ids))
         },
      )
   }

   // Nuevo: Diálogo de edición de lista seleccionada
   if (estado.listaParaEditar != null) {
      DialogoCrearLista(
         listaAEditar = estado.listaParaEditar,
         onDismiss = { onEvento(BibliotecaEvento.CerrarDialogoEditarListaSeleccionada) },
         onCrear = { nombre, descripcion, portadaUri ->
            onEvento(
               BibliotecaEvento.GuardarCambiosListaSeleccionada(
                  nombre = nombre,
                  descripcion = descripcion,
                  portadaUri = portadaUri,
               )
            )
         },
      )
   }
}

private fun debeMostrarBusqueda(tipo: TipoDeCuerpoBiblioteca): Boolean {
   return tipo != TipoDeCuerpoBiblioteca.LISTAS
}

// ==========================================
// FUNCIONES HELPER PARA GESTOS
// ==========================================

// ✅ Orden completo con navegación circular
private val ORDEN_NAVEGACION_SECCIONES =
   listOf(
      TipoDeCuerpoBiblioteca.CANCIONES,
      TipoDeCuerpoBiblioteca.LISTAS,
      TipoDeCuerpoBiblioteca.ALBUMES,
      TipoDeCuerpoBiblioteca.ARTISTAS,
      TipoDeCuerpoBiblioteca.GENEROS,
      TipoDeCuerpoBiblioteca.FAVORITOS,
   )

// Subsecciones que NO deben permitir gestos horizontales (son contextuales)
private val SECCIONES_SIN_NAVEGACION =
   setOf(
      TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
      TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
      TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
      TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA,
   )

private fun puedeNavegarConGestos(seccion: TipoDeCuerpoBiblioteca): Boolean {
   return seccion !in SECCIONES_SIN_NAVEGACION
}

// ✅ Navegación circular hacia adelante
private fun obtenerSiguienteSeccion(
   seccionActual: TipoDeCuerpoBiblioteca
): TipoDeCuerpoBiblioteca? {
   if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null

   val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
   val siguienteIndice = (indiceActual + 1) % ORDEN_NAVEGACION_SECCIONES.size

   return ORDEN_NAVEGACION_SECCIONES[siguienteIndice]
}

// ✅ Navegación circular hacia atrás
private fun obtenerSeccionAnterior(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
   if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null

   val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
   val anteriorIndice =
      if (indiceActual == 0) {
         ORDEN_NAVEGACION_SECCIONES.size - 1
      } else {
         indiceActual - 1
      }

   return ORDEN_NAVEGACION_SECCIONES[anteriorIndice]
}

@Composable
fun ChipAccion(
   icono: ImageVector,
   texto: String,
   onClick: () -> Unit,
   colorIcono: Color = Color(0xFFD500F9),
) {
   Surface(
      onClick = onClick,
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
      border = BorderStroke(1.dp, colorIcono.copy(alpha = 0.5f)),
   ) {
      Row(
         modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
         Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(18.dp),
         )
         Text(
            text = texto,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
         )
      }
   }
}

private object LibraryPreviewMocks {

   // 👤 Usuario
   val user =
      UserEntity(
         idUsuario = 1,
         nombreUsuario = "CosmicTraveler",
         correo = "demo@freeplayer.com",
         tipoAutenticacion = "LOCAL",
         contraseniaHash = "",
      )

   // 🎵 Canción Base
   val song1 =
      SongEntity(
         idCancion = 1,
         idArtista = 1,
         idAlbum = 1,
         idGenero = 1,
         titulo = "Starboy",
         duracionSegundos = 230,
         origen = "LOCAL",
         archivoPath = "/music/starboy.mp3",
      )

   val songWithArtist1 =
      SongWithArtist(
         cancion = song1,
         artistaNombre = "The Weeknd",
         albumNombre = "Starboy",
         generoNombre = "Pop",
         esFavorita = true,
      )

   // 💿 Álbum
   val album1 =
      AlbumEntity(
         idAlbum = 1,
         idArtista = 1,
         titulo = "Random Access Memories",
         anio = 2013,
         tipo = AlbumEntity.TIPO_ALBUM,
         tituloNormalizado = "random access memories",
      )

   // 🎤 Artista
   val artist1 = ArtistEntity(idArtista = 1, nombre = "Daft Punk", tipo = ArtistEntity.TIPO_DUO)

   // 🎸 Género
   val genre1 = GenreEntity(idGenero = 1, nombre = "Electronic", emoji = "🤖", color = "#D500F9")

   // 📜 Playlist
   val playlist1 =
      PlaylistEntity(
         idLista = 1,
         idUsuario = 1,
         nombre = "Favoritos",
         totalCanciones = 15,
         esFavorita = true,
      )

   // ================= ESTADOS CORREGIDOS =================

   // 1. Estado Base (Canciones)
   val stateSongs =
      BibliotecaEstado(
         usuarioActual = user,
         cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
         canciones =
            List(10) {
               songWithArtist1.copy(cancion = song1.copy(idCancion = it, titulo = "Canción $it"))
            },
         tituloDelCuerpo = "Canciones",
      )

   // 2. Estado Álbumes
   val stateAlbums =
      BibliotecaEstado(
         usuarioActual = user,
         cuerpoActual = TipoDeCuerpoBiblioteca.ALBUMES,
         albumes = List(6) { album1.copy(idAlbum = it, titulo = "Álbum $it") },
         tituloDelCuerpo = "Álbumes",
      )

   // 3. Estado Escaneando
   val stateScanning = stateSongs.copy(estaEscaneando = true)

   // 4. Estado Selección
   val stateSelection =
      stateSongs.copy(esModoSeleccion = true, cancionesSeleccionadas = setOf(1, 3, 5))

   // ✅ ESTADOS DEL REPRODUCTOR CORREGIDOS

   // 5. Estado Reproductor (Minimizado)
   val playerMini =
      PlayerState(
         currentSong = songWithArtist1, // ✅ Corregido
         isPlaying = true, // ✅ Corregido
         currentPositionMs = 45000, // ✅ Corregido
      )

   // 6. Estado Reproductor (Normal)
   val playerNormal = playerMini.copy(panelMode = PlayerPanelMode.NORMAL) // ✅ Agregado

   // 7. Estado Reproductor (Expandido)
   val playerExpanded = playerMini.copy(panelMode = PlayerPanelMode.EXPANDED) // ✅ Corregido

   // 8. Estado Reproductor (Inactivo)
   val playerIdle = PlayerState(currentSong = null, isPlaying = false) // ✅ Corregido
}

// ==========================================
// 📸 PREVIEWS DE ESCENARIOS CORREGIDAS
// ==========================================

@Preview(name = "1. Biblioteca - Lista Canciones", device = "id:pixel_7_pro")
@Composable
private fun PreviewLibrarySongs() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateSongs,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "2. Biblioteca + Reproductor Minimizado", heightDp = 900)
@Composable
private fun PreviewLibraryWithMiniPlayer() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateSongs,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "3. Biblioteca + Reproductor Normal", heightDp = 900)
@Composable
private fun PreviewLibraryWithNormalPlayer() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateSongs,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "4. Reproductor Expandido (Pantalla Completa)", heightDp = 900)
@Composable
private fun PreviewPlayerExpanded() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateSongs,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "5. Biblioteca - Grid Álbumes + Player", heightDp = 900)
@Composable
private fun PreviewLibraryAlbumsWithPlayer() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateAlbums,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "6. Modo Selección + Reproductor", heightDp = 900)
@Composable
private fun PreviewSelectionModeWithPlayer() {
   FreePlayerMTheme(darkTheme = true) {
      CuerpoBibliotecaGalactico(
         estadoBiblioteca = LibraryPreviewMocks.stateSelection,
         lazyListState = rememberLazyListState(),
         lazyGridState = rememberLazyGridState(),
         onBibliotecaEvento = {},
      )
   }
}

@Preview(name = "7. Transición de Modos del Reproductor", widthDp = 1200, heightDp = 500)
@Composable
private fun PreviewPlayerModesTransition() {
   FreePlayerMTheme(darkTheme = true) {
      Row(
         modifier = Modifier.fillMaxSize().background(Color(0xFF05000C)),
         horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
         listOf("MINIMIZED", "NORMAL", "EXPANDED").forEach { label ->
            Box(modifier = Modifier.weight(1f)) {
               Column(modifier = Modifier.fillMaxSize()) {
                  Text(
                     label,
                     color = Color.White,
                     style = MaterialTheme.typography.labelSmall,
                     modifier = Modifier.padding(8.dp),
                  )
                  CuerpoBibliotecaGalactico(
                     estadoBiblioteca = LibraryPreviewMocks.stateSongs,
                     lazyListState = rememberLazyListState(),
                     lazyGridState = rememberLazyGridState(),
                     onBibliotecaEvento = {},
                  )
               }
            }
         }
      }
   }
}

@Preview(name = "8. Estados Reproductor en Biblioteca", heightDp = 2000)
@Composable
private fun PreviewAllPlayerStatesInLibrary() {
   FreePlayerMTheme(darkTheme = true) {
      Column(
         modifier = Modifier.fillMaxSize().background(Color(0xFF05000C)).padding(16.dp),
         verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
         listOf("Sin Reproductor", "Reproductor Minimizado", "Reproductor Normal").forEach { label
            ->
            Text(
               label,
               color = Color.White,
               style = MaterialTheme.typography.labelMedium,
               modifier = Modifier.padding(vertical = 8.dp),
            )

            Surface(
               modifier = Modifier.fillMaxWidth().height(600.dp),
               color = MaterialTheme.colorScheme.background,
            ) {
               CuerpoBibliotecaGalactico(
                  estadoBiblioteca = LibraryPreviewMocks.stateSongs,
                  lazyListState = rememberLazyListState(),
                  lazyGridState = rememberLazyGridState(),
                  onBibliotecaEvento = {},
               )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
         }
      }
   }
}
