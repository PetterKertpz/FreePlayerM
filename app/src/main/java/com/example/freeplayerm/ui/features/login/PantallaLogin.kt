package com.example.freeplayerm.ui.features.login

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
import com.example.freeplayerm.ui.features.login.components.*
import com.example.freeplayerm.ui.features.nav.Rutas
import kotlinx.coroutines.launch
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import androidx.credentials.CredentialManager
// Asegúrate de importar tus componentes:
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.BotonGooglePulsante
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.EncabezadoLogoAnimado
import com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components.TextoTituloFlotante

// ==========================================
// 1. EL CEREBRO (Lógica + ViewModel)
// Este NO se previsualiza, solo se usa en la App
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

    // Efecto de Navegación
    LaunchedEffect(estado.usuarioIdExitoso) {
        estado.usuarioIdExitoso?.let { id ->
            navController.navigate(Rutas.Biblioteca.crearRuta(id)) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(LoginEvento.ConsumirEventoDeNavegacion)
        }
    }

    // Efecto de Error
    LaunchedEffect(estado.error) {
        estado.error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(LoginEvento.ConsumirError)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        // Esto evita que el teclado tape los inputs, empujando el contenido suavemente
        contentWindowInsets = WindowInsets.ime
    ) { padding ->

        // 1. FONDO (Capa Trasera)
        FondoGalaxiaAnimado()

        // 2. CONTENIDO (Capa Delantera)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp), // Margen lateral de seguridad
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // ZONA SUPERIOR (Logo pegado arriba)
            // ==========================================

            // Margen inicial para separarlo del borde superior de la pantalla
            Spacer(Modifier.height(60.dp))

            EncabezadoLogoAnimado()

            // >>> RESORTE 1: Empuja lo que sigue hacia el centro <<<
            Spacer(Modifier.weight(1f))


            // ==========================================
            // ZONA CENTRAL (Formulario Flotante)
            // ==========================================

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio limpio entre elementos
            ) {
                TextoTituloFlotante()

                Spacer(Modifier.height(10.dp))

                // INPUT USUARIO (Con transformación a círculo de carga)
                CampoEntradaLogin(
                    valor = estado.correoOUsuario,
                    alCambiarValor = { viewModel.enEvento(LoginEvento.CorreoOUsuarioCambiado(it)) },
                    etiqueta = "Usuario o Correo",
                    icono = Icons.Default.Person,
                    esError = estado.error != null && estado.correoOUsuario.isBlank(),
                    cargando = estado.cargandoLocalmente // <-- ACTIVA LA ANIMACIÓN CIRCULAR
                )

                // INPUT CONTRASEÑA (Con transformación a círculo de carga)
                CampoEntradaLogin(
                    valor = estado.contrasena,
                    alCambiarValor = { viewModel.enEvento(LoginEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    icono = Icons.Default.Lock,
                    esPassword = true,
                    esError = estado.error != null && estado.contrasena.isBlank(),
                    cargando = estado.cargandoLocalmente // <-- ACTIVA LA ANIMACIÓN CIRCULAR
                )

                // --- FILA DE ACCIONES (Registro + Recuperar) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Uno a cada extremo
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // IZQUIERDA: Registro
                    TextButton(
                        onClick = { navController.navigate(Rutas.Registro.ruta) },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Crear Cuenta",
                            color = Color(0xFFE040FB),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // DERECHA: Olvidé contraseña
                    TextButton(
                        onClick = { navController.navigate(Rutas.RecuperarClave.ruta) },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "¿Olvidaste contraseña?",
                            color = Color(0xFFE040FB), // Color Neón para destacar
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // BOTÓN INGRESAR
                BotonAnimado(
                    onClick = { viewModel.enEvento(LoginEvento.BotonLoginPresionado) },
                    enabled = !estado.cargandoLocalmente && !estado.cargandoConGoogle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.cargandoLocalmente) {
                        // El indicador visual ya está en los inputs, aquí ponemos un texto sutil
                        Text("VERIFICANDO...", fontSize = 14.sp, letterSpacing = 1.sp)
                    } else {
                        Text("INGRESAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // Divisor Visual
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    color = Color.White.copy(alpha = 0.2f)
                )

                // BOTÓN GOOGLE
                BotonGooglePulsante(
                    onClick = {
                        scope.launch {
                            val result = googleAuthUiClient.iniciarSesion()
                            viewModel.enEvento(LoginEvento.InicioSesionGoogleCompletado(result))
                        }
                    },
                    cargando = estado.cargandoConGoogle
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ==========================================
// 2. EL CUERPO (Solo UI Visual)
// ESTE ES EL QUE PREVISUALIZAMOS
// ==========================================
@Composable
fun ContenidoPantallaLogin(
    estado: LoginEstado,
    onEvento: (LoginEvento) -> Unit,
    onNavegarRegistro: () -> Unit,
    onNavegarRecuperar: () -> Unit,
    onLoginGoogle: () -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { padding ->
        FondoGalaxiaAnimado()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Scroll para pantallas chicas
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EncabezadoLogoAnimado()
            Spacer(Modifier.height(40.dp))
            TextoTituloFlotante()
            Spacer(Modifier.height(24.dp))

            // Inputs
            CampoEntradaLogin(
                valor = estado.correoOUsuario,
                alCambiarValor = { onEvento(LoginEvento.CorreoOUsuarioCambiado(it)) },
                etiqueta = "Usuario o Correo",
                icono = Icons.Default.Person,
                esError = estado.error != null && estado.correoOUsuario.isBlank()
            )

            Spacer(Modifier.height(16.dp))

            CampoEntradaLogin(
                valor = estado.contrasena,
                alCambiarValor = { onEvento(LoginEvento.ContrasenaCambiada(it)) },
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                esPassword = true,
                esError = estado.error != null && estado.contrasena.isBlank()
            )

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavegarRecuperar) {
                    Text("¿Olvidaste tu contraseña?", color = Color(0xFFE040FB))
                }
            }

            Spacer(Modifier.height(24.dp))

            BotonAnimado(
                onClick = { onEvento(LoginEvento.BotonLoginPresionado) },
                enabled = !estado.cargandoLocalmente && !estado.cargandoConGoogle,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargandoLocalmente) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("INGRESAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            BotonGooglePulsante(
                onClick = onLoginGoogle,
                cargando = estado.cargandoConGoogle
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", color = Color.White)
                TextButton(onClick = onNavegarRegistro) {
                    Text("Regístrate", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 3. LAS PREVIEWS (Ahora sí funcionan)
// ==========================================

@Preview(name = "Pantalla Login Completa", showSystemUi = true)
@Composable
fun PreviewLoginFinal() {
    // Creamos un estado falso para ver el diseño
    val estadoEjemplo = LoginEstado(
        correoOUsuario = "usuario@test.com",
        contrasena = "",
        cargandoLocalmente = false,
        cargandoConGoogle = false,
        error = null,
        usuarioIdExitoso = null
    )

    // Previsualizamos el "Cuerpo" (Contenido), no el "Cerebro"
    ContenidoPantallaLogin(
        estado = estadoEjemplo,
        onEvento = {},
        onNavegarRegistro = {},
        onNavegarRecuperar = {},
        onLoginGoogle = {}
    )
}