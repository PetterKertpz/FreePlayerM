package com.example.freeplayerm.ui.features.player.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.formatAsTime
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.example.freeplayerm.ui.theme.FreePlayerTheme
import com.example.freeplayerm.ui.theme.FreePlayerTypography
import kotlin.math.roundToInt

// ==================== CONFIGURACIÓN ====================

@Stable
private object SliderConfig {
   // Slider completo
   val trackHeightNormal = 6.dp
   val trackHeightActive = 8.dp
   val thumbSizeNormal = 18.dp
   val thumbSizeActive = 24.dp
   val glowRadius = 5.dp
   
   // Slider compacto
   val compactTrackHeight = 4.dp
   val compactTrackActive = 5.dp
   val compactThumbNormal = 12.dp
   val compactThumbActive = 14.dp
   
   // Comunes
   val thumbTouchTarget = 30.dp
   val tooltipWidth = 56.dp
   val tooltipHeight = 32.dp
   val timeWidth = 42.dp
   val tooltipSpacing = 8.dp
   
   // Animación
   const val SPRING_DAMPING = 0.7f
   val SPRING_STIFFNESS = Spring.StiffnessMedium
}

@Immutable
private data class SliderColors(
   val trackActiveStart: Color,
   val trackActiveEnd: Color,
   val trackInactive: Color,
   val trackBuffer: Color,
   val thumbColor: Color,
   val thumbGlow: Color,
   val thumbGlowActive: Color,
   val tooltipBackground: Color,
   val tooltipText: Color,
   val timeTextNormal: Color,
   val timeTextActive: Color,
)

@Composable
private fun rememberSliderColors(isCompact: Boolean): SliderColors {
   val scheme = MaterialTheme.colorScheme
   val extended = FreePlayerTheme.extendedColors
   
   return remember(scheme.primary, scheme.onSurface, scheme.onPrimary, isCompact) {
      SliderColors(
         trackActiveStart = if (isCompact) scheme.primary else AppColors.ElectricViolet.v5,
         trackActiveEnd = scheme.primary,
         trackInactive = scheme.onSurface.copy(alpha = if (isCompact) 0.12f else 0.15f),
         trackBuffer = scheme.onSurface.copy(alpha = if (isCompact) 0.20f else 0.25f),
         thumbColor = AppColors.Blanco,
         thumbGlow = scheme.primary.copy(alpha = if (isCompact) 0.3f else 0.6f),
         thumbGlowActive = if (isCompact)
            scheme.primary.copy(alpha = 0.5f)
         else
            AppColors.ElectricViolet.v5.copy(alpha = 0.8f),
         tooltipBackground = scheme.primary,
         tooltipText = scheme.onPrimary,
         timeTextNormal = scheme.onSurface.copy(alpha = if (isCompact) 0.5f else 0.6f),
         timeTextActive = scheme.onSurface.copy(alpha = if (isCompact) 0.7f else 0.9f),
      )
   }
}

// ==================== ESTADO DEL SLIDER ====================

/**
 * Holder del estado interno del slider.
 * Usa derivedStateOf para propiedades computadas → mejor rendimiento.
 */
@Stable
private class SliderStateHolder(
   initialValueMs: Float,
   private val durationMs: Long,
) {
   var valueMs by mutableFloatStateOf(initialValueMs)
   
   val progress: Float by derivedStateOf {
      if (durationMs > 0) (valueMs / durationMs).coerceIn(0f, 1f) else 0f
   }
   
   val currentTimeText: String by derivedStateOf {
      valueMs.toLong().formatAsTime()
   }
   
   val totalTimeText: String by derivedStateOf {
      durationMs.formatAsTime()
   }
   
   val remainingMs: Long by derivedStateOf {
      (durationMs - valueMs.toLong()).coerceAtLeast(0)
   }
   
   fun updateFromPlayer(positionMs: Float) {
      valueMs = positionMs
   }
}

/**
 * Resultado del estado del slider con interacción.
 */
@Immutable
private data class SliderInteractionState(
   val holder: SliderStateHolder,
   val isInteracting: Boolean,
)

