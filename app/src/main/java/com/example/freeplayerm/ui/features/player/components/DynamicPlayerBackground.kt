package com.example.freeplayerm.ui.features.player.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.palette.graphics.Palette
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

// ==================== CONFIGURACIÓN ====================

@Stable
object DynamicBackgroundConfig {
   const val COLOR_TRANSITION_DURATION = 800
   const val GLOW_ANIMATION_DURATION = 4000
   const val PULSE_ANIMATION_DURATION = 2000
   const val ORB_COUNT = 3
   const val BASE_ALPHA = 0.6f
   const val PLAYING_ALPHA = 0.85f
}

// ==================== MODELO DE PALETA ====================

@Immutable
data class DynamicPalette(
   val dominant: Color,
   val vibrant: Color,
   val muted: Color,
   val darkVibrant: Color,
   val lightVibrant: Color,
   val accent: Color,
) {
   companion object {
      val Default = DynamicPalette(
         dominant = AppColors.ElectricViolet.v6,
         vibrant = AppColors.ElectricViolet.v5,
         muted = AppColors.ElectricViolet.v8,
         darkVibrant = AppColors.ElectricViolet.v9,
         lightVibrant = AppColors.ElectricViolet.v4,
         accent = Color(0xFFFF6B9D),
      )
      
      val Warm = DynamicPalette(
         dominant = Color(0xFFE85D04),
         vibrant = Color(0xFFFF6B35),
         muted = Color(0xFF9D4B00),
         darkVibrant = Color(0xFF6B2D00),
         lightVibrant = Color(0xFFFFB366),
         accent = Color(0xFFFFD700),
      )
      
      val Cool = DynamicPalette(
         dominant = Color(0xFF0077B6),
         vibrant = Color(0xFF00B4D8),
         muted = Color(0xFF023E8A),
         darkVibrant = Color(0xFF001845),
         lightVibrant = Color(0xFF90E0EF),
         accent = Color(0xFF48CAE4),
      )
      
      val Neon = DynamicPalette(
         dominant = Color(0xFFBF00FF),
         vibrant = Color(0xFFFF00F5),
         muted = Color(0xFF7B00B5),
         darkVibrant = Color(0xFF3D0066),
         lightVibrant = Color(0xFFE879F9),
         accent = Color(0xFF00FFFF),
      )
   }
}

// ==================== EXTRACCIÓN DE PALETA ====================

@Stable
suspend fun extractPaletteFromBitmap(bitmap: Bitmap?): DynamicPalette {
   if (bitmap == null) return DynamicPalette.Default
   
   return withContext(Dispatchers.Default) {
      try {
         val palette = Palette.from(bitmap)
            .maximumColorCount(16)
            .generate()
         
         DynamicPalette(
            dominant = Color(palette.getDominantColor(0xFF8B5CF6.toInt())),
            vibrant = Color(palette.getVibrantColor(0xFF7C3AED.toInt())),
            muted = Color(palette.getMutedColor(0xFF6B21A8.toInt())),
            darkVibrant = Color(palette.getDarkVibrantColor(0xFF4C1D95.toInt())),
            lightVibrant = Color(palette.getLightVibrantColor(0xFFA78BFA.toInt())),
            accent = Color(palette.getLightMutedColor(0xFFFF6B9D.toInt())),
         )
      } catch (e: Exception) {
         DynamicPalette.Default
      }
   }
}

// ==================== COMPONENTE PRINCIPAL ====================

