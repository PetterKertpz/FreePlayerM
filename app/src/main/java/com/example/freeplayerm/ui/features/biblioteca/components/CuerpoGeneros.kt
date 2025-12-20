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
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.ui.features.shared.MarqueeTextConDesvanecido
import com.example.freeplayerm.ui.features.biblioteca.utils.GeneroVisuals

@Composable
fun CuerpoGeneros(
    generos: List<GeneroEntity>,
    lazyGridState: LazyGridState,
    onGeneroClick: (GeneroEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (generos.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin géneros", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columnas fijas se ven mejor para tarjetas de género
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(generos) { genero ->
                ItemGeneroGalactico(genero = genero, onClick = { onGeneroClick(genero) })
            }
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