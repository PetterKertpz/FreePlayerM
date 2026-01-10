// ui/features/inicio/components/GalaxyBackground.kt
package com.example.freeplayerm.ui.features.auth.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN GLOBAL - Tokens de diseño centralizados
// ═══════════════════════════════════════════════════════════════════════════════

private object GalaxyConfig {
   // Cantidades de elementos
   const val ESTRELLAS_FLOTANTES = 180
   const val ESTRELLAS_DISTANTES = 80
   const val PARTICULAS_POLVO = 40

   // Colores base del universo (Paleta cósmica)
   val COLOR_FONDO_BASE = Color(0xFF010208)
   val COLOR_NEBULOSA_PRINCIPAL = Color(0xFFD500F9)
   val COLOR_NEBULOSA_SECUNDARIA = Color(0xFF7C4DFF)
   val COLOR_NEBULOSA_TERCIARIA = Color(0xFF00E5FF)
   val COLOR_NEBULOSA_CALIDA = Color(0xFFFF6D00)
   val COLOR_MATERIA_OSCURA = Color(0xFF000000)
   val COLOR_ESTRELLA_FRIA = Color(0xFFB3E5FC)
   val COLOR_ESTRELLA_CALIDA = Color(0xFFFFF8E1)

   // Tiempos de animación (ms)
   const val DURACION_CICLO_NEBULOSA = 45000
   const val DURACION_RESPIRACION = 20000
   const val DURACION_CICLO_ESTRELLAS = 18000
   const val RANGO_FUGAZ_MIN = 2500L
   const val RANGO_FUGAZ_MAX = 7000L

   // ═══════════════════════════════════════════════════════════════════════════
   // NUEVO: Configuración de zonas de transición (fade)
   // ═══════════════════════════════════════════════════════════════════════════

   /** Zona de desvanecimiento en los bordes (porcentaje de la pantalla) */
   const val ZONA_FADE_BORDE = 0.12f

