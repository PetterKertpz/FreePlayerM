package com.example.freeplayerm.ui.features.biblioteca.components.items

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
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
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.components.items.shared.TarjetaCristal
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎵 ITEM DE CANCIÓN PARA LISTAS
 *
 * Componente optimizado para mostrar canciones en LazyColumn.
 *
 * Características:
 * - Portada con placeholder
 * - Título y artista
 * - Botón de opciones
 * - Estado de selección
 * - Gestos: click y long press
 */
@Composable
fun ItemCancionGalactico(
    cancion: CancionConArtista,
    esSeleccionado: Boolean,
    alClick: () -> Unit,
    alClickMasOpciones: () -> Unit,
    modifier: Modifier = Modifier,
    alLongClick: (() -> Unit)? = null
) {
    TarjetaCristal(
        onClick = alClick,
        seleccionado = esSeleccionado,
        onLongClick = alLongClick,
        modifier = modifier
    ) {
        // ✅ PORTADA CON PLACEHOLDER
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center
        ) {
            if (cancion.portadaPath != null) {
                Log.d("PORTADA_DEBUG", "Cargando: ${cancion.portadaPath}")
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cancion.portadaPath)
                        .crossfade(300)
                        .memoryCacheKey(cancion.portadaPath)
                        .listener(
                            onError = { _, result ->
                                Log.e("PORTADA_DEBUG", "Error: ${result.throwable.message}")
                            },
                            onSuccess = { _, _ ->
                                Log.d("PORTADA_DEBUG", "✅ Cargada exitosamente")
                            }
                        )
                        .build(),
                    contentDescription = "Portada de ${cancion.cancion.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.ic_notification),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // ✅ INFORMACIÓN DE LA CANCIÓN
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cancion.cancion.titulo,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cancion.artistaNombre ?: "Desconocido",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ✅ BOTÓN DE OPCIONES
        IconButton(onClick = alClickMasOpciones) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

// ==================== PREVIEWS ====================

private object SongItemMocks {
    val cancionBase = CancionEntity(
        idCancion = 1,
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        titulo = "Midnight City",
        duracionSegundos = 240,
        origen = "LOCAL",
        archivoPath = null
    )

    val cancionConArtista = CancionConArtista(
        cancion = cancionBase,
        artistaNombre = "M83",
        albumNombre = "Hurry Up, We're Dreaming",
        generoNombre = "Synthpop",
        esFavorita = true,
        portadaPath = null,
        fechaLanzamiento = null
    )
}

@Preview(name = "Light - Normal", showBackground = true)
@Preview(name = "Dark - Normal", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewItemCancionNormal() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemCancionGalactico(
                cancion = SongItemMocks.cancionConArtista,
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {}
            )
        }
    }
}

@Preview(name = "Seleccionado", showBackground = true)
@Composable
private fun PreviewItemCancionSeleccionado() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemCancionGalactico(
                cancion = SongItemMocks.cancionConArtista,
                esSeleccionado = true,
                alClick = {},
                alClickMasOpciones = {}
            )
        }
    }
}

@Preview(name = "Título largo", showBackground = true)
@Composable
private fun PreviewItemCancionTituloLargo() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemCancionGalactico(
                cancion = SongItemMocks.cancionConArtista.copy(
                    cancion = SongItemMocks.cancionBase.copy(
                        titulo = "Esta es una canción con un título extremadamente largo que debe truncarse"
                    ),
                    artistaNombre = "Artista con nombre muy largo también"
                ),
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {}
            )
        }
    }
}