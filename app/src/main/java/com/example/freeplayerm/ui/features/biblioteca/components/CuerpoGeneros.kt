package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.ui.features.biblioteca.components.items.ItemGeneroGalactico
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaGridLayout
import com.example.freeplayerm.ui.features.biblioteca.domain.toItem

@Composable
fun CuerpoGeneros(
    generos: List<GeneroEntity>,
    lazyGridState: LazyGridState,
    onGeneroClick: (GeneroEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by remember(generos) {
        derivedStateOf { generos.map { it.toItem() } }
    }

    BibliotecaGridLayout(
        items = items,
        gridState = lazyGridState,
        minItemSize = 180.dp,
        emptyMessage = "No hay géneros disponibles",
        modifier = modifier
    ) { generoItem ->
        ItemGeneroGalactico(
            genero = generoItem.genero,
            onClick = { onGeneroClick(generoItem.genero) }
        )
    }
}