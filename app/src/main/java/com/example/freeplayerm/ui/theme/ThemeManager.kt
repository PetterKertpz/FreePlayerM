package com.example.freeplayerm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity

// CompositionLocal para acceso al tema desde cualquier Composable
val LocalThemeManager = compositionLocalOf<ThemeManager> {
   error("ThemeManager no proporcionado")
}

// CompositionLocal para animaciones habilitadas
val LocalAnimationsEnabled = compositionLocalOf { true }

// Interfaz para el gestor de tema (permite testing y previews)
interface ThemeManager {
   val isDarkTheme: Boolean
   val animationsEnabled: Boolean
   val currentPreferences: UserPreferencesEntity?
   fun updateTheme(darkTheme: Boolean)
   fun updateAnimations(enabled: Boolean)
}

// Implementación dummy para Previews
class PreviewThemeManager(
   override var isDarkTheme: Boolean = true,
   override var animationsEnabled: Boolean = true,
   override var currentPreferences: UserPreferencesEntity? = null
) : ThemeManager {
   override fun updateTheme(darkTheme: Boolean) { isDarkTheme = darkTheme }
   override fun updateAnimations(enabled: Boolean) { animationsEnabled = enabled }
}

// Extension para acceso fácil desde Composables
object ThemeConfig {
   val isDarkTheme: Boolean
      @Composable get() = LocalThemeManager.current.isDarkTheme
   
   val animationsEnabled: Boolean
      @Composable get() = LocalAnimationsEnabled.current
}