package com.example.freeplayerm.ui.features.player.gesture

import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Stable

// ═══════════════════════════════════════════════════════════════════════════
// ⚙️ CONFIGURACIÓN UNIFICADA DEL SISTEMA DE GESTOS Y ALTURAS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Configuración inmutable y centralizada para todo el sistema de gestos del reproductor.
 *
 * Incluye:
 * - Alturas de los modos (como porcentajes de pantalla)
 * - Umbrales de velocidad y posición
 * - Parámetros de rubber banding
 * - Configuración de animaciones
 * - Habilitación de haptic feedback
 *
 * @property alturaMinimizado Altura del modo minimizado (0.10f = 10% de pantalla)
 * @property alturaNormal Altura del modo normal (0.25f = 25% de pantalla)
 * @property alturaExpandido Altura del modo expandido (1f = 100% de pantalla)
 */
@Stable
data class PlayerGestureConfig(
    // ═══════════════════════════════════════════════════════════════════════
    // ALTURAS DE LOS MODOS (porcentajes de pantalla)
    // ═══════════════════════════════════════════════════════════════════════

    /** Altura del modo minimizado (ej: 0.10f = 10% = 80dp en 800dp) */
    val alturaMinimizado: Float = 0.10f,

    /** Altura del modo normal (ej: 0.25f = 25% = 200dp en 800dp) */
    val alturaNormal: Float = 0.22f,

    /** Altura del modo expandido (siempre 1f = 100% de pantalla) */
    val alturaExpandido: Float = 1f,

    // ═══════════════════════════════════════════════════════════════════════
    // UMBRALES DE TRANSICIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Umbral inferior para determinar modo minimizado (< 30% se considera minimizado) */
    val umbralMinimizado: Float = 0.3f,

    /** Umbral superior para iniciar transición a expandido (> 50% inicia expansión) */
    val umbralExpandido: Float = 0.5f,

    // ═══════════════════════════════════════════════════════════════════════
    // UMBRALES DE VELOCIDAD (px/ms)
    // ═══════════════════════════════════════════════════════════════════════

    /** Velocidad para saltar 2 modos directamente (snap rápido) */
    val velocidadSnapRapido: Float = 1.5f,

    /** Velocidad para saltar 1 modo (snap normal) */
    val velocidadSnapNormal: Float = 0.4f,

    /** Velocidad mínima para considerar un fling */
    val velocidadMinimaFling: Float = 0.2f,

    // ═══════════════════════════════════════════════════════════════════════
    // UMBRALES DE POSICIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Porcentaje del recorrido para completar transición sin velocidad */
    val umbralTransicionPorcentaje: Float = 0.4f,

    /** Distancia mínima en px para activar un gesto */
    val distanciaMinimaActivacion: Float = 20f,

    // ═══════════════════════════════════════════════════════════════════════
    // RUBBER BANDING
    // ═══════════════════════════════════════════════════════════════════════

    /** Factor de resistencia base (0-1, menor = más resistencia) */
    val resistenciaBase: Float = 0.55f,

    /** Máximo over-scroll permitido como fracción */
    val maxOverscroll: Float = 0.15f,

    // ═══════════════════════════════════════════════════════════════════════
    // ANIMACIONES
    // ═══════════════════════════════════════════════════════════════════════

    val springStiffnessRapido: Float = Spring.StiffnessHigh,
    val springStiffnessNormal: Float = Spring.StiffnessMedium,
    val springStiffnessLento: Float = Spring.StiffnessMediumLow,
    val dampingRatioRapido: Float = Spring.DampingRatioLowBouncy,
    val dampingRatioNormal: Float = Spring.DampingRatioMediumBouncy,
    val dampingRatioLento: Float = Spring.DampingRatioNoBouncy,

    // ═══════════════════════════════════════════════════════════════════════
    // HAPTIC FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════

    val hapticEnUmbrales: Boolean = true,
    val hapticEnSnap: Boolean = true,
) {
    companion object {
        /**
         * Configuración por defecto optimizada.
         */
        val Default = PlayerGestureConfig()

        /**
         * Configuración para dispositivos de gama baja (menos animaciones).
         */
        val LowEnd = PlayerGestureConfig(
            springStiffnessRapido = Spring.StiffnessHigh,
            springStiffnessNormal = Spring.StiffnessHigh,
            springStiffnessLento = Spring.StiffnessMedium,
            dampingRatioRapido = Spring.DampingRatioNoBouncy,
            dampingRatioNormal = Spring.DampingRatioNoBouncy,
            dampingRatioLento = Spring.DampingRatioNoBouncy,
        )
    }

    /**
     * Valida que la configuración sea coherente.
     * @throws IllegalArgumentException si los valores no son válidos
     */
    init {
        require(alturaMinimizado in 0f..1f) { "alturaMinimizado debe estar en [0, 1]" }
        require(alturaNormal in 0f..1f) { "alturaNormal debe estar en [0, 1]" }
        require(alturaExpandido == 1f) { "alturaExpandido debe ser 1f (pantalla completa)" }
        require(alturaMinimizado < alturaNormal) {
            "alturaMinimizado ($alturaMinimizado) debe ser menor que alturaNormal ($alturaNormal)"
        }
        require(alturaNormal < alturaExpandido) {
            "alturaNormal ($alturaNormal) debe ser menor que alturaExpandido ($alturaExpandido)"
        }
        require(umbralMinimizado < umbralExpandido) {
            "umbralMinimizado ($umbralMinimizado) debe ser menor que umbralExpandido ($umbralExpandido)"
        }
    }
}