@Composable
private fun rememberSliderState(
   playerState: PlayerState,
   interactionSource: MutableInteractionSource,
): SliderInteractionState {
   val durationMs = playerState.durationMs
   val isPressed by interactionSource.collectIsPressedAsState()
   val isDragged by interactionSource.collectIsDraggedAsState()
   
   val holder = remember(playerState.currentSong?.cancion?.idCancion, durationMs) {
      SliderStateHolder(
         initialValueMs = playerState.displayPositionMs.toFloat(),
         durationMs = durationMs,
      )
   }
   
   // Sincronizar con el reproductor cuando no estamos scrubbing
   LaunchedEffect(playerState.displayPositionMs, playerState.isScrubbing) {
      if (!playerState.isScrubbing) {
         holder.updateFromPlayer(playerState.displayPositionMs.toFloat())
      }
   }
   
   val isInteracting = isPressed || isDragged || playerState.isScrubbing
   
   return remember(holder, isInteracting) {
      SliderInteractionState(holder, isInteracting)
   }
}

// ==================== SLIDER COMPLETO ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSlider(
   state: PlayerState,
   onSeekStart: (Long) -> Unit,
   onSeekUpdate: (Long) -> Unit,
   onSeekFinish: (Long) -> Unit,
   modifier: Modifier = Modifier,
   bufferProgress: Float = 1f,
) {
   val colors = rememberSliderColors(isCompact = false)
   val interactionSource = remember { MutableInteractionSource() }
   val (sliderHolder, isInteracting) = rememberSliderState(state, interactionSource)
   
   // Callbacks estables
   val onValueChange = remember(onSeekStart, onSeekUpdate, state.isScrubbing) {
      { newValue: Float ->
         if (!state.isScrubbing) onSeekStart(newValue.toLong())
         sliderHolder.valueMs = newValue
         onSeekUpdate(newValue.toLong())
      }
   }
   val onFinish = remember(onSeekFinish) {
      { onSeekFinish(sliderHolder.valueMs.toLong()) }
   }
   
   val thumbScale by animateFloatAsState(
      targetValue = if (isInteracting) 1.3f else 1f,
      animationSpec = spring(
         dampingRatio = SliderConfig.SPRING_DAMPING,
         stiffness = SliderConfig.SPRING_STIFFNESS,
      ),
      label = "thumbScale",
   )
   
   val trackHeight by animateDpAsState(
      targetValue = if (isInteracting) SliderConfig.trackHeightActive else SliderConfig.trackHeightNormal,
      animationSpec = spring(
         dampingRatio = SliderConfig.SPRING_DAMPING,
         stiffness = SliderConfig.SPRING_STIFFNESS,
      ),
      label = "trackHeight",
   )
   
   Column(
      modifier = modifier
         .fillMaxWidth()
         .semantics {
            contentDescription =
               "Progreso: ${sliderHolder.currentTimeText} de ${sliderHolder.totalTimeText}"
         }
   ) {
      SliderWithTrack(
         value = sliderHolder.valueMs,
         valueRange = 0f..state.durationMs.toFloat().coerceAtLeast(1f),
         progress = sliderHolder.progress,
         bufferProgress = bufferProgress,
         trackHeight = trackHeight,
         thumbSize = SliderConfig.thumbSizeNormal,
         thumbScale = thumbScale,
         isInteracting = isInteracting,
         colors = colors,
         isCompact = false,
         interactionSource = interactionSource,
         onValueChange = onValueChange,
         onValueChangeFinished = onFinish,
      )
      
      Spacer(modifier = Modifier.height(4.dp))
      
      TimeRow(
         currentTime = sliderHolder.currentTimeText,
         remainingMs = sliderHolder.remainingMs,
         isInteracting = isInteracting,
         colors = colors,
      )
   }
}

