// en: app/src/main/java/com/example/freeplayerm/ui/features/settings/SettingsRoute.kt
package com.example.freeplayerm.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Colores consistentes con SettingsScreen
private val FondoScreen = Color(0xFF0F0518)
private val NeonPrimario = Color(0xFFD500F9)

// Composable que conecta el ViewModel con el Screen stateless
@Composable
fun SettingsRoute(
   viewModel: SettingsViewModel = hiltViewModel(),
   onNavigateBack: () -> Unit
) {
   val preferences by viewModel.preferences.collectAsStateWithLifecycle()
   
   preferences?.let { prefs ->
      SettingsScreen(
         preferences = prefs,
         onNavigateBack = onNavigateBack,
         onUpdateTemaOscuro = viewModel::actualizarTemaOscuro,
         onUpdateAnimaciones = viewModel::actualizarAnimaciones,
         onUpdateCalidadAudio = viewModel::actualizarCalidadAudio,
         onUpdateCrossfade = viewModel::actualizarCrossfade,
         onUpdateReproduccionAutomatica = viewModel::actualizarReproduccionAutomatica,
         onUpdateNotificaciones = viewModel::actualizarNotificaciones,
         onUpdateSoloWifiStreaming = viewModel::actualizarSoloWifiStreaming,
         onUpdateNormalizarVolumen = viewModel::actualizarNormalizarVolumen,
         onUpdateCacheSize = viewModel::actualizarCacheSize,
         onRestaurarDefecto = viewModel::restaurarPorDefecto
      )
   } ?: SettingsLoadingScreen()
}

// Pantalla de carga mientras se obtienen las preferencias
@Composable
private fun SettingsLoadingScreen() {
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            Brush.verticalGradient(
               colors = listOf(
                  FondoScreen,
                  FondoScreen.copy(alpha = 0.8f),
                  Color.Black
               )
            )
         ),
      contentAlignment = Alignment.Center
   ) {
      CircularProgressIndicator(color = NeonPrimario)
   }
}