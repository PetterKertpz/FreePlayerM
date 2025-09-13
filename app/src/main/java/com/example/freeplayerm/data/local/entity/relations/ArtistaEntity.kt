// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ArtistaEntity.kt
package com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artistas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class ArtistaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_artista")
    val idArtista: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "pais_origen")
    val paisOrigen: String?,

    @ColumnInfo(name = "descripcion")
    val descripcion: String?
)