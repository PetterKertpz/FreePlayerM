package com.example.freeplayerm.ui.features.inicio.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroEstado(
    val nombreUsuario: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "", // <-- NUEVO CAMPO
    val error: String? = null,
    val estaCargando: Boolean = false,
    val usuarioIdExitoso: Int? = null
)

sealed class RegistroEvento {
    data class NombreUsuarioCambiado(val valor: String) : RegistroEvento()
    data class CorreoCambiado(val valor: String) : RegistroEvento()
    data class ContrasenaCambiada(val valor: String) : RegistroEvento()
    data class ConfirmarContrasenaCambiada(val valor: String) : RegistroEvento() // <-- NUEVO EVENTO
    object BotonRegistroPresionado : RegistroEvento()
    object ConsumirEventoDeNavegacion : RegistroEvento()
    object ConsumirError : RegistroEvento()
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorio: UsuarioRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RegistroEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: RegistroEvento) {
        when (evento) {
            is RegistroEvento.NombreUsuarioCambiado -> _estadoUi.update { it.copy(nombreUsuario = evento.valor, error = null) }
            is RegistroEvento.CorreoCambiado -> _estadoUi.update { it.copy(correo = evento.valor, error = null) }
            is RegistroEvento.ContrasenaCambiada -> _estadoUi.update { it.copy(contrasena = evento.valor, error = null) }
            is RegistroEvento.ConfirmarContrasenaCambiada -> _estadoUi.update { it.copy(confirmarContrasena = evento.valor, error = null) }
            RegistroEvento.BotonRegistroPresionado -> registrarseLocalmente()
            RegistroEvento.ConsumirEventoDeNavegacion -> _estadoUi.update { it.copy(usuarioIdExitoso = null) }
            RegistroEvento.ConsumirError -> _estadoUi.update { it.copy(error = null) }
        }
    }
    fun esContraseniaSegura(contrasenia: String): Boolean {
        if (contrasenia.length < 8) return false
        val tieneMayuscula = contrasenia.any { it.isUpperCase() }
        val tieneMinuscula = contrasenia.any { it.isLowerCase() }
        val tieneNumero = contrasenia.any { it.isDigit() }
        return tieneMayuscula && tieneMinuscula && tieneNumero
    }
    fun esCorreoValido(correo: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    private fun registrarseLocalmente() {
        val estado = _estadoUi.value

        // 1. Validar campos vacíos
        if (estado.nombreUsuario.isBlank() || estado.correo.isBlank() || estado.contrasena.isBlank()) {
            _estadoUi.update { it.copy(error = "Todos los campos son obligatorios.") }
            return
        }

        // 2. Validar coincidencia de contraseñas
        if (estado.contrasena != estado.confirmarContrasena) {
            _estadoUi.update { it.copy(error = "Las contraseñas no coinciden.") }
            return
        }

        // 3. Iniciar carga
        _estadoUi.update { it.copy(estaCargando = true) }

        viewModelScope.launch {
            repositorio.registrarUsuarioLocal(
                nombreUsuario = estado.nombreUsuario,
                correo = estado.correo,
                contrasena = estado.contrasena
            ).onSuccess { usuarioCreado ->
                sessionRepository.guardarIdDeUsuario(usuarioCreado.idUsuario)
                _estadoUi.update { it.copy(estaCargando = false, usuarioIdExitoso = usuarioCreado.idUsuario) }
            }.onFailure { error ->
                _estadoUi.update { it.copy(estaCargando = false, error = error.message ?: "Error al registrar") }
            }
        }
    }
}