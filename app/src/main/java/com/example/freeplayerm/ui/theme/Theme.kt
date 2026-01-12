package com.example.freeplayerm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ESQUEMA OSCURO — Prioridad para reproductor de música
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private val DarkColorScheme = darkColorScheme(
    // ── Colores Primarios ──
    primary = AppColors.ElectricViolet.v6,
    onPrimary = AppColors.Blanco,
    primaryContainer = AppColors.ElectricViolet.v9,
    onPrimaryContainer = AppColors.ElectricViolet.v2,
    inversePrimary = AppColors.ElectricViolet.v7,

    // ── Colores Secundarios ──
    secondary = AppColors.ElectricViolet.v4,
    onSecondary = AppColors.Negro,
    secondaryContainer = AppColors.ElectricViolet.v10,
    onSecondaryContainer = AppColors.ElectricViolet.v3,

    // ── Colores Terciarios ──
    tertiary = AppColors.Accent.Shuffle,
    onTertiary = AppColors.Negro,
    tertiaryContainer = AppColors.ElectricViolet.v11,
    onTertiaryContainer = AppColors.ElectricViolet.v3,

    // ── Fondos y Superficies ──
    background = AppColors.ElectricViolet.v16,
    onBackground = AppColors.ElectricViolet.v2,
    surface = AppColors.ElectricViolet.v14,
    onSurface = AppColors.ElectricViolet.v2,
    surfaceVariant = AppColors.ElectricViolet.v11,
    onSurfaceVariant = AppColors.Grays.v1,
    surfaceTint = AppColors.ElectricViolet.v6,

    // ── Superficies Inversas (para Snackbars, etc.) ──
    inverseSurface = AppColors.ElectricViolet.v2,
    inverseOnSurface = AppColors.ElectricViolet.v14,

    // ── Bordes y Contornos ──
    outline = AppColors.Grays.v3,
    outlineVariant = AppColors.Grays.v5,

    // ── Scrim (overlay para modales) ──
    scrim = AppColors.Overlay.Scrim,

    // ── Error ──
    error = AppColors.Semantic.ErrorLight,
    onError = AppColors.Negro,
    errorContainer = AppColors.Semantic.ErrorDark,
    onErrorContainer = AppColors.Semantic.ErrorContainer
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ESQUEMA CLARO
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private val LightColorScheme = lightColorScheme(
    // ── Colores Primarios ──
    primary = AppColors.ElectricViolet.v7,
    onPrimary = AppColors.Blanco,
    primaryContainer = AppColors.ElectricViolet.v3,
    onPrimaryContainer = AppColors.ElectricViolet.v10,
    inversePrimary = AppColors.ElectricViolet.v4,

    // ── Colores Secundarios ──
    secondary = AppColors.ElectricViolet.v8,
    onSecondary = AppColors.Blanco,
    secondaryContainer = AppColors.ElectricViolet.v2,
    onSecondaryContainer = AppColors.ElectricViolet.v10,

    // ── Colores Terciarios ──
    tertiary = AppColors.Semantic.InfoDark,
    onTertiary = AppColors.Blanco,
    tertiaryContainer = AppColors.Semantic.InfoContainer,
    onTertiaryContainer = AppColors.Semantic.OnInfoContainer,

    // ── Fondos y Superficies ──
    background = AppColors.ElectricViolet.v1,
    onBackground = AppColors.ElectricViolet.v12,
    surface = AppColors.Blanco,
    onSurface = AppColors.ElectricViolet.v12,
    surfaceVariant = AppColors.ElectricViolet.v2,
    onSurfaceVariant = AppColors.Grays.v3,
    surfaceTint = AppColors.ElectricViolet.v7,

    // ── Superficies Inversas ──
    inverseSurface = AppColors.ElectricViolet.v12,
    inverseOnSurface = AppColors.ElectricViolet.v1,

    // ── Bordes y Contornos ──
    outline = AppColors.Grays.v2,
    outlineVariant = AppColors.ElectricViolet.v3,

    // ── Scrim ──
    scrim = AppColors.Overlay.Scrim,

    // ── Error ──
    error = AppColors.Semantic.Error,
    onError = AppColors.Blanco,
    errorContainer = AppColors.Semantic.ErrorContainer,
    onErrorContainer = AppColors.Semantic.OnErrorContainer
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COLORES EXTENDIDOS — Para casos especiales del reproductor
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Immutable
data class ExtendedColors(
    // Superficies con elevación tonal (Material 3 spec)
    val surfaceElevated1: Color,
    val surfaceElevated2: Color,
    val surfaceElevated3: Color,
    val surfaceElevated4: Color,
    val surfaceElevated5: Color,

    // Estados semánticos
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,

    // Acentos del reproductor
    val favorito: Color,
    val favoritoContainer: Color,
    val shuffle: Color,
    val repeat: Color,

    // Overlays
    val scrimLight: Color,
    val scrimHeavy: Color,
    val gradientStart: Color,
    val gradientEnd: Color,

    // Player específico
    val playerBackground: Color,
    val miniPlayerSurface: Color
)

private val DarkExtendedColors = ExtendedColors(
    // Elevación tonal en Dark: mezcla con primary
    surfaceElevated1 = AppColors.ElectricViolet.v14,
    surfaceElevated2 = AppColors.ElectricViolet.v13,
    surfaceElevated3 = AppColors.ElectricViolet.v12,
    surfaceElevated4 = AppColors.ElectricViolet.v11,
    surfaceElevated5 = AppColors.ElectricViolet.v10,

    success = AppColors.Semantic.SuccessLight,
    onSuccess = AppColors.Negro,
    successContainer = AppColors.Semantic.SuccessDark,
    warning = AppColors.Semantic.WarningLight,
    onWarning = AppColors.Negro,
    warningContainer = AppColors.Semantic.WarningDark,
    info = AppColors.Semantic.InfoLight,
    onInfo = AppColors.Negro,
    infoContainer = AppColors.Semantic.InfoDark,

    favorito = AppColors.Accent.Favorito,
    favoritoContainer = AppColors.Accent.FavoritoContainer,
    shuffle = AppColors.Accent.Shuffle,
    repeat = AppColors.Accent.Repeat,

    scrimLight = AppColors.Overlay.ScrimLight,
    scrimHeavy = AppColors.Overlay.ScrimHeavy,
    gradientStart = AppColors.Overlay.GradientStart,
    gradientEnd = AppColors.Overlay.GradientEnd,

    playerBackground = AppColors.ElectricViolet.v16,
    miniPlayerSurface = AppColors.ElectricViolet.v14
)

private val LightExtendedColors = ExtendedColors(
    surfaceElevated1 = AppColors.Blanco,
    surfaceElevated2 = AppColors.ElectricViolet.v1,
    surfaceElevated3 = AppColors.ElectricViolet.v2,
    surfaceElevated4 = AppColors.ElectricViolet.v2,
    surfaceElevated5 = AppColors.ElectricViolet.v3,

    success = AppColors.Semantic.Success,
    onSuccess = AppColors.Blanco,
    successContainer = AppColors.Semantic.SuccessContainer,
    warning = AppColors.Semantic.Warning,
    onWarning = AppColors.Negro,
    warningContainer = AppColors.Semantic.WarningContainer,
    info = AppColors.Semantic.Info,
    onInfo = AppColors.Negro,
    infoContainer = AppColors.Semantic.InfoContainer,

    favorito = AppColors.Accent.Favorito,
    favoritoContainer = AppColors.ElectricViolet.v3,
    shuffle = AppColors.Semantic.InfoDark,
    repeat = AppColors.Semantic.SuccessDark,

    scrimLight = AppColors.Overlay.ScrimLight,
    scrimHeavy = AppColors.Overlay.ScrimHeavy,
    gradientStart = AppColors.Overlay.GradientStart,
    gradientEnd = AppColors.Overlay.GradientEnd,

    playerBackground = AppColors.ElectricViolet.v1,
    miniPlayerSurface = AppColors.Blanco
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TEMA PRINCIPAL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun FreePlayerMTheme(
   themeManager: ThemeManager? = null,
   darkTheme: Boolean = themeManager?.isDarkTheme ?: isSystemInDarkTheme(),
   animationsEnabled: Boolean = themeManager?.animationsEnabled ?: true,
   dynamicColor: Boolean = false,
   content: @Composable () -> Unit
) {
   val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
         val context = LocalContext.current
         if (darkTheme) dynamicDarkColorScheme(context)
         else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
   }
   
   val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
   
   val view = LocalView.current
   if (!view.isInEditMode) {
      SideEffect {
         val window = (view.context as Activity).window
         window.statusBarColor = colorScheme.background.toArgb()
         window.navigationBarColor = colorScheme.background.toArgb()
         val insetsController = WindowCompat.getInsetsController(window, view)
         insetsController.isAppearanceLightStatusBars = !darkTheme
         insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
   }
   
   // Usar ThemeManager proporcionado o crear uno para previews
   val effectiveThemeManager = themeManager ?: remember {
      PreviewThemeManager(darkTheme, animationsEnabled)
   }
   
   CompositionLocalProvider(
      LocalExtendedColors provides extendedColors,
      LocalExtendedTypography provides extendedTypography,
      LocalAnimationsEnabled provides animationsEnabled,
      LocalThemeManager provides effectiveThemeManager
   ) {
      MaterialTheme(
         colorScheme = colorScheme,
         typography = Typography,
         content = content
      )
   }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// EXTENSIÓN PARA ACCESO FÁCIL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object FreePlayerTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}