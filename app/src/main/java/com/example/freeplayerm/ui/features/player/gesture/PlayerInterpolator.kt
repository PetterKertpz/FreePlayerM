package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

object PlayerInterpolator {

    /**
     * Calcula todos los valores interpolados para el estado actual.
     *
     * @param progresoGlobal Progreso del gesto (0f a 1f)
     * @param screenHeightDp Altura de la pantalla
     * @param isDragging Si el usuario está arrastrando
     * @param config Configuración de gestos (importada desde PlayerGestureConfig.kt)
     */
    fun calcular(
        progresoGlobal: Float,
        screenHeightDp: Dp,
        isDragging: Boolean = false,
        config: PlayerGestureConfig = PlayerGestureConfig.Default,
    ): InterpolatedValues {
        val progreso = progresoGlobal.coerceIn(0f, 1f)

        return InterpolatedValues(
            // === PANEL ===
            panelHeightDp = calcularAlturaPanelDp(progreso, screenHeightDp, config, isDragging),
            panelAlpha = 1f,

            // === VINILO ===
            viniloSizeDp = calcularViniloSize(progreso, config, screenHeightDp),
            viniloOffsetX = calcularViniloOffsetX(progreso, config),
            viniloOffsetY = calcularViniloOffsetY(progreso, config, isDragging),
            viniloCenterProgress = calcularViniloCenterProgress(progreso, config),

            // === GLOW ===
            glowAlpha = calcularGlowAlpha(progreso, config),
            glowScale = calcularGlowScale(progreso, config),

            // === CONTROLES ===
            controlesNormalesAlpha = calcularControlesNormalesAlpha(progreso, config),
            controlesExpandidosAlpha = calcularControlesExpandidosAlpha(progreso, config),

            // === INFO ===
            infoScale = calcularInfoScale(progreso, config),
            infoCenterProgress = calcularInfoCenterProgress(progreso, config),

            // === SLIDER ===
            sliderCompactoAlpha = calcularSliderCompactoAlpha(progreso, config),
            sliderCompletoAlpha = calcularSliderCompletoAlpha(progreso, config),

            // === TABS ===
            tabsAlpha = calcularTabsAlpha(progreso, config),
            tabsOffsetY = calcularTabsOffsetY(progreso, config),

            // === FONDO ===
            fondoIntensidad = calcularFondoIntensidad(progreso, config),

            // === MINI PLAYER ===
            miniIndicadorAlpha = calcularMiniIndicadorAlpha(progreso, config),
            miniMarqueeEnabled = progreso < (config.alturaNormal * 0.5f),
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PANEL - ALTURA FIJA POR MODO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calcula la altura del panel basándose en el progreso.
     *
     * COMPORTAMIENTO:
     * - Si NO está arrastrando: Alturas FIJAS (80dp, 224dp, 800dp)
     * - Si está arrastrando: Interpolación SUAVE entre alturas
     *
     * Mapeo:
     * - progreso < 0.15f → Minimizado (10% = 80dp)
     * - progreso 0.15f - 0.6f → Normal (28% = 224dp)
     * - progreso > 0.6f → Expandido (100% = 800dp)
     */
    private fun calcularAlturaPanelDp(
        progreso: Float,
        screenHeightDp: Dp,
        config: PlayerGestureConfig,
        isDragging: Boolean,
    ): Dp {
        val alturaMini = screenHeightDp * config.alturaMinimizado // 80dp
        val alturaNormal = screenHeightDp * config.alturaNormal // 224dp
        val alturaMax = screenHeightDp // 800dp

        // ⚡ ALTURAS DISCRETAS cuando NO está arrastrando
        if (!isDragging) {
            return when {
                progreso < 0.15f -> alturaMini
                progreso < 0.6f -> alturaNormal
                else -> alturaMax
            }
        }

        // ⚡ INTERPOLACIÓN SUAVE cuando SÍ está arrastrando
        return when {
            progreso <= 0f -> alturaMini
            progreso >= 1f -> alturaMax
            progreso <= config.alturaNormal -> {
                // MINI → NORMAL: Interpolación suave
                val t = progreso / config.alturaNormal
                lerp(alturaMini, alturaNormal, easeOutCubic(t))
            }
            else -> {
                // NORMAL → EXPANDIDO: Interpolación suave
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(alturaNormal, alturaMax, easeOutCubic(t))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VINILO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calcula el tamaño del vinilo basándose en el progreso.
     *
     * Tamaños:
     * - Minimizado: 60% de altura normal (~50dp)
     * - Normal: 35% de altura del panel normal (~78dp)
     * - Expandido: 75% del ancho estimado (~300dp max)
     */
    private fun calcularViniloSize(progreso: Float, config: PlayerGestureConfig, screenHeightDp: Dp): Dp {
        val alturaNormal = screenHeightDp * config.alturaNormal

        // Tamaños calculados dinámicamente
        val sizeMini = (alturaNormal * 0.6f).coerceAtLeast(48.dp)
        val sizeNormal = (alturaNormal * 0.35f).coerceIn(76.dp, 110.dp)
        val sizeExpandido = (screenHeightDp * 0.5625f * 0.75f).coerceAtMost(300.dp)

        return when {
            progreso <= 0f -> sizeMini
            progreso >= 1f -> sizeExpandido
            progreso <= config.alturaNormal -> {
                val t = progreso / config.alturaNormal
                lerp(sizeMini, sizeNormal, easeOutBack(t, 0.8f))
            }
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(sizeNormal, sizeExpandido, easeOutBack(t, 0.5f))
            }
        }
    }

    private fun calcularViniloOffsetX(progreso: Float, config: PlayerGestureConfig): Dp {
        return when {
            progreso <= config.alturaNormal -> 0.dp
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(0.dp, 100.dp, easeOutQuad(t) * 0.7f)
            }
        }
    }

    private fun calcularViniloOffsetY(
        progreso: Float,
        config: PlayerGestureConfig,
        isDragging: Boolean,
    ): Dp {
        return when {
            !isDragging -> 0.dp
            progreso <= config.alturaNormal -> 0.dp
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                val floatAmount = sin(t * PI.toFloat()) * 16f
                floatAmount.dp
            }
        }
    }

    private fun calcularViniloCenterProgress(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 0f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                easeOutCubic(t)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GLOW
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularGlowAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 0f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(0f, 0.35f, easeOutQuad(t))
            }
        }
    }

