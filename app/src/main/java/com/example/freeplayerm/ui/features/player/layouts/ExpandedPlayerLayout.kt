package com.example.freeplayerm.ui.features.player.layouts

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.components.CompactProgressSlider
import com.example.freeplayerm.ui.features.player.components.DynamicPalette
import com.example.freeplayerm.ui.features.player.components.DynamicPlayerBackground
import com.example.freeplayerm.ui.features.player.components.ExpandedPlayerTabs
import com.example.freeplayerm.ui.features.player.components.FavoriteButton
import com.example.freeplayerm.ui.features.player.components.PlaybackControls
import com.example.freeplayerm.ui.features.player.components.ProgressSlider
import com.example.freeplayerm.ui.features.player.components.SpinningVinyl
import com.example.freeplayerm.ui.features.player.gesture.PlayerInterpolatedValues
import com.example.freeplayerm.ui.features.player.gesture.rememberInterpolatedValues
import com.example.freeplayerm.ui.features.player.model.ExpandedTab
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.formatAsTime
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.launch

/**
 * 🎨 EXPANDED PLAYER LAYOUT - Full UI con Scroll
 *
 * Layout para modo EXPANDED (pantalla completa). Contiene: Header, Vinilo flotante, Info centrada,
 * Slider completo, Controles expandidos. Los Tabs (Letra/Info/Enlaces) están ocultos debajo
 * y se acceden mediante scroll.
 */
@SuppressLint("FrequentlyChangingValue")
@Composable
fun ExpandedPlayerLayout(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   interpolatedValues: PlayerInterpolatedValues,
   onCollapseClick: () -> Unit,
   modifier: Modifier = Modifier,
   palette: DynamicPalette = DynamicPalette.Default,
) {
   val currentSong = state.currentSong ?: return
   val scrollState = rememberScrollState()
   val scope = rememberCoroutineScope()
   
   // Indicador de si hay contenido para scrollear
   val canScrollDown by remember {
      derivedStateOf { scrollState.value < scrollState.maxValue }
   }
   val canScrollUp by remember {
      derivedStateOf { scrollState.value > 0 }
   }
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
   Box(modifier = modifier.fillMaxSize()) {
      // Fondo dinámico animado
      DynamicPlayerBackground(
         palette = palette,
         isPlaying = state.isPlaying,
         modifier = Modifier
            .fillMaxSize()
            .alpha(interpolatedValues.expandedLayoutAlpha)
      )
      
      Column(
         modifier = Modifier
            .fillMaxSize()
            .alpha(interpolatedValues.expandedLayoutAlpha)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp, top = 40.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
      ) {
         // Header
         Row(
            modifier = Modifier
               .fillMaxWidth()
               .background(
                  color = Color.Black.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(16.dp)
               )
               .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
         ) {
            IconButton(onClick = onCollapseClick) {
               Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Collapse player",
                  tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
               )
            }
            
            Text(
               text = "NOW PLAYING",
               style = MaterialTheme.typography.labelSmall,
               color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
               letterSpacing = 2.sp,
            )
            
            FavoriteButton(
               isFavorite = state.isFavorite,
               onToggle = { onEvent(PlayerEvent.Settings.ToggleFavorite) },
            )
         }
         
         Spacer(modifier = Modifier.height(24.dp))
         
         // Vinilo
         Box(
            modifier = Modifier.graphicsLayer {
               translationY = interpolatedValues.vinylFloatOffsetY.toPx()
               scaleX = 1f + (interpolatedValues.vinylGlowScale - 1f)
               scaleY = 1f + (interpolatedValues.vinylGlowScale - 1f)
            },
            contentAlignment = Alignment.Center,
         ) {
            SpinningVinyl(
               cancion = currentSong,
               estaReproduciendo = state.isPlaying,
               modifier = Modifier.fillMaxWidth(0.80f),
            )
         }
         
         Spacer(modifier = Modifier.height(24.dp))
         
         // Info de la canción con glow dinámico
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .graphicsLayer {
                  scaleX = interpolatedValues.infoScale
                  scaleY = interpolatedValues.infoScale
               },
            horizontalAlignment = Alignment.CenterHorizontally,
         ) {
            Text(
               text = state.titleDisplay,
               style = MaterialTheme.typography.headlineSmall,
               color = Color.White,
               textAlign = TextAlign.Center,
               maxLines = 2,
               modifier = Modifier.graphicsLayer {
                  shadowElevation = if (state.isPlaying) 8f else 0f
               }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
               text = state.artistDisplay,
               style = MaterialTheme.typography.bodyLarge,
               color = Color.White.copy(alpha = 0.75f),
               textAlign = TextAlign.Center,
               maxLines = 1,
            )
         }
         
         // Row: Slider con timer
         Row(
            modifier = Modifier.fillMaxWidth(),
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
         
         // Controles de reproducción
         PlaybackControls(state = state, onEvent = onEvent, modifier = Modifier.fillMaxWidth())
         
         Spacer(modifier = Modifier.height(15.dp))
         
         // Indicador de scroll animado
         if (canScrollDown && scrollState.value == 0) {
            val infiniteTransition = rememberInfiniteTransition(label = "scrollHint")
            val bounceOffset by infiniteTransition.animateFloat(
               initialValue = 0f,
               targetValue = 8f,
               animationSpec = infiniteRepeatable(
                  animation = tween(800, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse
               ),
               label = "bounce"
            )
            
            Column(
               horizontalAlignment = Alignment.CenterHorizontally,
               modifier = Modifier
                  .fillMaxWidth()
                  .alpha(0.6f)
                  .graphicsLayer { translationY = bounceOffset }
            ) {
               Text(
                  text = "Desliza para ver más",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f),
               )
               Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Scroll down",
                  tint = Color.White.copy(alpha = 0.7f),
               )
            }
         }
         
         Spacer(modifier = Modifier.height(10.dp))
         
         // Tabs (ocultos, accesibles por scroll)
         ExpandedPlayerTabs(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
               .fillMaxWidth()
               .alpha(interpolatedValues.tabsAlpha)
               .graphicsLayer {
                  translationY = interpolatedValues.tabsOffsetY.toPx()
               },
         )
         
         // Espacio extra al final para mejor UX de scroll
         Spacer(modifier = Modifier.height(50.dp))
      }
      
      // FAB animado para scroll-to-top
      AnimatedVisibility(
         visible = canScrollUp,
         enter = fadeIn() + scaleIn(),
         exit = fadeOut() + scaleOut(),
         modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
      ) {
         Surface(
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            shape = RoundedCornerShape(12.dp),
            color = palette.dominant.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
         ) {
            Box(contentAlignment = Alignment.Center) {
               Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = "Scroll to top",
                  tint = Color.White,
               )
            }
         }
      }
   }
}
// ==================== PREVIEWS ====================

