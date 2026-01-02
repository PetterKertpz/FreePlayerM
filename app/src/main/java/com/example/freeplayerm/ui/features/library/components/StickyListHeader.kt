package com.example.freeplayerm.ui.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== COLORES LOCALES ====================

private object HeaderColors {
    val NeonPrimary = Color(0xFFD500F9)
    val NeonCyan = Color(0xFF00E5FF)
    val NeonDanger = Color(0xFFFF1744)
    val BackgroundDark = Color(0xFF0F0518).copy(alpha = 0.98f) // Casi opaco para tapar el scroll
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.6f)

    // Línea separadora inferior con gradiente
    val BottomDivider =
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, NeonPrimary.copy(alpha = 0.5f), Color.Transparent)
        )
}

// ==================== COMPONENTE PRINCIPAL ====================

/**
 * 📌 ENCABEZADO FIJO (STICKY HEADER)
 *
 * Se mantiene en la parte superior de la lista cuando el usuario hace scroll. Diseño compacto con
 * fondo oscuro para bloquear el contenido tras él.
 */
@Composable
fun StickyListHeader(
    lista: PlaylistEntity?,
    onVolverClick: () -> Unit,
    onEliminarListaClick: () -> Unit,
    onEditarListaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (lista == null) return

    Box(modifier = modifier.fillMaxWidth().background(HeaderColors.BackgroundDark)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 🔙 Botón Volver
            IconButton(onClick = onVolverClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = HeaderColors.TextPrimary,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 🖼️ Miniatura de la Lista
            Box(
                modifier =
                    Modifier.size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center,
            ) {
                if (lista.portadaUrl != null) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(LocalContext.current)
                                .data(lista.portadaUrl)
                                .crossfade(true)
                                .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 📝 Info de Texto
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = lista.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HeaderColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val descripcion = lista.descripcion
                if (!descripcion.isNullOrBlank()) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = HeaderColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // ✏️ Botón Editar
            IconButton(onClick = onEditarListaClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = HeaderColors.NeonCyan,
                    modifier = Modifier.size(20.dp),
                )
            }

            // 🗑️ Botón Eliminar
            IconButton(onClick = onEliminarListaClick) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Eliminar",
                    tint = HeaderColors.NeonDanger,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // ➖ Línea Separadora Neón (Abajo)
        Box(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HeaderColors.BottomDivider)
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(name = "Sticky Header - Normal")
@Composable
private fun PreviewStickyHeader() {
    FreePlayerMTheme(darkTheme = true) {
        StickyListHeader(
            lista =
                PlaylistEntity(
                    idLista = 1,
                    idUsuario = 1,
                    nombre = "Gym Motivation 2024",
                    descripcion = "Para romperla entrenando 💪",
                    portadaUrl = "https://example.com/cover.jpg",
                ),
            onVolverClick = {},
            onEliminarListaClick = {},
            onEditarListaClick = {},
        )
    }
}

@Preview(name = "Sticky Header - Texto Largo")
@Composable
private fun PreviewStickyHeaderLongText() {
    FreePlayerMTheme(darkTheme = true) {
        StickyListHeader(
            lista =
                PlaylistEntity(
                    idLista = 2,
                    idUsuario = 1,
                    nombre =
                        "Esta es una lista con un nombre extremadamente largo que debería cortarse",
                    descripcion =
                        "Y esta descripción también es kilométrica para probar el overflow visual",
                    portadaUrl = null,
                ),
            onVolverClick = {},
            onEliminarListaClick = {},
            onEditarListaClick = {},
        )
    }
}
