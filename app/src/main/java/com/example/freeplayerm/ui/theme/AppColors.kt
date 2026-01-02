package com.example.freeplayerm.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Sistema de colores para FreePlayerM.
 *
 * Organizado en:
 * - Colores base (Blanco, Negro, Transparente)
 * - Escala principal (ElectricViolet) - 16 tonos
 * - Escala de grises (Grays) - 7 tonos
 * - Colores semánticos (Error, Success, Warning, Info)
 * - Colores de acento (Favorito, etc.)
 */
@Immutable
object AppColors {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COLORES BASE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    val Blanco = Color(0xFFFFFFFF)
    val Negro = Color(0xFF000000)
    val Transparente = Color(0x00000000)

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ESCALA PRINCIPAL — ELECTRIC VIOLET
    // Desde casi-blanco (v1) hasta negro-violeta profundo (v16)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Immutable
    object ElectricViolet {
        // Tonos claros (para Light Theme y acentos en Dark)
        val v1 = Color(0xFFF3EEFF)  // Fondo Light / Tinte muy suave
        val v2 = Color(0xFFE1D6FF)  // onBackground Light
        val v3 = Color(0xFFC6AFFF)  // Container Light
        val v4 = Color(0xFFB08AFF)  // Acento suave Dark / Secondary
        val v5 = Color(0xFF9D64FE)  // Transición

        // Tonos vibrantes (Primarios)
        val v6 = Color(0xFF8C32FB)  // Primary Dark (vibrante)
        val v7 = Color(0xFF7300DD)  // Primary Light
        val v8 = Color(0xFF5D00B8)  // Secondary Light

        // Tonos profundos (Containers y fondos Dark)
        val v9 = Color(0xFF380074)   // PrimaryContainer Dark
        val v10 = Color(0xFF1F0048)  // SecondaryContainer Dark
        val v11 = Color(0xFF170039)  // Superficie elevada Dark
        val v12 = Color(0xFF130032)  // onBackground/onSurface Light
        val v13 = Color(0xFF11002E)  // Transición profunda

        // Tonos más oscuros (Fondos Dark Theme)
        val v14 = Color(0xFF10002D)  // Surface Dark
        val v15 = Color(0xFF0E0021)  // Background Dark
        val v16 = Color(0xFF05000C)  // Fondo más profundo (reproductor expandido)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ESCALA DE GRISES — Para texto secundario, iconos, bordes
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Immutable
    object Grays {
        val v0 = Color(0xFFBDBDBD)  // Texto secundario Light
        val v1 = Color(0xFF949494)  // Texto secundario Dark
        val v2 = Color(0xFF757575)  // Iconos activos secundarios
        val v3 = Color(0xFF595959)  // Iconos inactivos
        val v4 = Color(0xFF383838)  // Texto deshabilitado / Placeholder
        val v5 = Color(0xFF212121)  // Bordes sutiles Dark
        val v6 = Color(0xFF151515)  // Fondo inputs Dark
        val v7 = Color(0xFF0A0A0A)  // Overlay oscuro
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COLORES SEMÁNTICOS — Estados y feedback
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Immutable
    object Semantic {
        // Error
        val Error = Color(0xFFB20506)
        val ErrorLight = Color(0xFFFF5449)
        val ErrorDark = Color(0xFF930000)
        val OnError = Color(0xFFFFFFFF)
        val ErrorContainer = Color(0xFFFFDAD6)
        val OnErrorContainer = Color(0xFF410002)

        // Success
        val Success = Color(0xFF0AC429)
        val SuccessLight = Color(0xFF5EF67A)
        val SuccessDark = Color(0xFF008A1A)
        val OnSuccess = Color(0xFFFFFFFF)
        val SuccessContainer = Color(0xFFC8FFC4)
        val OnSuccessContainer = Color(0xFF002204)

        // Warning
        val Warning = Color(0xFFFFA726)
        val WarningLight = Color(0xFFFFD95B)
        val WarningDark = Color(0xFFC77800)
        val OnWarning = Color(0xFF000000)
        val WarningContainer = Color(0xFFFFE0A0)
        val OnWarningContainer = Color(0xFF2E1500)

        // Info
        val Info = Color(0xFF29B6F6)
        val InfoLight = Color(0xFF73E8FF)
        val InfoDark = Color(0xFF0086C3)
        val OnInfo = Color(0xFF000000)
        val InfoContainer = Color(0xFFCAEFFF)
        val OnInfoContainer = Color(0xFF001E2B)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COLORES DE ACENTO — Acciones especiales
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Immutable
    object Accent {
        val Favorito = Color(0xFFE056FD)
        val FavoritoPressed = Color(0xFFBE2EDC)
        val FavoritoContainer = Color(0xFF4A0072)

        val Shuffle = Color(0xFF00E5FF)
        val Repeat = Color(0xff70c900)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // OVERLAYS Y SCRIM — Para modales, bottom sheets, gradientes
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Immutable
    object Overlay {
        val Scrim = Color(0x99000000)          // 60% negro
        val ScrimLight = Color(0x4D000000)     // 30% negro
        val ScrimHeavy = Color(0xCC000000)     // 80% negro
        val GradientStart = Color(0x00000000)  // Transparente
        val GradientEnd = Color(0xE6000000)    // 90% negro (para artwork overlay)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COMPATIBILIDAD — Alias para migración suave
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Deprecated("Usar Semantic.Error", ReplaceWith("Semantic.Error"))
    val Error = Semantic.Error

    @Deprecated("Usar Semantic.Success", ReplaceWith("Semantic.Success"))
    val Exito = Semantic.Success

    @Deprecated("Usar Accent.Favorito", ReplaceWith("Accent.Favorito"))
    val Favorito = Accent.Favorito
}