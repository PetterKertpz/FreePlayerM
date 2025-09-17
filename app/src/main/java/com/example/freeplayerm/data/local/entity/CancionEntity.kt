// en: app/src/main/java/com/example/freeplayerm/data/local/entity/CancionEntity.kt
package com.example.freeplayerm.com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "canciones",
    foreignKeys = [
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id_album"],
            childColumns = ["id_album"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = GeneroEntity::class,
            parentColumns = ["id_genero"],
            childColumns = ["id_genero"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class CancionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int = 0,

    @ColumnInfo(name = "id_artista", index = true)
    val idArtista: Int?,

    @ColumnInfo(name = "id_album", index = true)
    val idAlbum: Int?,

    @ColumnInfo(name = "id_genero", index = true)
    val idGenero: Int?,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "duracion_segundos")
    val duracionSegundos: Int,

    @ColumnInfo(name = "origen")
    val origen: String, // "LOCAL", "REMOTA"

    @ColumnInfo(name = "archivo_path")
    val archivoPath: String? // Ruta local si el origen es "LOCAL"
)