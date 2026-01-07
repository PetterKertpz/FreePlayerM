package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

// Interpolador de valores para transiciones del reproductor
object PlayerInterpolator {
   
   private const val THRESHOLD = PlayerGestureConstants.TRANSITION_THRESHOLD
   
   private object InterpolationConstants {
      const val CENTER_PROGRESS_START = 0.2f
      const val TABS_APPEAR_THRESHOLD = 0.7f
      const val VINYL_FLOAT_START = 0.3f
      const val VINYL_FLOAT_AMPLITUDE_DP = 8f
      const val VINYL_GLOW_SCALE_MAX = 1.08f
      const val INFO_SCALE_MAX = 1.1f
      const val PANEL_CORNER_RADIUS_NORMAL = 24f
      const val TABS_OFFSET_INITIAL = 40f
      const val EASING_OVERSHOOT_DEFAULT = 1.70158f
      const val EASING_OVERSHOOT_VINYL = 0.3f
      const val EASING_OVERSHOOT_TABS = 0.6f
   }
   
   fun calculate(
      progress: Float,
      screenHeightDp: Dp,
      isDragging: Boolean = false,
   ): PlayerInterpolatedValues {
      val p = progress.coerceIn(0f, 1f)
      
      val normalHeightDp = Dp(screenHeightDp.value * PlayerGestureConstants.HEIGHT_FRACTION_NORMAL)
      
      return PlayerInterpolatedValues(
         // Layouts
         normalLayoutAlpha = calculateNormalAlpha(p),
         expandedLayoutAlpha = calculateExpandedAlpha(p),
         
         // Panel
         panelHeightDp = calculatePanelHeight(p, screenHeightDp, normalHeightDp),
         backgroundDimAlpha = calculateBackgroundDim(p),
         cornerRadiusDp = calculateCornerRadius(p),
         
         // Vinyl
         vinylSizeDp = calculateVinylSize(p, screenHeightDp, normalHeightDp),
         vinylCenterProgress = calculateCenterProgress(p),
         vinylFloatOffsetY = calculateVinylFloat(p, isDragging),
         vinylGlowAlpha = calculateGlowAlpha(p),
         vinylGlowScale = calculateGlowScale(p),
         
         // Info Text
         infoScale = calculateInfoScale(p),
         infoCenterProgress = calculateCenterProgress(p),
         
         // Controls
         compactControlsAlpha = calculateNormalAlpha(p),
         expandedControlsAlpha = calculateExpandedAlpha(p),
         
         // Slider
         compactSliderAlpha = calculateNormalAlpha(p),
         fullSliderAlpha = calculateExpandedAlpha(p),
         
         // Tabs
         tabsAlpha = calculateTabsAlpha(p),
         tabsOffsetY = calculateTabsOffset(p),
         
         // Progress helpers
         transitionProgress = p,
         isInNormalZone = p < THRESHOLD,
         isInExpandedZone = p >= THRESHOLD,
      )
   }
   
   // Alpha del layout NORMAL: 0% → 1.0, 50% → 0.0
   private fun calculateNormalAlpha(progress: Float): Float {
      return (1f - (progress / THRESHOLD)).coerceIn(0f, 1f)
   }
   
   // Alpha del layout EXPANDED: 50% → 0.0, 100% → 1.0
   private fun calculateExpandedAlpha(progress: Float): Float {
      return ((progress - THRESHOLD) / (1f - THRESHOLD)).coerceIn(0f, 1f)
   }
   
   // region Panel
   
   private fun calculatePanelHeight(progress: Float, expandedHeight: Dp, normalHeight: Dp): Dp {
      return lerp(normalHeight, expandedHeight, easeOutCubic(progress))
   }
   
   private fun calculateBackgroundDim(progress: Float): Float {
      return (progress * 0.6f).coerceIn(0f, 0.6f)
   }
   
   private fun calculateCornerRadius(progress: Float): Dp {
      return lerp(InterpolationConstants.PANEL_CORNER_RADIUS_NORMAL.dp, 0.dp, easeOutCubic(progress))
   }
   
   private fun calculateVinylSize(progress: Float, screenHeight: Dp, normalHeight: Dp): Dp {
      val sizeNormal = (normalHeight.value * 0.6f).coerceIn(60f, 100f).dp
      val sizeExpanded = (screenHeight.value * 0.28f).coerceIn(200f, 320f).dp
      return lerp(sizeNormal, sizeExpanded, easeOutBack(progress, InterpolationConstants.EASING_OVERSHOOT_VINYL))
   }
   
