// en: app/src/main/java/com/example/freeplayerm/data/local/entity/GeneroEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "generos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class GeneroEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_genero")
    val idGenero: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String
)