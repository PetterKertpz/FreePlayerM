package com.example.freeplayerm.ui.features.player.layouts

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.gesture.PlayerGestureConstants
import com.example.freeplayerm.ui.features.player.gesture.rememberInterpolatedValues
import com.example.freeplayerm.ui.features.player.gesture.rememberPlayerGestureController
import com.example.freeplayerm.ui.features.player.gesture.playerDragGesture
import com.example.freeplayerm.ui.features.player.gesture.rememberHorizontalSwipeController
import com.example.freeplayerm.ui.features.player.gesture.horizontalSwipeGesture
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.launch

/**
 * 🎛️ PLAYER PANEL - Gesture Handler + Interpolation Layer
 *
 * Maneja:
 * - Gestos verticales (expandir/colapsar)
 * - Gestos horizontales (cambiar canción)
 * - Interpolación de valores para transiciones suaves
 * - Sincronización con estado del ViewModel
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlayerPanel(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   modifier: Modifier = Modifier,
) {
   if (!state.hasSong) return
   
   val scope = rememberCoroutineScope()
   val haptic = LocalHapticFeedback.current
   val density = LocalDensity.current
   val configuration = LocalConfiguration.current
   
   val screenHeightDp = configuration.screenHeightDp.dp
   val screenHeightPx = with(density) { screenHeightDp.toPx() }
   
   // Controllers
   // Controllers
   val gestureController = rememberPlayerGestureController(
      screenHeightPx = screenHeightPx,
      initialMode = state.panelMode,
   )
   
   val swipeController = rememberHorizontalSwipeController()
   
   // Altura animada para minimización por scroll
   val minimizedHeightDp = screenHeightDp * PlayerGestureConstants.HEIGHT_FRACTION_MINIMIZED
   val normalHeightDp = screenHeightDp * PlayerGestureConstants.HEIGHT_FRACTION_NORMAL
   
   val targetMinimizeHeight by animateDpAsState(
      targetValue = if (state.isMinimizedByScroll && state.isNormal) minimizedHeightDp else normalHeightDp,
      animationSpec = spring(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessMediumLow,
      ),
      label = "minimizeHeight"
   )
   
   val minimizeAlpha by animateFloatAsState(
      targetValue = if (state.isMinimizedByScroll && state.isNormal) 0.7f else 1f,
      animationSpec = tween(durationMillis = 200),
      label = "minimizeAlpha"
   )
   
   // Valores interpolados basados en el progress del gesture
   val interpolatedValues = rememberInterpolatedValues(
      progress = gestureController.currentProgress,
      screenHeightDp = screenHeightDp,
      isDragging = gestureController.isDragging,
   )
   
   // Altura final del panel (considera minimización solo en modo NORMAL)
   val finalPanelHeight = if (state.isNormal && !gestureController.isTransitioning) {
      targetMinimizeHeight
   } else {
      interpolatedValues.panelHeightDp
   }
   
   // Sincronización unificada de gestos y modo
   LaunchedEffect(state.panelMode, gestureController.isDragging, gestureController.isAnimating) {
      when {
         // Notificar inicio de drag
         gestureController.isDragging -> {
            onEvent(PlayerEvent.Panel.Gesture.Started)
         }
         // Sincronizar modo cuando no hay transición activa
         !gestureController.isTransitioning -> {
            gestureController.syncToMode(state.panelMode)
         }
      }
   }
   
   Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.BottomCenter,
   ) {
      // Background dim cuando está expandido
      if (interpolatedValues.backgroundDimAlpha > 0.01f) {
         Box(
            modifier = Modifier
               .fillMaxSize()
               .background(Color.Black.copy(alpha = interpolatedValues.backgroundDimAlpha))
               .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  enabled = state.isExpanded,
               ) {
                  scope.launch {
                     gestureController.collapse(
                        haptic = haptic,
                        onModeChanged = { mode ->
                           onEvent(PlayerEvent.Panel.SetMode(mode))
                        },
                     )
                  }
               }
         )
      }
      
      // Panel principal con gestos
      Column(
         modifier = Modifier
            .fillMaxWidth()
            .height(finalPanelHeight)
            .shadow(
               elevation = 16.dp,
               shape = RoundedCornerShape(
                  topStart = interpolatedValues.cornerRadiusDp,
                  topEnd = interpolatedValues.cornerRadiusDp,
               ),
            )
            .clip(
               RoundedCornerShape(
                  topStart = interpolatedValues.cornerRadiusDp,
                  topEnd = interpolatedValues.cornerRadiusDp,
               )
            )
            .background(MaterialTheme.colorScheme.surface)
            .graphicsLayer { alpha = minimizeAlpha }
            .playerDragGesture(
               controller = gestureController,
               enabled = state.canInteract && !state.isScrubbing && !state.isMinimizedByScroll,
               onModeChanged = { mode ->
                  onEvent(
                     PlayerEvent.Panel.Gesture.Ended(
                        targetMode = mode
                     )
                  )
                  onEvent(PlayerEvent.Panel.SetMode(mode))
               },
            )
            .horizontalSwipeGesture(
               controller = swipeController,
               enabled = state.canInteract && !gestureController.isDragging && !state.isExpanded,
               onSwipe = { direction ->
                  onEvent(PlayerEvent.Swipe.Horizontal(direction))
               },
            )
      ) {
         // Layouts con crossfade basado en interpolación
         Box(modifier = Modifier.fillMaxSize()) {
            // Normal Layout - solo si es visible
            if (interpolatedValues.shouldShowNormalLayout) {
               NormalPlayerLayout(
                  state = state,
                  onEvent = onEvent,
                  interpolatedValues = interpolatedValues,
                  swipeController = swipeController,
                  onExpandClick = {
                     scope.launch {
                        gestureController.expand(
                           haptic = haptic,
                           onModeChanged = { mode ->
                              onEvent(PlayerEvent.Panel.SetMode(mode))
                           },
                        )
                     }
                  },
                  modifier = Modifier.fillMaxSize(),
               )
            }
            
            // Expanded Layout - solo si es visible
            if (interpolatedValues.shouldShowExpandedLayout) {
               ExpandedPlayerLayout(
                  state = state,
                  onEvent = onEvent,
                  interpolatedValues = interpolatedValues,
                  onCollapseClick = {
                     scope.launch {
                        gestureController.collapse(
                           haptic = haptic,
                           onModeChanged = { mode ->
                              onEvent(PlayerEvent.Panel.SetMode(mode))
                           },
                        )
                     }
                  },
                  modifier = Modifier.fillMaxSize(),
               )
            }
         }
      }
   }
}

@Preview(name = "Player Panel - Transition 25%", showBackground = true, heightDp = 600)
@Composable
private fun PreviewPlayerPanelTransition25() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
         PlayerPanel(
            state = PlayerState(
               currentSong = SongWithArtist.preview(),
               isPlaying = true,
               gestureProgress = 0.25f,
               panelMode = PlayerPanelMode.NORMAL,
            ),
            onEvent = {},
         )
      }
   }
}

@Preview(name = "Player Panel - Transition 75%", showBackground = true, heightDp = 600)
@Composable
private fun PreviewPlayerPanelTransition75() {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
         PlayerPanel(
            state = PlayerState(
               currentSong = SongWithArtist.preview(),
               isPlaying = true,
               gestureProgress = 0.75f,
               panelMode = PlayerPanelMode.EXPANDED,
            ),
            onEvent = {},
         )
      }
   }
}