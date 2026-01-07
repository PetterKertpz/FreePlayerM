package com.example.freeplayerm.ui.features.library.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.shared.GlassCard
import com.example.freeplayerm.ui.features.library.domain.LibraryZoomConfig

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
      modifier = modifier.fillMaxWidth(),
   ) {
      // ✅ PORTADA - tamaño proporcional al contenedor
      BoxWithConstraints(
         modifier =
            Modifier.fillMaxHeight()
               .aspectRatio(1f)
               .clip(RoundedCornerShape(8.dp))
               .background(Color(0xFF2A0F35)),
         contentAlignment = Alignment.Center,
      ) {
         val portada = cancion.portadaPath

         when {
            portada.isNullOrBlank() -> {
               Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color(0xFFD500F9).copy(alpha = 0.7f),
                  modifier = Modifier.size(maxWidth * 0.5f),
               )
            }
            else -> {
               AsyncImage(
                  model =
                     ImageRequest.Builder(LocalContext.current)
                        .data(portada)
                        .crossfade(300)
                        .build(),
                  contentDescription = "Portada de ${cancion.titulo}",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize(),
                  placeholder = painterResource(id = R.drawable.ic_notification),
                  error = painterResource(id = R.drawable.ic_notification),
               )
            }
         }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // ✅ INFORMACIÓN DE LA CANCIÓN
      Column(
         modifier = Modifier.weight(1f).fillMaxHeight(),
         verticalArrangement = Arrangement.Center,
      ) {
         Text(
            text = cancion.cancion.titulo,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
         )
         Spacer(modifier = Modifier.height(2.dp))
         Text(
            text = cancion.artistaNombre ?: "Desconocido",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
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
// ==================== PREVIEWS ====================

@Composable
private fun SongItemPreviewWrapper(
   nivelZoom: NivelZoom = NivelZoom.NORMAL,
   content: @Composable () -> Unit
) {
   val altura = 72.dp * LibraryZoomConfig.factorEscalaLista(nivelZoom)
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .height(altura)
         .background(Color(0xFF050010))
         .padding(horizontal = 8.dp)
   ) {
      content()
   }
}

@Preview(name = "🔍 Zoom Pequeño", widthDp = 360)
@Composable
private fun PreviewSongZoomPequeno() {
   SongItemPreviewWrapper(NivelZoom.PEQUENO) {
      ItemCancion(
         cancion = SongProvider().localSong,
         esSeleccionado = false,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "📋 Zoom Normal", widthDp = 360)
@Composable
private fun PreviewSongZoomNormal() {
   SongItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemCancion(
         cancion = SongProvider().localSong,
         esSeleccionado = false,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "🔎 Zoom Grande", widthDp = 360)
@Composable
private fun PreviewSongZoomGrande() {
   SongItemPreviewWrapper(NivelZoom.GRANDE) {
      ItemCancion(
         cancion = SongProvider().localSong,
         esSeleccionado = false,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "✅ Seleccionado", widthDp = 360)
@Composable
private fun PreviewSongSeleccionado() {
   SongItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemCancion(
         cancion = SongProvider().localSong,
         esSeleccionado = true,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "❓ Sin Metadata", widthDp = 360)
@Composable
private fun PreviewSongSinMetadata() {
   SongItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemCancion(
         cancion = SongProvider().unknownSong,
         esSeleccionado = false,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "📝 Texto Largo", widthDp = 360)
@Composable
private fun PreviewSongTextoLargo() {
   SongItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemCancion(
         cancion = SongProvider().longTextSong,
         esSeleccionado = false,
         alClick = {},
         alClickMasOpciones = {},
      )
   }
}

@Preview(name = "📱 Lista Contexto (x3)", widthDp = 360, heightDp = 280)
@Composable
private fun PreviewSongListaContexto() {
   val altura = 72.dp * LibraryZoomConfig.factorEscalaLista(NivelZoom.NORMAL)
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(Color(0xFF050010))
         .padding(8.dp)
   ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Box(Modifier.height(altura)) {
            ItemCancion(
               cancion = SongProvider().localSong,
               esSeleccionado = false,
               alClick = {},
               alClickMasOpciones = {},
            )
         }
         Box(Modifier.height(altura)) {
            ItemCancion(
               cancion = SongProvider().streamingSong,
               esSeleccionado = true,
               alClick = {},
               alClickMasOpciones = {},
            )
         }
         Box(Modifier.height(altura)) {
            ItemCancion(
               cancion = SongProvider().unknownSong,
               esSeleccionado = false,
               alClick = {},
               alClickMasOpciones = {},
            )
         }
      }
   }
}