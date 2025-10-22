// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.ui.features.shared.MarqueeTextConDesvanecido
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.shared.IconoCorazonAnimado
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Composable
fun CuerpoCanciones(
    canciones: List<CancionConArtista>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    // --- CAMBIO #1: LA LÓGICA DE EVENTOS SE MANEJA AQUÍ, EN EL NIVEL SUPERIOR ---
    if (canciones.isEmpty()) {
        val mensaje = if (estado.textoDeBusqueda.isNotBlank()) {
            "No se encontraron resultados para \"${estado.textoDeBusqueda}\""
        } else {
            "Esta sección está vacía."
        }
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text(mensaje, textAlign = TextAlign.Center)
        }
    } else {
        ListaDeCanciones(
            lazyListState = lazyListState,
            canciones = canciones,
            esModoSeleccion = estado.esModoSeleccion,
            cancionesSeleccionadas = estado.cancionesSeleccionadas,
            // El clic en la canción se traduce en un evento para el reproductor
            onCancionClick = { cancionSeleccionada ->
                onReproductorEvento(ReproductorEvento.EstablecerColaYReproducir(estado.canciones, cancionSeleccionada))
                // Limpiamos la barra de búsqueda al seleccionar una canción
                onBibliotecaEvento(BibliotecaEvento.LimpiarBusqueda)
            },
            // El clic en el favorito se traduce en un evento para la biblioteca
            onFavoritoClick = { cancionSeleccionada ->
                onBibliotecaEvento(BibliotecaEvento.AlternarFavorito(cancionSeleccionada))
            },
            onEditClick = { cancion ->
                onBibliotecaEvento(BibliotecaEvento.EditarCancion(cancion))
            },
            onToggleSeleccion = { cancionId ->
            onBibliotecaEvento(BibliotecaEvento.AlternarSeleccionCancion(cancionId))
            },
            onActivarModoSeleccion = { cancion ->
            onBibliotecaEvento(BibliotecaEvento.ActivarModoSeleccion(cancion))
            },

        )
    }
}

@Composable
private fun ListaDeCanciones(
    lazyListState: LazyListState,
    canciones: List<CancionConArtista>,
    esModoSeleccion: Boolean,
    cancionesSeleccionadas: Set<Int>,
    onCancionClick: (CancionConArtista) -> Unit,
    onFavoritoClick: (CancionConArtista) -> Unit,
    onEditClick: (CancionConArtista) -> Unit,
    onToggleSeleccion: (Int) -> Unit,
    onActivarModoSeleccion: (CancionConArtista) -> Unit,

) {
    LazyColumn(state = lazyListState) {
        items(
            items = canciones,
            key = { cancionConArtista -> cancionConArtista.cancion.idCancion }
        ) { cancionConArtista ->
            CancionItem(
                cancionConArtista = cancionConArtista,
                estaSeleccionada = cancionConArtista.cancion.idCancion in cancionesSeleccionadas,
                esModoSeleccion = esModoSeleccion,
                onClick = {
                    if (esModoSeleccion) {
                        onToggleSeleccion(cancionConArtista.cancion.idCancion)
                    } else {
                        onCancionClick(cancionConArtista)
                    }
                },
                onLongClick = { onActivarModoSeleccion(cancionConArtista) },
                onFavoritoClick = { onFavoritoClick(cancionConArtista) },
                onEditClick = { onEditClick(cancionConArtista) },

            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CancionItem(
    cancionConArtista: CancionConArtista,
    estaSeleccionada: Boolean,
    esModoSeleccion: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoritoClick: () -> Unit,
    onEditClick: () -> Unit,

) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,

            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (esModoSeleccion) {
            Checkbox(
                checked = estaSeleccionada,
                onCheckedChange = { onClick() }
            )
        }
        AsyncImage(
            model = cancionConArtista.portadaPath,
            contentDescription = "Portada de ${cancionConArtista.albumNombre}",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.GrisProfundo),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            MarqueeTextConDesvanecido(
                text = "  " + cancionConArtista.cancion.titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,

            )
            MarqueeTextConDesvanecido(
                text = ("  " + cancionConArtista.artistaNombre),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,

            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row {
            if (!esModoSeleccion) {
                IconButton(onClick = onFavoritoClick, modifier = Modifier.size(36.dp)) {
                    // --- ✅ REEMPLAZAMOS EL ICONO ANTIGUO POR EL NUEVO ---
                    IconoCorazonAnimado(
                        esFavorito = cancionConArtista.esFavorita
                    )
                }
            }

            // Los botones de Añadir a Lista y Editar solo se muestran EN modo selección
            if (esModoSeleccion) {
                // Botón de Editar
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar información")
                }
            }
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
            portadaPath = null,
            fechaLanzamiento = null
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
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = listaDeCancionesDePrueba
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
            portadaPath = null,
            fechaLanzamiento = null
        )
    }

    FreePlayerMTheme {
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = listaDeCancionesDePrueba
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
                portadaPath = null,
                fechaLanzamiento = null
            )
        }
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = listaDeCancionesDePrueba
        )
    }
}
