package com.example.freeplayerm


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.RepositorioDeMusicaLocal
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definición de estados de autenticación
sealed class AuthState {
    object Cargando : AuthState()
    data class Autenticado(val usuario: UsuarioEntity) : AuthState()
    object NoAutenticado : AuthState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val musicRepository: RepositorioDeMusicaLocal
) : ViewModel() {

    /**
     * ✅ FLUJO REACTIVO DE ESTADO DE AUTENTICACIÓN
     * -----------------------------------------------------
     * Este StateFlow observa cambios en el ID de sesión.
     * 1. Si el ID es null -> Emite NoAutenticado inmediatamente.
     * 2. Si hay ID -> Busca el usuario en la BD.
     * 3. Si encuentra usuario -> Emite Autenticado.
     * 4. Si hay ID pero no usuario (error de datos) -> Emite NoAutenticado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val authState: StateFlow<AuthState> = sessionRepository.idDeUsuarioActivo
        .flatMapLatest { id ->
            if (id == null) {
                // No hay sesión activa en DataStore
                flowOf(AuthState.NoAutenticado)
            } else {
                // Hay sesión, buscamos los detalles del usuario en tiempo real
                usuarioRepository.obtenerUsuarioPorIdFlow(id).map { usuario ->
                    if (usuario != null) {
                        AuthState.Autenticado(usuario)
                    } else {
                        // Caso borde: Hay ID de sesión pero el usuario no existe en la BD local
                        AuthState.NoAutenticado
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Cargando
        )

    init {
        // Iniciamos el escaneo de música en segundo plano al arrancar la App.
        // NOTA: Esto solo funcionará si el usuario ya concedió permisos previamente.
        // Si es la primera instalación, esto fallará silenciosamente (lo cual está bien,
        // ya que el escaneo se volverá a pedir en la pantalla de Biblioteca).
        viewModelScope.launch(Dispatchers.IO) {
            try {
                musicRepository.escanearYGuardarMusica()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}