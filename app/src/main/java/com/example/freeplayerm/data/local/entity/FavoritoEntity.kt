// en: app/src/main/java/com/example/freeplayerm/data/local/entity/FavoritoEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ❤️ FAVORITO ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa la relación muchos a muchos entre usuarios y canciones favoritas
 *
 * Características:
 * - Clave primaria compuesta (usuario + canción)
 * - CASCADE delete: si se borra el usuario o canción, se borra el favorito
 * - Timestamp de cuándo se marcó como favorito
 * - Ordenamiento personalizado
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "favoritos",
    primaryKeys = ["id_usuario", "id_cancion"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CancionEntity::class,
            parentColumns = ["id_cancion"],
            childColumns = ["id_cancion"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_cancion"]),
        Index(value = ["id_usuario", "fecha_agregado"]), // Para ordenar favoritos por fecha
        Index(value = ["fecha_agregado"])
    ]
)
data class FavoritoEntity(
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    @ColumnInfo(name = "id_cancion")
    val idCancion: Int,

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Int = System.currentTimeMillis().toInt(), // Cuándo se marcó como favorito

    @ColumnInfo(name = "orden")
    val orden: Int = 0, // Orden personalizado (si el usuario reordena favoritos)

    @ColumnInfo(name = "veces_reproducida_desde_favoritos")
    val vecesReproducidaDesdeFavoritos: Int = 0, // Cuántas veces la reprodujo desde favoritos

    @ColumnInfo(name = "calificacion")
    val calificacion: Float? = null, // Calificación personal del usuario (0-5 estrellas)

    @ColumnInfo(name = "notas")
    val notas: String? = null // Notas personales sobre la canción
) {
    /**
     * Verifica si tiene calificación
     */
    fun tieneCalificacion(): Boolean = calificacion != null && calificacion > 0

    /**
     * Verifica si tiene notas
     */
    fun tieneNotas(): Boolean = !notas.isNullOrBlank()

    companion object {
        /**
         * Crea un favorito básico
         */
        fun crear(idUsuario: Int, idCancion: Int): FavoritoEntity {
            return FavoritoEntity(
                idUsuario = idUsuario,
                idCancion = idCancion
            )
        }

        /**
         * Crea un favorito con calificación
         */
        fun crearConCalificacion(
            idUsuario: Int,
            idCancion: Int,
            calificacion: Float
        ): FavoritoEntity {
            return FavoritoEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                calificacion = calificacion.coerceIn(0f, 5f) // Asegura que esté entre 0 y 5
            )
        }
    }
}