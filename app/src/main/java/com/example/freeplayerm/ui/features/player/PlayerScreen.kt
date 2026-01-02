package com.example.freeplayerm.ui.features.player

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.gesture.PlayerGestureConfig
import com.example.freeplayerm.ui.features.player.gesture.lerp
import com.example.freeplayerm.ui.features.player.gesture.playerGestures
import com.example.freeplayerm.ui.features.player.gesture.rememberPlayerGestureController
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.launch
@Composable
fun PlayerScreen(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Early return si no hay canción
    val cancion = estado.cancionActual ?: return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenHeightDp = maxHeight
        val screenHeightPx = with(LocalDensity.current) { screenHeightDp.toPx() }
        val scope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current

        // CONTROLADOR DE GESTOS (Estado unificado)
        val gestureController =
            rememberPlayerGestureController(
                screenHeightPx = screenHeightPx,
                initialMode = estado.modoPanel,
                config = PlayerGestureConfig.Default,
            )

        LaunchedEffect(estado.modoPanel) {
            // Solo animar si el modo del controller difiere del estado
            if (
                gestureController.state.currentMode != estado.modoPanel &&
                    !gestureController.state.isDragging &&
                    !gestureController.state.isAnimating
            ) {

                gestureController.animateToMode(
                    mode = estado.modoPanel,
                    hapticFeedback = null, // Sin haptic en sincronización automática
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // VALORES INTERPOLADOS (Con caché)
        // ═══════════════════════════════════════════════════════════════════

        val interpolatedValues by
            remember(screenHeightDp) {
                derivedStateOf { gestureController.getInterpolatedValues(screenHeightDp) }
            }

        // Estado derivado para UI
        val isDragging by remember { derivedStateOf { gestureController.state.isDragging } }

        val progresoActual by remember { derivedStateOf { gestureController.progress.value } }

        // ═══════════════════════════════════════════════════════════════════
        // CONTENEDOR CON GESTOS
        // ═══════════════════════════════════════════════════════════════════

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(interpolatedValues.panelHeightDp)
                    .playerGestures(
                        controller = gestureController,
                        enabled = true,
                        onModeChanged = { nuevoModo ->
                            onEvento(ReproductorEvento.Panel.CambiarModo(nuevoModo))
                        },
                    ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            InterpolatedPlayerContent(
                cancion = cancion,
                estado = estado,
                interpolatedValues = interpolatedValues,
                progresoGlobal = progresoActual,
                puntoNormal = gestureController.puntoNormal,
                isDragging = isDragging,
                onEvento = onEvento,
                onExpandir = {
                    scope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        gestureController.animateToMode(
                            mode = ModoPanelReproductor.EXPANDIDO,
                            hapticFeedback = haptic,
                        )
                        onEvento(ReproductorEvento.Panel.Expandir)
                    }
                },
                onColapsar = {
                    scope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        gestureController.animateToMode(
                            mode = ModoPanelReproductor.NORMAL,
                            hapticFeedback = haptic,
                        )
                        onEvento(ReproductorEvento.Panel.Colapsar)
                    }
                },
            )
        }

    }
}

// ═══════════════════════════════════════════════════════════════════════════
// COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Fondo con gradiente interpolado según el progreso de expansión. Usa tokens del sistema de diseño
 * para consistencia visual.
 *
 * @param intensidad Valor de 0 (minimizado/normal) a 1 (expandido)
 */
@Composable
private fun FondoInterpolado(intensidad: Float, modifier: Modifier = Modifier) {
    // Colores base del tema
    val colorBaseInicio = AppColors.ElectricViolet.v14
    val colorBaseMedio = AppColors.ElectricViolet.v11

    // Colores expandido (más profundos)
    val colorExpandidoInicio = AppColors.ElectricViolet.v15
    val colorExpandidoMedio = AppColors.ElectricViolet.v16
    val colorExpandidoFin = AppColors.Negro

    // Calcular colores finales con derivedStateOf para optimizar recomposiciones
    val gradientColors by
        remember(intensidad) {
            derivedStateOf {
                val alphaBase = lerp(0.95f, 1f, intensidad)
                val alphaExpandido = (intensidad)

                if (intensidad > 0.3f) {
                    listOf(
                        colorExpandidoInicio.copy(alpha = alphaExpandido),
                        colorExpandidoMedio.copy(alpha = alphaExpandido),
                        colorExpandidoFin.copy(alpha = alphaExpandido),
                    )
                } else {
                    listOf(
                        colorBaseInicio.copy(alpha = alphaBase),
                        colorBaseMedio.copy(alpha = alphaBase),
                    )
                }
            }
        }

    Box(modifier = modifier.background(Brush.verticalGradient(colors = gradientColors)))
}

// ═══════════════════════════════════════════════════════════════════════════
// PREVIEWS EXHAUSTIVAS
// ═══════════════════════════════════════════════════════════════════════════

