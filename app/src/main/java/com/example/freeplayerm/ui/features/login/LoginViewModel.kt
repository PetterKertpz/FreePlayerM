package com.example.freeplayerm.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @HiltViewModel: Anotación clave que le dice a Hilt que esta clase es un ViewModel
 * y que debe prepararse para inyectar dependencias en ella.
 *
 * @Inject constructor: Le decimos a Hilt que, para construir este ViewModel, debe buscar
 * en sus "recetas" (nuestro ModuloDeAplicacion) cómo construir un UsuarioRepositorio
 * y pasárselo automáticamente al constructor. Esta es la magia de la inyección de dependencias.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
) : ViewModel() {

    // _estadoUi: Es un StateFlow mutable y privado. Contiene el estado actual de la pantalla.
    // Solo el ViewModel puede modificarlo.
    private val _estadoUi = MutableStateFlow(LoginEstado())

    // estadoUi: Es la versión pública e inmutable del StateFlow. La UI "escuchará" los cambios
    // en este objeto para redibujarse, pero no podrá modificarlo directamente.
    val estadoUi = _estadoUi.asStateFlow()

    /**
     * Esta es la única función pública que la UI llamará. Centraliza todos los eventos
     * del usuario en un solo lugar, siguiendo un patrón de diseño moderno (MVI).
     */
    fun enEvento(evento: LoginEvento) {
        when (evento) {
            is LoginEvento.CorreoCambiado -> {
                // Actualizamos el estado con el nuevo valor del correo
                _estadoUi.value = _estadoUi.value.copy(correo = evento.valor)
            }
            is LoginEvento.ContrasenaCambiada -> {
                // Actualizamos el estado con la nueva contraseña
                _estadoUi.value = _estadoUi.value.copy(contrasena = evento.valor)
            }
            LoginEvento.BotonLoginPresionado -> {
                // Aquí iría la lógica para iniciar sesión
                iniciarSesion()
            }
            LoginEvento.BotonRegistroPresionado -> {
                // Aquí iría la lógica para registrarse
                registrarse()
            }
            LoginEvento.BotonGooglePresionado -> {
                // Lógica para iniciar sesión con Google (lo veremos más adelante)
            }
        }
    }

    private fun iniciarSesion() {
        // La lógica de negocio se ejecuta en una coroutine para no bloquear la UI
        viewModelScope.launch {
            // Lógica de inicio de sesión... (la implementaremos en el siguiente paso)
        }
    }

    private fun registrarse() {
        // La lógica de negocio se ejecuta en una coroutine
        viewModelScope.launch {
            // Lógica de registro... (la implementaremos en el siguiente paso)
        }
    }
}

/**
 * Representa el estado de la UI en un momento dado. Es una única clase que contiene
 * toda la información que la pantalla necesita para dibujarse.
 */
data class LoginEstado(
    val correo: String = "",
    val contrasena: String = "",
    val estaCargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false, // <-- AÑADE ESTA LÍNEA
    val loginExitoso: Boolean = false
)

/**
 * Representa todas las acciones que el usuario puede realizar en la pantalla.
 */
sealed class LoginEvento {
    data class CorreoCambiado(val valor: String) : LoginEvento()
    data class ContrasenaCambiada(val valor: String) : LoginEvento()
    object BotonLoginPresionado : LoginEvento()
    object BotonRegistroPresionado : LoginEvento()
    object BotonGooglePresionado : LoginEvento()
}