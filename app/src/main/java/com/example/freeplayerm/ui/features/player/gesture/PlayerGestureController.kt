package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import kotlinx.coroutines.launch

// Constantes del sistema de gestos del reproductor
object PlayerGestureConstants {
   // Fracciones de altura de pantalla
   const val HEIGHT_FRACTION_MINIMIZED = 0.10f
   const val HEIGHT_FRACTION_NORMAL = 0.20f
   const val HEIGHT_FRACTION_EXPANDED = 1.0f
   
   // Umbral de transición crossfade (50%)
   const val TRANSITION_THRESHOLD = 0.5f
   
   // Distancia mínima para activar gesto (dp)
   const val MIN_DRAG_DISTANCE_DP = 16f
   
   // Spring para animación snap (drag end)
   const val SNAP_SPRING_STIFFNESS = Spring.StiffnessMediumLow
   const val SNAP_SPRING_DAMPING = Spring.DampingRatioMediumBouncy
   
   // Spring para animación tap (más rápida)
   const val TAP_SPRING_STIFFNESS = Spring.StiffnessMedium
   const val TAP_SPRING_DAMPING = Spring.DampingRatioMediumBouncy
   
   // Calcula altura en px basada en fracción de pantalla
   fun calculateHeightPx(fraction: Float, screenHeightPx: Float): Float {
      return screenHeightPx * fraction
   }
}

// Controlador de gestos verticales del panel del reproductor
// Maneja drag vertical, animaciones y transiciones entre NORMAL/EXPANDED
@Stable
class PlayerGestureController(
   private val screenHeightPx: Float,
   initialMode: PlayerPanelMode = PlayerPanelMode.NORMAL,
) {
   private val normalHeightPx: Float =
      screenHeightPx * PlayerGestureConstants.HEIGHT_FRACTION_NORMAL
   
   // Progreso animable [0, 1] donde 0=NORMAL, 1=EXPANDED
   val progress = Animatable(if (initialMode == PlayerPanelMode.EXPANDED) 1f else 0f)
   
   // Estados de interacción
   var isDragging by mutableStateOf(false)
      private set
   
   var isAnimating by mutableStateOf(false)
      private set
   
   // Tracking de haptic para evitar duplicados
   private var lastHapticProgress by mutableFloatStateOf(-1f)
   
   // region Computed Properties
   
   // Modo actual basado en progreso >= 50%
   val currentMode: PlayerPanelMode
      get() = if (progress.value >= PlayerGestureConstants.TRANSITION_THRESHOLD) {
         PlayerPanelMode.EXPANDED
      } else {
         PlayerPanelMode.NORMAL
      }
   
   // Progreso actual normalizado [0.0 - 1.0]
   val currentProgress: Float
      get() = progress.value.coerceIn(0f, 1f)
   
   // Altura actual del panel en píxeles
   val currentHeightPx: Float
      get() {
         val p = progress.value.coerceIn(0f, 1f)
         return normalHeightPx + (screenHeightPx - normalHeightPx) * p
      }
   
   // Si el panel está efectivamente expandido (>50%)
   val isExpanded: Boolean
      get() = currentMode == PlayerPanelMode.EXPANDED
   
   // Si está en transición (dragging o animating)
   val isTransitioning: Boolean
      get() = isDragging || isAnimating
   
   // Modo al que se snappeará si se suelta ahora
   val targetModeIfReleased: PlayerPanelMode
      get() = if (progress.value >= PlayerGestureConstants.TRANSITION_THRESHOLD) {
         PlayerPanelMode.EXPANDED
      } else {
         PlayerPanelMode.NORMAL
      }
   
   // region Drag Handlers
   
   // Inicia el drag - detiene animaciones y marca estado
   suspend fun onDragStart() {
      progress.stop()
      isDragging = true
      isAnimating = false
      lastHapticProgress = -1f
   }
   
   // Actualiza el progreso durante el drag
   // deltaY negativo = hacia arriba = expandir
   suspend fun onDrag(deltaY: Float, haptic: HapticFeedback? = null) {
      val range = screenHeightPx - normalHeightPx
      if (range <= 0) return
      
      val deltaProgress = -deltaY / range
      val newProgress = (progress.value + deltaProgress).coerceIn(0f, 1f)
      
      progress.snapTo(newProgress)
      
      haptic?.let { checkThresholdHaptic(newProgress, it) }
   }
   
   // Finaliza el drag y anima al snap point
   suspend fun onDragEnd(
      haptic: HapticFeedback? = null,
      onModeChanged: ((PlayerPanelMode) -> Unit)? = null,
   ) {
      isDragging = false
      isAnimating = true
      
      val targetProgress = resolveTargetProgress()
      val targetMode = if (targetProgress >= PlayerGestureConstants.TRANSITION_THRESHOLD) {
         PlayerPanelMode.EXPANDED
      } else {
         PlayerPanelMode.NORMAL
      }
      
      haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
      
      progress.animateTo(
         targetValue = targetProgress,
         animationSpec = spring(
            dampingRatio = PlayerGestureConstants.SNAP_SPRING_DAMPING,
            stiffness = PlayerGestureConstants.SNAP_SPRING_STIFFNESS,
         )
      )
      
      isAnimating = false
      onModeChanged?.invoke(targetMode)
   }
   
   // Cancela el drag y vuelve al estado anterior
   suspend fun onDragCancel() {
      isDragging = false
      isAnimating = true
      
      val targetProgress = resolveTargetProgress()
      
      progress.animateTo(
         targetValue = targetProgress,
         animationSpec = spring(
            dampingRatio = PlayerGestureConstants.SNAP_SPRING_DAMPING,
            stiffness = PlayerGestureConstants.SNAP_SPRING_STIFFNESS,
         )
      )
      
      isAnimating = false
   }
   
   // endregion
   
   // region Programmatic Animations
   
   // Expande el panel con animación (para tap en vinilo/título)
   suspend fun expand(
      haptic: HapticFeedback? = null,
      onModeChanged: ((PlayerPanelMode) -> Unit)? = null,
   ) {
      if (isDragging || progress.value >= 0.99f) return
      
      isAnimating = true
      haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
      
      progress.animateTo(
         targetValue = 1f,
         animationSpec = spring(
            dampingRatio = PlayerGestureConstants.TAP_SPRING_DAMPING,
            stiffness = PlayerGestureConstants.TAP_SPRING_STIFFNESS,
         )
      )
      
      isAnimating = false
      onModeChanged?.invoke(PlayerPanelMode.EXPANDED)
   }
   
   // Colapsa el panel con animación (para tap en botón colapsar)
   suspend fun collapse(
      haptic: HapticFeedback? = null,
      onModeChanged: ((PlayerPanelMode) -> Unit)? = null,
   ) {
      if (isDragging || progress.value <= 0.01f) return
      
      isAnimating = true
      haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
      
      progress.animateTo(
         targetValue = 0f,
         animationSpec = spring(
            dampingRatio = PlayerGestureConstants.TAP_SPRING_DAMPING,
            stiffness = PlayerGestureConstants.TAP_SPRING_STIFFNESS,
         )
      )
      
      isAnimating = false
      onModeChanged?.invoke(PlayerPanelMode.NORMAL)
   }
   
   // Anima a un modo específico
   suspend fun animateToMode(
      mode: PlayerPanelMode,
      haptic: HapticFeedback? = null,
      onModeChanged: ((PlayerPanelMode) -> Unit)? = null,
   ) {
      if (mode == PlayerPanelMode.EXPANDED) {
         expand(haptic, onModeChanged)
      } else {
         collapse(haptic, onModeChanged)
      }
   }
   
   // endregion
   
   // region Synchronization
   
   // Sincroniza el progreso con un modo externo sin animación
   suspend fun syncToMode(mode: PlayerPanelMode) {
      if (isDragging || isAnimating) return
      
      val targetProgress = if (mode == PlayerPanelMode.EXPANDED) 1f else 0f
      
      progress.snapTo(targetProgress)
   }
   
   // Sincroniza con un progreso específico sin animación
   suspend fun syncToProgress(value: Float) {
      if (isDragging || isAnimating) return
      progress.snapTo(value.coerceIn(0f, 1f))
   }
   
   // endregion
   
   // region Private Helpers
   
   private fun resolveTargetProgress(): Float {
      return if (progress.value >= PlayerGestureConstants.TRANSITION_THRESHOLD) 1f else 0f
   }
   
   private fun checkThresholdHaptic(currentProgress: Float, haptic: HapticFeedback) {
      val threshold = PlayerGestureConstants.TRANSITION_THRESHOLD
      val crossedThreshold = (lastHapticProgress < threshold && currentProgress >= threshold) ||
            (lastHapticProgress >= threshold && currentProgress < threshold)
      
      if (crossedThreshold) {
         haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
         lastHapticProgress = currentProgress
      }
   }
   
   // endregion
}

