package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista

/**
 * 🎵 VINILO GIRATORIO ANIMADO
 *
 * Características:
 * ✅ Animación solo cuando reproduce (optimizado)
 * ✅ Rotación infinita suave
 * ✅ Portada central con fallback
 * ✅ Tamaño configurable via modifier
 *
 * @param cancion Canción actual con metadata
 * @param estaReproduciendo True si debe girar
 * @param modifier Modifier para tamaño y posición
 */
@Composable
fun ViniloGiratorio(
    cancion: CancionConArtista,
    estaReproduciendo: Boolean,
    modifier: Modifier = Modifier
) {
    // Animación infinita optimizada
    val infiniteTransition = rememberInfiniteTransition(label = "viniloRotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Solo aplicar rotación si está reproduciendo
    val currentRotation = if (estaReproduciendo) rotation else 0f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Disco de vinilo
        Image(
            painter = painterResource(id = R.mipmap.img_disco_vinilo_foreground),
            contentDescription = "Disco de vinilo",
            modifier = Modifier
                .fillMaxSize()
                .rotate(currentRotation),
            contentScale = ContentScale.Fit
        )

        // Etiqueta central (portada o placeholder)
        Box(
            modifier = Modifier
                .fillMaxSize(0.28f)
                .clip(CircleShape)
                .background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center
        ) {
            if (!cancion.portadaPath.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cancion.portadaPath)
                        .crossfade(300)
                        .memoryCacheKey("vinilo_${cancion.cancion.idCancion}")
                        .build(),
                    contentDescription = "Portada ${cancion.cancion.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFD500F9),
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .padding(4.dp)
                )
            }
        }
    }
}