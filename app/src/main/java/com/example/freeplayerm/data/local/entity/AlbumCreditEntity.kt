package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar créditos de álbumes (productores, ingenieros, músicos, etc.)
 *
 * Los álbumes tienen múltiples personas involucradas en su creación más allá del artista. Esta
 * entidad permite registrar a todos los contribuyentes.
 *
 * Relaciones:
 * - N:1 con AlbumEntity (un álbum tiene múltiples créditos)
 * - Opcionalmente relacionado con ArtistEntity si el acreditado también es artista
 *
 * Casos de uso:
 * - Mostrar lista completa de créditos de un álbum
 * - Buscar todos los álbumes en los que trabajó un productor
 * - Explorar colaboraciones entre artistas y productores
 *
 * @property idCredito ID único del crédito
 * @property idAlbum ID del álbum (foreign key)
 * @property idArtista ID del artista si también es artista registrado (opcional)
 * @property nombre Nombre de la persona acreditada
 * @property rol Rol en la producción (producer, engineer, mixer, etc.)
 * @property rolesAdicionalesJson Otros roles que desempeñó (JSON array)
 * @property orden Orden de visualización
 * @property destacado Si debe mostrarse destacado (ej: productor ejecutivo)
 * @property fechaCreacion Timestamp de creación
 */
