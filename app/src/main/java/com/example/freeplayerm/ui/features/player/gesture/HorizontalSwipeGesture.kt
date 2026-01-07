package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.player.model.SwipeDirection
import kotlinx.coroutines.launch
import kotlin.math.abs

// Constantes para gestos horizontales
object HorizontalSwipeConstants {
   // Umbral mínimo para activar swipe (dp)
   const val MIN_SWIPE_THRESHOLD_DP = 80f
   
   // Umbral de velocidad para swipe rápido (dp/ms)
   const val VELOCITY_THRESHOLD = 0.8f
   
   // Máximo desplazamiento visual permitido (dp)
   const val MAX_OFFSET_DP = 120f
   
   // Resistencia del rubber band effect
   const val RUBBER_BAND_RESISTANCE = 0.4f
   
   // Spring para animación de retorno
   const val RETURN_SPRING_STIFFNESS = Spring.StiffnessMedium
   const val RETURN_SPRING_DAMPING = Spring.DampingRatioMediumBouncy
   
   // Spring para animación de salida (swipe exitoso)
   const val EXIT_SPRING_STIFFNESS = Spring.StiffnessLow
   const val EXIT_SPRING_DAMPING = Spring.DampingRatioLowBouncy
}

// Controlador para gestos horizontales de swipe
// Maneja navegación entre canciones con feedback visual
@Stable
class HorizontalSwipeController(
   private val maxOffsetDp: Float = HorizontalSwipeConstants.MAX_OFFSET_DP
) {
   // Offset horizontal animable (en dp)
   val offsetX = Animatable(0f)
   
   // Estados de interacción
   var isDragging by mutableStateOf(false)
      private set
   
   var isAnimating by mutableStateOf(false)
      private set
   
   // Tracking de velocidad
   private var dragStartTime = 0L
   private var totalDragDistance = 0f
   
   // Último haptic emitido
   private var lastHapticDirection: SwipeDirection? by mutableStateOf(null)
   
   // region Computed Properties
   
   // Offset actual en Dp
   val offsetDp: Dp
      get() = offsetX.value.dp
   
   // Progreso del swipe normalizado [-1, 1]
   val swipeProgress: Float
      get() = (offsetX.value / maxOffsetDp).coerceIn(-1f, 1f)
   
   // Alpha para indicador de siguiente (lado izquierdo)
   val nextIndicatorAlpha: Float
      get() = (-swipeProgress).coerceIn(0f, 1f)
   
   // Alpha para indicador de anterior (lado derecho)
   val previousIndicatorAlpha: Float
      get() = swipeProgress.coerceIn(0f, 1f)
   
   // Dirección actual basada en offset
   val currentDirection: SwipeDirection?
      get() = when {
         offsetX.value < -HorizontalSwipeConstants.MIN_SWIPE_THRESHOLD_DP -> SwipeDirection.LEFT
         offsetX.value > HorizontalSwipeConstants.MIN_SWIPE_THRESHOLD_DP -> SwipeDirection.RIGHT
         else -> null
      }
   
   // Si hay suficiente desplazamiento para activar swipe
   val hasReachedThreshold: Boolean
      get() = abs(offsetX.value) >= HorizontalSwipeConstants.MIN_SWIPE_THRESHOLD_DP
   
   // Si hay cualquier interacción activa
   val isInteracting: Boolean
      get() = isDragging || isAnimating
   
   // endregion
   
   // region Drag Handlers
   
   // Inicia el drag horizontal
   suspend fun onDragStart() {
      offsetX.stop()
      isDragging = true
      isAnimating = false
      dragStartTime = System.currentTimeMillis()
      totalDragDistance = 0f
      lastHapticDirection = null
   }
   
   // Actualiza el offset durante el drag con rubber band effect
   suspend fun onDrag(deltaX: Float, haptic: HapticFeedback? = null) {
      totalDragDistance += deltaX
      
      val currentOffset = offsetX.value
      val resistance = calculateResistance(currentOffset)
      val adjustedDelta = deltaX * resistance
      
      val newOffset = (currentOffset + adjustedDelta).coerceIn(-maxOffsetDp, maxOffsetDp)
      offsetX.snapTo(newOffset)
      
      haptic?.let { checkThresholdHaptic(it) }
   }
   
   // Finaliza el drag y determina la acción
   // Retorna dirección del swipe si fue exitoso, null si se cancela
   suspend fun onDragEnd(haptic: HapticFeedback? = null): SwipeDirection? {
      isDragging = false
      
      val dragDuration = System.currentTimeMillis() - dragStartTime
      val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
      
      val direction = resolveSwipeDirection(velocity)
      
      isAnimating = true
      
      if (direction != null) {
         haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
         
         val exitOffset = if (direction == SwipeDirection.LEFT) -maxOffsetDp else maxOffsetDp
         
         offsetX.animateTo(
            targetValue = exitOffset,
            animationSpec = spring(
               dampingRatio = HorizontalSwipeConstants.EXIT_SPRING_DAMPING,
               stiffness = HorizontalSwipeConstants.EXIT_SPRING_STIFFNESS,
            ),
         )
      }
      
      offsetX.animateTo(
         targetValue = 0f,
         animationSpec = spring(
            dampingRatio = HorizontalSwipeConstants.RETURN_SPRING_DAMPING,
            stiffness = HorizontalSwipeConstants.RETURN_SPRING_STIFFNESS,
         ),
      )
      
      isAnimating = false
      lastHapticDirection = null
      
      return direction
   }
   
   // Cancela el drag y regresa al centro
   suspend fun onDragCancel() {
      isDragging = false
      isAnimating = true
      
      offsetX.animateTo(
         targetValue = 0f,
         animationSpec = spring(
            dampingRatio = HorizontalSwipeConstants.RETURN_SPRING_DAMPING,
            stiffness = HorizontalSwipeConstants.RETURN_SPRING_STIFFNESS,
         ),
      )
      
      isAnimating = false
      lastHapticDirection = null
   }
   
   // Resetea el estado del controlador
   suspend fun reset() {
      offsetX.snapTo(0f)
      isDragging = false
      isAnimating = false
      lastHapticDirection = null
   }
   
   // endregion
   
   // region Private Helpers
   
   private fun calculateResistance(currentOffset: Float): Float {
      val progress = abs(currentOffset) / maxOffsetDp
      return 1f - (progress * HorizontalSwipeConstants.RUBBER_BAND_RESISTANCE)
   }
   
   private fun resolveSwipeDirection(velocity: Float): SwipeDirection? {
      val currentOffset = offsetX.value
      val threshold = HorizontalSwipeConstants.MIN_SWIPE_THRESHOLD_DP
      val velocityThreshold = HorizontalSwipeConstants.VELOCITY_THRESHOLD
      
      return when {
         velocity < -velocityThreshold && currentOffset < 0 -> SwipeDirection.LEFT
         velocity > velocityThreshold && currentOffset > 0 -> SwipeDirection.RIGHT
         currentOffset <= -threshold -> SwipeDirection.LEFT
         currentOffset >= threshold -> SwipeDirection.RIGHT
         else -> null
      }
   }
   
   private fun checkThresholdHaptic(haptic: HapticFeedback) {
      val newDirection = currentDirection
      
      if (newDirection != lastHapticDirection && newDirection != null) {
         haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
         lastHapticDirection = newDirection
      } else if (newDirection == null && lastHapticDirection != null) {
         lastHapticDirection = null
      }
   }
   
   // endregion
}

