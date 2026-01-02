package com.example.freeplayerm.ui.features.player

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.features.player.components.BotonFavorito
import com.example.freeplayerm.ui.features.player.components.ControlesNormales
import com.example.freeplayerm.ui.features.player.components.ExpandedPlayerTabs
import com.example.freeplayerm.ui.features.player.components.PlaybackControls
import com.example.freeplayerm.ui.features.player.components.ProgressSlider
import com.example.freeplayerm.ui.features.player.components.SliderProgresoCompacto
import com.example.freeplayerm.ui.features.player.components.SpinningVinyl
import com.example.freeplayerm.ui.features.player.gesture.PlayerGestureConfig
import com.example.freeplayerm.ui.features.player.gesture.InterpolatedValues
import com.example.freeplayerm.ui.features.player.gesture.PlayerInterpolator
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Immutable
private object PlayerTokens {
    // ═══════════════════════════════════════════════════════════════════════
    // BORDER RADIUS (solo superior)
    // ═══════════════════════════════════════════════════════════════════════

    /** Radio de esquinas superiores en modo minimizado */
    val MinimizedCornerRadius = 20.dp

    /** Radio de esquinas superiores en modo normal */
    val NormalCornerRadius = 24.dp

    /** Radio de esquinas superiores en modo expandido */
    val ExpandedCornerRadius = 28.dp

    // ═══════════════════════════════════════════════════════════════════════
    // PADDING INTERNO (para elementos específicos, no layouts)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Padding horizontal interno para contenido de layouts. NO se aplica al layout principal, solo
     * a elementos internos.
     */
    val ContentPaddingH = 16.dp

    /**
     * Padding vertical mínimo para elementos con texto. Usado en componentes individuales, no en
     * layouts.
     */
    val ContentPaddingV = 12.dp

    /** Padding horizontal para modo expandido. Aplicado al Column interno, no al Box contenedor. */
    val ExpandedContentPadding = 24.dp

    // ═══════════════════════════════════════════════════════════════════════
    // DIMENSIONES DE COMPONENTES
    // ═══════════════════════════════════════════════════════════════════════

    /** Altura de la barra de progreso lineal */
    val ProgressBarHeight = 3.dp

    /** Tamaño del indicador de reproducción (punto pulsante) */
    val PlayingIndicatorSize = 8.dp

    /** Ancho del indicador de swipe */
    val SwipeIndicatorWidth = 40.dp

    /** Altura del indicador de swipe */
    val SwipeIndicatorHeight = 4.dp

    /** Tamaño mínimo del vinilo */
    val ViniloMinSize = 48.dp

    /** Tamaño máximo del vinilo en modo expandido */
    val ViniloMaxSize = 300.dp

    // ═══════════════════════════════════════════════════════════════════════
    // SPACING ENTRE ELEMENTOS
    // ═══════════════════════════════════════════════════════════════════════

    /** Espacio entre vinilo e info en modo normal */
    val ViniloInfoSpacing = 12.dp

    /** Espacio entre elementos en modo expandido */
    val ExpandedSpacing = 24.dp

    /** Espacio compacto para elementos pequeños */
    val CompactSpacing = 8.dp

    // ═══════════════════════════════════════════════════════════════════════
    // ALTURAS DE LAYOUTS (porcentajes de pantalla)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Altura del panel minimizado como porcentaje de pantalla. Ejemplo: 0.09f = 9% de pantalla
     * (~72dp en 800dp)
     */
    const val AlturaMinimizadaMultiplier = 0.09f

    /**
     * Altura del panel normal como porcentaje de pantalla. Ejemplo: 0.28f = 28% de pantalla (~224dp
     * en 800dp)
     */
    const val AlturaNormalMultiplier = 0.28f

    // ═══════════════════════════════════════════════════════════════════════
    // UMBRALES DE TRANSICIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Umbral inferior para transiciones. Por debajo de este valor se considera minimizado. */
    const val ExpandedThresholdMultiplier = 0.3f

    /**
     * Umbral superior para transiciones. Por encima de este valor se inicia transición hacia
     * expandido.
     */
    const val MinimizedThresholdMultiplier = 0.5f

