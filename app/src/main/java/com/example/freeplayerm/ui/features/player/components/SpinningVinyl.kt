package com.example.freeplayerm.ui.features.player.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ═══════════════════════════════════════════════════════════════════════════
// 🎵 VINILO GIRATORIO REALISTA - ESCALADO PROPORCIONAL
// ═══════════════════════════════════════════════════════════════════════════


private object VinylTokens {
    // Proporciones (relativas al radio total)
    const val LABEL_RATIO = 0.36f
    const val SPINDLE_HOLE_RATIO = 0.025f
    const val OUTER_EDGE_RATIO = 0.98f
    const val DEAD_WAX_INNER_RATIO = 0.38f
    const val DEAD_WAX_OUTER_RATIO = 0.42f
    const val GROOVE_START_RATIO = 0.42f
    const val GROOVE_END_RATIO = 0.94f
    const val LEAD_IN_RATIO = 0.94f

    // ══════════════════════════════════════════════════════════════════════
    // NUEVOS TOKENS PROPORCIONALES (relativos al radio)
    // Estos valores se multiplican por el radio para obtener el tamaño real
    // ══════════════════════════════════════════════════════════════════════

    // Grosores de línea como proporción del radio
    const val OUTER_RIM_STROKE_RATIO = 0.012f // ~3dp en 250dp
    const val GROOVE_STROKE_THICK_RATIO = 0.005f // Surcos marcados
    const val GROOVE_STROKE_MEDIUM_RATIO = 0.0032f // Surcos medios
    const val GROOVE_STROKE_THIN_RATIO = 0.002f // Surcos sutiles
    const val LEAD_IN_STROKE_RATIO = 0.003f // Lead-in groove
    const val LEAD_IN_SPACING_RATIO = 0.016f // Espaciado lead-in
    const val DEAD_WAX_STROKE_RATIO = 0.004f // Dead wax stroke
    const val DEAD_WAX_SUBTLE_RATIO = 0.0012f // Surcos sutiles dead wax
    const val LABEL_BORDER_RATIO = 0.008f // Borde etiqueta
    const val SPINDLE_OUTER_RING_RATIO = 0.15f // Anillo exterior spindle
    const val SPINDLE_INNER_RING_RATIO = 0.08f // Anillo interior spindle

    // Colores del vinilo
    val VinylBase = Color(0xFF0D0D0D)
    val VinylDark = Color(0xFF080808)
    val VinylMid = Color(0xFF1A1A1A)
    val VinylHighlight = Color(0xFF2D2D2D)
    val VinylShine = Color(0xFF3A3A3A)
    val VinylReflection = Color(0xFF4A4A4A)

    // Colores de la etiqueta
    val LabelDefault = Color(0xFFF5F5F0)
    val LabelBorder = Color(0xFFE0E0DA)

    // Spindle hole
    val SpindleHole = Color(0xFF050505)
    val SpindleRing = Color(0xFF3A3A3A)

    // Animación
    const val ROTATION_DURATION_MS = 1800
}

// ───────────────────────────────────────────────────────────────────────────
// COMPONENTE PRINCIPAL
// ───────────────────────────────────────────────────────────────────────────

/**
 * 🎵 Vinilo Giratorio Realista con Escalado Proporcional
 *
 * Todos los elementos visuales (surcos, bordes, reflejos) escalan proporcionalmente con el tamaño
 * del componente.
 */
