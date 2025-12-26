package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar anotaciones de Genius sobre fragmentos específicos de letras.
 *
 * Las anotaciones de Genius son explicaciones, contexto histórico, referencias culturales
 * y análisis literario de fragmentos específicos de canciones.
 *
 * Relaciones:
 * - N:1 con LetraEntity (una letra puede tener múltiples anotaciones)
 *
 * Casos de uso:
 * - Mostrar explicaciones al tocar una línea de la letra
 * - Explorar el significado profundo de las canciones
 * - Aprender sobre referencias culturales y contexto
 *
 * @property idAnnotation ID único local de la anotación
 * @property idLetra ID de la letra (foreign key)
 * @property geniusAnnotationId ID de la anotación en Genius
 * @property fragmentoTexto Fragmento de texto al que se refiere la anotación
 * @property posicionInicio Posición de inicio del fragmento en el texto completo
 * @property posicionFin Posición de fin del fragmento en el texto completo
 * @property contenidoAnnotation Contenido HTML de la anotación
 * @property contenidoPlano Contenido en texto plano (sin HTML)
 * @property votos Votos en Genius (IQ points)
 * @property verificado Si fue verificado por editores de Genius
 * @property autores Lista de autores que contribuyeron (JSON)
 * @property fechaCreacion Timestamp de creación en Genius
 * @property fechaActualizacion Timestamp de última actualización
 * @property urlAnnotation URL directa a la anotación en Genius
 */
