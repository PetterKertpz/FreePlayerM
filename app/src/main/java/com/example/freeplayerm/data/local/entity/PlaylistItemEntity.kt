// en: app/src/main/java/com/example/freeplayerm/data/local/entity/PlaylistItemEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 📋 DETALLE LISTA REPRODUCCION ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa la relación muchos a muchos entre listas y canciones Incluye campo de
 * orden para mantener la secuencia de canciones en la lista
 *
 * Características:
 * - Clave primaria compuesta (lista + canción)
 * - CASCADE delete: si se borra la lista o canción, se borra el detalle
 * - Campo orden para ordenamiento personalizado
 * - Timestamp de cuándo se agregó la canción a la lista
 *
 * @version 2.0 - Enhanced with ordering
 */
@Entity(
    tableName = "detalle_lista_reproduccion",
    primaryKeys = ["id_lista", "id_cancion"],
    foreignKeys =
        [
            ForeignKey(
                entity = PlaylistEntity::class,
                parentColumns = ["id_lista"],
                childColumns = ["id_lista"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = SongEntity::class,
                parentColumns = ["id_cancion"],
                childColumns = ["id_cancion"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices =
        [
            Index(value = ["id_cancion"]),
            Index(value = ["id_lista", "orden"]), // Índice compuesto para ordenamiento eficiente
        ],
)
data class PlaylistItemEntity(
    @ColumnInfo(name = "id_lista") val idLista: Int,
    @ColumnInfo(name = "id_cancion") val idCancion: Int,
    @ColumnInfo(name = "orden") val orden: Int = 0, // Posición en la lista (0, 1, 2, ...)
    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(), // Cuándo se agregó a la lista
    @ColumnInfo(name = "agregada_por_usuario")
    val agregadaPorUsuario: Int? =
        null, // ID del usuario que la agregó (útil para listas colaborativas)
    @ColumnInfo(name = "numero_reproducciones_en_lista")
    val numeroReproduccionesEnLista: Int = 0, // Cuántas veces se ha reproducido desde esta lista
) {
    companion object {
        /** Crea un nuevo detalle con el siguiente orden disponible */
        fun crear(
            idLista: Int,
            idCancion: Int,
            ordenActual: Int,
            usuarioId: Int? = null,
        ): PlaylistItemEntity {
            return PlaylistItemEntity(
                idLista = idLista,
                idCancion = idCancion,
                orden = ordenActual,
                agregadaPorUsuario = usuarioId,
            )
        }
    }
}