   private fun calculateCenterProgress(progress: Float): Float {
      return if (progress <= InterpolationConstants.CENTER_PROGRESS_START) 0f
      else easeOutCubic((progress - InterpolationConstants.CENTER_PROGRESS_START) / (1f - InterpolationConstants.CENTER_PROGRESS_START))
   }
   
   private fun calculateVinylFloat(progress: Float, isDragging: Boolean): Dp {
      if (!isDragging || progress <= InterpolationConstants.VINYL_FLOAT_START) return 0.dp
      val t = (progress - InterpolationConstants.VINYL_FLOAT_START) / (1f - InterpolationConstants.VINYL_FLOAT_START)
      val floatAmount = sin(t * PI.toFloat()) * InterpolationConstants.VINYL_FLOAT_AMPLITUDE_DP
      return floatAmount.dp
   }
   
   private fun calculateGlowAlpha(progress: Float): Float {
      return if (progress <= THRESHOLD) 0f
      else lerp(0f, 0.35f, easeOutQuad((progress - THRESHOLD) / (1f - THRESHOLD)))
   }
   
   private fun calculateGlowScale(progress: Float): Float {
      return if (progress <= THRESHOLD) 1f
      else lerp(1f, InterpolationConstants.VINYL_GLOW_SCALE_MAX, easeOutQuad((progress - THRESHOLD) / (1f - THRESHOLD)))
   }
   
   
   private fun calculateInfoScale(progress: Float): Float {
      return lerp(1f, InterpolationConstants.INFO_SCALE_MAX, easeOutQuad(progress))
   }
   
   private fun calculateTabsAlpha(progress: Float): Float {
      return if (progress <= InterpolationConstants.TABS_APPEAR_THRESHOLD) 0f
      else easeOutQuad((progress - InterpolationConstants.TABS_APPEAR_THRESHOLD) / (1f - InterpolationConstants.TABS_APPEAR_THRESHOLD))
   }
   
   private fun calculateTabsOffset(progress: Float): Dp {
      return if (progress <= InterpolationConstants.TABS_APPEAR_THRESHOLD) InterpolationConstants.TABS_OFFSET_INITIAL.dp
      else lerp(
         InterpolationConstants.TABS_OFFSET_INITIAL.dp,
         0.dp,
         easeOutBack((progress - InterpolationConstants.TABS_APPEAR_THRESHOLD) / (1f - InterpolationConstants.TABS_APPEAR_THRESHOLD), InterpolationConstants.EASING_OVERSHOOT_TABS)
      )
   }
   
   // endregion
}

// Valores interpolados para renderizar el reproductor
@Immutable
data class PlayerInterpolatedValues(
   // Layouts
   val normalLayoutAlpha: Float = 1f,
   val expandedLayoutAlpha: Float = 0f,
   
   // Panel
   val panelHeightDp: Dp = 140.dp,
   val backgroundDimAlpha: Float = 0f,
   val cornerRadiusDp: Dp = 24.dp,
   
   // Vinyl
   val vinylSizeDp: Dp = 10.dp,
   val vinylCenterProgress: Float = 0f,
   val vinylFloatOffsetY: Dp = 0.dp,
   val vinylGlowAlpha: Float = 0f,
   val vinylGlowScale: Float = 1f,
   
   // Info Text
   val infoScale: Float = 1f,
   val infoCenterProgress: Float = 0f,
   
   // Controls
   val compactControlsAlpha: Float = 1f,
   val expandedControlsAlpha: Float = 0f,
   
   // Slider
   val compactSliderAlpha: Float = 1f,
   val fullSliderAlpha: Float = 0f,
   
   // Tabs
   val tabsAlpha: Float = 0f,
   val tabsOffsetY: Dp = 40.dp,
   
   // Progress helpers
   val transitionProgress: Float = 0f,
   val isInNormalZone: Boolean = true,
   val isInExpandedZone: Boolean = false,
) {
   // Indica si el layout NORMAL debe renderizarse
   val shouldShowNormalLayout: Boolean
      get() = normalLayoutAlpha > 0.01f
   
   // Indica si el layout EXPANDED debe renderizarse
   val shouldShowExpandedLayout: Boolean
      get() = expandedLayoutAlpha > 0.01f
   
   // Indica si los tabs deben renderizarse
   val shouldShowTabs: Boolean
      get() = tabsAlpha > 0.01f
   
   // Indica si está en medio de una transición
   val isTransitioning: Boolean
      get() = transitionProgress > 0.01f && transitionProgress < 0.99f
   
   // Indica si está completamente expandido
   val isFullyExpanded: Boolean
      get() = transitionProgress >= 0.99f
   
   // Indica si está completamente colapsado
   val isFullyCollapsed: Boolean
      get() = transitionProgress <= 0.01f
}

