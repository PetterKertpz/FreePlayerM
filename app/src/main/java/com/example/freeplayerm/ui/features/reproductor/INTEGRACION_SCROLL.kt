package com.example.freeplayerm.ui.features.reproductor

/**
 * ========================================================================
 * 📋 GUÍA DE INTEGRACIÓN - DETECCIÓN DE SCROLL
 * ========================================================================
 *
 * Para que el panel del reproductor se minimice automáticamente durante
 * el scroll, cada pantalla con listas debe notificar al ViewModel.
 *
 * ========================================================================
 */

/*
 * PASO 1: En cada pantalla con LazyColumn/LazyVerticalGrid
 * --------------------------------------------------------
 *
 * Agregar la detección de scroll:
 */

/*
@Composable
fun PantallaCanciones(
    reproductorViewModel: ReproductorViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()

    // ✅ Notificar al reproductor cuando hay scroll activo
    LaunchedEffect(listState.isScrollInProgress) {
        reproductorViewModel.onEvento(
            ReproductorEvento.Panel.NotificarScroll(listState.isScrollInProgress)
        )
    }

    LazyColumn(
        state = listState,
        // ... resto de la configuración
    ) {
        items(canciones) { cancion ->
            ItemCancion(cancion)
        }
    }
}
*/

/*
 * PASO 2: Para LazyVerticalGrid
 * -----------------------------
 */

/*
@Composable
fun PantallaAlbumes(
    reproductorViewModel: ReproductorViewModel = hiltViewModel()
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState.isScrollInProgress) {
        reproductorViewModel.onEvento(
            ReproductorEvento.Panel.NotificarScroll(gridState.isScrollInProgress)
        )
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2)
    ) {
        items(albumes) { album ->
            ItemAlbum(album)
        }
    }
}
*/

/*
 * PASO 3: Integración en el Scaffold principal
 * --------------------------------------------
 *
 * El ReproductorUnificado debe estar en el BottomSheetScaffold o
 * en un Box con el contenido principal:
 */

/*
@Composable
fun MainScreen(
    reproductorViewModel: ReproductorViewModel = hiltViewModel()
) {
    val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

    // Altura del panel según el modo
    val peekHeight = remember(estadoReproductor.modoPanelEfectivo) {
        estadoReproductor.modoPanelEfectivo.peekHeightDp.dp
    }

    BottomSheetScaffold(
        sheetContent = {
            ReproductorUnificado(
                estado = estadoReproductor,
                onEvento = reproductorViewModel::onEvento
            )
        },
        sheetPeekHeight = peekHeight,
        // ... resto
    ) { paddingValues ->
        // Contenido principal (NavHost, etc.)
        NavHost(
            modifier = Modifier.padding(paddingValues)
        )
    }
}
*/

/*
 * PASO 4: Manejar el efecto AbrirUrl
 * ----------------------------------
 */

/*
@Composable
fun MainScreen(
    reproductorViewModel: ReproductorViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Observar efectos
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
                    // Abrir URL en navegador externo
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(efecto.url))
                    context.startActivity(intent)
                }
            }
        }
    }

    // ... resto del composable
}
*/

/*
 * ========================================================================
 * ESTRUCTURA DE ARCHIVOS FINAL
 * ========================================================================
 *
 * ui/features/reproductor/
 * ├── ReproductorEstado.kt          ← Enums, Estado, Eventos
 * ├── ReproductorViewModel.kt       ← Lógica de negocio
 * ├── ReproductorUnificado.kt       ← Composable orquestador
 * ├── IconosReproductor.kt          ← Iconos centralizados
 * └── components/                   ← Componentes modulares
 *     ├── ViniloGiratorio.kt        ← Animación del vinilo
 *     ├── SliderProgreso.kt         ← Slider completo y compacto
 *     ├── ControlesReproduccion.kt  ← Botones de control
 *     ├── PanelMinimizado.kt        ← Modo 15%
 *     ├── PanelNormal.kt            ← Modo 25-30%
 *     ├── PanelExpandido.kt         ← Modo 100%
 *     └── TabsExpandido.kt          ← Tabs (Letra/Info/Enlaces)
 *
 * ========================================================================
 */