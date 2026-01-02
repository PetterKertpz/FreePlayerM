package com.example.freeplayerm.ui.features.library.components.layouts

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.domain.LibraryItem
import com.example.freeplayerm.ui.features.library.domain.LibraryZoomConfig
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlin.math.sqrt

/**
 * 🎨 LAYOUT ESPECIAL MEJORADO PARA GRIDS
 *
 * Versión mejorada con: ✨ Transiciones animadas suaves 🎭 Efectos visuales dinámicos 🌊 Animaciones
 * de entrada progresivas 🎯 Espaciado inteligente adaptativo ⚡ Gestos optimizados (un nivel por
 * gesto)
 */
@Composable
fun <T : LibraryItem> LibraryGridLayout(
    items: List<T>,
    gridState: LazyGridState,
    minItemSize: Dp = 160.dp,
    nivelZoom: NivelZoom = NivelZoom.NORMAL,
    onZoomChange: (NivelZoom) -> Unit = {},
    searchQuery: String = "",
    emptyMessage: String = "No hay elementos disponibles",
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 24.dp,
    horizontalSpacing: Dp = 16.dp,
    itemContent: @Composable (T) -> Unit,
) {

    // 📐 Espaciado dinámico usando configuración centralizada
    val factorEspaciado = LibraryZoomConfig.factorEspaciado(nivelZoom)

    val dynamicVerticalSpacing by animateDpAsState(
        targetValue = verticalSpacing * factorEspaciado,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "vertical_spacing",
    )

    val dynamicHorizontalSpacing by animateDpAsState(
        targetValue = horizontalSpacing * factorEspaciado,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "horizontal_spacing",
    )

    // Estado para detectar un solo gesto completo
    var escalaAcumulada by remember { mutableFloatStateOf(1f) }
    var gestoProcesado by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier.fillMaxSize().pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    escalaAcumulada = 1f
                    gestoProcesado = false

                    do {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }

                        if (pointers.size == 2 && !gestoProcesado) {
                            val pointer1 = pointers[0]
                            val pointer2 = pointers[1]

                            val currentDistance =
                                calculateDistance(pointer1.position, pointer2.position)

                            val previousDistance =
                                calculateDistance(
                                    pointer1.previousPosition,
                                    pointer2.previousPosition,
                                )

                            if (previousDistance > 0f) {
                                val scale = currentDistance / previousDistance
                                escalaAcumulada *= scale

                                when {
                                    escalaAcumulada > 1.5f &&
                                        nivelZoom != NivelZoom.GRANDE &&
                                        !gestoProcesado -> {
                                        onZoomChange(nivelZoom.siguiente())
                                        gestoProcesado = true
                                        pointer1.consume()
                                        pointer2.consume()
                                    }
                                    escalaAcumulada < 0.67f &&
                                        nivelZoom != NivelZoom.PEQUENO &&
                                        !gestoProcesado -> {
                                        onZoomChange(nivelZoom.anterior())
                                        gestoProcesado = true
                                        pointer1.consume()
                                        pointer2.consume()
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        when {
            items.isEmpty() && searchQuery.isNotBlank() -> {
                EnhancedEmptySearchState(query = searchQuery)
            }

            items.isEmpty() -> {
                EnhancedEmptyGridState(message = emptyMessage)
            }

            else -> {
                // 📐 Tamaño de item usando configuración centralizada
                val itemSizeAjustado by animateDpAsState(
                    targetValue = minItemSize * LibraryZoomConfig.factorEscala(nivelZoom),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "item_size",
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = itemSizeAjustado),
                    state = gridState,
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dynamicVerticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(dynamicHorizontalSpacing),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = items, key = { it.id }, contentType = { it::class }) { item ->
                        val itemIndex = remember { items.indexOf(item) }

                        EnhancedGridItem(index = itemIndex, content = { itemContent(item) })
                    }
                }
            }
        }
    }
}

/** 🎭 Item mejorado con animaciones (SIN scaling) */
@Composable
private fun EnhancedGridItem(index: Int, content: @Composable () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 30L)
        isVisible = true
    }

    val alpha by
        animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "item_alpha",
        )

    val offsetY by
        animateDpAsState(
            targetValue = if (isVisible) 0.dp else 20.dp,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "item_offset",
        )

    Box(
        modifier =
            Modifier.graphicsLayer {
                this.alpha = alpha
                translationY = offsetY.toPx()
            }
    ) {
        content()
    }
}

@Composable
private fun EnhancedEmptySearchState(query: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_pulse")
    val scale by
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "icon_scale",
        )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = 0.4f),
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            )
            Text(
                text = "No se encontraron resultados",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "\"$query\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64B5F6),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EnhancedEmptyGridState(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_float")
    val offsetY by
        infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "float_offset",
        )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(48.dp),
        ) {
            Text(
                text = "📂",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.graphicsLayer { translationY = offsetY },
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx * dx + dy * dy)
}

// ==================== PREVIEWS & FAKE DATA ====================

/**
 * 🦴 Mock de LibraryItem para Previews ✅ Ahora implementa TODOS los campos requeridos y funciona si
 * quitas 'sealed' de la interfaz
 */
private data class DemoItemGrid(
    override val id: Int,
    override val displayTitle: String,
    override val displaySubtitle: String? = null,
    override val imageUrl: String? = null,
    val color: Long, // Custom for demo visual
) : LibraryItem

/** 🛠️ Provider de datos para probar el Grid */
private class GridPreviewProvider : PreviewParameterProvider<List<DemoItemGrid>> {
    override val values =
        sequenceOf(
            // Caso 1: Lista poblada
            List(10) { i ->
                DemoItemGrid(
                    id = i,
                    displayTitle = "Item $i",
                    displaySubtitle = "Subtítulo $i",
                    color = if (i % 2 == 0) 0xFFBB86FC else 0xFF03DAC5,
                )
            },
            // Caso 2: Lista vacía
            emptyList(),
        )
}

@Preview(name = "📱 Grid Normal (Poblado)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewLibraryPopulated(
    @PreviewParameter(GridPreviewProvider::class) items: List<DemoItemGrid>
) {
    val data = items.ifEmpty { GridPreviewProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        LibraryGridLayout(
            items = data,
            gridState = rememberLazyGridState(),
            nivelZoom = NivelZoom.NORMAL,
            onZoomChange = {},
            modifier = Modifier.fillMaxSize(),
        ) { item ->
            // Renderizado simulado
            Box(
                modifier =
                    Modifier.aspectRatio(1f)
                        .background(Color(item.color), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.displayTitle,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview(name = "🔍 Búsqueda Vacía")
@Composable
private fun PreviewSearchEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        LibraryGridLayout<DemoItemGrid>(
            items = emptyList(),
            gridState = rememberLazyGridState(),
            searchQuery = "Metallica",
            modifier = Modifier.fillMaxSize(),
            itemContent = {},
        )
    }
}
