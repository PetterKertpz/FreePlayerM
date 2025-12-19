package com.example.freeplayerm.ui.features.inicio.recuperarcontrasena

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.ui.features.inicio.components.BotonAnimado
import com.example.freeplayerm.ui.features.inicio.components.CampoEntradaLogin
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado
// Importamos TUS componentes

// ==========================================
// 1. EL CEREBRO (Lógica + ViewModel)
// ==========================================
@Composable
fun PantallaRecuperarClave(
    navController: NavController,
    viewModel: RecuperarClaveViewModel = hiltViewModel()
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Manejo de Toasts de Error
    LaunchedEffect(estado.error) {
        estado.error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(RecuperarClaveEvento.ConsumirMensajes)
        }
    }

    // Delegamos la UI al componente puro
    ContenidoRecuperarClave(
        estado = estado,
        onEvento = viewModel::enEvento,
        onVolver = { navController.popBackStack() }
    )
}

// ==========================================
// 2. EL CUERPO (UI Pura - Stateless)
// ==========================================
@Composable
fun ContenidoRecuperarClave(
    estado: RecuperarClaveEstado, // Asegúrate de que tu Data Class se llame así
    onEvento: (RecuperarClaveEvento) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.ime
    ) { padding ->

        // 1. Fondo Galaxia
        FondoGalaxiaAnimado()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // HEADER
            // ==========================================
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            // Título Neón
            Text(
                text = "RECUPERAR",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0xFFD500F9),
                        blurRadius = 20f
                    )
                )
            )

            // RESORTE 1: Empuja contenido al centro visual
            Spacer(Modifier.weight(0.8f))

            // ==========================================
            // CONTENIDO ANIMADO (Formulario vs Éxito)
            // ==========================================
            AnimatedContent(
                targetState = estado.exito,
                label = "transicion_recuperacion",
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.9f) togetherWith
                            fadeOut(animationSpec = tween(400))
                }
            ) { fueExitoso ->
                if (fueExitoso) {
                    // --- VISTA DE ÉXITO ---
                    VistaExito(onVolver = onVolver)
                } else {
                    // --- VISTA DE FORMULARIO ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Introduce tu correo y te enviaremos un enlace mágico.",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        CampoEntradaLogin(
                            valor = estado.correo,
                            alCambiarValor = { onEvento(RecuperarClaveEvento.CorreoCambiado(it)) },
                            etiqueta = "Correo Electrónico",
                            icono = Icons.Default.Email,
                            esError = estado.error != null,
                            cargando = estado.estaCargando
                        )

                        Spacer(Modifier.height(16.dp))

                        BotonAnimado(
                            onClick = { onEvento(RecuperarClaveEvento.BotonEnviarCorreoPresionado) },
                            enabled = !estado.estaCargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (estado.estaCargando) {
                                Text("ENVIANDO...", fontSize = 14.sp, letterSpacing = 1.sp)
                            } else {
                                Text(
                                    "ENVIAR CÓDIGO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // RESORTE 2: Empuja footer abajo
            Spacer(Modifier.weight(1f))

            // ==========================================
            // FOOTER (Solo visible en Formulario)
            // ==========================================
            if (!estado.exito) {
                TextButton(
                    onClick = onVolver,
                    modifier = Modifier.padding(bottom = 30.dp)
                ) {
                    Text(
                        text = "CANCELAR Y VOLVER",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Spacer(Modifier.padding(bottom = 30.dp))
            }
        }
    }
}

// Sub-componente (Privado para este archivo)
@Composable
private fun VistaExito(onVolver: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono Check con Verde Neón
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF00E676)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "¡Correo Enviado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Revisa tu bandeja de entrada (y spam).\nHemos enviado las instrucciones.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(32.dp))

        BotonAnimado(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("VOLVER AL LOGIN", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 3. PREVIEWS (Formulario vs Éxito)
// ==========================================

// Mock del estado
val estadoRecuperarMock = RecuperarClaveEstado(
    correo = "",
    estaCargando = false,
    exito = false, // <--- Cambia esto a true para probar la vista de éxito
    error = null
)

@Preview(name = "1. Formulario - Normal", showSystemUi = true)
@Composable
fun PreviewRecuperarFormulario() {
    ContenidoRecuperarClave(
        estado = estadoRecuperarMock,
        onEvento = {},
        onVolver = {}
    )
}

@Preview(name = "2. Formulario - Cargando", showBackground = true)
@Composable
fun PreviewRecuperarCargando() {
    // Simulamos envío en proceso
    val estadoCargando = estadoRecuperarMock.copy(
        correo = "micorreo@gmail.com",
        estaCargando = true
    )

    ContenidoRecuperarClave(
        estado = estadoCargando,
        onEvento = {},
        onVolver = {}
    )
}

@Preview(name = "3. Vista de ÉXITO", showSystemUi = true)
@Composable
fun PreviewRecuperarExito() {
    // Simulamos que el correo ya se envió
    val estadoExito = estadoRecuperarMock.copy(
        exito = true
    )

    ContenidoRecuperarClave(
        estado = estadoExito,
        onEvento = {},
        onVolver = {}
    )
}