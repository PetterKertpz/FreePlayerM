package com.example.freeplayerm.ui.features.player.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.PlayerState
import com.example.freeplayerm.ui.features.player.ReproductorEvento
import com.example.freeplayerm.ui.features.player.formatearTiempo
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.example.freeplayerm.ui.theme.FreePlayerTheme
import com.example.freeplayerm.ui.theme.FreePlayerTypography
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════════════════
// 🎚️ PROGRESS SLIDER — VERSIÓN EXPANDIDA (PREMIUM)
// ═══════════════════════════════════════════════════════════════════════════════
//
// Características:
// ✅ Thumb con efecto glow animado
// ✅ Track con gradiente y altura dinámica
// ✅ Tooltip flotante que sigue al thumb
// ✅ Área táctil expandida (48dp) para mejor UX móvil
// ✅ Animaciones spring suaves
// ✅ Soporte para buffer progress
// ✅ Accesibilidad completa (TalkBack)
// ✅ Integración con sistema de tema
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────────
// TOKENS DE DISEÑO — Dimensiones (invariantes)
// ─────────────────────────────────────────────────────────────────────────────────

@Stable
private object SliderDimens {
    val TrackHeightNormal = 6.dp
    val TrackHeightActive = 8.dp
    val ThumbSize = 18.dp
    val ThumbTouchTarget = 48.dp
    val GlowRadius = 12.dp
    val TooltipWidth = 56.dp
    val TooltipHeight = 32.dp
    val TooltipCornerRadius = 8.dp

    const val SPRING_DAMPING = 0.7f
    const val SPRING_STIFFNESS = Spring.StiffnessMedium
}

@Stable
private object CompactSliderDimens {
    val TrackHeight = 4.dp
    val TrackHeightActive = 5.dp
    val ThumbSize = 12.dp
    val ThumbSizeActive = 14.dp
    val ThumbTouchTarget = 44.dp
    val TimeWidth = 42.dp
}

// ─────────────────────────────────────────────────────────────────────────────────
// COLORES DINÁMICOS — Se adaptan al tema actual
// ─────────────────────────────────────────────────────────────────────────────────

@Stable
private data class SliderColors(
    // Track
    val trackActiveStart: Color,
    val trackActiveEnd: Color,
    val trackInactive: Color,
    val trackBuffer: Color,
    // Thumb
    val thumbColor: Color,
    val thumbGlow: Color,
    val thumbGlowActive: Color,
    // Tooltip
    val tooltipBackground: Color,
    val tooltipText: Color,
    // Tiempo
    val timeTextNormal: Color,
    val timeTextActive: Color,
)

