package com.example.freeplayerm.ui.features.inicio.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.R
import com.example.freeplayerm.ui.features.inicio.components.BotonAnimado
import com.example.freeplayerm.ui.features.inicio.components.CampoEntradaLogin
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado
import com.example.freeplayerm.ui.features.nav.Rutas

// ==========================================
// 1. EL CEREBRO (Lógica)
// ==========================================
@Composable
fun PantallaRegistro(
    navController: NavController,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(key1 = estado.usuarioIdExitoso) {
        estado.usuarioIdExitoso?.let { id ->
            Toast.makeText(contexto, "¡Bienvenido a FreePlayer!", Toast.LENGTH_SHORT).show()
            navController.navigate(Rutas.Biblioteca.crearRuta(id)) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            viewModel.enEvento(RegistroEvento.ConsumirEventoDeNavegacion)
        }
    }

    LaunchedEffect(estado.error) {
        estado.error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(RegistroEvento.ConsumirError)
        }
    }

    ContenidoPantallaRegistro(
        estado = estado,
        onEvento = viewModel::enEvento,
        onVolver = { navController.popBackStack() }
    )
}

// ==========================================
// 2. EL CUERPO (UI Visual)
// ==========================================
@Composable
fun ContenidoPantallaRegistro(
    estado: RegistroEstado,
    onEvento: (RegistroEvento) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.ime
    ) { padding ->

        FondoGalaxiaAnimado()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // HEADER ACTUALIZADO (Icono + Título)
            // ==========================================
            Spacer(Modifier.height(16.dp))

            // 1. Botón Volver (Alineado izquierda)
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

            Spacer(Modifier.height(10.dp))

            // 2. Título + Icono de App (Centrados)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Icono de la App (Brillando en Morado)
                Image(
                    painter = painterResource(id = R.mipmap.iconfreeplayer_foreground), // <-- Tu archivo aquí
                    contentDescription = "Logo FreePlayer",
                    modifier = Modifier.size(200.dp) // Ajusta el tamaño según necesites
                )

                Spacer(Modifier.width(12.dp))

                // Título Neón
                Text(
                    text = "CREAR CUENTA",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp, // Reduje un poco el espaciado para que quepa bien
                        color = Color.White,
                        shadow = Shadow(
                            color = Color(0xFFD500F9),
                            blurRadius = 20f
                        )
                    )
                )
            }

            Spacer(Modifier.height(40.dp))

            // ==========================================
            // FORMULARIO
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CampoEntradaLogin(
                    valor = estado.nombreUsuario,
                    alCambiarValor = { onEvento(RegistroEvento.NombreUsuarioCambiado(it)) },
                    etiqueta = "Nombre de Usuario",
                    icono = Icons.Default.Person,
                    esError = estado.error != null && estado.nombreUsuario.isBlank(),
                    cargando = estado.estaCargando
                )

                CampoEntradaLogin(
                    valor = estado.correo,
                    alCambiarValor = { onEvento(RegistroEvento.CorreoCambiado(it)) },
                    etiqueta = "Correo Electrónico",
                    icono = Icons.Default.Email,
                    esError = estado.error != null && estado.correo.isBlank(),
                    cargando = estado.estaCargando
                )

                CampoEntradaLogin(
                    valor = estado.contrasena,
                    alCambiarValor = { onEvento(RegistroEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    icono = Icons.Default.Lock,
                    esPassword = true,
                    esError = estado.error != null && estado.contrasena.isBlank(),
                    cargando = estado.estaCargando
                )

                CampoEntradaLogin(
                    valor = estado.confirmarContrasena,
                    alCambiarValor = { onEvento(RegistroEvento.ConfirmarContrasenaCambiada(it)) },
                    etiqueta = "Confirmar Contraseña",
                    icono = Icons.Default.Lock,
                    esPassword = true,
                    esError = (estado.error != null && estado.contrasena != estado.confirmarContrasena),
                    cargando = estado.estaCargando
                )

                Spacer(Modifier.height(24.dp))

                BotonAnimado(
                    onClick = { onEvento(RegistroEvento.BotonRegistroPresionado) },
                    enabled = !estado.estaCargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.estaCargando) {
                        Text("CREANDO PERFIL...", fontSize = 14.sp, letterSpacing = 1.sp)
                    } else {
                        Text("REGISTRARSE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ==========================================
            // FOOTER
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 30.dp)
            ) {
                TextButton(
                    onClick = onVolver,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = "¿YA TIENES CUENTA?",
                        color = Color(0xFFE040FB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. PREVIEWS
// ==========================================
val estadoRegistroMock = RegistroEstado(
    nombreUsuario = "", correo = "", contrasena = "", confirmarContrasena = "",
    estaCargando = false, error = null, usuarioIdExitoso = null
)

@Preview(name = "Registro con Icono", showSystemUi = true)
@Composable
fun PreviewRegistroIcono() {
    ContenidoPantallaRegistro(
        estado = estadoRegistroMock,
        onEvento = {},
        onVolver = {}
    )
}