@Composable
fun SpinningVinyl(
    portadaPath: String?,
    estaReproduciendo: Boolean,
    modifier: Modifier = Modifier,
    cacheKey: String? = null,
    contentDescription: String = "Disco de vinilo",
) {
    // Animatable mantiene el estado de rotación entre pausas
    val rotationAnimatable = remember { Animatable(0f) }

    // Controla la animación basándose en el estado de reproducción
    LaunchedEffect(estaReproduciendo) {
        if (estaReproduciendo) {
            // Ciclo infinito mientras reproduce
            while (true) {
                // Calcula el ángulo restante para completar la vuelta actual
                val currentAngle = rotationAnimatable.value % 360f
                val remainingAngle = 360f - currentAngle
                val remainingDuration = (remainingAngle / 360f * VinylTokens.ROTATION_DURATION_MS).toInt()

                // Completa la vuelta actual
                rotationAnimatable.animateTo(
                    targetValue = rotationAnimatable.value + remainingAngle,
                    animationSpec = tween(
                        durationMillis = remainingDuration.coerceAtLeast(1),
                        easing = LinearEasing,
                    ),
                )

                // Normaliza para evitar overflow en reproducciones largas
                rotationAnimatable.snapTo(rotationAnimatable.value % 360f)

                // Vuelta completa siguiente
                rotationAnimatable.animateTo(
                    targetValue = rotationAnimatable.value + 360f,
                    animationSpec = tween(
                        durationMillis = VinylTokens.ROTATION_DURATION_MS,
                        easing = LinearEasing,
                    ),
                )
            }
        }
        // Cuando estaReproduciendo = false, el LaunchedEffect se cancela
        // y rotationAnimatable mantiene su último valor
    }

    // Usa el valor actual (se mantiene al pausar)
    val rotation = rotationAnimatable.value

    Box(
        modifier = modifier.aspectRatio(1f).graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center,
    ) {
        // Capa 1: Disco de vinilo completo
        VinylDiscCanvas(modifier = Modifier.fillMaxSize())

        // Capa 2: Etiqueta circular con portada
        CircularLabel(
            portadaPath = portadaPath,
            cacheKey = cacheKey,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(VinylTokens.LABEL_RATIO),
        )

        // Capa 3: Agujero del spindle
        SpindleHole(modifier = Modifier.fillMaxSize(VinylTokens.SPINDLE_HOLE_RATIO))
    }
}

// ───────────────────────────────────────────────────────────────────────────
// DISCO DE VINILO (Canvas) - ESCALADO PROPORCIONAL
// ───────────────────────────────────────────────────────────────────────────

@Composable
private fun VinylDiscCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        drawVinylBase(center, radius)
        drawOuterRim(center, radius)
        drawLeadInGroove(center, radius)
        drawMusicGrooves(center, radius)
        drawDeadWax(center, radius)
        drawRealisticReflections(center, radius)
    }
}

private fun DrawScope.drawVinylBase(center: Offset, radius: Float) {
    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0.0f to VinylTokens.VinylMid,
                        0.3f to VinylTokens.VinylBase,
                        0.6f to VinylTokens.VinylDark,
                        0.85f to VinylTokens.VinylBase,
                        1.0f to VinylTokens.VinylMid,
                    ),
                center = center,
                radius = radius,
            ),
        radius = radius * VinylTokens.OUTER_EDGE_RATIO,
        center = center,
    )
}

private fun DrawScope.drawOuterRim(center: Offset, radius: Float) {
    // ✅ CORREGIDO: strokeWidth proporcional al radio
    val strokeWidth = radius * VinylTokens.OUTER_RIM_STROKE_RATIO

    drawCircle(
        brush =
            Brush.sweepGradient(
                colorStops =
                    arrayOf(
                        0.0f to VinylTokens.VinylHighlight,
                        0.25f to VinylTokens.VinylBase,
                        0.5f to VinylTokens.VinylShine,
                        0.75f to VinylTokens.VinylBase,
                        1.0f to VinylTokens.VinylHighlight,
                    ),
                center = center,
            ),
        radius = radius * VinylTokens.OUTER_EDGE_RATIO,
        center = center,
        style = Stroke(width = strokeWidth),
    )
}

private fun DrawScope.drawLeadInGroove(center: Offset, radius: Float) {
    val leadInRadius = radius * VinylTokens.LEAD_IN_RATIO
    // ✅ CORREGIDO: espaciado y stroke proporcionales
    val spacing = radius * VinylTokens.LEAD_IN_SPACING_RATIO
    val strokeWidth = radius * VinylTokens.LEAD_IN_STROKE_RATIO

    for (i in 0..3) {
        val r = leadInRadius + (i * spacing)
        // Solo dibujar si el surco está dentro del disco
        if (r <= radius * VinylTokens.OUTER_EDGE_RATIO) {
            drawCircle(
                color = VinylTokens.VinylHighlight.copy(alpha = 0.3f),
                radius = r,
                center = center,
                style = Stroke(width = strokeWidth),
            )
        }
    }
}

