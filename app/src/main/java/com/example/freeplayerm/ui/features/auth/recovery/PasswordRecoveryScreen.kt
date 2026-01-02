package com.example.freeplayerm.ui.features.auth.recovery

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.R
import com.example.freeplayerm.ui.features.auth.components.BotonAnimado
import com.example.freeplayerm.ui.features.auth.components.CampoEntradaLogin
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground

// Importamos TUS componentes

// ==========================================
// 1. EL CEREBRO (Lógica + ViewModel)
// ==========================================
@Composable
fun PasswordRecoveryScreen(
    navController: NavController,
    viewModel: PasswordRecoveryViewModel = hiltViewModel(),
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
        onVolver = { navController.popBackStack() },
    )
}

// ==========================================
// 2. EL CUERPO (UI Pura - Stateless)
// ==========================================
@Composable
fun ContenidoRecuperarClave(
    estado: RecuperarClaveEstado, // Asegúrate de que tu Data Class se llame así
    onEvento: (RecuperarClaveEvento) -> Unit,
    onVolver: () -> Unit,
) {
    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets.ime) { padding
        ->

        // 1. Fondo Galaxia
        GalaxyBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ==========================================
            // HEADER
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                    )
                }
            }
            // Icono de la app
            Image(
                painter =
                    painterResource(id = R.mipmap.iconfreeplayer_foreground), // <-- Tu archivo aquí
                contentDescription = "Logo FreePlayer",
                modifier = Modifier.size(200.dp), // Ajusta el tamaño según necesites
            )

            // Título Neón
            Text(
                text = "RECUPERAR",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        shadow = Shadow(color = Color(0xFFD500F9), blurRadius = 20f),
                    ),
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
                },
            ) { fueExitoso ->
                if (fueExitoso) {
                    // --- VISTA DE ÉXITO ---
                    VistaExito(onVolver = onVolver)
                } else {
                    // --- VISTA DE FORMULARIO ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Introduce tu correo y te enviaremos un enlace mágico.",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                        )

                        Spacer(Modifier.height(8.dp))

                        CampoEntradaLogin(
                            valor = estado.correo,
                            alCambiarValor = { onEvento(RecuperarClaveEvento.CorreoCambiado(it)) },
                            etiqueta = "Correo Electrónico",
                            icono = Icons.Default.Email,
                            esError = estado.error != null,
                            cargando = estado.estaCargando,
                        )

                        Spacer(Modifier.height(16.dp))

                        BotonAnimado(
                            onClick = {
                                onEvento(RecuperarClaveEvento.BotonEnviarCorreoPresionado)
                            },
                            enabled = !estado.estaCargando,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (estado.estaCargando) {
                                Text("ENVIANDO...", fontSize = 14.sp, letterSpacing = 1.sp)
                            } else {
                                Text(
                                    "ENVIAR CÓDIGO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
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
                TextButton(onClick = onVolver, modifier = Modifier.padding(bottom = 30.dp)) {
                    Text(
                        text = "CANCELAR Y VOLVER",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
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
        verticalArrangement = Arrangement.Center,
    ) {
        // Icono Check con Verde Neón
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF00E676),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "¡Correo Enviado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Revisa tu bandeja de entrada (y spam).\nHemos enviado las instrucciones.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.8f),
        )

        Spacer(Modifier.height(32.dp))

        BotonAnimado(onClick = onVolver, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("VOLVER AL LOGIN", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 3. PREVIEWS (Escenarios de Pantalla)
// ==========================================

// --- ESCENARIO 1: Formulario Limpio ---
@Preview(
    name = "1. Formulario - Inicio",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun PreviewRecoveryInitial() {
    val estadoInicial =
        RecuperarClaveEstado(correo = "", estaCargando = false, exito = false, error = null)

    ContenidoRecuperarClave(estado = estadoInicial, onEvento = {}, onVolver = {})
}

// --- ESCENARIO 2: Estado de Carga (Enviando) ---
@Preview(
    name = "2. Formulario - Cargando",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewRecoveryLoading() {
    val estadoCargando =
        RecuperarClaveEstado(
            correo = "usuario@freeplayer.com",
            estaCargando = true, // Esto debe activar el loader en el input y deshabilitar el botón
            exito = false,
        )

    ContenidoRecuperarClave(estado = estadoCargando, onEvento = {}, onVolver = {})
}

// --- ESCENARIO 3: Error de Validación ---
@Preview(
    name = "3. Formulario - Error",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewRecoveryError() {
    val estadoError =
        RecuperarClaveEstado(
            correo = "correo_invalido",
            error = "El formato del correo no es correcto", // Activa borde rojo y toast
            estaCargando = false,
        )

    ContenidoRecuperarClave(estado = estadoError, onEvento = {}, onVolver = {})
}

// --- ESCENARIO 4: Éxito (Transición Completada) ---
@Preview(
    name = "4. Vista Éxito",
    group = "Pantallas Completas",
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun PreviewRecoverySuccess() {
    val estadoExito =
        RecuperarClaveEstado(
            correo = "usuario@freeplayer.com",
            exito = true, // Activa AnimatedContent -> VistaExito
        )

    ContenidoRecuperarClave(estado = estadoExito, onEvento = {}, onVolver = {})
}
