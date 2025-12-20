package com.example.freeplayerm.ui.features.inicio.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import androidx.credentials.CredentialManager
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.BotonGooglePulsante
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.EncabezadoLogoAnimado
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.TextoTituloFlotante
import com.example.freeplayerm.ui.features.inicio.components.BotonAnimado
import com.example.freeplayerm.ui.features.inicio.components.CampoEntradaLogin
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado
import com.example.freeplayerm.ui.features.nav.Rutas
import kotlinx.coroutines.launch


// ==========================================
// 1. EL CEREBRO (Solo Lógica)
// ==========================================
@Composable
fun PantallaLogin(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
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
            navController.navigate(Rutas.Biblioteca.crearRuta(id)) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
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
        onNavegarRegistro = { navController.navigate(Rutas.Registro.ruta) },
        onNavegarRecuperar = { navController.navigate(Rutas.RecuperarClave.ruta) },
        onLoginGoogle = {
            scope.launch {
                val result = googleAuthUiClient.iniciarSesion()
                viewModel.enEvento(LoginEvento.InicioSesionGoogleCompletado(result))
            }
        }
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
    onLoginGoogle: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.ime // Manejo teclado
    ) { padding ->

        // 1. FONDO (Capa Trasera)
        FondoGalaxiaAnimado()

        // 2. CONTENIDO (Capa Delantera)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                // Usamos verticalScroll para evitar crash en pantallas chicas cuando sale el teclado
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Usamos Arrangement en lugar de Spacer(Weight) porque weight no funciona bien dentro de Scroll
            verticalArrangement = Arrangement.Center
        ) {

            // --- HEADER ---
            // Un poco de espacio arriba para que no quede pegado si giras la pantalla
            Spacer(Modifier.height(40.dp))

            EncabezadoLogoAnimado()

            Spacer(Modifier.height(30.dp))

            // --- FORMULARIO ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextoTituloFlotante()

                Spacer(Modifier.height(10.dp))

                // Input Usuario
                CampoEntradaLogin(
                    valor = estado.correoOUsuario,
                    alCambiarValor = { onEvento(LoginEvento.CorreoOUsuarioCambiado(it)) },
                    etiqueta = "Usuario o Correo",
                    icono = Icons.Default.Person,
                    esError = estado.error != null && estado.correoOUsuario.isBlank(),
                    cargando = estado.cargandoLocalmente
                )

                // Input Contraseña
                CampoEntradaLogin(
                    valor = estado.contrasena,
                    alCambiarValor = { onEvento(LoginEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    icono = Icons.Default.Lock,
                    esPassword = true,
                    esError = estado.error != null && estado.contrasena.isBlank(),
                    cargando = estado.cargandoLocalmente
                )

                // --- ACCIONES (Crear / Recuperar) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onNavegarRegistro,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Crear Cuenta", color = Color(0xFFE040FB), fontSize = 14.sp)
                    }

                    TextButton(
                        onClick = onNavegarRecuperar,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("¿Olvidaste contraseña?", color = Color(0xFFE040FB), fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // BOTÓN INGRESAR
                BotonAnimado(
                    onClick = { onEvento(LoginEvento.BotonLoginPresionado) },
                    enabled = !estado.cargandoLocalmente && !estado.cargandoConGoogle,
                    modifier = Modifier.fillMaxWidth()
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
                    color = Color.White.copy(alpha = 0.2f)
                )

                // BOTÓN GOOGLE
                BotonGooglePulsante(
                    onClick = onLoginGoogle,
                    cargando = estado.cargandoConGoogle
                )
            }

            // Espacio final para asegurar margen inferior
            Spacer(Modifier.height(50.dp))
        }
    }
}

// ==========================================
// 3. PREVIEWS (Para probar sin ejecutar)
// ==========================================

@Preview(name = "Login - Pixel 7", device = "id:pixel_7", showSystemUi = true)
@Composable
fun PreviewLoginFinal() {
    val estadoDemo = LoginEstado(
        correoOUsuario = "test@freeplayer.com",
        contrasena = "123456"
    )
    ContenidoPantallaLogin(
        estado = estadoDemo,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {}
    )
}

@Preview(name = "Login - Cargando", showBackground = true)
@Composable
fun PreviewLoginCargando() {
    val estadoCargando = LoginEstado(
        correoOUsuario = "test",
        contrasena = "pass",
        cargandoLocalmente = true // Debería mostrar los círculos en inputs
    )
    ContenidoPantallaLogin(
        estado = estadoCargando,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {}
    )
}