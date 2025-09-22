package com.example.freeplayerm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.RepositorioDeMusicaLocal
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

sealed class AuthState {
    object Cargando : AuthState()
    data class Autenticado(val usuario: UsuarioEntity) : AuthState()
    object NoAutenticado : AuthState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val usuarioRepository: UsuarioRepository, // <-- Inyectamos el UsuarioRepository
    private val musicRepository: RepositorioDeMusicaLocal
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Cargando)
    val authState = _authState.asStateFlow()


    val usuarioActual: StateFlow<UsuarioEntity?> =
        sessionRepository.idDeUsuarioActivo.flatMapLatest { id ->
                // flatMapLatest reacciona a cada nuevo ID emitido por el Flow de sesión.
                if (id != null) {
                    // Si hay un ID, usamos el UsuarioRepository para obtener el Flow del usuario.
                    usuarioRepository.obtenerUsuarioPorIdFlow(id)
                } else {
                    // Si no hay ID (sesión cerrada), emitimos un Flow con valor nulo.
                    flowOf(null)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null // El valor inicial es nulo mientras se determina la sesión.
            )
    init {
        comprobarSesion()
        viewModelScope.launch {
            try {
                // Usamos el repositorio para escanear y guardar la música.
                // Esto no bloqueará la UI.
                musicRepository.escanearYGuardarMusica()
            } catch (e: Exception) {
                // Manejar el error silenciosamente si es necesario
                e.printStackTrace()
            }
        }
    }

    private fun comprobarSesion() {
        viewModelScope.launch {
            // Con withTimeoutOrNull, esperamos un máximo de 2 segundos.
            val usuario = withTimeoutOrNull(2000) {
                // filterNotNull() ignora el valor inicial nulo.
                // first() suspende la ejecución y espera hasta que llegue el primer usuario (no nulo).
                usuarioActual.filterNotNull().first()
            }

            // Cuando el código continúa, 'usuario' tendrá el usuario logueado
            // o será nulo si pasaron 2 segundos sin respuesta.
            if (usuario != null) {
                _authState.value = AuthState.Autenticado(usuario)
            } else {
                _authState.value = AuthState.NoAutenticado
            }
        }
    }
}