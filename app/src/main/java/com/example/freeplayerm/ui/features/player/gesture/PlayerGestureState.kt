package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.freeplayerm.ui.features.player.ModoPanelReproductor
import kotlin.math.abs

/**
 * ⚡ SISTEMA DE GESTOS DEL REPRODUCTOR - v4.0
 *
 * Define el estado del gesto y las constantes para las transiciones entre los 3 modos del panel:
 * MINIMIZADO, NORMAL, EXPANDIDO
 *
 * Características: ✅ Arrastre continuo con morphing de elementos ✅ Snap inteligente basado en
 * velocidad ✅ Interpolación fluida de todos los elementos ✅ Soporte para gestos desde cualquier
 * modo
 */

// ==================== CONSTANTES DEL SISTEMA ====================

object GestureConstants {
    // Alturas de cada modo en dp
    val ALTURA_MINIMIZADO = 72.dp
    val ALTURA_NORMAL = 140.dp
    // EXPANDIDO usa el 100% de la pantalla (se calcula dinámicamente)

    // Umbrales de velocidad (px/ms)
    const val VELOCIDAD_SNAP_RAPIDO = 1.5f // Swipe rápido → salta 2 modos
    const val VELOCIDAD_SNAP_NORMAL = 0.5f // Swipe normal → salta 1 modo

    // Umbrales de distancia para activar transición
    const val UMBRAL_ACTIVACION_PX = 50f // Mínimo para considerar un gesto
    const val UMBRAL_TRANSICION_PORCENTAJE = 0.35f // 35% del recorrido para completar

    // Resistencia en los límites (rubber banding)
    const val RESISTENCIA_LIMITE = 0.3f // Factor de resistencia al pasar límites

    // Duración de animaciones de snap (ms)
    const val DURACION_SNAP_MS = 350
    const val DURACION_SNAP_RAPIDO_MS = 250

    // Vinilo
    val VINILO_SIZE_MINI = 0.dp // No visible en mini
    val VINILO_SIZE_NORMAL = 5.dp
    val VINILO_SIZE_EXPANDIDO = 280.dp

    // Glow del vinilo
    const val GLOW_ALPHA_MINI = 0f
    const val GLOW_ALPHA_NORMAL = 0f
    const val GLOW_ALPHA_EXPANDIDO = 0.35f
}

// ==================== ESTADO DEL GESTO ====================

/** Estado inmutable que representa el gesto actual del usuario */
@Immutable
data class PlayerGestureState(
    // Modo desde el que inició el gesto
    val modoOrigen: ModoPanelReproductor = ModoPanelReproductor.NORMAL,

    // Modo objetivo actual (puede cambiar durante el gesto)
    val modoDestino: ModoPanelReproductor? = null,

    // Progreso de la transición [0.0, 1.0]
    // 0.0 = en modoOrigen, 1.0 = en modoDestino
    val progreso: Float = 0f,

    // Offset acumulado del gesto en píxeles
    val offsetAcumulado: Float = 0f,

    // Velocidad actual del gesto (px/ms)
    val velocidad: Float = 0f,

    // Si el gesto está activo
    val isDragging: Boolean = false,

    // Si se está animando hacia un snap point
    val isAnimating: Boolean = false,

    // Altura total de la pantalla (necesario para cálculos)
    val screenHeightPx: Float = 0f,
) {
    /**
     * Progreso global normalizado entre MINIMIZADO y EXPANDIDO [0.0, 1.0] 0.0 = MINIMIZADO ~0.3 =
     * NORMAL (depende de las alturas) 1.0 = EXPANDIDO
     */
    val progresoGlobal: Float
        get() {
            if (screenHeightPx <= 0)
                return when (modoOrigen) {
                    ModoPanelReproductor.MINIMIZADO -> 0f
                    ModoPanelReproductor.NORMAL -> 0.3f
                    ModoPanelReproductor.EXPANDIDO -> 1f
                }

            val alturaActual = calcularAlturaActual()
            val alturaMini = GestureConstants.ALTURA_MINIMIZADO.value
            val alturaMax = screenHeightPx

            return ((alturaActual - alturaMini) / (alturaMax - alturaMini)).coerceIn(0f, 1f)
        }

    /** Calcula la altura actual del panel basándose en el estado del gesto */
    fun calcularAlturaActual(): Float {
        if (!isDragging && !isAnimating) {
            return when (modoOrigen) {
                ModoPanelReproductor.MINIMIZADO -> GestureConstants.ALTURA_MINIMIZADO.value
                ModoPanelReproductor.NORMAL -> GestureConstants.ALTURA_NORMAL.value
                ModoPanelReproductor.EXPANDIDO -> screenHeightPx
            }
        }

        val alturaOrigen = getAlturaParaModo(modoOrigen)
        val alturaDestino = modoDestino?.let { getAlturaParaModo(it) } ?: alturaOrigen

        return lerp(alturaOrigen, alturaDestino, progreso)
    }

    private fun getAlturaParaModo(modo: ModoPanelReproductor): Float {
        return when (modo) {
            ModoPanelReproductor.MINIMIZADO -> GestureConstants.ALTURA_MINIMIZADO.value
            ModoPanelReproductor.NORMAL -> GestureConstants.ALTURA_NORMAL.value
            ModoPanelReproductor.EXPANDIDO -> screenHeightPx
        }
    }

    /**
     * Determina si el gesto debería completar la transición basándose en el progreso y la velocidad
     */
    fun debeCompletarTransicion(): Boolean {
        // Si la velocidad es alta, completar independientemente del progreso
        if (abs(velocidad) > GestureConstants.VELOCIDAD_SNAP_NORMAL) {
            return true
        }
        // Si el progreso supera el umbral, completar
        return progreso > GestureConstants.UMBRAL_TRANSICION_PORCENTAJE
    }

    /** Determina el modo final basándose en velocidad y progreso */
    fun calcularModoFinal(): ModoPanelReproductor {
        val destino = modoDestino ?: modoOrigen

        // Velocidad muy alta: puede saltar 2 modos
        if (abs(velocidad) > GestureConstants.VELOCIDAD_SNAP_RAPIDO) {
            return when {
                velocidad < 0 && modoOrigen == ModoPanelReproductor.MINIMIZADO ->
                    ModoPanelReproductor.EXPANDIDO
                velocidad > 0 && modoOrigen == ModoPanelReproductor.EXPANDIDO ->
                    ModoPanelReproductor.MINIMIZADO
                else -> destino
            }
        }

        // Velocidad normal o progreso suficiente: ir al destino
        return if (debeCompletarTransicion()) destino else modoOrigen
    }
}

// ==================== EVENTOS DE GESTO ====================

/** Eventos que el sistema de gestos puede emitir */
sealed interface GestureEvent {
    /** El usuario comenzó a arrastrar */
    data class DragStarted(val offsetY: Float) : GestureEvent

    /** El usuario está arrastrando */
    data class Dragging(val deltaY: Float, val velocityY: Float) : GestureEvent

    /** El usuario soltó el dedo */
    data class DragEnded(val velocityY: Float) : GestureEvent

    /** El gesto fue cancelado */
    data object DragCancelled : GestureEvent

    /** La animación de snap terminó */
    data class SnapCompleted(val modoFinal: ModoPanelReproductor) : GestureEvent
}


