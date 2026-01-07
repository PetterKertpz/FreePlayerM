package com.example.freeplayerm.ui.features.library.components.items

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.library.components.items.shared.GlassCard

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
fun ItemCancion(
    cancion: SongWithArtist,
    esSeleccionado: Boolean,
    alClick: () -> Unit,
    alClickMasOpciones: () -> Unit,
    modifier: Modifier = Modifier,
    alLongClick: (() -> Unit)? = null,
) {
    GlassCard(
        onClick = alClick,
        seleccionado = esSeleccionado,
        onLongClick = alLongClick,
        modifier = modifier,
    ) {
        // ✅ PORTADA CON PLACEHOLDER
        Box(
            modifier =
                Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center,
        ) {
            if (cancion.portadaPath != null) {
                Log.d("PORTADA_DEBUG", "Cargando: ${cancion.portadaPath}")
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(cancion.portadaPath)
                            .crossfade(300)
                            .memoryCacheKey(cancion.portadaPath)
                            .listener(
                                onError = { _, result ->
                                    Log.e("PORTADA_DEBUG", "Error: ${result.throwable.message}")
                                },
                                onSuccess = { _, _ ->
                                    Log.d("PORTADA_DEBUG", "✅ Cargada exitosamente")
                                },
                            )
                            .build(),
                    contentDescription = "Portada de ${cancion.cancion.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.ic_notification),
                    error = painterResource(id = R.drawable.ic_notification),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.7f),
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
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cancion.artistaNombre ?: "Desconocido",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// --- FAKE DATA (Provider Robusto) ---

/** Provee escenarios ricos basados en la entidad real SongEntity/SongWithArtist. */
class SongProvider : PreviewParameterProvider<SongWithArtist> {

    // 1. Canción Local Estándar (High Quality)
    val localSong =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 1,
                    idArtista = 10,
                    idAlbum = 20,
                    idGenero = 5,
                    titulo = "Midnight City",
                    duracionSegundos = 243, // 4:03
                    origen = SongEntity.ORIGEN_LOCAL,
                    archivoPath = "/storage/emulated/0/Music/M83/MidnightCity.mp3",
                    calidadAudio = SongEntity.CALIDAD_HIGH, // High Quality
                    vecesReproducida = 15,
                    fechaAgregado = System.currentTimeMillis(),
                ),
            artistaNombre = "M83",
            albumNombre = "Hurry Up, We're Dreaming",
            generoNombre = "Synthpop",
            esFavorita = true, // Es favorita
            portadaPath = "fake_cover_path",
            fechaLanzamiento = "2011",
        )

    // 2. Canción Streaming (Sin archivo local)
    val streamingSong =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 2,
                    idArtista = 11,
                    idAlbum = 21,
                    idGenero = 6,
                    titulo = "Instant Crush",
                    duracionSegundos = 337,
                    origen = SongEntity.ORIGEN_STREAMING, // Origen Streaming
                    archivoPath = null, // Sin path local
                    urlStreaming = "https://api.freeplayer.com/stream/123",
                    calidadAudio = SongEntity.CALIDAD_LOSSLESS,
                    letraDisponible = true, // Tiene letra
                ),
            artistaNombre = "Daft Punk ft. Julian Casablancas",
            albumNombre = "Random Access Memories",
            generoNombre = "Disco",
            esFavorita = false,
            portadaPath = "fake_cover_url",
            fechaLanzamiento = "2013",
        )

    // 3. Metadatos Faltantes (Fallback)
    val unknownSong =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 3,
                    idArtista = null, // Null
                    idAlbum = null, // Null
                    idGenero = null, // Null
                    titulo = "Audio_Recording_2024.wav", // Nombre de archivo como título
                    duracionSegundos = 120,
                    origen = SongEntity.ORIGEN_LOCAL,
                    archivoPath = "/rec/audio.wav",
                ),
            artistaNombre = null, // Activará "Artista Desconocido"
            albumNombre = null,
            generoNombre = null,
            esFavorita = false,
            portadaPath = null, // Activará Icono Placeholder
        )

    // 4. Títulos Extremos (Stress Test)
    val longTextSong =
        SongWithArtist(
            cancion =
                SongEntity(
                    idCancion = 4,
                    idArtista = 99,
                    idAlbum = 99,
                    idGenero = 99,
                    titulo =
                        "Esta es una canción con un título extremadamente largo para verificar el comportamiento del truncado en la UI",
                    duracionSegundos = 200,
                    origen = SongEntity.ORIGEN_LOCAL,
                    archivoPath = "path",
                ),
            artistaNombre =
                "Orquesta Sinfónica de Londres & Varios Artistas Colaboradores Invitados Especiales",
            albumNombre = "Edición Especial Aniversario 50 Años Remasterizada",
            generoNombre = "Classical",
            esFavorita = false,
            portadaPath = null,
        )

    override val values = sequenceOf(localSong, streamingSong, unknownSong, longTextSong)
}

/** Wrapper consistente con el tema Galaxia Oscura. */
@Composable
private fun SongItemWrapper(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .background(Color(0xFF050010)) // Fondo base unificado
                .padding(16.dp)
    ) {
        content()
    }
}

// --- CASO 1: Iteración de Datos (Genera 4 Previews) ---
@Preview(name = "Escenarios de Datos", group = "Estados")
@Composable
fun PreviewSongVariations(@PreviewParameter(SongProvider::class) song: SongWithArtist) {
    SongItemWrapper {
        ItemCancion(cancion = song, esSeleccionado = false, alClick = {}, alClickMasOpciones = {})
    }
}

// --- CASO 2: Interacción (Selección) ---
@Preview(name = "Estado Seleccionado", group = "Interacción")
@Composable
fun PreviewSongSelected() {
    // Usamos el helper del companion object para crear data rápida
    val song =
        SongWithArtist.preview(
            titulo = "Canción Seleccionada",
            artista = "Artista Demo",
            esFavorita = true,
        )

    SongItemWrapper {
        ItemCancion(
            cancion = song,
            esSeleccionado = true, // <--- Highlight visual
            alClick = {},
            alClickMasOpciones = {},
        )
    }
}

// --- CASO 3: Contexto de Lista (Vertical) ---
@Preview(name = "Contexto Lista (x3)", group = "Layout", widthDp = 400)
@Composable
fun PreviewSongListContext() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050010)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Item 1: Normal
            ItemCancion(
                cancion = SongWithArtist.preview("Track 01", "Artist A"),
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {},
            )
            // Item 2: Seleccionado
            ItemCancion(
                cancion = SongWithArtist.preview("Track 02", "Artist B"),
                esSeleccionado = true,
                alClick = {},
                alClickMasOpciones = {},
            )
            // Item 3: Streaming/Remoto
            ItemCancion(
                cancion = SongWithArtist.preview("Track 03", "Artist C"),
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {},
            )
        }
    }
}
