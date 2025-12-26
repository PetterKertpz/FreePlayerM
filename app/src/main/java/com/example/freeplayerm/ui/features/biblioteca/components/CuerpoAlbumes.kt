package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.ui.features.biblioteca.components.items.ItemAlbumVinilo
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaGridLayout
import com.example.freeplayerm.ui.features.biblioteca.domain.toItem

/**
 * ✅ REFACTORIZADO - Usa BibliotecaGridLayout genérico
 *
 * Antes: 60+ líneas con estados vacíos duplicados
 * Ahora: 25 líneas enfocadas en lógica de álbumes
 */
@Composable
fun CuerpoAlbumes(
    albumes: List<AlbumEntity>,
    lazyGridState: LazyGridState,
    onAlbumClick: (AlbumEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Convertir entidades a items de UI
    val items by remember(albumes) {
        derivedStateOf { albumes.map { it.toItem() } }
    }

    // ✅ Usar layout genérico
    BibliotecaGridLayout(
        items = items,
        gridState = lazyGridState,
        minItemSize = 190.dp,
        emptyMessage = "No hay álbumes en tu biblioteca",
        modifier = modifier
    ) { albumItem ->
        // Lógica específica de visualización de álbumes
        ItemAlbumVinilo(
            album = albumItem.album,
            alClick = onAlbumClick
        )
    }
}