// ==================== SLIDER COMPACTO ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactProgressSlider(
   state: PlayerState,
   onSeekStart: (Long) -> Unit,
   onSeekUpdate: (Long) -> Unit,
   onSeekFinish: (Long) -> Unit,
   modifier: Modifier = Modifier,
   showTimes: Boolean = true,
) {
   val colors = rememberSliderColors(isCompact = true)
   val interactionSource = remember { MutableInteractionSource() }
   val (sliderHolder, isInteracting) = rememberSliderState(state, interactionSource)
   
   // Callbacks estables
   val onValueChange = remember(onSeekStart, onSeekUpdate, state.isScrubbing) {
      { newValue: Float ->
         if (!state.isScrubbing) onSeekStart(newValue.toLong())
         sliderHolder.valueMs = newValue
         onSeekUpdate(newValue.toLong())
      }
   }
   val onFinish = remember(onSeekFinish) {
      { onSeekFinish(sliderHolder.valueMs.toLong()) }
   }
   
   val trackHeight by animateDpAsState(
      targetValue = if (isInteracting) SliderConfig.compactTrackActive else SliderConfig.compactTrackHeight,
      animationSpec = spring(dampingRatio = 0.8f),
      label = "compactTrack",
   )
   
   val thumbSize by animateDpAsState(
      targetValue = if (isInteracting) SliderConfig.compactThumbActive else SliderConfig.compactThumbNormal,
      animationSpec = spring(dampingRatio = 0.7f),
      label = "compactThumb",
   )
   
   Row(
      modifier = modifier
         .fillMaxWidth()
         .semantics {
            contentDescription =
               "Progreso: ${sliderHolder.currentTimeText} de ${sliderHolder.totalTimeText}"
         },
      verticalAlignment = Alignment.CenterVertically,
   ) {
      if (showTimes) {
         TimeText(
            text = sliderHolder.currentTimeText,
            color = if (isInteracting) colors.timeTextActive else colors.timeTextNormal,
            modifier = Modifier.width(SliderConfig.timeWidth),
         )
      }
      
      SliderWithTrack(
         value = sliderHolder.valueMs,
         valueRange = 0f..state.durationMs.toFloat().coerceAtLeast(1f),
         progress = sliderHolder.progress,
         bufferProgress = 1f,
         trackHeight = trackHeight,
         thumbSize = thumbSize,
         thumbScale = 1f,
         isInteracting = isInteracting,
         colors = colors,
         isCompact = true,
         interactionSource = interactionSource,
         onValueChange = onValueChange,
         onValueChangeFinished = onFinish,
         modifier = Modifier.weight(1f),
      )
      
      if (showTimes) {
         TimeText(
            text = sliderHolder.totalTimeText,
            color = colors.timeTextNormal,
            textAlign = TextAlign.End,
            modifier = Modifier.width(SliderConfig.timeWidth),
         )
      }
   }
}

// ==================== COMPONENTES COMPARTIDOS ====================

