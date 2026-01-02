package com.example.freeplayerm.ui.features.library.components.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.ui.features.player.components.SpinningVinyl

/**
 * 💿 ITEM DE ÁLBUM ESTILO VINILO
 *
 * Versión optimizada que REUTILIZA el componente SpinningVinyl: ✅ Sin duplicación de código ✅ Usa
 * SpinningVinyl con estaReproduciendo = false ✅ Diseño limpio vinilo + funda ✅ Título integrado
 *
 * @param album Entidad del álbum con metadata
 * @param alClick Callback al hacer clic
 * @param modifier Modificadores adicionales
 */
@Composable
fun ItemAlbum(album: AlbumEntity, alClick: (AlbumEntity) -> Unit, modifier: Modifier = Modifier) {
    // 📏 Dimensiones optimizadas
    val tamanioFunda = 140.dp
    val tamanioDisco = 90.dp

    Column(
        modifier = modifier.clickable { alClick(album) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 💿 COMPONENTE VINILO CON FUNDA
        Box(
            modifier = Modifier.width(tamanioFunda).height(tamanioFunda),
            contentAlignment = Alignment.Center,
        ) {
            // CAPA 1: DISCO (Atrás, desplazado a la derecha)
            // ✨ REUTILIZANDO SpinningVinyl en modo estático
            SpinningVinyl(
                portadaPath = album.portadaPath,
                estaReproduciendo = false, // Siempre estático en el grid
                cacheKey = "album_disco_${album.idAlbum}",
                contentDescription = album.titulo,
                modifier = Modifier.size(tamanioDisco).align(Alignment.CenterEnd),
            )

            // CAPA 2: FUNDA (Frente, alineada a la izquierda)
            FundaAlbum(
                portadaPath = album.portadaPath,
                titulo = album.titulo,
                modifier = Modifier.size(tamanioFunda).align(Alignment.CenterStart),
            )
        }

        // 📝 TÍTULO DEL ÁLBUM
        Text(
            text = album.titulo,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(tamanioFunda).padding(horizontal = 4.dp),
        )
    }
}

/**
 * 📀 COMPONENTE: FUNDA DE ÁLBUM
 *
 * Muestra la funda con la portada completa o placeholder. Componente privado específico para este
 * contexto.
 */
@Composable
private fun FundaAlbum(portadaPath: String?, titulo: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Funda vacía (textura base)
        Image(
            painter = painterResource(id = R.drawable.funda),
            contentDescription = "Funda de álbum",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Portada dentro de la funda
        if (!portadaPath.isNullOrEmpty()) {
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(portadaPath)
                        .crossfade(300)
                        .memoryCacheKey("album_funda_${titulo.hashCode()}")
                        .build(),
                contentDescription = "Portada $titulo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(1.dp)),
                error = painterResource(id = R.drawable.ic_notification),
            )
        } else {
            // Placeholder: Primera letra del título
            Text(
                text = titulo.take(1).uppercase(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black.copy(alpha = 1f),
            )
        }
    }
}

// ==========================================
// PREVIEWS (Componente Aislado)
// ==========================================

// --- FAKE DATA ---
private object AlbumMocks {
    val estandar =
        AlbumEntity(
            idAlbum = 1,
            idArtista = 101,
            anio = 2023,
            titulo = "Random Access Memories",
            portadaPath = "fake_path_img", // Simula una ruta
        )

    val sinPortada =
        AlbumEntity(
            idAlbum = 2,
            idArtista = 102,
            anio = 1990,
            titulo = "Unknown Pleasures",
            portadaPath = null, // Activa placeholder
        )

    val tituloLargo =
        AlbumEntity(
            idAlbum = 3,
            idArtista = 103,
            anio = 2005,
            titulo = "The Dark Side of the Moon: 50th Anniversary Remastered",
            portadaPath = null,
        )
}

/**
 * Wrapper para simular el fondo oscuro de la biblioteca y dar espacio para ver las sombras y
 * bordes.
 */
@Composable
private fun AlbumItemWrapper(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.wrapContentSize()
                .background(Color(0xFF050010)) // Fondo Galaxia Oscuro
                .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// --- CASO 1: Álbum Estándar (Ideal) ---
@Preview(name = "1. Álbum - Con Portada", group = "Estados")
@Composable
fun PreviewAlbumStandard() {
    AlbumItemWrapper { ItemAlbum(album = AlbumMocks.estandar, alClick = {}) }
}

// --- CASO 2: Sin Portada (Placeholder) ---
// Verifica que se vea la letra inicial y el color de fondo por defecto
@Preview(name = "2. Álbum - Placeholder", group = "Estados")
@Composable
fun PreviewAlbumPlaceholder() {
    AlbumItemWrapper { ItemAlbum(album = AlbumMocks.sinPortada, alClick = {}) }
}

// --- CASO 3: Título Largo (Stress Test) ---
// Verifica el 'ellipsize' (...) en la segunda línea
@Preview(name = "3. Álbum - Texto Largo", group = "Estados")
@Composable
fun PreviewAlbumLongText() {
    AlbumItemWrapper { ItemAlbum(album = AlbumMocks.tituloLargo, alClick = {}) }
}

// --- CASO 4: Simulación de Grid (Contexto) ---
// Renderiza 2 ítems juntos para verificar el espaciado entre ellos
@Preview(name = "4. Contexto Grid (x2)", group = "Layout", widthDp = 360)
@Composable
fun PreviewAlbumGridRow() {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF050010)).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.wrapContentWidth(),
        ) {
            ItemAlbum(album = AlbumMocks.estandar, alClick = {})
            ItemAlbum(album = AlbumMocks.sinPortada, alClick = {})
        }
    }
}
