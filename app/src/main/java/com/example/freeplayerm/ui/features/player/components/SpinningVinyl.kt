package com.example.freeplayerm.ui.features.player.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
// CONFIGURACIÓN
// ═══════════════════════════════════════════════════════════════════════════

@Stable
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
   
   // Grosores proporcionales al radio
   const val OUTER_RIM_STROKE_RATIO = 0.012f
   const val GROOVE_STROKE_THICK_RATIO = 0.005f
   const val GROOVE_STROKE_MEDIUM_RATIO = 0.0032f
   const val GROOVE_STROKE_THIN_RATIO = 0.002f
   const val LEAD_IN_STROKE_RATIO = 0.003f
   const val LEAD_IN_SPACING_RATIO = 0.016f
   const val DEAD_WAX_STROKE_RATIO = 0.004f
   const val DEAD_WAX_SUBTLE_RATIO = 0.0012f
   const val LABEL_BORDER_RATIO = 0.008f
   const val SPINDLE_OUTER_RING_RATIO = 0.15f
   const val SPINDLE_INNER_RING_RATIO = 0.08f
   
   // Animación
   const val ROTATION_DURATION_MS = 1800
   const val FULL_ROTATION = 360f
}

@Immutable
private object VinylColors {
   val Base = Color(0xFF0D0D0D)
   val Dark = Color(0xFF080808)
   val Mid = Color(0xFF1A1A1A)
   val Highlight = Color(0xFF2D2D2D)
   val Shine = Color(0xFF3A3A3A)
   val Reflection = Color(0xFF4A4A4A)
   
   val LabelDefault = Color(0xFFF5F5F0)
   val LabelBorder = Color(0xFFE0E0DA)
   
   val SpindleHole = Color(0xFF050505)
   val SpindleRing = Color(0xFF3A3A3A)
}

// ═══════════════════════════════════════════════════════════════════════════
// COMPONENTE PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun SpinningVinyl(
   portadaPath: String?,
   estaReproduciendo: Boolean,
   modifier: Modifier = Modifier,
   cacheKey: String? = null,
   contentDescription: String = "Disco de vinilo",
) {
   val rotationAnimatable = remember { Animatable(0f) }
   
   LaunchedEffect(estaReproduciendo) {
      if (estaReproduciendo) {
         rotateIndefinitely(rotationAnimatable)
      }
      // Cuando estaReproduciendo = false, se cancela y mantiene posición
   }
   
   Box(
      modifier = modifier
         .aspectRatio(1f)
         .graphicsLayer { rotationZ = rotationAnimatable.value },
      contentAlignment = Alignment.Center,
   ) {
      VinylDiscCanvas(modifier = Modifier.fillMaxSize())
      
      CircularLabel(
         portadaPath = portadaPath,
         cacheKey = cacheKey,
         contentDescription = contentDescription,
         modifier = Modifier.fillMaxSize(VinylTokens.LABEL_RATIO),
      )
      
      SpindleHole(modifier = Modifier.fillMaxSize(VinylTokens.SPINDLE_HOLE_RATIO))
   }
}

/**
 * Sobrecarga conveniente con SongWithArtist
 */
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
// ANIMACIÓN
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Función suspendida que rota indefinidamente el vinilo.
 * Extraída para simplificar el LaunchedEffect y facilitar testing.
 */
private suspend fun rotateIndefinitely(animatable: Animatable<Float, *>) {
   while (true) {
      // Completa la vuelta actual
      val currentAngle = animatable.value % VinylTokens.FULL_ROTATION
      val remainingAngle = VinylTokens.FULL_ROTATION - currentAngle
      val remainingDuration = calculateDuration(remainingAngle)
      
      animatable.animateTo(
         targetValue = animatable.value + remainingAngle,
         animationSpec = tween(remainingDuration, easing = LinearEasing),
      )
      
      // Normaliza para evitar overflow
      animatable.snapTo(animatable.value % VinylTokens.FULL_ROTATION)
      
      // Vuelta completa
      animatable.animateTo(
         targetValue = animatable.value + VinylTokens.FULL_ROTATION,
         animationSpec = tween(VinylTokens.ROTATION_DURATION_MS, easing = LinearEasing),
      )
   }
}

private fun calculateDuration(angle: Float): Int =
   (angle / VinylTokens.FULL_ROTATION * VinylTokens.ROTATION_DURATION_MS)
      .toInt()
      .coerceAtLeast(1)

// ═══════════════════════════════════════════════════════════════════════════
// CANVAS - DISCO DE VINILO
// ═══════════════════════════════════════════════════════════════════════════

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
      drawReflections(center, radius)
   }
}

private fun DrawScope.drawVinylBase(center: Offset, radius: Float) {
   drawCircle(
      brush = Brush.radialGradient(
         colorStops = arrayOf(
            0.0f to VinylColors.Mid,
            0.3f to VinylColors.Base,
            0.6f to VinylColors.Dark,
            0.85f to VinylColors.Base,
            1.0f to VinylColors.Mid,
         ),
         center = center,
         radius = radius,
      ),
      radius = radius * VinylTokens.OUTER_EDGE_RATIO,
      center = center,
   )
}

