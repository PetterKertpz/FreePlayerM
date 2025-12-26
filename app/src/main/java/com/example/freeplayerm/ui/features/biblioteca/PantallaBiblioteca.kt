package com.example.freeplayerm.ui.features.biblioteca

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.data.local.entity.*
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.components.*
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado
import com.example.freeplayerm.ui.features.reproductor.*
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


/**
 * ⚡ PANTALLA BIBLIOTECA - v3.0
 *
 * Integración con el nuevo sistema de 3 modos del reproductor:
 * ✅ Detección de scroll para minimizar automáticamente
 * ✅ Manejo de efectos (Toast, Error, AbrirUrl)
 * ✅ Altura dinámica del panel según modo
 * ✅ BackHandler para colapsar desde expandido
 *
 * @version 3.0 - Sistema de 3 Modos
 */

// ==========================================
// 1. COMPOSABLE INTELIGENTE (LÓGICA)
// ==========================================

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PantallaBiblioteca(
    usuarioId: Int,
    bibliotecaViewModel: BibliotecaViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel,
    onPermisosConfirmados: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Gestión de Permisos según versión de Android
    val permisoRequerido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val estadoPermiso = rememberPermissionState(permission = permisoRequerido)

    // ✅ NUEVO: Observar efectos del reproductor
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
        val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
        val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

        // Gestión de Scroll persistente por pestaña
        val cuerpoActual = estadoBiblioteca.cuerpoActual

// Mapa local de estados (sobrevive recomposiciones, no config changes)
        val listScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyListState>() }
        val gridScrollStates = remember { mutableMapOf<TipoDeCuerpoBiblioteca, LazyGridState>() }

        val lazyListState = remember(cuerpoActual) {
            listScrollStates.getOrPut(cuerpoActual) { LazyListState() }
        }
        val lazyGridState = remember(cuerpoActual) {
            gridScrollStates.getOrPut(cuerpoActual) { LazyGridState() }
        }

        // ✅ NUEVO: Detectar scroll para notificar al reproductor
        LaunchedEffect(lazyListState.isScrollInProgress) {
            reproductorViewModel.onEvento(
                ReproductorEvento.Panel.NotificarScroll(lazyListState.isScrollInProgress)
            )
        }
        LaunchedEffect(lazyGridState.isScrollInProgress) {
            reproductorViewModel.onEvento(
                ReproductorEvento.Panel.NotificarScroll(lazyGridState.isScrollInProgress)
            )
        }

        // Efectos iniciales
        LaunchedEffect(Unit) {
            onPermisosConfirmados() // ← Notifica a MainViewModel para inicializar scanner
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
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    // ✅ NUEVO: Altura dinámica según el modo del panel
    val peekHeight =
        remember(estadoReproductor.modoPanelEfectivo, estadoReproductor.cancionActual) {
            if (estadoReproductor.cancionActual == null) {
                0.dp
            } else {
                when (estadoReproductor.modoPanelEfectivo) {
                    ModoPanelReproductor.MINIMIZADO -> 80.dp
                    ModoPanelReproductor.NORMAL -> 160.dp
                    ModoPanelReproductor.EXPANDIDO -> 0.dp // Pantalla completa, no usa peek
                }
            }
        }

    // ✅ NUEVO: Sincronizar estado del BottomSheet con el modo del panel
    LaunchedEffect(estadoReproductor.modoPanel) {
        when (estadoReproductor.modoPanel) {
            ModoPanelReproductor.EXPANDIDO -> {
                scaffoldState.bottomSheetState.expand()
            }

            ModoPanelReproductor.NORMAL, ModoPanelReproductor.MINIMIZADO -> {
                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }
    }

    // ✅ NUEVO: BackHandler mejorado para el modo expandido
    BackHandler(
        enabled = estadoReproductor.modoPanel == ModoPanelReproductor.EXPANDIDO || scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        onReproductorEvento(ReproductorEvento.Panel.Colapsar)
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
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
                // ✅ NUEVO: Usar ReproductorUnificado con el nuevo sistema
                ContenidoSheetReproductor(
                    estadoReproductor = estadoReproductor, onReproductorEvento = onReproductorEvento
                )
            },
            topBar = {
                SeccionEncabezadoConEstado(
                    estadoBiblioteca = estadoBiblioteca, onBibliotecaEvento = onBibliotecaEvento
                )
            }) { paddingValues ->

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

        // D. FAB FLOTANTE EXPANDIBLE (Modo Selección)
        FabSeleccionBiblioteca(
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

/**
 * ✅ NUEVO: Contenido del sheet simplificado usando ReproductorUnificado
 */
@Composable
private fun ContenidoSheetReproductor(
    estadoReproductor: ReproductorEstado, onReproductorEvento: (ReproductorEvento) -> Unit
) {
    if (estadoReproductor.cancionActual == null) {
        Spacer(Modifier.height(1.dp))
        return
    }

    // ✅ Usar el nuevo ReproductorUnificado con sistema de 3 modos
    ReproductorUnificado(
        estado = estadoReproductor, onEvento = onReproductorEvento
    )
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
            TipoDeCuerpoBiblioteca.CANCIONES, TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM, TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA, TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO, TipoDeCuerpoBiblioteca.FAVORITOS, TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                CuerpoCanciones(
                    canciones = estadoBiblioteca.canciones,
                    estado = estadoBiblioteca,
                    lazyListState = lazyListState,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento
                )
            }

            TipoDeCuerpoBiblioteca.ALBUMES -> CuerpoAlbumes(
                albumes = estadoBiblioteca.albumes,
                lazyGridState = lazyGridState,
                onAlbumClick = { onBibliotecaEvento(BibliotecaEvento.AlbumSeleccionado(it)) })

            TipoDeCuerpoBiblioteca.ARTISTAS -> CuerpoArtistas(
                artistas = estadoBiblioteca.artistas,
                lazyGridState = lazyGridState,
                onArtistaClick = { onBibliotecaEvento(BibliotecaEvento.ArtistaSeleccionado(it)) })

            TipoDeCuerpoBiblioteca.GENEROS -> CuerpoGeneros(
                generos = estadoBiblioteca.generos,
                lazyGridState = lazyGridState,
                onGeneroClick = { onBibliotecaEvento(BibliotecaEvento.GeneroSeleccionado(it)) })

            TipoDeCuerpoBiblioteca.LISTAS -> CuerpoListas(
                listas = estadoBiblioteca.listas,
                lazyListState = lazyListState,
                onListaClick = { onBibliotecaEvento(BibliotecaEvento.ListaSeleccionada(it)) })
        }
    }
}

