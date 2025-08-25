package com.example.freeplayerm.ui.features.login

// ... (asegúrate de que todos los imports necesarios estén aquí)
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Esta es la función principal y "real" de nuestra pantalla.
 * Se conecta al ViewModel para obtener el estado y manejar los eventos.
 * Nota que esta función NO tiene la anotación @Preview.
 */
@Composable
fun PantallaLogin(
    viewModel: LoginViewModel = hiltViewModel() // Hilt nos proporciona automáticamente el ViewModel
) {
    // Obtenemos el estado actual de la UI desde el ViewModel.
    val estado by viewModel.estadoUi.collectAsState()
    val context = LocalContext.current

    // Este bloque maneja los "efectos secundarios", como mostrar un Toast.
    // Se ejecutará cada vez que el valor de registroExitoso cambie.
    LaunchedEffect(key1 = estado.registroExitoso) {
        if (estado.registroExitoso) {
            Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
        }
    }

    // Llamamos a la función que contiene el diseño visual, pasándole el estado y los eventos.
    ContenidoPantallaLogin(
        estado = estado,
        enEvento = viewModel::enEvento // Pasamos una referencia a la función de eventos del ViewModel
    )
}

/**
 * Esta función contiene SOLO el código de la interfaz de usuario.
 * Es "tonta": recibe el estado y no sabe de dónde viene. Esto la hace perfecta para previsualizar.
 */
@Composable
fun ContenidoPantallaLogin(
    estado: LoginEstado,
    enEvento: (LoginEvento) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido a FreePlayer", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = estado.correo,
                onValueChange = { enEvento(LoginEvento.CorreoCambiado(it)) },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                isError = estado.error != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = { enEvento(LoginEvento.ContrasenaCambiada(it)) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = estado.error != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.error != null) {
                Text(
                    text = estado.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { enEvento(LoginEvento.BotonLoginPresionado) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar Sesión")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { enEvento(LoginEvento.BotonRegistroPresionado) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }

        if (estado.estaCargando) {
            CircularProgressIndicator()
        }
    }
}

/**
 * ESTA es la función que Android Studio usará para la previsualización.
 * No tiene parámetros, por lo que es fácil de dibujar para el sistema.
 */
@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaLogin() {
    // Para la vista previa, creamos un estado de ejemplo y se lo pasamos
    // a nuestro Composable de la UI.
    ContenidoPantallaLogin(
        estado = LoginEstado(correo = "ejemplo@correo.com", contrasena = "12345"),
        enEvento = {} // Para la vista previa, los eventos no necesitan hacer nada.
    )
}

@Preview(showBackground = true, name = "Estado de Error")
@Composable
fun VistaPreviaEstadoError() {
    // Podemos crear múltiples vistas previas para probar diferentes estados.
    ContenidoPantallaLogin(
        estado = LoginEstado(error = "El correo ya está en uso."),
        enEvento = {}
    )
}