// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoListas.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.features.shared.MarqueeTextConDesvanecido
import com.example.freeplayerm.ui.theme.AppColors


@Composable
fun CuerpoListas(
    listas: List<ListaReproduccionEntity>,
    lazyListState: LazyListState,
    onListaClick: (ListaReproduccionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (listas.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Crea tu primera lista...", color = Color.Gray)
        }
    } else {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(listas) { lista ->
                ItemListaGalactico(lista = lista, onClick = { onListaClick(lista) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCuerpoListas() {
    val listasDePrueba = listOf(
        ListaReproduccionEntity(1, 1, "Para entrenar", "Música para el gimnasio", ""),
        ListaReproduccionEntity(2, 1, "Relax", "Canciones tranquilas", "")
    )
    MaterialTheme {
        CuerpoListas(
            listas = listasDePrueba, onListaClick = {},
            lazyListState = LazyListState(0, 0)
        )
    }
}