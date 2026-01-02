package com.example.freeplayerm.ui.features.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.com.example.freeplayerm.ui.features.auth.components.BotonGooglePulsante
import com.example.freeplayerm.com.example.freeplayerm.ui.features.auth.components.EncabezadoLogoAnimado
import com.example.freeplayerm.com.example.freeplayerm.ui.features.auth.components.TextoIniciarSesion
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.ui.features.auth.components.BotonAnimado
import com.example.freeplayerm.ui.features.auth.components.CampoEntradaLogin
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.features.nav.Routes
import kotlinx.coroutines.launch

// ==========================================
// 1. EL CEREBRO (Solo Lógica)
// ==========================================
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthUiClient = remember {
        GoogleAuthUiClient(contexto, CredentialManager.create(contexto))
    }

    // --- EFECTOS SECUNDARIOS ---

    // 1. Navegación al éxito
    LaunchedEffect(estado.usuarioIdExitoso) {
        estado.usuarioIdExitoso?.let { id ->
            navController.navigate(Routes.Biblioteca.crearRuta(id)) {
                popUpTo(Routes.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(LoginEvento.ConsumirEventoDeNavegacion)
        }
    }

    // 2. Manejo de Errores
    LaunchedEffect(estado.error) {
        estado.error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(LoginEvento.ConsumirError)
        }
    }

    // --- DELEGACIÓN A LA UI ---
    // Aquí es donde el cerebro le pasa los datos al cuerpo.
    // NO dibujamos Scaffolds ni columnas aquí.
    ContenidoPantallaLogin(
        estado = estado,
        onEvento = viewModel::enEvento,
        onNavegarRegistro = { navController.navigate(Routes.Registro.ruta) },
        onNavegarRecuperar = { navController.navigate(Routes.RecuperarClave.ruta) },
        onLoginGoogle = {
            scope.launch {
                val result = googleAuthUiClient.iniciarSesion()
                viewModel.enEvento(LoginEvento.InicioSesionGoogleCompletado(result))
            }
        },
    )
}

// ==========================================
// 2. EL CUERPO (Solo UI Visual)
// ==========================================
@Composable
fun ContenidoPantallaLogin(
    estado: LoginEstado,
    onEvento: (LoginEvento) -> Unit,
    onNavegarRegistro: () -> Unit,
    onNavegarRecuperar: () -> Unit,
    onLoginGoogle: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.ime, // Manejo teclado
    ) { padding ->

        // 1. FONDO (Capa Trasera)
        GalaxyBackground()

        // 2. CONTENIDO (Capa Delantera)
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    // Usamos verticalScroll para evitar crash en pantallas chicas cuando sale el
                    // teclado
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Usamos Arrangement en lugar de Spacer(Weight) porque weight no funciona bien dentro
            // de Scroll
            verticalArrangement = Arrangement.Center,
        ) {

            // --- HEADER ---
            // Un poco de espacio arriba para que no quede pegado si giras la pantalla
            Spacer(Modifier.height(20.dp))

            EncabezadoLogoAnimado()

            Spacer(Modifier.height(30.dp))

            // --- FORMULARIO ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextoIniciarSesion()

                Spacer(Modifier.height(10.dp))

                // Input Usuario
                CampoEntradaLogin(
                    valor = estado.correoOUsuario,
                    alCambiarValor = { onEvento(LoginEvento.CorreoOUsuarioCambiado(it)) },
                    etiqueta = "Usuario o Correo",
                    icono = Icons.Default.Person,
                    esError = estado.error != null && estado.correoOUsuario.isBlank(),
                    cargando = estado.cargandoLocalmente,
                )

                // Input Contraseña
                CampoEntradaLogin(
                    valor = estado.contrasena,
                    alCambiarValor = { onEvento(LoginEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    icono = Icons.Default.Lock,
                    esPassword = true,
                    esError = estado.error != null && estado.contrasena.isBlank(),
                    cargando = estado.cargandoLocalmente,
                )

                // --- ACCIONES (Crear / Recuperar) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onNavegarRegistro,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Text("Crear Cuenta", color = Color(0xFFE040FB), fontSize = 14.sp)
                    }

                    TextButton(
                        onClick = onNavegarRecuperar,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Text("¿Olvidaste contraseña?", color = Color(0xFFE040FB), fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // BOTÓN INGRESAR
                BotonAnimado(
                    onClick = { onEvento(LoginEvento.BotonLoginPresionado) },
                    enabled = !estado.cargandoLocalmente && !estado.cargandoConGoogle,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (estado.cargandoLocalmente) {
                        // El texto cambia sutilmente mientras los inputs giran
                        Text("VERIFICANDO...", fontSize = 14.sp, letterSpacing = 1.sp)
                    } else {
                        Text("INGRESAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Divisor
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    color = Color.White.copy(alpha = 0.2f),
                )

                // BOTÓN GOOGLE
                BotonGooglePulsante(onClick = onLoginGoogle, cargando = estado.cargandoConGoogle)
            }

            // Espacio final para asegurar margen inferior
            Spacer(Modifier.height(50.dp))
        }
    }
}

// ==========================================
// 3. PREVIEWS (Escenarios Login Correctos)
// ==========================================

// --- ESCENARIO 1: Login Inicial (Vacío) ---
@Preview(
    name = "1. Login - Inicio",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun PreviewLoginInitial() {
    // Estado inicial limpio
    val estadoInicial =
        LoginEstado(
            correoOUsuario = "",
            contrasena = "",
            cargandoLocalmente = false,
            cargandoConGoogle = false,
            error = null,
        )

    ContenidoPantallaLogin(
        estado = estadoInicial,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {},
    )
}

// --- ESCENARIO 2: Usuario Escribiendo ---
@Preview(
    name = "2. Login - Con Datos",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewLoginFilled() {
    val estadoLleno =
        LoginEstado(correoOUsuario = "usuario@freeplayer.com", contrasena = "password123")

    ContenidoPantallaLogin(
        estado = estadoLleno,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {},
    )
}

// --- ESCENARIO 3: Cargando (Verificación Local) ---
// Valida que el botón diga "VERIFICANDO..." y los inputs se transformen
@Preview(
    name = "3. Login - Cargando Local",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewLoginLoadingLocal() {
    val estadoCargando =
        LoginEstado(
            correoOUsuario = "test@user.com",
            cargandoLocalmente = true, // <-- Activa el spinner en inputs y texto botón
        )

    ContenidoPantallaLogin(
        estado = estadoCargando,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {},
    )
}

// --- ESCENARIO 4: Cargando (Google) ---
// Valida que el botón de Google muestre el spinner
@Preview(
    name = "4. Login - Cargando Google",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewLoginLoadingGoogle() {
    val estadoGoogle =
        LoginEstado(
            cargandoConGoogle = true // <-- Activa spinner en botón Google
        )

    ContenidoPantallaLogin(
        estado = estadoGoogle,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {},
    )
}

// --- ESCENARIO 5: Error de Credenciales ---
@Preview(
    name = "5. Login - Error",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewLoginError() {
    val estadoError =
        LoginEstado(
            correoOUsuario = "usuario_erroneo",
            contrasena = "123",
            error = "Credenciales incorrectas", // <-- Debe activar bordes rojos
        )

    ContenidoPantallaLogin(
        estado = estadoError,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {},
    )
}
