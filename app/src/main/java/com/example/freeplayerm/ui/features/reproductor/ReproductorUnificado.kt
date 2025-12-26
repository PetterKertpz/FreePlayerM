package com.example.freeplayerm.ui.features.reproductor

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.components.PanelExpandido
import com.example.freeplayerm.ui.features.reproductor.components.PanelMinimizado
import com.example.freeplayerm.ui.features.reproductor.components.PanelNormal
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * ⚡ REPRODUCTOR UNIFICADO - v3.0
 *
 * Orquestador principal de los 3 modos del reproductor:
 *
 * 📱 MINIMIZADO (15%)
 *    - Aparece durante scroll activo en listas
 *    - Muestra: título + artista + progreso
 *    - Click para volver a NORMAL
 *
 * 🎵 NORMAL (25-30%)
 *    - Estado por defecto mientras reproduce
 *    - Muestra: vinilo + info + controles + slider
 *    - Click en vinilo/título para EXPANDIDO
 *    - Se minimiza automáticamente durante scroll
 *
 * 📺 EXPANDIDO (100%)
 *    - Pantalla completa
 *    - Muestra: todo + tabs (Letra/Info/Enlaces)
 *    - Swipe down o botón para NORMAL
 *
 * Gestos:
 * - Swipe up: Expandir
 * - Swipe down: Colapsar
 *
 * @param estado Estado completo del reproductor
 * @param onEvento Callback unificado para todos los eventos
 * @param modifier Modificador opcional
 */
@Composable
fun ReproductorUnificado(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    // Si no hay canción, no mostrar nada
    val cancion = estado.cancionActual ?: return

    val haptic = LocalHapticFeedback.current

    // Usar el modo efectivo (considera scroll)
    val modoActual = estado.modoPanelEfectivo

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(modoActual) {
                detectVerticalDragGestures(
                    onDragEnd = {},
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()

                        // Swipe up -> expandir (solo desde NORMAL)
                        if (dragAmount < -50 && modoActual == ModoPanelReproductor.NORMAL) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvento(ReproductorEvento.Panel.Expandir)
                        }
                        // Swipe down -> colapsar (solo desde EXPANDIDO)
                        else if (dragAmount > 50 && modoActual == ModoPanelReproductor.EXPANDIDO) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvento(ReproductorEvento.Panel.Colapsar)
                        }
                    }
                )
            }
    ) {
        // Transición animada entre modos
        AnimatedContent(
            targetState = modoActual,
            transitionSpec = { crearTransicionModo(initialState, targetState) },
            label = "modoPanel"
        ) { modo ->
            when (modo) {
                ModoPanelReproductor.MINIMIZADO -> {
                    PanelMinimizado(
                        cancion = cancion,
                        estado = estado,
                        onExpandir = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Desde MINIMIZADO volver a NORMAL
                            onEvento(ReproductorEvento.Panel.CambiarModo(ModoPanelReproductor.NORMAL))
                        }
                    )
                }

                ModoPanelReproductor.NORMAL -> {
                    PanelNormal(
                        cancion = cancion,
                        estado = estado,
                        onEvento = onEvento,
                        onExpandir = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvento(ReproductorEvento.Panel.Expandir)
                        }
                    )
                }

                ModoPanelReproductor.EXPANDIDO -> {
                    PanelExpandido(
                        cancion = cancion,
                        estado = estado,
                        onEvento = onEvento,
                        onColapsar = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvento(ReproductorEvento.Panel.Colapsar)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Crea la especificación de transición según los modos de origen y destino
 */
@OptIn(ExperimentalAnimationApi::class)
private fun crearTransicionModo(
    desde: ModoPanelReproductor,
    hacia: ModoPanelReproductor
): ContentTransform {
    return when (// MINIMIZADO -> NORMAL: Expandir desde abajo
        desde) {
        ModoPanelReproductor.MINIMIZADO if hacia == ModoPanelReproductor.NORMAL -> {
            (fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Bottom))
                .togetherWith(fadeOut(tween(100)))
        }

        // NORMAL -> MINIMIZADO: Contraer hacia abajo
        ModoPanelReproductor.NORMAL if hacia == ModoPanelReproductor.MINIMIZADO -> {
            (fadeIn(tween(150)) + slideInVertically { -it })
                .togetherWith(fadeOut(tween(100)) + shrinkVertically(shrinkTowards = Alignment.Bottom))
        }

        // NORMAL -> EXPANDIDO: Deslizar hacia arriba
        ModoPanelReproductor.NORMAL if hacia == ModoPanelReproductor.EXPANDIDO -> {
            (fadeIn(tween(300)) + slideInVertically { it / 4 })
                .togetherWith(fadeOut(tween(150)))
        }

        // EXPANDIDO -> NORMAL: Deslizar hacia abajo
        ModoPanelReproductor.EXPANDIDO if hacia == ModoPanelReproductor.NORMAL -> {
            fadeIn(tween(150))
                .togetherWith(fadeOut(tween(300)) + slideOutVertically { it / 4 })
        }

        // Cualquier otra transición: fade simple
        else -> {
            fadeIn(tween(200)).togetherWith(fadeOut(tween(200)))
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(name = "Panel Minimizado", showBackground = true)
@Composable
private fun PreviewPanelMinimizado() {
    FreePlayerMTheme {
        ReproductorUnificado(
            estado = crearEstadoDemo().copy(
                modoPanel = ModoPanelReproductor.MINIMIZADO
            ),
            onEvento = {}
        )
    }
}

@Preview(name = "Panel Normal", showBackground = true)
@Composable
private fun PreviewPanelNormal() {
    FreePlayerMTheme {
        ReproductorUnificado(
            estado = crearEstadoDemo().copy(
                modoPanel = ModoPanelReproductor.NORMAL
            ),
            onEvento = {}
        )
    }
}

@Preview(
    name = "Panel Expandido",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PreviewPanelExpandido() {
    FreePlayerMTheme {
        ReproductorUnificado(
            estado = crearEstadoDemo().copy(
                modoPanel = ModoPanelReproductor.EXPANDIDO,
                letra = "Esta es la letra de ejemplo...\n\nVerso 1\nLínea 2\nLínea 3",
                infoArtista = "Queen es una banda británica de rock formada en 1970...",
                cargandoLetra = false,
                cargandoInfo = false
            ),
            onEvento = {}
        )
    }
}

@Preview(
    name = "Modo Aleatorio Activo",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewModoAleatorio() {
    FreePlayerMTheme {
        ReproductorUnificado(
            estado = crearEstadoDemo().copy(
                modoReproduccion = ModoReproduccion.ALEATORIO,
                modoRepeticion = ModoRepeticion.REPETIR_LISTA
            ),
            onEvento = {}
        )
    }
}

// Helper para crear estado de demo
private fun crearEstadoDemo(): ReproductorEstado {
    val cancionDemo = CancionConArtista(
        cancion = CancionEntity(
            idCancion = 1,
            titulo = "Bohemian Rhapsody",
            idArtista = 1,
            idAlbum = 1,
            idGenero = 1,
            duracionSegundos = 355,
            origen = "LOCAL",
            archivoPath = "/music/queen/bohemian.mp3"
        ),
        artistaNombre = "Queen",
        albumNombre = "A Night at the Opera",
        generoNombre = "Rock",
        esFavorita = true,
        portadaPath = null,
        fechaLanzamiento = "1975"
    )

    return ReproductorEstado(
        cancionActual = cancionDemo,
        estaReproduciendo = true,
        progresoActualMs = 125000L,
        esFavorita = true,
        modoPanel = ModoPanelReproductor.NORMAL
    )
}