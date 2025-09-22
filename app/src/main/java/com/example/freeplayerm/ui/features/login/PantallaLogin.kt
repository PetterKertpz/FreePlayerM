package com.example.freeplayerm.ui.features.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.ui.features.login.components.BotonIngresarGoogleMejorado
import com.example.freeplayerm.ui.features.login.components.CampoDeTextoAutenticacion
import com.example.freeplayerm.ui.features.login.components.TemaBotonGoogle
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val googleAuthUiClient = remember {
        GoogleAuthUiClient(
            context = contexto,
            credentialManager = CredentialManager.create(contexto)
        )
    }

    LaunchedEffect(key1 = estado.usuarioIdExitoso) {
        if (estado.usuarioIdExitoso != null) {
            navController.navigate(Rutas.Biblioteca.crearRuta(estado.usuarioIdExitoso!!)) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(LoginEvento.ConsumirEventoDeNavegacion)
        }
    }
    LaunchedEffect(key1 = estado.error) {
        estado.error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(LoginEvento.ConsumirError)
        }
    }

    CuerpoPantallaLogin(
        estado = estado,
        enEvento = viewModel::enEvento,
        onNavigateToRegistro = { navController.navigate(Rutas.Registro.ruta) },
        onNavigateToRecuperarClave = {
            Toast.makeText(contexto, "Función no implementada", Toast.LENGTH_SHORT).show()
        },
        // --- LÓGICA CORREGIDA Y SIMPLIFICADA AQUÍ ---
        onBotonGoogleClick = {
            coroutineScope.launch {
                // 1. Llamamos a la única función que maneja todo el flujo
                val signInResult = googleAuthUiClient.iniciarSesion()
                // 2. Enviamos el resultado al ViewModel
                viewModel.enEvento(LoginEvento.InicioSesionGoogleCompletado(signInResult))
            }
        }
    )
}

@Composable
fun CuerpoPantallaLogin(
    estado: LoginEstado,
    enEvento: (LoginEvento) -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToRecuperarClave: () -> Unit,
    onBotonGoogleClick: () -> Unit
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
            // ... (Header se mantiene igual) ...
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FreePlayer",
                    fontSize = 50.sp,
                    fontFamily = FontFamily.Default,
                    style = TextStyle(
                        shadow = Shadow(
                            color = AppColors.RojoFuerte,
                            blurRadius = 20f
                        )
                    ),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PurpuraClaro,
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

                // Este espacio es solo para el botón de login local
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { enEvento(LoginEvento.BotonLoginPresionado) },
                    // El botón se deshabilita si CUALQUIERA de las dos cargas está activa
                    enabled = !estado.cargandoLocalmente && !estado.cargandoConGoogle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PurpuraProfundo,
                        contentColor = AppColors.Blanco,
                        disabledContainerColor = AppColors.GrisProfundo,
                        disabledContentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, AppColors.Negro),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    // --- LÓGICA DE CARGA LOCAL ---
                    if (estado.cargandoLocalmente) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Ingresar",
                            color = AppColors.Blanco,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ... (Textos para registrarse y recuperar clave) ...
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "¿No tienes una cuenta?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onNavigateToRegistro() })
                    Text(
                        "¿Olvidaste tu contraseña?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onNavigateToRecuperarClave() })
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
                // --- LÓGICA DE CARGA DE GOOGLE ---
                BotonIngresarGoogleMejorado(
                    texto = "Acceder con Google",
                    // Pasamos el estado de carga de Google al botón
                    cargando = estado.cargandoConGoogle,
                    tema = TemaBotonGoogle.Oscuro,
                    // El botón se deshabilita si CUALQUIERA de las dos cargas está activa
                    onClick = if (!estado.cargandoLocalmente && !estado.cargandoConGoogle) onBotonGoogleClick else {{}}
                )
            }
        }
    }
}


// (Tus Previews se mantienen igual, solo necesitas actualizarlas para el nuevo estado)
@Preview(name = "Estado Normal", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLoginNormal() {
    FreePlayerMTheme {
        CuerpoPantallaLogin(
            estado = LoginEstado(
                correoOUsuario = "usuario@ejemplo.com",
                contrasena = "123456"
            ),
            enEvento = {},
            onNavigateToRegistro = {},
            onNavigateToRecuperarClave = {},
            onBotonGoogleClick = {}
        )
    }
}

@Preview(name = "Estado Cargando (Local)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLoginCargandoLocalmente() {
    FreePlayerMTheme {
        CuerpoPantallaLogin(
            estado = LoginEstado(
                correoOUsuario = "usuario@ejemplo.com",
                contrasena = "123456",
                cargandoLocalmente = true // <-- Se activa la carga local
            ),
            enEvento = {},
            onNavigateToRegistro = {},
            onNavigateToRecuperarClave = {},
            onBotonGoogleClick = {}
        )
    }
}

@Preview(name = "Estado Cargando (Google)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaLoginCargandoGoogle() {
    FreePlayerMTheme {
        CuerpoPantallaLogin(
            estado = LoginEstado(
                correoOUsuario = "usuario@ejemplo.com",
                contrasena = "123456",
                cargandoConGoogle = true // <-- Se activa la carga de Google
            ),
            enEvento = {},
            onNavigateToRegistro = {},
            onNavigateToRecuperarClave = {},
            onBotonGoogleClick = {}
        )
    }
}