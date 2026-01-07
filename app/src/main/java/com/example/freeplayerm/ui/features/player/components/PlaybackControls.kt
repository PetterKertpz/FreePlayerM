package com.example.freeplayerm.ui.features.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.com.example.freeplayerm.ui.theme.PlayerIcons
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.model.PlaybackMode
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.RepeatMode
import com.example.freeplayerm.ui.theme.AppColors
import kotlinx.coroutines.delay

// ==================== CONSTANTES ====================

private object PlaybackAnimationSpec {
   const val FADE_DURATION = 300
   const val SCALE_DURATION = 300
   const val STAGGER_DELAY = 50
   val BUTTON_SPRING = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
   val FAVORITE_SPRING = spring<Float>(dampingRatio = 0.3f, stiffness = 500f)
}

private object PlaybackColors {
   val ActiveTint = AppColors.ElectricViolet.v6
   val InactiveTint = Color.White.copy(alpha = 0.5f)
   val PrimaryTint = Color.White
   val GradientStart = AppColors.ElectricViolet.v6
   val GradientEnd = Color(0xFFAA00FF)
}

private object PlaybackSizes {
   val SmallIcon = 20.dp
   val MediumIcon = 24.dp
   val LargeIcon = 28.dp
   val XLargeIcon = 36.dp
   val NavIcon = 40.dp
   val SmallButton = 36.dp
   val MainButton = 72.dp
}

private enum class ControlType { SHUFFLE, PREV, PLAY, NEXT, REPEAT }
private val CONTROL_SEQUENCE = ControlType.entries

// ==================== COMPONENTES PRINCIPALES ====================

/**
 * 🎛️ CONTROLES EXPANDIDOS
 *
 * Controles completos para modo expandido con animaciones secuenciales.
 */
@Composable
fun PlaybackControls(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   modifier: Modifier = Modifier,
) {
   val songId = state.currentSong?.cancion?.idCancion
   var animationTrigger by remember { mutableStateOf(false) }
   
   LaunchedEffect(songId) {
      animationTrigger = false
      delay(50)
      animationTrigger = true
   }
   
   Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
   ) {
      CONTROL_SEQUENCE.forEachIndexed { index, control ->
         AnimatedControlButton(
            visible = animationTrigger,
            index = index,
         ) {
            when (control) {
               ControlType.SHUFFLE -> ShuffleButton(state.playbackMode, onEvent)
               ControlType.PREV -> PreviousButton(onEvent)
               ControlType.PLAY -> PlayPauseMainButton(state.isPlaying, onEvent)
               ControlType.NEXT -> NextButton(onEvent)
               ControlType.REPEAT -> RepeatButton(state.repeatMode, onEvent)
            }
         }
      }
   }
}

@Composable
private fun AnimatedControlButton(
   visible: Boolean,
   index: Int,
   content: @Composable () -> Unit,
) {
   AnimatedVisibility(
      visible = visible,
      enter = fadeIn(
         tween(
            durationMillis = PlaybackAnimationSpec.FADE_DURATION,
            delayMillis = PlaybackAnimationSpec.STAGGER_DELAY * index,
         )
      ) + scaleIn(
         initialScale = 0.8f,
         animationSpec = tween(
            durationMillis = PlaybackAnimationSpec.SCALE_DURATION,
            delayMillis = PlaybackAnimationSpec.STAGGER_DELAY * index,
         ),
      ),
      exit = fadeOut(tween(150)),
   ) {
      content()
   }
}

/**
 * 🎛️ CONTROLES COMPACTOS
 *
 * Controles simplificados para mini-player.
 */
@Composable
fun CompactControls(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   modifier: Modifier = Modifier,
) {
   // Callbacks estables - evita recomposiciones innecesarias
   val onPrevious = remember(onEvent) { { onEvent(PlayerEvent.Playback.Previous) } }
   val onPlayPause = remember(onEvent) { { onEvent(PlayerEvent.Playback.PlayPause) } }
   val onNext = remember(onEvent) { { onEvent(PlayerEvent.Playback.Next) } }
   
   Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
   ) {
      CompactIconButton(
         onClick = onPrevious,
         icon = PlayerIcons.Previous,
         contentDescription = "Canción anterior",
      )
      
      CompactPlayPauseButton(
         isPlaying = state.isPlaying,
         onClick = onPlayPause,
      )
      
      CompactIconButton(
         onClick = onNext,
         icon = PlayerIcons.Next,
         contentDescription = "Siguiente canción",
      )
   }
}

