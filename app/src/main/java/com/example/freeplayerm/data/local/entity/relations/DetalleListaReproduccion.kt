package com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "detalle_lista_reproduccion",
    primaryKeys = ["id_lista", "id_cancion"],
    foreignKeys = [
        ForeignKey(
            entity = ListaReproduccionEntity::class,
            parentColumns = ["id_lista"],
            childColumns = ["id_lista"],
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
data class DetalleListaReproduccion(
    @ColumnInfo(name = "id_lista")
    val idLista: Int,

    @ColumnInfo(name = "id_cancion", index = true)
    val idCancion: Int
)