
// en: app/src/main/java/com/example/freeplayerm/data/local/entity/AlbumEntity.kt
package com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "albumes",
    foreignKeys = [
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.SET_NULL // Si se borra el artista, el id_artista del álbum se pone en null
        )
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_album")
    val idAlbum: Int = 0,

    @ColumnInfo(name = "id_artista", index = true)
    val idArtista: Int?,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "anio")
    val anio: Int?,

    @ColumnInfo(name = "portada_url")
    val portadaUrl: String?
)