@Preview(
   name = "🌙 Expanded - Playing Dark",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedPlaying() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Bohemian Rhapsody",
               artista = "Queen",
               album = "A Night at the Opera",
            ),
            isPlaying = true,
            currentPositionMs = 125000L,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.LYRICS,
            lyrics = "Is this the real life?\nIs this just fantasy?\nCaught in a landslide...",
            isFavorite = true,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Default,
      )
   }
}

@Preview(
   name = "⏸️ Expanded - Paused",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedPaused() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Stairway to Heaven",
               artista = "Led Zeppelin",
            ),
            isPlaying = false,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.INFO,
            isFavorite = false,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Cool,
      )
   }
}

@Preview(
   name = "🔥 Expanded - Warm Palette",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedWarm() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Blinding Lights",
               artista = "The Weeknd",
               album = "After Hours",
            ),
            isPlaying = true,
            currentPositionMs = 60000L,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.LYRICS,
            isFavorite = true,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Warm,
      )
   }
}

@Preview(
   name = "💜 Expanded - Neon Palette",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedNeon() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Midnight City",
               artista = "M83",
               album = "Hurry Up, We're Dreaming",
            ),
            isPlaying = true,
            currentPositionMs = 90000L,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.CREDITS,
            isFavorite = true,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Neon,
      )
   }
}

@Preview(
   name = "☀️ Expanded - Light Theme",
   showBackground = true,
   backgroundColor = 0xFFF3EEFF,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedLight() {
   FreePlayerMTheme(darkTheme = false) {
      Surface(color = MaterialTheme.colorScheme.surface) {
         ExpandedPlayerLayout(
            state = PlayerState(
               currentSong = SongWithArtist.preview(
                  titulo = "Here Comes The Sun",
                  artista = "The Beatles",
               ),
               isPlaying = true,
               panelMode = PlayerPanelMode.EXPANDED,
               activeTab = ExpandedTab.LINKS,
               geniusUrl = "https://genius.com",
               youtubeUrl = "https://youtube.com",
               isFavorite = false,
            ),
            onEvent = {},
            interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
            onCollapseClick = {},
            palette = DynamicPalette.Warm,
         )
      }
   }
}

@Preview(
   name = "🎵 Expanded - Credits Tab",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedCredits() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "One More Time",
               artista = "Daft Punk",
            ),
            isPlaying = true,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.CREDITS,
            featuredArtists = listOf("Romanthony"),
            producers = listOf("Daft Punk", "Thomas Bangalter"),
            isFavorite = true,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Default,
      )
   }
}

@Preview(
   name = "📱 Expanded - Compact Height",
   showBackground = true,
   backgroundColor = 0xFF050508,
   heightDp = 600,
)
@Composable
private fun PreviewExpandedCompact() {
   FreePlayerMTheme(darkTheme = true) {
      ExpandedPlayerLayout(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Short Song",
               artista = "Artist",
            ),
            isPlaying = false,
            panelMode = PlayerPanelMode.EXPANDED,
         ),
         onEvent = {},
         interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 600.dp),
         onCollapseClick = {},
         palette = DynamicPalette.Cool,
      )
   }
}