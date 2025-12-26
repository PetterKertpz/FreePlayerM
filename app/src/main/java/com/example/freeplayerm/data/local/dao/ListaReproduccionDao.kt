package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de playlists en la base de datos.
 * ✅ CORREGIDO - Campos sincronizados con ListaReproduccionEntity
 */
@Dao
interface ListaReproduccionDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(lista: ListaReproduccionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListas(listas: List<ListaReproduccionEntity>): List<Long>

    // ==================== ACTUALIZACIÓN ====================

    @Update
    suspend fun actualizarLista(lista: ListaReproduccionEntity): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET nombre = :nombre,
            descripcion = :descripcion,
            fecha_modificacion = :timestamp
        WHERE id_lista = :id
    """)
    suspend fun actualizarInformacion(
        id: Int,
        nombre: String,
        descripcion: String?,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET portada_url = :portadaUrl,
            fecha_modificacion = :timestamp
        WHERE id_lista = :id
    """)
    suspend fun actualizarPortada(
        id: Int,
        portadaUrl: String?,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET es_publica = :esPublica,
            fecha_modificacion = :timestamp
        WHERE id_lista = :id
    """)
    suspend fun cambiarVisibilidad(
        id: Int,
        esPublica: Boolean,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET veces_reproducida = veces_reproducida + :incremento,
            ultima_reproduccion = :timestamp
        WHERE id_lista = :id
    """)
    suspend fun incrementarReproduccionesLista(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ELIMINACIÓN ====================

    @Delete
    suspend fun eliminarLista(lista: ListaReproduccionEntity): Int

    @Query("DELETE FROM listas_reproduccion WHERE id_lista = :id")
    suspend fun eliminarListaPorId(id: Int): Int

    @Query("DELETE FROM listas_reproduccion WHERE total_canciones = 0 AND fecha_creacion < :timestampLimite")
    suspend fun eliminarListasVaciasAntiguas(timestampLimite: Int): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM listas_reproduccion WHERE id_lista = :id LIMIT 1")
    suspend fun obtenerListaPorId(id: Int): ListaReproduccionEntity?

    @Query("SELECT * FROM listas_reproduccion WHERE id_lista = :id LIMIT 1")
    fun obtenerListaPorIdFlow(id: Int): Flow<ListaReproduccionEntity?>

    @Query("SELECT * FROM listas_reproduccion ORDER BY nombre ASC")
    fun obtenerTodasLasListas(): Flow<List<ListaReproduccionEntity>>

    @Query("SELECT COUNT(*) FROM listas_reproduccion")
    suspend fun contarListas(): Int

    @Query("SELECT COUNT(*) FROM listas_reproduccion")
    fun contarListasFlow(): Flow<Int>

    // ==================== BÚSQUEDA ====================

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE nombre LIKE '%' || :query || '%'
           OR descripcion LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN nombre LIKE :query || '%' THEN 1 ELSE 2 END,
            nombre ASC
        LIMIT :limite
    """)
    fun buscarListas(query: String, limite: Int = 50): Flow<List<ListaReproduccionEntity>>

    // ==================== FILTROS POR USUARIO ====================

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_modificacion DESC
    """)
    fun obtenerListasDelUsuario(usuarioId: Int): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_modificacion DESC
    """)
    suspend fun obtenerListasDelUsuarioSync(usuarioId: Int): List<ListaReproduccionEntity>

    @Query("""
        SELECT COUNT(*) FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
    """)
    suspend fun contarListasDelUsuario(usuarioId: Int): Int

    // ==================== FILTROS POR VISIBILIDAD ====================

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE es_publica = 1
        ORDER BY veces_reproducida DESC
        LIMIT :limite
    """)
    fun obtenerListasPublicas(limite: Int = 50): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND es_publica = 0
        ORDER BY fecha_modificacion DESC
    """)
    fun obtenerListasPrivadasDelUsuario(usuarioId: Int): Flow<List<ListaReproduccionEntity>>

    // ==================== COLABORATIVAS ====================

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE es_colaborativa = 1
          AND id_usuario = :usuarioId
        ORDER BY fecha_modificacion DESC
    """)
    fun obtenerListasColaborativas(usuarioId: Int): Flow<List<ListaReproduccionEntity>>

    // ==================== ORDENAMIENTO Y RANKINGS ====================

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_creacion DESC
        LIMIT :limite
    """)
    fun obtenerListasRecientes(usuarioId: Int, limite: Int = 20): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY veces_reproducida DESC
        LIMIT :limite
    """)
    fun obtenerListasMasEscuchadas(usuarioId: Int, limite: Int = 20): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY total_canciones DESC
        LIMIT :limite
    """)
    fun obtenerListasMasGrandes(usuarioId: Int, limite: Int = 20): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
          AND ultima_reproduccion IS NOT NULL
        ORDER BY ultima_reproduccion DESC
        LIMIT :limite
    """)
    fun obtenerListasReproducidasRecientemente(usuarioId: Int, limite: Int = 10): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE es_publica = 1
        ORDER BY veces_reproducida DESC
        LIMIT :limite
    """)
    fun obtenerListasPublicasPopulares(limite: Int = 50): Flow<List<ListaReproduccionEntity>>

    // ==================== ESTADÍSTICAS ====================

    @Query("""
        UPDATE listas_reproduccion 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM detalle_lista_reproduccion 
            WHERE detalle_lista_reproduccion.id_lista = listas_reproduccion.id_lista
        ),
        duracion_total_segundos = (
            SELECT COALESCE(SUM(c.duracion_segundos), 0)
            FROM detalle_lista_reproduccion dlr
            INNER JOIN canciones c ON dlr.id_cancion = c.id_cancion
            WHERE dlr.id_lista = listas_reproduccion.id_lista
        ),
        fecha_modificacion = :timestamp
        WHERE id_lista = :id
    """)
    suspend fun recalcularEstadisticas(
        id: Int,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM detalle_lista_reproduccion 
            WHERE detalle_lista_reproduccion.id_lista = listas_reproduccion.id_lista
        ),
        duracion_total_segundos = (
            SELECT COALESCE(SUM(c.duracion_segundos), 0)
            FROM detalle_lista_reproduccion dlr
            INNER JOIN canciones c ON dlr.id_cancion = c.id_cancion
            WHERE dlr.id_lista = listas_reproduccion.id_lista
        ),
        fecha_modificacion = :timestamp
        WHERE id_usuario = :usuarioId
    """)
    suspend fun recalcularEstadisticasDelUsuario(
        usuarioId: Int,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE listas_reproduccion 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM detalle_lista_reproduccion 
            WHERE detalle_lista_reproduccion.id_lista = listas_reproduccion.id_lista
        ),
        duracion_total_segundos = (
            SELECT COALESCE(SUM(c.duracion_segundos), 0)
            FROM detalle_lista_reproduccion dlr
            INNER JOIN canciones c ON dlr.id_cancion = c.id_cancion
            WHERE dlr.id_lista = listas_reproduccion.id_lista
        ),
        fecha_modificacion = :timestamp
    """)
    suspend fun recalcularTodasLasEstadisticas(
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ANÁLISIS ====================

    @Query("""
        SELECT AVG(total_canciones) FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId AND total_canciones > 0
    """)
    suspend fun obtenerPromedioCancionesPorLista(usuarioId: Int): Float?

    @Query("""
        SELECT AVG(duracion_total_segundos) FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId AND duracion_total_segundos > 0
    """)
    suspend fun obtenerPromedioDuracionListas(usuarioId: Int): Float?

    @Query("""
        SELECT COUNT(*) FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId AND es_publica = 1
    """)
    suspend fun contarListasPublicasDelUsuario(usuarioId: Int): Int

    @Query("""
        SELECT SUM(total_canciones) FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId
    """)
    suspend fun contarTotalCancionesEnListas(usuarioId: Int): Int?

    // ==================== UTILIDADES ====================

    @Query("SELECT EXISTS(SELECT 1 FROM listas_reproduccion WHERE id_lista = :id)")
    suspend fun existeLista(id: Int): Boolean

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM listas_reproduccion 
            WHERE id_lista = :id AND id_usuario = :usuarioId
        )
    """)
    suspend fun usuarioPoseeLista(id: Int, usuarioId: Int): Boolean

    @Query("""
        SELECT id_lista FROM listas_reproduccion 
        WHERE nombre = :nombre 
          AND id_usuario = :usuarioId 
        COLLATE NOCASE 
        LIMIT 1
    """)
    suspend fun obtenerIdListaPorNombre(nombre: String, usuarioId: Int): Int?

    @Transaction
    suspend fun duplicarLista(
        listaId: Int,
        nuevoNombre: String,
        usuarioId: Int
    ): Long {  // Cambiar a Long
        val original = obtenerListaPorId(listaId) ?: return -1

        val copia = original.copy(
            idLista = 0,
            nombre = nuevoNombre,
            idUsuario = usuarioId,
            fechaCreacion = System.currentTimeMillis(),
            fechaModificacion = System.currentTimeMillis(),
            vecesReproducida = 0,
            ultimaReproduccion = null
        )

        return insertarLista(copia)
    }

    @Transaction
    suspend fun obtenerOCrearLista(
        nombre: String,
        usuarioId: Int
    ): ListaReproduccionEntity {
        val idExistente = obtenerIdListaPorNombre(nombre, usuarioId)

        if (idExistente != null) {
            return obtenerListaPorId(idExistente)!!
        }

        val nuevaLista = ListaReproduccionEntity(
            nombre = nombre,
            idUsuario = usuarioId
        )
        val id = insertarLista(nuevaLista)
        return nuevaLista.copy(idLista = id.toInt())
    }
}