   /** Margen extra fuera de pantalla para spawn/despawn suave */
   const val MARGEN_SPAWN = 0.08f
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODELOS DE DATOS - Inmutables para máxima estabilidad
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
data class Estrella(
   val xNorm: Float,
   val yNorm: Float,
   val radio: Float,
   val velocidad: Float,
   val alfaBase: Float,
   val faseParpadeo: Float,
   val temperatura: Float,
)

@Immutable
data class ParticulaPolvo(
   val xNorm: Float,
   val yNorm: Float,
   val radio: Float,
   val alfaBase: Float,
   val velocidadX: Float,
   val velocidadY: Float,
)

@Immutable
data class EstrellaFugazEstado(
   val id: Long,
   val inicio: Offset,
   val fin: Offset,
   val grosor: Float,
   val animacion: Animatable<Float, AnimationVector1D>,
)

// ═══════════════════════════════════════════════════════════════════════════════
// FUNCIONES DE UTILIDAD PARA TRANSICIONES SUAVES
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Calcula un factor de opacidad basado en la posición vertical. Crea zonas de fade-in (arriba) y
 * fade-out (abajo) para transiciones suaves.
 *
 * @param yNormalizada Posición Y normalizada [0, 1] donde 0 = arriba, 1 = abajo
 * @param zonaFade Tamaño de la zona de transición (0.1 = 10% de la pantalla)
 * @return Factor de opacidad [0, 1]
 */
private fun calcularFadeBordeVertical(yNormalizada: Float, zonaFade: Float): Float {
   return when {
      // Zona superior: fade-in gradual (aparece desde arriba)
      yNormalizada < zonaFade -> {
         (yNormalizada / zonaFade).coerceIn(0f, 1f)
      }
      // Zona inferior: fade-out gradual (desaparece hacia abajo)
      yNormalizada > (1f - zonaFade) -> {
         ((1f - yNormalizada) / zonaFade).coerceIn(0f, 1f)
      }
      // Zona central: opacidad completa
      else -> 1f
   }
}

/** Calcula factor de opacidad para bordes horizontales (usado en polvo cósmico). */
private fun calcularFadeBordeHorizontal(xNormalizada: Float, zonaFade: Float): Float {
   return when {
      xNormalizada < zonaFade -> (xNormalizada / zonaFade).coerceIn(0f, 1f)
      xNormalizada > (1f - zonaFade) -> ((1f - xNormalizada) / zonaFade).coerceIn(0f, 1f)
      else -> 1f
   }
}

/**
 * Wrap-around extendido que permite que los elementos se muevan ligeramente fuera de la pantalla
 * antes de reaparecer, sincronizado con el fade.
 *
 * @param posicion Posición actual
 * @param limite Límite máximo (ancho o alto)
 * @param margen Margen extra fuera de pantalla
 * @return Nueva posición con wrap-around suave
 */
private fun wrapAroundSuave(posicion: Float, limite: Float, margen: Float): Float {
   val limiteExtendido = limite * (1f + margen * 2)
   val offset = limite * margen

   // Ajusta la posición al rango extendido
   val posAjustada = posicion + offset
   val posWrapped = posAjustada.mod(limiteExtendido)

   return posWrapped - offset
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSABLE PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GalaxyBackground(
   modifier: Modifier = Modifier,
   intensidadNebulosas: Float = 1f,
   velocidadAnimacion: Float = 1f,
) {
   val density = LocalDensity.current

   // ───────────────────────────────────────────────────────────────────────────
   // SISTEMA DE ANIMACIONES - Transición infinita centralizada
   // ───────────────────────────────────────────────────────────────────────────
   val transition = rememberInfiniteTransition(label = "galaxy_master")

   // Movimiento orbital lento para nebulosas
   val orbitalPhase by
      transition.animateFloat(
         initialValue = 0f,
         targetValue = (2 * PI).toFloat(),
         animationSpec =
            infiniteRepeatable(
               animation =
                  tween(
                     durationMillis =
                        (GalaxyConfig.DURACION_CICLO_NEBULOSA / velocidadAnimacion).toInt(),
                     easing = LinearEasing,
                  ),
               repeatMode = RepeatMode.Restart,
            ),
         label = "orbital",
      )

   // Respiración de nebulosas (escala pulsante)
   val respiracion by
      transition.animateFloat(
         initialValue = 0.9f,
         targetValue = 1.15f,
         animationSpec =
            infiniteRepeatable(
               animation =
                  tween(
                     durationMillis =
                        (GalaxyConfig.DURACION_RESPIRACION / velocidadAnimacion).toInt(),
                     easing = FastOutSlowInEasing,
                  ),
               repeatMode = RepeatMode.Reverse,
            ),
         label = "respiracion",
      )

   // Tiempo normalizado para estrellas [0,1] - Ciclo continuo
   val tiempoEstrellas by
      transition.animateFloat(
         initialValue = 0f,
         targetValue = 1f,
         animationSpec =
            infiniteRepeatable(
               animation =
                  tween(
                     durationMillis =
                        (GalaxyConfig.DURACION_CICLO_ESTRELLAS / velocidadAnimacion).toInt(),
                     easing = LinearEasing,
                  ),
               repeatMode = RepeatMode.Restart,
            ),
         label = "tiempo_estrellas",
      )

   // Pulso secundario para efectos de brillo
   val pulsoGlobal by
      transition.animateFloat(
         initialValue = 0f,
         targetValue = 1f,
         animationSpec =
            infiniteRepeatable(
               animation = tween(4000, easing = EaseInOutCubic),
               repeatMode = RepeatMode.Reverse,
            ),
         label = "pulso",
      )

   // ───────────────────────────────────────────────────────────────────────────
   // DATOS PRE-CALCULADOS - Solo se generan una vez
   // ───────────────────────────────────────────────────────────────────────────
   val estrellasFlotantes = remember {
      List(GalaxyConfig.ESTRELLAS_FLOTANTES) {
         Estrella(
            xNorm = Random.nextFloat(),
            // Distribuir en rango extendido para entrada suave
            yNorm =
               Random.nextFloat() * (1f + GalaxyConfig.MARGEN_SPAWN * 2) -
                  GalaxyConfig.MARGEN_SPAWN,
            radio = Random.nextFloat() * 2.5f + 0.4f,
            velocidad = Random.nextFloat() * 0.25f + 0.03f,
            alfaBase = Random.nextFloat() * 0.45f + 0.35f,
            faseParpadeo = Random.nextFloat() * (2 * PI).toFloat(),
            temperatura = Random.nextFloat(),
         )
      }
   }

   val estrellasDistantes = remember {
      List(GalaxyConfig.ESTRELLAS_DISTANTES) {
         Estrella(
            xNorm = Random.nextFloat(),
            yNorm =
               Random.nextFloat() * (1f + GalaxyConfig.MARGEN_SPAWN * 2) -
                  GalaxyConfig.MARGEN_SPAWN,
            radio = Random.nextFloat() * 0.8f + 0.2f,
            velocidad = Random.nextFloat() * 0.01f + 0.005f,
            alfaBase = Random.nextFloat() * 0.2f + 0.1f,
            faseParpadeo = Random.nextFloat() * (2 * PI).toFloat(),
            temperatura = Random.nextFloat(),
         )
      }
   }

   val particulasPolvo = remember {
      List(GalaxyConfig.PARTICULAS_POLVO) {
         ParticulaPolvo(
            xNorm = Random.nextFloat(),
            yNorm = Random.nextFloat(),
            radio = Random.nextFloat() * 60f + 30f,
            alfaBase = Random.nextFloat() * 0.04f + 0.01f,
            velocidadX = (Random.nextFloat() - 0.5f) * 0.02f,
            velocidadY = Random.nextFloat() * 0.01f + 0.005f,
         )
      }
   }

   // ───────────────────────────────────────────────────────────────────────────
   // SISTEMA DE ESTRELLAS FUGACES
   // ───────────────────────────────────────────────────────────────────────────
   val estrellasFugaces = remember { mutableStateListOf<EstrellaFugazEstado>() }

   LaunchedEffect(velocidadAnimacion) {
      while (isActive) {
         delay(
            (Random.nextLong(GalaxyConfig.RANGO_FUGAZ_MIN, GalaxyConfig.RANGO_FUGAZ_MAX) /
                  velocidadAnimacion)
               .toLong()
         )

         val inicioX = Random.nextFloat() * 1.2f - 0.1f
         val inicioY = Random.nextFloat() * 0.3f - 0.1f

         val nueva =
            EstrellaFugazEstado(
               id = System.nanoTime(),
               inicio = Offset(inicioX, inicioY),
               fin =
                  Offset(
                     inicioX + Random.nextFloat() * 0.4f + 0.3f,
                     inicioY + Random.nextFloat() * 0.5f + 0.4f,
                  ),
               grosor = Random.nextFloat() * 2f + 2f,
               animacion = Animatable(0f),
            )

         estrellasFugaces.add(nueva)

         launch {
            nueva.animacion.animateTo(
               targetValue = 1f,
               animationSpec =
                  tween(durationMillis = Random.nextInt(600, 1200), easing = EaseInOutCubic),
            )
            estrellasFugaces.remove(nueva)
         }
      }
   }

   // ───────────────────────────────────────────────────────────────────────────
   // VALORES DERIVADOS - Optimiza recálculos
   // ───────────────────────────────────────────────────────────────────────────
   val offsetOrbital by
      remember(orbitalPhase) {
         derivedStateOf {
            Offset(x = cos(orbitalPhase) * 0.15f, y = sin(orbitalPhase * 0.7f) * 0.1f)
         }
      }

   // ═══════════════════════════════════════════════════════════════════════════
   // RENDERIZADO POR CAPAS
   // ═══════════════════════════════════════════════════════════════════════════

   Box(modifier = modifier.fillMaxSize()) {

      // CAPA 0: Fondo base con gradiente profundo
      Canvas(modifier = Modifier.fillMaxSize()) { dibujarFondoBase() }

      // CAPA 1: Nebulosas y materia oscura
      Canvas(modifier = Modifier.fillMaxSize()) {
         dibujarNebulosas(
            offsetOrbital = offsetOrbital,
            respiracion = respiracion,
            intensidad = intensidadNebulosas,
            pulso = pulsoGlobal,
         )
      }

      // CAPA 2: Polvo cósmico (partículas difusas) - CON FADE
      Canvas(modifier = Modifier.fillMaxSize()) {
         dibujarPolvoCosmico(particulas = particulasPolvo, tiempo = tiempoEstrellas)
      }

      // CAPA 3: Estrellas distantes (parallax lento) - CON FADE
      Canvas(modifier = Modifier.fillMaxSize()) {
         dibujarEstrellasConFade(
            estrellas = estrellasDistantes,
            tiempo = tiempoEstrellas * 0.3f,
            factorBrillo = pulsoGlobal * 0.5f + 0.5f,
         )
      }

      // CAPA 4: Estrellas cercanas (parallax rápido) - CON FADE
      Canvas(modifier = Modifier.fillMaxSize()) {
         dibujarEstrellasConFade(
            estrellas = estrellasFlotantes,
            tiempo = tiempoEstrellas,
            factorBrillo = 1f,
         )
      }

      // CAPA 5: Estrellas fugaces
      Canvas(modifier = Modifier.fillMaxSize()) { dibujarEstrellasFugaces(estrellasFugaces) }

      // CAPA 6: Viñeta sutil para profundidad
      Canvas(modifier = Modifier.fillMaxSize()) { dibujarVineta() }
   }
}

// ═══════════════════════════════════════════════════════════════════════════════
// EXTENSIONES DE DRAWSCOPE - Lógica de dibujo encapsulada
// ═══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.dibujarFondoBase() {
   drawRect(
      brush =
         Brush.radialGradient(
            colors = listOf(Color(0xFF0A0A18), Color(0xFF050510), GalaxyConfig.COLOR_FONDO_BASE),
            center = Offset(size.width * 0.5f, size.height * 0.4f),
            radius = size.maxDimension * 1.2f,
         )
   )
}

private fun DrawScope.dibujarNebulosas(
   offsetOrbital: Offset,
   respiracion: Float,
   intensidad: Float,
   pulso: Float,
) {
   val w = size.width
   val h = size.height
   val maxDim = size.maxDimension

   // Nebulosa Principal (Violeta/Magenta)
   drawCircle(
      brush =
         Brush.radialGradient(
            colors =
               listOf(
                  GalaxyConfig.COLOR_NEBULOSA_PRINCIPAL.copy(alpha = 0.22f * intensidad),
                  GalaxyConfig.COLOR_NEBULOSA_SECUNDARIA.copy(alpha = 0.08f * intensidad),
                  Color.Transparent,
               ),
            center = Offset(w * (0.3f + offsetOrbital.x), h * (0.25f + offsetOrbital.y)),
            radius = maxDim * respiracion * 0.9f,
         ),
      radius = maxDim * 2f,
   )

   // Nebulosa Secundaria (Cyan/Azul)
   drawCircle(
      brush =
         Brush.radialGradient(
            colors =
               listOf(
                  GalaxyConfig.COLOR_NEBULOSA_TERCIARIA.copy(alpha = 0.12f * intensidad),
                  Color(0xFF1A237E).copy(alpha = 0.06f * intensidad),
                  Color.Transparent,
               ),
            center = Offset(w * (0.8f - offsetOrbital.x * 0.5f), h * (0.7f - offsetOrbital.y)),
            radius = maxDim * (respiracion * 0.8f + pulso * 0.1f),
         ),
      radius = maxDim * 2f,
   )

   // Acento cálido (Naranja tenue)
   drawCircle(
      brush =
         Brush.radialGradient(
            colors =
               listOf(
                  GalaxyConfig.COLOR_NEBULOSA_CALIDA.copy(alpha = 0.06f * intensidad),
                  Color.Transparent,
               ),
            center = Offset(w * 0.15f, h * 0.85f),
            radius = maxDim * 0.5f * respiracion,
         ),
      radius = maxDim,
   )

   // Manchas de Materia Oscura (Contraste)
   drawCircle(
      brush =
         Brush.radialGradient(
            colors =
               listOf(
                  GalaxyConfig.COLOR_MATERIA_OSCURA.copy(alpha = 0.75f),
                  GalaxyConfig.COLOR_MATERIA_OSCURA.copy(alpha = 0.3f),
                  Color.Transparent,
               ),
            center = Offset(w * (0.5f - offsetOrbital.x * 0.3f), h * 0.55f),
            radius = maxDim * 0.7f,
         ),
      radius = maxDim * 2f,
   )

   drawCircle(
      brush =
         Brush.radialGradient(
            colors =
               listOf(GalaxyConfig.COLOR_MATERIA_OSCURA.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(w * 0.85f, h * 0.2f),
            radius = maxDim * 0.4f,
         ),
      radius = maxDim,
   )
}

/** MEJORADO: Dibuja polvo cósmico con fade en los bordes para transiciones suaves. */
private fun DrawScope.dibujarPolvoCosmico(particulas: List<ParticulaPolvo>, tiempo: Float) {
   val w = size.width
   val h = size.height
   val margen = GalaxyConfig.MARGEN_SPAWN
   val zonaFade = GalaxyConfig.ZONA_FADE_BORDE

   particulas.forEach { p ->
      // Cálculo de posición con wrap-around extendido
      val xRaw = p.xNorm + tiempo * p.velocidadX * 10
      val yRaw = p.yNorm + tiempo * p.velocidadY * 10

      val xWrapped = wrapAroundSuave(xRaw * w, w, margen)
      val yWrapped = wrapAroundSuave(yRaw * h, h, margen)

      // Calcular posición normalizada para el fade
      val xNorm = (xWrapped / w).coerceIn(0f, 1f)
      val yNorm = (yWrapped / h).coerceIn(0f, 1f)

      // Factor de fade combinado (horizontal y vertical)
      val fadeX = calcularFadeBordeHorizontal(xNorm, zonaFade)
      val fadeY = calcularFadeBordeVertical(yNorm, zonaFade)
      val fadeCombinado = fadeX * fadeY

      // Solo dibujar si es visible
      if (fadeCombinado > 0.01f) {
         drawCircle(
            brush =
               Brush.radialGradient(
                  colors =
                     listOf(
                        GalaxyConfig.COLOR_NEBULOSA_SECUNDARIA.copy(
                           alpha = p.alfaBase * fadeCombinado
                        ),
                        Color.Transparent,
                     ),
                  center = Offset(xWrapped, yWrapped),
                  radius = p.radio,
               ),
            center = Offset(xWrapped, yWrapped),
            radius = p.radio,
         )
      }
   }
}

/**
 * MEJORADO: Dibuja estrellas con sistema de fade en bordes verticales. Las estrellas se desvanecen
 * gradualmente al acercarse a los bordes, haciendo que el wrap-around sea completamente
 * imperceptible.
 */
private fun DrawScope.dibujarEstrellasConFade(
   estrellas: List<Estrella>,
   tiempo: Float,
   factorBrillo: Float,
) {
   val w = size.width
   val h = size.height
   val zonaFade = GalaxyConfig.ZONA_FADE_BORDE
   val margen = GalaxyConfig.MARGEN_SPAWN

   estrellas.forEach { estrella ->
      // Movimiento vertical con wrap-around extendido
      val desplazamiento = tiempo * 150f * estrella.velocidad
      val yBase = estrella.yNorm * h - desplazamiento

      // Wrap-around suave con margen extra
      val yPos = wrapAroundSuave(yBase, h, margen)
      val xPos = estrella.xNorm * w

      // Calcular posición normalizada para el fade
      val yNormalizada = (yPos / h).coerceIn(-margen, 1f + margen)

      // Factor de fade basado en posición vertical
      val factorFade = calcularFadeBordeVertical(yNormalizada.coerceIn(0f, 1f), zonaFade)

      // Parpadeo suave usando seno
      val parpadeo = (sin(tiempo * 25f + estrella.faseParpadeo) + 1f) / 2f

      // Alpha final combinando: base + parpadeo + brillo global + fade de borde
      val alphaFinal = (estrella.alfaBase * 0.6f + parpadeo * 0.4f) * factorBrillo * factorFade

      // Solo dibujar si tiene opacidad significativa
      if (alphaFinal > 0.01f) {
         // Color basado en "temperatura" de la estrella
         val colorEstrella =
            androidx.compose.ui.graphics.lerp(
               GalaxyConfig.COLOR_ESTRELLA_FRIA,
               GalaxyConfig.COLOR_ESTRELLA_CALIDA,
               estrella.temperatura,
            )

         // Halo difuso para estrellas grandes
         if (estrella.radio > 1.5f && alphaFinal > 0.1f) {
            drawCircle(
               color = colorEstrella.copy(alpha = alphaFinal * 0.15f),
               radius = estrella.radio * 4f,
               center = Offset(xPos, yPos),
            )
         }

         // Núcleo brillante
         drawCircle(
            color = colorEstrella.copy(alpha = alphaFinal.coerceIn(0f, 0.95f)),
            radius = estrella.radio * (0.85f + parpadeo * 0.3f),
            center = Offset(xPos, yPos),
         )
      }
   }
}

private fun DrawScope.dibujarEstrellasFugaces(estrellas: List<EstrellaFugazEstado>) {
   val w = size.width
   val h = size.height

   estrellas.forEach { ef ->
      val progreso = ef.animacion.value
      if (progreso <= 0f || progreso >= 1f) return@forEach

      val inicioReal = Offset(ef.inicio.x * w, ef.inicio.y * h)
      val finReal = Offset(ef.fin.x * w, ef.fin.y * h)

      val posCabeza =
         Offset(
            x = androidx.compose.ui.util.lerp(inicioReal.x, finReal.x, progreso),
            y = androidx.compose.ui.util.lerp(inicioReal.y, finReal.y, progreso),
         )

      val longitudCola = 0.2f * (1f - progreso * 0.5f)
      val progresoCola = (progreso - longitudCola).coerceAtLeast(0f)
      val posCola =
         Offset(
            x = androidx.compose.ui.util.lerp(inicioReal.x, finReal.x, progresoCola),
            y = androidx.compose.ui.util.lerp(inicioReal.y, finReal.y, progresoCola),
         )

      val alphaFugaz =
         if (progreso > 0.7f) {
            1f - ((progreso - 0.7f) / 0.3f)
         } else 1f

      drawLine(
         brush =
            Brush.linearGradient(
               colors =
                  listOf(
                     Color.Transparent,
                     Color.White.copy(alpha = 0.3f * alphaFugaz),
                     Color.White.copy(alpha = 0.9f * alphaFugaz),
                  ),
               start = posCola,
               end = posCabeza,
            ),
         start = posCola,
         end = posCabeza,
         strokeWidth = ef.grosor * (1f - progreso * 0.6f),
         cap = StrokeCap.Round,
      )

      drawCircle(
         color = Color.White.copy(alpha = 0.6f * alphaFugaz),
         radius = ef.grosor * 2f,
         center = posCabeza,
      )
   }
}

private fun DrawScope.dibujarVineta() {
   drawRect(
      brush =
         Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.4f)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.maxDimension * 0.85f,
         )
   )
}