    // ═══════════════════════════════════════════════════════════════════════
    // COLORES DE FONDO (gradientes)
    // ═══════════════════════════════════════════════════════════════════════

    val MinimizedGradientStart = Color(0xFF1A1A1A)
    val MinimizedGradientEnd = Color(0xFF2D1B36)
    val NormalGradientStart = Color(0xFF1E1E1E)
    val NormalGradientEnd = Color(0xFF2D1B36)
    val ExpandedGradientStart = Color(0xFF0A0A0A)
    val ExpandedGradientEnd = Color(0xFF1A0B1F)

    // ═══════════════════════════════════════════════════════════════════════
    // OPACIDADES
    // ═══════════════════════════════════════════════════════════════════════

    const val BackgroundAlpha = 0.98f
    const val NormalBackgroundAlpha = 0.95f
    const val ExpandedBackgroundAlpha = 1f
    const val SecondaryTextAlpha = 0.7f
    const val TertiaryTextAlpha = 0.5f
    const val DisabledAlpha = 0.3f
    const val ProgressTrackAlpha = 0.1f

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Genera el RoundedCornerShape con bordes superiores redondeados solamente.
     *
     * @param radius Radio para las esquinas superiores
     * @return Shape con esquinas superiores redondeadas y inferiores rectas
     */
    fun topRoundedShape(radius: Dp) =
        RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 0.dp, bottomEnd = 0.dp)

    /** Shape para modo minimizado (solo bordes superiores). */
    val MinimizedShape = topRoundedShape(MinimizedCornerRadius)

    /** Shape para modo normal (solo bordes superiores). */
    val NormalShape = topRoundedShape(NormalCornerRadius)

    /** Shape para modo expandido (solo bordes superiores). */
    val ExpandedShape = topRoundedShape(ExpandedCornerRadius)
}

@Composable
private fun calcularFactorInterpolacion(
    modoPanel: ModoPanelReproductor,
    progresoGlobal: Float,
    isDragging: Boolean,
): Float {
    return remember(modoPanel, progresoGlobal, isDragging) {
            derivedStateOf {
                when {
                    // Durante el arrastre, usar el progreso continuo
                    isDragging -> progresoGlobal.coerceIn(0f, 1f)

                    // En estado estable, mapear el modo a un valor discreto
                    else ->
                        when (modoPanel) {
                            ModoPanelReproductor.MINIMIZADO -> 0f
                            ModoPanelReproductor.NORMAL -> PlayerTokens.MinimizedThresholdMultiplier
                            ModoPanelReproductor.EXPANDIDO -> 1f
                        }
                }
            }
        }
        .value
}

@Composable
private fun determinarLayoutType(factorInterpolacion: Float): PlayerLayoutType {
    return remember(factorInterpolacion) {
            derivedStateOf {
                when {
                    factorInterpolacion < PlayerTokens.ExpandedThresholdMultiplier ->
                        PlayerLayoutType.MINIMIZED
                    factorInterpolacion < PlayerTokens.MinimizedThresholdMultiplier ->
                        PlayerLayoutType.NORMAL
                    else -> PlayerLayoutType.EXPANDED
                }
            }
        }
        .value
}

private enum class PlayerLayoutType {
    MINIMIZED, // 72dp - Barra compacta (solo scroll)
    NORMAL, // 140dp - Estado base de reproducción
    EXPANDED, // 100% - Pantalla completa
}

