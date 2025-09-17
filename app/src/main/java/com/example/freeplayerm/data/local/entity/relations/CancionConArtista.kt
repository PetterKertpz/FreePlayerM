// en: app/src/main/java/com/example/freeplayerm/data/local/entity/relations/CancionConArtista.kt
package com.example.freeplayerm.data.local.entity.relations
import androidx.room.Embedded
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity

data class CancionConArtista(
    @Embedded
    val cancion: CancionEntity,

    val artistaNombre: String?,

    val albumNombre: String?,

    val generoNombre: String?,

    val esFavorita: Boolean,

    val portadaPath: String?
)