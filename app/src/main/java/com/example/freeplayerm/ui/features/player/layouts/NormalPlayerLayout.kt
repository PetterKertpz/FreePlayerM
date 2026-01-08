package com.example.freeplayerm.ui.features.player.layouts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.features.player.components.CompactControls
import com.example.freeplayerm.ui.features.player.components.CompactProgressSlider
import com.example.freeplayerm.ui.features.player.components.SpinningVinyl
import com.example.freeplayerm.ui.features.player.gesture.HorizontalSwipeController
import com.example.freeplayerm.ui.features.player.gesture.PlayerInterpolatedValues
import com.example.freeplayerm.ui.features.player.gesture.rememberHorizontalSwipeController
import com.example.freeplayerm.ui.features.player.gesture.rememberInterpolatedValues
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.formatAsTime
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.example.freeplayerm.utils.clickableWithoutRipple

@Composable
fun NormalPlayerLayout(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   interpolatedValues: PlayerInterpolatedValues,
   swipeController: HorizontalSwipeController,
   onExpandClick: () -> Unit,
   modifier: Modifier = Modifier,
) {
   val currentSong = state.currentSong ?: return
   
   // Animación de escala cuando reproduce
   val playingScale by animateFloatAsState(
      targetValue = if (state.isPlaying) 1f else 0.98f,
      animationSpec = spring(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessLow
      ),
      label = "playingScale"
   )
   
   // Color de acento animado
   val accentColor by animateColorAsState(
      targetValue = if (state.isPlaying) {
         MaterialTheme.colorScheme.primary
      } else {
         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      },
      animationSpec = tween(durationMillis = 300),
      label = "accentColor"
   )
   
   // Colores galácticos
   val galaxyPurple = Color(0xFFD500F9)
   val galaxyCyan = Color(0xFF00E5FF)
   val galaxyDeepPurple = Color(0xFF7C4DFF)
   val galaxyBlack = Color(0xFF050510)
   
   // Contenedor raíz con fondo galáctico
   Box(
      modifier = modifier
         .alpha(interpolatedValues.normalLayoutAlpha)
         .graphicsLayer {
            translationX = swipeController.offsetX.value
            scaleX = playingScale
            scaleY = playingScale
         }
         .background(
            brush = Brush.verticalGradient(
               colors = listOf(
                  galaxyDeepPurple.copy(alpha = 0.3f),
                  galaxyPurple.copy(alpha = 0.15f),
                  galaxyBlack.copy(alpha = 0.95f),
                  Color.Black
               ),
               startY = 0f,
               endY = Float.POSITIVE_INFINITY
            )
         )
         .border(
            width = 1.5.dp,
            brush = Brush.verticalGradient(
               colors = listOf(
                  galaxyCyan.copy(alpha = 0.6f),
                  galaxyPurple.copy(alpha = 0.4f),
                  galaxyBlack.copy(alpha = 0.8f)
               )
            ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
         ),
      contentAlignment = Alignment.TopCenter
   ) {
      // Glow superior sutil
      Box(
         modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
               brush = Brush.horizontalGradient(
                  colors = listOf(
                     Color.Transparent,
                     galaxyCyan.copy(alpha = 0.5f),
                     galaxyPurple.copy(alpha = 0.6f),
                     galaxyCyan.copy(alpha = 0.5f),
                     Color.Transparent
                  )
               )
            )
      )
      
      Column(
         modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp),
         horizontalAlignment = Alignment.CenterHorizontally
      ) {
         // Row principal: Vinilo + (Info + Controles)
         Row(
            modifier = Modifier
               .fillMaxWidth()
               .weight(1f),
            verticalAlignment = Alignment.CenterVertically
         ) {
            // Vinilo (30% del ancho)
            Box(
               modifier = Modifier
                  .fillMaxWidth(0.2f)
                  .fillMaxHeight()
                  .clickableWithoutRipple { onExpandClick() },
               contentAlignment = Alignment.Center
            ) {
               // Glow cuando reproduce
               if (state.isPlaying) {
                  val glowAlpha by animateFloatAsState(
                     targetValue = 0.3f,
                     animationSpec = tween(500),
                     label = "glowAlpha"
                  )
                  Box(
                     modifier = Modifier
                        .size(72.dp)
                        .alpha(glowAlpha)
                        .background(
                           brush = Brush.radialGradient(
                              colors = listOf(
                                 accentColor.copy(alpha = 0.4f),
                                 Color.Transparent
                              )
                           ),
                           shape = RoundedCornerShape(50)
                        )
                  )
               }
               
               SpinningVinyl(
                  cancion = currentSong,
                  estaReproduciendo = state.isPlaying,
                  modifier = Modifier.padding(2.dp)
               )
            }
            
            // Info + Controles (70% restante)
            Row(
               modifier = Modifier
                  .fillMaxWidth()
                  .fillMaxHeight(),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.SpaceBetween
            ) {
               // Column: Info de la canción
               Column(
                  modifier = Modifier
                     .weight(1f)
                     .clickableWithoutRipple { onExpandClick() },
                  verticalArrangement = Arrangement.Center
               ) {
                  Text(
                     text = state.titleDisplay,
                     style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                     ),
                     color = MaterialTheme.colorScheme.onSurface,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                  )
                  
                  Spacer(modifier = Modifier.height(2.dp))
                  
                  Text(
                     text = state.artistDisplay,
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                  )
               }
               
               // Row: Controles
               CompactControls(
                  state = state,
                  onEvent = onEvent
               )
            }
         }
         
         // Row: Slider con timer
         Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
         ) {
            // Tiempo actual
            TimeLabel(
               time = state.remainingMs.formatAsTime(),
               isActive = false,
               accentColor = accentColor
            )
            
            // Slider
            CompactProgressSlider(
               state = state,
               onSeekStart = { onEvent(PlayerEvent.Seek.Start(it)) },
               onSeekUpdate = { onEvent(PlayerEvent.Seek.Update(it)) },
               onSeekFinish = { onEvent(PlayerEvent.Seek.Finish(it)) },
               showTimes = false,
               modifier = Modifier.weight(1f)
            )
            
            // Tiempo restante
            TimeLabel(
               time = state.durationMs.formatAsTime(),
               isActive = false,
               accentColor = accentColor
            )
            
         }
         
         // Spacer inferior (define separación del bottom)
         Spacer(modifier = Modifier.height(16.dp))
      }
   }
}