private val Size.maxDimension: Float
   get() = max(width, height)

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEWS EXHAUSTIVAS - GalaxyBackground
// ═══════════════════════════════════════════════════════════════════════════════

class GalaxyConfigProvider : PreviewParameterProvider<GalaxyPreviewConfig> {
   override val values: Sequence<GalaxyPreviewConfig> =
      sequenceOf(
         GalaxyPreviewConfig(nombre = "Default", intensidad = 1f, velocidad = 1f),
         GalaxyPreviewConfig(nombre = "Sutil", intensidad = 0.5f, velocidad = 0.7f),
         GalaxyPreviewConfig(nombre = "Intenso", intensidad = 1.5f, velocidad = 1.3f),
         GalaxyPreviewConfig(nombre = "Minimalista", intensidad = 0.3f, velocidad = 0.5f),
      )
}

data class GalaxyPreviewConfig(val nombre: String, val intensidad: Float, val velocidad: Float)

@Preview(
   name = "📱 Full Screen - Pixel 7 Pro",
   group = "Dispositivos",
   device = Devices.PIXEL_7_PRO,
   showSystemUi = true,
)
@Composable
fun PreviewGalaxyFullScreen() {
   GalaxyBackground()
}

@Preview(
   name = "📱 Tablet - Pixel Tablet",
   group = "Dispositivos",
   device = Devices.PIXEL_TABLET,
   showSystemUi = true,
)
@Composable
fun PreviewGalaxyTablet() {
   GalaxyBackground()
}

