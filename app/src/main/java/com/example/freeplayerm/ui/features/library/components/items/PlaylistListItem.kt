package com.example.freeplayerm.ui.features.library.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.ui.features.library.components.items.shared.GlassCard

/**
 * 📋 ITEM DE LISTA DE REPRODUCCIÓN
 *
 * Componente para mostrar playlists en LazyColumn.
 *
 * Características:
 * - Portada o icono por defecto
 * - Nombre y descripción
 * - Click handler
 */
@Composable
fun ItemLista(lista: PlaylistEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(onClick = onClick, modifier = modifier) {
        // ✅ PORTADA DE LA LISTA
        Box(
            modifier =
                Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center,
        ) {
            if (!lista.portadaUrl.isNullOrEmpty()) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(lista.portadaUrl)
                            .crossfade(300)
                            .memoryCacheKey(lista.portadaUrl)
                            .build(),
                    contentDescription = "Portada de ${lista.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_notification),
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.5f),
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // ✅ INFORMACIÓN DE LA LISTA
        Column {
            Text(
                text = lista.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            if (!lista.descripcion.isNullOrBlank()) {
                Text(
                    text = lista.descripcion,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(text = "Lista local", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// PREVIEWS (Componente Aislado)
// ==========================================

// --- FAKE DATA (Provider) ---
/** Provee los 3 estados fundamentales de una Playlist en la UI. */
class PlaylistProvider : PreviewParameterProvider<PlaylistEntity> {
    override val values =
        sequenceOf(
            // 1. Estándar (Ideal)
            PlaylistEntity(
                idLista = 1,
                idUsuario = 1,
                nombre = "Workout 2024",
                descripcion = "High energy beats",
                portadaUrl = "fake_url",
            ),
            // 2. Sin Portada (Fallback Icono + Texto "Lista local")
            PlaylistEntity(
                idLista = 2,
                idUsuario = 1,
                nombre = "Mis Favoritos",
                descripcion = null, // Esto activa el texto "Lista local"
                portadaUrl = null, // Esto activa el icono musical
            ),
            // 3. Texto Largo (Stress Test)
            PlaylistEntity(
                idLista = 3,
                idUsuario = 1,
                nombre = "Playlist con nombre extremadamente largo para testear",
                descripcion =
                    "Descripción muy detallada que debería cortarse con puntos suspensivos al final de la línea",
                portadaUrl = null,
            ),
        )
}

/** Wrapper consistente con el tema Galaxia Oscura. */
@Composable
private fun PlaylistWrapper(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxWidth() // Simula el ancho de pantalla
                .background(Color(0xFF050010)) // Fondo base unificado
                .padding(16.dp)
    ) {
        content()
    }
}

// --- CASO 1: Iteración de Estados (Individual) ---
@Preview(name = "Variaciones de Item", group = "Estados")
@Composable
fun PreviewPlaylistItem(@PreviewParameter(PlaylistProvider::class) playlist: PlaylistEntity) {
    PlaylistWrapper { ItemLista(lista = playlist, onClick = {}) }
}

// --- CASO 2: Contexto de Lista (Vertical) ---
// Simula una LazyColumn visualmente
@Preview(name = "Contexto Lista (x3)", group = "Layout", widthDp = 400)
@Composable
fun PreviewPlaylistListContext() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050010)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ItemLista(lista = PlaylistEntity(1, 1, "Gym Hits", "Energy", null), onClick = {})
            ItemLista(lista = PlaylistEntity(2, 1, "Sleep Sounds", null, null), onClick = {})
            ItemLista(
                lista = PlaylistEntity(3, 1, "Road Trip", "California vibes", "url"),
                onClick = {},
            )
        }
    }
}
