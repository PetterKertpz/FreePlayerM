package com.example.freeplayerm.com.example.freeplayerm.ui.features.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.R
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.theme.AppColors

// 5. Título con flotación lenta (Sin cambios)
// ui/features/login/components/AuthItems.kt

@Composable
fun TextoIniciarSesion() {
    val infiniteTransition = rememberInfiniteTransition(label = "flotacion_titulo")

    // 1. Animación de movimiento vertical (Flotar)
    val offsetY by
        infiniteTransition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "offsetY",
        )

    // 2. Animación de "Respiración" (Opacidad sutil)
    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "alpha",
        )

    // Definimos un pincel (Brush) para que el texto tenga degradado
    val textoGradiente =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color.White,
                    Color(0xFFE1BEE7), // Lila muy claro
                )
        )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "INICIAR SESIÓN", // Mayúsculas para más presencia
            style =
                TextStyle(
                    fontSize = 22.sp, // Un poco más pequeño pero más espaciado
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 6.sp, // ESTO da el toque elegante/cinematográfico
                    brush = textoGradiente,
                    shadow =
                        Shadow(
                            color = Color(0xFFD500F9).copy(alpha = 0.6f), // Resplandor Morado Neón
                            blurRadius = 20f, // Muy difuminado
                            offset = Offset(0f, 0f),
                        ),
                ),
            modifier =
                Modifier.offset(y = offsetY.dp).graphicsLayer {
                    this.alpha = alpha
                }, // Aplica la respiración
        )
    }
}

// ==========================================
// 1. Logo y Texto "FreePlayer" Estilizado
// ==========================================
@Composable
fun EncabezadoLogoAnimado() {
    var visible by remember { mutableStateOf(false) }

    // Colores para el texto estilo "WordArt"
    val colorRellenoMorado = Color(0xFFD500F9) // Morado neón brillante
    val colorBordeOscuro = Color(0xFF2A0036) // Morado casi negro para contraste
    val estiloFuente = FontFamily.Serif
    val tamanioFuente = 58.sp

    LaunchedEffect(Unit) { visible = true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        // --- EL ÍCONO (Justo encima) ---
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(tween(1000, delayMillis = 200)) + fadeIn(tween(1000)),
        ) {
            // REEMPLAZA ESTO CON TU RECURSO REAL SI ES UNA IMAGEN O LOTTIE
            Image(
                painter = painterResource(id = R.drawable.free_player), // <-- Tu archivo aquí
                contentDescription = "Logo FreePlayer",
            )
        }
        // --- EL TEXTO ESTILIZADO (Relleno Morado + Borde Oscuro) ---
        AnimatedVisibility(
            visible = visible,
            enter =
                slideInVertically(
                    initialOffsetY = { 80 },
                    animationSpec = tween(800, delayMillis = 400),
                ) + fadeIn(tween(800)),
        ) {
            // Usamos un Box para superponer el texto dos veces
            Box {
                // CAPA 1: El Borde (Stroke) - Se dibuja atrás y más grueso
                Text(
                    text = "FreePlayer",
                    fontSize = tamanioFuente,
                    fontFamily = estiloFuente,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic, // Simula cursiva
                    style =
                        LocalTextStyle.current.copy(
                            drawStyle =
                                Stroke(
                                    miter = 10f,
                                    width = 12f, // Grosor del borde
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                )
                        ),
                    color = colorBordeOscuro,
                )

                // CAPA 2: El Relleno (Fill) - Se dibuja encima
                Text(
                    text = "FreePlayer",
                    fontSize = tamanioFuente,
                    fontFamily = estiloFuente,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic, // Simula cursiva
                    color = colorRellenoMorado,
                )
            }
        }
    }
}

// 7. Botón Google con Zoom suave (Sin cambios importantes)
@Composable
fun BotonGooglePulsante(onClick: () -> Unit, cargando: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "google_pulse")
    val escala by
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "escala",
        )

    Button(
        onClick = onClick,
        enabled = !cargando,
        colors =
            ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth(0.85f).scale(if (cargando) 1f else escala).height(55.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = AppColors.ElectricViolet.v6,
                strokeWidth = 4.dp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Asumiendo que tienes el icono drawable
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(16.dp))
                Text("Acceder con Google", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

// ==========================================
// 1. FAKE DATA (Estados y Mocking)
// ==========================================

/**
 * Provee los estados para el botón de Google. Permite validar visualmente si el spinner de carga
 * está centrado y es visible.
 */
class GoogleButtonStateProvider : PreviewParameterProvider<Boolean> {
    override val values =
        sequenceOf(
            false, // Estado Normal
            true, // Estado Cargando (Loading Spinner)
        )
}

// ==========================================
// 2. PREVIEWS INDIVIDUALES (Aisladas)
// ==========================================

/**
 * Wrapper específico para simular el fondo "Galaxy" sin cargar el Canvas pesado. Usa el color base
 * de la galaxia (Deep Purple/Black) para asegurar que los efectos de luz y neón sean visibles.
 */
@Composable
private fun GalaxyContextWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
        GalaxyBackground()
        content()
    }
}

// --- COMPONENTE A: Título Flotante (Neón) ---
@Preview(name = "1. Título Flotante", group = "Decoraciones")
@Composable
fun PreviewFloatingTitle() {
    GalaxyContextWrapper {
        // Validamos el "glow" morado sobre el fondo oscuro
        TextoIniciarSesion()
    }
}

// --- COMPONENTE B: Logo Animado (Capas) ---
@Preview(name = "2. Logo Header", group = "Decoraciones")
@Composable
fun PreviewAnimatedHeader() {
    GalaxyContextWrapper {
        // Validamos la superposición del Borde Oscuro vs Relleno Morado
        EncabezadoLogoAnimado(
        
        )
    }
}

// --- COMPONENTE C: Botón Interactivo (Estados) ---
@Preview(name = "3. Botón Google", group = "Interacción")
@Composable
fun PreviewGoogleButton(@PreviewParameter(GoogleButtonStateProvider::class) isLoading: Boolean) {
    GalaxyContextWrapper { BotonGooglePulsante(onClick = {}, cargando = isLoading) }
}
