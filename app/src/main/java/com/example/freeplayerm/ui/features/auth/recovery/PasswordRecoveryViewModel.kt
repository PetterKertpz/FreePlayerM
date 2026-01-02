package com.example.freeplayerm.ui.features.auth.recovery

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecuperarClaveEstado(
    val correo: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false,
)

sealed class RecuperarClaveEvento {
    data class CorreoCambiado(val valor: String) : RecuperarClaveEvento()

    object BotonEnviarCorreoPresionado : RecuperarClaveEvento()

    object ConsumirMensajes : RecuperarClaveEvento() // Limpia errores o éxitos para no repetirlos
}

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(private val repositorio: UserRepository) :
    ViewModel() {

    private val _estadoUi = MutableStateFlow(RecuperarClaveEstado())
    val estadoUi = _estadoUi.asStateFlow()

    fun enEvento(evento: RecuperarClaveEvento) {
        when (evento) {
            is RecuperarClaveEvento.CorreoCambiado -> {
                _estadoUi.update { it.copy(correo = evento.valor, error = null) }
            }
            RecuperarClaveEvento.BotonEnviarCorreoPresionado -> enviarCorreo()
            RecuperarClaveEvento.ConsumirMensajes -> {
                _estadoUi.update { it.copy(error = null, exito = false) }
            }
        }
    }

    private fun enviarCorreo() {
        val correo = _estadoUi.value.correo

        // Validación
        if (correo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _estadoUi.update { it.copy(error = "Por favor, introduce un correo válido.") }
            return
        }

        _estadoUi.update { it.copy(estaCargando = true) }

        viewModelScope.launch {
            repositorio
                .enviarCorreoRecuperacion(correo)
                .onSuccess { _estadoUi.update { it.copy(estaCargando = false, exito = true) } }
                .onFailure { error ->
                    _estadoUi.update {
                        it.copy(
                            estaCargando = false,
                            error = error.message ?: "Error al enviar correo.",
                        )
                    }
                }
        }
    }
}
