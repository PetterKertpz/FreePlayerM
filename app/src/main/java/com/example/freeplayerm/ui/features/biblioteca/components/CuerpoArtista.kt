package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.ui.features.biblioteca.components.items.ItemArtistaGalactico
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaGridLayout
import com.example.freeplayerm.ui.features.biblioteca.domain.toItem

/**
 * ✅ REFACTORIZADO - Usa BibliotecaGridLayout genérico
 */
@Composable
fun CuerpoArtistas(
    artistas: List<ArtistaEntity>,
    lazyGridState: LazyGridState,
    onArtistaClick: (ArtistaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by remember(artistas) {
        derivedStateOf { artistas.map { it.toItem() } }
    }

    BibliotecaGridLayout(
        items = items,
        gridState = lazyGridState,
        minItemSize = 140.dp,
        emptyMessage = "No hay artistas en tu biblioteca",
        modifier = modifier
    ) { artistaItem ->
        ItemArtistaGalactico(
            artista = artistaItem.artista,
            onClick = { onArtistaClick(artistaItem.artista) }
        )
    }
}