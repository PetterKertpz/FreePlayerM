package com.example.freeplayerm.com.example.freeplayerm.ui.features.login.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.R
import com.example.freeplayerm.ui.features.inicio.components.FondoGalaxiaAnimado

// 5. Título con flotación lenta (Sin cambios)
// ui/features/login/components/ComponentesEspeciales.kt

@Composable
fun TextoTituloFlotante() {
    val infiniteTransition = rememberInfiniteTransition(label = "flotacion_titulo")

    // 1. Animación de movimiento vertical (Flotar)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // 2. Animación de "Respiración" (Opacidad sutil)
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Definimos un pincel (Brush) para que el texto tenga degradado
    val textoGradiente = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color(0xFFE1BEE7) // Lila muy claro
        )
    )

    Text(
        text = "INICIAR SESIÓN", // Mayúsculas para más presencia
        style = TextStyle(
            fontSize = 22.sp, // Un poco más pequeño pero más espaciado
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 6.sp, // ESTO da el toque elegante/cinematográfico
            brush = textoGradiente,
            shadow = Shadow(
                color = Color(0xFFD500F9).copy(alpha = 0.6f), // Resplandor Morado Neón
                blurRadius = 20f, // Muy difuminado
                offset = Offset(0f, 0f)
            )
        ),
        modifier = Modifier
            .offset(y = offsetY.dp)
            .graphicsLayer { this.alpha = alpha } // Aplica la respiración
    )
}

// ==========================================
// 1. Logo y Texto "FreePlayer" Estilizado
// ==========================================
@Composable
fun EncabezadoLogoAnimado() {
    var visible by remember { mutableStateOf(false) }

    // Colores para el texto estilo "WordArt"
    val colorRellenoMorado = Color(0xFFD500F9) // Morado neón brillante
    val colorBordeOscuro = Color(0xFF2A0036)   // Morado casi negro para contraste

    // NOTA SOBRE FUENTE CURSIVA:
    // Para un verdadero estilo cursivo, debes descargar una fuente (ej. "Dancing Script" o "Pacifico" de Google Fonts)
    // y ponerla en res/font. Por ahora, usaremos Serif + Italic para simularlo.
    val estiloFuente = FontFamily.Serif
    val tamañoFuente = 52.sp

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- EL ÍCONO (Justo encima) ---
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(tween(1000, delayMillis = 200)) + fadeIn(tween(1000))
        ) {
            // REEMPLAZA ESTO CON TU RECURSO REAL SI ES UNA IMAGEN O LOTTIE
            Image(
                painter = painterResource(id = R.mipmap.iconfreeplayer_foreground), // <-- Tu archivo aquí
                contentDescription = "Logo FreePlayer",
                modifier = Modifier.size(200.dp) // Ajusta el tamaño según necesites
            )
        }

        // --- EL TEXTO ESTILIZADO (Relleno Morado + Borde Oscuro) ---
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 80 },
                animationSpec = tween(800, delayMillis = 400)
            ) + fadeIn(tween(800))
        ) {
            // Usamos un Box para superponer el texto dos veces
            Box() {
                // CAPA 1: El Borde (Stroke) - Se dibuja atrás y más grueso
                Text(
                    text = "FreePlayer",
                    fontSize = tamañoFuente,
                    fontFamily = estiloFuente,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic, // Simula cursiva
                    style = LocalTextStyle.current.copy(
                        drawStyle = Stroke(
                            miter = 10f,
                            width = 12f, // Grosor del borde
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    ),
                    color = colorBordeOscuro
                )

                // CAPA 2: El Relleno (Fill) - Se dibuja encima
                Text(
                    text = "FreePlayer",
                    fontSize = tamañoFuente,
                    fontFamily = estiloFuente,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic, // Simula cursiva
                    color = colorRellenoMorado
                )
            }
        }
    }
}

// 7. Botón Google con Zoom suave (Sin cambios importantes)
@Composable
fun BotonGooglePulsante(
    onClick: () -> Unit,
    cargando: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "google_pulse")
    val escala by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Button(
        onClick = onClick,
        enabled = !cargando,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .scale(if(cargando) 1f else escala)
            .height(55.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 3.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Asumiendo que tienes el icono drawable
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text("Acceder con Google", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// WRAPPER: Reutilizamos el contexto Galaxia
// -----------------------------------------------------------------------------
@Composable
private fun GalaxyPreviewContext(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo base (si tienes el composable FondoGalaxiaAnimado importado, úsalo aquí)
        // Si no, usamos un color sólido oscuro para simularlo en estas pruebas aisladas
        Surface(color = Color(0xFF0F0518), modifier = Modifier.fillMaxSize()) {
            FondoGalaxiaAnimado() // Asumiendo que está en el mismo paquete o importado
        }

        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW 1: COREOGRAFÍA DE ENTRADA (Logo + Título)
// -----------------------------------------------------------------------------
// Usa el "Interactive Mode" (dedito en Android Studio) para ver la animación de entrada
@Preview(name = "1. Coreografía Header", group = "Animaciones", showBackground = true)
@Composable
fun PreviewHeaderAnimado() {
    GalaxyPreviewContext {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EncabezadoLogoAnimado()

            Spacer(modifier = Modifier.height(8.dp))

            // Validamos que la flotación sutil no distraiga del logo
            TextoTituloFlotante()
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW 2: ESTADOS DEL BOTÓN GOOGLE
// -----------------------------------------------------------------------------
@Preview(name = "2. Google - Normal (Pulsando)", group = "Botones")
@Composable
fun PreviewGoogleNormal() {
    GalaxyPreviewContext {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Estado Normal: Debería hacer "Zoom In/Out" suavemente
            BotonGooglePulsante(
                onClick = {},
                cargando = false
            )
        }
    }
}

@Preview(name = "3. Google - Cargando (Quieto)", group = "Botones")
@Composable
fun PreviewGoogleCargando() {
    GalaxyPreviewContext {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Estado Carga: El botón debe estar quieto (scale 1f) y mostrar spinner
            BotonGooglePulsante(
                onClick = {},
                cargando = true
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW 3: COMPOSICIÓN FINAL (El "Look & Feel")
// -----------------------------------------------------------------------------
@Preview(name = "4. Login Full Layout", device = "id:pixel_7_pro", showSystemUi = true)
@Composable
fun PreviewLayoutCompleto() {
    GalaxyPreviewContext {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Distribución vertical
        ) {
            // Sección Superior
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EncabezadoLogoAnimado()
                TextoTituloFlotante()
            }

            // Sección Inputs (Simulada visualmente)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Aquí irían tus campos de texto
                // Pongo un placeholder visual para ver el espacio
                Text(
                    "Area de Inputs (Correo/Pass)",
                    color = Color.White.copy(0.3f)
                )
            }

            // Sección Inferior
            BotonGooglePulsante(
                onClick = {},
                cargando = false
            )
        }
    }
}