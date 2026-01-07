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
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.shared.GlassCard
import com.example.freeplayerm.ui.features.library.domain.LibraryZoomConfig
import com.example.freeplayerm.utils.mediaStoreImageRequest

@Composable
fun ItemLista(lista: PlaylistEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
   GlassCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
      // ✅ PORTADA - tamaño proporcional
      BoxWithConstraints(
         modifier =
            Modifier.fillMaxHeight()
               .aspectRatio(1f)
               .clip(RoundedCornerShape(8.dp))
               .background(Color(0xFF2A0F35)),
         contentAlignment = Alignment.Center,
      ) {
         if (!lista.portadaUrl.isNullOrEmpty()) {
            AsyncImage(
               model = LocalContext.current.mediaStoreImageRequest(lista.portadaUrl),
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
               modifier = Modifier.size(maxWidth * 0.5f),
            )
         }
      }

      Spacer(Modifier.width(12.dp))

      // ✅ INFORMACIÓN DE LA LISTA
      Column(
         modifier = Modifier.weight(1f).fillMaxHeight(),
         verticalArrangement = Arrangement.Center,
      ) {
         Text(
            text = lista.nombre,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
         )
         Spacer(modifier = Modifier.height(2.dp))
         Text(
            text = lista.descripcion?.takeIf { it.isNotBlank() } ?: "Lista local",
            color = Color.White.copy(alpha = if (lista.descripcion.isNullOrBlank()) 0.4f else 0.6f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
         )
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
// ==================== PREVIEWS ====================

@Composable
private fun PlaylistItemPreviewWrapper(
   nivelZoom: NivelZoom = NivelZoom.NORMAL,
   content: @Composable () -> Unit
) {
   val altura = 80.dp * LibraryZoomConfig.factorEscalaLista(nivelZoom)
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
private fun PreviewPlaylistZoomPequeno() {
   val lista = PlaylistEntity(1, 1, "Workout 2024", "High energy beats", "fake_url")
   PlaylistItemPreviewWrapper(NivelZoom.PEQUENO) {
      ItemLista(lista = lista, onClick = {})
   }
}

@Preview(name = "📋 Zoom Normal", widthDp = 360)
@Composable
private fun PreviewPlaylistZoomNormal() {
   val lista = PlaylistEntity(1, 1, "Workout 2024", "High energy beats", "fake_url")
   PlaylistItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemLista(lista = lista, onClick = {})
   }
}

@Preview(name = "🔎 Zoom Grande", widthDp = 360)
@Composable
private fun PreviewPlaylistZoomGrande() {
   val lista = PlaylistEntity(1, 1, "Workout 2024", "High energy beats", "fake_url")
   PlaylistItemPreviewWrapper(NivelZoom.GRANDE) {
      ItemLista(lista = lista, onClick = {})
   }
}

@Preview(name = "🎵 Sin Portada", widthDp = 360)
@Composable
private fun PreviewPlaylistSinPortada() {
   val lista = PlaylistEntity(2, 1, "Mis Favoritos", null, null)
   PlaylistItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemLista(lista = lista, onClick = {})
   }
}

@Preview(name = "📝 Texto Largo", widthDp = 360)
@Composable
private fun PreviewPlaylistTextoLargo() {
   val lista = PlaylistEntity(
      3, 1,
      "Playlist con nombre extremadamente largo",
      "Descripción muy detallada que debería cortarse",
      null
   )
   PlaylistItemPreviewWrapper(NivelZoom.NORMAL) {
      ItemLista(lista = lista, onClick = {})
   }
}

@Preview(name = "📱 Lista Contexto (x3)", widthDp = 360, heightDp = 320)
@Composable
private fun PreviewPlaylistListaContexto() {
   val altura = 80.dp * LibraryZoomConfig.factorEscalaLista(NivelZoom.NORMAL)
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(Color(0xFF050010))
         .padding(8.dp)
   ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
         Box(Modifier.height(altura)) {
            ItemLista(
               lista = PlaylistEntity(1, 1, "Gym Hits", "Energy", null),
               onClick = {}
            )
         }
         Box(Modifier.height(altura)) {
            ItemLista(
               lista = PlaylistEntity(2, 1, "Sleep Sounds", null, null),
               onClick = {}
            )
         }
         Box(Modifier.height(altura)) {
            ItemLista(
               lista = PlaylistEntity(3, 1, "Road Trip", "California vibes", "url"),
               onClick = {}
            )
         }
      }
   }
}