package com.example.freeplayerm.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel
@Inject
constructor(
   private val userRepository: UserRepository,
   private val sessionRepository: SessionRepository,
) : ViewModel() {

   // Flujo reactivo del usuario con estadísticas calculadas automáticamente
   val usuario: StateFlow<UserEntity?> =
      sessionRepository.idDeUsuarioActivo
         .filterNotNull()
         .flatMapLatest { userId -> userRepository.obtenerUsuarioPorIdFlow(userId) }
         .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
         )

   // Sincronizar estadísticas manualmente (útil para forzar actualización)
   fun sincronizarEstadisticas() {
      viewModelScope.launch {
         // Recolectamos el primer valor emitido por el Flow y dejamos de escuchar
         sessionRepository.idDeUsuarioActivo.firstOrNull()?.let { userId ->
            userRepository.sincronizarEstadisticas(userId)
         }
      }
   }

   fun registrarInicioSesion() {
      viewModelScope.launch {
         sessionRepository.idDeUsuarioActivo.firstOrNull()?.let { userId ->
            userRepository.actualizarUltimaSesion(userId)
         }
      }
   }

   // Cerrar sesión
   fun cerrarSesion() {
      viewModelScope.launch { sessionRepository.cerrarSesion() }
   }

   // Actualizar foto de perfil
   fun actualizarFotoPerfil(nuevaFotoUrl: String) {
      viewModelScope.launch {
         val usuarioActual = usuario.value ?: return@launch
         userRepository.actualizarFotoPerfil(usuarioActual.idUsuario, nuevaFotoUrl)
      }
   }

   // Actualizar información básica
   fun actualizarInformacion(nombreCompleto: String? = null, biografia: String? = null) {
      viewModelScope.launch {
         val usuarioActual = usuario.value ?: return@launch
         userRepository.actualizarInformacion(
            userId = usuarioActual.idUsuario,
            nombreCompleto = nombreCompleto,
            biografia = biografia,
         )
      }
   }
}
