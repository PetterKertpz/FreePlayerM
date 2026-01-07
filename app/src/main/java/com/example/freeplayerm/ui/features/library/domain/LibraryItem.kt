package com.example.freeplayerm.ui.features.library.domain

import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.library.NivelZoom

interface LibraryItem {
    /** Identificador único del elemento (usado para key en LazyLists) */
    val id: Int

    /** Título principal a mostrar (Ej. Nombre de canción, Título del álbum) */
    val displayTitle: String

    /** Subtítulo opcional (Ej. Nombre del artista, Cantidad de canciones) */
    val displaySubtitle: String?

    /** URL o Path local para cargar la imagen de portada (puede ser null) */
    val imageUrl: String?
}

// ==================== IMPLEMENTACIONES POR TIPO ====================

/** Wrapper para canciones en listas */
data class CancionItem(val songWithArtist: SongWithArtist, val isSelected: Boolean = false) :
    LibraryItem {
    override val id: Int = songWithArtist.cancion.idCancion
    override val displayTitle: String = songWithArtist.cancion.titulo
    override val displaySubtitle: String? = songWithArtist.artistaNombre
    override val imageUrl: String? = songWithArtist.portadaPath

    val isFavorite: Boolean = songWithArtist.esFavorita
    val duration: Int = songWithArtist.cancion.duracionSegundos
}

/** Wrapper para álbumes en grid */
data class AlbumItem(val album: AlbumEntity, val artistName: String? = null) : LibraryItem {
    override val id: Int = album.idAlbum
    override val displayTitle: String = album.titulo
    override val displaySubtitle: String? = artistName ?: album.anio?.toString()
    override val imageUrl: String? = album.portadaPath
}

/** Wrapper para artistas en grid */
data class ArtistaItem(val artista: ArtistEntity) : LibraryItem {
    override val id: Int = artista.idArtista
    override val displayTitle: String = artista.nombre
    override val displaySubtitle: String? = artista.paisOrigen
    override val imageUrl: String? = artista.imageUrl
}

/** Wrapper para géneros en grid */
data class GeneroItem(val genero: GenreEntity) : LibraryItem {
    override val id: Int = genero.idGenero
    override val displayTitle: String = genero.nombre
    override val displaySubtitle: String? = null
    override val imageUrl: String? = null // Se genera dinámicamente en UI
}

/** Wrapper para listas de reproducción */
data class PlaylistItem(val lista: PlaylistEntity, val songCount: Int = 0) : LibraryItem {
    override val id: Int = lista.idLista
    override val displayTitle: String = lista.nombre
    override val displaySubtitle: String? =
        lista.descripcion ?: if (songCount > 0) "$songCount canciones" else null
    override val imageUrl: String? = lista.portadaUrl
}

// ==================== MAPPERS DE EXTENSIÓN ====================

/** Convierte SongWithArtist a CancionItem para UI */
fun SongWithArtist.toItem(isSelected: Boolean = false) =
    CancionItem(songWithArtist = this, isSelected = isSelected)

/** Convierte lista de SongWithArtist a lista de items */
fun List<SongWithArtist>.toItems(selectedIds: Set<Int> = emptySet()) = map {
    it.toItem(isSelected = it.cancion.idCancion in selectedIds)
}

/** Convierte AlbumEntity a AlbumItem */
fun AlbumEntity.toItem(artistName: String? = null) =
    AlbumItem(album = this, artistName = artistName)

/** Convierte ArtistEntity a ArtistaItem */
fun ArtistEntity.toItem() = ArtistaItem(artista = this)

/** Convierte GenreEntity a GeneroItem */
fun GenreEntity.toItem() = GeneroItem(genero = this)

/** Convierte PlaylistEntity a PlaylistItem */
fun PlaylistEntity.toItem(songCount: Int = 0) = PlaylistItem(lista = this, songCount = songCount)

// =================================================================
// 🛠️ UTILIDADES PARA PREVIEWS Y TESTING
// =================================================================

/**
 * Implementación concreta y simple de [LibraryItem] diseñada EXCLUSIVAMENTE para su uso
 * en @Previews de Jetpack Compose y tests unitarios.
 *
 * NO usar en código de producción. Usar las entidades mapeadas en su lugar.
 */
data class MockLibraryItem(
    override val id: Int,
    override val displayTitle: String,
    override val displaySubtitle: String? = null,
    override val imageUrl: String? = null,
    // Propiedad extra útil para previews visuales si no hay imagen (color de fondo simulado)
    val previewColorHex: Long = 0xFF6200EE,
) : LibraryItem

/** Colección de datos de prueba listos para usar en @Previews. */
object LibraryPreviewsData {

    /** Un solo item genérico para pruebas unitarias de componentes */
    val mockItemSolo =
        MockLibraryItem(
            id = 1,
            displayTitle = "Título de Prueba",
            displaySubtitle = "Subtítulo Descriptivo",
            previewColorHex = 0xFFD500F9, // Neón Púrpura
        )

    /** Un item sin subtítulo ni imagen, para probar layouts compactos */
    val mockItemMinimo =
        MockLibraryItem(
            id = 2,
            displayTitle = "Item Mínimo",
            previewColorHex = 0xFF00E5FF, // Neón Cian
        )

    /** Una lista variada de 10 elementos para poblar grids y listas */
    val mockListaPoplada: List<LibraryItem> =
        listOf(
            mockItemSolo,
            mockItemMinimo,
            MockLibraryItem(3, "Random Access Memories", "Daft Punk", previewColorHex = 0xFFFF1744),
            MockLibraryItem(4, "Favoritos", "120 canciones", previewColorHex = 0xFF2979FF),
            MockLibraryItem(5, "Rock Clásico", null, previewColorHex = 0xFFFF9100),
            MockLibraryItem(
                6,
                "Bohemian Rhapsody",
                "Queen • A Night at the Opera",
                previewColorHex = 0xFF00C853,
            ),
            MockLibraryItem(7, "Entrenamiento", "Playlist • 45 mins", previewColorHex = 0xFFAA00FF),
            MockLibraryItem(
                8,
                "Podcast Episodio 1",
                "Duración: 1h 20m",
                previewColorHex = 0xFF64DD17,
            ),
            MockLibraryItem(9, "Abbey Road", "The Beatles", previewColorHex = 0xFF304FFE),
            MockLibraryItem(10, "Lo-Fi Beats", "Para estudiar", previewColorHex = 0xFF00BFA5),
        )

    /** Una lista vacía para probar estados de "No hay resultados" */
    val mockListaVacia: List<LibraryItem> = emptyList()
}
// =================================================================
// 📐 CONFIGURACIÓN CENTRALIZADA DE ZOOM
// =================================================================


object LibraryZoomConfig {
   
   // Columnas por nivel de zoom (para grids)
   fun columnasGrid(nivel: NivelZoom): Int = when (nivel) {
      NivelZoom.PEQUENO -> 4
      NivelZoom.NORMAL -> 3
      NivelZoom.GRANDE -> 2
   }
   
   // Multiplicadores de espaciado
   fun factorEspaciado(nivel: NivelZoom): Float = when (nivel) {
      NivelZoom.PEQUENO -> 0.6f
      NivelZoom.NORMAL -> 1.0f
      NivelZoom.GRANDE -> 1.2f
   }
   
   // Factor de escala para listas (no grids)
   fun factorEscalaLista(nivel: NivelZoom): Float = when (nivel) {
      NivelZoom.PEQUENO -> 0.85f
      NivelZoom.NORMAL -> 1.0f
      NivelZoom.GRANDE -> 1.2f
   }
}
