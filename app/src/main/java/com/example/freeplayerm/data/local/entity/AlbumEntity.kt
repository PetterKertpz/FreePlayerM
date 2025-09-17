// en: app/src/main/java/com/example/freeplayerm/data/local/entity/AlbumEntity.kt
package com.example.freeplayerm.com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index // <-- Asegúrate de importar Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albumes",
    foreignKeys = [
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.CASCADE // <-- Corregido en la respuesta anterior
        )
    ],
    // --- CAMBIO CLAVE: AÑADIMOS UN ÍNDICE ÚNICO ---
    // Esta regla le dice a la base de datos: "La combinación de 'titulo' y 'id_artista'
    // debe ser única. No permitas dos filas que tengan ambos valores iguales".
    indices = [Index(value = ["titulo", "id_artista"], unique = true)]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_album")
    val idAlbum: Int = 0,

    // Corregido para ser no opcional, ya que un álbum siempre tiene un artista
    @ColumnInfo(name = "id_artista", index = true)
    val idArtista: Int,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "anio")
    val anio: Int?,

    @ColumnInfo(name = "portada_path")
    val portadaPath: String?
)