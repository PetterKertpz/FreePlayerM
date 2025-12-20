package com.example.freeplayerm.ui.features.inicio.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.core.auth.SignInResult
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- CAMBIO CLAVE AQUÍ ---
// Reemplazamos 'estaCargando' por dos variables específicas.
data class LoginEstado(
    val correoOUsuario: String = "",
    val contrasena: String = "",
    val cargandoLocalmente: Boolean = false, // <-- NUEVO
    val cargandoConGoogle: Boolean = false, // <-- NUEVO
    val error: String? = null,
    val usuarioIdExitoso: Int? = null
)

// (El sealed class LoginEvento se mantiene igual)
sealed class LoginEvento {
    data class CorreoOUsuarioCambiado(val valor: String) : LoginEvento()
    data class ContrasenaCambiada(val valor: String) : LoginEvento()
    object BotonLoginPresionado : LoginEvento()
    data class InicioSesionGoogleCompletado(val resultado: SignInResult) : LoginEvento()
    object ConsumirEventoDeNavegacion : LoginEvento()
    object ConsumirError : LoginEvento()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositorio: UsuarioRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(LoginEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: LoginEvento) {
        when (evento) {
            is LoginEvento.CorreoOUsuarioCambiado -> _estadoUi.update { it.copy(correoOUsuario = evento.valor, error = null) }
            is LoginEvento.ContrasenaCambiada -> _estadoUi.update { it.copy(contrasena = evento.valor, error = null) }
            LoginEvento.BotonLoginPresionado -> iniciarSesionLocal()
            is LoginEvento.InicioSesionGoogleCompletado -> procesarResultadoGoogle(evento.resultado)
            LoginEvento.ConsumirEventoDeNavegacion -> _estadoUi.update { it.copy(usuarioIdExitoso = null) }
            LoginEvento.ConsumirError -> _estadoUi.update { it.copy(error = null) }
        }
    }

    private fun procesarResultadoGoogle(resultadoSignIn: SignInResult) {
        // Activamos solo la carga de Google
        _estadoUi.update { it.copy(cargandoConGoogle = true) }
        viewModelScope.launch {
            when (resultadoSignIn) {
                is SignInResult.Success -> {
                    val datosUsuario = resultadoSignIn.data
                    if (datosUsuario.correo == null) {
                        _estadoUi.update { it.copy(error = "No se pudo obtener el correo de Google.", cargandoConGoogle = false) }
                        return@launch
                    }
                    repositorio.buscarOCrearUsuarioGoogle(
                        correo = datosUsuario.correo,
                        nombreUsuario = datosUsuario.nombreUsuario ?: "Usuario",
                        fotoUrl = datosUsuario.fotoPerfilUrl
                    ).onSuccess { usuario ->
                        sessionRepository.guardarIdDeUsuario(usuario.idUsuario)
                        // Desactivamos la carga al terminar
                        _estadoUi.update { it.copy(cargandoConGoogle = false, usuarioIdExitoso = usuario.idUsuario) }
                    }.onFailure { error ->
                        _estadoUi.update { it.copy(cargandoConGoogle = false, error = error.message ?: "No se pudo guardar el usuario.") }
                    }
                }
                is SignInResult.Error -> _estadoUi.update { it.copy(error = resultadoSignIn.message, cargandoConGoogle = false) }
                is SignInResult.Cancelled -> _estadoUi.update { it.copy(cargandoConGoogle = false) }
            }
        }
    }

    private fun iniciarSesionLocal() {
        val estadoActual = _estadoUi.value
        if (estadoActual.correoOUsuario.isBlank() || estadoActual.contrasena.isBlank()) {
            _estadoUi.update { it.copy(error = "El correo y la contraseña son obligatorios.") }
            return
        }
        // Activamos solo la carga local
        _estadoUi.update { it.copy(cargandoLocalmente = true) }
        viewModelScope.launch {
            repositorio.iniciarSesionLocal(
                identificador = estadoActual.correoOUsuario,
                contrasena = estadoActual.contrasena
            ).onSuccess { usuario ->
                sessionRepository.guardarIdDeUsuario(usuario.idUsuario)
                // Desactivamos la carga al terminar
                _estadoUi.update { it.copy(cargandoLocalmente = false, usuarioIdExitoso = usuario.idUsuario) }
            }.onFailure { error ->
                _estadoUi.update { it.copy(cargandoLocalmente = false, error = error.message) }
            }
        }
    }
}