package com.example.freeplayerm.ui.features.biblioteca.components.items

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎤 ITEM DE ARTISTA PARA GRID
 *
 * Componente que muestra artistas en formato circular.
 *
 * Características:
 * - Avatar circular con borde degradado
 * - Generación automática de avatar si no tiene foto
 * - Nombre del artista
 * - Click handler
 */
@Composable
fun ItemArtistaGalactico(
    artista: ArtistaEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ AVATAR CIRCULAR
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD500F9),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            val urlFoto = artista.imageUrl ?: generarUrlAvatar(artista.nombre)

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urlFoto)
                    .crossfade(300)
                    .memoryCacheKey(artista.nombre)
                    .build(),
                contentDescription = "Foto de ${artista.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        // ✅ NOMBRE DEL ARTISTA
        Text(
            text = artista.nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Genera URL de avatar usando UI Avatars API
 */
private fun generarUrlAvatar(nombre: String): String {
    return "https://ui-avatars.com/api/" +
            "?name=${nombre.replace(" ", "+")}" +
            "&background=random" +
            "&color=fff" +
            "&size=256"
}

// ==================== PREVIEWS ====================

private object ArtistaItemMocks {
    val artistaConFoto = ArtistaEntity(
        idArtista = 1,
        nombre = "Daft Punk",
        paisOrigen = "France",
        descripcion = "Legendary Duo",
        imageUrl = "https://example.com/daftpunk.jpg"
    )

    val artistaSinFoto = ArtistaEntity(
        idArtista = 2,
        nombre = "Arctic Monkeys",
        paisOrigen = "UK",
        descripcion = null,
        imageUrl = null
    )

    val artistaNombreLargo = ArtistaEntity(
        idArtista = 3,
        nombre = "Nombre de Artista Muy Largo Que Debe Truncarse",
        paisOrigen = null,
        descripcion = null,
        imageUrl = null
    )
}

@Preview(name = "Light - Con foto", showBackground = true)
@Preview(name = "Dark - Con foto", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewArtistaConFoto() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemArtistaGalactico(
                artista = ArtistaItemMocks.artistaConFoto,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Sin foto (avatar generado)", showBackground = true)
@Composable
private fun PreviewArtistaSinFoto() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemArtistaGalactico(
                artista = ArtistaItemMocks.artistaSinFoto,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Nombre largo", showBackground = true)
@Composable
private fun PreviewArtistaNombreLargo() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemArtistaGalactico(
                artista = ArtistaItemMocks.artistaNombreLargo,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Grid completo", showBackground = true, widthDp = 400)
@Composable
private fun PreviewGridArtistas() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ItemArtistaGalactico(
                    artista = ArtistaItemMocks.artistaConFoto,
                    onClick = {}
                )
                ItemArtistaGalactico(
                    artista = ArtistaItemMocks.artistaSinFoto,
                    onClick = {}
                )
            }
        }
    }
}