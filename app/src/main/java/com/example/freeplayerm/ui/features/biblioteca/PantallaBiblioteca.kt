package com.example.freeplayerm.ui.features.biblioteca

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.data.local.entity.*
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.components.*
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado
import com.example.freeplayerm.ui.features.reproductor.*
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import java.util.Date

// ==========================================
// 1. COMPOSABLE INTELIGENTE (LÓGICA)
// ==========================================
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PantallaBiblioteca(
    usuarioId: Int,
    bibliotecaViewModel: BibliotecaViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel
) {
    val focusManager = LocalFocusManager.current

    // Gestión de Permisos según versión de Android
    val permisoRequerido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val estadoPermiso = rememberPermissionState(permission = permisoRequerido)

    if (estadoPermiso.status.isGranted) {
        val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
        val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()
        val letra by reproductorViewModel.letra.collectAsStateWithLifecycle()
        val infoArtista by reproductorViewModel.infoArtista.collectAsStateWithLifecycle()

        // Gestión de Scroll persistente por pestaña
        val cuerpoActual = estadoBiblioteca.cuerpoActual
        val lazyListState = remember(cuerpoActual) {
            bibliotecaViewModel.listScrollStates.getOrPut(cuerpoActual) { LazyListState(0, 0) }
        }
        val lazyGridState = remember(cuerpoActual) {
            bibliotecaViewModel.gridScrollStates.getOrPut(cuerpoActual) { LazyGridState(0, 0) }
        }

        // Efectos iniciales
        LaunchedEffect(Unit) {
            bibliotecaViewModel.enEvento(BibliotecaEvento.PermisoConcedido)
            if (estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES) {
                bibliotecaViewModel.enEvento(BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES))
            }
        }
        LaunchedEffect(usuarioId) {
            bibliotecaViewModel.cargarDatosDeUsuario(usuarioId)
        }

        CuerpoBibliotecaGalactico(
            estadoBiblioteca = estadoBiblioteca,
            estadoReproductor = estadoReproductor,
            letra = letra,
            infoArtista = infoArtista,
            lazyListState = lazyListState,
            lazyGridState = lazyGridState,
            onBibliotecaEvento = { evento ->
                bibliotecaViewModel.enEvento(evento)
                if (evento is BibliotecaEvento.LimpiarBusqueda) focusManager.clearFocus()
            },
            onReproductorEvento = reproductorViewModel::onEvento
        )
    } else {
        // Pantalla de Permisos con Fondo Galaxia
        Box(modifier = Modifier.fillMaxSize()) {
            FondoGalaxiaAnimado()
            PantallaSolicitudPermiso(estadoPermiso = estadoPermiso)
        }
    }
}

// ==========================================
// 2. COMPOSABLE VISUAL ("TONTO")
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoBibliotecaGalactico(
    estadoBiblioteca: BibliotecaEstado,
    estadoReproductor: ReproductorEstado,
    lazyListState: LazyListState,
    lazyGridState: LazyGridState,
    letra: String?,
    infoArtista: String?,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Altura del minireproductor (Peek)
    val peekHeight = if (estadoReproductor.cancionActual != null) 140.dp else 0.dp

    // Manejo del Back Button para cerrar el BottomSheet si está expandido
    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        // La lógica de colapso se maneja internamente o via evento si es necesario
    }

    // --- DIÁLOGOS FLOTANTES ---
    ManejadorDeDialogos(estadoBiblioteca, onBibliotecaEvento)

    // --- ESTRUCTURA PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize()) {

        // A. CAPA DE FONDO (Fija)
        FondoGalaxiaAnimado()

        // B. CAPA DE CONTENIDO (Scaffold con BottomSheet)
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = Color.White,
            containerColor = Color.Transparent,
            sheetDragHandle = null,
            sheetContent = {
                // CONTENIDO DEL REPRODUCTOR (Deslizable)
                ContenidoSheetReproductor(
                    estadoReproductor = estadoReproductor,
                    scaffoldState = scaffoldState,
                    onReproductorEvento = onReproductorEvento,
                    letra = letra,
                    infoArtista = infoArtista
                )
            },
            topBar = {
                // BARRA SUPERIOR FLOTANTE
                SeccionEncabezado(
                    usuario = estadoBiblioteca.usuarioActual,
                    cuerpoActual = estadoBiblioteca.cuerpoActual,
                    onMenuClick = { onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(it)) },
                    escaneoManualEnProgreso = estadoBiblioteca.escaneoManualEnProgreso,
                    onReescanearClick = { onBibliotecaEvento(BibliotecaEvento.ForzarReescaneo) }
                )
            }
        ) { paddingValues ->

            // C. CONTENIDO CENTRAL (Listas y Grillas)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp)
            ) {
                // Barra de Búsqueda (condicional)
                if (debeMostrarBusqueda(estadoBiblioteca.cuerpoActual)) {
                    BarraDeBusquedaYFiltros(
                        textoDeBusqueda = estadoBiblioteca.textoDeBusqueda,
                        criterioDeOrdenamiento = estadoBiblioteca.criterioDeOrdenamiento,
                        direccionDeOrdenamiento = estadoBiblioteca.direccionDeOrdenamiento,
                        enEvento = onBibliotecaEvento
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Contenedor de Listas
                Box(modifier = Modifier.weight(1f)) {
                    ContenidoPrincipalBiblioteca(
                        estadoBiblioteca = estadoBiblioteca,
                        lazyListState = lazyListState,
                        lazyGridState = lazyGridState,
                        onBibliotecaEvento = onBibliotecaEvento,
                        onReproductorEvento = onReproductorEvento
                    )
                }
            }
        }

        // D. FABs FLOTANTES (Modo Selección)
        FabsAccionesSeleccion(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = peekHeight + 16.dp),
            estadoBiblioteca = estadoBiblioteca,
            onBibliotecaEvento = onBibliotecaEvento
        )
    }
}

