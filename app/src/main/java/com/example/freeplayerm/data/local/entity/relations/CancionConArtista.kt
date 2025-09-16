// en: app/src/main/java/com/example/freeplayerm/data/local/entity/relations/CancionConArtista.kt
package com.example.freeplayerm.data.local.entity.relations
import androidx.room.Embedded
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity

data class CancionConArtista(
    @Embedded
    val cancion: CancionEntity,

    // CAMBIO: Se mantienen y ahora se unen a los nuevos campos.
    val artistaNombre: String?,

    // NUEVO: El nombre del álbum al que pertenece la canción.
    // Es opcional (nullable) por si una canción no tiene álbum.
    val albumNombre: String?,

    // NUEVO: El nombre del género de la canción.
    // También es opcional.
    val generoNombre: String?
)