@Composable
private fun CompactIconButton(
   onClick: () -> Unit,
   icon: androidx.compose.ui.graphics.vector.ImageVector,
   contentDescription: String,
) {
   IconButton(
      onClick = onClick,
      modifier = Modifier.size(PlaybackSizes.SmallButton),
   ) {
      Icon(
         imageVector = icon,
         contentDescription = contentDescription,
         tint = PlaybackColors.PrimaryTint.copy(alpha = 0.7f),
         modifier = Modifier.size(PlaybackSizes.SmallIcon),
      )
   }
}

@Composable
private fun CompactPlayPauseButton(
   isPlaying: Boolean,
   onClick: () -> Unit,
) {
   IconButton(
      onClick = onClick,
      modifier = Modifier
         .size(PlaybackSizes.SmallButton)
         .background(PlaybackColors.ActiveTint.copy(alpha = 0.2f), CircleShape),
   ) {
      Icon(
         imageVector = PlayerIcons.getPlayPauseIcon(isPlaying),
         contentDescription = if (isPlaying) "Pausar" else "Reproducir",
         tint = PlaybackColors.PrimaryTint,
         modifier = Modifier.size(PlaybackSizes.SmallIcon),
      )
   }
}

// ==================== BOTONES INDIVIDUALES ====================

/**
 * Solo recibe el estado que necesita (playbackMode) en lugar de todo PlayerState
 */
@Composable
private fun ShuffleButton(
   playbackMode: PlaybackMode,
   onEvent: (PlayerEvent) -> Unit,
) {
   val isActive = playbackMode == PlaybackMode.SHUFFLE
   val onClick = remember(onEvent) { { onEvent(PlayerEvent.Settings.TogglePlaybackMode) } }
   
   IconButton(onClick = onClick) {
      Icon(
         imageVector = PlayerIcons.Shuffle,
         contentDescription = if (isActive) "Desactivar aleatorio" else "Activar aleatorio",
         tint = if (isActive) PlaybackColors.ActiveTint else PlaybackColors.InactiveTint,
         modifier = Modifier.size(PlaybackSizes.MediumIcon),
      )
   }
}

@Composable
private fun PreviousButton(onEvent: (PlayerEvent) -> Unit) {
   val onClick = remember(onEvent) { { onEvent(PlayerEvent.Playback.Previous) } }
   
   IconButton(onClick = onClick) {
      Icon(
         imageVector = PlayerIcons.Previous,
         contentDescription = "Canción anterior",
         tint = PlaybackColors.PrimaryTint,
         modifier = Modifier.size(PlaybackSizes.NavIcon),
      )
   }
}

/**
 * Solo recibe isPlaying en lugar de todo PlayerState
 */
@Composable
private fun PlayPauseMainButton(
   isPlaying: Boolean,
   onEvent: (PlayerEvent) -> Unit,
) {
   val interactionSource = remember { MutableInteractionSource() }
   val isPressed by interactionSource.collectIsPressedAsState()
   val onClick = remember(onEvent) { { onEvent(PlayerEvent.Playback.PlayPause) } }
   
   val scale by animateFloatAsState(
      targetValue = if (isPressed) 0.9f else 1f,
      animationSpec = PlaybackAnimationSpec.BUTTON_SPRING,
      label = "buttonScale",
   )
   
   Box(
      modifier = Modifier
         .size(PlaybackSizes.MainButton)
         .scale(scale)
         .clip(CircleShape)
         .background(
            Brush.linearGradient(
               colors = listOf(PlaybackColors.GradientStart, PlaybackColors.GradientEnd)
            )
         )
         .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
         .semantics {
            contentDescription = if (isPlaying) "Pausar reproducción" else "Iniciar reproducción"
         },
      contentAlignment = Alignment.Center,
   ) {
      Crossfade(
         targetState = isPlaying,
         animationSpec = tween(200),
         label = "playPauseIcon",
      ) { playing ->
         Icon(
            imageVector = PlayerIcons.getPlayPauseIcon(playing),
            contentDescription = null, // Ya está en el Box
            tint = PlaybackColors.PrimaryTint,
            modifier = Modifier.size(PlaybackSizes.XLargeIcon),
         )
      }
   }
}

