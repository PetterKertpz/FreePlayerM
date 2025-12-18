
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "letras",
    foreignKeys = [
        ForeignKey(
            entity = CancionEntity::class,
            parentColumns = ["id_cancion"],
            childColumns = ["id_cancion"],
            onDelete = ForeignKey.CASCADE // Si se borra la canción, se borra la letra
        )
    ]
)
data class LetraEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int, // Mismo ID que la canción (Relación 1 a 1)

    @ColumnInfo(name = "letra")
    val letra: String
)