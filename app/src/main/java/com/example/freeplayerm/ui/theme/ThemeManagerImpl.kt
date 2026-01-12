package com.example.freeplayerm.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Stable
@Singleton
class ThemeManagerImpl @Inject constructor(
   private val sessionRepository: SessionRepository,
   private val preferencesRepository: UserPreferencesRepository
) : ThemeManager {
   
   private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
   
   private var _isDarkTheme by mutableStateOf(true)
   override val isDarkTheme: Boolean get() = _isDarkTheme
   
   private var _animationsEnabled by mutableStateOf(true)
   override val animationsEnabled: Boolean get() = _animationsEnabled
   
   private var _currentPreferences by mutableStateOf<UserPreferencesEntity?>(null)
   override val currentPreferences: UserPreferencesEntity? get() = _currentPreferences
   
   init {
      observePreferences()
   }
   
   private fun observePreferences() {
      scope.launch {
         sessionRepository.idDeUsuarioActivo
            .filterNotNull()
            .flatMapLatest { userId ->
               preferencesRepository.obtenerPreferenciasPorIdFlow(userId)
            }
            .collectLatest { prefs ->
               prefs?.let {
                  _isDarkTheme = it.temaOscuro
                  _animationsEnabled = it.animacionesHabilitadas
                  _currentPreferences = it
               }
            }
      }
   }
   
   override fun updateTheme(darkTheme: Boolean) {
      _isDarkTheme = darkTheme
   }
   
   override fun updateAnimations(enabled: Boolean) {
      _animationsEnabled = enabled
   }
}