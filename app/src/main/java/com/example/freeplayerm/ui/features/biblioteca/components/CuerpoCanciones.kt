// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Composable
fun CuerpoCanciones(
    modifier: Modifier = Modifier,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    if (estado.canciones.isEmpty()) {
        if (estado.textoDeBusqueda.isNotBlank()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "No se encontraron resultados para \"${estado.textoDeBusqueda}\"",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
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
        AsyncImage(
        // --- CORRECCIÓN: Accedemos directamente a la propiedad en CancionConArtista ---
        model = cancionConArtista.portadaPath,
        contentDescription = "Portada de ${cancionConArtista.albumNombre}",
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.GrisProfundo),
        contentScale = ContentScale.Crop
        )
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
@Preview(showBackground = true, name = "Con Datos")
@Composable
private fun CuerpoCancionesConDatosPreview() {
    // 1. Creación de datos de prueba
    val listaDeCancionesDePrueba = (1..20).map { i ->
        CancionConArtista(
            cancion = CancionEntity(
                idCancion = i.toInt(),
                titulo = "Una Canción Muy Larga Para Probar El Efecto Marquee Número $i",
                idArtista = (i % 5).toInt(), // 5 artistas diferentes
                idAlbum = (i % 3).toInt(),   // 3 álbumes diferentes
                duracionSegundos = 210,
                archivoPath = "/path/to/song$i.mp3",
                origen = "LOCAL",
                idGenero = null
            ),
            artistaNombre = "Artista ${(i % 5) + 1}",
            albumNombre = null,
            generoNombre = null,
            esFavorita = false,
            portadaPath = null
        )
    }

    val estadoDePrueba = BibliotecaEstado(
        canciones = listaDeCancionesDePrueba,
        textoDeBusqueda = ""
    )

    // 2. Envolvemos el Composable en nuestro tema para que se vea correctamente
    FreePlayerMTheme {
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {}
        )
    }
}

/**
 * Vista previa para el Composable CuerpoCanciones cuando no hay canciones.
 */
@Preview(showBackground = true, name = "Estado Vacío")
@Composable
private fun CuerpoCancionesVacioPreview() {
    val estadoDePrueba = BibliotecaEstado(
        canciones = emptyList(), // Lista vacía
        textoDeBusqueda = ""
    )

    FreePlayerMTheme {
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {}
        )
    }
}

/**
 * Vista previa para CuerpoCanciones cuando una búsqueda no devuelve resultados.
 */
@Preview(showBackground = true, name = "Búsqueda Sin Resultados")
@Composable
private fun CuerpoCancionesBusquedaSinResultadosPreview() {
    val estadoDePrueba = BibliotecaEstado(
        canciones = emptyList(), // Lista vacía
        textoDeBusqueda = "Una canción que no existe" // Texto de búsqueda activo
    )

    FreePlayerMTheme {
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {}
        )
    }
}
