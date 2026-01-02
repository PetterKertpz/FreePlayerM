// en: app/src/main/java/com/example/freeplayerm/data/local/entity/StatisticsModels.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo

/**
 * Data class para estadísticas de top elementos (artistas, géneros, etc.) Usado en queries de
 * ranking
 */
data class TopItemEstadistica(
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "cantidad") val cantidad: Int,
)

/**
 * Data class para estadísticas generales de la biblioteca musical Contiene totales agregados de
 * todos los elementos
 */
data class EstadisticasGenerales(
    @ColumnInfo(name = "total_canciones") val totalCanciones: Int,
    @ColumnInfo(name = "total_artistas") val totalArtistas: Int,
    @ColumnInfo(name = "total_albumes") val totalAlbumes: Int,
    @ColumnInfo(name = "total_generos") val totalGeneros: Int,
    @ColumnInfo(name = "duracion_total") val duracionTotal: Int,
)

/** Data class para estadísticas de álbumes */
data class AlbumEstadistica(
    @ColumnInfo(name = "titulo") val titulo: String,
    @ColumnInfo(name = "artista_nombre") val artistaNombre: String,
    @ColumnInfo(name = "cantidad_canciones") val cantidadCanciones: Int,
    @ColumnInfo(name = "duracion_total") val duracionTotal: Int,
)

/** Data class para estadísticas de reproducción */
data class EstadisticaReproduccion(
    @ColumnInfo(name = "id_cancion") val idCancion: Int,
    @ColumnInfo(name = "titulo_cancion") val tituloCancion: String,
    @ColumnInfo(name = "reproducciones") val reproducciones: Int,
    @ColumnInfo(name = "ultima_reproduccion") val ultimaReproduccion: Int?,
)