    private fun calcularGlowScale(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 1f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(1f, 1.08f, easeOutQuad(t))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONTROLES
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularControlesNormalesAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val rangoTransicion = 1f - config.alturaNormal
        val fadeOutStart = config.alturaNormal
        val fadeOutEnd = config.alturaNormal + (rangoTransicion * 0.30f)

        return when {
            progreso <= fadeOutStart -> 1f
            progreso >= fadeOutEnd -> 0f
            else -> {
                val t = (progreso - fadeOutStart) / (fadeOutEnd - fadeOutStart)
                1f - easeOutQuad(t)
            }
        }
    }

    private fun calcularControlesExpandidosAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val rangoTransicion = 1f - config.alturaNormal
        val fadeInStart = config.alturaNormal + (rangoTransicion * 0.60f)

        return when {
            progreso <= fadeInStart -> 0f
            progreso >= 1f -> 1f
            else -> {
                val t = (progreso - fadeInStart) / (1f - fadeInStart)
                easeOutQuad(t)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INFO
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularInfoScale(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 1f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                lerp(1f, 1.12f, easeOutQuad(t))
            }
        }
    }

    private fun calcularInfoCenterProgress(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 0f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                easeOutCubic(t)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SLIDER
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularSliderCompactoAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val rangoTransicion = 1f - config.alturaNormal
        val fadeOutStart = config.alturaNormal
        val fadeOutEnd = config.alturaNormal + (rangoTransicion * 0.35f)

        return when {
            progreso <= fadeOutStart -> 1f
            progreso >= fadeOutEnd -> 0f
            else -> {
                val t = (progreso - fadeOutStart) / (fadeOutEnd - fadeOutStart)
                1f - easeOutQuad(t)
            }
        }
    }

    private fun calcularSliderCompletoAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val rangoTransicion = 1f - config.alturaNormal
        val fadeInStart = config.alturaNormal + (rangoTransicion * 0.50f)

        return when {
            progreso <= fadeInStart -> 0f
            else -> {
                val t = (progreso - fadeInStart) / (1f - fadeInStart)
                easeOutQuad(t)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TABS
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularTabsAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val fadeInStart = config.alturaNormal + 0.75f * (1f - config.alturaNormal)

        return when {
            progreso <= fadeInStart -> 0f
            else -> {
                val t = (progreso - fadeInStart) / (1f - fadeInStart)
                easeOutQuad(t)
            }
        }
    }

    private fun calcularTabsOffsetY(progreso: Float, config: PlayerGestureConfig): Dp {
        val animStart = config.alturaNormal + 0.75f * (1f - config.alturaNormal)

        return when {
            progreso <= animStart -> 40.dp
            else -> {
                val t = (progreso - animStart) / (1f - animStart)
                lerp(40.dp, 0.dp, easeOutBack(t, 0.8f))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FONDO
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularFondoIntensidad(progreso: Float, config: PlayerGestureConfig): Float {
        return when {
            progreso <= config.alturaNormal -> 0f
            else -> {
                val t = (progreso - config.alturaNormal) / (1f - config.alturaNormal)
                easeOutCubic(t)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MINI PLAYER
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularMiniIndicadorAlpha(progreso: Float, config: PlayerGestureConfig): Float {
        val fadeOutEnd = config.alturaNormal * 0.5f

        return when {
            progreso >= fadeOutEnd -> 0f
            else -> {
                val t = progreso / fadeOutEnd
                1f - easeOutQuad(t)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// INTERPOLATED VALUES
// ═══════════════════════════════════════════════════════════════════════════

@Immutable
data class InterpolatedValues(
    val panelHeightDp: Dp = 140.dp,
    val panelAlpha: Float = 1f,
    val viniloSizeDp: Dp = 108.dp,
    val viniloOffsetX: Dp = 0.dp,
    val viniloOffsetY: Dp = 0.dp,
    val viniloCenterProgress: Float = 0f,
    val glowAlpha: Float = 0f,
    val glowScale: Float = 1f,
    val controlesNormalesAlpha: Float = 1f,
    val controlesExpandidosAlpha: Float = 0f,
    val infoScale: Float = 1f,
    val infoCenterProgress: Float = 0f,
    val sliderCompactoAlpha: Float = 1f,
    val sliderCompletoAlpha: Float = 0f,
    val tabsAlpha: Float = 0f,
    val tabsOffsetY: Dp = 40.dp,
    val fondoIntensidad: Float = 0f,
    val miniIndicadorAlpha: Float = 0f,
    val miniMarqueeEnabled: Boolean = false,
)

// ═══════════════════════════════════════════════════════════════════════════
// EASING FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════════

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
