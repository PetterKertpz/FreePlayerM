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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositorio: UsuarioRepository,
    private val googleAuthCliente: GoogleAuthUiClient // ✅ Inyectamos nuestro "ayudante"
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(LoginEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: LoginEvento) {
        when (evento) {
            is LoginEvento.CorreoOUsuarioCambiado -> {
                _estadoUi.update { it.copy(correoOUsuario = evento.valor, error = null) }
            }
            is LoginEvento.ContrasenaCambiada -> {
                _estadoUi.update { it.copy(contrasena = evento.valor, error = null) }
            }
            is LoginEvento.NombreUsuarioCambiado -> {
                _estadoUi.update { it.copy(nombreUsuario = evento.valor, error = null) }
            }
            LoginEvento.BotonLoginPresionado -> {
                // Lógica de inicio de sesión local...
            }
            LoginEvento.BotonRegistroPresionado -> {
                // Lógica de registro local...
            }
            LoginEvento.BotonGooglePresionado -> {
                iniciarSesionConGoogle()
            }
            LoginEvento.ConsumirEventoDeNavegacion -> {
                _estadoUi.update { it.copy(registroExitoso = false, loginExitoso = false, error = null) }
            }
        }
    }
    private fun iniciarSesionConGoogle() {
        viewModelScope.launch {
            _estadoUi.update { it.copy(estaCargando = true) }
            val resultadoSignIn = googleAuthCliente.iniciarSesion()

            when (resultadoSignIn) {
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
                        _estadoUi.update { it.copy(estaCargando = false, error = "No se pudo guardar el usuario en la base de datos.") }
                    }
                }
                is SignInResult.Error -> {
                    _estadoUi.update { it.copy(error = resultadoSignIn.message, estaCargando = false) }
                }
                is SignInResult.Cancelled -> {
                    _estadoUi.update { it.copy(estaCargando = false) }
                }
            }
        }
    }
}

// Clases de estado y evento
data class LoginEstado(
    val nombreUsuario: String = "",
    val correoOUsuario: String = "",
    val contrasena: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false,
    val loginExitoso: Boolean = false
)

sealed class LoginEvento {
    data class NombreUsuarioCambiado(val valor: String) : LoginEvento()
    data class CorreoOUsuarioCambiado(val valor: String) : LoginEvento()
    data class ContrasenaCambiada(val valor: String) : LoginEvento()
    object BotonLoginPresionado : LoginEvento()
    object BotonRegistroPresionado : LoginEvento()
    object BotonGooglePresionado : LoginEvento()
    object ConsumirEventoDeNavegacion : LoginEvento()
}