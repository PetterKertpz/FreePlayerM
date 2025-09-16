// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento

@Composable
fun CuerpoCanciones(
    modifier: Modifier = Modifier,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    if (estado.canciones.isEmpty()) {
        if (estado.textoDeBusqueda.isNotBlank()) {
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize().padding(16.dp)) {
                Text("No se encontraron resultados para \"${estado.textoDeBusqueda}\"", textAlign = TextAlign.Center)
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize().padding(16.dp)) {
                Text("Esta sección está vacía.", textAlign = TextAlign.Center)
            }
        }
    } else {
        ListaDeCanciones(
            canciones = estado.canciones,
            onCancionClick = { cancionConArtistaSeleccionada ->
                onReproductorEvento(
                    ReproductorEvento.SeleccionarCancion(cancionConArtistaSeleccionada)
                )
            }
        )
    }
}

@Composable
private fun ListaDeCanciones(
    canciones: List<CancionConArtista>,
    onCancionClick: (CancionConArtista) -> Unit
) {
    LazyColumn {
        items(
            items = canciones,
            key = { cancionConArtista -> cancionConArtista.cancion.idCancion }
        ) { cancionConArtista ->
            CancionItem(
                cancionConArtista = cancionConArtista,
                onClick = { onCancionClick(cancionConArtista) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CancionItem(
    cancionConArtista: CancionConArtista,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cancionConArtista.cancion.titulo,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = cancionConArtista.artistaNombre ?: "Artista Desconocido",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

// ... (Preview sin cambios)