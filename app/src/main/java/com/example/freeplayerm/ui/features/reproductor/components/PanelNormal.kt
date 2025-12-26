package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento

/**
 * 🎵 PANEL NORMAL (25-30%)
 *
 * Estado por defecto mientras hay música reproduciéndose.
 * Muestra:
 * - Vinilo giratorio (clickeable para expandir)
 * - Título y artista
 * - Controles básicos (play/pause, siguiente)
 * - Slider de progreso compacto
 *
 * @param cancion Canción actual
 * @param estado Estado del reproductor
 * @param onEvento Callback para eventos
 * @param onExpandir Callback para ir a modo expandido
 */
@Composable
fun PanelNormal(
    cancion: CancionConArtista,
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    onExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E1E).copy(alpha = 0.95f),
                        Color(0xFF2D1B36).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vinilo clickeable
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onExpandir() }
            ) {
                ViniloGiratorio(
                    cancion = cancion,
                    estaReproduciendo = estado.estaReproduciendo,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info canción
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onExpandir)
            ) {
                Text(
                    text = cancion.cancion.titulo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cancion.artistaNombre ?: "Desconocido",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controles
            ControlesNormales(estado, onEvento)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slider compacto
        SliderProgresoCompacto(estado, onEvento)
    }
}
