package com.example.freeplayerm.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
   private val userRepository: UserRepository,
   private val sessionRepository: SessionRepository
) : ViewModel() {
   
   // Flujo reactivo del usuario actual
   val usuario: StateFlow<UserEntity?> = sessionRepository.idDeUsuarioActivo
      .filterNotNull()
      .flatMapLatest { userId ->
         userRepository.obtenerUsuarioPorIdFlow(userId)
      }
      .stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = null
      )
   
   // Cerrar sesión
   fun cerrarSesion() {
      viewModelScope.launch {
         sessionRepository.cerrarSesion()
      }
   }
   
   // Actualizar foto de perfil
   fun actualizarFotoPerfil(nuevaFotoUrl: String) {
      viewModelScope.launch {
         val usuarioActual = usuario.value ?: return@launch
         val usuarioActualizado = usuarioActual.copy(fotoPerfil = nuevaFotoUrl)
         userRepository.actualizarUsuario(usuarioActualizado)
      }
   }
   
   // Actualizar información básica
   fun actualizarInformacion(
      nombreCompleto: String? = null,
      biografia: String? = null
   ) {
      viewModelScope.launch {
         val usuarioActual = usuario.value ?: return@launch
         val usuarioActualizado = usuarioActual.copy(
            nombreCompleto = nombreCompleto ?: usuarioActual.nombreCompleto,
            biografia = biografia ?: usuarioActual.biografia
         )
         userRepository.actualizarUsuario(usuarioActualizado)
      }
   }
}