package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de álbumes en la base de datos.
 * ✅ CORREGIDO - Campos sincronizados con AlbumEntity
 */
@Dao
interface AlbumDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlbum(album: AlbumEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlbumes(albumes: List<AlbumEntity>): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAlbumSiNoExiste(album: AlbumEntity): Int

    // ==================== ACTUALIZACIÓN ====================

    @Update
    suspend fun actualizarAlbum(album: AlbumEntity): Int

    @Query("""
        UPDATE albumes 
        SET titulo = :titulo,
            descripcion = :descripcion,
            portada_path = :portadaPath
        WHERE id_album = :id
    """)
    suspend fun actualizarInformacionBasica(
        id: Int,
        titulo: String,
        descripcion: String?,
        portadaPath: String?
    ): Int

    @Query("""
        UPDATE albumes 
        SET total_canciones = total_canciones + :incremento
        WHERE id_album = :id
    """)
    suspend fun incrementarContadorCanciones(id: Int, incremento: Int= 1): Int

    @Query("""
        UPDATE albumes 
        SET total_reproducciones = total_reproducciones + :incremento
        WHERE id_album = :id
    """)
    suspend fun incrementarReproduccionesAlbum(id: Int, incremento: Int= 1): Int

    // ==================== ELIMINACIÓN ====================

    @Delete
    suspend fun eliminarAlbum(album: AlbumEntity): Int

    @Query("DELETE FROM albumes WHERE id_album = :id")
    suspend fun eliminarAlbumPorId(id: Int): Int

    @Query("DELETE FROM albumes WHERE total_canciones = 0")
    suspend fun eliminarAlbumesVacios(): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM albumes WHERE id_album = :id LIMIT 1")
    suspend fun obtenerAlbumPorId(id: Int): AlbumEntity?

    @Query("SELECT * FROM albumes WHERE id_album = :id LIMIT 1")
    fun obtenerAlbumPorIdFlow(id: Int): Flow<AlbumEntity?>

    @Query("SELECT * FROM albumes ORDER BY titulo ASC")
    fun obtenerTodosLosAlbumes(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albumes ORDER BY titulo ASC")
    suspend fun obtenerTodosLosAlbumesSync(): List<AlbumEntity>

    @Query("SELECT COUNT(*) FROM albumes")
    suspend fun contarAlbumes(): Int

    @Query("SELECT COUNT(*) FROM albumes")
    fun contarAlbumesFlow(): Flow<Int>

    // ==================== BÚSQUEDA ====================

    @Query("""
        SELECT * FROM albumes 
        WHERE titulo LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN titulo LIKE :query || '%' THEN 1 ELSE 2 END,
            titulo ASC
        LIMIT :limite
    """)
    fun buscarAlbumes(query: String, limite: Int= 50): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE titulo = :titulo 
          AND id_artista = :artistaId 
        COLLATE NOCASE
        LIMIT 1
    """)
    suspend fun buscarAlbumPorTituloYArtista(titulo: String, artistaId: Int): AlbumEntity?

    // ==================== FILTROS POR ARTISTA ====================

    @Query("""
        SELECT * FROM albumes 
        WHERE id_artista = :artistaId
        ORDER BY anio DESC, titulo ASC
    """)
    fun obtenerAlbumesPorArtista(artistaId: Int): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE id_artista = :artistaId
        ORDER BY anio DESC, titulo ASC
    """)
    suspend fun obtenerAlbumesPorArtistaSync(artistaId: Int): List<AlbumEntity>

    @Query("""
        SELECT COUNT(*) FROM albumes 
        WHERE id_artista = :artistaId
    """)
    suspend fun contarAlbumesPorArtista(artistaId: Int): Int

    // ==================== FILTROS POR AÑO ====================

    @Query("""
        SELECT * FROM albumes 
        WHERE anio = :anio
        ORDER BY titulo ASC
    """)
    fun obtenerAlbumesDelAnio(anio: Int): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE anio BETWEEN :anioInicio AND :anioFin
        ORDER BY anio DESC, titulo ASC
    """)
    fun obtenerAlbumesEnRangoDeAnios(anioInicio: Int, anioFin: Int): Flow<List<AlbumEntity>>

    @Query("""
        SELECT DISTINCT anio FROM albumes 
        WHERE anio IS NOT NULL 
        ORDER BY anio DESC
    """)
    fun obtenerAniosConAlbumes(): Flow<List<Int>>

    // ==================== FILTROS POR TIPO ====================

    @Query("""
        SELECT * FROM albumes 
        WHERE tipo = :tipo
        ORDER BY anio DESC, titulo ASC
    """)
    fun obtenerAlbumesPorTipo(tipo: String): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE tipo = 'SINGLE'
        ORDER BY fecha_lanzamiento DESC
        LIMIT :limite
    """)
    fun obtenerSinglesRecientes(limite: Int= 50): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE tipo = 'EP'
        ORDER BY anio DESC
        LIMIT :limite
    """)
    fun obtenerEPs(limite: Int= 50): Flow<List<AlbumEntity>>

    // ==================== ORDENAMIENTO Y RANKINGS ====================

    @Query("""
        SELECT * FROM albumes 
        ORDER BY fecha_agregado DESC
        LIMIT :limite
    """)
    fun obtenerAlbumesRecientes(limite: Int= 20): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        ORDER BY total_reproducciones DESC
        LIMIT :limite
    """)
    fun obtenerAlbumesMasEscuchados(limite: Int= 20): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        WHERE calificacion_promedio > 0
        ORDER BY calificacion_promedio DESC
        LIMIT :limite
    """)
    fun obtenerAlbumesMejorCalificados(limite: Int= 20): Flow<List<AlbumEntity>>

    @Query("""
        SELECT * FROM albumes 
        ORDER BY total_canciones DESC
        LIMIT :limite
    """)
    fun obtenerAlbumesMasCanciones(limite: Int= 20): Flow<List<AlbumEntity>>

    // ==================== ESTADÍSTICAS ====================

    @Query("""
        UPDATE albumes 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_album = albumes.id_album
        ),
        duracion_total_segundos = (
            SELECT COALESCE(SUM(duracion_segundos), 0)
            FROM canciones 
            WHERE canciones.id_album = albumes.id_album
        )
        WHERE id_album = :id
    """)
    suspend fun recalcularEstadisticas(id: Int): Int

    @Query("""
        UPDATE albumes 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_album = albumes.id_album
        ),
        duracion_total_segundos = (
            SELECT COALESCE(SUM(duracion_segundos), 0)
            FROM canciones 
            WHERE canciones.id_album = albumes.id_album
        )
    """)
    suspend fun recalcularTodasLasEstadisticas(): Int

    // ==================== CALIFICACIONES ====================

    @Query("""
        UPDATE albumes 
        SET calificacion_promedio = :calificacion
        WHERE id_album = :id
    """)
    suspend fun actualizarCalificacion(id: Int, calificacion: Float): Int

    // ==================== ANÁLISIS ====================

    @Query("""
        SELECT AVG(total_canciones) FROM albumes 
        WHERE tipo = 'ALBUM' AND total_canciones > 0
    """)
    suspend fun obtenerPromedioCancionesPorAlbum(): Float?

    @Query("""
        SELECT AVG(duracion_total_segundos) FROM albumes 
        WHERE duracion_total_segundos > 0
    """)
    suspend fun obtenerPromedioDuracionAlbumes(): Float?

    @Query("""
        SELECT COUNT(*) FROM albumes 
        WHERE anio = :anio
    """)
    suspend fun contarAlbumesDelAnio(anio: Int): Int

    @Query("""
        SELECT anio, COUNT(*) as cantidad 
        FROM albumes 
        WHERE anio IS NOT NULL 
        GROUP BY anio 
        ORDER BY anio DESC
    """)
    suspend fun obtenerEstadisticasPorAnio(): List<AnioEstadisticas>

    data class AnioEstadisticas(
        val anio: Int,
        val cantidad: Int
    )

    // ==================== LIMPIEZA ====================

    @Query("""
        UPDATE albumes 
        SET portada_path = NULL 
        WHERE portada_path IS NOT NULL 
          AND portada_path NOT LIKE 'http%'
          AND LENGTH(portada_path) = 0
    """)
    suspend fun limpiarPortadasInvalidas(): Int

    @Query("""
        DELETE FROM albumes 
        WHERE total_canciones = 0 
          AND fecha_agregado < :timestampLimite
    """)
    suspend fun eliminarAlbumesVaciosAntiguos(timestampLimite: Int): Int

    // ==================== UTILIDADES ====================

    @Query("SELECT EXISTS(SELECT 1 FROM albumes WHERE id_album = :id)")
    suspend fun existeAlbum(id: Int): Boolean

    @Query("SELECT id_album FROM albumes WHERE titulo = :titulo AND id_artista = :artistaId LIMIT 1")
    suspend fun obtenerIdAlbumPorTituloYArtista(titulo: String, artistaId: Int): Int?

    @Transaction
    suspend fun insertarOActualizarAlbum(album: AlbumEntity): Int {
        val existente = buscarAlbumPorTituloYArtista(album.titulo, album.idArtista)
        return if (existente != null) {
            actualizarAlbum(album.copy(idAlbum = existente.idAlbum))
            existente.idAlbum
        } else {
            insertarAlbum(album)
        }
    }

    @Transaction
    suspend fun obtenerOCrearAlbum(titulo: String, artistaId: Int, anio: Int?): AlbumEntity {
        val existente = buscarAlbumPorTituloYArtista(titulo, artistaId)

        return existente ?: run {
            val nuevoAlbum = AlbumEntity(
                titulo = titulo,
                idArtista = artistaId,
                anio = anio
            )
            val id = insertarAlbum(nuevoAlbum)
            nuevoAlbum.copy(idAlbum = id)
        }
    }
}