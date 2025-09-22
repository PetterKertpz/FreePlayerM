package com.example.freeplayerm.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI
data class RecuperarClaveEstado(
    val correo: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false // Para saber cuándo mostrar el mensaje de éxito
)

// Eventos que la UI puede enviar
sealed class RecuperarClaveEvento {
    data class CorreoCambiado(val valor: String) : RecuperarClaveEvento()
    object BotonEnviarCorreoPresionado : RecuperarClaveEvento()
    object ConsumirMensajes : RecuperarClaveEvento()
}

@HiltViewModel
class RecuperarClaveViewModel @Inject constructor(
    private val repositorio: UsuarioRepository // Hilt inyecta tu repositorio
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RecuperarClaveEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: RecuperarClaveEvento) {
        when (evento) {
            is RecuperarClaveEvento.CorreoCambiado -> _estadoUi.update { it.copy(correo = evento.valor, error = null) }
            RecuperarClaveEvento.BotonEnviarCorreoPresionado -> enviarCorreo()
            RecuperarClaveEvento.ConsumirMensajes -> _estadoUi.update { it.copy(error = null, exito = false) }
        }
    }

    private fun enviarCorreo() {
        val correo = _estadoUi.value.correo
        if (correo.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _estadoUi.update { it.copy(error = "Por favor, introduce un correo válido.") }
            return
        }

        _estadoUi.update { it.copy(estaCargando = true) }
        viewModelScope.launch {
            // Llama a la función que ya tienes en tu repositorio
            repositorio.enviarCorreoRecuperacion(correo)
                .onSuccess {
                    _estadoUi.update { it.copy(estaCargando = false, exito = true) }
                }
                .onFailure { error ->
                    _estadoUi.update { it.copy(estaCargando = false, error = error.message ?: "Ocurrió un error.") }
                }
        }
    }
}