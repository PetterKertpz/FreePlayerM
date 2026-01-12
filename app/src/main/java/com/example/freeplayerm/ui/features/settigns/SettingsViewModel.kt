package com.example.freeplayerm.ui.features.settigns

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import com.example.freeplayerm.services.PlayerPreferencesManager
import com.example.freeplayerm.services.StreamingState
import com.example.freeplayerm.ui.theme.ThemeManager
import com.example.freeplayerm.utils.NetworkMonitor
import com.example.freeplayerm.utils.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de UI para Settings
data class SettingsUiState(
   val isLoading: Boolean = true,
   val networkStatus: NetworkStatus = NetworkStatus.Unknown,
   val streamingBlocked: Boolean = false,
   val streamingBlockedReason: String? = null,
   val notificationPermissionGranted: Boolean = true,
   val showNotificationPermissionDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
   @ApplicationContext private val context: Context,
   private val sessionRepository: SessionRepository,
   private val preferencesRepository: UserPreferencesRepository,
   private val themeManager: ThemeManager,
   private val networkMonitor: NetworkMonitor,
   private val playerPreferencesManager: PlayerPreferencesManager
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
   
   // Estado de UI
   private val _uiState = MutableStateFlow(SettingsUiState())
   val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
   
   // Estado de red
   val networkStatus: StateFlow<NetworkStatus> = networkMonitor.networkStatus
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = NetworkStatus.Unknown
      )
   
   init {
      observeNetworkAndStreaming()
      checkNotificationPermission()
   }
   
   private fun observeNetworkAndStreaming() {
      viewModelScope.launch {
         // Observar estado de streaming
         playerPreferencesManager.streamingState.collect { state ->
            _uiState.value = _uiState.value.copy(
               streamingBlocked = state is StreamingState.Blocked,
               streamingBlockedReason = (state as? StreamingState.Blocked)?.reason
            )
         }
      }
      
      viewModelScope.launch {
         networkMonitor.networkStatus.collect { status ->
            _uiState.value = _uiState.value.copy(networkStatus = status)
         }
      }
      
      viewModelScope.launch {
         preferences.collect { prefs ->
            _uiState.value = _uiState.value.copy(isLoading = prefs == null)
         }
      }
   }
   
   private fun checkNotificationPermission() {
      val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
         ) == PackageManager.PERMISSION_GRANTED
      } else {
         true
      }
      _uiState.value = _uiState.value.copy(notificationPermissionGranted = hasPermission)
   }
   
   // Actualizar tema oscuro - con efecto inmediato
   fun actualizarTemaOscuro(habilitado: Boolean) {
      // Actualización inmediata en UI
      themeManager.updateTheme(habilitado)
      
      // Persistir en BD
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(temaOscuro = habilitado)
            )
         }
      }
   }
   
   // Actualizar animaciones - con efecto inmediato
   fun actualizarAnimaciones(habilitado: Boolean) {
      themeManager.updateAnimations(habilitado)
      
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
      // Si intenta habilitar pero no tiene permiso, mostrar diálogo
      if (habilitado && !_uiState.value.notificationPermissionGranted) {
         _uiState.value = _uiState.value.copy(showNotificationPermissionDialog = true)
         return
      }
      
      viewModelScope.launch {
         preferences.value?.let { prefs ->
            preferencesRepository.actualizarPreferencias(
               prefs.copy(notificacionesHabilitadas = habilitado)
            )
         }
      }
   }
   
   // Cerrar diálogo de permisos
   fun dismissNotificationPermissionDialog() {
      _uiState.value = _uiState.value.copy(showNotificationPermissionDialog = false)
   }
   
   // Actualizar después de solicitar permiso
   fun onNotificationPermissionResult(granted: Boolean) {
      _uiState.value = _uiState.value.copy(
         notificationPermissionGranted = granted,
         showNotificationPermissionDialog = false
      )
      
      if (granted) {
         actualizarNotificaciones(true)
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
            val defaultPrefs = UserPreferencesEntity.crearPorDefecto(prefs.idUsuario)
            preferencesRepository.actualizarPreferencias(defaultPrefs)
            
            // Actualizar UI inmediatamente
            themeManager.updateTheme(defaultPrefs.temaOscuro)
            themeManager.updateAnimations(defaultPrefs.animacionesHabilitadas)
         }
      }
   }
   
   // Verificaciones de estado
   fun isWifiConnected(): Boolean = networkMonitor.isWifiConnected()
   
   fun canStream(): Boolean = playerPreferencesManager.canPlay()
   
   fun getStreamingBlockedReason(): String? = playerPreferencesManager.getBlockedReason()
}