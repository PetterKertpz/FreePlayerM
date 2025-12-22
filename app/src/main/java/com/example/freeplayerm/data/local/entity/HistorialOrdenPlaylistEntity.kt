package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad OPCIONAL para registrar el historial de cambios de orden en playlists.
 *
 * ⚠️ ADVERTENCIA: Esta entidad genera muchos registros y puede afectar performance.
 * Solo usar si necesitas:
 * - Función "deshacer" cambios de orden
 * - Análisis de cómo los usuarios organizan sus playlists
 * - Auditoría de cambios en playlists colaborativas
 *
 * Si no necesitas estas funcionalidades, NO USES ESTA ENTIDAD.
 *
 * Relaciones:
 * - N:1 con ListaReproduccionEntity
 * - N:1 con UsuarioEntity (quien hizo el cambio)
 *
 * @property idHistorial ID único del registro de historial
 * @property idLista ID de la playlist (foreign key)
 * @property idUsuario ID del usuario que hizo el cambio (foreign key)
 * @property tipoOperacion Tipo de operación (REORDER, ADD, REMOVE, MOVE)
 * @property idCancion ID de la canción afectada (si aplica)
 * @property posicionAnterior Posición anterior de la canción
 * @property posicionNueva Posición nueva de la canción
 * @property detallesJson Detalles adicionales en JSON (para operaciones complejas)
 * @property timestamp Timestamp del cambio
 */