private fun DrawScope.drawMusicGrooves(center: Offset, radius: Float) {
    val innerRadius = radius * VinylTokens.GROOVE_START_RATIO
    val outerRadius = radius * VinylTokens.GROOVE_END_RATIO

    // ✅ CORREGIDO: Cantidad de surcos proporcional al espacio disponible
    // Esto mantiene la densidad visual consistente en cualquier tamaño
    val grooveAreaSize = outerRadius - innerRadius
    val baseGrooveSpacing = radius * 0.008f // Espaciado base proporcional
    val grooveCount = (grooveAreaSize / baseGrooveSpacing).toInt().coerceIn(20, 80)

    val radiusStep = grooveAreaSize / grooveCount

    // ✅ CORREGIDO: strokeWidths proporcionales al radio
    val strokeThick = radius * VinylTokens.GROOVE_STROKE_THICK_RATIO
    val strokeMedium = radius * VinylTokens.GROOVE_STROKE_MEDIUM_RATIO
    val strokeThin = radius * VinylTokens.GROOVE_STROKE_THIN_RATIO

    for (i in 0 until grooveCount) {
        val currentRadius = innerRadius + (radiusStep * i)

        val baseAlpha =
            when {
                i % 7 == 0 -> 0.25f
                i % 3 == 0 -> 0.15f
                else -> 0.08f
            }

        val strokeWidth =
            when {
                i % 7 == 0 -> strokeThick
                i % 3 == 0 -> strokeMedium
                else -> strokeThin
            }

        drawCircle(
            color = VinylTokens.VinylHighlight.copy(alpha = baseAlpha),
            radius = currentRadius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawDeadWax(center: Offset, radius: Float) {
    val innerRadius = radius * VinylTokens.DEAD_WAX_INNER_RATIO
    val outerRadius = radius * VinylTokens.DEAD_WAX_OUTER_RATIO
    // ✅ CORREGIDO: strokes proporcionales
    val mainStroke = radius * VinylTokens.DEAD_WAX_STROKE_RATIO
    val subtleStroke = radius * VinylTokens.DEAD_WAX_SUBTLE_RATIO

    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        VinylTokens.VinylMid,
                        VinylTokens.VinylBase,
                        VinylTokens.VinylMid.copy(alpha = 0.8f),
                    ),
                center = center,
                radius = outerRadius,
            ),
        radius = outerRadius,
        center = center,
    )

    drawCircle(
        color = VinylTokens.VinylHighlight.copy(alpha = 0.2f),
        radius = outerRadius,
        center = center,
        style = Stroke(width = mainStroke),
    )

    for (i in 0..4) {
        val r = innerRadius + ((outerRadius - innerRadius) / 5 * i)
        drawCircle(
            color = VinylTokens.VinylHighlight.copy(alpha = 0.1f),
            radius = r,
            center = center,
            style = Stroke(width = subtleStroke),
        )
    }
}

private fun DrawScope.drawRealisticReflections(center: Offset, radius: Float) {
    val reflectionOffset = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f)

    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0.0f to Color.White.copy(alpha = 0.08f),
                        0.5f to Color.White.copy(alpha = 0.03f),
                        1.0f to Color.Transparent,
                    ),
                center = reflectionOffset,
                radius = radius * 0.6f,
            ),
        radius = radius * 0.6f,
        center = reflectionOffset,
    )

    drawArc(
        brush =
            Brush.sweepGradient(
                colorStops =
                    arrayOf(
                        0.0f to Color.Transparent,
                        0.08f to VinylTokens.VinylReflection.copy(alpha = 0.15f),
                        0.12f to VinylTokens.VinylShine.copy(alpha = 0.25f),
                        0.16f to VinylTokens.VinylReflection.copy(alpha = 0.15f),
                        0.24f to Color.Transparent,
                        1.0f to Color.Transparent,
                    ),
                center = center,
            ),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
    )

    val reflection2Offset = Offset(center.x + radius * 0.3f, center.y + radius * 0.3f)
    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0.0f to Color.White.copy(alpha = 0.04f),
                        0.7f to Color.Transparent,
                        1.0f to Color.Transparent,
                    ),
                center = reflection2Offset,
                radius = radius * 0.3f,
            ),
        radius = radius * 0.3f,
        center = reflection2Offset,
    )
}

// ───────────────────────────────────────────────────────────────────────────
// ETIQUETA CIRCULAR
// ───────────────────────────────────────────────────────────────────────────

