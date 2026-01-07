package com.example.freeplayerm.ui.features.player.layouts

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.ui.features.player.model.PlayerEffect
import com.example.freeplayerm.ui.features.player.viewmodel.PlayerViewModel
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * 🎵 PLAYER SCREEN - Stateful Layer
 *
 * Conecta con PlayerViewModel y maneja efectos one-shot.
 * Pasa estado y eventos hacia abajo (UDF).
 */
@Composable
fun PlayerScreen(
   viewModel: PlayerViewModel = hiltViewModel(),
   @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
   val state by viewModel.state.collectAsStateWithLifecycle()
   val context = LocalContext.current
   val haptic = LocalHapticFeedback.current
   
   // Manejo de efectos one-shot
   LaunchedEffect(Unit) {
      viewModel.effects.collect { effect ->
         when (effect) {
            is PlayerEffect.ShowToast -> {
               Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
            is PlayerEffect.ShowError -> {
               Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
            }
            is PlayerEffect.OpenUrl -> {
               runCatching {
                  val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                  context.startActivity(intent)
               }.onFailure { e ->
                  Toast.makeText(
                     context,
                     "No se pudo abrir el enlace",
                     Toast.LENGTH_SHORT
                  ).show()
               }
            }
            is PlayerEffect.HapticClick -> {
               haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            is PlayerEffect.HapticHeavy -> {
               haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            is PlayerEffect.HapticSuccess -> {
               haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> { /* Otros efectos */ }
         }
      }
   }
   
   PlayerContent(
      state = state,
      onEvent = viewModel::onEvent,
      modifier = modifier,
   )
}