@Composable
fun DynamicPlayerBackground(
   palette: DynamicPalette,
   isPlaying: Boolean,
   modifier: Modifier = Modifier,
) {
   val isPreview = LocalInspectionMode.current
   
   // Animación de colores con transición suave
   val dominantAnimated by animateColorAsState(
      targetValue = palette.dominant,
      animationSpec = tween(DynamicBackgroundConfig.COLOR_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      label = "dominant"
   )
   val vibrantAnimated by animateColorAsState(
      targetValue = palette.vibrant,
      animationSpec = tween(DynamicBackgroundConfig.COLOR_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      label = "vibrant"
   )
   val accentAnimated by animateColorAsState(
      targetValue = palette.accent,
      animationSpec = tween(DynamicBackgroundConfig.COLOR_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      label = "accent"
   )
   val darkAnimated by animateColorAsState(
      targetValue = palette.darkVibrant,
      animationSpec = tween(DynamicBackgroundConfig.COLOR_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      label = "dark"
   )
   
   // Animación de alpha según estado de reproducción
   val targetAlpha = if (isPlaying) DynamicBackgroundConfig.PLAYING_ALPHA else DynamicBackgroundConfig.BASE_ALPHA
   val alphaAnimatable = remember { Animatable(targetAlpha) }
   
   LaunchedEffect(isPlaying) {
      alphaAnimatable.animateTo(
         targetValue = targetAlpha,
         animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
      )
   }
   
   // Transición infinita para animaciones de fondo
   val infiniteTransition = rememberInfiniteTransition(label = "bgInfinite")
   
   // Rotación de orbes
   val orbRotation by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
         animation = tween(DynamicBackgroundConfig.GLOW_ANIMATION_DURATION * 3, easing = LinearEasing),
         repeatMode = RepeatMode.Restart
      ),
      label = "orbRotation"
   )
   
   // Pulso de escala
   val pulseScale by infiniteTransition.animateFloat(
      initialValue = 0.95f,
      targetValue = 1.05f,
      animationSpec = infiniteRepeatable(
         animation = tween(DynamicBackgroundConfig.PULSE_ANIMATION_DURATION, easing = FastOutSlowInEasing),
         repeatMode = RepeatMode.Reverse
      ),
      label = "pulse"
   )
   
   // Offset de ondulación
   val waveOffset by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
         animation = tween(DynamicBackgroundConfig.GLOW_ANIMATION_DURATION, easing = LinearEasing),
         repeatMode = RepeatMode.Restart
      ),
      label = "wave"
   )
   
   Box(modifier = modifier.fillMaxSize()) {
      // Capa 1: Gradiente base oscuro
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(
               Brush.verticalGradient(
                  colors = listOf(
                     darkAnimated.copy(alpha = 0.95f),
                     Color(0xFF0A0A0F),
                     Color(0xFF050508),
                  )
               )
            )
      )
      
      // Capa 2: Orbes animados
      Canvas(modifier = Modifier.fillMaxSize()) {
         val effectiveScale = if (isPlaying) pulseScale else 1f
         val effectiveRotation = if (isPlaying) orbRotation else 0f
         
         drawAnimatedOrbs(
            colors = listOf(dominantAnimated, vibrantAnimated, accentAnimated),
            rotation = effectiveRotation,
            scale = effectiveScale,
            alpha = alphaAnimatable.value,
            waveOffset = if (isPlaying) waveOffset else 0f,
         )
      }
      
      // Capa 3: Gradiente de viñeta superior
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(
               Brush.verticalGradient(
                  colors = listOf(
                     Color.Black.copy(alpha = 0.3f),
                     Color.Transparent,
                     Color.Transparent,
                     Color.Black.copy(alpha = 0.5f),
                  ),
                  startY = 0f,
                  endY = Float.POSITIVE_INFINITY
               )
            )
      )
      
      // Capa 4: Gradiente radial central (glow del artwork)
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(
               Brush.radialGradient(
                  colors = listOf(
                     dominantAnimated.copy(alpha = 0.15f * alphaAnimatable.value),
                     Color.Transparent,
                  ),
                  center = Offset(0.5f, 0.35f),
                  radius = 800f
               )
            )
      )
   }
}

// ==================== FUNCIONES DE DIBUJO ====================

private fun DrawScope.drawAnimatedOrbs(
   colors: List<Color>,
   rotation: Float,
   scale: Float,
   alpha: Float,
   waveOffset: Float,
) {
   val centerX = size.width / 2
   val centerY = size.height / 3
   val baseRadius = size.minDimension * 0.4f
   
   colors.forEachIndexed { index, color ->
      val angleOffset = (360f / colors.size) * index
      val currentAngle = Math.toRadians((rotation + angleOffset).toDouble())
      val waveModifier = sin(waveOffset * Math.PI * 2 + index).toFloat() * 0.1f
      
      val orbX = centerX + (cos(currentAngle) * baseRadius * 0.3f * scale).toFloat()
      val orbY = centerY + (sin(currentAngle) * baseRadius * 0.2f * scale).toFloat()
      val orbRadius = baseRadius * (0.6f + waveModifier) * scale
      
      // Glow exterior
      drawCircle(
         brush = Brush.radialGradient(
            colors = listOf(
               color.copy(alpha = alpha * 0.4f),
               color.copy(alpha = alpha * 0.1f),
               Color.Transparent,
            ),
            center = Offset(orbX, orbY),
            radius = orbRadius * 1.5f
         ),
         radius = orbRadius * 1.5f,
         center = Offset(orbX, orbY)
      )
      
      // Núcleo del orbe
      drawCircle(
         brush = Brush.radialGradient(
            colors = listOf(
               color.copy(alpha = alpha * 0.6f),
               color.copy(alpha = alpha * 0.2f),
               Color.Transparent,
            ),
            center = Offset(orbX, orbY),
            radius = orbRadius
         ),
         radius = orbRadius,
         center = Offset(orbX, orbY)
      )
   }
}

// ==================== PREVIEWS ====================

private class PalettePreviewProvider : PreviewParameterProvider<Pair<DynamicPalette, Boolean>> {
   override val values = sequenceOf(
      DynamicPalette.Default to true,
      DynamicPalette.Default to false,
      DynamicPalette.Warm to true,
      DynamicPalette.Cool to true,
      DynamicPalette.Neon to true,
   )
}

@Preview(name = "🌙 Dynamic BG - Estados", showBackground = true, heightDp = 400)
@Composable
private fun PreviewDynamicBackground(
   @PreviewParameter(PalettePreviewProvider::class) params: Pair<DynamicPalette, Boolean>
) {
   FreePlayerMTheme(darkTheme = true) {
      DynamicPlayerBackground(
         palette = params.first,
         isPlaying = params.second,
      )
   }
}

@Preview(name = "🎵 Playing State", showBackground = true, heightDp = 600)
@Composable
private fun PreviewPlayingState() {
   FreePlayerMTheme(darkTheme = true) {
      DynamicPlayerBackground(
         palette = DynamicPalette.Neon,
         isPlaying = true,
      )
   }
}

@Preview(name = "⏸️ Paused State", showBackground = true, heightDp = 600)
@Composable
private fun PreviewPausedState() {
   FreePlayerMTheme(darkTheme = true) {
      DynamicPlayerBackground(
         palette = DynamicPalette.Default,
         isPlaying = false,
      )
   }
}