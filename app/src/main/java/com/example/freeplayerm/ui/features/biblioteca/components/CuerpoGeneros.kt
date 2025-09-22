package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.ui.features.biblioteca.utils.GeneroVisuals

@Composable
fun CuerpoGeneros(
    modifier: Modifier = Modifier,
    generos: List<GeneroEntity>,
    lazyGridState: LazyGridState,
    onGeneroClick: (GeneroEntity) -> Unit
) {
    if (generos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No se encontraron géneros.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Una cuadrícula con 2 columnas
            state = lazyGridState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(generos, key = { it.idGenero }) { genero ->
                GeneroItem(
                    genero = genero,
                    onClick = { onGeneroClick(genero) }
                )
            }
        }
    }
}

@Composable
private fun GeneroItem(
    genero: GeneroEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Hace que la tarjeta sea un cuadrado perfecto
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart // Alinea el texto abajo a la izquierda
        ) {
            // La imagen del género
            AsyncImage(
                model = GeneroVisuals.getImageForGenre(genero.nombre),
                contentDescription = genero.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Asegura que la imagen llene la tarjeta
            )
            // Un gradiente oscuro para que el texto sea legible sobre cualquier imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f // El gradiente empieza más abajo
                        )
                    )
            )
            // El nombre del género
            Text(
                text = genero.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCuerpoGeneros() {
    val listaDePrueba = listOf(
        GeneroEntity(1, "Rock"),
        GeneroEntity(2, "Pop"),
        GeneroEntity(3, "Cumbia"),
        GeneroEntity(4, "Electronica")

    )
    MaterialTheme {
        CuerpoGeneros(
            generos = listaDePrueba,
            onGeneroClick = {},
            lazyGridState = LazyGridState()
        )
    }
}