// ───────────────────────────────────────────────────────────────────────────
// COMPONENTE PRINCIPAL
// ───────────────────────────────────────────────────────────────────────────
@Composable
fun InterpolatedPlayerContent(
    cancion: SongWithArtist,
    estado: PlayerState,
    interpolatedValues: InterpolatedValues,
    progresoGlobal: Float,
    puntoNormal: Float,
    isDragging: Boolean,
    onEvento: (ReproductorEvento) -> Unit,
    onExpandir: () -> Unit,
    onColapsar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutType =
        remember(estado.modoPanel) {
                derivedStateOf {
                    when (estado.modoPanel) {
                        ModoPanelReproductor.MINIMIZADO -> PlayerLayoutType.MINIMIZED
                        ModoPanelReproductor.NORMAL -> PlayerLayoutType.NORMAL
                        ModoPanelReproductor.EXPANDIDO -> PlayerLayoutType.EXPANDED
                    }
                }
            }
            .value

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = layoutType,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
            label = "PlayerLayoutTransition",
            contentKey = { it },
        ) { currentLayout ->
            when (currentLayout) {
                PlayerLayoutType.MINIMIZED -> {
                    LayoutMinimizado(
                        cancion = cancion,
                        estado = estado,
                        interpolatedValues = interpolatedValues, // ⚡ Añadido
                        onExpandir = onExpandir,
                    )
                }

                PlayerLayoutType.NORMAL -> {
                    LayoutNormal(
                        cancion = cancion,
                        estado = estado,
                        interpolatedValues = interpolatedValues,
                        isDragging = isDragging,
                        onEvento = onEvento,
                        onExpandir = onExpandir,
                    )
                }

                PlayerLayoutType.EXPANDED -> {
                    LayoutExpandido(
                        cancion = cancion,
                        estado = estado,
                        interpolatedValues = interpolatedValues,
                        onEvento = onEvento,
                        onColapsar = onColapsar,
                        scrollState = scrollState,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LAYOUT MINIMIZADO
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun LayoutMinimizado(
    cancion: SongWithArtist,
    estado: PlayerState,
    interpolatedValues: InterpolatedValues,
    onExpandir: () -> Unit,
) {
    val tiempoFormateado =
        remember(estado.progresoVisibleMs, estado.duracionTotalMs) {
            "${estado.progresoVisibleMs.formatearTiempo()} / ${estado.duracionTotalMs.formatearTiempo()}"
        }

    val infoCancion =
        remember(cancion.cancion.titulo, cancion.artistaNombre) {
            "${cancion.cancion.titulo} • ${cancion.artistaNombre ?: "Desconocido"}"
        }

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(interpolatedValues.panelHeightDp) // ⚡ Altura desde interpolador
                .clip(PlayerTokens.MinimizedShape) // ⚡ Shape predefinido
                .background(
                    Brush.horizontalGradient(
                        colors =
                            listOf(
                                PlayerTokens.MinimizedGradientStart.copy(
                                    alpha = PlayerTokens.BackgroundAlpha
                                ),
                                PlayerTokens.MinimizedGradientEnd.copy(
                                    alpha = PlayerTokens.BackgroundAlpha
                                ),
                            )
                    )
                )
                .clickable(onClick = onExpandir)
                .semantics {
                    contentDescription = "Reproductor minimizado. $infoCancion. Toca para expandir."
                }
    ) {
        // ═══════════════════════════════════════════════════════════════
        // CONTENIDO PRINCIPAL
        // ═══════════════════════════════════════════════════════════════

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = PlayerTokens.ContentPaddingH,
                        end = PlayerTokens.ContentPaddingH,
                        top = 0.dp,
                        bottom = PlayerTokens.ProgressBarHeight, // ⚡ Espacio para la barra
                    )
                    .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PlayerTokens.CompactSpacing),
        ) {
            // ─────────────────────────────────────────────────────────
            // Indicador de reproducción
            // ─────────────────────────────────────────────────────────
            PlayingIndicator(isPlaying = estado.estaReproduciendo)

            // ─────────────────────────────────────────────────────────
            // Título y artista con marquee (flexible)
            // ─────────────────────────────────────────────────────────
            Text(
                text = infoCancion,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.weight(1f)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 2000,
                            velocity = 30.dp,
                        ),
            )

            // ─────────────────────────────────────────────────────────
            // Tiempo (ancho fijo)
            // ─────────────────────────────────────────────────────────
            Text(
                text = tiempoFormateado,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.widthIn(min = 70.dp, max = 90.dp),
                textAlign = TextAlign.End,
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // BARRA DE PROGRESO (siempre al fondo)
        // ═══════════════════════════════════════════════════════════════

        if (estado.duracionTotalMs > 0) {
            LinearProgressIndicator(
                progress = { estado.progresoPorcentaje },
                modifier =
                    Modifier.fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(PlayerTokens.ProgressBarHeight),
                color = AppColors.ElectricViolet.v6,
                trackColor = Color.White.copy(alpha = PlayerTokens.ProgressTrackAlpha),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LAYOUT NORMAL (Estado base de reproducción)
// ═══════════════════════════════════════════════════════════════════════════

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun LayoutNormal(
    cancion: SongWithArtist,
    estado: PlayerState,
    interpolatedValues: InterpolatedValues,
    isDragging: Boolean,
    onEvento: (ReproductorEvento) -> Unit,
    onExpandir: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // ⚡ Altura real desde el interpolador
    val alturaPanel = interpolatedValues.panelHeightDp
    val viniloSize = interpolatedValues.viniloSizeDp

    // Tamaños de fuente responsivos basados en altura del panel
    val tituloFontSize =
        remember(alturaPanel) {
            when {
                alturaPanel < 180.dp -> 15.sp
                alturaPanel < 220.dp -> 16.sp
                else -> 17.sp
            }
        }

    val artistaFontSize =
        remember(alturaPanel) {
            when {
                alturaPanel < 180.dp -> 12.sp
                alturaPanel < 220.dp -> 13.sp
                else -> 14.sp
            }
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .height(alturaPanel) // ⚡ Altura desde interpolador
                .clip(PlayerTokens.NormalShape) // ⚡ Shape predefinido
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                PlayerTokens.NormalGradientStart.copy(
                                    alpha = PlayerTokens.NormalBackgroundAlpha
                                ),
                                PlayerTokens.NormalGradientEnd.copy(
                                    alpha = PlayerTokens.NormalBackgroundAlpha
                                ),
                            )
                    )
                )
                .padding(horizontal = PlayerTokens.ContentPaddingH) // ⚡ Padding interno
    ) {
        Spacer(modifier = Modifier.height(PlayerTokens.ContentPaddingV))

        // ────────────────────────────────────────────────────────────────────
        // Fila superior: Vinilo + Info + Controles
        // ────────────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PlayerTokens.ViniloInfoSpacing),
        ) {
            // ─────────────────────────────────────────────────────────────
            // Vinilo clickeable con animación de escala
            // ─────────────────────────────────────────────────────────────
            ViniloClickeable(
                cancion = cancion,
                estado = estado,
                size = viniloSize,
                onExpandir = onExpandir,
                interactionSource = interactionSource,
            )

            // ─────────────────────────────────────────────────────────────
            // Info de canción (flexible)
            // ─────────────────────────────────────────────────────────────
            Column(
                modifier =
                    Modifier.weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onExpandir,
                        ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = cancion.cancion.titulo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = tituloFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 2000,
                            velocity = 30.dp,
                        ),
                )
                Text(
                    text = cancion.artistaNombre ?: "Desconocido",
                    color = Color.White.copy(alpha = PlayerTokens.SecondaryTextAlpha),
                    fontSize = artistaFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ─────────────────────────────────────────────────────────────
            // Controles normales
            // ─────────────────────────────────────────────────────────────
            Box(modifier = Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.Center) {
                ControlesNormales(
                    estado = estado,
                    onEvento = onEvento,
                    modifier = Modifier.alpha(interpolatedValues.controlesNormalesAlpha),
                )
            }
        }

        Spacer(modifier = Modifier.height(PlayerTokens.CompactSpacing))

        // ────────────────────────────────────────────────────────────────────
        // Slider de progreso compacto
        // ────────────────────────────────────────────────────────────────────
        SliderProgresoCompacto(
            estado = estado,
            onEvento = onEvento,
            modifier = Modifier.fillMaxWidth().alpha(interpolatedValues.sliderCompactoAlpha),
        )

        Spacer(modifier = Modifier.height(PlayerTokens.CompactSpacing))
    }
}

/** 🎵 Vinilo con animación de escala al presionar */
@Composable
private fun ViniloClickeable(
    cancion: SongWithArtist,
    estado: PlayerState,
    size: Dp,
    onExpandir: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    // Animación de escala al presionar
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            label = "ViniloScale",
        )

    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onExpandir,
                )
                .semantics {
                    contentDescription = "Portada del álbum. Toca para expandir."
                    role = Role.Button
                }
    ) {
        SpinningVinyl(
            cancion = cancion,
            estaReproduciendo = estado.estaReproduciendo,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LAYOUT EXPANDIDO
// ═══════════════════════════════════════════════════════════════════════════

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun LayoutExpandido(
    cancion: SongWithArtist,
    estado: PlayerState,
    interpolatedValues: InterpolatedValues,
    onEvento: (ReproductorEvento) -> Unit,
    onColapsar: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    // ⚡ Tamaño del vinilo desde el interpolador
    val viniloSize = interpolatedValues.viniloSizeDp

    Box(
        modifier =
            Modifier.fillMaxSize()
                .clip(PlayerTokens.ExpandedShape) // ⚡ Shape predefinido
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                PlayerTokens.ExpandedGradientStart.copy(
                                    alpha = PlayerTokens.ExpandedBackgroundAlpha
                                ),
                                PlayerTokens.ExpandedGradientEnd.copy(
                                    alpha = PlayerTokens.ExpandedBackgroundAlpha
                                ),
                            )
                    )
                )
    ) {
        // Overlay oscuro progresivo
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = interpolatedValues.fondoIntensidad * 0.3f))
        )

        // ───────────────────────────────────────────────────────────────────
        // Contenido scrolleable
        // ───────────────────────────────────────────────────────────────────
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = PlayerTokens.ExpandedContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ───────────────────────────────────────────────────────────────
            // Header con botón colapsar
            // ───────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(vertical = PlayerTokens.ContentPaddingV)
                        .alpha(interpolatedValues.controlesExpandidosAlpha)
                        .graphicsLayer {
                            translationY = -20f * (1f - interpolatedValues.controlesExpandidosAlpha)
                        }
            ) {
                ExpandedHeader(
                    alpha = interpolatedValues.controlesExpandidosAlpha,
                    onColapsar = onColapsar,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───────────────────────────────────────────────────────────────
            // Vinilo con glow
            // ───────────────────────────────────────────────────────────────
            ViniloConGlow(
                cancion = cancion,
                estado = estado,
                viniloSize = viniloSize,
                glowScale = interpolatedValues.glowScale,
                glowAlpha = interpolatedValues.glowAlpha,
                modifier =
                    Modifier.graphicsLayer {
                        val scale = 0.85f + (0.15f * interpolatedValues.controlesExpandidosAlpha)
                        scaleX = scale
                        scaleY = scale
                    },
            )

            Spacer(modifier = Modifier.height(PlayerTokens.ExpandedSpacing))

            // ───────────────────────────────────────────────────────────────
            // Info de canción centrada
            // ───────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .alpha(interpolatedValues.controlesExpandidosAlpha)
                        .graphicsLayer {
                            scaleX = interpolatedValues.infoScale
                            scaleY = interpolatedValues.infoScale
                            translationY = 10f * (1f - interpolatedValues.controlesExpandidosAlpha)
                        }
            ) {
                ExpandedSongInfo(
                    cancion = cancion,
                    estado = estado,
                    alpha = interpolatedValues.controlesExpandidosAlpha,
                    onToggleFavorito = {
                        onEvento(ReproductorEvento.Configuracion.AlternarFavorito)
                    },
                )
            }

            Spacer(modifier = Modifier.height(PlayerTokens.ExpandedSpacing))

            // ───────────────────────────────────────────────────────────────
            // Slider completo
            // ───────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .alpha(interpolatedValues.sliderCompletoAlpha)
                        .graphicsLayer {
                            translationY = 15f * (1f - interpolatedValues.sliderCompletoAlpha)
                        }
            ) {
                ProgressSlider(
                    estado = estado,
                    onEvento = onEvento,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───────────────────────────────────────────────────────────────
            // Controles expandidos
            // ───────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .alpha(interpolatedValues.controlesExpandidosAlpha)
                        .graphicsLayer {
                            translationY = 20f * (1f - interpolatedValues.controlesExpandidosAlpha)
                        }
            ) {
                PlaybackControls(
                    estado = estado,
                    onEvento = onEvento,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(PlayerTokens.ExpandedSpacing))

            // ───────────────────────────────────────────────────────────────
            // Tabs
            // ───────────────────────────────────────────────────────────────
            Box(
                modifier =
                    Modifier.fillMaxWidth().alpha(interpolatedValues.tabsAlpha).graphicsLayer {
                        translationY = interpolatedValues.tabsOffsetY.value
                    }
            ) {
                ExpandedPlayerTabs(
                    estado = estado,
                    onEvento = onEvento,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// COMPONENTES REUTILIZABLES
// ═══════════════════════════════════════════════════════════════════════════

/** Indicador visual de estado de reproducción */
@Composable
private fun PlayingIndicator(isPlaying: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(PlayerTokens.PlayingIndicatorSize)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isPlaying) AppColors.ElectricViolet.v6
                    else Color.White.copy(alpha = PlayerTokens.DisabledAlpha)
                )
                .semantics { contentDescription = if (isPlaying) "Reproduciendo" else "Pausado" }
    )
}

/** Header del modo expandido */
@Composable
private fun ExpandedHeader(alpha: Float, onColapsar: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp).alpha(alpha)) {
        IconButton(
            onClick = onColapsar,
            modifier =
                Modifier.align(Alignment.CenterStart).semantics {
                    contentDescription = "Minimizar reproductor"
                },
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }

        Text(
            text = "REPRODUCIENDO",
            color = Color.White.copy(alpha = PlayerTokens.SecondaryTextAlpha),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun ViniloConGlow(
    cancion: SongWithArtist,
    estado: PlayerState,
    viniloSize: Dp,
    glowScale: Float,
    glowAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.size(viniloSize + 40.dp).graphicsLayer {
                scaleX = glowScale
                scaleY = glowScale
            },
        contentAlignment = Alignment.Center,
    ) {
        // Glow effect
        if (glowAlpha > 0.01f) {
            Box(
                modifier =
                    Modifier.size(viniloSize + 40.dp)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            AppColors.ElectricViolet.v6.copy(alpha = glowAlpha),
                                            AppColors.ElectricViolet.v6.copy(
                                                alpha = glowAlpha * 0.4f
                                            ),
                                            Color.Transparent,
                                        )
                                ),
                            shape = CircleShape,
                        )
            )
        }

        SpinningVinyl(
            cancion = cancion,
            estaReproduciendo = estado.estaReproduciendo,
            modifier = Modifier.size(viniloSize),
        )
    }
}