// Crea y recuerda un HorizontalSwipeController
@Composable
fun rememberHorizontalSwipeController(
   maxOffsetDp: Float = HorizontalSwipeConstants.MAX_OFFSET_DP
): HorizontalSwipeController {
   return remember(maxOffsetDp) { HorizontalSwipeController(maxOffsetDp = maxOffsetDp) }
}

// Modifier que añade gestos de swipe horizontal con controlador
fun Modifier.horizontalSwipeGesture(
   controller: HorizontalSwipeController,
   enabled: Boolean = true,
   onSwipe: (SwipeDirection) -> Unit = {},
): Modifier = composed {
   if (!enabled) return@composed this
   
   val haptic = LocalHapticFeedback.current
   val scope = rememberCoroutineScope()
   
   this.pointerInput(controller) {
      detectHorizontalDragGestures(
         onDragStart = {
            scope.launch { controller.onDragStart() }
         },
         onDragEnd = {
            scope.launch {
               val direction = controller.onDragEnd(haptic = haptic)
               direction?.let { onSwipe(it) }
            }
         },
         onDragCancel = {
            scope.launch { controller.onDragCancel() }
         },
         onHorizontalDrag = { change, dragAmount ->
            change.consume()
            scope.launch {
               controller.onDrag(deltaX = dragAmount, haptic = haptic)
            }
         },
      )
   }
}

// Modifier simplificado para swipe horizontal sin controlador externo
fun Modifier.onHorizontalSwipe(
   enabled: Boolean = true,
   onSwipe: (SwipeDirection) -> Unit,
): Modifier = composed {
   if (!enabled) return@composed this
   
   val haptic = LocalHapticFeedback.current
   val density = LocalDensity.current
   val thresholdPx = with(density) {
      HorizontalSwipeConstants.MIN_SWIPE_THRESHOLD_DP.dp.toPx()
   }
   var totalDrag by remember { mutableFloatStateOf(0f) }
   
   this.pointerInput(thresholdPx) {
      detectHorizontalDragGestures(
         onDragStart = { totalDrag = 0f },
         onDragEnd = {
            val direction = when {
               totalDrag < -thresholdPx -> SwipeDirection.LEFT
               totalDrag > thresholdPx -> SwipeDirection.RIGHT
               else -> null
            }
            direction?.let {
               haptic.performHapticFeedback(HapticFeedbackType.LongPress)
               onSwipe(it)
            }
            totalDrag = 0f
         },
         onDragCancel = { totalDrag = 0f },
         onHorizontalDrag = { change, dragAmount ->
            change.consume()
            totalDrag += dragAmount
         },
      )
   }
}