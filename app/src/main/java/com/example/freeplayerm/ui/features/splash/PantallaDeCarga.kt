package com.example.freeplayerm.ui.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.R
// Importa tu fondo galáctico
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado

@Composable
fun PantallaDeCarga() {
    // Animación de "Latido" para el Logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

    val escalaLogo by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f, // Zoom suave
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala_logo"
    )

    val opacidadTexto by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacidad_texto"
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->

        // 1. FONDO (Mismo universo que el Login)
        FondoGalaxiaAnimado()

        // 2. CONTENIDO CENTRADO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // LOGO ANIMADO (Latiendo)
                // Asegúrate de tener tu icono en drawable o mipmap
                Image(
                    painter = painterResource(id = R.mipmap.iconfreeplayer_foreground),
                    contentDescription = "Logo FreePlayer",
                    modifier = Modifier
                        .size(180.dp)
                        .scale(escalaLogo) // Aplica el latido
                )

                Spacer(modifier = Modifier.height(40.dp))

                // INDICADOR DE CARGA NEÓN
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFFD500F9), // Morado Neón
                    trackColor = Color(0xFF4A148C).copy(alpha = 0.5f), // Fondo oscuro del track
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(24.dp))

                // TEXTO CARGANDO
                Text(
                    text = "CARGANDO...",
                    color = Color.White.copy(alpha = opacidadTexto),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp, // Espaciado elegante estilo cine
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // 3. VERSIÓN DE LA APP (Opcional, detalle profesional al pie de página)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "FreePlayer v1.0",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(
    name = "1. Vista Completa (Pixel 7)",
    device = "id:pixel_7_pro",
    showSystemUi = true
)
@Composable
fun PreviewSplashCompleto() {
    // Aquí veremos la distribución vertical completa:
    // Logo centrado y Versión pegada al fondo.
    PantallaDeCarga()
}

@Preview(
    name = "2. Foco en Animación y Contraste",
    widthDp = 400,
    heightDp = 400
)
@Composable
fun PreviewSplashDetalle() {
    // Esta preview cuadrada nos ayuda a centrarnos solo en el logo y el loader.
    // Úsala con el "Interactive Mode" (el icono del dedo) para ver:
    // 1. El latido del logo.
    // 2. El parpadeo del texto "CARGANDO...".
    PantallaDeCarga()
}