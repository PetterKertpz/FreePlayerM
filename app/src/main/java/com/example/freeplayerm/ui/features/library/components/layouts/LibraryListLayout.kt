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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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


@Composable
fun <T : LibraryItem> LibraryListLayout(
   items: List<T>,
   listState: LazyListState,
   baseItemHeight: Dp = 72.dp,
   nivelZoom: NivelZoom = NivelZoom.NORMAL,
   onZoomChange: (NivelZoom) -> Unit = {},
   searchQuery: String = "",
   emptyMessage: String = "No hay elementos disponibles",
   modifier: Modifier = Modifier,
   contentPadding: PaddingValues = PaddingValues(bottom = 100.dp),
   itemSpacing: Dp = 8.dp,
   itemContent: @Composable (T) -> Unit,
) {
   // 📐 Configuración dinámica centralizada
   val factorEscala = LibraryZoomConfig.factorEscalaLista(nivelZoom)
   val factorEspaciado = LibraryZoomConfig.factorEspaciado(nivelZoom)
   
   val dynamicItemSpacing by animateDpAsState(
      targetValue = itemSpacing * factorEspaciado,
      animationSpec = tween(300, easing = FastOutSlowInEasing),
      label = "item_spacing",
   )
   
   val itemHeightAjustado by animateDpAsState(
      targetValue = baseItemHeight * factorEscala,
      animationSpec = spring(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessLow,
      ),
      label = "item_height",
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
                EnhancedEmptySearchStateList(query = searchQuery)
            }

            items.isEmpty() -> {
                EnhancedEmptyListState(message = emptyMessage)
            }

            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dynamicItemSpacing),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = items, key = { it.id }, contentType = { it::class }) { item ->
                        val itemIndex = remember { items.indexOf(item) }

                        EnhancedListItem(
                            index = itemIndex,
                            itemHeight = itemHeightAjustado,
                            content = { itemContent(item) },
                        )
                    }
                }
            }
        }
    }
}

/** 🎭 Item mejorado con animaciones en cascada */
@Composable
private fun EnhancedListItem(
   index: Int,
   itemHeight: Dp,
   content: @Composable () -> Unit
) {
   var isVisible by remember { mutableStateOf(false) }
   
   LaunchedEffect(Unit) {
      kotlinx.coroutines.delay(index * 25L)
      isVisible = true
   }
   
   val alpha by animateFloatAsState(
      targetValue = if (isVisible) 1f else 0f,
      animationSpec = tween(250, easing = FastOutSlowInEasing),
      label = "item_alpha",
   )
   
   val offsetX by animateDpAsState(
      targetValue = if (isVisible) 0.dp else (-30).dp,
      animationSpec = spring(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessMedium,
      ),
      label = "item_offset",
   )
   
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .height(itemHeight)
         .graphicsLayer {
            this.alpha = alpha
            translationX = offsetX.toPx()
         },
      contentAlignment = Alignment.CenterStart,
   ) {
      content()
   }
}

/** 🔍 Estado vacío de búsqueda mejorado */
@Composable
private fun EnhancedEmptySearchStateList(query: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_pulse")

    val rotation by
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "icon_rotation",
        )

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
                        rotationZ = rotation
                    },
            )
            Text(
                text = "No se encontraron resultados",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Box(
                modifier =
                    Modifier.padding(horizontal = 16.dp).graphicsLayer { shadowElevation = 8f }
            ) {
                Text(
                    text = "\"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Intenta con otras palabras clave",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 📭 Estado vacío mejorado con animación flotante */
@Composable
private fun EnhancedEmptyListState(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_float")

    val offsetY by
        infiniteTransition.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "float_offset",
        )

    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.5f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "icon_alpha",
        )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(48.dp),
        ) {
            Text(
                text = "📭",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = alpha),
                modifier = Modifier.graphicsLayer { translationY = offsetY },
            )

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tu biblioteca está esperando ser llenada",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
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
 * 🦴 Mock de LibraryItem para Previews Implementa TODOS los campos requeridos por la interfaz
 * LibraryItem
 */
private data class DemoItemList(
    override val id: Int,
    override val displayTitle: String,
    override val displaySubtitle: String? = null,
    override val imageUrl: String? = null,
    val color: Long, // Campo extra para visualización en preview
) : LibraryItem

/** 🛠️ Provider de datos para probar la Lista */
private class ListPreviewProvider : PreviewParameterProvider<List<DemoItemList>> {
    override val values =
        sequenceOf(
            // Caso 1: Lista poblada
            List(10) { i ->
                DemoItemList(
                    id = i,
                    displayTitle = "Canción Número $i",
                    displaySubtitle = "Artista Desconocido • Álbum $i",
                    color = if (i % 2 == 0) 0xFFBB86FC else 0xFF03DAC5,
                )
            },
            // Caso 2: Lista vacía
            emptyList(),
        )
}

@Preview(name = "📜 Lista Normal (Poblada)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewListPopulated(
    @PreviewParameter(ListPreviewProvider::class) items: List<DemoItemList>
) {
    val data = items.ifEmpty { ListPreviewProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        LibraryListLayout(
            items = data,
            listState = rememberLazyListState(),
            nivelZoom = NivelZoom.NORMAL,
            onZoomChange = {},
            modifier = Modifier.fillMaxSize(),
        ) { item ->
            // Diseño simulado de fila
            Row(
                modifier =
                    Modifier.fillMaxSize()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Placeholder de portada
                Box(
                    modifier =
                        Modifier.size(48.dp).background(Color(item.color), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.displayTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(text = item.displaySubtitle ?: "", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(name = "🔍 Búsqueda Sin Resultados", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewListSearchEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        LibraryListLayout<DemoItemList>(
            items = emptyList(),
            listState = rememberLazyListState(),
            searchQuery = "Nirvana",
            modifier = Modifier.fillMaxSize(),
            itemContent = {},
        )
    }
}

@Preview(name = "📭 Lista Vacía", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewListEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        LibraryListLayout<DemoItemList>(
            items = emptyList(),
            listState = rememberLazyListState(),
            searchQuery = "",
            emptyMessage = "No hay canciones guardadas",
            modifier = Modifier.fillMaxSize(),
            itemContent = {},
        )
    }
}