@Composable
private fun sliderColors(): SliderColors {
    val scheme = MaterialTheme.colorScheme
    val extended = FreePlayerTheme.extendedColors

    return remember(scheme, extended) {
        SliderColors(
            trackActiveStart = AppColors.ElectricViolet.v5,
            trackActiveEnd = scheme.primary,
            trackInactive = scheme.onSurface.copy(alpha = 0.15f),
            trackBuffer = scheme.onSurface.copy(alpha = 0.25f),
            thumbColor = AppColors.Blanco,
            thumbGlow = scheme.primary.copy(alpha = 0.6f),
            thumbGlowActive = AppColors.ElectricViolet.v5.copy(alpha = 0.8f),
            tooltipBackground = scheme.primary,
            tooltipText = scheme.onPrimary,
            timeTextNormal = scheme.onSurface.copy(alpha = 0.6f),
            timeTextActive = scheme.onSurface.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun compactSliderColors(): SliderColors {
    val scheme = MaterialTheme.colorScheme

    return remember(scheme) {
        SliderColors(
            trackActiveStart = scheme.primary,
            trackActiveEnd = scheme.primary,
            trackInactive = scheme.onSurface.copy(alpha = 0.12f),
            trackBuffer = scheme.onSurface.copy(alpha = 0.20f),
            thumbColor = AppColors.Blanco,
            thumbGlow = scheme.primary.copy(alpha = 0.3f),
            thumbGlowActive = scheme.primary.copy(alpha = 0.5f),
            tooltipBackground = scheme.primary,
            tooltipText = scheme.onPrimary,
            timeTextNormal = scheme.onSurface.copy(alpha = 0.5f),
            timeTextActive = scheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// COMPONENTE PRINCIPAL — SLIDER EXPANDIDO
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * 🎚️ Slider de Progreso Premium
 *
 * Versión completa para modo expandido del reproductor. Incluye tiempos, tooltip flotante y
 * feedback visual rico.
 *
 * @param estado Estado actual del reproductor
 * @param onEvento Callback para eventos de navegación
 * @param modifier Modifier externo
 * @param bufferProgress Progreso del buffer (0f-1f)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSlider(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
    bufferProgress: Float = 1f,
) {
    val colors = sliderColors()
    val duracionTotal = estado.duracionTotalMs
    val interactionSource = remember { MutableInteractionSource() }

    // Estados de interacción
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged || estado.isScrubbing

    // Valor del slider con remember por canción
    var sliderValue by
        remember(estado.cancionActual) { mutableFloatStateOf(estado.progresoVisibleMs.toFloat()) }

    // Sincronizar con estado real cuando no hay scrubbing
    LaunchedEffect(estado.progresoVisibleMs, estado.isScrubbing) {
        if (!estado.isScrubbing) {
            sliderValue = estado.progresoVisibleMs.toFloat()
        }
    }

    // Progreso normalizado (0f - 1f)
    val progress by
        remember(sliderValue, duracionTotal) {
            derivedStateOf {
                if (duracionTotal > 0) (sliderValue / duracionTotal).coerceIn(0f, 1f) else 0f
            }
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES
    // ═══════════════════════════════════════════════════════════════════════════

    val thumbScale by
        animateFloatAsState(
            targetValue = if (isInteracting) 1.3f else 1f,
            animationSpec =
                spring(
                    dampingRatio = SliderDimens.SPRING_DAMPING,
                    stiffness = SliderDimens.SPRING_STIFFNESS,
                ),
            label = "thumbScale",
        )

    val trackHeight by
        animateDpAsState(
            targetValue =
                if (isInteracting) SliderDimens.TrackHeightActive
                else SliderDimens.TrackHeightNormal,
            animationSpec =
                spring(
                    dampingRatio = SliderDimens.SPRING_DAMPING,
                    stiffness = SliderDimens.SPRING_STIFFNESS,
                ),
            label = "trackHeight",
        )

    val glowAlpha by
        animateFloatAsState(
            targetValue = if (isInteracting) 1f else 0.5f,
            animationSpec = tween(200),
            label = "glowAlpha",
        )

    // Texto de accesibilidad
    val tiempoActual = sliderValue.toLong().formatearTiempo()
    val tiempoTotal = duracionTotal.formatearTiempo()
    val accessibilityDescription = "Progreso de reproducción: $tiempoActual de $tiempoTotal"

    // ═══════════════════════════════════════════════════════════════════════════
    // LAYOUT
    // ═══════════════════════════════════════════════════════════════════════════

    // Estado para el ancho del contenedor (necesario para calcular posición del tooltip)
    var containerWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Column(
        modifier =
            modifier.fillMaxWidth().semantics {
                contentDescription = accessibilityDescription
                stateDescription = if (isInteracting) "Ajustando posición" else ""
            }
    ) {
        // ── Tooltip flotante ──
        FloatingTooltip(
            isVisible = isInteracting,
            timeText = tiempoActual,
            progress = progress,
            colors = colors,
        )

        // ── Slider con track custom ──
        Box(
            modifier = Modifier.fillMaxWidth().height(SliderDimens.ThumbTouchTarget),
            contentAlignment = Alignment.Center,
        ) {
            // Track personalizado
            CustomTrack(
                progress = progress,
                bufferProgress = bufferProgress,
                trackHeight = trackHeight,
                colors = colors,
                modifier = Modifier.fillMaxWidth().padding(horizontal = SliderDimens.ThumbSize / 2),
            )

            // Slider para capturar gestos
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    onEvento(ReproductorEvento.Navegacion.OnScrub(newValue.toLong()))
                },
                onValueChangeFinished = {
                    onEvento(ReproductorEvento.Navegacion.OnScrubFinished(sliderValue.toLong()))
                },
                valueRange = 0f..duracionTotal.toFloat().coerceAtLeast(1f),
                interactionSource = interactionSource,
                colors =
                    SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                thumb = {
                    GlowingThumb(
                        scale = thumbScale,
                        glowAlpha = glowAlpha,
                        isInteracting = isInteracting,
                        colors = colors,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Tiempos ──
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = tiempoActual,
                color = if (isInteracting) colors.timeTextActive else colors.timeTextNormal,
                style = FreePlayerTypography.extended.timerText,
            )

            val tiempoRestante = (duracionTotal - sliderValue.toLong()).coerceAtLeast(0)
            Text(
                text = "-${tiempoRestante.formatearTiempo()}",
                color = colors.timeTextNormal,
                style = FreePlayerTypography.extended.timerText,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// COMPONENTES INTERNOS — SLIDER EXPANDIDO
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomTrack(
    progress: Float,
    bufferProgress: Float,
    trackHeight: Dp,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    val gradientBrush =
        remember(colors) {
            Brush.horizontalGradient(listOf(colors.trackActiveStart, colors.trackActiveEnd))
        }

    Box(
        modifier =
            modifier.height(trackHeight).clip(RoundedCornerShape(trackHeight / 2)).drawBehind {
                val cornerRadius = CornerRadius(size.height / 2, size.height / 2)

                // 1. Track inactivo (fondo)
                drawRoundRect(color = colors.trackInactive, cornerRadius = cornerRadius)

                // 2. Buffer progress
                if (bufferProgress < 1f) {
                    drawRoundRect(
                        color = colors.trackBuffer,
                        size = Size(size.width * bufferProgress, size.height),
                        cornerRadius = cornerRadius,
                    )
                }

                // 3. Track activo con gradiente
                if (progress > 0f) {
                    drawRoundRect(
                        brush = gradientBrush,
                        size = Size(size.width * progress, size.height),
                        cornerRadius = cornerRadius,
                    )
                }
            }
    )
}

@Composable
private fun GlowingThumb(
    scale: Float,
    glowAlpha: Float,
    isInteracting: Boolean,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(SliderDimens.ThumbTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        // Glow exterior (blur) - solo cuando interactúa
        if (isInteracting) {
            Box(
                modifier =
                    Modifier.size(SliderDimens.ThumbSize + SliderDimens.GlowRadius)
                        .scale(scale)
                        .blur(8.dp)
                        .background(
                            color = colors.thumbGlowActive.copy(alpha = glowAlpha * 0.6f),
                            shape = CircleShape,
                        )
            )
        }

        // Glow medio
        Box(
            modifier =
                Modifier.size(SliderDimens.ThumbSize + 6.dp)
                    .scale(scale)
                    .background(
                        color = colors.thumbGlow.copy(alpha = glowAlpha * 0.4f),
                        shape = CircleShape,
                    )
        )

        // Thumb principal
        Box(
            modifier =
                Modifier.size(SliderDimens.ThumbSize)
                    .scale(scale)
                    .shadow(elevation = if (isInteracting) 8.dp else 4.dp, shape = CircleShape)
                    .background(color = colors.thumbColor, shape = CircleShape)
        )
    }
}

// Añadir este composable privado después de GlowingThumb

@Composable
private fun FloatingTooltip(
    isVisible: Boolean,
    timeText: String,
    progress: Float,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    var containerWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val tooltipWidthPx = with(density) { SliderDimens.TooltipWidth.toPx() }

    val tooltipOffsetX by
        remember(progress, containerWidth, tooltipWidthPx) {
            derivedStateOf {
                if (containerWidth > 0) {
                    val thumbPosition = progress * containerWidth
                    (thumbPosition - tooltipWidthPx / 2).coerceIn(
                        0f,
                        (containerWidth - tooltipWidthPx).coerceAtLeast(0f),
                    )
                } else 0f
            }
        }

    Box(
        modifier =
            modifier.fillMaxWidth().height(SliderDimens.TooltipHeight + 4.dp).onSizeChanged {
                containerWidth = it.width
            },
        contentAlignment = Alignment.BottomStart,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter =
                fadeIn(tween(150)) +
                    scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f)),
            exit =
                fadeOut(tween(150)) +
                    scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f)),
        ) {
            Surface(
                color = colors.tooltipBackground,
                shape = RoundedCornerShape(SliderDimens.TooltipCornerRadius),
                shadowElevation = 8.dp,
                modifier =
                    Modifier.offset { IntOffset(tooltipOffsetX.roundToInt(), 0) }
                        .width(SliderDimens.TooltipWidth),
            ) {
                Text(
                    text = timeText,
                    color = colors.tooltipText,
                    style = FreePlayerTypography.extended.timerText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎚️ SLIDER COMPACTO — VERSIÓN MINIMALISTA
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 🎚️ Slider Compacto Minimalista
 *
 * Para el mini player. Prioriza el espacio y la simplicidad.
 *
 * @param estado Estado del reproductor
 * @param onEvento Callback para eventos
 * @param modifier Modifier externo
 * @param showTimes Si mostrar los tiempos a los lados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderProgresoCompacto(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
    showTimes: Boolean = true,
) {
    val colors = compactSliderColors()
    val duracionTotal = estado.duracionTotalMs
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged || estado.isScrubbing

    var sliderValue by
        remember(estado.cancionActual) { mutableFloatStateOf(estado.progresoVisibleMs.toFloat()) }

    LaunchedEffect(estado.progresoVisibleMs, estado.isScrubbing) {
        if (!estado.isScrubbing) {
            sliderValue = estado.progresoVisibleMs.toFloat()
        }
    }

    val progress by
        remember(sliderValue, duracionTotal) {
            derivedStateOf {
                if (duracionTotal > 0) (sliderValue / duracionTotal).coerceIn(0f, 1f) else 0f
            }
        }

    // Animaciones sutiles
    val trackHeight by
        animateDpAsState(
            targetValue =
                if (isInteracting) CompactSliderDimens.TrackHeightActive
                else CompactSliderDimens.TrackHeight,
            animationSpec = spring(dampingRatio = 0.8f),
            label = "compactTrackHeight",
        )

    val thumbSize by
        animateDpAsState(
            targetValue =
                if (isInteracting) CompactSliderDimens.ThumbSizeActive
                else CompactSliderDimens.ThumbSize,
            animationSpec = spring(dampingRatio = 0.7f),
            label = "compactThumbSize",
        )

    // Accesibilidad
    val tiempoActual = sliderValue.toLong().formatearTiempo()
    val tiempoTotal = duracionTotal.formatearTiempo()

    Row(
        modifier =
            modifier.fillMaxWidth().semantics {
                contentDescription = "Progreso: $tiempoActual de $tiempoTotal"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showTimes) {
            Text(
                text = tiempoActual,
                color = if (isInteracting) colors.timeTextActive else colors.timeTextNormal,
                style = FreePlayerTypography.extended.duration,
                modifier = Modifier.width(CompactSliderDimens.TimeWidth),
            )
        }

        Box(
            modifier = Modifier.weight(1f).height(CompactSliderDimens.ThumbTouchTarget),
            contentAlignment = Alignment.Center,
        ) {
            CompactTrack(
                progress = progress,
                trackHeight = trackHeight,
                colors = colors,
                modifier =
                    Modifier.fillMaxWidth().padding(horizontal = CompactSliderDimens.ThumbSize / 2),
            )

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    onEvento(ReproductorEvento.Navegacion.OnScrub(newValue.toLong()))
                },
                onValueChangeFinished = {
                    onEvento(ReproductorEvento.Navegacion.OnScrubFinished(sliderValue.toLong()))
                },
                valueRange = 0f..duracionTotal.toFloat().coerceAtLeast(1f),
                interactionSource = interactionSource,
                colors =
                    SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                thumb = {
                    CompactThumb(size = thumbSize, isInteracting = isInteracting, colors = colors)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (showTimes) {
            Text(
                text = tiempoTotal,
                color = colors.timeTextNormal,
                style = FreePlayerTypography.extended.duration,
                textAlign = TextAlign.End,
                modifier = Modifier.width(CompactSliderDimens.TimeWidth),
            )
        }
    }
}

@Composable
private fun CompactTrack(
    progress: Float,
    trackHeight: Dp,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.height(trackHeight).clip(RoundedCornerShape(trackHeight / 2)).drawBehind {
                val cornerRadius = CornerRadius(size.height / 2, size.height / 2)

                drawRoundRect(color = colors.trackInactive, cornerRadius = cornerRadius)

                if (progress > 0f) {
                    drawRoundRect(
                        color = colors.trackActiveStart,
                        size = Size(size.width * progress, size.height),
                        cornerRadius = cornerRadius,
                    )
                }
            }
    )
}

@Composable
private fun CompactThumb(
    size: Dp,
    isInteracting: Boolean,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(CompactSliderDimens.ThumbTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        if (isInteracting) {
            Box(
                modifier =
                    Modifier.size(size + 4.dp)
                        .background(color = colors.thumbGlow, shape = CircleShape)
            )
        }

        Box(
            modifier =
                Modifier.size(size)
                    .shadow(elevation = if (isInteracting) 4.dp else 2.dp, shape = CircleShape)
                    .background(color = colors.thumbColor, shape = CircleShape)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎚️ PROGRESS BAR — ULTRA-MINIMALISTA
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 🎚️ Barra de Progreso Táctil
 *
 * Versión ultra-minimalista: solo una barra sin thumb visible. Ideal para espacios muy reducidos o
 * como indicador secundario.
 *
 * @param progress Progreso actual (0f - 1f)
 * @param onSeek Callback cuando el usuario toca para buscar
 * @param modifier Modifier externo
 * @param height Altura de la barra
 * @param interactive Si permite interacción táctil
 */
@Composable
fun ProgressBar(
    progress: Float,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    interactive: Boolean = true,
) {
    val colors = sliderColors()

    val animatedProgress by
        animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(100),
            label = "barProgress",
        )

    val gradientBrush =
        remember(colors) {
            Brush.horizontalGradient(listOf(colors.trackActiveStart, colors.trackActiveEnd))
        }

    val pointerModifier =
        if (interactive && onSeek != null) {
            Modifier.pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
        } else Modifier

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .then(pointerModifier)
                .semantics { contentDescription = "Progreso: ${(animatedProgress * 100).toInt()}%" }
                .drawBehind {
                    val cornerRadius = CornerRadius(size.height / 2, size.height / 2)

                    drawRoundRect(color = colors.trackInactive, cornerRadius = cornerRadius)

                    if (animatedProgress > 0f) {
                        drawRoundRect(
                            brush = gradientBrush,
                            size = Size(size.width * animatedProgress, size.height),
                            cornerRadius = cornerRadius,
                        )
                    }
                }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 PREVIEWS EXHAUSTIVAS — Añadir al final de ProgressSlider.kt
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────────
// Preview Data Provider
// ─────────────────────────────────────────────────────────────────────────────────

private data class SliderPreviewState(val nombre: String, val estado: PlayerState)

private class SliderPreviewProvider : PreviewParameterProvider<SliderPreviewState> {

    private val song =
        SongWithArtist.preview(
            titulo = "Bohemian Rhapsody",
            artista = "Queen",
            duracionSegundos = 354,
        )

    private val durationMs = 354 * 1000L

    override val values =
        sequenceOf(
            SliderPreviewState(
                nombre = "▶️ Inicio (00:00)",
                estado =
                    PlayerState(
                        cancionActual = song,
                        progresoActualMs = 0L,
                        estaReproduciendo = true,
                    ),
            ),
            SliderPreviewState(
                nombre = "▶️ Mitad (02:57)",
                estado =
                    PlayerState(
                        cancionActual = song,
                        progresoActualMs = durationMs / 2,
                        estaReproduciendo = true,
                    ),
            ),
            SliderPreviewState(
                nombre = "🎚️ Scrubbing",
                estado =
                    PlayerState(
                        cancionActual = song,
                        progresoActualMs = 60000L,
                        progresoTemporalMs = 240000L,
                        isScrubbing = true,
                        estaReproduciendo = true,
                    ),
            ),
            SliderPreviewState(
                nombre = "⏸️ Final (05:30)",
                estado =
                    PlayerState(
                        cancionActual = song,
                        progresoActualMs = 330000L,
                        estaReproduciendo = false,
                    ),
            ),
        )
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Slider Expandido — Dark
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🎚️ Expandido • Dark",
    group = "Slider Expandido",
    showBackground = true,
    backgroundColor = 0xFF0E0021,
    widthDp = 360,
    heightDp = 140,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProgressSliderDarkPreview(
    @PreviewParameter(SliderPreviewProvider::class) data: SliderPreviewState
) {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
        ) {
            Text(
                text = data.nombre,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProgressSlider(estado = data.estado, onEvento = {})
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Slider Expandido — Light
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🎚️ Expandido • Light",
    group = "Slider Expandido",
    showBackground = true,
    backgroundColor = 0xFFF3EEFF,
    widthDp = 360,
    heightDp = 140,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun ProgressSliderLightPreview(
    @PreviewParameter(SliderPreviewProvider::class) data: SliderPreviewState
) {
    FreePlayerMTheme(darkTheme = false) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
        ) {
            Text(
                text = data.nombre,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProgressSlider(estado = data.estado, onEvento = {})
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Slider Compacto
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🎚️ Compacto • Dark",
    group = "Slider Compacto",
    showBackground = true,
    backgroundColor = 0xFF0E0021,
    widthDp = 360,
    heightDp = 100,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SliderCompactoDarkPreview(
    @PreviewParameter(SliderPreviewProvider::class) data: SliderPreviewState
) {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = data.nombre,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            SliderProgresoCompacto(estado = data.estado, onEvento = {})
        }
    }
}

@Preview(
    name = "🎚️ Compacto • Light",
    group = "Slider Compacto",
    showBackground = true,
    backgroundColor = 0xFFF3EEFF,
    widthDp = 360,
    heightDp = 100,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun SliderCompactoLightPreview(
    @PreviewParameter(SliderPreviewProvider::class) data: SliderPreviewState
) {
    FreePlayerMTheme(darkTheme = false) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = data.nombre,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            SliderProgresoCompacto(estado = data.estado, onEvento = {})
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Comparación de Variantes
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🔄 Comparación de Variantes",
    group = "Comparación",
    showBackground = true,
    backgroundColor = 0xFF0E0021,
    widthDp = 360,
    heightDp = 420,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SliderComparisonPreview() {
    val estado =
        PlayerState(
            cancionActual =
                SongWithArtist.preview(
                    titulo = "Test Song",
                    artista = "Artist",
                    duracionSegundos = 240,
                ),
            progresoActualMs = 90000L,
            estaReproduciendo = true,
        )

    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Expandido
            SectionLabel("SLIDER EXPANDIDO")
            ProgressSlider(estado = estado, onEvento = {})

            // Compacto con tiempos
            SectionLabel("SLIDER COMPACTO")
            SliderProgresoCompacto(estado = estado, onEvento = {})

            // Compacto sin tiempos
            SectionLabel("COMPACTO SIN TIEMPOS")
            SliderProgresoCompacto(estado = estado, onEvento = {}, showTimes = false)

            // Progress Bar
            SectionLabel("PROGRESS BAR (Ultra-minimal)")
            Spacer(modifier = Modifier.height(4.dp))
            ProgressBar(progress = 0.375f, height = 3.dp)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        letterSpacing = 1.sp,
    )
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: En Contexto del Player
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "📱 En Contexto del Player",
    group = "Contexto",
    showBackground = true,
    backgroundColor = 0xFF0E0021,
    widthDp = 360,
    heightDp = 300,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SliderInPlayerContextPreview() {
    val estado =
        PlayerState(
            cancionActual =
                SongWithArtist.preview(
                    titulo = "Bohemian Rhapsody",
                    artista = "Queen",
                    duracionSegundos = 354,
                ),
            progresoActualMs = 142000L,
            estaReproduciendo = true,
        )

    FreePlayerMTheme(darkTheme = true) {
        val extended = FreePlayerTheme.extendedColors

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(AppColors.ElectricViolet.v10, extended.playerBackground)
                            )
                    )
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Título y artista
            Text(
                text = estado.tituloDisplay,
                style = FreePlayerTypography.extended.nowPlayingTitle,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = estado.artistaDisplay,
                style = FreePlayerTypography.extended.nowPlayingArtist,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Slider expandido
            ProgressSlider(estado = estado, onEvento = {})

            Spacer(modifier = Modifier.weight(1f))

            // Mini player card
            Surface(color = extended.miniPlayerSurface, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = estado.tituloDisplay,
                                style = FreePlayerTypography.extended.miniPlayerTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = estado.artistaDisplay,
                                style = FreePlayerTypography.extended.miniPlayerSubtitle,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderProgresoCompacto(estado = estado, onEvento = {})
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Con Buffer Progress
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "📶 Con Buffer Progress",
    group = "Estados Especiales",
    showBackground = true,
    backgroundColor = 0xFF0E0021,
    widthDp = 360,
    heightDp = 220,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SliderWithBufferPreview() {
    val estado =
        PlayerState(
            cancionActual =
                SongWithArtist.preview(
                    titulo = "Streaming Song",
                    artista = "Online Artist",
                    duracionSegundos = 300,
                ),
            progresoActualMs = 45000L,
            estaReproduciendo = true,
        )

    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column {
                SectionLabel("Buffer al 60%")
                Spacer(modifier = Modifier.height(8.dp))
                ProgressSlider(estado = estado, onEvento = {}, bufferProgress = 0.6f)
            }

            Column {
                SectionLabel("Buffer al 100% (default)")
                Spacer(modifier = Modifier.height(8.dp))
                ProgressSlider(estado = estado, onEvento = {}, bufferProgress = 1f)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// Preview: Light vs Dark
// ─────────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "🌓 Light vs Dark",
    group = "Temas",
    showBackground = true,
    widthDp = 360,
    heightDp = 320,
)
@Composable
private fun SliderThemeComparisonPreview() {
    val estado =
        PlayerState(
            cancionActual =
                SongWithArtist.preview(
                    titulo = "Theme Test",
                    artista = "Artist",
                    duracionSegundos = 180,
                ),
            progresoActualMs = 60000L,
            estaReproduciendo = true,
        )

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark Theme
        FreePlayerMTheme(darkTheme = true) {
            Column(
                modifier =
                    Modifier.weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
            ) {
                SectionLabel("DARK THEME")
                Spacer(modifier = Modifier.height(8.dp))
                ProgressSlider(estado = estado, onEvento = {})
            }
        }

        // Light Theme
        FreePlayerMTheme(darkTheme = false) {
            Column(
                modifier =
                    Modifier.weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
            ) {
                SectionLabel("LIGHT THEME")
                Spacer(modifier = Modifier.height(8.dp))
                ProgressSlider(estado = estado, onEvento = {})
            }
        }
    }
}
