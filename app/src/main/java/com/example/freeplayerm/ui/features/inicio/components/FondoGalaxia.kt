// ui/features/inicio/components/FondoGalaxia.kt
package com.example.freeplayerm.ui.features.inicio.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

// --- Configuración General ---
private const val CANTIDAD_ESTRELLAS_FLOTANTES = 150

// Datos para las estrellas flotantes normales
data class Estrella(
    val xInicial: Float, val yInicial: Float, val radio: Float,
    val velocidad: Float, val alfaBase: Float, val faseParpadeo: Float
)

// Datos para una estrella fugaz activa
data class EstrellaFugazEstado(
    val id: Long,
    val inicio: Offset,
    val fin: Offset,
    val animacionProgreso: Animatable<Float, AnimationVector1D>
)

@Composable
fun FondoGalaxiaAnimado() {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // ================= ANIMACIONES DE FONDO LENTAS =================
    val infiniteTransition = rememberInfiniteTransition(label = "fondo_galaxia")

    // Movimientos para nebulosas y manchas oscuras
    val offsetLento1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Reverse),
        label = "lento1"
    )
    val offsetLento2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -1500f,
        animationSpec = infiniteRepeatable(tween(35000, easing = LinearEasing), RepeatMode.Reverse),
        label = "lento2"
    )
    val respiracionNebulosa by infiniteTransition.animateFloat(
        initialValue = 1.2f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(18000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "respiracion"
    )

    // ================= CONFIG ESTRELLAS FLOTANTES =================
    val estrellasFlotantes = remember {
        List(CANTIDAD_ESTRELLAS_FLOTANTES) {
            Estrella(
                xInicial = Random.nextFloat(), yInicial = Random.nextFloat(),
                radio = Random.nextFloat() * 2.2f + 0.5f,
                velocidad = Random.nextFloat() * 0.3f + 0.05f,
                alfaBase = Random.nextFloat() * 0.5f + 0.3f,
                faseParpadeo = Random.nextFloat() * 2 * Math.PI.toFloat()
            )
        }
    }
    val tiempoAnimadoEstrellas by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "tiempo_estrellas"
    )

    // ================= SISTEMA DE ESTRELLAS FUGACES =================
    // Lista mutable para trackear las estrellas fugaces activas
    val estrellasFugacesActivas = remember { mutableStateListOf<EstrellaFugazEstado>() }

    // Generador de estrellas fugaces
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(Random.nextLong(3000, 8000)) // Lanzar una cada 3-8 segundos

            // Crear nueva estrella fugaz
            val id = System.nanoTime()
            // Empiezan fuera de la pantalla (arriba/izquierda) y cruzan hacia abajo/derecha
            val inicioX = Random.nextFloat() * 1000f // Rango amplio horizontal
            val inicioY = -Random.nextFloat() * 500f // Arriba de la pantalla
            val finX = inicioX + Random.nextFloat() * 800f + 500f // Trayectoria diagonal
            val finY = Random.nextFloat() * 2000f + 1000f // Abajo de la pantalla

            val nuevaEstrella = EstrellaFugazEstado(
                id = id,
                inicio = Offset(inicioX, inicioY),
                fin = Offset(finX, finY),
                animacionProgreso = Animatable(0f)
            )

            estrellasFugacesActivas.add(nuevaEstrella)

            // Iniciar animación y luego eliminarla
            launch {
                nuevaEstrella.animacionProgreso.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = Random.nextInt(800, 1500), easing = LinearOutSlowInEasing)
                )
                estrellasFugacesActivas.remove(nuevaEstrella)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // CAPA 1: NEBULOSAS DE COLOR Y MANCHAS OSCURAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxDim = max(width, height)

            // Fondo base casi negro
            drawRect(color = Color(0xFF020005))

            // --- NEBULOSAS DE LUZ (Colores) ---
            // Morado Principal
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD500F9).copy(alpha = 0.25f), Color(0xFF4A148C).copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(x = offsetLento1 % (width * 2), y = height * 0.3f),
                    radius = maxDim * 1.5f
                ), center = Offset(width / 2, height / 2), radius = maxDim * 3f
            )
            // Cian Respirando
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color(0xFF311B92).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(x = width * 1.2f, y = height * 0.8f),
                    radius = maxDim * respiracionNebulosa
                ), center = Offset(width / 2, height / 2), radius = maxDim * 3f
            )

            // --- NUEVO: MANCHAS DE MATERIA OSCURA (Contrastes) ---
            // Estas se dibujan SOBRE la luz para crear "vacíos"
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(x = width * 0.5f + offsetLento2 * 0.5f, y = height * 0.5f),
                    radius = maxDim * 1.2f
                ),
                center = Offset(width / 2, height / 2), radius = maxDim * 3f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(x = width * 0.2f, y = height * 1.2f),
                    radius = maxDim * 0.9f
                ),
                center = Offset(width / 2, height / 2), radius = maxDim * 3f
            )
        }

        // CAPA 2: ESTRELLAS FLOTANTES (Sin cambios)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            estrellasFlotantes.forEach { estrella ->
                val dy = (tiempoAnimadoEstrellas * 100 * estrella.velocidad)
                val yFin = (estrella.yInicial * h - dy) % h
                val yReal = if (yFin < 0) yFin + h else yFin
                val brillo = (sin((tiempoAnimadoEstrellas * 20 + estrella.faseParpadeo).toDouble()).toFloat() + 1f) / 2f
                val alpha = (estrella.alfaBase * 0.6f) + (brillo * 0.4f)
                drawCircle(Color.White.copy(alpha = alpha.coerceIn(0f, 0.8f)), estrella.radio * (0.9f + brillo * 0.2f), Offset(estrella.xInicial * w, yReal))
            }
        }

        // CAPA 3: NUEVO - ESTRELLAS FUGACES
        Canvas(modifier = Modifier.fillMaxSize()) {
            estrellasFugacesActivas.forEach { estrella ->
                val progreso = estrella.animacionProgreso.value
                // Calcular posición actual de la cabeza
                val currentPos = Offset(
                    x = androidx.compose.ui.util.lerp(estrella.inicio.x, estrella.fin.x, progreso),
                    y = androidx.compose.ui.util.lerp(estrella.inicio.y, estrella.fin.y, progreso)
                )
                // Calcular posición de la cola (un poco más atrás en el tiempo)
                val tailProgress = (progreso - 0.15f).coerceAtLeast(0f)
                val tailPos = Offset(
                    x = androidx.compose.ui.util.lerp(estrella.inicio.x, estrella.fin.x, tailProgress),
                    y = androidx.compose.ui.util.lerp(estrella.inicio.y, estrella.fin.y, tailProgress)
                )

                // Dibujar la estela
                if (progreso > 0f && progreso < 1f) {
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White, Color.White), // Cola transparente -> Cabeza blanca
                            start = tailPos,
                            end = currentPos
                        ),
                        start = tailPos,
                        end = currentPos,
                        strokeWidth = 4f * (1f - progreso), // Se hace más fina al final
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 915)
@Composable
fun PreviewFondoCompleto() {
    FondoGalaxiaAnimado()
}