package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.formatearTiempo
import com.example.freeplayerm.ui.theme.AppColors

/**
 * 📱 PANEL MINIMIZADO (15%)
 *
 * Visible durante scroll activo en las listas.
 * Muestra información mínima:
 * - Título + Artista (marquee)
 * - Tiempo actual / total
 * - Barra de progreso
 *
 * @param cancion Canción actual
 * @param estado Estado del reproductor
 * @param onExpandir Callback para volver al modo normal
 */
@Composable
fun PanelMinimizado(
    cancion: CancionConArtista,
    estado: ReproductorEstado,
    onExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A).copy(alpha = 0.98f),
                        Color(0xFF2D1B36).copy(alpha = 0.98f)
                    )
                )
            )
            .clickable(onClick = onExpandir)
    ) {
        // Barra de progreso inferior
        if (estado.duracionTotalMs > 0) {
            LinearProgressIndicator(
                progress = { estado.progresoPorcentaje },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(3.dp),
                color = AppColors.AcentoRosa,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de reproducción
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (estado.estaReproduciendo) AppColors.AcentoRosa
                        else Color.White.copy(alpha = 0.3f)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Título y artista con marquee
            Text(
                text = "${cancion.cancion.titulo} • ${cancion.artistaNombre ?: "Desconocido"}",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Tiempo
            Text(
                text = "${estado.progresoVisibleMs.formatearTiempo()} / ${estado.duracionTotalMs.formatearTiempo()}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

