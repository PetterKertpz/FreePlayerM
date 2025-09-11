package com.example.freeplayerm.ui.features.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.ui.features.login.components.BotonIngresarGoogleMejorado
import com.example.freeplayerm.ui.features.login.components.CampoDeTextoAutenticacion
import com.example.freeplayerm.ui.features.login.components.TemaBotonGoogle
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * =================================================================
 * 1. El "Composable Inteligente" (Smart Composable)
 * =================================================================
 * Se encarga de la lógica: obtener el ViewModel, manejar los
 * efectos secundarios (Toasts, navegación) y pasar el estado y
 * los eventos a la UI.
 */
@Composable
fun PantallaLogin(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Efecto para manejar la navegación tras un login exitoso
    LaunchedEffect(key1 = estado.loginExitoso) {
        if (estado.loginExitoso) {
            Toast.makeText(contexto, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
            navController.navigate(Rutas.Biblioteca.ruta) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(LoginEvento.ConsumirEventoDeNavegacion)
        }
    }

    // Efecto para mostrar errores en un Toast
    LaunchedEffect(key1 = estado.error) {
        estado.error?.let { mensajeError ->
            Toast.makeText(contexto, mensajeError, Toast.LENGTH_LONG).show()
            // Aquí también podrías consumir el error en el ViewModel si es necesario
            viewModel.enEvento(LoginEvento.ConsumirError)
        }
    }

    // Llamamos al Composable de la UI
    CuerpoPantallaLogin(
        estado = estado,
        enEvento = viewModel::enEvento,
        onNavigateToRegistro = { navController.navigate(Rutas.Registro.ruta) },
        onNavigateToRecuperarClave = {
            // Aquí iría la navegación a la pantalla de recuperar clave cuando la crees
            Toast.makeText(contexto, "Función no implementada", Toast.LENGTH_SHORT).show()
        })
}


/**
 * =================================================================
 * 2. El "Composable Tonto" (Dumb Composable)
 * =================================================================
 * Solo recibe el estado y lambdas para notificar eventos.
 * No tiene idea de dónde vienen los datos ni qué hacen los eventos.
 * Es totalmente previsualizable y reutilizable.
 */
@Composable
fun CuerpoPantallaLogin(
    estado: LoginEstado,
    enEvento: (LoginEvento) -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToRecuperarClave: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // 🔹 HEADER
            Row(
                horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FreePlayer",
                    fontSize = 50.sp,
                    fontFamily = FontFamily.Default,
                    style = TextStyle(
                        shadow = Shadow(
                            color = AppColors.PurpuraOscuro, blurRadius = 20f
                        )
                    ),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PurpuraProfundo,
                )
            }

            // 🔹 CONTENIDO CENTRAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
            ) {
                Text(
                    "Iniciar Sesión",
                    fontSize = 35.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
                CampoDeTextoAutenticacion(
                    valor = estado.correoOUsuario,
                    enCambioDeValor = { enEvento(LoginEvento.CorreoOUsuarioCambiado(it)) },
                    etiqueta = "Nombre de usuario o Correo Electrónico"
                )
                CampoDeTextoAutenticacion(
                    valor = estado.contrasena,
                    enCambioDeValor = { enEvento(LoginEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    esCampoDeContrasena = true
                )
                if (estado.estaCargando) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "¿No tienes una cuenta?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { onNavigateToRegistro() })
                    Text(
                        "¿Olvidaste tu contraseña?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { onNavigateToRecuperarClave() })
                }

                Button(
                    onClick = { enEvento(LoginEvento.BotonLoginPresionado) },
                    enabled = !estado.estaCargando,
                    colors = ButtonColors(
                        containerColor = AppColors.PurpuraProfundo,
                        contentColor = AppColors.Negro,
                        disabledContainerColor = AppColors.GrisProfundo,
                        disabledContentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, AppColors.Negro),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        "Ingresar",
                        color = AppColors.Blanco,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 🔹 FOOTER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
                    .background(Color.Transparent)
            ) {
                BotonIngresarGoogleMejorado(
                    texto = "Acceder con Google",
                    cargando = estado.estaCargando,
                    tema = TemaBotonGoogle.Oscuro,
                    onClick = { enEvento(LoginEvento.BotonGooglePresionado) })
            }
        }
    }
}


/**
 * =================================================================
 * 3. Previsualizaciones que ahora funcionan perfectamente
 * =================================================================
 * Ahora podemos crear previews para diferentes escenarios de la UI
 * sin depender de Hilt ni de NavController.
 */
@Preview(name = "Estado Normal", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLoginNormal() {
    FreePlayerMTheme {
        CuerpoPantallaLogin(
            estado = LoginEstado(
            correoOUsuario = "usuario@ejemplo.com", contrasena = "123456", estaCargando = false
        ), enEvento = {}, onNavigateToRegistro = {}, onNavigateToRecuperarClave = {})
    }
}

@Preview(name = "Estado Cargando", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLoginCargando() {
    FreePlayerMTheme {
        CuerpoPantallaLogin(
            estado = LoginEstado(
            correoOUsuario = "usuario@ejemplo.com", contrasena = "123456", estaCargando = true
        ), enEvento = {}, onNavigateToRegistro = {}, onNavigateToRecuperarClave = {})
    }
}