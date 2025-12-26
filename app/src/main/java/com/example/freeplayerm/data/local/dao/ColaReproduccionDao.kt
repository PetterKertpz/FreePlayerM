package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.ColaReproduccionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de la cola de reproducción.
 *
 * Maneja:
 * - CRUD de la cola por usuario
 * - Ordenamiento y reordenamiento
 * - Estado de reproducción
 * - Gestión de origen (manual/sugerencia/radio)
 */
@Dao
interface ColaReproduccionDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEnCola(item: ColaReproduccionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEnCola(items: List<ColaReproduccionEntity>): List<Long>

    // ==================== ACTUALIZACIÓN ====================

    @Update
    suspend fun actualizarItem(item: ColaReproduccionEntity): Int

    @Query("""
        UPDATE cola_reproduccion 
        SET reproducido = :reproducido
        WHERE id_cola = :id
    """)
    suspend fun marcarComoReproducido(id: Int, reproducido: Boolean = true): Int

    @Query("""
        UPDATE cola_reproduccion 
        SET orden = :nuevoOrden
        WHERE id_cola = :id
    """)
    suspend fun actualizarOrden(id: Int, nuevoOrden: Int): Int

    @Query("""
        UPDATE cola_reproduccion 
        SET origen = :origen
        WHERE id_cola = :id
    """)
    suspend fun actualizarOrigen(id: Int, origen: String): Int

    // ==================== ELIMINACIÓN ====================

    @Delete
    suspend fun eliminarItem(item: ColaReproduccionEntity): Int

    @Query("DELETE FROM cola_reproduccion WHERE id_cola = :id")
    suspend fun eliminarItemPorId(id: Int): Int

    @Query("DELETE FROM cola_reproduccion WHERE id_usuario = :usuarioId")
    suspend fun limpiarCola(usuarioId: Int): Int

    @Query("""
        DELETE FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 1
    """)
    suspend fun eliminarReproducidos(usuarioId: Int): Int

    @Query("""
        DELETE FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun eliminarCancionDeCola(usuarioId: Int, cancionId: Int): Int

    @Query("""
        DELETE FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cola IN (:ids)
    """)
    suspend fun eliminarItemsPorIds(usuarioId: Int, ids: List<Int>): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM cola_reproduccion WHERE id_cola = :id LIMIT 1")
    suspend fun obtenerItemPorId(id: Int): ColaReproduccionEntity?

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY orden ASC, fecha_agregado ASC
    """)
    fun obtenerColaCompleta(usuarioId: Int): Flow<List<ColaReproduccionEntity>>

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY orden ASC, fecha_agregado ASC
    """)
    suspend fun obtenerColaCompletaSync(usuarioId: Int): List<ColaReproduccionEntity>

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
        ORDER BY orden ASC, fecha_agregado ASC
    """)
    fun obtenerColaPendiente(usuarioId: Int): Flow<List<ColaReproduccionEntity>>

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
        ORDER BY orden ASC, fecha_agregado ASC
    """)
    suspend fun obtenerColaPendienteSync(usuarioId: Int): List<ColaReproduccionEntity>

    @Query("SELECT COUNT(*) FROM cola_reproduccion WHERE id_usuario = :usuarioId")
    suspend fun contarItemsCola(usuarioId: Int): Int

    @Query("SELECT COUNT(*) FROM cola_reproduccion WHERE id_usuario = :usuarioId")
    fun contarItemsColaFlow(usuarioId: Int): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
    """)
    suspend fun contarItemsPendientes(usuarioId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
    """)
    fun contarItemsPendientesFlow(usuarioId: Int): Flow<Int>

    // ==================== CANCIONES CON DETALLES ====================

    @Transaction
    @Query("""
        SELECT 
            c.*,
            a.nombre AS artistaNombre,
            al.titulo AS albumNombre,
            g.nombre AS generoNombre,
            (f.id_usuario IS NOT NULL) AS esFavorita,
            al.portada_path AS portadaPath,
            al.anio AS fechaLanzamiento
        FROM cola_reproduccion cr
        INNER JOIN canciones c ON cr.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE cr.id_usuario = :usuarioId 
          AND cr.reproducido = 0
        ORDER BY cr.orden ASC, cr.fecha_agregado ASC
    """)
    fun obtenerCancionesEnColaConDetalles(usuarioId: Int): Flow<List<CancionConArtista>>

    // ==================== SIGUIENTE/ANTERIOR ====================

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
        ORDER BY orden ASC, fecha_agregado ASC
        LIMIT 1
    """)
    suspend fun obtenerSiguienteEnCola(usuarioId: Int): ColaReproduccionEntity?

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
          AND orden > :ordenActual
        ORDER BY orden ASC
        LIMIT 1
    """)
    suspend fun obtenerSiguienteDespuesDe(usuarioId: Int, ordenActual: Int): ColaReproduccionEntity?

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND orden < :ordenActual
        ORDER BY orden DESC
        LIMIT 1
    """)
    suspend fun obtenerAnteriorAnteDe(usuarioId: Int, ordenActual: Int): ColaReproduccionEntity?

    // ==================== FILTROS POR ORIGEN ====================

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = :origen
        ORDER BY orden ASC, fecha_agregado ASC
    """)
    fun obtenerColaPorOrigen(usuarioId: Int, origen: String): Flow<List<ColaReproduccionEntity>>

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = 'MANUAL'
        ORDER BY orden ASC
    """)
    fun obtenerColaManuales(usuarioId: Int): Flow<List<ColaReproduccionEntity>>

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen IN ('SUGERENCIA', 'RADIO')
        ORDER BY orden ASC
    """)
    fun obtenerColaSugerencias(usuarioId: Int): Flow<List<ColaReproduccionEntity>>

    @Query("""
        SELECT COUNT(*) FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = :origen
    """)
    suspend fun contarItemsPorOrigen(usuarioId: Int, origen: String): Int

    // ==================== VERIFICACIÓN ====================

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM cola_reproduccion 
            WHERE id_usuario = :usuarioId 
              AND id_cancion = :cancionId 
              AND reproducido = 0
        )
    """)
    suspend fun cancionEstaEnCola(usuarioId: Int, cancionId: Int): Boolean

    @Query("""
        SELECT orden FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
        ORDER BY orden DESC 
        LIMIT 1
    """)
    suspend fun obtenerUltimoOrden(usuarioId: Int): Int?

    // ==================== REORDENAMIENTO ====================

    @Transaction
    suspend fun reordenarCola(usuarioId: Int, nuevosOrdenes: Map<Int, Int>) {
        nuevosOrdenes.forEach { (idCola, orden) ->
            actualizarOrden(idCola, orden)
        }
    }
    

    @Transaction
    suspend fun moverItem(usuarioId: Int, idCola: Int, nuevaPosicion: Int) {
        val item = obtenerItemPorId(idCola) ?: return
        val items = obtenerColaCompletaSync(usuarioId).toMutableList()

        val indiceActual = items.indexOfFirst { it.idCola == idCola }
        if (indiceActual == -1) return

        items.removeAt(indiceActual)
        items.add(nuevaPosicion, item)

        items.forEachIndexed { index, itemCola ->
            actualizarOrden(itemCola.idCola, index)
        }
    }

    @Transaction
    suspend fun moverAlFinal(usuarioId: Int, idCola: Int) {
        val ultimoOrden = obtenerUltimoOrden(usuarioId) ?: 0
        actualizarOrden(idCola, ultimoOrden + 1)
    }

    @Transaction
    suspend fun moverAlPrincipio(usuarioId: Int, idCola: Int) {
        val items = obtenerColaCompletaSync(usuarioId)

        // Incrementar orden de todos los items
        items.forEach { item ->
            if (item.idCola != idCola) {
                actualizarOrden(item.idCola, item.orden + 1)
            }
        }

        // Mover el item al principio
        actualizarOrden(idCola, 0)
    }

    // ==================== AGREGAR A COLA ====================

    @Transaction
    suspend fun agregarAlFinal(
        usuarioId: Int,
        cancionId: Int,
        origen: String = "MANUAL"
    ): Long {  // Cambiar a Long
        val ultimoOrden = obtenerUltimoOrden(usuarioId) ?: -1
        val item = ColaReproduccionEntity(
            idUsuario = usuarioId,
            idCancion = cancionId,
            orden = ultimoOrden + 1,
            origen = origen
        )
        return insertarEnCola(item)
    }

    @Transaction
    suspend fun agregarVariasAlFinal(
        usuarioId: Int,
        cancionIds: List<Int>,
        origen: String = "MANUAL"
    ): List<Long> {  // Cambiar a List<Long>
        var ordenActual = (obtenerUltimoOrden(usuarioId) ?: -1) + 1
        val items = cancionIds.map { cancionId ->
            ColaReproduccionEntity(
                idUsuario = usuarioId,
                idCancion = cancionId,
                orden = ordenActual++,
                origen = origen
            )
        }
        return insertarEnCola(items)
    }

    @Transaction
    suspend fun agregarSiguiente(
        usuarioId: Int,
        cancionId: Int,
        origen: String = "MANUAL"
    ): Long {  // Cambiar a Long
        val items = obtenerColaCompletaSync(usuarioId)

        items.filter { !it.reproducido }.forEach { item ->
            actualizarOrden(item.idCola, item.orden + 1)
        }

        val primerOrden = items.firstOrNull { !it.reproducido }?.orden ?: 0
        val item = ColaReproduccionEntity(
            idUsuario = usuarioId,
            idCancion = cancionId,
            orden = primerOrden,
            origen = origen
        )
        return insertarEnCola(item)
    }

    // ==================== REPRODUCIR SIGUIENTE ====================

    @Transaction
    suspend fun reproducirSiguiente(usuarioId: Int): ColaReproduccionEntity? {
        val siguiente = obtenerSiguienteEnCola(usuarioId)
        siguiente?.let {
            marcarComoReproducido(it.idCola, true)
        }
        return siguiente
    }

    @Transaction
    suspend fun marcarTodasComoReproducidas(usuarioId: Int) {
        val items = obtenerColaPendienteSync(usuarioId)
        items.forEach { item ->
            marcarComoReproducido(item.idCola, true)
        }
    }

    @Transaction
    suspend fun desmarcarTodasComoReproducidas(usuarioId: Int) {
        val items = obtenerColaCompletaSync(usuarioId)
        items.forEach { item ->
            marcarComoReproducido(item.idCola, false)
        }
    }

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    @Query("""
        DELETE FROM cola_reproduccion 
        WHERE id_cancion NOT IN (SELECT id_cancion FROM canciones)
    """)
    suspend fun limpiarColaHuerfana(): Int

    @Query("""
        DELETE FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_agregado < :fechaLimite
    """)
    suspend fun eliminarItemsAntiguos(usuarioId: Int, fechaLimite: Int): Int

    @Transaction
    suspend fun compactarOrdenes(usuarioId: Int) {
        val items = obtenerColaCompletaSync(usuarioId)
            .sortedBy { it.orden }

        items.forEachIndexed { index, item ->
            actualizarOrden(item.idCola, index)
        }
    }

    // ==================== ESTADÍSTICAS ====================

    @Query("""
        SELECT AVG(orden) FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
    """)
    suspend fun obtenerOrdenPromedio(usuarioId: Int): Float?

    @Query("""
        SELECT origen, COUNT(*) as cantidad 
        FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId
        GROUP BY origen
    """)
    suspend fun obtenerEstadisticasPorOrigen(usuarioId: Int): List<OrigenStats>

    data class OrigenStats(
        @ColumnInfo(name = "origen") val origen: String,
        @ColumnInfo(name = "cantidad") val cantidad: Int
    )

    // ==================== UTILIDADES ====================

    @Query("""
        SELECT * FROM cola_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND reproducido = 0
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun obtenerItemAleatorio(usuarioId: Int): ColaReproduccionEntity?

    @Transaction
    suspend fun mezclarCola(usuarioId: Int) {
        val items = obtenerColaPendienteSync(usuarioId).shuffled()
        items.forEachIndexed { index, item ->
            actualizarOrden(item.idCola, index)
        }
    }

    @Query("""
        SELECT SUM(c.duracion_segundos) 
        FROM cola_reproduccion cr
        INNER JOIN canciones c ON cr.id_cancion = c.id_cancion
        WHERE cr.id_usuario = :usuarioId 
          AND cr.reproducido = 0
    """)
    suspend fun obtenerDuracionTotalColaPendiente(usuarioId: Int): Int?
}