@Composable
private fun CircularLabel(
    portadaPath: String?,
    cacheKey: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current

    Box(
        modifier =
            modifier.clip(CircleShape).background(VinylTokens.LabelDefault).drawBehind {
                // ✅ CORREGIDO: borde proporcional
                val borderStroke = size.minDimension * VinylTokens.LABEL_BORDER_RATIO * 0.5f
                drawCircle(color = VinylTokens.LabelBorder, style = Stroke(width = borderStroke))
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to Color.Transparent,
                                    0.85f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.1f),
                                )
                        )
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            isPreview && !portadaPath.isNullOrBlank() -> PreviewLabelContent()
            !portadaPath.isNullOrBlank() -> {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(portadaPath)
                            .crossfade(300)
                            .apply { cacheKey?.let { memoryCacheKey(it) } }
                            .build(),
                    contentDescription = "Portada: $contentDescription",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> DefaultLabelContent()
        }
    }
}

@Composable
private fun DefaultLabelContent() {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    brush =
                        Brush.radialGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to VinylTokens.LabelDefault,
                                    0.7f to VinylTokens.LabelDefault,
                                    1.0f to VinylTokens.LabelBorder,
                                )
                        )
                ),
        contentAlignment = Alignment.Center,
    ) {}
}

@Composable
private fun PreviewLabelContent() {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    AppColors.ElectricViolet.v5,
                                    AppColors.ElectricViolet.v7,
                                    AppColors.ElectricViolet.v6,
                                )
                        )
                ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            // ✅ CORREGIDO: stroke proporcional
            val decorStroke = size.minDimension * 0.01f

            for (i in 1..3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = size.minDimension / 2 * (0.3f + i * 0.2f),
                    center = center,
                    style = Stroke(width = decorStroke),
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// AGUJERO DEL SPINDLE
// ───────────────────────────────────────────────────────────────────────────

@Composable
private fun SpindleHole(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.clip(CircleShape).background(VinylTokens.SpindleHole).drawBehind {
                val radius = size.minDimension / 2
                // ✅ CORREGIDO: strokes proporcionales al tamaño del spindle
                val outerRingStroke = radius * VinylTokens.SPINDLE_OUTER_RING_RATIO
                val innerRingStroke = radius * VinylTokens.SPINDLE_INNER_RING_RATIO

                drawCircle(
                    brush =
                        Brush.sweepGradient(
                            colors =
                                listOf(
                                    VinylTokens.SpindleRing,
                                    Color(0xFF2A2A2A),
                                    VinylTokens.SpindleRing.copy(alpha = 0.8f),
                                    Color(0xFF1A1A1A),
                                    VinylTokens.SpindleRing,
                                )
                        ),
                    style = Stroke(width = outerRingStroke),
                )
                drawCircle(
                    color = Color.Black,
                    radius = radius - outerRingStroke,
                    style = Stroke(width = innerRingStroke),
                )
            }
    )
}

// ───────────────────────────────────────────────────────────────────────────
// SOBRECARGA CON SongWithArtist
// ───────────────────────────────────────────────────────────────────────────

@Composable
fun SpinningVinyl(
    cancion: SongWithArtist,
    estaReproduciendo: Boolean,
    modifier: Modifier = Modifier,
) {
    SpinningVinyl(
        portadaPath = cancion.portadaPath,
        estaReproduciendo = estaReproduciendo,
        cacheKey = "vinilo_${cancion.cancion.idCancion}",
        contentDescription = cancion.cancion.titulo,
        modifier = modifier,
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// 🎨 PREVIEWS EXHAUSTIVAS
// ═══════════════════════════════════════════════════════════════════════════

data class VinylPreviewState(
    val nombre: String,
    val portadaPath: String?,
    val estaReproduciendo: Boolean,
)

class VinylPreviewProvider : PreviewParameterProvider<VinylPreviewState> {
    override val values =
        sequenceOf(
            VinylPreviewState("▶️ Reproduciendo + Portada", "fake/album/cover.jpg", true),
            VinylPreviewState("⏸️ Pausado + Portada", "fake/album/cover.jpg", false),
            VinylPreviewState("▶️ Reproduciendo (Sin Portada)", null, true),
            VinylPreviewState("⏸️ Pausado (Sin Portada)", null, false),
        )
}

@Preview(
    name = "🌙 Dark - Estados",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 280,
    heightDp = 340,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylDarkPreview(
    @PreviewParameter(VinylPreviewProvider::class) state: VinylPreviewState
) {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                state.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SpinningVinyl(
                portadaPath = state.portadaPath,
                estaReproduciendo = state.estaReproduciendo,
                contentDescription = state.nombre,
                modifier = Modifier.size(240.dp),
            )
        }
    }
}

@Preview(
    name = "☀️ Light - Estados",
    showBackground = true,
    backgroundColor = 0xFFF3EEFF,
    widthDp = 280,
    heightDp = 340,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun VinylLightPreview(
    @PreviewParameter(VinylPreviewProvider::class) state: VinylPreviewState
) {
    FreePlayerMTheme(darkTheme = false) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                state.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SpinningVinyl(
                portadaPath = state.portadaPath,
                estaReproduciendo = state.estaReproduciendo,
                contentDescription = state.nombre,
                modifier = Modifier.size(240.dp),
            )
        }
    }
}

