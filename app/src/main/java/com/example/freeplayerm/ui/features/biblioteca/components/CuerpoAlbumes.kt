// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoAlbumes.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity

/**
 * Muestra una cuadrícula de álbumes.
 * @param albumes La lista de entidades de álbum a mostrar.
 * @param onAlbumClick Un callback que se invoca con el AlbumEntity cuando un item es presionado.
 */
@Composable
fun CuerpoAlbumes(
    modifier: Modifier = Modifier,
    albumes: List<AlbumEntity>,
    onAlbumClick: (AlbumEntity) -> Unit
) {
    if (albumes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No se encontraron álbumes.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp), // Se adapta al tamaño de la pantalla
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albumes) { album ->
                AlbumItem(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
private fun AlbumItem(
    album: AlbumEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp) // Ancho fijo para cada item de la cuadrícula
            .clickable(onClick = onClick)
    ) {
        // Portada del álbum con sombra
        AsyncImage(
            model = album.portadaUrl,
            contentDescription = "Portada de ${album.titulo}",
            modifier = Modifier
                .aspectRatio(1f) // Mantiene la proporción cuadrada
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Información del álbum
        Text(
            text = album.titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
        Text(
            text = "Artista ${album.idArtista}", // TODO: Obtener nombre del artista
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Gray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCuerpoAlbumes() {
    val listaDePrueba = listOf(
        AlbumEntity(1, 1, "Viaje de Lujo", 2023, ""),
        AlbumEntity(2, 2, "Noches de Verano", 2022, ""),
        AlbumEntity(3, 3, "Ecos Urbanos", 2024, "")
    )
    MaterialTheme {
        CuerpoAlbumes(albumes = listaDePrueba, onAlbumClick = {})
    }
}