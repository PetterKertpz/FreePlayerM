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
import androidx.compose.material3.ButtonDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.ui.features.login.components.CampoDeTextoAutenticacion
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * =================================================================
 * 1. El "Composable Inteligente" (Smart Composable)
 * =================================================================
 * Se encarga de la lógica: obtener el ViewModel, manejar los
 * efectos secundarios (Toasts, navegación) y pasar el estado y
 * los eventos a la UI.
 */
@Composable
fun PantallaRegistro(
    navController: NavController,
    viewModel: RegistroViewModel = hiltViewModel() // Inyectamos el ViewModel de Registro
) {
    // Recolectamos el estado del ViewModel de forma segura para el ciclo de vida
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Este efecto se ejecuta cuando el estado de 'registroExitoso' o 'error' cambia
    LaunchedEffect(key1 = estado.usuarioIdExitoso) { // <-- Observa el ID
        estado.usuarioIdExitoso?.let { id -> // <-- Reacciona cuando el ID no es nulo
            Toast.makeText(contexto, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
            // Navega usando el ID
            navController.navigate(Rutas.Biblioteca.crearRuta(id)) {
                popUpTo(Rutas.Login.ruta) { inclusive = true }
            }
            // Limpia el estado
            viewModel.enEvento(RegistroEvento.ConsumirEventoDeNavegacion)
        }
    }

    // Llamamos al Composable "Tonto" que solo se encarga de dibujar la UI
    CuerpoPantallaRegistro(
        estado = estado,
        enEvento = viewModel::enEvento, // Pasamos la función para manejar eventos
        onNavigateToLogin = {
            navController.navigate(Rutas.Login.ruta) {
                popUpTo("registro") { inclusive = true }
            }
        }
    )
}

/**
 * =================================================================
 * 2. El "Composable Tonto" (Dumb Composable)
 * =================================================================
 * Solo recibe el estado y lambdas para notificar eventos.
 * No tiene idea de ViewModels ni de navegación. Es 100% reutilizable y previsualizable.
 */
@Composable
fun CuerpoPantallaRegistro(
    estado: RegistroEstado,
    enEvento: (RegistroEvento) -> Unit,
    onNavigateToLogin: () -> Unit
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
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FreePlayer",
                    fontSize = 50.sp,
                    fontFamily = FontFamily.Default,
                    style = TextStyle(
                        shadow = Shadow(
                            color = AppColors.PurpuraOscuro,
                            blurRadius = 20f
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
                    "Crear Cuenta",
                    fontSize = 35.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                // ¡ADIÓS A 'remember' Y A LA LÓGICA DE ESTADO LOCAL!
                // Usamos nuestro componente reutilizable y el estado del ViewModel.
                CampoDeTextoAutenticacion(
                    valor = estado.nombreUsuario,
                    enCambioDeValor = { enEvento(RegistroEvento.NombreUsuarioCambiado(it)) },
                    etiqueta = "Nombre de usuario"
                )

                CampoDeTextoAutenticacion(
                    valor = estado.correo,
                    enCambioDeValor = { enEvento(RegistroEvento.CorreoCambiado(it)) },
                    etiqueta = "Correo Electrónico"
                )

                CampoDeTextoAutenticacion(
                    valor = estado.contrasena,
                    enCambioDeValor = { enEvento(RegistroEvento.ContrasenaCambiada(it)) },
                    etiqueta = "Contraseña",
                    esCampoDeContrasena = true
                )

                // Mostramos el indicador de progreso si el estado lo indica


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Tienes una cuenta?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
                if (!estado.estaCargando) {
                    Button(
                        onClick = { enEvento(RegistroEvento.BotonRegistroPresionado) },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PurpuraProfundo,
                            contentColor = AppColors.Blanco,
                            disabledContainerColor = AppColors.GrisProfundo,
                            disabledContentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, AppColors.Negro),
                        shape = RoundedCornerShape(15.dp),
                    ) {
                        Text(
                            "Registrarse",
                            color = AppColors.Blanco,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
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
                // Footer vacío como en tu diseño original
            }
        }
    }
}


/**
 * =================================================================
 * 3. Previsualización
 * =================================================================
 * La preview ahora llama al Composable "Tonto", pasándole un estado de ejemplo.
 * Funciona perfectamente sin necesidad de ejecutar toda la app.
 */
@Preview(name = "Estado Normal", showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaRegistroNormal() {
    FreePlayerMTheme {
        CuerpoPantallaRegistro(
            estado = RegistroEstado(
                nombreUsuario = "Usuario de Prueba",
                correo = "test@ejemplo.com",
                contrasena = "123456"
            ),
            enEvento = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Estado Cargando", showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaRegistroCargando() {
    FreePlayerMTheme {
        CuerpoPantallaRegistro(
            estado = RegistroEstado(
                nombreUsuario = "Usuario de Prueba",
                correo = "test@ejemplo.com",
                contrasena = "123456",
                estaCargando = true // <-- Activamos el estado de carga
            ),
            enEvento = {},
            onNavigateToLogin = {}
        )
    }
}