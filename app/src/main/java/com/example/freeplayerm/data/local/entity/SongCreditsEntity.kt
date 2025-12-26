package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎼 SONG CREDITS ENTITY
 *
 * Almacena créditos de canciones (productores, compositores, ingenieros, etc.)
 * Fuente: Genius API CustomPerformances
 */
@Entity(
    tableName = "song_credits",
    foreignKeys = [
        ForeignKey(
            entity = CancionEntity::class,
            parentColumns = ["id_cancion"],
            childColumns = ["id_cancion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArtistaEntity::class,
            parentColumns = ["id_artista"],
            childColumns = ["id_artista"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_cancion"]),
        Index(value = ["id_artista"]),
        Index(value = ["rol"])
    ]
)
data class SongCreditsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_credito")
    val idCredito: Int = 0,

    @ColumnInfo(name = "id_cancion")
    val idCancion: Int,

    @ColumnInfo(name = "id_artista")
    val idArtista: Int? = null,

    @ColumnInfo(name = "nombre_persona")
    val nombrePersona: String, // Si no hay artista asociado

    @ColumnInfo(name = "rol")
    val rol: String, // PRODUCER, WRITER, ENGINEER, MIXER, etc.

    @ColumnInfo(name = "orden")
    val orden: Int = 0,

    @ColumnInfo(name = "fuente")
    val fuente: String = "GENIUS",

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROL_PRODUCER = "PRODUCER"
        const val ROL_WRITER = "WRITER"
        const val ROL_COMPOSER = "COMPOSER"
        const val ROL_ENGINEER = "ENGINEER"
        const val ROL_MIXER = "MIXER"
        const val ROL_MASTERING = "MASTERING"
    }
}