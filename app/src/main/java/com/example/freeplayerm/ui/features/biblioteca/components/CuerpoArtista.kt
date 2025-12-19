// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoArtistas.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.ui.features.shared.MarqueeTextConDesvanecido
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun CuerpoArtistas(
    modifier: Modifier = Modifier,
    artistas: List<ArtistaEntity>,
    lazyGridState: LazyGridState,
    onArtistaClick: (ArtistaEntity) -> Unit
) {
    if (artistas.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No se encontraron artistas.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            state = lazyGridState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(artistas) { artista ->
                ArtistaItem(
                    artista = artista,
                    onClick = { onArtistaClick(artista) }
                )
            }
        }
    }
}

@Composable
private fun ArtistaItem(
    artista: ArtistaEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Usaremos una URL de avatar genérica por ahora
        val urlFotoArtista = "https://ui-avatars.com/api/?name=${artista.nombre}&background=random&color=fff&size=256"

        AsyncImage(
            model = urlFotoArtista,
            contentDescription = "Foto de ${artista.nombre}",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppColors.GrisProfundo),// Los artistas suelen tener fotos circulares
            contentScale = ContentScale.Crop
        )

        MarqueeTextConDesvanecido(
            text = "  " + artista.nombre,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,

        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCuerpoArtistas() {
    val listaDePrueba = listOf(
        ArtistaEntity(1, "La Oreja de Van Gogh", "España", null),
        ArtistaEntity(2, "Morat", "Colombia", null),
        ArtistaEntity(3, "Queen", "Reino Unido", null)
    )
    MaterialTheme {
        CuerpoArtistas(
            artistas = listaDePrueba, onArtistaClick = {},
            lazyGridState = LazyGridState(),
        )
    }
}