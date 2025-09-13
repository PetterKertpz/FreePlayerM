// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/ListaDeCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import android.annotation.SuppressLint
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
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import java.util.concurrent.TimeUnit

/**
 * Muestra una lista de canciones de forma vertical y optimizada.
 * @param canciones La lista de entidades de canción a mostrar.
 * @param onCancionClick Un callback que se invoca con la CancionEntity cuando un item es presionado.
 */
@Composable
fun ListaDeCanciones(
    modifier: Modifier = Modifier,
    canciones: List<CancionEntity>,
    onCancionClick: (CancionEntity) -> Unit
) {
    if (canciones.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aún no tienes canciones en tu biblioteca.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = canciones,
                key = { cancion -> cancion.idCancion } // Ayuda a Compose a optimizar la lista
            ) { cancion ->
                CancionItem(
                    cancion = cancion,
                    onClick = { onCancionClick(cancion) }
                )
            }
        }
    }
}

/**
 * El Composable para un único elemento (fila) en la lista de canciones.
 */
@Composable
private fun CancionItem(
    cancion: CancionEntity,
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
        // Portada de la canción
        AsyncImage(
            model = cancion.portadaUrl,
            contentDescription = "Portada de ${cancion.titulo}",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        // Información de la canción (Título y Artista)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cancion.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Text(
                text = "Artista ${cancion.idArtista}", // TODO: Obtener nombre del artista
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )
        }

        // Duración de la canción
        Text(
            text = formatDuracion(cancion.duracionSegundos),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuracion(segundos: Int): String {
    val minutos = TimeUnit.SECONDS.toMinutes(segundos.toLong())
    val segundosRestantes = segundos % 60
    return String.format("%d:%02d", minutos, segundosRestantes)
}


// Añadir al final de ListaDeCanciones.kt

@Preview(showBackground = true)
@Composable
fun PreviewListaDeCanciones() {
    val listaDePrueba = listOf(
        CancionEntity(1, 1, 1, 1, "El Sol no Regresa", 227, "", "LOCAL", null),
        CancionEntity(2, 2, 2, 2, "La Playa", 247, "", "LOCAL", null),
        CancionEntity(3, 3, 3, 3, "Rosas", 235, "", "LOCAL", null)
    )
    FreePlayerMTheme {
        ListaDeCanciones(canciones = listaDePrueba, onCancionClick = {})
    }
}