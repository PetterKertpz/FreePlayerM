package com.example.freeplayerm.ui.features.library.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== CONSTANTES DE DISEÑO ====================

private object GalacticDefaults {
    val NeonPrimary = Color(0xFFD500F9)
    val NeonSecondary = Color(0xFF7C4DFF)
    val GlassBackgroundTop = Color(0xFF2A0F35).copy(alpha = 0.7f) // Púrpura muy oscuro
    val GlassBackgroundBottom = Color(0xFF0F0518).copy(alpha = 0.9f) // Casi negro

    // Gradiente del borde para efecto de iluminación
    val BorderGradient =
        Brush.linearGradient(
            colors =
                listOf(
                    Color.White.copy(alpha = 0.3f),
                    NeonPrimary.copy(alpha = 0.5f),
                    Color.Transparent,
                ),
            tileMode = TileMode.Clamp,
        )
}

// ==================== COMPONENTES DEL SISTEMA ====================

/**
 * 🪟 CONTENEDOR GLASS "GALÁCTICO"
 *
 * Contenedor base para tarjetas, listas y paneles. Efecto de vidrio ahumado con tinte púrpura y
 * bordes iluminados.
 */
@Composable
fun LibraryDesignSystem(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors =
                listOf(GalacticDefaults.GlassBackgroundTop, GalacticDefaults.GlassBackgroundBottom)
        )
    }

    Box(
        modifier =
            modifier
                .clip(shape)
                .background(backgroundBrush)
                .border(width = 1.dp, brush = GalacticDefaults.BorderGradient, shape = shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Sin ripple para mantener el look limpio
                            onClick = onClick,
                        )
                    } else Modifier
                )
    ) {
        content()
    }
}

/**
 * 🌌 TRANSICIÓN DE CONTENIDO
 *
 * Maneja el cambio suave entre pestañas (Canciones <-> Álbumes <-> Listas). Usa una combinación de
 * Fade + Scale sutil.
 */
@Composable
fun <T> TransicionDeContenidoBiblioteca(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(400)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(400))) togetherWith
                (fadeOut(animationSpec = tween(300)))
        },
        label = "transicion_biblioteca",
        modifier = modifier,
    ) { state ->
        content(state)
    }
}

// ==================== PREVIEWS & DEMOS ====================

@Preview(
    name = "1. Tarjeta Glass Galáctica",
    showBackground = true,
    backgroundColor = 0xFF050510, // Fondo muy oscuro para ver el efecto
)
@Composable
private fun LibraryDesignSystemPreview() {
    FreePlayerMTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            LibraryDesignSystem(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GalacticDefaults.NeonPrimary,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Estética Galáctica",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vidrio ahumado con bordes de neón",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "2. Demo Transición Interactiva",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun TransicionPreview() {
    var estadoActual by remember { mutableStateOf(0) }
    val colores =
        listOf(
            Color(0xFFD500F9), // Neon Pink
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFFEA00), // Yellow
        )

    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Toca la tarjeta para transicionar",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Contenedor interactivo
            LibraryDesignSystem(
                modifier = Modifier.size(280.dp),
                onClick = { estadoActual = (estadoActual + 1) % 3 },
            ) {
                TransicionDeContenidoBiblioteca(
                    targetState = estadoActual,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Círculo de color que cambia
                            Box(
                                modifier =
                                    Modifier.size(80.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(colores[page])
                                        .border(2.dp, Color.White, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "VISTA ${page + 1}",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