@Entity(
    tableName = "genius_annotations",
    foreignKeys = [
        ForeignKey(
            entity = LetraEntity::class,
            parentColumns = ["id_letra"],
            childColumns = ["id_letra"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_letra"]),
        Index(value = ["genius_annotation_id"], unique = true),
        Index(value = ["verificado"]),
        Index(value = ["votos"])
    ]
)
data class GeniusAnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_annotation")
    val idAnnotation: Int = 0,

    // ==================== RELACIÓN CON LETRA ====================

    @ColumnInfo(name = "id_letra")
    val idLetra: Int,

    // ==================== IDS GENIUS ====================

    /**
     * ID único de la anotación en Genius
     */
    @ColumnInfo(name = "genius_annotation_id")
    val geniusAnnotationId: String,

    // ==================== FRAGMENTO ANOTADO ====================

    /**
     * Fragmento exacto del texto al que se refiere
     * Ejemplo: "I got my driver's license last week"
     */
    @ColumnInfo(name = "fragmento_texto")
    val fragmentoTexto: String,

    /**
     * Posición del primer carácter del fragmento en el texto completo
     */
    @ColumnInfo(name = "posicion_inicio")
    val posicionInicio: Int,

    /**
     * Posición del último carácter del fragmento en el texto completo
     */
    @ColumnInfo(name = "posicion_fin")
    val posicionFin: Int,

    /**
     * Número de línea donde aparece (opcional, para navegación rápida)
     */
    @ColumnInfo(name = "numero_linea")
    val numeroLinea: Int? = null,

    // ==================== CONTENIDO DE LA ANOTACIÓN ====================

    /**
     * Contenido completo de la anotación en HTML
     * Genius devuelve anotaciones con formato HTML
     */
    @ColumnInfo(name = "contenido_annotation")
    val contenidoAnnotation: String,

    /**
     * Contenido en texto plano (sin HTML)
     * Para búsquedas y preview
     */
    @ColumnInfo(name = "contenido_plano")
    val contenidoPlano: String,

    // ==================== METADATOS GENIUS ====================

    /**
     * Votos de la anotación en Genius (IQ points)
     * Valores típicos: 0-500+
     */
    @ColumnInfo(name = "votos")
    val votos: Int = 0,

    /**
     * Si fue verificada/aprobada por editores de Genius
     */
    @ColumnInfo(name = "verificado")
    val verificado: Boolean = false,

    /**
     * Estado de la anotación en Genius
     * Valores: "accepted", "pending", "rejected"
     */
    @ColumnInfo(name = "estado")
    val estado: String = "accepted",

    /**
     * Lista de autores que contribuyeron (JSON)
     * Formato: [{"id":123,"name":"User","iq":1500}]
     */
    @ColumnInfo(name = "autores_json")
    val autoresJson: String? = null,

    // ==================== CATEGORIZACIÓN ====================

    /**
     * Tipo de anotación
     * Valores: "meaning", "reference", "wordplay", "context", "trivia", "other"
     */
    @ColumnInfo(name = "tipo_annotation")
    val tipoAnnotation: String = "meaning",

    /**
     * Tags de la anotación (JSON array)
     * Ejemplo: ["cultural-reference", "historical-context"]
     */
    @ColumnInfo(name = "tags_json")
    val tagsJson: String? = null,

    // ==================== URLS Y REFERENCIAS ====================

    @ColumnInfo(name = "url_annotation")
    val urlAnnotation: String,

    /**
     * URLs de referencia citadas en la anotación (JSON array)
     */
    @ColumnInfo(name = "referencias_urls_json")
    val referenciasUrlsJson: String? = null,

    // ==================== METADATOS TEMPORALES ====================

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Int,

    @ColumnInfo(name = "fecha_actualizacion")
    val fechaActualizacion: Int,

    /**
     * Última vez que se sincronizó desde Genius
     */
    @ColumnInfo(name = "ultima_sincronizacion")
    val ultimaSincronizacion: Long = System.currentTimeMillis(),

    // ==================== CONTROL LOCAL ====================

    /**
     * Si el usuario marcó como favorita
     */
    @ColumnInfo(name = "es_favorita")
    val esFavorita: Boolean = false,

    /**
     * Veces que el usuario vio esta anotación
     */
    @ColumnInfo(name = "veces_vista")
    val vecesVista: Int = 0,

    @ColumnInfo(name = "activa")
    val activa: Boolean = true

) {
    companion object {
        /**
         * Tipos de anotación disponibles
         */
        object TipoAnnotation {
            const val MEANING = "meaning" // Explicación del significado
            const val REFERENCE = "reference" // Referencias culturales/artísticas
            const val WORDPLAY = "wordplay" // Juegos de palabras
            const val CONTEXT = "context" // Contexto histórico/personal
            const val TRIVIA = "trivia" // Datos curiosos
            const val OTHER = "other" // Otros tipos
        }

        /**
         * Estados de anotación
         */
        object Estado {
            const val ACCEPTED = "accepted"
            const val PENDING = "pending"
            const val REJECTED = "rejected"
        }

        /**
         * Determina si una anotación es de alta calidad
         */
        fun esAltaCalidad(
            votos: Int,
            verificado: Boolean,
            estado: String
        ): Boolean {
            return (verificado && estado == Estado.ACCEPTED) ||
                    (votos >= 100 && estado == Estado.ACCEPTED)
        }

        /**
         * Extrae texto plano de HTML
         */
        fun extraerTextoPlano(html: String): String {
            return html
                .replace(Regex("<[^>]*>"), "") // Eliminar tags HTML
                .replace(Regex("&nbsp;"), " ")
                .replace(Regex("&amp;"), "&")
                .replace(Regex("&lt;"), "<")
                .replace(Regex("&gt;"), ">")
                .replace(Regex("&quot;"), "\"")
                .trim()
        }
    }

    /**
     * Indica si la anotación es de alta calidad
     */
    fun esAltaCalidad(): Boolean {
        return esAltaCalidad(votos, verificado, estado)
    }

    /**
     * Indica si debería mostrarse prominentemente
     */
    fun esDestacada(): Boolean {
        return verificado && votos >= 50 && activa
    }

    /**
     * Indica si necesita actualización (más de 30 días sin sincronizar)
     */
    fun necesitaSincronizacion(): Boolean {
        val treintaDias = 30L * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - ultimaSincronizacion > treintaDias
    }

    /**
     * Copia marcando como vista
     */
    fun marcarComoVista(): GeniusAnnotationEntity {
        return copy(vecesVista = vecesVista + 1)
    }

    /**
     * Copia alternando favorito
     */
    fun toggleFavorita(): GeniusAnnotationEntity {
        return copy(esFavorita = !esFavorita)
    }

    /**
     * Obtiene preview corto del contenido (primeros 100 chars)
     */
    fun obtenerPreview(maxLength: Int = 100): String {
        return if (contenidoPlano.length <= maxLength) {
            contenidoPlano
        } else {
            contenidoPlano.take(maxLength) + "..."
        }
    }

    /**
     * Valida que las posiciones sean correctas
     */
    fun posicionesValidas(): Boolean {
        return posicionInicio >= 0 && posicionFin > posicionInicio
    }
}