@Preview(
   name = "📱 Compact - Pixel 4a",
   group = "Dispositivos",
   device = Devices.PIXEL_4A,
   showSystemUi = true,
)
@Composable
fun PreviewGalaxyCompact() {
   GalaxyBackground()
}

@Preview(
   name = "⚙️ Configuraciones",
   group = "Variantes",
   showBackground = true,
   backgroundColor = 0xFF000000,
   widthDp = 360,
   heightDp = 640,
)
@Composable
fun PreviewGalaxyConfigs(
   @PreviewParameter(GalaxyConfigProvider::class) config: GalaxyPreviewConfig
) {
   Column(
      modifier = Modifier.fillMaxSize().background(Color.Black),
      horizontalAlignment = Alignment.CenterHorizontally,
   ) {
      Surface(
         modifier = Modifier.padding(16.dp),
         color = Color.White.copy(alpha = 0.1f),
         shape = MaterialTheme.shapes.small,
      ) {
         Text(
            text = "Config: ${config.nombre}",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
         )
      }

      Box(modifier = Modifier.weight(1f)) {
         GalaxyBackground(
            intensidadNebulosas = config.intensidad,
            velocidadAnimacion = config.velocidad,
         )
      }
   }
}

@Preview(
   name = "📲 Grid Comparativo",
   group = "Análisis",
   showBackground = true,
   backgroundColor = 0xFF000000,
   widthDp = 400,
   heightDp = 800,
)
@Composable
fun PreviewGalaxyGrid() {
   Column(
      modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
   ) {
      listOf("Sutil (0.4x)" to 0.4f, "Normal (1.0x)" to 1f, "Intenso (1.5x)" to 1.5f).forEach {
         (label, intensidad) ->
         Column(modifier = Modifier.weight(1f)) {
            Text(
               text = label,
               color = Color.White.copy(alpha = 0.7f),
               style = MaterialTheme.typography.labelSmall,
               modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
            )
            Box(modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium)) {
               GalaxyBackground(intensidadNebulosas = intensidad)
            }
         }
      }
   }
}

