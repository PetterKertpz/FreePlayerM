package com.example.freeplayerm.ui.features.biblioteca.components.items

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.features.biblioteca.components.items.shared.TarjetaCristal
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

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
fun ItemListaGalactico(
    lista: ListaReproduccionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaCristal(
        onClick = onClick,
        modifier = modifier
    ) {
        // ✅ PORTADA DE LA LISTA
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center
        ) {
            if (!lista.portadaUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(lista.portadaUrl)
                        .crossfade(300)
                        .memoryCacheKey(lista.portadaUrl)
                        .build(),
                    contentDescription = "Portada de ${lista.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.5f)
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
                fontSize = 16.sp
            )
            if (!lista.descripcion.isNullOrBlank()) {
                Text(
                    text = lista.descripcion,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Lista local",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

private object PlaylistItemMocks {
    val listaConPortada = ListaReproduccionEntity(
        idLista = 1,
        idUsuario = 1,
        nombre = "Workout Mix 2024",
        descripcion = "Las mejores canciones para entrenar",
        portadaUrl = "https://example.com/workout.jpg"
    )

    val listaSinPortada = ListaReproduccionEntity(
        idLista = 2,
        idUsuario = 1,
        nombre = "Road Trip Classics",
        descripcion = null,
        portadaUrl = null
    )

    val listaNombreLargo = ListaReproduccionEntity(
        idLista = 3,
        idUsuario = 1,
        nombre = "Playlist con un nombre extremadamente largo para probar truncado",
        descripcion = "Descripción también muy larga que debería truncarse correctamente",
        portadaUrl = null
    )
}

@Preview(name = "Light - Con portada y descripción", showBackground = true)
@Preview(name = "Dark - Con portada y descripción", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewListaConPortada() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemListaGalactico(
                lista = PlaylistItemMocks.listaConPortada,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Sin portada ni descripción", showBackground = true)
@Composable
private fun PreviewListaSinPortada() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemListaGalactico(
                lista = PlaylistItemMocks.listaSinPortada,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Nombre y descripción largos", showBackground = true)
@Composable
private fun PreviewListaNombreLargo() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemListaGalactico(
                lista = PlaylistItemMocks.listaNombreLargo,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Lista completa", showBackground = true, heightDp = 400)
@Composable
private fun PreviewListaCompleta() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ItemListaGalactico(
                    lista = PlaylistItemMocks.listaConPortada,
                    onClick = {}
                )
                ItemListaGalactico(
                    lista = PlaylistItemMocks.listaSinPortada,
                    onClick = {}
                )
                ItemListaGalactico(
                    lista = PlaylistItemMocks.listaNombreLargo,
                    onClick = {}
                )
            }
        }
    }
}