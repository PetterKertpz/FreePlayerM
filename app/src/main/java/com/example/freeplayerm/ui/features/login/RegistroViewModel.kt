package com.example.freeplayerm.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 1. Estado específico y limpio para el registro
data class RegistroEstado(
    val nombreUsuario: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

// 2. Eventos que solo tienen sentido en la pantalla de registro
sealed class RegistroEvento {
    data class NombreUsuarioCambiado(val valor: String) : RegistroEvento()
    data class CorreoCambiado(val valor: String) : RegistroEvento()
    data class ContrasenaCambiada(val valor: String) : RegistroEvento()
    object BotonRegistroPresionado : RegistroEvento()
    object ConsumirEventoDeNavegacion : RegistroEvento()
    object ConsumirError : RegistroEvento() // Evento para limpiar el error
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorio: UsuarioRepository
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RegistroEstado())
    val estadoUi = _estadoUi.asStateFlow()

    // 3. La lógica de 'enEvento' es ahora más simple y enfocada
    fun enEvento(evento: RegistroEvento) {
        when (evento) {
            is RegistroEvento.NombreUsuarioCambiado -> _estadoUi.update { it.copy(nombreUsuario = evento.valor, error = null) }
            is RegistroEvento.CorreoCambiado -> _estadoUi.update { it.copy(correo = evento.valor, error = null) }
            is RegistroEvento.ContrasenaCambiada -> _estadoUi.update { it.copy(contrasena = evento.valor, error = null) }
            RegistroEvento.BotonRegistroPresionado -> registrarseLocalmente()
            RegistroEvento.ConsumirEventoDeNavegacion -> _estadoUi.update { it.copy(registroExitoso = false) }
            RegistroEvento.ConsumirError -> _estadoUi.update { it.copy(error = null) }
        }
    }

    // 4. Hemos movido esta función desde LoginViewModel aquí, donde pertenece
    private fun registrarseLocalmente() {
        val estadoActual = _estadoUi.value

        if (estadoActual.nombreUsuario.isBlank() || estadoActual.correo.isBlank() || estadoActual.contrasena.isBlank()) {
            _estadoUi.update { it.copy(error = "Todos los campos son obligatorios.") }
            return
        }

        _estadoUi.update { it.copy(estaCargando = true) }

        viewModelScope.launch(Dispatchers.IO) {
            repositorio.registrarUsuarioLocal(
                nombreUsuario = estadoActual.nombreUsuario,
                correo = estadoActual.correo,
                contrasena = estadoActual.contrasena
            ).onSuccess {
                withContext(Dispatchers.Main) {
                    _estadoUi.update { it.copy(estaCargando = false, registroExitoso = true) }
                }
            }.onFailure { error ->
                withContext(Dispatchers.Main) {
                    _estadoUi.update { it.copy(estaCargando = false, error = error.message) }
                }
            }
        }
    }
}