private fun DrawScope.drawOuterRim(center: Offset, radius: Float) {
   val strokeWidth = radius * VinylTokens.OUTER_RIM_STROKE_RATIO
   
   drawCircle(
      brush = Brush.sweepGradient(
         colorStops = arrayOf(
            0.0f to VinylColors.Highlight,
            0.25f to VinylColors.Base,
            0.5f to VinylColors.Shine,
            0.75f to VinylColors.Base,
            1.0f to VinylColors.Highlight,
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
   val spacing = radius * VinylTokens.LEAD_IN_SPACING_RATIO
   val strokeWidth = radius * VinylTokens.LEAD_IN_STROKE_RATIO
   val maxRadius = radius * VinylTokens.OUTER_EDGE_RATIO
   
   for (i in 0..3) {
      val r = leadInRadius + (i * spacing)
      if (r <= maxRadius) {
         drawCircle(
            color = VinylColors.Highlight.copy(alpha = 0.3f),
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
   val grooveAreaSize = outerRadius - innerRadius
   
   // Cantidad de surcos proporcional al espacio
   val baseGrooveSpacing = radius * 0.008f
   val grooveCount = (grooveAreaSize / baseGrooveSpacing).toInt().coerceIn(20, 80)
   val radiusStep = grooveAreaSize / grooveCount
   
   // Strokes proporcionales
   val strokeThick = radius * VinylTokens.GROOVE_STROKE_THICK_RATIO
   val strokeMedium = radius * VinylTokens.GROOVE_STROKE_MEDIUM_RATIO
   val strokeThin = radius * VinylTokens.GROOVE_STROKE_THIN_RATIO
   
   for (i in 0 until grooveCount) {
      val currentRadius = innerRadius + (radiusStep * i)
      val (alpha, strokeWidth) = getGrooveStyle(i, strokeThick, strokeMedium, strokeThin)
      
      drawCircle(
         color = VinylColors.Highlight.copy(alpha = alpha),
         radius = currentRadius,
         center = center,
         style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )
   }
}

private fun getGrooveStyle(
   index: Int,
   thick: Float,
   medium: Float,
   thin: Float,
): Pair<Float, Float> = when {
   index % 7 == 0 -> 0.25f to thick
   index % 3 == 0 -> 0.15f to medium
   else -> 0.08f to thin
}

private fun DrawScope.drawDeadWax(center: Offset, radius: Float) {
   val innerRadius = radius * VinylTokens.DEAD_WAX_INNER_RATIO
   val outerRadius = radius * VinylTokens.DEAD_WAX_OUTER_RATIO
   val mainStroke = radius * VinylTokens.DEAD_WAX_STROKE_RATIO
   val subtleStroke = radius * VinylTokens.DEAD_WAX_SUBTLE_RATIO
   
   // Fondo del dead wax
   drawCircle(
      brush = Brush.radialGradient(
         colors = listOf(
            VinylColors.Mid,
            VinylColors.Base,
            VinylColors.Mid.copy(alpha = 0.8f),
         ),
         center = center,
         radius = outerRadius,
      ),
      radius = outerRadius,
      center = center,
   )
   
   // Borde exterior
   drawCircle(
      color = VinylColors.Highlight.copy(alpha = 0.2f),
      radius = outerRadius,
      center = center,
      style = Stroke(width = mainStroke),
   )
   
   // Surcos sutiles
   val radiusRange = outerRadius - innerRadius
   for (i in 0..4) {
      val r = innerRadius + (radiusRange / 5 * i)
      drawCircle(
         color = VinylColors.Highlight.copy(alpha = 0.1f),
         radius = r,
         center = center,
         style = Stroke(width = subtleStroke),
      )
   }
}

private fun DrawScope.drawReflections(center: Offset, radius: Float) {
   // Reflejo principal (superior izquierdo)
   val mainReflectionOffset = Offset(
      center.x - radius * 0.25f,
      center.y - radius * 0.25f,
   )
   
   drawCircle(
      brush = Brush.radialGradient(
         colorStops = arrayOf(
            0.0f to Color.White.copy(alpha = 0.08f),
            0.5f to Color.White.copy(alpha = 0.03f),
            1.0f to Color.Transparent,
         ),
         center = mainReflectionOffset,
         radius = radius * 0.6f,
      ),
      radius = radius * 0.6f,
      center = mainReflectionOffset,
   )
   
   // Reflejo de barrido (efecto iridiscente)
   drawArc(
      brush = Brush.sweepGradient(
         colorStops = arrayOf(
            0.0f to Color.Transparent,
            0.08f to VinylColors.Reflection.copy(alpha = 0.15f),
            0.12f to VinylColors.Shine.copy(alpha = 0.25f),
            0.16f to VinylColors.Reflection.copy(alpha = 0.15f),
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
   
   // Reflejo secundario (inferior derecho)
   val secondaryReflectionOffset = Offset(
      center.x + radius * 0.3f,
      center.y + radius * 0.3f,
   )
   
   drawCircle(
      brush = Brush.radialGradient(
         colorStops = arrayOf(
            0.0f to Color.White.copy(alpha = 0.04f),
            0.7f to Color.Transparent,
            1.0f to Color.Transparent,
         ),
         center = secondaryReflectionOffset,
         radius = radius * 0.3f,
      ),
      radius = radius * 0.3f,
      center = secondaryReflectionOffset,
   )
}

// ═══════════════════════════════════════════════════════════════════════════
// ETIQUETA CIRCULAR
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CircularLabel(
   portadaPath: String?,
   cacheKey: String?,
   contentDescription: String,
   modifier: Modifier = Modifier,
) {
   val isPreview = LocalInspectionMode.current
   
   Box(
      modifier = modifier
         .clip(CircleShape)
         .background(VinylColors.LabelDefault)
         .drawBehind {
            val borderStroke = size.minDimension * VinylTokens.LABEL_BORDER_RATIO * 0.5f
            drawCircle(
               color = VinylColors.LabelBorder,
               style = Stroke(width = borderStroke),
            )
            drawCircle(
               brush = Brush.radialGradient(
                  colorStops = arrayOf(
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
         !portadaPath.isNullOrBlank() -> LabelImage(portadaPath, cacheKey, contentDescription)
         else -> DefaultLabelContent()
      }
   }
}

@Composable
private fun LabelImage(
   portadaPath: String,
   cacheKey: String?,
   contentDescription: String,
) {
   val context = LocalContext.current
   val imageRequest = remember(portadaPath, cacheKey) {
      ImageRequest.Builder(context)
         .data(portadaPath)
         .crossfade(300)
         .apply { cacheKey?.let { memoryCacheKey(it) } }
         .build()
   }
   
   AsyncImage(
      model = imageRequest,
      contentDescription = "Portada: $contentDescription",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
   )
}

@Composable
private fun DefaultLabelContent() {
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            brush = Brush.radialGradient(
               colorStops = arrayOf(
                  0.0f to VinylColors.LabelDefault,
                  0.7f to VinylColors.LabelDefault,
                  1.0f to VinylColors.LabelBorder,
               )
            )
         ),
   )
}

@Composable
private fun PreviewLabelContent() {
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            brush = Brush.linearGradient(
               colors = listOf(
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

// ═══════════════════════════════════════════════════════════════════════════
// SPINDLE HOLE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SpindleHole(modifier: Modifier = Modifier) {
   Box(
      modifier = modifier
         .clip(CircleShape)
         .background(VinylColors.SpindleHole)
         .drawBehind {
            val radius = size.minDimension / 2
            val outerRingStroke = radius * VinylTokens.SPINDLE_OUTER_RING_RATIO
            val innerRingStroke = radius * VinylTokens.SPINDLE_INNER_RING_RATIO
            
            drawCircle(
               brush = Brush.sweepGradient(
                  colors = listOf(
                     VinylColors.SpindleRing,
                     Color(0xFF2A2A2A),
                     VinylColors.SpindleRing.copy(alpha = 0.8f),
                     Color(0xFF1A1A1A),
                     VinylColors.SpindleRing,
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

// ═══════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════

@Immutable
private data class VinylPreviewState(
   val nombre: String,
   val portadaPath: String?,
   val estaReproduciendo: Boolean,
)

private class VinylPreviewProvider : PreviewParameterProvider<VinylPreviewState> {
   override val values = sequenceOf(
      VinylPreviewState("▶️ Reproduciendo", "fake/cover.jpg", true),
      VinylPreviewState("⏸️ Pausado", "fake/cover.jpg", false),
      VinylPreviewState("🎵 Sin Portada", null, true),
   )
}

@Preview(name = "Estados", showBackground = true, backgroundColor = 0xFF05000C)
@Composable
private fun VinylStatesPreview(
   @PreviewParameter(VinylPreviewProvider::class) state: VinylPreviewState
) {
   FreePlayerMTheme(darkTheme = true) {
      Column(
         modifier = Modifier
            .fillMaxSize()
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
            modifier = Modifier.size(200.dp),
         )
      }
   }
}

@Preview(name = "Escalas", showBackground = true, backgroundColor = 0xFF05000C, widthDp = 500)
@Composable
private fun VinylSizesPreview() {
   FreePlayerMTheme(darkTheme = true) {
      Row(
         modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
         horizontalArrangement = Arrangement.SpaceEvenly,
         verticalAlignment = Alignment.CenterVertically,
      ) {
         listOf(48, 80, 120, 160).forEach { size ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
               SpinningVinyl(
                  portadaPath = null,
                  estaReproduciendo = false,
                  modifier = Modifier.size(size.dp),
               )
               Text(
                  "${size}dp",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
               )
            }
         }
      }
   }
}

@Preview(name = "Pause/Resume", showBackground = true, backgroundColor = 0xFF05000C)
@Composable
private fun VinylInteractivePreview() {
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
            modifier = Modifier.size(200.dp),
         )
         
         Button(onClick = { isPlaying = !isPlaying }) {
            Text(if (isPlaying) "Pausar" else "Reanudar")
         }
      }
   }
}