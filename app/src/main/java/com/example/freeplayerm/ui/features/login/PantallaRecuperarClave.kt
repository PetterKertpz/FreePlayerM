package com.example.freeplayerm.ui.features.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.freeplayerm.ui.features.login.components.CampoDeTextoAutenticacion
import com.example.freeplayerm.ui.features.nav.Rutas

@Composable
fun PantallaRecuperarClave(
    navController: NavController,
    viewModel: RecuperarClaveViewModel = hiltViewModel()
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(estado.error) {
        estado.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.enEvento(RecuperarClaveEvento.ConsumirMensajes)
        }
    }

    if (estado.exito) {
        PantallaExito(
            onVolverAlLogin = {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(Rutas.Login.ruta) { inclusive = true }
                }
            }
        )
    } else {
        PantallaSolicitarCorreo(estado = estado, enEvento = viewModel::enEvento)
    }
}

@Composable
private fun PantallaSolicitarCorreo(
    estado: RecuperarClaveEstado,
    enEvento: (RecuperarClaveEvento) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Recuperar Contraseña", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Introduce tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        CampoDeTextoAutenticacion(
            valor = estado.correo,
            enCambioDeValor = { enEvento(RecuperarClaveEvento.CorreoCambiado(it)) },
            etiqueta = "Correo Electrónico"
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (estado.estaCargando) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { enEvento(RecuperarClaveEvento.BotonEnviarCorreoPresionado) }) {
                Text("Enviar Correo")
            }
        }
    }
}

@Composable
private fun PantallaExito(onVolverAlLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Correo Enviado", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Revisa tu bandeja de entrada (y la carpeta de spam) y sigue las instrucciones del enlace para restablecer tu contraseña.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onVolverAlLogin) {
            Text("Volver a Inicio de Sesión")
        }
    }
}