// Crea y recuerda un PlayerGestureController
@Composable
fun rememberPlayerGestureController(
   screenHeightPx: Float,
   initialMode: PlayerPanelMode = PlayerPanelMode.NORMAL,
): PlayerGestureController {
   return remember(screenHeightPx) {
      PlayerGestureController(
         screenHeightPx = screenHeightPx,
         initialMode = initialMode,
      )
   }
}

// Modifier que añade gestos de drag vertical al panel
fun Modifier.playerDragGesture(
   controller: PlayerGestureController,
   enabled: Boolean = true,
   onModeChanged: ((PlayerPanelMode) -> Unit)? = null,
): Modifier = composed {
   if (!enabled) return@composed this
   
   val haptic = LocalHapticFeedback.current
   val density = LocalDensity.current
   val scope = rememberCoroutineScope()
   
   this.pointerInput(controller) {
      detectVerticalDragGestures(
         onDragStart = {
            scope.launch { controller.onDragStart() }
         },
         onDragEnd = {
            scope.launch {
               controller.onDragEnd(
                  haptic = haptic,
                  onModeChanged = onModeChanged,
               )
            }
         },
         onDragCancel = {
            scope.launch { controller.onDragCancel() }
         },
         onVerticalDrag = { change, dragAmount ->
            change.consume()
            scope.launch {
               controller.onDrag(deltaY = dragAmount, haptic = haptic)
            }
         },
      )
   }
}