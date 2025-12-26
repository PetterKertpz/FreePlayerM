package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.TabExpandido
import com.example.freeplayerm.ui.theme.AppColors

/**
 * 📑 TABS DEL MODO EXPANDIDO
 *
 * Permite navegar entre:
 * - Letra de la canción
 * - Info del artista/álbum
 * - Enlaces externos (Genius, YouTube, Google)
 */
@Composable
fun TabsExpandido(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Selector de tabs
        TabSelector(
            tabActivo = estado.tabExpandidoActivo,
            onTabSelected = { tab ->
                onEvento(ReproductorEvento.Panel.CambiarTab(tab))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido del tab activo
        AnimatedContent(
            targetState = estado.tabExpandidoActivo,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                TabExpandido.LETRA -> TabLetraContent(estado)
                TabExpandido.INFO -> TabInfoContent(estado)
                TabExpandido.ENLACES -> TabEnlacesContent(estado, onEvento)
            }
        }
    }
}

@Composable
private fun TabSelector(
    tabActivo: TabExpandido,
    onTabSelected: (TabExpandido) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabExpandido.entries.forEach { tab ->
            TabItem(
                tab = tab,
                isSelected = tab == tabActivo,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: TabExpandido,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (tab) {
        TabExpandido.LETRA -> Icons.AutoMirrored.Filled.Article
        TabExpandido.INFO -> Icons.Default.Info
        TabExpandido.ENLACES -> Icons.Default.Link
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) AppColors.AcentoRosa.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tab.titulo,
                tint = if (isSelected) AppColors.AcentoRosa else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tab.titulo,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ==================== CONTENIDO DE TABS ====================

@Composable
private fun TabLetraContent(estado: ReproductorEstado) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp, max = 300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
    ) {
        if (estado.cargandoLetra) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AppColors.AcentoRosa,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cargando letra...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Text(
                text = estado.letra ?: "Letra no disponible",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun TabInfoContent(estado: ReproductorEstado) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp, max = 300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
    ) {
        if (estado.cargandoInfo) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AppColors.AcentoRosa,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cargando información...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Info del artista
                Text(
                    text = estado.infoArtista ?: "Información no disponible",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                // Info adicional del álbum si existe
                estado.cancionActual?.let { cancion ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(
                        icon = Icons.Default.Album,
                        label = "Álbum",
                        value = cancion.albumNombre ?: "Desconocido"
                    )

                    cancion.generoNombre?.let { genero ->
                        InfoRow(
                            icon = Icons.Default.MusicNote,
                            label = "Género",
                            value = genero
                        )
                    }

                    cancion.fechaLanzamiento?.let { fecha ->
                        InfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Año",
                            value = fecha.take(4)
                        )
                    }

                    InfoRow(
                        icon = Icons.Default.Timer,
                        label = "Duración",
                        value = formatDuration(cancion.cancion.duracionSegundos)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.AcentoRosa.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TabEnlacesContent(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Más sobre esta canción",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        // Genius
        EnlaceButton(
            icon = Icons.Default.Lyrics,
            titulo = "Ver en Genius",
            subtitulo = "Letra y anotaciones",
            color = Color(0xFFFFFF64),
            habilitado = !estado.enlaceGenius.isNullOrBlank(),
            onClick = { onEvento(ReproductorEvento.Enlaces.AbrirGenius) }
        )

        // YouTube
        EnlaceButton(
            icon = Icons.Default.PlayCircle,
            titulo = "Buscar en YouTube",
            subtitulo = "Videos y conciertos",
            color = Color(0xFFFF0000),
            habilitado = !estado.enlaceYoutube.isNullOrBlank(),
            onClick = { onEvento(ReproductorEvento.Enlaces.AbrirYoutube) }
        )

        // Google
        EnlaceButton(
            icon = Icons.Default.Search,
            titulo = "Buscar artista",
            subtitulo = "Google Search",
            color = Color(0xFF4285F4),
            habilitado = !estado.enlaceGoogle.isNullOrBlank(),
            onClick = { onEvento(ReproductorEvento.Enlaces.AbrirGoogle) }
        )
    }
}

@Composable
private fun EnlaceButton(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    color: Color,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (habilitado) color.copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.02f)
            )
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (habilitado) color.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.05f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (habilitado) color else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = if (habilitado) Color.White else Color.White.copy(alpha = 0.3f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitulo,
                color = if (habilitado) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
                fontSize = 12.sp
            )
        }

        if (habilitado) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper
private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