// ───────────────────────────────────────────────────────────────────────────
// Preview Data Provider
// ───────────────────────────────────────────────────────────────────────────

@Immutable
private data class PlayerPreviewData(
    val nombre: String,
    val estado: PlayerState,
    val descripcion: String = "",
)

private class PlayerStateProvider : PreviewParameterProvider<PlayerPreviewData> {

    private val cancionDemo =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 1,
                    titulo = "Bohemian Rhapsody",
                    idArtista = 1,
                    idAlbum = 1,
                    idGenero = 1,
                    duracionSegundos = 355,
                    origen = "LOCAL",
                    archivoPath = "/music/queen/bohemian.mp3",
                ),
            artistaNombre = "Queen",
            albumNombre = "A Night at the Opera",
            generoNombre = "Rock",
            esFavorita = true,
            portadaPath = null,
            fechaLanzamiento = "1975",
        )

    private val cancionLarga =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 2,
                    titulo = "November Rain - Remastered 2022 Extended Version",
                    idArtista = 2,
                    idAlbum = 2,
                    idGenero = 1,
                    duracionSegundos = 537,
                    origen = "LOCAL",
                    archivoPath = "/music/gnr/november_rain.mp3",
                ),
            artistaNombre = "Guns N' Roses featuring Orchestra",
            albumNombre = "Use Your Illusion I (Super Deluxe)",
            generoNombre = "Rock",
            esFavorita = false,
            portadaPath = null,
            fechaLanzamiento = "1991",
        )

    private val estadoBase =
        PlayerState(
            cancionActual = cancionDemo,
            estaReproduciendo = true,
            progresoActualMs = 125000L,
            esFavorita = true,
            modoPanel = ModoPanelReproductor.NORMAL,
        )

    override val values =
        sequenceOf(
            // Estados por modo de panel
            PlayerPreviewData(
                nombre = "Minimizado",
                estado =
                    estadoBase.copy(
                        modoPanel = ModoPanelReproductor.MINIMIZADO,
                        progresoActualMs = 60000L,
                    ),
                descripcion = "Panel colapsado durante scroll",
            ),
            PlayerPreviewData(
                nombre = "Normal - Reproduciendo",
                estado =
                    estadoBase.copy(
                        modoPanel = ModoPanelReproductor.NORMAL,
                        estaReproduciendo = true,
                    ),
                descripcion = "Estado por defecto mientras reproduce",
            ),
            PlayerPreviewData(
                nombre = "Normal - Pausado",
                estado =
                    estadoBase.copy(
                        modoPanel = ModoPanelReproductor.NORMAL,
                        estaReproduciendo = false,
                        progresoActualMs = 180000L,
                    ),
                descripcion = "Reproducción pausada",
            ),
            PlayerPreviewData(
                nombre = "Expandido",
                estado =
                    estadoBase.copy(
                        modoPanel = ModoPanelReproductor.EXPANDIDO,
                        letra =
                            "Is this the real life?\nIs this just fantasy?\nCaught in a landslide...",
                    ),
                descripcion = "Pantalla completa con letra",
            ),
            // Estados especiales
            PlayerPreviewData(
                nombre = "Título Largo",
                estado =
                    estadoBase.copy(
                        cancionActual = cancionLarga,
                        modoPanel = ModoPanelReproductor.NORMAL,
                    ),
                descripcion = "Prueba de texto con marquee",
            ),
            PlayerPreviewData(
                nombre = "Sin Favorito",
                estado =
                    estadoBase.copy(esFavorita = false, modoPanel = ModoPanelReproductor.EXPANDIDO),
                descripcion = "Canción no marcada como favorita",
            ),
            PlayerPreviewData(
                nombre = "Inicio de Canción",
                estado =
                    estadoBase.copy(progresoActualMs = 0L, modoPanel = ModoPanelReproductor.NORMAL),
                descripcion = "Progreso en 0",
            ),
            PlayerPreviewData(
                nombre = "Final de Canción",
                estado =
                    estadoBase.copy(
                        progresoActualMs = 350000L,
                        modoPanel = ModoPanelReproductor.NORMAL,
                    ),
                descripcion = "Progreso casi completo",
            ),
        )
}

