package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.features.biblioteca.components.items.ItemListaGalactico
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaGridLayout
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaListLayout
import com.example.freeplayerm.ui.features.biblioteca.domain.toItem

@Composable
fun CuerpoListas(
    listas: List<ListaReproduccionEntity>,
    lazyListState: LazyListState,
    onListaClick: (ListaReproduccionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by remember(listas) {
        derivedStateOf { listas.map { it.toItem() } }
    }

    BibliotecaListLayout(
        items = items,
        listState = lazyListState,
        emptyMessage = "Crea tu primera lista de reproducción",
        modifier = modifier
    ) { listaItem ->
        ItemListaGalactico(
            lista = listaItem.lista,
            onClick = { onListaClick(listaItem.lista) }
        )
    }
}