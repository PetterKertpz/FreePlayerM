package com.example.freeplayerm.ui.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.library.BibliotecaEstado
import com.example.freeplayerm.ui.features.library.BibliotecaEvento
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.ItemCancion
import com.example.freeplayerm.ui.features.library.components.layouts.LibraryListLayout
import com.example.freeplayerm.ui.features.library.domain.toItems
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎵 CONTENIDO DE CANCIONES
 *
 * Muestra la lista principal de canciones usando el layout genérico. Sincronizado con SongEntity
 * v2.0 y SongWithArtist v2.0.
 */
@Composable
fun SongsContent(
   canciones: List<SongWithArtist>,
   modifier: Modifier = Modifier,
   lazyListState: LazyListState,
   estado: BibliotecaEstado,
   onBibliotecaEvento: (BibliotecaEvento) -> Unit,
   onPlayerEvent: (PlayerEvent) -> Unit,
) {
    // ✅ Convertir entidades a items de UI con estado de selección (Optimizado)
    val items by
        remember(canciones, estado.cancionesSeleccionadas) {
            derivedStateOf { canciones.toItems(estado.cancionesSeleccionadas) }
        }

    // ✅ Usar layout genérico para listas
    LibraryListLayout(
        items = items,
        listState = lazyListState,
        baseItemHeight = 72.dp, // 🎯 Altura estándar para items de canción
        nivelZoom = estado.nivelZoom,
        onZoomChange = { onBibliotecaEvento(BibliotecaEvento.CambiarNivelZoom(it)) },
        searchQuery = estado.textoDeBusqueda,
        emptyMessage = "No hay canciones en tu biblioteca",
        modifier = modifier,
    ) { cancionItem ->

        // 🌌 Item Galáctico de Canción
        ItemCancion(
            cancion = cancionItem.songWithArtist,
            esSeleccionado = cancionItem.isSelected,
            alClick = {
                if (estado.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.AlternarSeleccionCancion(cancionItem.id))
                } else {
                    onPlayerEvent(
                       PlayerEvent.Playback.SetQueueAndPlay(
                            estado.canciones,
                            cancionItem.songWithArtist,
                        )
                    )
                    // Limpiar búsqueda al reproducir para ver contexto
                    if (estado.textoDeBusqueda.isNotEmpty()) {
                        onBibliotecaEvento(BibliotecaEvento.LimpiarBusqueda)
                    }
                }
            },
            alLongClick = {
                if (!estado.esModoSeleccion) {
                    onBibliotecaEvento(
                        BibliotecaEvento.ActivarModoSeleccion(cancionItem.songWithArtist)
                    )
                }
            },
            alClickMasOpciones = {
                onBibliotecaEvento(BibliotecaEvento.EditarCancion(cancionItem.songWithArtist))
            },
        )
    }
}

// ==================== PREVIEWS & FAKE DATA ====================

/** 🎵 Provider de datos falsos para Canciones Genera relaciones completas SongWithArtist */
class SongsProvider : PreviewParameterProvider<List<SongWithArtist>> {

    private val emptyList = emptyList<SongWithArtist>()

    // 🎵 Lista poblada con variedad de metadatos
    private val populatedList =
        listOf(
            createFakeSong(
                id = 1,
                titulo = "Starboy",
                artista = "The Weeknd",
                album = "Starboy",
                duracion = 230,
                esFavorita = true,
                hasCover = true,
            ),
            createFakeSong(
                id = 2,
                titulo = "Instant Crush",
                artista = "Daft Punk",
                album = "RAM",
                duracion = 337,
                esFavorita = true,
                hasCover = true,
            ),
            createFakeSong(
                id = 3,
                titulo = "Audio de WhatsApp 2024-01-01",
                artista = null,
                album = null,
                duracion = 45,
                esFavorita = false,
                hasCover = false, // Caso archivo local sin tags
            ),
            createFakeSong(
                id = 4,
                titulo = "Bohemian Rhapsody",
                artista = "Queen",
                album = "A Night at the Opera",
                duracion = 354,
                esFavorita = true,
                hasCover = true,
            ),
            createFakeSong(
                id = 5,
                titulo = "Levitating (Remix)",
                artista = "Dua Lipa",
                album = "Future Nostalgia",
                duracion = 203,
                esFavorita = false,
                hasCover = true,
            ),
            createFakeSong(
                id = 6,
                titulo = "Midnight City",
                artista = "M83",
                album = "Hurry Up, We're Dreaming",
                duracion = 243,
                esFavorita = false,
                hasCover = true,
            ),
        )

    override val values = sequenceOf(populatedList, emptyList)

    /** 🛠️ Helper para crear instancias complejas de SongWithArtist */
    private fun createFakeSong(
        id: Int,
        titulo: String,
        artista: String?,
        album: String?,
        duracion: Int,
        esFavorita: Boolean,
        hasCover: Boolean,
    ): SongWithArtist {
        // 1. Crear entidad base
        val entity =
            SongEntity(
                idCancion = id,
                titulo = titulo,
                duracionSegundos = duracion,
                idArtista = if (artista != null) id * 10 else null,
                idAlbum = if (album != null) id * 100 else null,
                idGenero = 1,
                origen = SongEntity.ORIGEN_LOCAL,
                archivoPath = "/storage/emulated/0/Music/$titulo.mp3",
                portadaPath = if (hasCover) "/path/to/cover.jpg" else null,
                vecesReproducida = if (esFavorita) 50 else 2,
                letraDisponible = true,
            )

        // 2. Crear relación
        return SongWithArtist(
            cancion = entity,
            artistaNombre = artista,
            albumNombre = album,
            generoNombre = "Pop", // Default para preview
            portadaPath = entity.portadaPath,
            esFavorita = esFavorita,
            fechaLanzamiento = "2023",
        )
    }
}

// ==================== PREVIEWS ====================

// 🛑 Mock de Estado para que el Preview compile sin el ViewModel real
private val MockState =
    BibliotecaEstado(
        canciones = emptyList(),
        nivelZoom = NivelZoom.NORMAL,
        textoDeBusqueda = "",
        esModoSeleccion = false,
        cancionesSeleccionadas = emptySet(),
    )

@Preview(
    name = "🎵 Lista Canciones - Poblada",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun PreviewSongsList(
    @PreviewParameter(SongsProvider::class) canciones: List<SongWithArtist>
) {
    val data = canciones.ifEmpty { SongsProvider().values.first() }

    // Estado mockeado con datos
    val stateWithData = MockState.copy(canciones = data)

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            SongsContent(
                canciones = data,
                lazyListState = rememberLazyListState(),
                estado = stateWithData,
                onBibliotecaEvento = {},
                onPlayerEvent = {},
            )
        }
    }
}

@Preview(
    name = "📭 Estado Vacío",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun PreviewSongsEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            SongsContent(
                canciones = emptyList(),
                lazyListState = rememberLazyListState(),
                estado = MockState, // Estado vacío por defecto
                onBibliotecaEvento = {},
                onPlayerEvent = {},
            )
        }
    }
}
