package com.example.freeplayerm.ui.features.player.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.model.ExpandedTab
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎨 PLAYER CONTENT - Stateless Orchestrator
 *
 * Composable stateless que orquesta el panel del reproductor.
 * No tiene lógica, solo estructura visual.
 */
@Composable
fun PlayerContent(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   modifier: Modifier = Modifier,
) {
   Box(
      modifier = modifier
         .fillMaxSize()
         .background(Color.Transparent),
      contentAlignment = Alignment.BottomCenter,
   ) {
      // El panel ocupa la parte inferior y crece según el modo
      PlayerPanel(
         state = state,
         onEvent = onEvent,
      )
   }
}

@Preview(name = "Player Content - Normal", showBackground = true)
@Composable
private fun PreviewPlayerContentNormal() {
   FreePlayerMTheme(darkTheme = true) {
      PlayerContent(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Bohemian Rhapsody",
               artista = "Queen",
            ),
            isPlaying = true,
            currentPositionMs = 125000L,
            panelMode = PlayerPanelMode.NORMAL,
         ),
         onEvent = {},
      )
   }
}

@Preview(name = "Player Content - Expanded", showBackground = true, heightDp = 800)
@Composable
private fun PreviewPlayerContentExpanded() {
   FreePlayerMTheme(darkTheme = true) {
      PlayerContent(
         state = PlayerState(
            currentSong = SongWithArtist.preview(
               titulo = "Stairway to Heaven",
               artista = "Led Zeppelin",
            ),
            isPlaying = false,
            panelMode = PlayerPanelMode.EXPANDED,
            activeTab = ExpandedTab.LYRICS,
            lyrics = "Is this the real life?\nIs this just fantasy?",
         ),
         onEvent = {},
      )
   }
}