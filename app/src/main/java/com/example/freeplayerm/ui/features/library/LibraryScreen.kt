package com.example.freeplayerm.ui.features.library

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.example.freeplayerm.ui.features.library.components.AlbumsContent
import com.example.freeplayerm.ui.features.library.components.ArtistsContent
import com.example.freeplayerm.ui.features.library.components.FabSeleccionBiblioteca
import com.example.freeplayerm.ui.features.library.components.GenresContent
import com.example.freeplayerm.ui.features.library.components.PermissionRequestScreen
import com.example.freeplayerm.ui.features.library.components.PlaylistDialog
import com.example.freeplayerm.ui.features.library.components.PlaylistsContent
import com.example.freeplayerm.ui.features.library.components.SearchBarWithFilters
import com.example.freeplayerm.ui.features.library.components.SeccionEncabezadoConEstado
import com.example.freeplayerm.ui.features.library.components.SongsContent
import com.example.freeplayerm.ui.features.library.components.TransicionDeContenidoBiblioteca
import com.example.freeplayerm.ui.features.player.ModoPanelReproductor
import com.example.freeplayerm.ui.features.player.PlayerScreen
import com.example.freeplayerm.ui.features.player.PlayerState
import com.example.freeplayerm.ui.features.player.ReproductorEfecto
import com.example.freeplayerm.ui.features.player.ReproductorEvento
import com.example.freeplayerm.ui.features.player.ReproductorViewModel
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * ⚡ PANTALLA BIBLIOTECA - v3.1
 *
 * CORRECCIÓN v3.1:
 * - Eliminada sincronización conflictiva entre BottomSheetScaffold y modoPanel
 * - El BottomSheet ahora es solo contenedor, no controla estados
 * - PlayerScreen maneja todo internamente
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    usuarioId: Int,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel,
    onPermisosConfirmados: () -> Unit = {},
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

    // Observar efectos del reproductor
    LaunchedEffect(Unit) {
        reproductorViewModel.efectos.collect { efecto ->
            when (efecto) {
                is ReproductorEfecto.MostrarToast -> {
                    Toast.makeText(context, efecto.mensaje, Toast.LENGTH_SHORT).show()
                }
                is ReproductorEfecto.Error -> {
                    Toast.makeText(context, efecto.mensaje, Toast.LENGTH_LONG).show()
                }
                is ReproductorEfecto.AbrirUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, efecto.url.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    if (estadoPermiso.status.isGranted) {
        val estadoBiblioteca by libraryViewModel.estadoUi.collectAsStateWithLifecycle()
        val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

        val cuerpoActual = estadoBiblioteca.cuerpoActual
        val listScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyListState>() }
        val gridScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyGridState>() }

        val lazyListState =
            remember(cuerpoActual) { listScrollStates.getOrPut(cuerpoActual) { LazyListState() } }
        val lazyGridState =
            remember(cuerpoActual) { gridScrollStates.getOrPut(cuerpoActual) { LazyGridState() } }

        // ✅ MEJORADO: Detectar scroll solo si no está en modo expandido
        LaunchedEffect(lazyListState.isScrollInProgress, estadoReproductor.modoPanel) {
            // No notificar scroll si está expandido o en transición
            if (estadoReproductor.modoPanel != ModoPanelReproductor.EXPANDIDO) {
                reproductorViewModel.onEvento(
                    ReproductorEvento.Panel.NotificarScroll(lazyListState.isScrollInProgress)
                )
            }
        }

        LaunchedEffect(lazyGridState.isScrollInProgress, estadoReproductor.modoPanel) {
            if (estadoReproductor.modoPanel != ModoPanelReproductor.EXPANDIDO) {
                reproductorViewModel.onEvento(
                    ReproductorEvento.Panel.NotificarScroll(lazyGridState.isScrollInProgress)
                )
            }
        }

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

        CuerpoBibliotecaGalactico(
            estadoBiblioteca = estadoBiblioteca,
            estadoReproductor = estadoReproductor,
            lazyListState = lazyListState,
            lazyGridState = lazyGridState,
            onBibliotecaEvento = { evento ->
                libraryViewModel.enEvento(evento)
                if (evento is BibliotecaEvento.LimpiarBusqueda) focusManager.clearFocus()
            },
            onReproductorEvento = reproductorViewModel::onEvento,
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
    estadoReproductor: PlayerState,
    lazyListState: LazyListState,
    lazyGridState: LazyGridState,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit,
) {
    // ✅ NUEVO: Calcular altura dinámica basada en el modo del reproductor
    val alturaPanelActual =
        remember(estadoReproductor.modoPanel, estadoReproductor.cancionActual) {
            when {
                estadoReproductor.cancionActual == null -> 0.dp
                estadoReproductor.modoPanel == ModoPanelReproductor.EXPANDIDO ->
                    0.dp // No necesita padding
                estadoReproductor.modoPanel == ModoPanelReproductor.MINIMIZADO -> 80.dp
                else -> 160.dp // NORMAL
            }
        }

    // ✅ BackHandler para el modo expandido
    BackHandler(enabled = estadoReproductor.modoPanel == ModoPanelReproductor.EXPANDIDO) {
        onReproductorEvento(ReproductorEvento.Panel.Colapsar)
    }

    // BackHandler para navegación de biblioteca
    BackHandler(
        enabled =
            estadoReproductor.modoPanel != ModoPanelReproductor.EXPANDIDO &&
                (estadoBiblioteca.esModoSeleccion ||
                    estadoBiblioteca.cuerpoActual != TipoDeCuerpoBiblioteca.CANCIONES)
    ) {
        when {
            estadoBiblioteca.esModoSeleccion -> {
                onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion)
            }
            estadoBiblioteca.cuerpoActual != TipoDeCuerpoBiblioteca.CANCIONES -> {
                onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES))
            }
        }
    }

    ManejadorDeDialogos(estadoBiblioteca, onBibliotecaEvento)

    Box(modifier = Modifier.fillMaxSize()) {
        GalaxyBackground()

        // ✅ CAMBIO PRINCIPAL: Usar Box con el reproductor superpuesto
        if (estadoReproductor.modoPanel == ModoPanelReproductor.EXPANDIDO) {
            // Modo expandido: reproductor ocupa toda la pantalla
            PlayerScreen(
                estado = estadoReproductor,
                onEvento = onReproductorEvento,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Modos NORMAL y MINIMIZADO: usar estructura normal
            Column(modifier = Modifier.fillMaxSize()) {
                // TopBar
                SeccionEncabezadoConEstado(
                    estadoBiblioteca = estadoBiblioteca,
                    onBibliotecaEvento = onBibliotecaEvento,
                )

                // Contenido principal
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
                                                                        BibliotecaEvento
                                                                            .CambiarCuerpo(it)
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
                                                                        BibliotecaEvento
                                                                            .CambiarCuerpo(it)
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
                                onReproductorEvento = onReproductorEvento,
                            )
                        }

                        // ✅ Espacio dinámico para el reproductor
                        if (estadoReproductor.cancionActual != null) {
                            Spacer(modifier = Modifier.height(alturaPanelActual))
                        }
                    }
                }
            }

            // ✅ Reproductor flotante con el nuevo sistema de gestos
            if (estadoReproductor.cancionActual != null) {
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                    PlayerScreen(estado = estadoReproductor, onEvento = onReproductorEvento)
                }
            }

            // FAB flotante
            FabSeleccionBiblioteca(
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = alturaPanelActual + 16.dp),
                estadoBiblioteca = estadoBiblioteca,
                onBibliotecaEvento = onBibliotecaEvento,
            )
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
    onReproductorEvento: (ReproductorEvento) -> Unit,
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
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
            TipoDeCuerpoBiblioteca.FAVORITOS,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                SongsContent(
                    canciones = estadoBiblioteca.canciones,
                    estado = estadoBiblioteca,
                    lazyListState = lazyListState,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento,
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
                    onArtistaClick = {
                        onBibliotecaEvento(BibliotecaEvento.ArtistaSeleccionado(it))
                    },
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
                )
        }
    }
}