// ───────────────────────────────────────────────────────────────────────────
// Preview: Dark Mode
// ───────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🌙 Dark Mode",
    group = "Estados",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 380,
    heightDp = 850,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewReproductorDark(
    @PreviewParameter(PlayerStateProvider::class) data: PlayerPreviewData
) {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.ElectricViolet.v16)) {
            // Label del estado
            Text(
                text = "${data.nombre}\n${data.descripcion}",
                color = AppColors.ElectricViolet.v3,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            )

            // Reproductor
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                PlayerScreen(estado = data.estado, onEvento = {})
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Preview: Light Mode
// ───────────────────────────────────────────────────────────────────────────

@Preview(
    name = "☀️ Light Mode",
    group = "Estados",
    showBackground = true,
    backgroundColor = 0xFFF3EEFF,
    widthDp = 380,
    heightDp = 850,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PreviewReproductorLight(
    @PreviewParameter(PlayerStateProvider::class) data: PlayerPreviewData
) {
    FreePlayerMTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.ElectricViolet.v1)) {
            Text(
                text = "${data.nombre}\n${data.descripcion}",
                color = AppColors.ElectricViolet.v12,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            )

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                PlayerScreen(estado = data.estado, onEvento = {})
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Preview: Comparación de Modos
// ───────────────────────────────────────────────────────────────────────────

@Preview(
    name = "📊 Comparación Modos",
    group = "Comparación",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 400,
    heightDp = 900,
)
@Composable
private fun PreviewComparacionModos() {
    val cancion = crearCancionDemo()

    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxSize().background(AppColors.Negro).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Mini
            PreviewModoCard(titulo = "MINIMIZADO (72dp)", altura = 72.dp) {
                PlayerScreen(
                    estado =
                        PlayerState(
                            cancionActual = cancion,
                            estaReproduciendo = true,
                            progresoActualMs = 60000L,
                            modoPanel = ModoPanelReproductor.MINIMIZADO,
                        ),
                    onEvento = {},
                )
            }

            // Normal
            PreviewModoCard(titulo = "NORMAL (140dp)", altura = 140.dp) {
                PlayerScreen(
                    estado =
                        PlayerState(
                            cancionActual = cancion,
                            estaReproduciendo = true,
                            progresoActualMs = 125000L,
                            modoPanel = ModoPanelReproductor.NORMAL,
                        ),
                    onEvento = {},
                )
            }

            // Expandido (parcial para preview)
            PreviewModoCard(titulo = "EXPANDIDO (100%)", altura = 500.dp) {
                PlayerScreen(
                    estado =
                        PlayerState(
                            cancionActual = cancion,
                            estaReproduciendo = true,
                            progresoActualMs = 200000L,
                            modoPanel = ModoPanelReproductor.EXPANDIDO,
                            letra = "Is this the real life?\nIs this just fantasy?",
                        ),
                    onEvento = {},
                )
            }
        }
    }
}

@Composable
private fun PreviewModoCard(titulo: String, altura: Dp, content: @Composable () -> Unit) {
    Column {
        Text(
            text = titulo,
            color = AppColors.ElectricViolet.v4,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(altura)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.ElectricViolet.v15)
        ) {
            content()
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Preview: Tablet
// ───────────────────────────────────────────────────────────────────────────

@Preview(
    name = "💻 Tablet Landscape",
    group = "Dispositivos",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 840,
    heightDp = 600,
    device = "spec:width=840dp,height=600dp,dpi=240",
)
@Composable
private fun PreviewReproductorTablet() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.ElectricViolet.v16)) {
            PlayerScreen(
                estado =
                    PlayerState(
                        cancionActual = crearCancionDemo(),
                        estaReproduciendo = true,
                        progresoActualMs = 180000L,
                        modoPanel = ModoPanelReproductor.EXPANDIDO,
                        letra =
                            "Is this the real life?\nIs this just fantasy?\nCaught in a landslide,\nNo escape from reality.",
                    ),
                onEvento = {},
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Preview: Accesibilidad
// ───────────────────────────────────────────────────────────────────────────

@Preview(
    name = "♿ Font Scale 1.5x",
    group = "Accesibilidad",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 380,
    heightDp = 200,
    fontScale = 1.5f,
)
@Composable
private fun PreviewReproductorFontScale() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.ElectricViolet.v16)) {
            PlayerScreen(
                estado =
                    PlayerState(
                        cancionActual = crearCancionDemo(),
                        estaReproduciendo = true,
                        progresoActualMs = 125000L,
                        modoPanel = ModoPanelReproductor.NORMAL,
                    ),
                onEvento = {},
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Helpers
// ───────────────────────────────────────────────────────────────────────────

private fun crearCancionDemo(): SongWithArtist {
    return SongWithArtist(
        cancion =
            SongEntity(
                idCancion = 1,
                titulo = "Bohemian Rhapsody",
                idArtista = 1,
                idAlbum = 1,
                idGenero = 1,
                duracionSegundos = 355,
                origen = "LOCAL",
                archivoPath = "/music/queen/bohemian.mp3",
            ),
        artistaNombre = "Queen",
        albumNombre = "A Night at the Opera",
        generoNombre = "Rock",
        esFavorita = true,
        portadaPath = null,
        fechaLanzamiento = "1975",
    )
}

private fun crearEstadoDemo(): PlayerState {
    return PlayerState(
        cancionActual = crearCancionDemo(),
        estaReproduciendo = true,
        progresoActualMs = 125000L,
        esFavorita = true,
        modoPanel = ModoPanelReproductor.NORMAL,
    )
}
