package com.example.freeplayerm.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.PlaybackHistoryEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones del historial de reproducciones.
 *
 * Maneja:
 * - Registro de reproducciones
 * - Analytics y estadísticas
 * - Historial por usuario
 * - Reproducciones por contexto
 */
@Dao
interface PlaybackHistoryDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHistorial(historial: PlaybackHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHistoriales(historiales: List<PlaybackHistoryEntity>): List<Long>

    // ==================== ACTUALIZACIÓN ====================

    @Update suspend fun actualizarHistorial(historial: PlaybackHistoryEntity): Int

    @Query(
        """
        UPDATE historial_reproduccion 
        SET duracion_reproducida_ms = :duracion,
            completo = :completo
        WHERE id_historial = :id
    """
    )
    suspend fun actualizarDuracionReproducida(id: Int, duracion: Int, completo: Boolean): Int

    // ==================== ELIMINACIÓN ====================

    @Delete suspend fun eliminarHistorial(historial: PlaybackHistoryEntity): Int

    @Query("DELETE FROM historial_reproduccion WHERE id_historial = :id")
    suspend fun eliminarHistorialPorId(id: Int): Int

    @Query("DELETE FROM historial_reproduccion WHERE id_usuario = :usuarioId")
    suspend fun eliminarHistorialDelUsuario(usuarioId: Int): Int

    @Query(
        """
        DELETE FROM historial_reproduccion 
        WHERE fecha_reproduccion < :fechaLimite
    """
    )
    suspend fun eliminarHistorialAntiguo(fechaLimite: Int): Int

    @Query(
        """
        DELETE FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion < :fechaLimite
    """
    )
    suspend fun eliminarHistorialAntiguoDelUsuario(usuarioId: Int, fechaLimite: Int): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM historial_reproduccion WHERE id_historial = :id LIMIT 1")
    suspend fun obtenerHistorialPorId(id: Int): PlaybackHistoryEntity?

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDelUsuario(usuarioId: Int): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_reproduccion DESC
        LIMIT :limite
    """
    )
    fun obtenerHistorialReciente(
        usuarioId: Int,
        limite: Int = 50,
    ): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT COUNT(*) FROM historial_reproduccion WHERE id_usuario = :usuarioId")
    suspend fun contarHistorial(usuarioId: Int): Int

    @Query("SELECT COUNT(*) FROM historial_reproduccion WHERE id_usuario = :usuarioId")
    fun contarHistorialFlow(usuarioId: Int): Flow<Int>

    // ==================== CANCIONES CON DETALLES ====================

    @Transaction
    @Query(
        """
        SELECT DISTINCT
            c.*,
            a.nombre AS artistaNombre,
            al.titulo AS albumNombre,
            g.nombre AS generoNombre,
            (f.id_usuario IS NOT NULL) AS esFavorita,
            al.portada_path AS portadaPath,
            al.anio AS fechaLanzamiento
        FROM historial_reproduccion h
        INNER JOIN canciones c ON h.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE h.id_usuario = :usuarioId
        ORDER BY h.fecha_reproduccion DESC
        LIMIT :limite
    """
    )
    fun obtenerCancionesReproducidasRecientemente(
        usuarioId: Int,
        limite: Int = 50,
    ): Flow<List<SongWithArtist>>

    // ==================== FILTROS POR CANCIÓN ====================

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDeCancion(usuarioId: Int, cancionId: Int): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """
    )
    suspend fun contarReproduccionesDeCancion(usuarioId: Int, cancionId: Int): Int

    @Query(
        """
        SELECT COUNT(*) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId 
          AND completo = 1
    """
    )
    suspend fun contarReproduccionesCompletasDeCancion(usuarioId: Int, cancionId: Int): Int

    @Query(
        """
        SELECT fecha_reproduccion FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
        ORDER BY fecha_reproduccion DESC 
        LIMIT 1
    """
    )
    suspend fun obtenerUltimaReproduccionDeCancion(usuarioId: Int, cancionId: Int): Int?

    // ==================== FILTROS POR ORIGEN ====================

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = :origen
        ORDER BY fecha_reproduccion DESC
        LIMIT :limite
    """
    )
    fun obtenerHistorialPorOrigen(
        usuarioId: Int,
        origen: String,
        limite: Int = 100,
    ): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = 'PLAYLIST' 
          AND id_contexto = :playlistId
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDePlaylist(
        usuarioId: Int,
        playlistId: Int,
    ): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND origen = 'ALBUM' 
          AND id_contexto = :albumId
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDeAlbum(usuarioId: Int, albumId: Int): Flow<List<PlaybackHistoryEntity>>

    // ==================== FILTROS POR FECHA ====================

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion >= :fechaInicio
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDesde(usuarioId: Int, fechaInicio: Int): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialEnRango(
        usuarioId: Int,
        fechaInicio: Int,
        fechaFin: Int,
    ): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion >= :timestampHoy
        ORDER BY fecha_reproduccion DESC
    """
    )
    fun obtenerHistorialDeHoy(usuarioId: Int, timestampHoy: Int): Flow<List<PlaybackHistoryEntity>>

    // ==================== ESTADÍSTICAS GENERALES ====================

    @Query(
        """
        SELECT COUNT(*) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND completo = 1
    """
    )
    suspend fun contarReproduccionesCompletas(usuarioId: Int): Int

    @Query(
        """
        SELECT AVG(duracion_reproducida_ms) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND duracion_reproducida_ms > 0
    """
    )
    suspend fun obtenerDuracionPromedioReproduccion(usuarioId: Int): Float?

    @Query(
        """
        SELECT SUM(duracion_reproducida_ms) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
    """
    )
    suspend fun obtenerTiempoTotalEscuchado(usuarioId: Int): Int?

    @Query(
        """
        SELECT COUNT(DISTINCT id_cancion) FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
    """
    )
    suspend fun contarCancionesUnicasReproducidas(usuarioId: Int): Int

    @Query(
        """
        SELECT COUNT(DISTINCT DATE(fecha_reproduccion / 1000, 'unixepoch')) 
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
    """
    )
    suspend fun contarDiasConReproduccion(usuarioId: Int): Int

    // ==================== TOP CANCIONES ====================

    @Query(
        """
        SELECT id_cancion, COUNT(*) as total 
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
        GROUP BY id_cancion 
        ORDER BY total DESC 
        LIMIT :limite
    """
    )
    suspend fun obtenerTopCancionesMasReproducidas(
        usuarioId: Int,
        limite: Int = 20,
    ): List<CancionReproduccionCount>

    data class CancionReproduccionCount(
        @ColumnInfo(name = "id_cancion") val idCancion: Int,
        @ColumnInfo(name = "total") val total: Int,
    )

    @Query(
        """
        SELECT id_cancion, COUNT(*) as total 
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion >= :desde
        GROUP BY id_cancion 
        ORDER BY total DESC 
        LIMIT :limite
    """
    )
    suspend fun obtenerTopCancionesEnPeriodo(
        usuarioId: Int,
        desde: Int,
        limite: Int = 20,
    ): List<CancionReproduccionCount>

    // ==================== ANÁLISIS POR HORA ====================

    @Query(
        """
        SELECT 
            CAST(strftime('%H', fecha_reproduccion / 1000, 'unixepoch') AS INTEGER) as hora,
            COUNT(*) as reproducciones
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
        GROUP BY hora 
        ORDER BY hora ASC
    """
    )
    suspend fun obtenerReproduccionesPorHora(usuarioId: Int): List<HoraStats>

    data class HoraStats(
        @ColumnInfo(name = "hora") val hora: Int,
        @ColumnInfo(name = "reproducciones") val reproducciones: Int,
    )

    // ==================== ANÁLISIS POR DÍA ====================

    @Query(
        """
        SELECT 
            DATE(fecha_reproduccion / 1000, 'unixepoch') as fecha,
            COUNT(*) as reproducciones
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND fecha_reproduccion >= :desde
        GROUP BY fecha 
        ORDER BY fecha DESC
    """
    )
    suspend fun obtenerReproduccionesPorDia(usuarioId: Int, desde: Int): List<DiaStats>

    data class DiaStats(
        @ColumnInfo(name = "fecha") val fecha: String,
        @ColumnInfo(name = "reproducciones") val reproducciones: Int,
    )

    // ==================== STREAKS ====================

    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT DATE(fecha_reproduccion / 1000, 'unixepoch') as fecha
            FROM historial_reproduccion 
            WHERE id_usuario = :usuarioId
            GROUP BY fecha
            ORDER BY fecha DESC
        )
    """
    )
    suspend fun contarDiasConsecutivosConActividad(usuarioId: Int): Int

    // ==================== LIMPIEZA ====================

    @Query(
        """
        DELETE FROM historial_reproduccion 
        WHERE id_cancion NOT IN (SELECT id_cancion FROM canciones)
    """
    )
    suspend fun limpiarHistorialHuerfano(): Int

    @Query(
        """
        DELETE FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
          AND duracion_reproducida_ms < :duracionMinima
    """
    )
    suspend fun eliminarReproduccionesIncompletas(
        usuarioId: Int,
        duracionMinima: Int = 5000, // 5 segundos
    ): Int

    // ==================== UTILIDADES ====================

    @Transaction
    suspend fun registrarReproduccion(
        usuarioId: Int,
        cancionId: Int,
        duracionMs: Int,
        origen: String,
        idContexto: Int? = null,
    ): Long { // Cambiar a Long
        val historial =
            PlaybackHistoryEntity(
                idUsuario = usuarioId,
                idCancion = cancionId,
                fechaReproduccion = System.currentTimeMillis(),
                duracionReproducidaMs = duracionMs,
                completo = duracionMs > 30000,
                origen = origen,
                idContexto = idContexto,
                duracionTotalCancionMs = duracionMs,
            )
        return insertarHistorial(historial)
    }

    @Query(
        """
        SELECT * FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId 
        ORDER BY RANDOM() 
        LIMIT :cantidad
    """
    )
    suspend fun obtenerHistorialAleatorio(
        usuarioId: Int,
        cantidad: Int,
    ): List<PlaybackHistoryEntity>

    @Query(
        """
        SELECT AVG(
            CASE WHEN completo = 1 THEN 1.0 ELSE 0.0 END
        ) * 100 
        FROM historial_reproduccion 
        WHERE id_usuario = :usuarioId
    """
    )
    suspend fun obtenerPorcentajeReproduccionesCompletas(usuarioId: Int): Float?
}
