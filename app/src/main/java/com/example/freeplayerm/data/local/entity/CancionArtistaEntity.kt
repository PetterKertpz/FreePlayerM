// en: app/src/main/java/com/example/freeplayerm/data/local/entity/CancionArtistaEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 🎤 CANCION ARTISTA ENTITY - COLABORACIONES v1.0
 *
 * Entidad que representa la relación muchos a muchos entre canciones y artistas
 * Permite que una canción tenga múltiples artistas (colaboraciones, features, remixes)
 *
 * Características:
 * - Clave primaria compuesta (canción + artista + tipo)
 * - CASCADE delete: si se borra la canción o artista, se borra la relación
 * - Tipos de participación para diferenciar roles
 * - Orden para mostrar artistas en secuencia correcta
 * - Timestamps de cuándo se agregó la colaboración
 *
 * Casos de uso:
 * - Artista principal + features
 * - Remixers
 * - Productores
 * - Compositores
 * - Colaboraciones múltiples
 *
 * @version 1.0 - Initial Release
 */
@Entity(
    tableName = "cancion_artista",
    primaryKeys = ["id_cancion", "id_artista", "tipo_participacion"],
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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_cancion"]),
        Index(value = ["id_artista"]),
        Index(value = ["tipo_participacion"]),
        Index(value = ["id_cancion", "orden"]) // Para ordenar artistas por canción
    ]
)
data class CancionArtistaEntity(
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int,

    @ColumnInfo(name = "id_artista")
    val idArtista: Int,

    // ==================== TIPO DE PARTICIPACIÓN ====================

    @ColumnInfo(name = "tipo_participacion")
    val tipoParticipacion: String, // PRINCIPAL, FEATURING, REMIXER, PRODUCTOR, COMPOSITOR

    @ColumnInfo(name = "orden")
    val orden: Int = 0, // Orden de aparición (0 = principal, 1 = primer feat, etc.)

    // ==================== METADATA ====================

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "verificado")
    val verificado: Boolean = false, // Si la colaboración está verificada

    @ColumnInfo(name = "fuente")
    val fuente: String = FUENTE_LOCAL, // De dónde se obtuvo la info

    @ColumnInfo(name = "credito_texto")
    val creditoTexto: String? = null, // Texto del crédito si existe (ej: "prod. by X")

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "veces_reproducida_colaboracion")
    val vecesReproducidaColaboracion: Int = 0 // Reproducciones de esta colaboración específica
) {
    /**
     * Verifica si es el artista principal
     */
    fun esPrincipal(): Boolean = tipoParticipacion == TIPO_PRINCIPAL

    /**
     * Verifica si es un featuring
     */
    fun esFeaturing(): Boolean = tipoParticipacion == TIPO_FEATURING

    /**
     * Verifica si es un remixer
     */
    fun esRemixer(): Boolean = tipoParticipacion == TIPO_REMIXER

    /**
     * Verifica si es productor
     */
    fun esProductor(): Boolean = tipoParticipacion == TIPO_PRODUCTOR

    /**
     * Verifica si es compositor
     */
    fun esCompositor(): Boolean = tipoParticipacion == TIPO_COMPOSITOR

    /**
     * Obtiene el prefijo para mostrar según el tipo
     * Ejemplo: "feat.", "remix by", "prod. by"
     */
    fun obtenerPrefijoTipo(): String? {
        return when (tipoParticipacion) {
            TIPO_FEATURING -> "feat."
            TIPO_REMIXER -> "remix by"
            TIPO_PRODUCTOR -> "prod. by"
            TIPO_COMPOSITOR -> "written by"
            else -> null
        }
    }

    companion object {
        // Tipos de participación
        const val TIPO_PRINCIPAL = "PRINCIPAL"
        const val TIPO_FEATURING = "FEATURING"
        const val TIPO_REMIXER = "REMIXER"
        const val TIPO_PRODUCTOR = "PRODUCTOR"
        const val TIPO_COMPOSITOR = "COMPOSITOR"
        const val TIPO_COLABORADOR = "COLABORADOR" // Genérico
        const val TIPO_INVITADO = "INVITADO"

        // Fuentes
        const val FUENTE_LOCAL = "LOCAL"
        const val FUENTE_GENIUS = "GENIUS"
        const val FUENTE_SPOTIFY = "SPOTIFY"
        const val FUENTE_MANUAL = "MANUAL"
        const val FUENTE_METADATA = "METADATA" // De tags ID3

        /**
         * Crea una colaboración principal
         */
        fun crearPrincipal(idCancion: Int, idArtista: Int): CancionArtistaEntity {
            return CancionArtistaEntity(
                idCancion = idCancion,
                idArtista = idArtista,
                tipoParticipacion = TIPO_PRINCIPAL,
                orden = 0
            )
        }

        /**
         * Crea un featuring
         */
        fun crearFeaturing(
            idCancion: Int,
            idArtista: Int,
            orden: Int = 1
        ): CancionArtistaEntity {
            return CancionArtistaEntity(
                idCancion = idCancion,
                idArtista = idArtista,
                tipoParticipacion = TIPO_FEATURING,
                orden = orden
            )
        }

        /**
         * Crea un remixer
         */
        fun crearRemixer(idCancion: Int, idArtista: Int): CancionArtistaEntity {
            return CancionArtistaEntity(
                idCancion = idCancion,
                idArtista = idArtista,
                tipoParticipacion = TIPO_REMIXER,
                orden = 999 // Los remixers van al final
            )
        }

        /**
         * Crea un productor
         */
        fun crearProductor(idCancion: Int, idArtista: Int): CancionArtistaEntity {
            return CancionArtistaEntity(
                idCancion = idCancion,
                idArtista = idArtista,
                tipoParticipacion = TIPO_PRODUCTOR,
                orden = 998 // Los productores van antes del remixer
            )
        }

        /**
         * Parsea un string de artistas y crea las entidades
         * Ejemplo: "Artist A feat. Artist B, Artist C"
         * @return Lista de CancionArtistaEntity sin IDs asignados (usar después de insertar artistas)
         */
        fun parsearArtistasDeTexto(
            texto: String,
            idCancion: Int,
            mapaArtistas: Map<String, Int> // nombre -> id
        ): List<CancionArtistaEntity> {
            val resultado = mutableListOf<CancionArtistaEntity>()

            // Detectar patterns comunes
            val patronesFeat = listOf(
                Regex("""(feat\.|featuring|ft\.|with)\s*(.+)""", RegexOption.IGNORE_CASE),
                Regex("""&\s*(.+)"""),
                Regex(""",\s*(.+)""")
            )

            // Extraer artista principal (antes de cualquier feat/&/,)
            var textoRestante = texto
            var artistaPrincipal: String? = null

            patronesFeat.forEach { patron ->
                val match = patron.find(textoRestante)
                if (match != null) {
                    artistaPrincipal = textoRestante.take(match.range.first).trim()
                    textoRestante = match.groupValues.last()
                }
            }

            // Si no se encontró patrón, todo el texto es el artista principal
            if (artistaPrincipal == null) {
                artistaPrincipal = texto.trim()
            }

            // Agregar artista principal
            artistaPrincipal.let { nombre ->
                mapaArtistas[nombre]?.let { id ->
                    resultado.add(crearPrincipal(idCancion, id))
                }
            }

            // Agregar features (separar por comas, &, "and")
            val features = textoRestante.split(Regex(""",|&|\sand\s"""))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            features.forEachIndexed { index, nombre ->
                mapaArtistas[nombre]?.let { id ->
                    resultado.add(crearFeaturing(idCancion, id, index + 1))
                }
            }

            return resultado
        }
    }
}