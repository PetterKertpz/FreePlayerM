package com.example.freeplayerm.ui.features.player.layouts

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
import androidx.compose.foundation.rememberScrollState
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
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.launch

/**
 * 🎨 EXPANDED PLAYER LAYOUT - Full UI con Scroll
 *
 * Layout para modo EXPANDED (pantalla completa). Contiene: Header, Vinilo flotante, Info centrada,
 * Slider completo, Controles expandidos. Los Tabs (Letra/Info/Enlaces) están ocultos debajo
 * y se acceden mediante scroll.
 */
@Composable
fun ExpandedPlayerLayout(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   interpolatedValues: PlayerInterpolatedValues,
   onCollapseClick: () -> Unit,
   modifier: Modifier = Modifier,
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
   
   Box(modifier = modifier.fillMaxSize()) {
      Column(
         modifier = Modifier
            .fillMaxSize()
            .alpha(interpolatedValues.expandedLayoutAlpha)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp, top = 30.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
      ) {
         // Header
         Row(
            modifier = Modifier.fillMaxWidth(),
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
         
         // Info de la canción
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
               color = MaterialTheme.colorScheme.onSurface,
               textAlign = TextAlign.Center,
               maxLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
               text = state.artistDisplay,
               style = MaterialTheme.typography.bodyLarge,
               color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
               textAlign = TextAlign.Center,
               maxLines = 1,
            )
         }
         
         // Slider de progreso
         ProgressSlider(
            state = state,
            onSeekStart = { pos -> onEvent(PlayerEvent.Seek.Start(pos)) },
            onSeekUpdate = { pos -> onEvent(PlayerEvent.Seek.Update(pos)) },
            onSeekFinish = { pos -> onEvent(PlayerEvent.Seek.Finish(pos)) },
            modifier = Modifier.fillMaxWidth(),
         )
         
         // Controles de reproducción
         PlaybackControls(state = state, onEvent = onEvent, modifier = Modifier.fillMaxWidth())
         
         Spacer(modifier = Modifier.height(15.dp))
         
         // Indicador visual de que hay más contenido abajo
         if (canScrollDown && scrollState.value == 0) {
            Column(
               horizontalAlignment = Alignment.CenterHorizontally,
               modifier = Modifier
                  .fillMaxWidth()
                  .alpha(0.5f)
            ) {
               Text(
                  text = "Desliza para ver más",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface,
               )
               Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Scroll down",
                  tint = MaterialTheme.colorScheme.onSurface,
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
      
      // Botón flotante para volver arriba (cuando se ha scrolleado)
      if (canScrollUp) {
         IconButton(
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
               .align(Alignment.BottomEnd)
               .padding(16.dp)
               .alpha(0.8f)
         ) {
            Icon(
               imageVector = Icons.Default.KeyboardArrowUp,
               contentDescription = "Scroll to top",
               tint = MaterialTheme.colorScheme.onSurface,
            )
         }
      }
   }
}

@Preview(
   name = "🌙 Expanded Layout - Scrollable",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedLayoutScrollable() {
   FreePlayerMTheme(darkTheme = true) {
      Surface(color = MaterialTheme.colorScheme.surface) {
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
         )
      }
   }
}

@Preview(
   name = "☀️ Expanded Layout - Light Theme",
   showBackground = true,
   backgroundColor = 0xFFF3EEFF,
   heightDp = 800,
)
@Composable
private fun PreviewExpandedLayoutLight() {
   FreePlayerMTheme(darkTheme = false) {
      Surface(color = MaterialTheme.colorScheme.surface) {
         ExpandedPlayerLayout(
            state = PlayerState(
               currentSong = SongWithArtist.preview(
                  titulo = "Stairway to Heaven",
                  artista = "Led Zeppelin",
               ),
               isPlaying = true,
               panelMode = PlayerPanelMode.EXPANDED,
               activeTab = ExpandedTab.INFO,
               isFavorite = false,
            ),
            onEvent = {},
            interpolatedValues = rememberInterpolatedValues(progress = 1f, screenHeightDp = 800.dp),
            onCollapseClick = {},
         )
      }
   }
}