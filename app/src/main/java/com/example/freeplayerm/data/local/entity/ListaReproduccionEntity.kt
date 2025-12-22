// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ListaReproduccionEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎵 LISTA REPRODUCCION ENTITY - OPTIMIZADA v2.0
 *
 * Entidad que representa una playlist o lista de reproducción
 * Incluye metadata, permisos y estadísticas
 *
 * Características:
 * - Foreign key a usuario con CASCADE delete
 * - Soporte para listas públicas/privadas
 * - Contador de canciones y duración total
 * - Timestamps de creación y modificación
 * - Soporte para colaboración
 *
 * @version 2.0 - Enhanced
 */
@Entity(
    tableName = "listas_reproduccion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borran sus listas
        )
    ],
    indices = [
        Index(value = ["id_usuario"]),
        Index(value = ["nombre"]),
        Index(value = ["fecha_creacion"]),
        Index(value = ["es_publica"])
    ]
)
data class ListaReproduccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_lista")
    val idLista: Int = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    // ==================== INFORMACIÓN BÁSICA ====================

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "portada_url")
    val portadaUrl: String? = null,

    // ==================== CONFIGURACIÓN ====================

    @ColumnInfo(name = "es_publica")
    val esPublica: Boolean = false, // Si otros usuarios pueden verla

    @ColumnInfo(name = "es_colaborativa")
    val esColaborativa: Boolean = false, // Si otros usuarios pueden agregar canciones

    @ColumnInfo(name = "color_tema")
    val colorTema: String? = null, // Color hexadecimal para tema de la lista

    // ==================== TIMESTAMPS ====================

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Int = System.currentTimeMillis().toInt(),

    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Int = System.currentTimeMillis().toInt(),

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "total_canciones")
    val totalCanciones: Int = 0,

    @ColumnInfo(name = "duracion_total_segundos")
    val duracionTotalSegundos: Int = 0,

    @ColumnInfo(name = "veces_reproducida")
    val vecesReproducida: Int = 0,

    @ColumnInfo(name = "ultima_reproduccion")
    val ultimaReproduccion: Int? = null,

    // ==================== METADATA ====================

    @ColumnInfo(name = "categoria")
    val categoria: String? = null, // Workout, Relax, Party, etc.

    @ColumnInfo(name = "genero_principal")
    val generoPrincipal: String? = null, // Género predominante

    @ColumnInfo(name = "es_favorita")
    val esFavorita: Boolean = false, // Si el usuario la marcó como favorita

    @ColumnInfo(name = "orden_visualizacion")
    val ordenVisualizacion: Int = 0 // Para ordenamiento personalizado por usuario
) {
    /**
     * Duración formateada en HH:MM:SS
     */
    fun duracionFormateada(): String {
        val horas = duracionTotalSegundos / 3600
        val minutos = (duracionTotalSegundos % 3600) / 60
        val segundos = duracionTotalSegundos % 60

        return if (horas > 0) {
            String.format("%02d:%02d:%02d", horas, minutos, segundos)
        } else {
            String.format("%02d:%02d", minutos, segundos)
        }
    }

    /**
     * Verifica si la lista está vacía
     */
    fun estaVacia(): Boolean = totalCanciones == 0

    /**
     * Verifica si la lista es del sistema (nombres especiales)
     */
    fun esListaDelSistema(): Boolean = nombre in listasDelSistema

    companion object {
        // Listas especiales del sistema
        val listasDelSistema = listOf(
            "Favoritos",
            "Recientes",
            "Más Reproducidas",
            "Mi Mix",
            "Descubrimiento Semanal"
        )

        // Categorías predefinidas
        const val CATEGORIA_WORKOUT = "Workout"
        const val CATEGORIA_RELAX = "Relax"
        const val CATEGORIA_PARTY = "Party"
        const val CATEGORIA_FOCUS = "Focus"
        const val CATEGORIA_SLEEP = "Sleep"
        const val CATEGORIA_VIAJE = "Viaje"
        const val CATEGORIA_ESTUDIO = "Estudio"
        const val CATEGORIA_CUSTOM = "Custom"

        /**
         * Valida el nombre de la lista
         */
        fun esNombreValido(nombre: String): Boolean {
            return nombre.isNotBlank() && nombre.length <= 100
        }
    }
}