@Composable
private fun ManejadorDeDialogos(estado: BibliotecaEstado, onEvento: (BibliotecaEvento) -> Unit) {
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
}

private fun debeMostrarBusqueda(tipo: TipoDeCuerpoBiblioteca): Boolean {
    return tipo != TipoDeCuerpoBiblioteca.LISTAS
}

// ==========================================
// FUNCIONES HELPER PARA GESTOS
// ==========================================

private val ORDEN_NAVEGACION_SECCIONES =
    listOf(
        TipoDeCuerpoBiblioteca.CANCIONES,
        TipoDeCuerpoBiblioteca.ALBUMES,
        TipoDeCuerpoBiblioteca.ARTISTAS,
        TipoDeCuerpoBiblioteca.GENEROS,
        TipoDeCuerpoBiblioteca.LISTAS,
    )

private fun puedeNavegarConGestos(seccion: TipoDeCuerpoBiblioteca): Boolean {
    return seccion in ORDEN_NAVEGACION_SECCIONES
}

private fun obtenerSiguienteSeccion(
    seccionActual: TipoDeCuerpoBiblioteca
): TipoDeCuerpoBiblioteca? {
    if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null
    val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
    val siguienteIndice = indiceActual + 1
    return if (siguienteIndice < ORDEN_NAVEGACION_SECCIONES.size) {
        ORDEN_NAVEGACION_SECCIONES[siguienteIndice]
    } else null
}