@Composable
private fun NextButton(onEvent: (PlayerEvent) -> Unit) {
   val onClick = remember(onEvent) { { onEvent(PlayerEvent.Playback.Next) } }
   
   IconButton(onClick = onClick) {
      Icon(
         imageVector = PlayerIcons.Next,
         contentDescription = "Siguiente canción",
         tint = PlaybackColors.PrimaryTint,
         modifier = Modifier.size(PlaybackSizes.NavIcon),
      )
   }
}

/**
 * Solo recibe repeatMode en lugar de todo PlayerState
 */
@Composable
private fun RepeatButton(
   repeatMode: RepeatMode,
   onEvent: (PlayerEvent) -> Unit,
) {
   val isActive = repeatMode != RepeatMode.OFF
   val onClick = remember(onEvent) { { onEvent(PlayerEvent.Settings.ToggleRepeatMode) } }
   
   val (icon, description) = remember(repeatMode) {
      when (repeatMode) {
         RepeatMode.OFF -> PlayerIcons.RepeatAll to "Activar repetición"
         RepeatMode.ALL -> PlayerIcons.RepeatAll to "Repetir todas activo"
         RepeatMode.ONE -> PlayerIcons.RepeatOne to "Repetir una activo"
      }
   }
   
   IconButton(onClick = onClick) {
      Icon(
         imageVector = icon,
         contentDescription = description,
         tint = if (isActive) PlaybackColors.ActiveTint else PlaybackColors.InactiveTint,
         modifier = Modifier.size(PlaybackSizes.MediumIcon),
      )
   }
}

/**
 * Botón de favorito con animación
 */
@Composable
fun FavoriteButton(
   isFavorite: Boolean,
   onToggle: () -> Unit,
   modifier: Modifier = Modifier,
) {
   val scale by animateFloatAsState(
      targetValue = if (isFavorite) 1.1f else 1f,
      animationSpec = PlaybackAnimationSpec.FAVORITE_SPRING,
      label = "heartScale",
   )
   
   val tintColor by animateColorAsState(
      targetValue = if (isFavorite) Color.Red else PlaybackColors.InactiveTint.copy(alpha = 0.6f),
      animationSpec = tween(300),
      label = "heartColor",
   )
   
   IconButton(onClick = onToggle, modifier = modifier) {
      Icon(
         imageVector = PlayerIcons.getFavoriteIcon(isFavorite),
         contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
         tint = tintColor,
         modifier = Modifier
            .size(PlaybackSizes.LargeIcon)
            .scale(scale),
      )
   }
}

// ==================== PREVIEWS ====================

private class PlayerStateProvider : PreviewParameterProvider<PlayerState> {
   override val values = sequenceOf(
      PlayerState(
         currentSong = SongWithArtist.preview(
            titulo = "Bohemian Rhapsody",
            artista = "Queen",
            album = "A Night at the Opera",
         ),
         isPlaying = true,
         playbackMode = PlaybackMode.SEQUENTIAL,
         repeatMode = RepeatMode.OFF,
         isFavorite = false,
      ),
      PlayerState(
         currentSong = SongWithArtist.preview(
            titulo = "Stairway to Heaven",
            artista = "Led Zeppelin"
         ),
         isPlaying = false,
         playbackMode = PlaybackMode.SHUFFLE,
         repeatMode = RepeatMode.ALL,
         isFavorite = true,
      ),
   )
}

@Preview(name = "Expanded Controls", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewPlaybackControls(
   @PreviewParameter(PlayerStateProvider::class) state: PlayerState
) {
   PlaybackControls(state = state, onEvent = {})
}

@Preview(name = "Compact Controls", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewCompactControls(
   @PreviewParameter(PlayerStateProvider::class) state: PlayerState
) {
   CompactControls(state = state, onEvent = {})
}