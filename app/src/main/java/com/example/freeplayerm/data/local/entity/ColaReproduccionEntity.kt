// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ColaReproduccionEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 📋 COLA REPRODUCCION ENTITY - QUEUE MANAGEMENT v1.0
 *
 * Entidad que representa la cola de reproducción dinámica de cada usuario
 * Permite agregar canciones "a continuación" o "al final de la cola"
 *
 * Características:
 * - Cola persistente entre sesiones
 * - Orden explícito y reordenable
 * - Origen de cada canción (manual, sugerencia, radio)
 * - Estado de reproducción (reproducido/pendiente)
 * - Timestamps de cuándo se agregó
 * - Soporte para múltiples usuarios
 *
 * Diferencia con EstadoReproduccionEntity:
 * - EstadoReproduccion: Estado ACTUAL del player (1 registro por usuario)
 * - ColaReproduccion: Lista DINÁMICA de canciones (N registros por usuario)
 *
 * Casos de uso:
 * - "Agregar a la cola"
 * - "Reproducir a continuación"
 * - "Ver cola de reproducción"
 * - "Reordenar cola"
 * - Radio inteligente (agregar sugerencias automáticamente)
 *
 * @version 1.0 - Initial Release
 */
@Entity(
    tableName = "cola_reproduccion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CancionEntity::class,
            parentColumns = ["id_cancion"],
            childColumns = ["id_cancion"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_usuario"]),
        Index(value = ["id_cancion"]),
        Index(value = ["id_usuario", "orden"]), // Para ordenar cola por usuario
        Index(value = ["reproducido"]),
        Index(value = ["fecha_agregado"])
    ]
)
data class ColaReproduccionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cola")
    val idCola: Int = 0,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    @ColumnInfo(name = "id_cancion")
    val idCancion: Int,

    // ==================== ORDEN Y ESTADO ====================

    @ColumnInfo(name = "orden")
    val orden: Int, // Posición en la cola (0 = siguiente, 1 = después, etc.)

    @ColumnInfo(name = "reproducido")
    val reproducido: Boolean = false, // Si ya se reprodujo

    @ColumnInfo(name = "fecha_reproducido")
    val fechaReproducido: Int? = null,

    // ==================== ORIGEN ====================

    @ColumnInfo(name = "origen")
    val origen: String, // "MANUAL", "SUGERENCIA", "RADIO", "AUTOPLAY"

    @ColumnInfo(name = "agregado_desde")
    val agregadoDesde: String? = null, // Contexto desde donde se agregó

    @ColumnInfo(name = "id_contexto_origen")
    val idContextoOrigen: Int? = null, // ID de playlist/álbum de origen

    // ==================== METADATA ====================

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Int = System.currentTimeMillis().toInt(),

    @ColumnInfo(name = "score_sugerencia")
    val scoreSugerencia: Float? = null, // Si es sugerencia, qué tan buena es (0.0-1.0)

    @ColumnInfo(name = "razon_sugerencia")
    val razonSugerencia: String? = null, // Por qué se sugirió ("similar_genre", "same_artist", etc.)

    // ==================== CONFIGURACIÓN ESPECIAL ====================

    @ColumnInfo(name = "inicio_personalizado_ms")
    val inicioPersonalizadoMs: Int? = null, // Empezar en X segundos (para intros largas)

    @ColumnInfo(name = "fin_personalizado_ms")
    val finPersonalizadoMs: Int? = null, // Terminar en X segundos (para outros largos)

    @ColumnInfo(name = "transicion_tipo")
    val transicionTipo: String = TRANSICION_NORMAL, // "NORMAL", "CROSSFADE", "GAPLESS", "INSTANT"

    @ColumnInfo(name = "transicion_duracion_ms")
    val transicionDuracionMs: Int? = null, // Duración del crossfade si aplica

    // ==================== SINCRONIZACIÓN ====================

    @ColumnInfo(name = "dispositivo_id")
    val dispositivoId: String? = null, // Dispositivo que la agregó

    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false,

    @ColumnInfo(name = "sync_id")
    val syncId: String? = null
) {
    /**
     * Verifica si fue agregada manualmente
     */
    fun esManual(): Boolean = origen == ORIGEN_MANUAL

    /**
     * Verifica si es una sugerencia
     */
    fun esSugerencia(): Boolean = origen == ORIGEN_SUGERENCIA

    /**
     * Verifica si es parte del radio automático
     */
    fun esRadio(): Boolean = origen == ORIGEN_RADIO

    /**
     * Verifica si es autoplay
     */
    fun esAutoplay(): Boolean = origen == ORIGEN_AUTOPLAY

    /**
     * Verifica si tiene transición especial
     */
    fun tieneTransicionEspecial(): Boolean = transicionTipo != TRANSICION_NORMAL

    /**
     * Verifica si tiene tiempos personalizados
     */
    fun tieneTiemposPersonalizados(): Boolean =
        inicioPersonalizadoMs != null || finPersonalizadoMs != null

    /**
     * Calcula cuánto tiempo lleva en la cola
     */
    fun tiempoEnColaMs(): Int = System.currentTimeMillis().toInt() - fechaAgregado

    /**
     * Calcula cuánto tiempo lleva en la cola en minutos
     */
    fun tiempoEnColaMinutos(): Int = tiempoEnColaMs() / (60 * 1000)

    /**
     * Verifica si es una buena sugerencia (score > 0.7)
     */
    fun esBuenaSugerencia(): Boolean = (scoreSugerencia ?: 0f) > 0.7f

    /**
     * Crea una copia marcada como reproducida
     */
    fun marcarComoReproducida(): ColaReproduccionEntity {
        return copy(
            reproducido = true,
            fechaReproducido = System.currentTimeMillis().toInt()
        )
    }

    companion object {
        // Orígenes
        const val ORIGEN_MANUAL = "MANUAL" // Usuario la agregó explícitamente
        const val ORIGEN_SUGERENCIA = "SUGERENCIA" // Sistema la sugirió
        const val ORIGEN_RADIO = "RADIO" // Radio inteligente
        const val ORIGEN_AUTOPLAY = "AUTOPLAY" // Continuar reproducción automática
        const val ORIGEN_COLABORATIVO = "COLABORATIVO" // Otro usuario en sesión compartida

        // Tipos de transición
        const val TRANSICION_NORMAL = "NORMAL"
        const val TRANSICION_CROSSFADE = "CROSSFADE"
        const val TRANSICION_GAPLESS = "GAPLESS"
        const val TRANSICION_INSTANT = "INSTANT" // Sin silencio ni crossfade
        const val TRANSICION_FADE_OUT = "FADE_OUT" // Solo fade out de la anterior

        // Razones de sugerencia
        const val RAZON_MISMO_ARTISTA = "same_artist"
        const val RAZON_MISMO_GENERO = "same_genre"
        const val RAZON_MISMO_ALBUM = "same_album"
        const val RAZON_SIMILAR_TEMPO = "similar_tempo"
        const val RAZON_SIMILAR_MOOD = "similar_mood"
        const val RAZON_FRECUENTEMENTE_JUNTAS = "frequently_together" // Se reproducen juntas a menudo
        const val RAZON_MISMA_EPOCA = "same_era"
        const val RAZON_POPULAR = "popular"
        const val RAZON_NUEVA = "new_release"

        /**
         * Crea una entrada manual en la cola
         */
        fun crearManual(
            idUsuario: Int,
            idCancion: Int,
            orden: Int,
            agregadoDesde: String? = null
        ): ColaReproduccionEntity {
            return ColaReproduccionEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                orden = orden,
                origen = ORIGEN_MANUAL,
                agregadoDesde = agregadoDesde
            )
        }

        /**
         * Crea una sugerencia en la cola
         */
        fun crearSugerencia(
            idUsuario: Int,
            idCancion: Int,
            orden: Int,
            score: Float,
            razon: String
        ): ColaReproduccionEntity {
            return ColaReproduccionEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                orden = orden,
                origen = ORIGEN_SUGERENCIA,
                scoreSugerencia = score,
                razonSugerencia = razon
            )
        }

        /**
         * Crea una entrada de radio
         */
        fun crearRadio(
            idUsuario: Int,
            idCancion: Int,
            orden: Int,
            razon: String? = null
        ): ColaReproduccionEntity {
            return ColaReproduccionEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                orden = orden,
                origen = ORIGEN_RADIO,
                razonSugerencia = razon
            )
        }

        /**
         * Crea una entrada de autoplay
         */
        fun crearAutoplay(
            idUsuario: Int,
            idCancion: Int,
            orden: Int
        ): ColaReproduccionEntity {
            return ColaReproduccionEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                orden = orden,
                origen = ORIGEN_AUTOPLAY
            )
        }

        /**
         * Crea entrada con crossfade
         */
        fun crearConCrossfade(
            idUsuario: Int,
            idCancion: Int,
            orden: Int,
            crossfadeDuracionMs: Int
        ): ColaReproduccionEntity {
            return crearManual(idUsuario, idCancion, orden).copy(
                transicionTipo = TRANSICION_CROSSFADE,
                transicionDuracionMs = crossfadeDuracionMs
            )
        }

        /**
         * Reordena una lista de entradas de cola
         * Actualiza el campo 'orden' de cada una
         */
        fun reordenar(items: List<ColaReproduccionEntity>): List<ColaReproduccionEntity> {
            return items.mapIndexed { index, item ->
                item.copy(orden = index)
            }
        }

        /**
         * Inserta una canción en una posición específica
         * Retorna la lista actualizada con órdenes correctos
         */
        fun insertarEn(
            lista: List<ColaReproduccionEntity>,
            nuevaEntrada: ColaReproduccionEntity,
            posicion: Int
        ): List<ColaReproduccionEntity> {
            val mutableList = lista.toMutableList()
            mutableList.add(posicion, nuevaEntrada)
            return reordenar(mutableList)
        }
    }
}