package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.example.freeplayerm.ui.features.player.ModoPanelReproductor
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.launch


// ───────────────────────────────────────────────────────────────────────────
// ESTADO DEL GESTO
// ───────────────────────────────────────────────────────────────────────────

/** Estado observable del gesto actual. Inmutable para garantizar consistencia. */
@Stable
data class GestureState(
    /** Si el usuario está arrastrando activamente */
    val isDragging: Boolean = false,
    /** Si hay una animación de snap en progreso */
    val isAnimating: Boolean = false,
    /** Modo actual del panel */
    val currentMode: ModoPanelReproductor = ModoPanelReproductor.NORMAL,
    /** Modo objetivo durante el gesto (puede ser null si no hay destino claro) */
    val targetMode: ModoPanelReproductor? = null,
    /** Velocidad actual del gesto en px/ms */
    val velocity: Float = 0f,
    /** Dirección del gesto: -1 = arriba (expandir), 1 = abajo (minimizar), 0 = estático */
    val direction: Int = 0,
    /** Si se ha dado haptic feedback en el umbral actual */
    val hapticGivenForThreshold: HapticThreshold? = null,
)

/** Umbrales donde se da haptic feedback */
enum class HapticThreshold {
    MINIMIZED_TO_NORMAL,
    NORMAL_TO_EXPANDED,
    EXPANDED_TO_NORMAL,
    NORMAL_TO_MINIMIZED,
}

// ───────────────────────────────────────────────────────────────────────────
// CONTROLADOR PRINCIPAL
// ───────────────────────────────────────────────────────────────────────────

/**
 * Controlador unificado para los gestos del reproductor.
 *
 * Encapsula toda la lógica de:
 * - Tracking del progreso animable
 * - Cálculo de snap points
 * - Rubber banding
 * - Haptic feedback
 * - Animaciones de transición
 *
 * @param screenHeightPx Altura de la pantalla en píxeles
 * @param initialMode Modo inicial del panel
 * @param config Configuración del sistema de gestos
 */
