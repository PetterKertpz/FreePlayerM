package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.TabExpandido
import com.example.freeplayerm.ui.theme.AppColors

/**
 * 📺 PANEL EXPANDIDO (100%)
 *
 * Pantalla completa con toda la información:
 * - Header con botón colapsar
 * - Vinilo grande con glow
 * - Info completa (título, artista, álbum)
 * - Slider de progreso
 * - Controles completos
 * - Tabs (Letra, Info, Enlaces)
 *
 * @param cancion Canción actual
 * @param estado Estado del reproductor
 * @param onEvento Callback para eventos
 * @param onColapsar Callback para volver a modo normal
 */
@Composable
fun PanelExpandido(
    cancion: CancionConArtista,
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    onColapsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E0B24),
                        Color(0xFF0A0510),
                        Color.Black
                    )
                )
            )
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        HeaderExpandido(onColapsar = onColapsar)

        Spacer(modifier = Modifier.height(16.dp))

        // Vinilo con glow
        ViniloConGlow(
            cancion = cancion,
            estaReproduciendo = estado.estaReproduciendo,
            onClick = { /* Doble tap para like? */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info de la canción
        InfoCancionExpandida(
            cancion = cancion,
            esFavorita = estado.esFavorita,
            onToggleFavorito = {
                onEvento(ReproductorEvento.Configuracion.AlternarFavorito)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Slider de progreso
        SliderProgreso(estado, onEvento)

        Spacer(modifier = Modifier.height(16.dp))

        // Controles expandidos
        ControlesExpandidos(estado, onEvento)

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs (Letra, Info, Enlaces)
        TabsExpandido(
            estado = estado,
            onEvento = onEvento,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==================== COMPONENTES INTERNOS ====================

@Composable
private fun HeaderExpandido(onColapsar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Botón colapsar
        IconButton(
            onClick = onColapsar,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minimizar reproductor",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Título central
        Text(
            text = "REPRODUCIENDO",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ViniloConGlow(
    cancion: CancionConArtista,
    estaReproduciendo: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glow de fondo
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.AcentoRosa.copy(alpha = 0.25f),
                            AppColors.AcentoRosa.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Vinilo
        ViniloGiratorio(
            cancion = cancion,
            estaReproduciendo = estaReproduciendo,
            modifier = Modifier.size(280.dp)
        )
    }
}

@Composable
private fun InfoCancionExpandida(
    cancion: CancionConArtista,
    esFavorita: Boolean,
    onToggleFavorito: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = cancion.cancion.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Artista
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = cancion.artistaNombre ?: "Artista Desconocido",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.AcentoRosa,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Botón favorito
            BotonFavorito(
                esFavorita = esFavorita,
                onToggle = onToggleFavorito,
                modifier = Modifier.size(32.dp)
            )
        }

        // Álbum
        cancion.albumNombre?.let { album ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