/** Información de canción en modo expandido */
@Composable
private fun ExpandedSongInfo(
    cancion: SongWithArtist,
    estado: PlayerState,
    alpha: Float,
    onToggleFavorito: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = cancion.cancion.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = cancion.artistaNombre ?: "Artista Desconocido",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.ElectricViolet.v6,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.width(12.dp))

            BotonFavorito(
                esFavorita = estado.esFavorita,
                onToggle = onToggleFavorito,
                modifier = Modifier.size(32.dp),
            )
        }

        cancion.albumNombre?.let { album ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = PlayerTokens.TertiaryTextAlpha),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// 🎨 PREVIEWS MINIMALISTAS
// ══════════════════════════════════════════════════════════════════════════════

// ──────────────────────────────────────────────────────────────────────────────
// Preview Data Provider
// ──────────────────────────────────────────────────────────────────────────────

private fun createPreviewSong(
    titulo: String = "Starboy",
    artista: String = "The Weeknd",
    album: String? = "Starboy",
    duracionSegundos: Int = 230,
): SongWithArtist {
    return SongWithArtist(
        cancion =
            SongEntity(
                idCancion = 1,
                titulo = titulo,
                duracionSegundos = duracionSegundos,
                idArtista = 1,
                idAlbum = if (album != null) 1 else null,
                idGenero = 1,
                origen = SongEntity.ORIGEN_LOCAL,
                archivoPath = "/storage/emulated/0/Music/${titulo.replace(" ", "_")}.mp3",
                urlStreaming = null,
                geniusId = "12345",
                geniusUrl = "https://genius.com/fake-song",
                tituloCompleto = "$titulo (feat. Daft Punk)",
                lyricsState = "complete",
                hot = true,
                pageviews = 1500000,
                idioma = "en",
                ubicacionGrabacion = "Los Angeles, CA",
                externalIdsJson = """{"spotify": "abc123", "youtube": "xyz789"}""",
                vecesReproducida = 156,
                ultimaReproduccion = System.currentTimeMillis() - (1000 * 60 * 60 * 2),
                fechaAgregado = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7),
                fechaModificacion = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                calidadAudio = SongEntity.CALIDAD_HIGH,
                bitrate = 320,
                letraDisponible = true,
                portadaPath = "/storage/emulated/0/Music/Covers/${album?.replace(" ", "_")}.jpg",
                numeroPista = 1,
                anio = 2016,
            ),
        artistaNombre = artista,
        albumNombre = album,
    )
}

