// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ListaReproduccionEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.freeplayerm.data.local.entity.UsuarioEntity

@Entity(
    tableName = "listas_reproduccion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borran sus listas
        )
    ]
)
data class ListaReproduccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_lista")
    val idLista: Int = 0,

    @ColumnInfo(name = "id_usuario", index = true)
    val idUsuario: Int,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String?,

    @ColumnInfo(name = "portada_url")
    val portadaUrl: String?
)