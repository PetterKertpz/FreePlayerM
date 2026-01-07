package com.example.freeplayerm.ui.features.player.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.components.*
import com.example.freeplayerm.ui.features.player.gesture.HorizontalSwipeController
import com.example.freeplayerm.ui.features.player.gesture.PlayerInterpolatedValues
import com.example.freeplayerm.ui.features.player.gesture.rememberHorizontalSwipeController
import com.example.freeplayerm.ui.features.player.gesture.rememberInterpolatedValues
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerState
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

   Box(
      modifier =
         modifier.alpha(interpolatedValues.normalLayoutAlpha).graphicsLayer {
            translationX = swipeController.offsetX.value
         }
   ) {
      Column(modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp)) {
         Row(
            modifier = Modifier.fillMaxWidth().weight(2f),
            horizontalArrangement = Arrangement.SpaceBetween,
         ) {
            SpinningVinyl(
               cancion = currentSong,
               estaReproduciendo = state.isPlaying,
               modifier =
                  Modifier.weight(0.7f)
                     .fillMaxWidth()
                     .clickableWithoutRipple { onExpandClick() }
                     .padding(top = 5.dp, end = 5.dp),
            )

            Column(
               modifier =
                  Modifier.weight(1.5f).fillMaxWidth().clickableWithoutRipple { onExpandClick() },
               verticalArrangement = Arrangement.Center,
            ) {
               Text(
                  text = state.titleDisplay,
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(top = 5.dp),
               )
               Spacer(modifier = Modifier.height(2.dp))
               Text(
                  text = state.artistDisplay,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
               )
            }

            Column(
               modifier = Modifier.weight(1f).fillMaxWidth(),
               verticalArrangement = Arrangement.SpaceBetween,
               horizontalAlignment = Alignment.CenterHorizontally,
            ) {
               CompactControls(
                  state = state,
                  onEvent = onEvent,
                  modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically),
               )
            }
         }

         CompactProgressSlider(
            state = state,
            onSeekStart = { position -> onEvent(PlayerEvent.Seek.Start(position)) },
            onSeekUpdate = { position -> onEvent(PlayerEvent.Seek.Update(position)) },
            onSeekFinish = { position -> onEvent(PlayerEvent.Seek.Finish(position)) },
            showTimes = false,
            modifier = Modifier.fillMaxWidth(),
         )

         Spacer(modifier = Modifier.height(15.dp))
      }
   }
}


@Preview(name = "🌙 Normal Layout - Playing", showBackground = true, backgroundColor = 0xFFF3EEFF)
@Composable
private fun PreviewNormalLayoutPlaying() {
   FreePlayerMTheme(darkTheme = true) {
      Surface(color = MaterialTheme.colorScheme.surface) {
         NormalPlayerLayout(
            state =
               PlayerState(
                  currentSong =
                     SongWithArtist.preview(
                        titulo = "Hotel California",
                        artista = "Eagles",
                        esFavorita = true,
                     ),
                  isPlaying = true,
                  currentPositionMs = 180000L,
               ),
            onEvent = {},
            interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
            swipeController = rememberHorizontalSwipeController(),
            onExpandClick = {},
            modifier = Modifier.height(140.dp),
         )
      }
   }
}

@Preview(name = "☀️ Normal Layout - Paused", showBackground = true, backgroundColor = 0xFFF3EEFF)
@Composable
private fun PreviewNormalLayoutPaused() {
   FreePlayerMTheme(darkTheme = false) {
      Surface(color = MaterialTheme.colorScheme.surface) {
         NormalPlayerLayout(
            state =
               PlayerState(
                  currentSong = SongWithArtist.preview(titulo = "Imagine", artista = "John Lennon"),
                  isPlaying = false,
                  currentPositionMs = 90000L,
                  isFavorite = false,
               ),
            onEvent = {},
            interpolatedValues = rememberInterpolatedValues(progress = 0f, screenHeightDp = 800.dp),
            swipeController = rememberHorizontalSwipeController(),
            onExpandClick = {},
            modifier = Modifier.height(140.dp),
         )
      }
   }
}