@Entity(
    tableName = "historial_orden_playlist",
    foreignKeys = [
        ForeignKey(
            entity = ListaReproduccionEntity::class,
            parentColumns = ["id_lista"],
            childColumns = ["id_lista"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_lista"]),
        Index(value = ["id_usuario"]),
        Index(value = ["timestamp"]),
        Index(value = ["tipo_operacion"])
    ]
)
data class HistorialOrdenPlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_historial")
    val idHistorial: Int = 0,

    // ==================== RELACIONES ====================

    @ColumnInfo(name = "id_lista")
    val idLista: Int,

    /**
     * Usuario que realizó el cambio
     * Puede ser null si fue una operación del sistema
     */
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int? = null,

    // ==================== OPERACIÓN ====================

    /**
     * Tipo de operación realizada
     * Valores: REORDER, ADD, REMOVE, MOVE, BATCH_REORDER, SHUFFLE, SORT
     */
    @ColumnInfo(name = "tipo_operacion")
    val tipoOperacion: String,

    /**
     * ID de la canción afectada (null para operaciones batch)
     */
    @ColumnInfo(name = "id_cancion")
    val idCancion: Int? = null,

    // ==================== CAMBIO DE POSICIÓN ====================

    /**
     * Posición anterior (orden antes del cambio)
     * null para operaciones ADD
     */
    @ColumnInfo(name = "posicion_anterior")
    val posicionAnterior: Int? = null,

    /**
     * Posición nueva (orden después del cambio)
     * null para operaciones REMOVE
     */
    @ColumnInfo(name = "posicion_nueva")
    val posicionNueva: Int? = null,

    // ==================== DETALLES ADICIONALES ====================

    /**
     * Detalles adicionales en JSON para operaciones complejas
     *
     * Para BATCH_REORDER: {"changes": [{"id":1,"from":5,"to":2}]}
     * Para SHUFFLE: {"seed": 12345, "count": 50}
     * Para SORT: {"criteria": "title", "direction": "asc"}
     */
    @ColumnInfo(name = "detalles_json")
    val detallesJson: String? = null,

    /**
     * Descripción legible de la operación
     */
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    // ==================== METADATOS ====================

    @ColumnInfo(name = "timestamp")
    val timestamp: Int = System.currentTimeMillis().toInt(),

    /**
     * Si esta operación es reversible (tiene suficiente información para deshacer)
     */
    @ColumnInfo(name = "reversible")
    val reversible: Boolean = true,

    /**
     * Si ya fue revertida
     */
    @ColumnInfo(name = "revertida")
    val revertida: Boolean = false,

    /**
     * ID del historial que revirtió esta operación (si aplica)
     */
    @ColumnInfo(name = "revertida_por")
    val revertidaPor: Int? = null

) {
    companion object {
        /**
         * Tipos de operación disponibles
         */
        object TipoOperacion {
            const val REORDER = "REORDER" // Cambio de una canción
            const val ADD = "ADD" // Canción agregada
            const val REMOVE = "REMOVE" // Canción eliminada
            const val MOVE = "MOVE" // Mover canción a otra posición
            const val BATCH_REORDER = "BATCH_REORDER" // Múltiples cambios simultáneos
            const val SHUFFLE = "SHUFFLE" // Aleatorización de toda la playlist
            const val SORT = "SORT" // Ordenamiento por criterio

            /**
             * Obtiene descripción legible del tipo
             */
            fun obtenerDescripcion(tipo: String): String {
                return when (tipo) {
                    REORDER -> "Reordenamiento"
                    ADD -> "Canción agregada"
                    REMOVE -> "Canción eliminada"
                    MOVE -> "Canción movida"
                    BATCH_REORDER -> "Reordenamiento múltiple"
                    SHUFFLE -> "Aleatorización"
                    SORT -> "Ordenamiento"
                    else -> tipo
                }
            }
        }

        /**
         * Política de retención: eliminar registros antiguos
         *
         * Para evitar que la tabla crezca infinitamente,
         * se recomienda mantener solo los últimos N días
         */
        const val DIAS_RETENCION_DEFAULT = 30

        /**
         * Máximo de registros por playlist
         * Después de este límite, eliminar los más antiguos
         */
        const val MAX_REGISTROS_POR_PLAYLIST = 100

        /**
         * Crea un registro de movimiento simple
         */
        fun crearMovimiento(
            idLista: Int,
            idUsuario: Int,
            idCancion: Int,
            posicionAnterior: Int,
            posicionNueva: Int
        ): HistorialOrdenPlaylistEntity {
            return HistorialOrdenPlaylistEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                tipoOperacion = TipoOperacion.MOVE,
                idCancion = idCancion,
                posicionAnterior = posicionAnterior,
                posicionNueva = posicionNueva,
                descripcion = "Movida de posición $posicionAnterior a $posicionNueva"
            )
        }

        /**
         * Crea un registro de canción agregada
         */
        fun crearAgregado(
            idLista: Int,
            idUsuario: Int,
            idCancion: Int,
            posicion: Int
        ): HistorialOrdenPlaylistEntity {
            return HistorialOrdenPlaylistEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                tipoOperacion = TipoOperacion.ADD,
                idCancion = idCancion,
                posicionNueva = posicion,
                descripcion = "Canción agregada en posición $posicion"
            )
        }

        /**
         * Crea un registro de canción eliminada
         */
        fun crearEliminado(
            idLista: Int,
            idUsuario: Int,
            idCancion: Int,
            posicionAnterior: Int
        ): HistorialOrdenPlaylistEntity {
            return HistorialOrdenPlaylistEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                tipoOperacion = TipoOperacion.REMOVE,
                idCancion = idCancion,
                posicionAnterior = posicionAnterior,
                descripcion = "Canción eliminada de posición $posicionAnterior"
            )
        }

        /**
         * Crea un registro de shuffle
         */
        fun crearShuffle(
            idLista: Int,
            idUsuario: Int,
            cantidadCanciones: Int,
            seed: Int? = null
        ): HistorialOrdenPlaylistEntity {
            val detalles = if (seed != null) {
                """{"seed": $seed, "count": $cantidadCanciones}"""
            } else {
                """{"count": $cantidadCanciones}"""
            }

            return HistorialOrdenPlaylistEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                tipoOperacion = TipoOperacion.SHUFFLE,
                detallesJson = detalles,
                descripcion = "Playlist aleatorizada ($cantidadCanciones canciones)",
                reversible = false // Shuffle no es reversible sin guardar orden anterior
            )
        }

        /**
         * Crea un registro de ordenamiento
         */
        fun crearOrdenamiento(
            idLista: Int,
            idUsuario: Int,
            criterio: String,
            direccion: String = "asc"
        ): HistorialOrdenPlaylistEntity {
            val detalles = """{"criteria": "$criterio", "direction": "$direccion"}"""

            return HistorialOrdenPlaylistEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                tipoOperacion = TipoOperacion.SORT,
                detallesJson = detalles,
                descripcion = "Ordenada por $criterio ($direccion)",
                reversible = false // Sort no es reversible sin guardar orden anterior
            )
        }
    }

    /**
     * Indica si la operación puede deshacerse
     */
    fun puedeDeshacer(): Boolean {
        return reversible && !revertida
    }

    /**
     * Indica si fue una operación del sistema (no de usuario)
     */
    fun esOperacionSistema(): Boolean {
        return idUsuario == null
    }

    /**
     * Obtiene descripción legible del tipo de operación
     */
    fun obtenerDescripcionTipo(): String {
        return TipoOperacion.obtenerDescripcion(tipoOperacion)
    }

    /**
     * Obtiene descripción completa de la operación
     */
    fun obtenerDescripcionCompleta(): String {
        return descripcion ?: when (tipoOperacion) {
            TipoOperacion.MOVE -> "Movida de posición $posicionAnterior a $posicionNueva"
            TipoOperacion.ADD -> "Agregada en posición $posicionNueva"
            TipoOperacion.REMOVE -> "Eliminada de posición $posicionAnterior"
            TipoOperacion.REORDER -> "Reordenamiento"
            TipoOperacion.BATCH_REORDER -> "Reordenamiento múltiple"
            TipoOperacion.SHUFFLE -> "Playlist aleatorizada"
            TipoOperacion.SORT -> "Playlist ordenada"
            else -> "Operación desconocida"
        }
    }

    /**
     * Calcula el cambio de posición (cuántos lugares se movió)
     */
    fun calcularCambioposicion(): Int? {
        return if (posicionAnterior != null && posicionNueva != null) {
            posicionNueva - posicionAnterior
        } else null
    }

    /**
     * Indica si fue un movimiento hacia adelante
     */
    fun movioHaciaAdelante(): Boolean {
        val cambio = calcularCambioposicion()
        return cambio != null && cambio > 0
    }

    /**
     * Indica si fue un movimiento hacia atrás
     */
    fun movioHaciaAtras(): Boolean {
        val cambio = calcularCambioposicion()
        return cambio != null && cambio < 0
    }

    /**
     * Copia marcando como revertida
     */
    fun marcarComoRevertida(idHistorialReversion: Int): HistorialOrdenPlaylistEntity {
        return copy(
            revertida = true,
            revertidaPor = idHistorialReversion
        )
    }

    /**
     * Crea la operación inversa para deshacer
     */
    fun crearOperacionInversa(): HistorialOrdenPlaylistEntity? {
        if (!puedeDeshacer()) return null

        return when (tipoOperacion) {
            TipoOperacion.MOVE, TipoOperacion.REORDER -> {
                // Invertir el movimiento
                copy(
                    idHistorial = 0, // Nuevo registro
                    posicionAnterior = posicionNueva,
                    posicionNueva = posicionAnterior,
                    descripcion = "Deshacer: ${obtenerDescripcionCompleta()}",
                    timestamp = System.currentTimeMillis().toInt()
                )
            }
            TipoOperacion.ADD -> {
                // Convertir ADD en REMOVE
                copy(
                    idHistorial = 0,
                    tipoOperacion = TipoOperacion.REMOVE,
                    posicionAnterior = posicionNueva,
                    posicionNueva = null,
                    descripcion = "Deshacer: ${obtenerDescripcionCompleta()}",
                    timestamp = System.currentTimeMillis().toInt()
                )
            }
            TipoOperacion.REMOVE -> {
                // Convertir REMOVE en ADD
                copy(
                    idHistorial = 0,
                    tipoOperacion = TipoOperacion.ADD,
                    posicionAnterior = null,
                    posicionNueva = posicionAnterior,
                    descripcion = "Deshacer: ${obtenerDescripcionCompleta()}",
                    timestamp = System.currentTimeMillis().toInt()
                )
            }
            else -> null // SHUFFLE y SORT no son reversibles
        }
    }
}