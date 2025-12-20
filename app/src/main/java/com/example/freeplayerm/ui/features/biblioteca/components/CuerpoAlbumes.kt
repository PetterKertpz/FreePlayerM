package com.example.freeplayerm.ui.features.biblioteca.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * ✅ OPTIMIZADO: Grid de álbumes con vinilos
 *
 * Mejoras:
 * - Corrección del llamado a ItemAlbumVinilo
 * - Keys estables para performance
 * - ContentType para reciclaje optimizado
 * - Previews completas (Light/Dark)
 */
@Composable
fun CuerpoAlbumes(
    albumes: List<AlbumEntity>,
    lazyGridState: LazyGridState,
    onAlbumClick: (AlbumEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (albumes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin álbumes disponibles",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 190.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(
                items = albumes,
                key = { it.idAlbum }, // ⚡ KEY ESTABLE para performance
                contentType = { "AlbumItem" } // ⚡ RECICLAJE optimizado
            ) { album ->
                // ✅ CORRECCIÓN: Llamado correcto con parámetros
                ItemAlbumVinilo(
                    album = album,
                    alClick = onAlbumClick,
                    modifier = Modifier.animateItem() // ⚡ Animaciones suaves
                )
            }
        }
    }
}

// ==========================================
// ✅ PREVIEWS COMPLETAS
// ==========================================

@Preview(name = "Light - Con álbumes", showBackground = true)
@Preview(name = "Dark - Con álbumes", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCuerpoAlbumesConDatos() {
    val listaDePrueba = listOf(
        AlbumEntity(
            idAlbum = 1,
            idArtista = 1,
            titulo = "Viaje de Lujo",
            anio = 2025
        ),
        AlbumEntity(
            idAlbum = 2,
            idArtista = 2,
            titulo = "Noches de Verano",
            anio = 2022
        ),
        AlbumEntity(
            idAlbum = 3,
            idArtista = 3,
            titulo = "Ecos Urbanos",
            anio = 2024
        ),
        AlbumEntity(
            idAlbum = 4,
            idArtista = 4,
            titulo = "Sueños Digitales",
            anio = 2023
        ),
        AlbumEntity(
            idAlbum = 5,
            idArtista = 5,
            titulo = "Ritmos del Alma",
            anio = 2022
        ),
        AlbumEntity(
            idAlbum = 6,
            idArtista = 6,
            titulo = "Melodías Nocturnas",
            anio = 2024
        )
    )

    FreePlayerMTheme {
        CuerpoAlbumes(
            albumes = listaDePrueba,
            onAlbumClick = {},
            lazyGridState = rememberLazyGridState()
        )
    }
}

@Preview(name = "Estado Vacío", showBackground = true)
@Composable
private fun PreviewCuerpoAlbumesVacio() {
    FreePlayerMTheme {
        CuerpoAlbumes(
            albumes = emptyList(),
            onAlbumClick = {},
            lazyGridState = rememberLazyGridState()
        )
    }
}

@Preview(name = "Pocos álbumes", showBackground = true)
@Composable
private fun PreviewCuerpoAlbumesPocos() {
    val listaDePrueba = listOf(
        AlbumEntity(
            idAlbum = 1,
            idArtista = 1,
            titulo = "Solo Album",
            anio = 2024
        )
    )

    FreePlayerMTheme {
        CuerpoAlbumes(
            albumes = listaDePrueba,
            onAlbumClick = {},
            lazyGridState = rememberLazyGridState()
        )
    }
}

@Preview(name = "Muchos álbumes (Performance)", showBackground = true)
@Composable
private fun PreviewCuerpoAlbumesMuchos() {
    val listaDePrueba = (1..20).map { i ->
        AlbumEntity(
            idAlbum = i,
            idArtista = (i % 5) + 1,
            titulo = "Álbum #$i",
            anio = 2020 + (i % 5)
        )
    }

    FreePlayerMTheme {
        CuerpoAlbumes(
            albumes = listaDePrueba,
            onAlbumClick = {},
            lazyGridState = rememberLazyGridState()
        )
    }
}