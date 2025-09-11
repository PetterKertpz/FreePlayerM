package com.example.freeplayerm.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.core.auth.SignInResult
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. El estado ahora es específico para el Login. ¡No más 'nombreUsuario'!
data class LoginEstado(
    val correoOUsuario: String = "",
    val contrasena: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

// 2. Los eventos también son específicos del Login.
sealed class LoginEvento {
    data class CorreoOUsuarioCambiado(val valor: String) : LoginEvento()
    data class ContrasenaCambiada(val valor: String) : LoginEvento()
    object BotonLoginPresionado : LoginEvento()
    object BotonGooglePresionado : LoginEvento()
    object ConsumirEventoDeNavegacion : LoginEvento()
    object ConsumirError : LoginEvento()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositorio: UsuarioRepository,
    private val googleAuthCliente: GoogleAuthUiClient
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(LoginEstado())
    val estadoUi = _estadoUi.asStateFlow()

    // 3. El 'when' es mucho más pequeño y fácil de leer.
    fun enEvento(evento: LoginEvento) {
        when (evento) {
            is LoginEvento.CorreoOUsuarioCambiado -> _estadoUi.update { it.copy(correoOUsuario = evento.valor, error = null) }
            is LoginEvento.ContrasenaCambiada -> _estadoUi.update { it.copy(contrasena = evento.valor, error = null) }
            LoginEvento.BotonLoginPresionado -> iniciarSesionLocal()
            LoginEvento.BotonGooglePresionado -> iniciarSesionConGoogle()
            LoginEvento.ConsumirEventoDeNavegacion -> _estadoUi.update { it.copy(loginExitoso = false) }
            LoginEvento.ConsumirError -> _estadoUi.update { it.copy(error = null) }
        }
    }

    // 4. Las únicas funciones que quedan son las relacionadas con el inicio de sesión.
    private fun iniciarSesionConGoogle() {
        viewModelScope.launch {
            _estadoUi.update { it.copy(estaCargando = true) }
            when (val resultadoSignIn = googleAuthCliente.iniciarSesion()) {
                is SignInResult.Success -> {
                    val datosUsuario = resultadoSignIn.data
                    if (datosUsuario.correo == null) {
                        _estadoUi.update { it.copy(error = "No se pudo obtener el correo de Google.", estaCargando = false) }
                        return@launch
                    }
                    repositorio.buscarOCrearUsuarioGoogle(
                        correo = datosUsuario.correo,
                        nombreUsuario = datosUsuario.nombreUsuario ?: "Usuario"
                    ).onSuccess {
                        _estadoUi.update { it.copy(estaCargando = false, loginExitoso = true) }
                    }.onFailure {
                        _estadoUi.update { it.copy(estaCargando = false, error = "No se pudo guardar el usuario.") }
                    }
                }
                is SignInResult.Error -> _estadoUi.update { it.copy(error = resultadoSignIn.message, estaCargando = false) }
                is SignInResult.Cancelled -> _estadoUi.update { it.copy(estaCargando = false) }
            }
        }
    }

    private fun iniciarSesionLocal() {
        val estadoActual = _estadoUi.value
        if (estadoActual.correoOUsuario.isBlank() || estadoActual.contrasena.isBlank()) {
            _estadoUi.update { it.copy(error = "El correo y la contraseña son obligatorios.") }
            return
        }
        _estadoUi.update { it.copy(estaCargando = true) }
        viewModelScope.launch {
            repositorio.iniciarSesionLocal(
                identificador = estadoActual.correoOUsuario,
                contrasena = estadoActual.contrasena
            ).onSuccess {
                _estadoUi.update { it.copy(estaCargando = false, loginExitoso = true) }
            }.onFailure { error ->
                _estadoUi.update { it.copy(estaCargando = false, error = error.message) }
            }
        }
    }
}