@Preview(
   name = "🔄 Landscape",
   group = "Orientación",
   widthDp = 800,
   heightDp = 400,
   showBackground = true,
   backgroundColor = 0xFF000000,
)
@Composable
fun PreviewGalaxyLandscape() {
   GalaxyBackground()
}

@Preview(
   name = "⬜ Square (Widget)",
   group = "Formatos",
   widthDp = 300,
   heightDp = 300,
   showBackground = true,
   backgroundColor = 0xFF000000,
)
@Composable
fun PreviewGalaxySquare() {
   Box(modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.large)) { GalaxyBackground() }
}

@Preview(
   name = "♿ Font Scale Large",
   group = "Accesibilidad",
   fontScale = 1.5f,
   widthDp = 360,
   heightDp = 640,
)
@Composable
fun PreviewGalaxyFontScale() {
   GalaxyBackground()
}

@Preview(
   name = "🌙 Night Mode",
   group = "Temas",
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   widthDp = 360,
   heightDp = 640,
)
@Composable
fun PreviewGalaxyNightMode() {
   GalaxyBackground()
}

@Preview(
   name = "☀️ Day Mode",
   group = "Temas",
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   widthDp = 360,
   heightDp = 640,
)
@Composable
fun PreviewGalaxyDayMode() {
   GalaxyBackground()
}

