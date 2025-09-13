package com.example.freeplayerm.com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.freeplayerm.data.local.entity.UsuarioEntity

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
    ]
)
data class FavoritoEntity(
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    @ColumnInfo(name = "id_cancion", index = true)
    val idCancion: Int
)