// ==========================================
// 3. SUB-COMPONENTES AUXILIARES
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoSheetReproductor(
    estadoReproductor: ReproductorEstado,
    scaffoldState: BottomSheetScaffoldState,
    onReproductorEvento: (ReproductorEvento) -> Unit,
    letra: String?,
    infoArtista: String?
) {
    if (estadoReproductor.cancionActual == null) {
        Spacer(Modifier.height(1.dp))
        return
    }

    val scope = rememberCoroutineScope()
    val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

    // Contenedor Glassmorphism para el Player
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF120818).copy(alpha = 0.95f), Color.Black)
                )
            )
    ) {
        // ✅ Solo UNA instancia de ReproductorUnificado
        ReproductorUnificado(
            estado = estadoReproductor,
            estaExpandido = isExpanded,
            onToggleExpandir = { nuevoEstado ->
                scope.launch {
                    if (nuevoEstado) {
                        scaffoldState.bottomSheetState.expand()
                    } else {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                }
            },
            onEvento = onReproductorEvento
        )
    }
}

@Composable
private fun ContenidoPrincipalBiblioteca(
    estadoBiblioteca: BibliotecaEstado,
    lazyListState: LazyListState,
    lazyGridState: LazyGridState,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    if (estadoBiblioteca.estaEscaneando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFD500F9))
        }
        return
    }

    TransicionDeContenidoBiblioteca(targetState = estadoBiblioteca.cuerpoActual) { cuerpo ->
        when (cuerpo) {
            TipoDeCuerpoBiblioteca.CANCIONES,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
            TipoDeCuerpoBiblioteca.FAVORITOS,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                // ✅ CORREGIDO: CuerpoCanciones maneja internamente los eventos del reproductor
                // No necesitamos wrapper aquí porque CuerpoCanciones ya tiene la lógica completa
                CuerpoCanciones(
                    canciones = estadoBiblioteca.canciones,
                    estado = estadoBiblioteca,
                    lazyListState = lazyListState,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento // ✅ Pasar directamente
                )
            }
            TipoDeCuerpoBiblioteca.ALBUMES -> CuerpoAlbumes(
                albumes = estadoBiblioteca.albumes,
                lazyGridState = lazyGridState,
                onAlbumClick = { onBibliotecaEvento(BibliotecaEvento.AlbumSeleccionado(it)) }
            )
            TipoDeCuerpoBiblioteca.ARTISTAS -> CuerpoArtistas(
                artistas = estadoBiblioteca.artistas,
                lazyGridState = lazyGridState,
                onArtistaClick = { onBibliotecaEvento(BibliotecaEvento.ArtistaSeleccionado(it)) }
            )
            TipoDeCuerpoBiblioteca.GENEROS -> CuerpoGeneros(
                generos = estadoBiblioteca.generos,
                lazyGridState = lazyGridState,
                onGeneroClick = { onBibliotecaEvento(BibliotecaEvento.GeneroSeleccionado(it)) }
            )
            TipoDeCuerpoBiblioteca.LISTAS -> CuerpoListas(
                listas = estadoBiblioteca.listas,
                lazyListState = lazyListState,
                onListaClick = { onBibliotecaEvento(BibliotecaEvento.ListaSeleccionada(it)) }
            )
        }
    }
}

@Composable
private fun FabsAccionesSeleccion(
    modifier: Modifier,
    estadoBiblioteca: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit
) {
    val visible = estadoBiblioteca.esModoSeleccion && estadoBiblioteca.cancionesSeleccionadas.isNotEmpty()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = { onBibliotecaEvento(BibliotecaEvento.AbrirDialogoAnadirSeleccionALista) },
            containerColor = Color(0xFFD500F9)
        ) {
            Text("+", color = Color.White)
        }
    }
}

@Composable
private fun ManejadorDeDialogos(
    estado: BibliotecaEstado,
    onEvento: (BibliotecaEvento) -> Unit
) {
    if (estado.mostrarDialogoPlaylist) {
        VentanaListasReproduccion(
            listasExistentes = estado.listas,
            onDismiss = { onEvento(BibliotecaEvento.CerrarDialogoPlaylist) },
            onCrearLista = { n, d, p ->
                if (estado.esModoSeleccion) onEvento(BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas(n, d, p))
                else onEvento(BibliotecaEvento.CrearNuevaListaYAnadirCancion(n, d, p))
            },
            onAnadirAListas = { ids ->
                if (estado.esModoSeleccion) onEvento(BibliotecaEvento.AnadirCancionesSeleccionadasAListas(ids))
                else onEvento(BibliotecaEvento.AnadirCancionAListasExistentes(ids))
            }
        )
    }
}

