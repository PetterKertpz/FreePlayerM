package com.example.freeplayerm.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
   private val sessionRepository: SessionRepository,
   private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
   
   // Estado de las preferencias del usuario
   val preferences: StateFlow<UserPreferencesEntity?> = sessionRepository.idDeUsuarioActivo
      .filterNotNull()
      .flatMapLatest { userId ->
         preferencesRepository.obtenerPreferenciasPorIdFlow(userId)
      }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = null
      )
   
   // Actualizar tema oscuro
   fun actualizarTemaOscuro(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(temaOscuro = habilitado)
            )
         }
      }
   }
   
   // Actualizar animaciones
   fun actualizarAnimaciones(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(animacionesHabilitadas = habilitado)
            )
         }
      }
   }
   
   // Actualizar calidad de audio
   fun actualizarCalidadAudio(calidad: String) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(calidadPreferida = calidad)
            )
         }
      }
   }
   
   // Actualizar crossfade
   fun actualizarCrossfade(milisegundos: Int) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(crossfadeMs = milisegundos)
            )
         }
      }
   }
   
   // Actualizar reproducción automática
   fun actualizarReproduccionAutomatica(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(reproduccionAutomatica = habilitado)
            )
         }
      }
   }
   
   // Actualizar notificaciones
   fun actualizarNotificaciones(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(notificacionesHabilitadas = habilitado)
            )
         }
      }
   }
   
   // Actualizar streaming solo WiFi
   fun actualizarSoloWifiStreaming(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(soloWifiStreaming = habilitado)
            )
         }
      }
   }
   
   // Actualizar normalización de volumen
   fun actualizarNormalizarVolumen(habilitado: Boolean) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(normalizarVolumen = habilitado)
            )
         }
      }
   }
   
   // Actualizar tamaño de caché
   fun actualizarCacheSize(sizeMb: Int) {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(cacheSizeMb = sizeMb)
            )
         }
      }
   }
   
   // Restaurar configuración por defecto
   fun restaurarPorDefecto() {
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               UserPreferencesEntity.crearPorDefecto(prefs.idUsuario)
            )
         }
      }
   }
}