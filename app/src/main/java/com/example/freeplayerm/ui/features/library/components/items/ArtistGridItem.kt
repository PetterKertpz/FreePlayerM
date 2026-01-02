package com.example.freeplayerm.ui.features.library.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.example.freeplayerm.data.local.entity.ArtistEntity

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
fun ItemArtista(artista: ArtistEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .width(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ✅ AVATAR CIRCULAR
        Box(
            modifier =
                Modifier.size(120.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        Brush.verticalGradient(listOf(Color(0xFFD500F9), Color.Transparent)),
                        CircleShape,
                    )
                    .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            val urlFoto = artista.imageUrl ?: generarUrlAvatar(artista.nombre)

            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(urlFoto)
                        .crossfade(300)
                        .memoryCacheKey(artista.nombre)
                        .build(),
                contentDescription = "Foto de ${artista.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
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
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Genera URL de avatar usando UI Avatars API */
private fun generarUrlAvatar(nombre: String): String {
    return "https://ui-avatars.com/api/" +
        "?name=${nombre.replace(" ", "+")}" +
        "&background=random" +
        "&color=fff" +
        "&size=256"
}

// ==========================================
// PREVIEWS (Componente Aislado)
// ==========================================

// --- FAKE DATA ---
private object ArtistMocks {
    val conFoto =
        ArtistEntity(
            idArtista = 1,
            nombre = "Daft Punk",
            paisOrigen = "France",
            descripcion = "Electronic Duo",
            imageUrl = "https://path.to.image", // URL simulada
        )

    val sinFoto =
        ArtistEntity(
            idArtista = 2,
            nombre = "Arctic Monkeys",
            paisOrigen = "UK",
            descripcion = null,
            imageUrl = null, // Esto activará el generador de UI Avatars
        )

    val nombreLargo =
        ArtistEntity(
            idArtista = 3,
            nombre = "The Philharmonic Orchestra of London",
            paisOrigen = "UK",
            descripcion = null,
            imageUrl = null,
        )
}

/**
 * Wrapper consistente con el tema Galaxia Oscura. Permite visualizar el borde degradado y el texto
 * blanco correctamente.
 */
@Composable
private fun ArtistItemWrapper(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.wrapContentSize()
                .background(Color(0xFF050010)) // Fondo base unificado
                .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// --- CASO 1: Artista Estándar (Ideal) ---
@Preview(name = "1. Artista - Con Foto", group = "Estados")
@Composable
fun PreviewArtistStandard() {
    ArtistItemWrapper { ItemArtista(artista = ArtistMocks.conFoto, onClick = {}) }
}

// --- CASO 2: Sin Foto (Avatar Generado) ---
// Valida que el degradado se vea bien sobre el fondo semitransparente
@Preview(name = "2. Artista - Avatar Generado", group = "Estados")
@Composable
fun PreviewArtistGenerated() {
    ArtistItemWrapper { ItemArtista(artista = ArtistMocks.sinFoto, onClick = {}) }
}

// --- CASO 3: Nombre Largo (Stress Test) ---
// Verifica el 'ellipsis' (...) al final del nombre
@Preview(name = "3. Artista - Texto Largo", group = "Estados")
@Composable
fun PreviewArtistLongText() {
    ArtistItemWrapper { ItemArtista(artista = ArtistMocks.nombreLargo, onClick = {}) }
}

// --- CASO 4: Grid Context (Alineación) ---
// Verifica el espaciado entre dos elementos circulares
@Preview(name = "4. Contexto Grid (x2)", group = "Layout", widthDp = 360)
@Composable
fun PreviewArtistGridRow() {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF050010)).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.wrapContentWidth(),
        ) {
            ItemArtista(artista = ArtistMocks.conFoto, onClick = {})
            ItemArtista(artista = ArtistMocks.sinFoto, onClick = {})
        }
    }
}