@Composable
fun rememberOptimizedInterpolatedValues(
   progress: Float,
   screenHeightDp: Dp,
   isDragging: Boolean = false,
): PlayerInterpolatedValues {
   // Valores que cambian frecuentemente durante drag
   val frequentValues by remember(progress, screenHeightDp) {
      derivedStateOf {
         val p = progress.coerceIn(0f, 1f)
         val normalHeightDp = Dp(screenHeightDp.value * PlayerGestureConstants.HEIGHT_FRACTION_NORMAL)
         
         FrequentInterpolatedValues(
            panelHeightDp = lerp(normalHeightDp, screenHeightDp, easeOutCubic(p)),
            backgroundDimAlpha = (p * 0.6f).coerceIn(0f, 0.6f),
            cornerRadiusDp = lerp(24.dp, 0.dp, easeOutCubic(p)),
         )
      }
   }
   
   // Valores que solo cambian en thresholds (50%, 70%)
   val thresholdValues by remember(progress >= 0.5f, progress >= 0.7f) {
      derivedStateOf {
         val p = progress.coerceIn(0f, 1f)
         ThresholdInterpolatedValues(
            normalLayoutAlpha = if (p < 0.5f) (1f - (p / 0.5f)).coerceIn(0f, 1f) else 0f,
            expandedLayoutAlpha = if (p >= 0.5f) ((p - 0.5f) / 0.5f).coerceIn(0f, 1f) else 0f,
            tabsAlpha = if (p <= 0.7f) 0f else easeOutQuad((p - 0.7f) / 0.3f),
         )
      }
   }
   
   // Valores de vinyl solo durante drag
   val vinylFloatOffsetY = remember(isDragging, progress) {
      if (!isDragging || progress <= 0.3f) 0.dp
      else {
         val t = (progress - 0.3f) / 0.7f
         (sin(t * PI.toFloat()) * 8f).dp
      }
   }
   
   // Combinar en el objeto final
   return remember(frequentValues, thresholdValues, vinylFloatOffsetY, progress, screenHeightDp, isDragging) {
      PlayerInterpolator.calculate(progress, screenHeightDp, isDragging)
   }
}

// Data classes auxiliares para derivedStateOf
@Immutable
private data class FrequentInterpolatedValues(
   val panelHeightDp: Dp,
   val backgroundDimAlpha: Float,
   val cornerRadiusDp: Dp,
)

@Immutable
private data class ThresholdInterpolatedValues(
   val normalLayoutAlpha: Float,
   val expandedLayoutAlpha: Float,
   val tabsAlpha: Float,
)

// region Easing Functions

fun lerp(start: Float, stop: Float, fraction: Float): Float {
   return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
   return (start.value + (stop.value - start.value) * fraction.coerceIn(0f, 1f)).dp
}

fun easeOutQuad(t: Float): Float {
   val x = t.coerceIn(0f, 1f)
   return 1f - (1f - x) * (1f - x)
}

fun easeOutCubic(t: Float): Float {
   val x = t.coerceIn(0f, 1f)
   return 1f - (1f - x) * (1f - x) * (1f - x)
}

fun easeOutBack(t: Float, overshoot: Float = 1.70158f): Float {
   val x = t.coerceIn(0f, 1f)
   val c3 = overshoot + 1f
   return 1f + c3 * (x - 1f).let { it * it * it } + overshoot * (x - 1f).let { it * it }
}

// endregion

// Crea y recuerda valores interpolados
@Composable
fun rememberInterpolatedValues(
   progress: Float,
   screenHeightDp: Dp,
   isDragging: Boolean = false,
): PlayerInterpolatedValues {
   return remember(progress, screenHeightDp, isDragging) {
      PlayerInterpolator.calculate(progress, screenHeightDp, isDragging)
   }
}