private fun debeMostrarBusqueda(tipo: TipoDeCuerpoBiblioteca): Boolean {
    return tipo != TipoDeCuerpoBiblioteca.LISTAS
}

// ==========================================
// 4. PREVIEWS Y MOCKS
// ==========================================

object PreviewDataMocks {
    val mockUsuario = UsuarioEntity(
        idUsuario = 1,
        nombreUsuario = "Astronauta",
        correo = "astro@freeplayer.com",
        tipoAutenticacion = "LOCAL",
        contrasenia = "secreto123"
    )

    private val mockCancionEntity1 = CancionEntity(
        idCancion = 101,
        idArtista = 5,
        idAlbum = 10,
        idGenero = 2,
        titulo = "Starlight Echoes",
        duracionSegundos = 245,
        origen = "LOCAL",
        archivoPath = "/music/starlight.mp3"
    )

    private val mockCancionEntity2 = CancionEntity(
        idCancion = 102,
        idArtista = 5,
        idAlbum = 10,
        idGenero = 2,
        titulo = "Nebula Dreams",
        duracionSegundos = 180,
        origen = "LOCAL",
        archivoPath = "/music/nebula.mp3"
    )

    val mockCancionConArtista1 = CancionConArtista(
        cancion = mockCancionEntity1,
        artistaNombre = "Cosmic Drifters",
        albumNombre = "Galaxy Tours",
        generoNombre = "Ambient",
        esFavorita = true,
        portadaPath = null,
        fechaLanzamiento = null
    )

    val mockCancionConArtista2 = CancionConArtista(
        cancion = mockCancionEntity2,
        artistaNombre = "Cosmic Drifters",
        albumNombre = "Galaxy Tours",
        generoNombre = "Ambient",
        esFavorita = false,
        portadaPath = null,
        fechaLanzamiento = null
    )

    val listaCancionesMock = listOf(
        mockCancionConArtista1,
        mockCancionConArtista2,
        mockCancionConArtista1.copy(cancion = mockCancionEntity1.copy(idCancion = 103, titulo = "Void Walker"))
    )

    val listaAlbumesMock = listOf(
        AlbumEntity(idAlbum = 1, idArtista = 5, titulo = "Galaxy Tours", anio = 2024, portadaPath = null),
        AlbumEntity(idAlbum = 2, idArtista = 6, titulo = "Dark Matter", anio = 2023, portadaPath = null)
    )

    val estadoBibliotecaCanciones = BibliotecaEstado(
        usuarioActual = mockUsuario,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        tituloDelCuerpo = "Todas las Canciones",
        canciones = listaCancionesMock,
        estaEscaneando = false
    )

    val estadoBibliotecaAlbumes = BibliotecaEstado(
        usuarioActual = mockUsuario,
        cuerpoActual = TipoDeCuerpoBiblioteca.ALBUMES,
        tituloDelCuerpo = "Álbumes",
        albumes = listaAlbumesMock
    )

    val estadoReproductorActivo = ReproductorEstado(
        cancionActual = mockCancionConArtista1,
        estaReproduciendo = true,
        progresoActualMs = 120000L,
        modoReproduccion = ModoReproduccion.EN_ORDEN,
        esFavorita = true
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "1. Lista Canciones + Player", device = "id:pixel_7_pro", showSystemUi = true)
@Composable
fun PreviewBibliotecaLista() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = PreviewDataMocks.estadoBibliotecaCanciones,
        estadoReproductor = PreviewDataMocks.estadoReproductorActivo,
        lazyListState = listState,
        lazyGridState = gridState,
        letra = "Letra de ejemplo...",
        infoArtista = "Biografía del artista...",
        onBibliotecaEvento = {},
        onReproductorEvento = {}
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "2. Grid Álbumes", showBackground = true)
@Composable
fun PreviewBibliotecaAlbumes() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = PreviewDataMocks.estadoBibliotecaAlbumes,
        estadoReproductor = ReproductorEstado(cancionActual = null),
        lazyListState = listState,
        lazyGridState = gridState,
        letra = null,
        infoArtista = null,
        onBibliotecaEvento = {},
        onReproductorEvento = {}
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "3. Modo Selección", showBackground = true)
@Composable
fun PreviewModoSeleccion() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val estadoSeleccion = PreviewDataMocks.estadoBibliotecaCanciones.copy(
        esModoSeleccion = true,
        cancionesSeleccionadas = setOf(101, 102),
        tituloDelCuerpo = "2 Seleccionados"
    )

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = estadoSeleccion,
        estadoReproductor = ReproductorEstado(cancionActual = null),
        lazyListState = listState,
        lazyGridState = gridState,
        letra = null,
        infoArtista = null,
        onBibliotecaEvento = {},
        onReproductorEvento = {}
    )
}