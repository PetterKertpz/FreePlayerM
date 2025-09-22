package com.example.freeplayerm.ui.features.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun IconoCorazonAnimado(
    modifier: Modifier = Modifier,
    esFavorito: Boolean
) {
    // 1. Animación de color
    // Anima el color del tinte entre 'onSurfaceVariant' (para no favorito) y RojoMedio (para favorito).
    val colorAnimado by animateColorAsState(
        targetValue = if (esFavorito) AppColors.RojoMedio else LocalContentColor.current,
        animationSpec = tween(durationMillis = 300), // Duración de la animación de color
        label = "color_favorito"
    )

    // 2. Animación de escala (tamaño)
    // Anima la escala del icono. Será 1.3x su tamaño por un momento al marcarse como favorito.
    val escalaAnimada by animateFloatAsState(
        targetValue = if (esFavorito) 1.3f else 1.0f,
        animationSpec = tween(durationMillis = 200), // Duración de la animación de escala
        label = "escala_favorito"
    )

    // 3. El icono que se dibujará
    Icon(
        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.Favorite,
        contentDescription = if (esFavorito) "Quitar de favoritos" else "Añadir a favoritos",
        modifier = modifier
            .scale(escalaAnimada), // Aplicamos la animación de escala
        tint = colorAnimado // Aplicamos la animación de color
    )
}