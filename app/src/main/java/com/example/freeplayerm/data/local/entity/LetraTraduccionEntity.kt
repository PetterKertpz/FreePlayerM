package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar traducciones de letras en múltiples idiomas.
 *
 * Relaciones:
 * - N:1 con LetraEntity (una letra puede tener múltiples traducciones)
 *
 * Casos de uso:
 * - Mostrar letras en el idioma preferido del usuario
 * - Comparar letra original con traducción
 * - Comunidad contribuyendo traducciones
 *
 * @property idTraduccion ID único de la traducción
 * @property idLetra ID de la letra original (foreign key)
 * @property idioma Código ISO 639-1 del idioma (es, en, fr, etc.)
 * @property textoTraducido Texto completo de la letra traducida
 * @property fuente Fuente de la traducción (Genius, Musixmatch, comunidad, etc.)
 * @property idTraductorUsuario ID del usuario que tradujo (si es contribución de comunidad)
 * @property verificada Si la traducción fue verificada por moderadores
 * @property confiabilidad Puntuación de confiabilidad 0-100
 * @property votos Total de votos positivos - negativos de la comunidad
 * @property formato Formato de la traducción (LRC si está sincronizada)
 * @property lrcContent Contenido LRC si la traducción está sincronizada
 * @property fechaCreacion Timestamp de creación
 * @property fechaActualizacion Timestamp de última actualización
 * @property reportesError Cantidad de reportes de error recibidos
 */
