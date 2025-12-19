// ui/features/login/components/FondoGalaxia.kt
package com.example.freeplayerm.ui.features.login.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FondoGalaxiaAnimado() {
    val infiniteTransition = rememberInfiniteTransition(label = "animacion_fondo")

    // Animamos el desplazamiento del gradiente para simular movimiento nebular
    val offsetAnimado by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val coloresGalaxia = listOf(
        Color(0xFF0F0518), // Negro Morado Profundo
        Color(0xFF240A3A), // Morado Oscuro
        Color(0xFF420D69), // Morado Vibrante
        Color(0xFF1A0524)  // Vuelta al oscuro
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = coloresGalaxia,
                    center = Offset(x = offsetAnimado, y = offsetAnimado * 0.5f),
                    radius = 1500f
                )
            )
    )
}

@Preview
@Composable
fun PreviewFondo() {
    FondoGalaxiaAnimado()
}