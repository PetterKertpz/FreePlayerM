package com.example.freeplayerm.ui.features.biblioteca.domain

import com.example.freeplayerm.data.local.entity.*
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista

/**
 * 🎯 CONTRATOS UNIFICADOS PARA ITEMS DE BIBLIOTECA
 *
 * Abstracción ligera sobre entidades de Room para facilitar
 * el manejo unificado en componentes de UI reutilizables.
 *
 * NO reemplaza las entidades, solo las envuelve para UI.
 */

// ==================== INTERFACE BASE ====================

/**
 * Contrato común para todos los items mostrados en la biblioteca
 */
sealed interface BibliotecaItem {
    val id: Int
    val displayTitle: String
    val displaySubtitle: String?
    val imageUrl: String?
}

// ==================== IMPLEMENTACIONES POR TIPO ====================

/**
 * Wrapper para canciones en listas
 */
data class CancionItem(
    val cancionConArtista: CancionConArtista,
    val isSelected: Boolean = false
) : BibliotecaItem {
    override val id: Int = cancionConArtista.cancion.idCancion
    override val displayTitle: String = cancionConArtista.cancion.titulo
    override val displaySubtitle: String? = cancionConArtista.artistaNombre
    override val imageUrl: String? = cancionConArtista.portadaPath

    val isFavorite: Boolean = cancionConArtista.esFavorita
    val duration: Int = cancionConArtista.cancion.duracionSegundos
}

/**
 * Wrapper para álbumes en grid
 */
data class AlbumItem(
    val album: AlbumEntity,
    val artistName: String? = null
) : BibliotecaItem {
    override val id: Int = album.idAlbum
    override val displayTitle: String = album.titulo
    override val displaySubtitle: String? = artistName ?: album.anio?.toString()
    override val imageUrl: String? = album.portadaPath
}

/**
 * Wrapper para artistas en grid
 */
data class ArtistaItem(
    val artista: ArtistaEntity
) : BibliotecaItem {
    override val id: Int = artista.idArtista
    override val displayTitle: String = artista.nombre
    override val displaySubtitle: String? = artista.paisOrigen
    override val imageUrl: String? = artista.imageUrl
}

/**
 * Wrapper para géneros en grid
 */
data class GeneroItem(
    val genero: GeneroEntity
) : BibliotecaItem {
    override val id: Int = genero.idGenero
    override val displayTitle: String = genero.nombre
    override val displaySubtitle: String? = null
    override val imageUrl: String? = null // Se genera dinámicamente en UI
}

/**
 * Wrapper para listas de reproducción
 */
data class PlaylistItem(
    val lista: ListaReproduccionEntity,
    val songCount: Int = 0
) : BibliotecaItem {
    override val id: Int = lista.idLista
    override val displayTitle: String = lista.nombre
    override val displaySubtitle: String? = lista.descripcion ?:
    if (songCount > 0) "$songCount canciones" else null
    override val imageUrl: String? = lista.portadaUrl
}

// ==================== MAPPERS DE EXTENSIÓN ====================

/**
 * Convierte CancionConArtista a CancionItem para UI
 */
fun CancionConArtista.toItem(isSelected: Boolean = false) = CancionItem(
    cancionConArtista = this,
    isSelected = isSelected
)

/**
 * Convierte lista de CancionConArtista a lista de items
 */
fun List<CancionConArtista>.toItems(selectedIds: Set<Int> = emptySet()) = map {
    it.toItem(isSelected = it.cancion.idCancion in selectedIds)
}

/**
 * Convierte AlbumEntity a AlbumItem
 */
fun AlbumEntity.toItem(artistName: String? = null) = AlbumItem(
    album = this,
    artistName = artistName
)

/**
 * Convierte ArtistaEntity a ArtistaItem
 */
fun ArtistaEntity.toItem() = ArtistaItem(artista = this)

/**
 * Convierte GeneroEntity a GeneroItem
 */
fun GeneroEntity.toItem() = GeneroItem(genero = this)

/**
 * Convierte ListaReproduccionEntity a PlaylistItem
 */
fun ListaReproduccionEntity.toItem(songCount: Int = 0) = PlaylistItem(
    lista = this,
    songCount = songCount
)