@Entity(
    tableName = "credito_album",
    foreignKeys =
        [
            ForeignKey(
                entity = AlbumEntity::class,
                parentColumns = ["id_album"],
                childColumns = ["id_album"],
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = ArtistEntity::class,
                parentColumns = ["id_artista"],
                childColumns = ["id_artista"],
                onDelete = ForeignKey.SET_NULL,
                onUpdate = ForeignKey.CASCADE,
            ),
        ],
    indices =
        [
            Index(value = ["id_album"]),
            Index(value = ["id_artista"]),
            Index(value = ["rol"]),
            Index(value = ["nombre"]),
            Index(value = ["destacado"]),
        ],
)
data class AlbumCreditEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_credito") val idCredito: Int = 0,

    // ==================== RELACIONES ====================

    @ColumnInfo(name = "id_album") val idAlbum: Int,

    /**
     * ID del artista si esta persona también es un artista registrado Permite vincular créditos con
     * perfiles de artista
     */
    @ColumnInfo(name = "id_artista") val idArtista: Int? = null,

    // ==================== INFORMACIÓN DE LA PERSONA ====================

    /** Nombre completo de la persona acreditada */
    @ColumnInfo(name = "nombre") val nombre: String,

    /** Alias o nombre artístico (si aplica) */
    @ColumnInfo(name = "alias") val alias: String? = null,

    // ==================== ROL EN EL ÁLBUM ====================

    /**
     * Rol principal en la producción del álbum Valores: PRODUCER, EXECUTIVE_PRODUCER, ENGINEER,
     * MIXING_ENGINEER, MASTERING_ENGINEER, COMPOSER, SONGWRITER, ARRANGER, MUSICIAN, VOCALIST,
     * FEATURED_ARTIST, GUEST, etc.
     */
    @ColumnInfo(name = "rol") val rol: String,

    /** Roles adicionales que desempeñó (JSON array) Ejemplo: ["engineer", "mixing"] */
    @ColumnInfo(name = "roles_adicionales_json") val rolesAdicionalesJson: String? = null,

    /**
     * Instrumentos que tocó (si es músico) - JSON array Ejemplo: ["guitar", "bass", "keyboards"]
     */
    @ColumnInfo(name = "instrumentos_json") val instrumentosJson: String? = null,

    /**
     * Tracks específicos en los que trabajó (JSON array de números) Si es null, significa que
     * trabajó en todo el álbum Ejemplo: [1,3,5,7] = trabajó en los tracks 1, 3, 5 y 7
     */
    @ColumnInfo(name = "tracks_json") val tracksJson: String? = null,

    // ==================== VISUALIZACIÓN ====================

    /** Orden de visualización dentro de su categoría de rol Menor = más importante (1 = primero) */
    @ColumnInfo(name = "orden") val orden: Int = 999,

    /**
     * Si debe mostrarse destacado en la lista de créditos Ejemplo: productor ejecutivo, productor
     * principal
     */
    @ColumnInfo(name = "destacado") val destacado: Boolean = false,

    // ==================== METADATOS ====================

    /**
     * Fuente de la información Valores: "MANUAL", "MUSICBRAINZ", "DISCOGS", "ALLMUSIC", "GENIUS"
     */
    @ColumnInfo(name = "fuente") val fuente: String = "MANUAL",

    /** URL a perfil externo (MusicBrainz, Discogs, etc.) */
    @ColumnInfo(name = "url_perfil") val urlPerfil: String? = null,
    @ColumnInfo(name = "notas") val notas: String? = null,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "activo") val activo: Boolean = true,
) {
    companion object {
        /** Roles disponibles en la producción de un álbum */
        object Rol {
            // Producción
            const val PRODUCER = "PRODUCER"
            const val EXECUTIVE_PRODUCER = "EXECUTIVE_PRODUCER"
            const val CO_PRODUCER = "CO_PRODUCER"
            const val ASSOCIATE_PRODUCER = "ASSOCIATE_PRODUCER"

            // Ingeniería
            const val ENGINEER = "ENGINEER"
            const val MIXING_ENGINEER = "MIXING_ENGINEER"
            const val MASTERING_ENGINEER = "MASTERING_ENGINEER"
            const val RECORDING_ENGINEER = "RECORDING_ENGINEER"
            const val ASSISTANT_ENGINEER = "ASSISTANT_ENGINEER"

            // Composición y escritura
            const val COMPOSER = "COMPOSER"
            const val SONGWRITER = "SONGWRITER"
            const val LYRICIST = "LYRICIST"
            const val ARRANGER = "ARRANGER"

            // Músicos
            const val MUSICIAN = "MUSICIAN"
            const val SESSION_MUSICIAN = "SESSION_MUSICIAN"
            const val VOCALIST = "VOCALIST"
            const val BACKGROUND_VOCALIST = "BACKGROUND_VOCALIST"

            // Artistas
            const val FEATURED_ARTIST = "FEATURED_ARTIST"
            const val GUEST_ARTIST = "GUEST_ARTIST"

            // Otros
            const val MIXER = "MIXER"
            const val PROGRAMMER = "PROGRAMMER"
            const val CONDUCTOR = "CONDUCTOR"
            const val ORCHESTRATOR = "ORCHESTRATOR"
            const val CHOIR_DIRECTOR = "CHOIR_DIRECTOR"
            const val ART_DIRECTION = "ART_DIRECTION"
            const val PHOTOGRAPHY = "PHOTOGRAPHY"
            const val GRAPHIC_DESIGN = "GRAPHIC_DESIGN"

            /** Obtiene el nombre para mostrar de un rol */
            fun obtenerNombreDisplay(rol: String): String {
                return when (rol) {
                    PRODUCER -> "Productor"
                    EXECUTIVE_PRODUCER -> "Productor Ejecutivo"
                    CO_PRODUCER -> "Co-Productor"
                    ASSOCIATE_PRODUCER -> "Productor Asociado"
                    ENGINEER -> "Ingeniero"
                    MIXING_ENGINEER -> "Ingeniero de Mezcla"
                    MASTERING_ENGINEER -> "Ingeniero de Masterización"
                    RECORDING_ENGINEER -> "Ingeniero de Grabación"
                    ASSISTANT_ENGINEER -> "Ingeniero Asistente"
                    COMPOSER -> "Compositor"
                    SONGWRITER -> "Compositor de Canciones"
                    LYRICIST -> "Letrista"
                    ARRANGER -> "Arreglista"
                    MUSICIAN -> "Músico"
                    SESSION_MUSICIAN -> "Músico de Sesión"
                    VOCALIST -> "Vocalista"
                    BACKGROUND_VOCALIST -> "Corista"
                    FEATURED_ARTIST -> "Artista Invitado"
                    GUEST_ARTIST -> "Artista Invitado"
                    MIXER -> "Mezclador"
                    PROGRAMMER -> "Programador"
                    CONDUCTOR -> "Director de Orquesta"
                    ORCHESTRATOR -> "Orquestador"
                    CHOIR_DIRECTOR -> "Director de Coro"
                    ART_DIRECTION -> "Dirección de Arte"
                    PHOTOGRAPHY -> "Fotografía"
                    GRAPHIC_DESIGN -> "Diseño Gráfico"
                    else -> rol.lowercase().replaceFirstChar { it.uppercase() }
                }
            }

            /** Obtiene la categoría del rol para agrupación */
            fun obtenerCategoria(rol: String): String {
                return when (rol) {
                    PRODUCER,
                    EXECUTIVE_PRODUCER,
                    CO_PRODUCER,
                    ASSOCIATE_PRODUCER -> "Producción"
                    ENGINEER,
                    MIXING_ENGINEER,
                    MASTERING_ENGINEER,
                    RECORDING_ENGINEER,
                    ASSISTANT_ENGINEER,
                    MIXER -> "Ingeniería"
                    COMPOSER,
                    SONGWRITER,
                    LYRICIST,
                    ARRANGER -> "Composición"
                    MUSICIAN,
                    SESSION_MUSICIAN -> "Instrumentistas"
                    VOCALIST,
                    BACKGROUND_VOCALIST -> "Voces"
                    FEATURED_ARTIST,
                    GUEST_ARTIST -> "Artistas Invitados"
                    ART_DIRECTION,
                    PHOTOGRAPHY,
                    GRAPHIC_DESIGN -> "Arte y Diseño"
                    else -> "Otros"
                }
            }

            /** Obtiene el orden de prioridad para visualización */
            fun obtenerPrioridad(rol: String): Int {
                return when (rol) {
                    EXECUTIVE_PRODUCER -> 1
                    PRODUCER -> 2
                    CO_PRODUCER -> 3
                    FEATURED_ARTIST -> 4
                    MIXING_ENGINEER -> 5
                    MASTERING_ENGINEER -> 6
                    ENGINEER -> 7
                    SONGWRITER -> 8
                    COMPOSER -> 9
                    MUSICIAN -> 10
                    else -> 99
                }
            }
        }

        /** Crea un crédito de productor */
        fun crearProductor(
            idAlbum: Int,
            nombre: String,
            ejecutivo: Boolean = false,
            destacado: Boolean = true,
        ): AlbumCreditEntity {
            return AlbumCreditEntity(
                idAlbum = idAlbum,
                nombre = nombre,
                rol = if (ejecutivo) Rol.EXECUTIVE_PRODUCER else Rol.PRODUCER,
                destacado = destacado,
                orden =
                    Rol.obtenerPrioridad(if (ejecutivo) Rol.EXECUTIVE_PRODUCER else Rol.PRODUCER),
            )
        }

        /** Crea un crédito de ingeniero */
        fun crearIngeniero(
            idAlbum: Int,
            nombre: String,
            tipoIngenieria: String = Rol.ENGINEER,
        ): AlbumCreditEntity {
            return AlbumCreditEntity(
                idAlbum = idAlbum,
                nombre = nombre,
                rol = tipoIngenieria,
                orden = Rol.obtenerPrioridad(tipoIngenieria),
            )
        }
    }

    /** Obtiene el nombre para mostrar del rol */
    fun obtenerNombreRol(): String {
        return Rol.obtenerNombreDisplay(rol)
    }

    /** Obtiene la categoría del rol */
    fun obtenerCategoriaRol(): String {
        return Rol.obtenerCategoria(rol)
    }

    /** Indica si trabajó en todo el álbum o en tracks específicos */
    fun trabajoEnTodoElAlbum(): Boolean {
        return tracksJson.isNullOrBlank()
    }

    /** Indica si debe mostrarse en la lista principal de créditos */
    fun esCreditoPrincipal(): Boolean {
        return destacado && activo
    }

    /** Indica si es un rol de producción */
    fun esRolProduccion(): Boolean {
        return rol in
            listOf(Rol.PRODUCER, Rol.EXECUTIVE_PRODUCER, Rol.CO_PRODUCER, Rol.ASSOCIATE_PRODUCER)
    }

    /** Indica si es un rol de ingeniería */
    fun esRolIngenieria(): Boolean {
        return rol.contains("ENGINEER") || rol == Rol.MIXER
    }

    /** Obtiene el nombre completo (con alias si existe) */
    fun obtenerNombreCompleto(): String {
        return if (alias != null) {
            "$nombre ($alias)"
        } else {
            nombre
        }
    }
}
