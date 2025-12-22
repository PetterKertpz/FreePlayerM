// en: app/src/main/java/com/example/freeplayerm/data/local/entity/EstadoReproduccionEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎮 ESTADO REPRODUCCION ENTITY - PLAYER STATE v1.0
 *
 * Entidad singleton (1 por usuario) que almacena el estado actual del reproductor
 * Permite retomar la reproducción exactamente donde el usuario la dejó
 *
 * Características:
 * - Estado completo del player
 * - Canción actual y posición
 * - Modos de reproducción (shuffle, repeat)
 * - Volumen y configuración de audio
 * - Contexto de reproducción actual
 * - Persiste entre sesiones
 *
 * Casos de uso:
 * - "Continuar donde lo dejaste" al abrir app
 * - Sincronizar estado entre dispositivos
 * - Restaurar estado después de crash
 * - Mantener contexto de reproducción
 *
 * @version 1.0 - Initial Release
 */
@Entity(
    tableName = "estado_reproduccion",
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
            childColumns = ["id_cancion_actual"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["id_usuario"], unique = true), // Singleton por usuario
        Index(value = ["id_cancion_actual"])
    ]
)
data class EstadoReproduccionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    // ==================== CANCIÓN ACTUAL ====================

    @ColumnInfo(name = "id_cancion_actual")
    val idCancionActual: Int? = null,

    @ColumnInfo(name = "posicion_ms")
    val posicionMs: Int = 0, // Posición actual en milisegundos

    @ColumnInfo(name = "duracion_cancion_ms")
    val duracionCancionMs: Int = 0, // Duración total de la canción

    // ==================== ESTADO DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "esta_reproduciendo")
    val estaReproduciendo: Boolean = false,

    @ColumnInfo(name = "velocidad_reproduccion")
    val velocidadReproduccion: Float = 1.0f, // 0.5x - 2.0x

    @ColumnInfo(name = "pitch")
    val pitch: Float = 1.0f, // Tono de la música

    // ==================== MODOS DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "modo_repetir")
    val modoRepetir: String = MODO_REPETIR_NINGUNO, // "NONE", "ONE", "ALL"

    @ColumnInfo(name = "modo_aleatorio")
    val modoAleatorio: Boolean = false,

    @ColumnInfo(name = "modo_aleatorio_inteligente")
    val modoAleatorioInteligente: Boolean = false, // Shuffle basado en gustos

    // ==================== AUDIO ====================

    @ColumnInfo(name = "volumen")
    val volumen: Float = 0.7f, // 0.0 - 1.0

    @ColumnInfo(name = "silenciado")
    val silenciado: Boolean = false,

    @ColumnInfo(name = "ecualizador_activo")
    val ecualizadorActivo: Boolean = false,

    @ColumnInfo(name = "ecualizador_preset")
    val ecualizadorPreset: String? = null,

    // ==================== CONTEXTO DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "tipo_contexto")
    val tipoContexto: String? = null, // "PLAYLIST", "ALBUM", "ARTISTA", "GENERO", "FAVORITOS", "ALEATORIO"

    @ColumnInfo(name = "id_contexto")
    val idContexto: Int? = null, // ID de la playlist/álbum/etc

    @ColumnInfo(name = "nombre_contexto")
    val nombreContexto: String? = null, // Nombre para mostrar en UI

    @ColumnInfo(name = "portada_contexto")
    val portadaContexto: String? = null, // URL de la portada del contexto

    @ColumnInfo(name = "contexto_json")
    val contextoJson: String? = null, // Información adicional del contexto en JSON

    // ==================== COLA DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "indice_cola_actual")
    val indiceColaActual: Int = 0, // Posición en la cola

    @ColumnInfo(name = "total_canciones_cola")
    val totalCancionesCola: Int = 0,

    @ColumnInfo(name = "cola_ids_json")
    val colaIdsJson: String? = null, // Array de IDs de canciones en cola (JSON)

    @ColumnInfo(name = "cola_origen")
    val colaOrigen: String? = null, // De dónde se generó la cola

    @ColumnInfo(name = "cola_shuffle_seed")
    val colaShuffleSeed: Int? = null, // Seed para reproducir shuffle de forma consistente

    // ==================== HISTORIAL DE REPRODUCCIÓN (NAVEGACIÓN) ====================

    @ColumnInfo(name = "historial_navegacion_json")
    val historialNavegacionJson: String? = null, // IDs de canciones previas (para botón "anterior")

    @ColumnInfo(name = "puede_ir_anterior")
    val puedeIrAnterior: Boolean = false,

    @ColumnInfo(name = "puede_ir_siguiente")
    val puedeIrSiguiente: Boolean = false,

    // ==================== DISPOSITIVO ====================

    @ColumnInfo(name = "dispositivo_id")
    val dispositivoId: String? = null, // ID del dispositivo que guardó este estado

    @ColumnInfo(name = "salida_audio_actual")
    val salidaAudioActual: String? = null, // "SPEAKER", "HEADPHONES", "BLUETOOTH"

    // ==================== METADATA ====================

    @ColumnInfo(name = "ultima_actualizacion")
    val ultimaActualizacion: Int = System.currentTimeMillis().toInt(),

    @ColumnInfo(name = "version_estado")
    val versionEstado: Int = 1, // Para compatibilidad con versiones futuras

    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false, // Si se sincronizó con servidor

    @ColumnInfo(name = "sync_id")
    val syncId: String? = null // ID de sincronización cross-device
) {
    /**
     * Calcula el porcentaje de progreso de la canción
     */
    fun calcularProgreso(): Float {
        if (duracionCancionMs == 0) return 0f
        return (posicionMs.toFloat() / duracionCancionMs.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Verifica si está en pausa
     */
    fun estaEnPausa(): Boolean = idCancionActual != null && !estaReproduciendo

    /**
     * Verifica si está detenido (no hay canción)
     */
    fun estaDetenido(): Boolean = idCancionActual == null

    /**
     * Verifica si tiene contexto activo
     */
    fun tieneContexto(): Boolean = tipoContexto != null && idContexto != null

    /**
     * Verifica si está reproduciendo desde una playlist
     */
    fun estaEnPlaylist(): Boolean = tipoContexto == CONTEXTO_PLAYLIST

    /**
     * Verifica si está reproduciendo un álbum
     */
    fun estaEnAlbum(): Boolean = tipoContexto == CONTEXTO_ALBUM

    /**
     * Verifica si tiene cola de reproducción
     */
    fun tieneCola(): Boolean = totalCancionesCola > 0

    /**
     * Calcula cuántas canciones faltan en la cola
     */
    fun cancionesFaltantes(): Int {
        return (totalCancionesCola - indiceColaActual - 1).coerceAtLeast(0)
    }

    /**
     * Verifica si es la última canción de la cola
     */
    fun esUltimaCancion(): Boolean = indiceColaActual >= totalCancionesCola - 1

    /**
     * Verifica si es la primera canción de la cola
     */
    fun esPrimeraCancion(): Boolean = indiceColaActual == 0

    /**
     * Obtiene el modo de repetición en texto
     */
    fun obtenerTextoModoRepetir(): String {
        return when (modoRepetir) {
            MODO_REPETIR_NINGUNO -> "Sin repetición"
            MODO_REPETIR_UNO -> "Repetir canción"
            MODO_REPETIR_TODOS -> "Repetir cola"
            else -> "Desconocido"
        }
    }

    /**
     * Crea una copia con la canción actualizada
     */
    fun conCancionActualizada(
        idCancion: Int,
        duracionMs: Int,
        estaReproduciendo: Boolean = true
    ): EstadoReproduccionEntity {
        return copy(
            idCancionActual = idCancion,
            duracionCancionMs = duracionMs,
            posicionMs = 0,
            estaReproduciendo = estaReproduciendo,
            ultimaActualizacion = System.currentTimeMillis().toInt()
        )
    }

    /**
     * Crea una copia con la posición actualizada
     */
    fun conPosicionActualizada(nuevaPosicionMs: Int): EstadoReproduccionEntity {
        return copy(
            posicionMs = nuevaPosicionMs.coerceIn(0, duracionCancionMs),
            ultimaActualizacion = System.currentTimeMillis().toInt()
        )
    }

    companion object {
        // Modos de repetición
        const val MODO_REPETIR_NINGUNO = "NONE"
        const val MODO_REPETIR_UNO = "ONE"
        const val MODO_REPETIR_TODOS = "ALL"

        // Tipos de contexto
        const val CONTEXTO_PLAYLIST = "PLAYLIST"
        const val CONTEXTO_ALBUM = "ALBUM"
        const val CONTEXTO_ARTISTA = "ARTISTA"
        const val CONTEXTO_GENERO = "GENERO"
        const val CONTEXTO_FAVORITOS = "FAVORITOS"
        const val CONTEXTO_ALEATORIO = "ALEATORIO"
        const val CONTEXTO_BUSQUEDA = "BUSQUEDA"

        // Salidas de audio
        const val SALIDA_SPEAKER = "SPEAKER"
        const val SALIDA_HEADPHONES = "HEADPHONES"
        const val SALIDA_BLUETOOTH = "BLUETOOTH"
        const val SALIDA_EXTERNAL = "EXTERNAL"

        /**
         * Crea un estado inicial vacío para un usuario
         */
        fun crearVacio(idUsuario: Int): EstadoReproduccionEntity {
            return EstadoReproduccionEntity(idUsuario = idUsuario)
        }

        /**
         * Crea un estado con una canción lista para reproducir
         */
        fun crearConCancion(
            idUsuario: Int,
            idCancion: Int,
            duracionMs: Int,
            tipoContexto: String? = null,
            idContexto: Int? = null,
            nombreContexto: String? = null
        ): EstadoReproduccionEntity {
            return EstadoReproduccionEntity(
                idUsuario = idUsuario,
                idCancionActual = idCancion,
                duracionCancionMs = duracionMs,
                estaReproduciendo = false, // No auto-reproducir
                tipoContexto = tipoContexto,
                idContexto = idContexto,
                nombreContexto = nombreContexto
            )
        }

        /**
         * Parsea el array de IDs de cola desde JSON
         */
        fun parsearColaIds(colaIdsJson: String?): List<Int> {
            if (colaIdsJson.isNullOrBlank()) return emptyList()
            return try {
                com.google.gson.Gson().fromJson(
                    colaIdsJson,
                    object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type
                )
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Convierte lista de IDs a JSON
         */
        fun colaIdsAJson(ids: List<Int>): String {
            return com.google.gson.Gson().toJson(ids)
        }
    }
}