@Preview(
    name = "🔄 Comparación Estados",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 720,
    heightDp = 220,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylComparisonPreview() {
    FreePlayerMTheme(darkTheme = true) {
        Row(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VinylStateItem("▶️ Play", "cover.jpg", true)
            VinylStateItem("⏸️ Pausa", "cover.jpg", false)
            VinylStateItem("🎵 Sin Portada", null, true)
            VinylStateItem("💿 Idle", null, false)
        }
    }
}

@Composable
private fun VinylStateItem(label: String, portadaPath: String?, estaReproduciendo: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SpinningVinyl(
            portadaPath = portadaPath,
            estaReproduciendo = estaReproduciendo,
            contentDescription = label,
            modifier = Modifier.size(150.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 📐 PREVIEW CRÍTICA: ESCALAS DE TAMAÑO (Verifica el fix)
// ═══════════════════════════════════════════════════════════════════════════

@Preview(
    name = "📐 Escalas de Tamaño (TEST FIX)",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 600,
    heightDp = 220,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylSizePreview() {
    FreePlayerMTheme(darkTheme = true) {
        Row(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ✅ Esta preview verifica que los surcos escalen correctamente
            listOf(32 to "32dp", 64 to "64dp", 100 to "100dp", 150 to "150dp", 180 to "180dp")
                .forEach { (size, label) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SpinningVinyl(
                            portadaPath = null,
                            estaReproduciendo = false,
                            modifier = Modifier.size(size.dp),
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                        )
                    }
                }
        }
    }
}

@Preview(
    name = "🔬 Micro vs Macro",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 400,
    heightDp = 350,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylMicroMacroPreview() {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                "Comparación extrema de escalas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpinningVinyl(
                        portadaPath = "cover.jpg",
                        estaReproduciendo = true,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        "40dp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpinningVinyl(
                        portadaPath = "cover.jpg",
                        estaReproduciendo = true,
                        modifier = Modifier.size(250.dp),
                    )
                    Text(
                        "250dp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Preview(
    name = "📱 Player Context",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 360,
    heightDp = 700,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylPlayerContextPreview() {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        AppColors.ElectricViolet.v10,
                                        AppColors.ElectricViolet.v16,
                                    )
                            )
                    )
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "NOW PLAYING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
            )
            SpinningVinyl(
                portadaPath = "cover.jpg",
                estaReproduciendo = true,
                contentDescription = "Album Cover",
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            )
            Text(
                "Bohemian Rhapsody",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Queen • A Night at the Opera",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }
    }
}

@Preview(
    name = "💻 Tablet Layout",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 840,
    heightDp = 500,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=840dp,height=500dp,dpi=240",
)
@Composable
private fun VinylTabletPreview() {
    FreePlayerMTheme(darkTheme = true) {
        Row(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(40.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpinningVinyl(
                portadaPath = "cover.jpg",
                estaReproduciendo = true,
                contentDescription = "Album Cover",
                modifier = Modifier.size(360.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "A Night at the Opera",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Queen • 1975",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
                Text(
                    "Track 11 of 12 • 5:55",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
@Preview(
    name = "⏯️ Pause/Resume Test",
    showBackground = true,
    backgroundColor = 0xFF05000C,
    widthDp = 300,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun VinylPauseResumePreview() {
    FreePlayerMTheme(darkTheme = true) {
        var isPlaying by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = if (isPlaying) "▶️ Reproduciendo" else "⏸️ Pausado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            SpinningVinyl(
                portadaPath = "cover.jpg",
                estaReproduciendo = isPlaying,
                contentDescription = "Test vinyl",
                modifier = Modifier.size(200.dp),
            )

            androidx.compose.material3.Button(
                onClick = { isPlaying = !isPlaying }
            ) {
                Text(if (isPlaying) "Pausar" else "Reanudar")
            }

            Text(
                text = "El disco debe mantener su posición al pausar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}