private val cancionPreview = createPreviewSong()

// ──────────────────────────────────────────────────────────────────────────────
// Preview 1: Modo Minimizado
// ──────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "Minimizado",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 400,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewMinimizado() {
    val screenHeight = 800.dp
    val config = PlayerGestureConfig.Default

    val estado =
        PlayerState(
            cancionActual = cancionPreview,
            estaReproduciendo = true,
            progresoActualMs = 60000L,
            modoPanel = ModoPanelReproductor.MINIMIZADO,
        )

    val interpolatedValues =
        PlayerInterpolator.calcular(
            progresoGlobal = config.alturaMinimizado,
            screenHeightDp = screenHeight,
            isDragging = false,
            config = config,
        )

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            GalaxyBackground()

            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                InterpolatedPlayerContent(
                    cancion = cancionPreview,
                    estado = estado,
                    interpolatedValues = interpolatedValues,
                    progresoGlobal = config.alturaMinimizado,
                    puntoNormal = config.alturaNormal,
                    isDragging = false,
                    onEvento = {},
                    onExpandir = {},
                    onColapsar = {},
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Preview 2: Modo Normal
// ──────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "Normal",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 400,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewNormal() {
    val screenHeight = 800.dp
    val config = PlayerGestureConfig.Default

    val estado =
        PlayerState(
            cancionActual = cancionPreview,
            estaReproduciendo = true,
            progresoActualMs = 125000L,
            modoPanel = ModoPanelReproductor.NORMAL,
        )

    val interpolatedValues =
        PlayerInterpolator.calcular(
            progresoGlobal = config.alturaNormal,
            screenHeightDp = screenHeight,
            isDragging = false,
            config = config,
        )

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            GalaxyBackground()

            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                InterpolatedPlayerContent(
                    cancion = cancionPreview,
                    estado = estado,
                    interpolatedValues = interpolatedValues,
                    progresoGlobal = config.alturaNormal,
                    puntoNormal = config.alturaNormal,
                    isDragging = false,
                    onEvento = {},
                    onExpandir = {},
                    onColapsar = {},
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Preview 3: Modo Expandido
// ──────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "Expandido",
    showBackground = true,
    backgroundColor = 0xFF050010,
    widthDp = 400,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewExpandido() {
    val screenHeight = 800.dp
    val config = PlayerGestureConfig.Default

    val estado =
        PlayerState(
            cancionActual = cancionPreview,
            estaReproduciendo = true,
            progresoActualMs = 180000L,
            modoPanel = ModoPanelReproductor.EXPANDIDO,
            letra =
                "I'm tryna put you in the worst mood, ah\nP1 cleaner than your church shoes, ah\nMilli point two just to hurt you, ah\nAll red Lamb' just to tease you, ah",
        )

    val interpolatedValues =
        PlayerInterpolator.calcular(
            progresoGlobal = 1f,
            screenHeightDp = screenHeight,
            isDragging = false,
            config = config,
        )

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            GalaxyBackground()

            InterpolatedPlayerContent(
                cancion = cancionPreview,
                estado = estado,
                interpolatedValues = interpolatedValues,
                progresoGlobal = 1f,
                puntoNormal = config.alturaNormal,
                isDragging = false,
                onEvento = {},
                onExpandir = {},
                onColapsar = {},
            )
        }
    }
}