/**
 * Slider con track custom - unifica lógica de ambos sliders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderWithTrack(
   value: Float,
   valueRange: ClosedFloatingPointRange<Float>,
   progress: Float,
   bufferProgress: Float,
   trackHeight: Dp,
   thumbSize: Dp,
   thumbScale: Float,
   isInteracting: Boolean,
   colors: SliderColors,
   isCompact: Boolean,
   interactionSource: MutableInteractionSource,
   onValueChange: (Float) -> Unit,
   onValueChangeFinished: () -> Unit,
   modifier: Modifier = Modifier,
) {
   Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
   ) {
      SliderTrack(
         progress = progress,
         bufferProgress = bufferProgress,
         trackHeight = trackHeight,
         colors = colors,
         isCompact = isCompact,
         modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) SliderConfig.compactThumbNormal / 2 else SliderConfig.thumbSizeNormal / 2),
      )
      
      Slider(
         value = value,
         onValueChange = onValueChange,
         onValueChangeFinished = onValueChangeFinished,
         valueRange = valueRange,
         interactionSource = interactionSource,
         colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
         ),
         thumb = {
            SliderThumb(
               size = thumbSize,
               scale = thumbScale,
               isInteracting = isInteracting,
               colors = colors,
               isCompact = isCompact,
            )
         },
         modifier = Modifier.fillMaxWidth(),
      )
   }
}

@Composable
private fun TimeRow(
   currentTime: String,
   remainingMs: Long,
   isInteracting: Boolean,
   colors: SliderColors,
) {
   Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
   ) {
      TimeText(
         text = currentTime,
         color = if (isInteracting) colors.timeTextActive else colors.timeTextNormal,
      )
      TimeText(
         text = "-${remainingMs.formatAsTime()}",
         color = colors.timeTextNormal,
      )
   }
}

@Composable
private fun TimeText(
   text: String,
   color: Color,
   modifier: Modifier = Modifier,
   textAlign: TextAlign = TextAlign.Start,
) {
   Text(
      text = text,
      color = color,
      style = FreePlayerTypography.extended.timerText,
      textAlign = textAlign,
      modifier = modifier,
   )
}

// ==================== TRACK Y THUMB ====================

@Composable
private fun SliderTrack(
   progress: Float,
   bufferProgress: Float,
   trackHeight: Dp,
   colors: SliderColors,
   isCompact: Boolean,
   modifier: Modifier = Modifier,
) {
   // Gradient brush cacheado solo por los colores necesarios
   val gradientBrush = remember(colors.trackActiveStart, colors.trackActiveEnd, isCompact) {
      if (isCompact) {
         Brush.horizontalGradient(listOf(colors.trackActiveStart, colors.trackActiveStart))
      } else {
         Brush.horizontalGradient(listOf(colors.trackActiveStart, colors.trackActiveEnd))
      }
   }
   
   val trackInactive = colors.trackInactive
   val trackBuffer = colors.trackBuffer
   
   Box(
      modifier = modifier
         .height(trackHeight)
         .clip(RoundedCornerShape(trackHeight / 2))
         .drawBehind {
            val cornerRadius = CornerRadius(size.height / 2, size.height / 2)
            
            // Track inactivo (fondo)
            drawRoundRect(color = trackInactive, cornerRadius = cornerRadius)
            
            // Buffer (solo en slider completo)
            if (!isCompact && bufferProgress < 1f) {
               drawRoundRect(
                  color = trackBuffer,
                  size = Size(size.width * bufferProgress, size.height),
                  cornerRadius = cornerRadius,
               )
            }
            
            // Track activo (progreso)
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
private fun SliderThumb(
   size: Dp,
   scale: Float,
   isInteracting: Boolean,
   colors: SliderColors,
   isCompact: Boolean,
   modifier: Modifier = Modifier,
) {
   Box(
      modifier = modifier.size(SliderConfig.thumbTouchTarget),
      contentAlignment = Alignment.Center,
   ) {
      // Glow blur (solo slider completo cuando interactúa)
      if (isInteracting && !isCompact) {
         Box(
            modifier = Modifier
               .size(size + SliderConfig.glowRadius)
               .scale(scale)
               .blur(8.dp)
               .background(
                  color = colors.thumbGlowActive.copy(alpha = 0.6f),
                  shape = CircleShape,
               )
         )
      }
      
      // Halo de interacción
      if (isInteracting) {
         val haloSize = if (isCompact) size + 2.dp else size + 4.dp
         val haloScale = if (isCompact) 1f else scale
         val haloAlpha = if (isCompact) 1f else 0.4f
         
         Box(
            modifier = Modifier
               .size(haloSize)
               .scale(haloScale)
               .background(
                  color = colors.thumbGlow.copy(alpha = haloAlpha),
                  shape = CircleShape,
               )
         )
      }
      
      // Thumb principal
      val elevation = when {
         isInteracting && isCompact -> 4.dp
         isInteracting -> 8.dp
         isCompact -> 2.dp
         else -> 4.dp
      }
      
      Box(
         modifier = Modifier
            .size(size)
            .scale(if (isCompact) 1f else scale)
            .shadow(elevation = elevation, shape = CircleShape)
            .background(color = colors.thumbColor, shape = CircleShape)
      )
   }
}


// ==================== PREVIEWS ====================

private fun previewSong(
   title: String,
   artist: String,
   durationSec: Int,
) = SongWithArtist(
   cancion = SongEntity(
      idCancion = 1,
      titulo = title,
      duracionSegundos = durationSec,
      archivoPath = "/music/song.mp3",
      portadaPath = null,
      idArtista = 1,
      idAlbum = 1,
      idGenero = 1,
      origen = "LOCAL",
   ),
   artistaNombre = artist,
   albumNombre = "Album",
   generoNombre = "Rock",
   fechaLanzamiento = "2020-01-01",
)

@Preview(name = "Slider Completo - Light", showBackground = true)
@Preview(name = "Slider Completo - Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewProgressSlider() {
   FreePlayerMTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
         ProgressSlider(
            state = PlayerState(
               currentSong = previewSong("Bohemian Rhapsody", "Queen", 355),
               isPlaying = true,
               currentPositionMs = 125000L,
            ),
            onSeekStart = {},
            onSeekUpdate = {},
            onSeekFinish = {},
            bufferProgress = 0.75f,
            modifier = Modifier.padding(16.dp),
         )
      }
   }
}

@Preview(name = "Compact - Light", showBackground = true)
@Preview(name = "Compact - Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCompactSlider() {
   FreePlayerMTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
         CompactProgressSlider(
            state = PlayerState(
               currentSong = previewSong("Hotel California", "Eagles", 391),
               isPlaying = true,
               currentPositionMs = 90000L,
            ),
            onSeekStart = {},
            onSeekUpdate = {},
            onSeekFinish = {},
            modifier = Modifier.padding(16.dp),
         )
      }
   }
}

@Preview(name = "Comparación", showBackground = true, heightDp = 200)
@Composable
private fun PreviewBothSliders() {
   val state = PlayerState(
      currentSong = previewSong("Wish You Were Here", "Pink Floyd", 334),
      isPlaying = true,
      currentPositionMs = 45000L,
   )
   
   FreePlayerMTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
         ) {
            ProgressSlider(state = state, onSeekStart = {}, onSeekUpdate = {}, onSeekFinish = {})
            CompactProgressSlider(state = state, onSeekStart = {}, onSeekUpdate = {}, onSeekFinish = {})
         }
      }
   }
}