// Label de tiempo con animación
@Composable
fun TimeLabel(
   time: String,
   isActive: Boolean,
   accentColor: Color,
   modifier: Modifier = Modifier
) {
   val textColor by animateColorAsState(
      targetValue = if (isActive) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      animationSpec = tween(200),
      label = "timeColor"
   )
   
   Text(
      text = time,
      style = MaterialTheme.typography.labelSmall.copy(
         fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
         letterSpacing = 0.5.sp
      ),
      color = textColor,
      modifier = modifier
   )
}

// region Previews

class PlayerStatePreviewProvider : PreviewParameterProvider<PlayerState> {
   override val values: Sequence<PlayerState> = sequenceOf(
      PlayerState(
         currentSong = SongWithArtist.preview(titulo = "Bohemian Rhapsody", artista = "Queen"),
         isPlaying = true,
         currentPositionMs = 125000L,
         isFavorite = true
      ),
      PlayerState(
         currentSong = SongWithArtist.preview(titulo = "Hotel California", artista = "Eagles"),
         isPlaying = false,
         currentPositionMs = 45000L
      ),
      PlayerState(
         currentSong = SongWithArtist.preview(titulo = "Stairway to Heaven", artista = "Led Zeppelin"),
         isPlaying = true,
         currentPositionMs = 200000L,
         scrubPositionMs = 180000L,
         isScrubbing = true
      )
   )
}

@Preview(name = "🌙 Dark - States", widthDp = 400, heightDp = 700)
@Composable
private fun PreviewDark(
   @PreviewParameter(PlayerStatePreviewProvider::class) state: PlayerState
) {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = state,
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 700.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "🎵 Playing - Full Context", widthDp = 400, heightDp = 800)
@Composable
private fun PreviewPlayingFullContext() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(titulo = "Imagine", artista = "John Lennon"),
                     isPlaying = true,
                     currentPositionMs = 90000L,
                     isFavorite = true
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "⏸️ Paused - Full Context", widthDp = 400, heightDp = 800)
@Composable
private fun PreviewPausedFullContext() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(titulo = "Yesterday", artista = "The Beatles"),
                     isPlaying = false,
                     currentPositionMs = 120000L
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "🎚️ Scrubbing", widthDp = 400, heightDp = 800)
@Composable
private fun PreviewScrubbing() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(titulo = "Comfortably Numb", artista = "Pink Floyd"),
                     isPlaying = true,
                     currentPositionMs = 300000L,
                     scrubPositionMs = 180000L,
                     isScrubbing = true
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "📱 Compact Width", widthDp = 320, heightDp = 700)
@Composable
private fun PreviewCompact() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(titulo = "Sweet Child O' Mine", artista = "Guns N' Roses"),
                     isPlaying = true,
                     currentPositionMs = 180000L
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 700.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "🔤 Large Font", widthDp = 400, heightDp = 800, fontScale = 1.3f)
@Composable
private fun PreviewLargeFont() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(160.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(titulo = "Thriller", artista = "Michael Jackson"),
                     isPlaying = true,
                     currentPositionMs = 150000L
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

@Preview(name = "📱 Long Title", widthDp = 400, heightDp = 800)
@Composable
private fun PreviewLongTitle() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize()) {
         GalaxyBackground(modifier = Modifier.fillMaxSize())
         
         Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
         ) {
            Surface(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp),
               color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
               shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
               tonalElevation = 8.dp
            ) {
               NormalPlayerLayout(
                  state = PlayerState(
                     currentSong = SongWithArtist.preview(
                        titulo = "The Show Must Go On - Live at Wembley Stadium 1986 Remastered",
                        artista = "Queen feat. Special Guests & Orchestra"
                     ),
                     isPlaying = true,
                     currentPositionMs = 60000L
                  ),
                  onEvent = {},
                  interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
                  swipeController = rememberHorizontalSwipeController(),
                  onExpandClick = {}
               )
            }
         }
      }
   }
}

// endregion