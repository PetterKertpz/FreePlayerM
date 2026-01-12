package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de artistas en la base de datos. ✅ CORREGIDO - Campos sincronizados con
 * ArtistEntity
 */
@Dao
interface ArtistDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarArtista(artista: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarArtistas(artistas: List<ArtistEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarArtistaSiNoExiste(artista: ArtistEntity): Long

    // ==================== ACTUALIZACIÓN ====================

    @Update suspend fun actualizarArtista(artista: ArtistEntity): Int

    @Query(
        """
        UPDATE artistas 
        SET nombre = :nombre,
            nombre_normalizado = :nombreNormalizado,
            biografia = :biografia,
            image_path_local = :imagePath,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun actualizarInformacionBasica(
        id: Int,
        nombre: String,
        nombreNormalizado: String,
        biografia: String?,
        imagePath: String?,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    @Query(
        """
        UPDATE artistas 
        SET genius_id = :geniusId,
            genius_url = :geniusUrl,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun actualizarDatosGenius(
        id: Int,
        geniusId: String?,
        geniusUrl: String?,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    @Query(
        """
        UPDATE artistas 
        SET spotify_id = :spotifyId,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun actualizarDatosSpotify(
        id: Int,
        spotifyId: String?,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    @Query(
        """
        UPDATE artistas 
        SET total_canciones = total_canciones + :incremento,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun incrementarContadorCanciones(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    @Query(
        """
        UPDATE artistas 
        SET total_albumes = total_albumes + :incremento,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun incrementarContadorAlbumes(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    // ==================== ELIMINACIÓN ====================

    @Delete suspend fun eliminarArtista(artista: ArtistEntity): Int

    @Query("DELETE FROM artistas WHERE id_artista = :id")
    suspend fun eliminarArtistaPorId(id: Int): Int

    @Query("DELETE FROM artistas WHERE total_canciones = 0 AND total_albumes = 0")
    suspend fun eliminarArtistasSinContenido(): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM artistas WHERE id_artista = :id LIMIT 1")
    suspend fun obtenerArtistaPorId(id: Int): ArtistEntity?

    @Query("SELECT * FROM artistas WHERE id_artista = :id LIMIT 1")
    fun obtenerArtistaPorIdFlow(id: Int): Flow<ArtistEntity?>

    @Query("SELECT * FROM artistas ORDER BY nombre ASC")
    fun obtenerTodosLosArtistas(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artistas ORDER BY nombre ASC")
    suspend fun obtenerTodosLosArtistasSync(): List<ArtistEntity>

    @Query("SELECT COUNT(*) FROM artistas") suspend fun contarArtistas(): Int

    @Query("SELECT COUNT(*) FROM artistas") fun contarArtistasFlow(): Flow<Int>

    // ==================== BÚSQUEDA ====================

    @Query(
        """
        SELECT * FROM artistas 
        WHERE nombre LIKE '%' || :query || '%'
           OR nombre_normalizado LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN nombre LIKE :query || '%' THEN 1 ELSE 2 END,
            nombre ASC
        LIMIT :limite
    """
    )
    fun buscarArtistas(query: String, limite: Int = 50): Flow<List<ArtistEntity>>

    @Query(
        """
        SELECT * FROM artistas 
        WHERE nombre = :nombre COLLATE NOCASE
        LIMIT 1
    """
    )
    suspend fun buscarArtistaPorNombreExacto(nombre: String): ArtistEntity?

    @Query(
        """
        SELECT * FROM artistas 
        WHERE nombre_normalizado = :nombreNormalizado
        LIMIT 1
    """
    )
    suspend fun buscarArtistaPorNombreNormalizado(nombreNormalizado: String): ArtistEntity?

    // ==================== FILTROS Y ORDENAMIENTO ====================

    @Query(
        """
        SELECT * FROM artistas 
        WHERE es_popular = 1
        ORDER BY nombre ASC
        LIMIT :limite
    """
    )
    fun obtenerArtistasPopulares(limite: Int = 50): Flow<List<ArtistEntity>>

    @Query(
        """
        SELECT * FROM artistas 
        ORDER BY total_canciones DESC
        LIMIT :limite
    """
    )
    fun obtenerArtistasMasCanciones(limite: Int = 20): Flow<List<ArtistEntity>>

    @Query(
        """
        SELECT * FROM artistas 
        ORDER BY total_albumes DESC
        LIMIT :limite
    """
    )
    fun obtenerArtistasMasAlbumes(limite: Int = 20): Flow<List<ArtistEntity>>

    @Query(
        """
        SELECT * FROM artistas 
        ORDER BY fecha_agregado DESC
        LIMIT :limite
    """
    )
    fun obtenerArtistasRecientes(limite: Int = 20): Flow<List<ArtistEntity>>

    // ==================== GENIUS API ====================

    @Query("SELECT * FROM artistas WHERE genius_id = :geniusId LIMIT 1")
    suspend fun obtenerArtistaPorGeniusId(geniusId: String): ArtistEntity?

    @Query(
        """
        SELECT * FROM artistas 
        WHERE genius_id IS NULL 
           OR genius_url IS NULL
           OR (ultima_actualizacion < :timestampLimite)
        LIMIT :limite
    """
    )
    suspend fun obtenerArtistasParaEnriquecerConGenius(
        timestampLimite: Int,
        limite: Int = 50,
    ): List<ArtistEntity>

    @Query(
        """
        UPDATE artistas 
        SET genius_id = :geniusId,
            genius_url = :geniusUrl,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun sincronizarConGenius(
        id: Int,
        geniusId: String,
        geniusUrl: String,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    // ==================== SPOTIFY API ====================

    @Query("SELECT * FROM artistas WHERE spotify_id = :spotifyId LIMIT 1")
    suspend fun obtenerArtistaPorSpotifyId(spotifyId: String): ArtistEntity?

    // ==================== ESTADÍSTICAS ====================

    @Query(
        """
        SELECT * FROM artistas 
        WHERE total_canciones > 0
        ORDER BY total_reproducciones DESC
        LIMIT :limite
    """
    )
    fun obtenerArtistasMasEscuchados(limite: Int = 20): Flow<List<ArtistEntity>>

    @Query(
        """
        UPDATE artistas 
        SET total_reproducciones = total_reproducciones + :incremento,
            ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun incrementarReproduccionesArtista(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    // ==================== RECÁLCULO DE ESTADÍSTICAS ====================

    @Query(
        """
        UPDATE artistas 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_artista = artistas.id_artista
        ),
        total_albumes = (
            SELECT COUNT(DISTINCT id_album) 
            FROM canciones 
            WHERE canciones.id_artista = artistas.id_artista 
              AND id_album IS NOT NULL
        ),
        ultima_actualizacion = :timestamp
        WHERE id_artista = :id
    """
    )
    suspend fun recalcularEstadisticas(
        id: Int,
        timestamp: Int = System.currentTimeMillis().toInt(),
    ): Int

    @Query(
        """
        UPDATE artistas 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_artista = artistas.id_artista
        ),
        total_albumes = (
            SELECT COUNT(DISTINCT id_album) 
            FROM canciones 
            WHERE canciones.id_artista = artistas.id_artista 
              AND id_album IS NOT NULL
        ),
        ultima_actualizacion = :timestamp
    """
    )
    suspend fun recalcularTodasLasEstadisticas(
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ANÁLISIS Y MÉTRICAS ====================

    @Query(
        """
        SELECT AVG(total_canciones) FROM artistas 
        WHERE total_canciones > 0
    """
    )
    suspend fun obtenerPromedioCancionesPorArtista(): Float?

    @Query(
        """
        SELECT AVG(total_albumes) FROM artistas 
        WHERE total_albumes > 0
    """
    )
    suspend fun obtenerPromedioAlbumesPorArtista(): Float?

    @Query(
        """
        SELECT COUNT(*) FROM artistas 
        WHERE total_canciones >= :minimoApariciones
    """
    )
    suspend fun contarArtistasConMinimoCanciones(minimoApariciones: Int): Int

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    @Query(
        """
        UPDATE artistas 
        SET image_path_local = NULL 
        WHERE image_path_local IS NOT NULL 
          AND image_path_local NOT LIKE 'http%'
          AND LENGTH(image_path_local) = 0
    """
    )
    suspend fun limpiarImagenesInvalidas(): Int

    @Query(
        """
        DELETE FROM artistas 
        WHERE total_canciones = 0 
          AND total_albumes = 0
          AND fecha_agregado < :timestampLimite
    """
    )
    suspend fun eliminarArtistasAntiguosSinContenido(timestampLimite: Int): Int

    @Query(
        """
        SELECT * FROM artistas 
        WHERE nombre_normalizado IS NULL 
           OR LENGTH(nombre_normalizado) = 0
    """
    )
    suspend fun obtenerArtistasConNombresNoNormalizados(): List<ArtistEntity>

    // ==================== UTILIDADES ====================

    @Query("SELECT EXISTS(SELECT 1 FROM artistas WHERE id_artista = :id)")
    suspend fun existeArtista(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM artistas WHERE nombre = :nombre COLLATE NOCASE)")
    suspend fun existeArtistaPorNombre(nombre: String): Boolean

    @Query("SELECT id_artista FROM artistas WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerIdArtistaPorNombre(nombre: String): Int?

    @Transaction
    suspend fun insertarOActualizarArtista(artista: ArtistEntity): Long { // Cambiar a Long
        val existente = buscarArtistaPorNombreNormalizado(artista.nombreNormalizado)
        return if (existente != null) {
            actualizarArtista(artista.copy(idArtista = existente.idArtista))
            existente.idArtista.toLong() // Convertir a Long
        } else {
            insertarArtista(artista)
        }
    }

    @Transaction
    suspend fun obtenerOCrearArtista(nombre: String): ArtistEntity {
        val nombreNormalizado = ArtistEntity.normalizar(nombre)
        val existente = buscarArtistaPorNombreNormalizado(nombreNormalizado)

        return existente
            ?: run {
                val nuevoArtista =
                    ArtistEntity(nombre = nombre, nombreNormalizado = nombreNormalizado)
                val id = insertarArtista(nuevoArtista)
                nuevoArtista.copy(idArtista = id.toInt()) // Convertir Long a Int
            }
    }
   @Transaction
   suspend fun actualizarEstadisticasMasivas() {
      // 1. Resetear contadores para evitar datos fantasma
      resetearContadores()
      
      // 2. Calcular y actualizar canciones reales
      actualizarTotalCancionesDesdeCanciones()
      
      // 3. Calcular y actualizar álbumes reales
      actualizarTotalAlbumesDesdeCanciones()
   }
   
   @Query("UPDATE artistas SET total_canciones = 0, total_albumes = 0")
   suspend fun resetearContadores()
   
   @Query("""
        UPDATE artistas
        SET total_canciones = (
            SELECT COUNT(*) FROM canciones
            WHERE canciones.id_artista = artistas.id_artista
        )
        WHERE EXISTS (SELECT 1 FROM canciones WHERE canciones.id_artista = artistas.id_artista)
    """)
   suspend fun actualizarTotalCancionesDesdeCanciones()
   
   @Query("""
        UPDATE artistas
        SET total_albumes = (
            SELECT COUNT(DISTINCT id_album) FROM canciones
            WHERE canciones.id_artista = artistas.id_artista
            AND id_album IS NOT NULL
        )
        WHERE EXISTS (SELECT 1 FROM canciones WHERE canciones.id_artista = artistas.id_artista)
    """)
   suspend fun actualizarTotalAlbumesDesdeCanciones()
}