private fun obtenerSeccionAnterior(seccionActual: TipoDeCuerpoBiblioteca): TipoDeCuerpoBiblioteca? {
    if (seccionActual !in ORDEN_NAVEGACION_SECCIONES) return null
    val indiceActual = ORDEN_NAVEGACION_SECCIONES.indexOf(seccionActual)
    val anteriorIndice = indiceActual - 1
    return if (anteriorIndice >= 0) {
        ORDEN_NAVEGACION_SECCIONES[anteriorIndice]
    } else null
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
        border = androidx.compose.foundation.BorderStroke(1.dp, colorIcono.copy(alpha = 0.5f)),
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

// ==========================================
// 🎨 DATOS MOCK PARA PREVIEWS
// ==========================================

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

    // ================= ESTADOS =================

    // 1. Estado Base (Canciones)
    val stateSongs =
        BibliotecaEstado(
            usuarioActual = user,
            cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
            canciones =
                List(10) {
                    songWithArtist1.copy(
                        cancion = song1.copy(idCancion = it, titulo = "Canción $it")
                    )
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

    // 5. Estado Reproductor (Minimizado)
    val playerMini =
        PlayerState(
            cancionActual = songWithArtist1,
            estaReproduciendo = true,
            modoPanel = ModoPanelReproductor.MINIMIZADO,
            progresoActualMs = 45000,
        )

    // 6. Estado Reproductor (Expandido)
    val playerExpanded = playerMini.copy(modoPanel = ModoPanelReproductor.EXPANDIDO)

    // 7. Estado Reproductor (Inactivo)
    val playerIdle = PlayerState(cancionActual = null, estaReproduciendo = false)
}

// ==========================================
// 📸 PREVIEWS DE ESCENARIOS
// ==========================================

@Preview(name = "1. Biblioteca - Lista Canciones", device = "id:pixel_7_pro")
@Composable
private fun PreviewLibrarySongs() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateSongs,
            estadoReproductor = LibraryPreviewMocks.playerIdle,
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}

@Preview(name = "2. Biblioteca - Grid Álbumes")
@Composable
private fun PreviewLibraryAlbums() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateAlbums,
            estadoReproductor = LibraryPreviewMocks.playerMini, // Con mini player
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}

@Preview(name = "3. Estado - Escaneando")
@Composable
private fun PreviewLibraryScanning() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateScanning,
            estadoReproductor = LibraryPreviewMocks.playerIdle,
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}

@Preview(name = "4. Modo Selección Activo")
@Composable
private fun PreviewLibrarySelectionMode() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateSelection,
            estadoReproductor = LibraryPreviewMocks.playerMini,
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}

@Preview(name = "5. Reproductor Expandido (Full Screen)")
@Composable
private fun PreviewPlayerExpanded() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateSongs,
            estadoReproductor = LibraryPreviewMocks.playerExpanded,
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}

@Preview(name = "6. Estado Vacío / Sin Permisos", heightDp = 600)
@Composable
private fun PreviewLibraryEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        CuerpoBibliotecaGalactico(
            estadoBiblioteca = LibraryPreviewMocks.stateSongs.copy(canciones = emptyList()),
            estadoReproductor = LibraryPreviewMocks.playerIdle,
            lazyListState = rememberLazyListState(),
            lazyGridState = rememberLazyGridState(),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
        )
    }
}
