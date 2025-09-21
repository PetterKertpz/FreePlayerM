// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoAlbumes.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.freeplayerm.R
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.ui.theme.AppColors

/**
 * Muestra una cuadrícula de álbumes.
 * @param albumes La lista de entidades de álbum a mostrar.
 * @param onAlbumClick Un callback que se invoca con el AlbumEntity cuando un item es presionado.
 */
@Composable
fun CuerpoAlbumes(
    modifier: Modifier = Modifier,
    albumes: List<AlbumEntity>,
    lazyGridState: LazyGridState,
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
            columns = GridCells.Adaptive(minSize = 160.dp),
            state = lazyGridState,
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
fun AlbumItem(
    album: AlbumEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Box(

                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()

            ) {

                //vinilo
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(
                            start = 60.dp,
                        ) // hace que se vea sobresaliendo a la derecha
                        .align(Alignment.CenterEnd)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vinilo_foreground), // tu PNG del vinilo sin fondo
                        contentDescription = null,
                        modifier = Modifier
                            .zIndex(0f)
                            .fillMaxSize()
                            .padding(0.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 1f
                    )
                    // Label central del vinilo (portada en círculo)
                    AsyncImage(
                        model = album.portadaPath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(AppColors.GrisMedio)
                            .padding(0.dp),
                        contentScale = ContentScale.Crop,
                        alpha = 1f
                    )
                }
                //Carton
                AsyncImage(
                    model = album.portadaPath,
                    contentDescription = "Portada de ${album.titulo}",
                    modifier = Modifier
                        .shadow(8.dp,RoundedCornerShape(4.dp), clip = false)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.GrisProfundo)
                        .align(Alignment.CenterStart)
                        .fillMaxSize(0.75f)
                        .zIndex(1f),
                    contentScale = ContentScale.Crop,
                    alpha = 1f
                )

            }
        }
        Column (
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ℹ️ Información del álbum
            Text(
                text = album.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .basicMarquee(
                        spacing = MarqueeSpacing(0.dp)
                    )

            )
            Text(
                text = "Artista ${album.idArtista}", // reemplaza con el nombre real
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier
                    .basicMarquee()
            )
        }

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
        CuerpoAlbumes(
            albumes = listaDePrueba, onAlbumClick = {},
            lazyGridState = LazyGridState()
        )
    }
}