@Entity(
    tableName = "letra_traduccion",
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
        Index(value = ["idioma"]),
        Index(value = ["id_letra", "idioma"], unique = true), // Una traducción por idioma
        Index(value = ["verificada"]),
        Index(value = ["confiabilidad"])
    ]
)
data class LetraTraduccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_traduccion")
    val idTraduccion: Int = 0,

    // ==================== RELACIÓN CON LETRA ====================

    @ColumnInfo(name = "id_letra")
    val idLetra: Int,

    // ==================== INFORMACIÓN DE IDIOMA ====================

    /**
     * Código ISO 639-1 del idioma
     * Ejemplos: es, en, fr, de, pt, it, ja, ko, zh
     */
    @ColumnInfo(name = "idioma")
    val idioma: String,

    @ColumnInfo(name = "nombre_idioma")
    val nombreIdioma: String, // "Español", "English", etc.

    // ==================== CONTENIDO ====================

    @ColumnInfo(name = "texto_traducido")
    val textoTraducido: String,

    // ==================== FUENTE Y VERIFICACIÓN ====================

    /**
     * Fuente de la traducción
     * Valores: "GENIUS", "MUSIXMATCH", "USUARIO", "GOOGLE_TRANSLATE", "DEEPL", "IMPORTADA"
     */
    @ColumnInfo(name = "fuente")
    val fuente: String = "USUARIO",

    /**
     * ID del usuario que contribuyó la traducción (si es fuente USUARIO)
     */
    @ColumnInfo(name = "id_traductor_usuario")
    val idTraductorUsuario: Int? = null,

    /**
     * Si fue verificada por moderadores o fuente confiable
     */
    @ColumnInfo(name = "verificada")
    val verificada: Boolean = false,

    /**
     * Puntuación de confiabilidad 0-100
     * Basada en: fuente, verificación, votos, reportes
     */
    @ColumnInfo(name = "confiabilidad")
    val confiabilidad: Int = 0,

    // ==================== COMUNIDAD ====================

    /**
     * Votos positivos - votos negativos
     */
    @ColumnInfo(name = "votos")
    val votos: Int = 0,

    @ColumnInfo(name = "reportes_error")
    val reportesError: Int = 0,

    // ==================== SINCRONIZACIÓN ====================

    /**
     * Formato de sincronización si aplica
     * Valores: "NONE", "LRC", "SRT"
     */
    @ColumnInfo(name = "formato_sincronizacion")
    val formatoSincronizacion: String = "NONE",

    /**
     * Contenido LRC completo si está sincronizada
     */
    @ColumnInfo(name = "lrc_content")
    val lrcContent: String? = null,

    // ==================== METADATOS ====================

    @ColumnInfo(name = "notas_traductor")
    val notasTraductor: String? = null, // Notas sobre la traducción

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "fecha_actualizacion")
    val fechaActualizacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "activa")
    val activa: Boolean = true // Para soft delete

) {
    companion object {
        /**
         * Idiomas soportados oficialmente
         */
        val IDIOMAS_SOPORTADOS = mapOf(
            "es" to "Español",
            "en" to "English",
            "fr" to "Français",
            "de" to "Deutsch",
            "pt" to "Português",
            "it" to "Italiano",
            "ja" to "日本語",
            "ko" to "한국어",
            "zh" to "中文",
            "ru" to "Русский",
            "ar" to "العربية"
        )

        /**
         * Fuentes confiables que no requieren verificación manual
         */
        val FUENTES_CONFIABLES = listOf(
            "GENIUS",
            "MUSIXMATCH",
            "DEEPL"
        )

        /**
         * Determina la confiabilidad basada en fuente, verificación y votos
         */
        fun calcularConfiabilidad(
            fuente: String,
            verificada: Boolean,
            votos: Int,
            reportes: Int
        ): Int {
            var score = when {
                verificada -> 90
                fuente in FUENTES_CONFIABLES -> 75
                fuente == "USUARIO" -> 50
                fuente == "GOOGLE_TRANSLATE" -> 40
                else -> 30
            }

            // Ajustar por votos (cada 5 votos positivos = +5 puntos, max +20)
            val ajusteVotos = (votos / 5).coerceIn(-10, 20)
            score += ajusteVotos

            // Penalizar por reportes (cada reporte = -10 puntos)
            score -= (reportes * 10)

            return score.coerceIn(0, 100)
        }

        /**
         * Determina si una traducción debería mostrarse como predeterminada
         */
        fun esTraduccionPredeterminada(
            traduccion: LetraTraduccionEntity,
            idiomaUsuario: String
        ): Boolean {
            return traduccion.idioma == idiomaUsuario &&
                    traduccion.confiabilidad >= 60 &&
                    traduccion.activa
        }
    }

    /**
     * Indica si la traducción está sincronizada (tiene LRC)
     */
    fun estaSincronizada(): Boolean {
        return formatoSincronizacion == "LRC" && !lrcContent.isNullOrBlank()
    }

    /**
     * Indica si es una traducción confiable para mostrar
     */
    fun esConfiable(): Boolean {
        return confiabilidad >= 60 && reportesError < 3 && activa
    }

    /**
     * Indica si necesita revisión (muchos reportes o baja confiabilidad)
     */
    fun necesitaRevision(): Boolean {
        return reportesError >= 3 || (confiabilidad < 40 && !verificada)
    }

    /**
     * Copia con actualización de metadatos
     */
    fun actualizarContenido(nuevoTexto: String): LetraTraduccionEntity {
        return copy(
            textoTraducido = nuevoTexto,
            fechaActualizacion = System.currentTimeMillis(),
            confiabilidad = calcularConfiabilidad(fuente, verificada, votos, reportesError)
        )
    }

    /**
     * Copia con nuevo voto
     */
    fun agregarVoto(positivo: Boolean): LetraTraduccionEntity {
        val nuevosVotos = if (positivo) votos + 1 else votos - 1
        return copy(
            votos = nuevosVotos,
            confiabilidad = calcularConfiabilidad(fuente, verificada, nuevosVotos, reportesError)
        )
    }

    /**
     * Copia con nuevo reporte de error
     */
    fun agregarReporte(): LetraTraduccionEntity {
        val nuevosReportes = reportesError + 1
        return copy(
            reportesError = nuevosReportes,
            confiabilidad = calcularConfiabilidad(fuente, verificada, votos, nuevosReportes),
            activa = nuevosReportes < 5 // Desactivar automáticamente con 5+ reportes
        )
    }
}