@Stable
class PlayerGestureController(
    private val screenHeightPx: Float,
    initialMode: ModoPanelReproductor,
    private val config: PlayerGestureConfig = PlayerGestureConfig.Default,
) {
    // ⚡ Altura dinámica del modo NORMAL basada en screenHeight
    private val alturaMinimizadoPx: Float = screenHeightPx * config.alturaMinimizado
    private val alturaNormalPx: Float = screenHeightPx * config.alturaNormal

    // === Progreso animable [0, 1] ===
    val progress = Animatable(calcularProgresoParaModo(initialMode))

    // === Estado observable ===
    var state by mutableStateOf(GestureState(currentMode = initialMode))
        private set

    // === Punto de anclaje del modo NORMAL ===
    val puntoNormal: Float = calcularPuntoNormal()

    // === Caché de cálculos ===
    private var lastCalculatedProgress: Float = -1f
    private var cachedInterpolatedValues: InterpolatedValues? = null

    // ═══════════════════════════════════════════════════════════════════════
    // CÁLCULOS DE PUNTOS DE REFERENCIA
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calcula el punto de anclaje del modo NORMAL en el progreso [0, 1]. Usa la altura dinámica
     * basada en la fracción de pantalla configurada.
     */
    private fun calcularPuntoNormal(): Float {
        val alturaMini = alturaMinimizadoPx  // ⚡ Usa la altura dinámica
        val alturaNormal = alturaNormalPx     // ⚡ Usa la altura dinámica
        val alturaMax = screenHeightPx

        if (alturaMax <= alturaMini) return config.alturaNormal

        return ((alturaNormal - alturaMini) / (alturaMax - alturaMini))
            .coerceIn(0.1f, config.alturaNormal + 0.05f)
    }

    private fun calcularProgresoParaModo(modo: ModoPanelReproductor): Float {
        return when (modo) {
            ModoPanelReproductor.MINIMIZADO -> 0f
            ModoPanelReproductor.NORMAL -> puntoNormal
            ModoPanelReproductor.EXPANDIDO -> 1f
        }
    }

    fun getModoParaProgreso(progreso: Float): ModoPanelReproductor {
        val progresoNormalizado = progreso.coerceIn(0f, 1f)

        // Solo considerar NORMAL y EXPANDIDO como opciones
        val distanciaNormal = abs(progresoNormalizado - puntoNormal)
        val distanciaExpandido = abs(progresoNormalizado - 1f)

        return if (distanciaNormal <= distanciaExpandido) {
            ModoPanelReproductor.NORMAL
        } else {
            ModoPanelReproductor.EXPANDIDO
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MANEJO DE GESTOS
    // ═══════════════════════════════════════════════════════════════════════

    /** Llamar cuando inicia el arrastre. */
    suspend fun onDragStart() {
        progress.stop()
        state =
            state.copy(
                isDragging = true,
                isAnimating = false,
                hapticGivenForThreshold = null,
                velocity = 0f,
            )
    }

    /**
     * Llamar durante el arrastre con el delta en píxeles.
     *
     * @param deltaY Cambio en Y (negativo = hacia arriba = expandir)
     * @param velocityY Velocidad actual en px/ms
     * @param hapticFeedback Referencia para haptic feedback
     * @return El nuevo progreso (ya con rubber banding aplicado)
     */
    suspend fun onDrag(deltaY: Float, velocityY: Float, hapticFeedback: HapticFeedback?): Float {
        val alturaMiniPx = config.alturaMinimizado
        val rangoTotal = screenHeightPx - alturaMiniPx

        // Calcular cambio de progreso (negativo deltaY = aumentar progreso)
        val cambioProgreso = -deltaY / rangoTotal

        // Aplicar el cambio con rubber banding
        val progresoActual = progress.value
        var nuevoProgreso = progresoActual + cambioProgreso

        // Rubber banding logarítmico en los límites
        nuevoProgreso = aplicarRubberBanding(nuevoProgreso)

        // Actualizar progreso
        progress.snapTo(nuevoProgreso)

        // Actualizar estado
        val direction =
            when {
                velocityY < -config.velocidadMinimaFling -> -1 // Hacia arriba (expandir)
                velocityY > config.velocidadMinimaFling -> 1 // Hacia abajo (minimizar)
                else -> 0
            }

        state =
            state.copy(
                velocity = velocityY / 1000f, // Convertir a px/ms
                direction = direction,
                targetMode = calcularModoObjetivo(nuevoProgreso, velocityY / 1000f),
            )

        // Haptic feedback en umbrales
        if (config.hapticEnUmbrales && hapticFeedback != null) {
            checkAndPerformHaptic(progresoActual, nuevoProgreso, hapticFeedback)
        }

        return nuevoProgreso
    }

    /**
     * Llamar cuando termina el arrastre.
     *
     * @param velocityY Velocidad final en px/ms
     * @param hapticFeedback Referencia para haptic feedback
     * @param onModeChanged Callback cuando cambia el modo
     */
    suspend fun onDragEnd(
        velocityY: Float,
        hapticFeedback: HapticFeedback?,
        onModeChanged: (ModoPanelReproductor) -> Unit,
    ) {
        val velocidadPxMs = velocityY / 1000f
        val progresoActual = progress.value

        // Calcular modo final con lógica híbrida
        val modoFinal = calcularModoFinal(progresoActual, velocidadPxMs)
        val progresoFinal = calcularProgresoParaModo(modoFinal)

        // Determinar animación según contexto
        val animSpec = calcularAnimSpec(velocidadPxMs, progresoActual, progresoFinal)

        state = state.copy(isDragging = false, isAnimating = true, targetMode = modoFinal)

        // Haptic feedback en snap
        if (config.hapticEnSnap && modoFinal != state.currentMode && hapticFeedback != null) {
            val hapticType =
                when (modoFinal) {
                    ModoPanelReproductor.EXPANDIDO -> HapticFeedbackType.LongPress
                    else -> HapticFeedbackType.TextHandleMove
                }
            hapticFeedback.performHapticFeedback(hapticType)
        }

        // Animar hacia el modo final
        progress.animateTo(targetValue = progresoFinal, animationSpec = animSpec)

        // Actualizar estado final
        state =
            state.copy(
                isAnimating = false,
                currentMode = modoFinal,
                targetMode = null,
                velocity = 0f,
                direction = 0,
                hapticGivenForThreshold = null,
            )

        // Notificar cambio
        if (modoFinal != state.currentMode) {
            onModeChanged(modoFinal)
        }
    }

    /** Llamar cuando se cancela el gesto. */
    suspend fun onDragCancel() {
        val modoActual = state.currentMode
        val progresoModo = calcularProgresoParaModo(modoActual)

        state = state.copy(isDragging = false, isAnimating = true)

        progress.animateTo(
            targetValue = progresoModo,
            animationSpec =
                spring(
                    dampingRatio = config.dampingRatioNormal,
                    stiffness = config.springStiffnessNormal,
                ),
        )

        state = state.copy(isAnimating = false)
    }

    /** Animar programáticamente a un modo específico. */
    suspend fun animateToMode(mode: ModoPanelReproductor, hapticFeedback: HapticFeedback? = null) {
        if (state.isDragging) return

        val progresoObjetivo = calcularProgresoParaModo(mode)

        if (config.hapticEnSnap && hapticFeedback != null) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        state = state.copy(isAnimating = true, targetMode = mode)

        progress.animateTo(
            targetValue = progresoObjetivo,
            animationSpec =
                spring(
                    dampingRatio = config.dampingRatioNormal,
                    stiffness = config.springStiffnessNormal,
                ),
        )

        state = state.copy(isAnimating = false, currentMode = mode, targetMode = null)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RUBBER BANDING (ESTILO iOS)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Aplica rubber banding logarítmico para una sensación más natural.
     *
     * La fórmula es: offset * (1 - ln(1 + |offset| * factor) / maxOffset) Esto crea una resistencia
     * que aumenta exponencialmente.
     */
    private fun aplicarRubberBanding(progreso: Float): Float {
        return when {
            progreso < 0f -> {
                // Over-scroll hacia abajo (debajo de minimizado)
                val overscroll = -progreso
                val dampedOverscroll = calcularRubberBandOffset(overscroll)
                -dampedOverscroll.coerceAtMost(config.maxOverscroll)
            }
            progreso > 1f -> {
                // Over-scroll hacia arriba (encima de expandido)
                val overscroll = progreso - 1f
                val dampedOverscroll = calcularRubberBandOffset(overscroll)
                1f + dampedOverscroll.coerceAtMost(config.maxOverscroll)
            }
            else -> progreso
        }
    }

    /** Calcula el offset con resistencia logarítmica. */
    private fun calcularRubberBandOffset(offset: Float): Float {
        if (offset <= 0f) return 0f

        // Fórmula logarítmica para resistencia natural
        val c = config.resistenciaBase
        val maxOffset = config.maxOverscroll

        // ln(1 + x) crece más lento que x, creando resistencia
        return (1f - (1f / (offset * (1f / c) + 1f))) * maxOffset
    }

    /**
     * ⚡ Calcula el modo objetivo durante el arrastre activo. Solo considera NORMAL y EXPANDIDO como
     * destinos válidos.
     */
    private fun calcularModoObjetivo(progreso: Float, velocidadPxMs: Float): ModoPanelReproductor {
        val absVelocidad = abs(velocidadPxMs)

        // Si estamos en MINIMIZADO, el objetivo es siempre NORMAL
        if (state.currentMode == ModoPanelReproductor.MINIMIZADO) {
            return ModoPanelReproductor.NORMAL
        }

        // Si hay velocidad significativa, usar dirección
        if (absVelocidad > config.velocidadMinimaFling) {
            val direccion = sign(velocidadPxMs)
            return if (direccion < 0) {
                // Hacia arriba → siempre EXPANDIDO
                ModoPanelReproductor.EXPANDIDO
            } else {
                // Hacia abajo → siempre NORMAL
                ModoPanelReproductor.NORMAL
            }
        }

        // Sin velocidad, usar posición (excluye MINIMIZADO)
        return getModoParaProgreso(progreso)
    }

    /**
     * ⚡ NAVEGACIÓN MANUAL: Solo permite NORMAL y EXPANDIDO
     *
     * El modo MINIMIZADO es exclusivamente automático (activado por scroll). Los gestos del usuario
     * solo navegan entre:
     * - NORMAL (estado base)
     * - EXPANDIDO (pantalla completa)
     *
     * Lógica de velocidad:
     * - Velocidad ALTA hacia arriba → EXPANDIDO
     * - Velocidad ALTA hacia abajo → NORMAL
     * - Velocidad BAJA → Snap al modo más cercano (NORMAL o EXPANDIDO)
     */
    private fun calcularModoFinal(progreso: Float, velocidadPxMs: Float): ModoPanelReproductor {
        val absVelocidad = abs(velocidadPxMs)
        val modoActual = state.currentMode

        // ✅ Si estamos en MINIMIZADO (por scroll), cualquier gesto manual nos lleva a NORMAL
        if (modoActual == ModoPanelReproductor.MINIMIZADO) {
            return ModoPanelReproductor.NORMAL
        }

        // === VELOCIDAD ALTA: Intención clara del usuario ===
        if (absVelocidad > config.velocidadSnapNormal) {
            return when {
                velocidadPxMs < 0 -> ModoPanelReproductor.EXPANDIDO // Swipe up → Expandir
                else -> ModoPanelReproductor.NORMAL // Swipe down → Normal
            }
        }

        // === VELOCIDAD BAJA: Snap al modo más cercano ===
        // Solo consideramos NORMAL y EXPANDIDO como destinos válidos
        val progresoNormalizado = progreso.coerceIn(0f, 1f)

        // Calcular distancias (ignorando MINIMIZADO como opción)
        val distanciaNormal = abs(progresoNormalizado - puntoNormal)
        val distanciaExpandido = abs(progresoNormalizado - 1f)

        // Verificar umbral de transición desde el modo actual
        if (absVelocidad > config.velocidadMinimaFling) {
            val direccion = sign(velocidadPxMs)

            val progresoDesdeActual =
                when (modoActual) {
                    ModoPanelReproductor.NORMAL -> {
                        if (direccion < 0) {
                            // Hacia arriba desde NORMAL
                            (progresoNormalizado - puntoNormal) / (1f - puntoNormal)
                        } else {
                            // Hacia abajo desde NORMAL → volver a NORMAL (no minimizar)
                            0f
                        }
                    }
                    ModoPanelReproductor.EXPANDIDO -> {
                        if (direccion > 0) {
                            // Hacia abajo desde EXPANDIDO
                            (1f - progresoNormalizado) / (1f - puntoNormal)
                        } else {
                            // Hacia arriba desde EXPANDIDO → mantener EXPANDIDO
                            0f
                        }
                    }
                    ModoPanelReproductor.MINIMIZADO -> 1f // Ya manejado arriba
                }

            // Si supera el umbral, completar la transición
            if (progresoDesdeActual > config.umbralTransicionPorcentaje) {
                return when {
                    direccion < 0 && modoActual == ModoPanelReproductor.NORMAL ->
                        ModoPanelReproductor.EXPANDIDO
                    direccion > 0 && modoActual == ModoPanelReproductor.EXPANDIDO ->
                        ModoPanelReproductor.NORMAL
                    else -> modoActual
                }
            }
        }

        // Por defecto: ir al más cercano entre NORMAL y EXPANDIDO
        return if (distanciaNormal <= distanciaExpandido) {
            ModoPanelReproductor.NORMAL
        } else {
            ModoPanelReproductor.EXPANDIDO
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ANIMACIONES ADAPTATIVAS
    // ═══════════════════════════════════════════════════════════════════════

    private fun calcularAnimSpec(
        velocidadPxMs: Float,
        progresoInicial: Float,
        progresoFinal: Float,
    ): AnimationSpec<Float> {
        val absVelocidad = abs(velocidadPxMs)
        val distancia = abs(progresoFinal - progresoInicial)

        return when {
            // Swipe muy rápido: animación corta y snappy
            absVelocidad > config.velocidadSnapRapido ->
                spring(
                    dampingRatio = config.dampingRatioRapido,
                    stiffness = config.springStiffnessRapido,
                )
            // Swipe normal con buena velocidad
            absVelocidad > config.velocidadSnapNormal ->
                spring(
                    dampingRatio = config.dampingRatioNormal,
                    stiffness = config.springStiffnessNormal,
                )
            // Distancia larga sin velocidad: más suave
            distancia > 0.5f ->
                spring(
                    dampingRatio = config.dampingRatioLento,
                    stiffness = config.springStiffnessLento,
                )
            // Por defecto: normal
            else ->
                spring(
                    dampingRatio = config.dampingRatioNormal,
                    stiffness = config.springStiffnessNormal,
                )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HAPTIC FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════

    private fun checkAndPerformHaptic(
        progresoAnterior: Float,
        progresoNuevo: Float,
        hapticFeedback: HapticFeedback,
    ) {
        // Detectar cruce del umbral de puntoNormal
        val cruzaNormalSubiendo = progresoAnterior < puntoNormal && progresoNuevo >= puntoNormal
        val cruzaNormalBajando = progresoAnterior > puntoNormal && progresoNuevo <= puntoNormal

        // Detectar cruce del umbral hacia expandido (0.7 como ejemplo)
        val umbralExpandido = puntoNormal + (1f - puntoNormal) * 0.7f
        val cruzaExpandidoSubiendo =
            progresoAnterior < umbralExpandido && progresoNuevo >= umbralExpandido
        val cruzaExpandidoBajando =
            progresoAnterior > umbralExpandido && progresoNuevo <= umbralExpandido

        val threshold =
            when {
                cruzaNormalSubiendo -> HapticThreshold.MINIMIZED_TO_NORMAL
                cruzaNormalBajando -> HapticThreshold.NORMAL_TO_MINIMIZED
                cruzaExpandidoSubiendo -> HapticThreshold.NORMAL_TO_EXPANDED
                cruzaExpandidoBajando -> HapticThreshold.EXPANDED_TO_NORMAL
                else -> null
            }

        // Solo dar haptic si es un umbral nuevo
        if (threshold != null && threshold != state.hapticGivenForThreshold) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            state = state.copy(hapticGivenForThreshold = threshold)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Obtiene los valores interpolados con caché. Solo recalcula si el progreso ha cambiado
     * significativamente.
     */
    fun getInterpolatedValues(screenHeightDp: Dp): InterpolatedValues {
        val currentProgress = progress.value

        // Umbral de cambio para recalcular (evitar cálculos en cada frame)
        val threshold = 0.001f
        if (
            cachedInterpolatedValues != null &&
                abs(currentProgress - lastCalculatedProgress) < threshold
        ) {
            return cachedInterpolatedValues!!
        }

        lastCalculatedProgress = currentProgress
        cachedInterpolatedValues =
            PlayerInterpolator.calcular(
                progresoGlobal = currentProgress,
                screenHeightDp = screenHeightDp,
                isDragging = state.isDragging,
            )

        return cachedInterpolatedValues!!
    }

    /**
     * Invalida la caché de interpolación. Llamar cuando cambian parámetros externos (ej:
     * screenHeight).
     */
    fun invalidateCache() {
        cachedInterpolatedValues = null
        lastCalculatedProgress = -1f
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SINCRONIZACIÓN EXTERNA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sincroniza el controlador con un modo externo sin animación. Útil cuando el estado cambia
     * desde el ViewModel.
     */
    suspend fun syncToMode(mode: ModoPanelReproductor) {
        if (state.isDragging || state.isAnimating) return

        val progresoObjetivo = calcularProgresoParaModo(mode)
        progress.snapTo(progresoObjetivo)

        state = state.copy(currentMode = mode, targetMode = null, velocity = 0f, direction = 0)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// REMEMBER HELPER
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Crea y recuerda un PlayerGestureController.
 *
 * @param screenHeightPx Altura de la pantalla en píxeles
 * @param initialMode Modo inicial del panel
 * @param config Configuración opcional del sistema de gestos
 */
@Composable
fun rememberPlayerGestureController(
    screenHeightPx: Float,
    initialMode: ModoPanelReproductor,
    config: PlayerGestureConfig = PlayerGestureConfig.Default,
): PlayerGestureController {
    // Crear controlador solo cuando cambian parámetros estructurales
    val controller =
        remember(screenHeightPx, config) {
            PlayerGestureController(
                screenHeightPx = screenHeightPx,
                initialMode = initialMode,
                config = config,
            )
        }

    // Sincronizar modo cuando cambia externamente (sin recrear el controlador)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(initialMode) {
        // ✅ MEJORADO: Usar syncToMode para cambios rápidos sin animación
        if (
            !controller.state.isDragging &&
                !controller.state.isAnimating &&
                controller.state.currentMode != initialMode
        ) {

            // Si es el primer cambio o la diferencia es grande, animar
            val debeAnimar =
                controller.state.currentMode == ModoPanelReproductor.NORMAL ||
                    initialMode == ModoPanelReproductor.EXPANDIDO

            if (debeAnimar) {
                controller.animateToMode(initialMode, haptic)
            } else {
                controller.syncToMode(initialMode)
            }
        }
    }

    return controller
}

// ═══════════════════════════════════════════════════════════════════════════
// MODIFIER EXTENSION
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Modifier que añade gestos de arrastre vertical al reproductor.
 *
 * Encapsula toda la lógica de detección de gestos:
 * - VelocityTracker para tracking de velocidad
 * - Integración con PlayerGestureController
 * - Haptic feedback automático
 *
 * @param controller El controlador de gestos del reproductor
 * @param enabled Si los gestos están habilitados
 * @param onModeChanged Callback cuando cambia el modo del panel
 */
fun Modifier.playerGestures(
    controller: PlayerGestureController,
    enabled: Boolean = true,
    onModeChanged: (ModoPanelReproductor) -> Unit,
): Modifier = composed {
    if (!enabled) return@composed this

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    this.pointerInput(controller) {
        val velocityTracker = VelocityTracker()

        detectVerticalDragGestures(
            onDragStart = { _ ->
                velocityTracker.resetTracking()
                scope.launch { controller.onDragStart() }
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity()
                scope.launch {
                    controller.onDragEnd(
                        velocityY = velocity.y,
                        hapticFeedback = haptic,
                        onModeChanged = onModeChanged,
                    )
                }
            },
            onDragCancel = { scope.launch { controller.onDragCancel() } },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                val velocity = velocityTracker.calculateVelocity()

                scope.launch {
                    controller.onDrag(
                        deltaY = dragAmount,
                        velocityY = velocity.y,
                        hapticFeedback = haptic,
                    )
                }
            },
        )
    }
}