@Preview(
   name = "🔧 Debug Overlay",
   group = "Desarrollo",
   widthDp = 360,
   heightDp = 640,
   showBackground = true,
   backgroundColor = 0xFF000000,
)
@Composable
fun PreviewGalaxyDebug() {
   val isInspection = LocalInspectionMode.current

   Box(modifier = Modifier.fillMaxSize()) {
      GalaxyBackground()

      if (isInspection) {
         Column(
            modifier =
               Modifier.align(Alignment.TopStart)
                  .padding(16.dp)
                  .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                  .padding(12.dp)
         ) {
            Text("🔍 Debug Info", color = Color.White, style = MaterialTheme.typography.labelLarge)
            Text(
               "Estrellas: ${GalaxyConfig.ESTRELLAS_FLOTANTES + GalaxyConfig.ESTRELLAS_DISTANTES}",
               color = Color.White.copy(alpha = 0.8f),
               style = MaterialTheme.typography.bodySmall,
            )
            Text(
               "Polvo: ${GalaxyConfig.PARTICULAS_POLVO}",
               color = Color.White.copy(alpha = 0.8f),
               style = MaterialTheme.typography.bodySmall,
            )
            Text(
               "Capas: 7",
               color = Color.White.copy(alpha = 0.8f),
               style = MaterialTheme.typography.bodySmall,
            )
            Text(
               "Zona Fade: ${(GalaxyConfig.ZONA_FADE_BORDE * 100).toInt()}%",
               color = Color.Cyan.copy(alpha = 0.8f),
               style = MaterialTheme.typography.bodySmall,
            )
         }
      }
   }
}

@Preview(name = "📋 Thumbnail", group = "Catálogo", widthDp = 150, heightDp = 280)
@Composable
fun PreviewGalaxyThumbnail() {
   Surface(
      modifier = Modifier.fillMaxSize(),
      shape = MaterialTheme.shapes.medium,
      shadowElevation = 8.dp,
   ) {
      GalaxyBackground()
   }
}
