// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoListas.kt
package com.example.freeplayerm.ui.features.biblioteca.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity


@Composable
fun CuerpoListas(
    modifier: Modifier = Modifier,
    listas: List<ListaReproduccionEntity>,
    onListaClick: (ListaReproduccionEntity) -> Unit
) {
    if (listas.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Aún no has creado ninguna lista.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listas) { lista ->
                ListaItem(
                    lista = lista,
                    onClick = { onListaClick(lista) }
                )
            }
        }
    }
}

@Composable
private fun ListaItem(
    lista: ListaReproduccionEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = lista.portadaUrl,
            contentDescription = "Portada de ${lista.nombre}",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            // Mostramos un icono de música por defecto si no hay portada

        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lista.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Text(
                text = lista.descripcion ?: "",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )
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
        CuerpoListas(listas = listasDePrueba, onListaClick = {})
    }
}