@Composable
private fun ManejadorDeDialogos(
    estado: BibliotecaEstado, onEvento: (BibliotecaEvento) -> Unit
) {
    if (estado.mostrarDialogoPlaylist) {
        VentanaListasReproduccion(
            listasExistentes = estado.listas,
            onDismiss = { onEvento(BibliotecaEvento.CerrarDialogoPlaylist) },
            onCrearLista = { n, d, p ->
                if (estado.esModoSeleccion) onEvento(
                    BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas(
                        n, d, p
                    )
                )
                else onEvento(BibliotecaEvento.CrearNuevaListaYAnadirCancion(n, d, p))
            },
            onAnadirAListas = { ids ->
                if (estado.esModoSeleccion) onEvento(
                    BibliotecaEvento.AnadirCancionesSeleccionadasAListas(
                        ids
                    )
                )
                else onEvento(BibliotecaEvento.AnadirCancionAListasExistentes(ids))
            })
    }
}

private fun debeMostrarBusqueda(tipo: TipoDeCuerpoBiblioteca): Boolean {
    return tipo != TipoDeCuerpoBiblioteca.LISTAS
}


/**
 * Chip de acción individual
 */
@Composable
fun ChipAccion(
    icono: ImageVector, texto: String, onClick: () -> Unit, colorIcono: Color = Color(0xFFD500F9)
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
        border = BorderStroke(1.dp, colorIcono.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = texto,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==========================================
// 5. PREVIEWS Y MOCKS
// ==========================================

object PreviewDataMocks {
    val mockUsuario = UsuarioEntity(
        idUsuario = 1,
        nombreUsuario = "Astronauta",
        correo = "astro@freeplayer.com",
        tipoAutenticacion = "LOCAL",
        contraseniaHash = "Secreto12345"
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
        mockCancionConArtista1, mockCancionConArtista2, mockCancionConArtista1.copy(
            cancion = mockCancionEntity1.copy(
                idCancion = 103, titulo = "Void Walker"
            )
        )
    )

    val listaAlbumesMock = listOf(
        AlbumEntity(
            idAlbum = 1, idArtista = 5, titulo = "Galaxy Tours", anio = 2024, portadaPath = null
        ), AlbumEntity(
            idAlbum = 2, idArtista = 6, titulo = "Dark Matter", anio = 2023, portadaPath = null
        )
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

    // ✅ ACTUALIZADO: Estado con los nuevos campos
    val estadoReproductorActivo = ReproductorEstado(
        cancionActual = mockCancionConArtista1,
        estaReproduciendo = true,
        progresoActualMs = 120000L,
        modoReproduccion = ModoReproduccion.EN_ORDEN,
        modoRepeticion = ModoRepeticion.NO_REPETIR,
        esFavorita = true,
        modoPanel = ModoPanelReproductor.NORMAL
    )

    val estadoReproductorMinimizado = estadoReproductorActivo.copy(
        modoPanel = ModoPanelReproductor.MINIMIZADO, isScrollActivo = true
    )

    val estadoReproductorExpandido = estadoReproductorActivo.copy(
        modoPanel = ModoPanelReproductor.EXPANDIDO,
        letra = "Esta es la letra de ejemplo de la canción...\n\nVerso 1\nLínea 2\nLínea 3",
        infoArtista = "Cosmic Drifters es una banda de ambient electrónico...",
        enlaceGenius = "https://genius.com",
        enlaceYoutube = "https://youtube.com",
        enlaceGoogle = "https://google.com"
    )
}

@Preview(
    name = "1. Lista Canciones + Player Normal", device = "id:pixel_7_pro", showSystemUi = true
)
@Composable
fun PreviewBibliotecaLista() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = PreviewDataMocks.estadoBibliotecaCanciones,
        estadoReproductor = PreviewDataMocks.estadoReproductorActivo,
        lazyListState = listState,
        lazyGridState = gridState,
        onBibliotecaEvento = {},
        onReproductorEvento = {})
}

@Preview(name = "2. Player Minimizado (Scroll)", device = "id:pixel_7_pro", showSystemUi = true)
@Composable
fun PreviewBibliotecaMinimizado() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = PreviewDataMocks.estadoBibliotecaCanciones,
        estadoReproductor = PreviewDataMocks.estadoReproductorMinimizado,
        lazyListState = listState,
        lazyGridState = gridState,
        onBibliotecaEvento = {},
        onReproductorEvento = {})
}

@Preview(name = "3. Grid Álbumes", showBackground = true)
@Composable
fun PreviewBibliotecaAlbumes() {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    CuerpoBibliotecaGalactico(
        estadoBiblioteca = PreviewDataMocks.estadoBibliotecaAlbumes,
        estadoReproductor = ReproductorEstado(cancionActual = null),
        lazyListState = listState,
        lazyGridState = gridState,
        onBibliotecaEvento = {},
        onReproductorEvento = {})
}

@Preview(name = "4. Modo Selección", showBackground = true)
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
        onBibliotecaEvento = {},
        onReproductorEvento = {})
}