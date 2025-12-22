package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.FavoritoEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de favoritos en la base de datos.
 *
 * Maneja:
 * - CRUD básico de favoritos
 * - Relación Usuario-Canción
 * - Estadísticas y ordenamiento
 * - Calificaciones personales
 */
@Dao
interface FavoritoDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFavorito(favorito: FavoritoEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFavoritos(favoritos: List<FavoritoEntity>): List<Int>

    // ==================== ACTUALIZACIÓN ====================

    @Update
    suspend fun actualizarFavorito(favorito: FavoritoEntity): Int

    @Query("""
        UPDATE favoritos 
        SET calificacion = :calificacion
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun actualizarCalificacion(
        usuarioId: Int,
        cancionId: Int,
        calificacion: Float
    ): Int

    @Query("""
        UPDATE favoritos 
        SET notas = :notas
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun actualizarNotas(
        usuarioId: Int,
        cancionId: Int,
        notas: String?
    ): Int

    @Query("""
        UPDATE favoritos 
        SET orden = :nuevoOrden
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun actualizarOrden(
        usuarioId: Int,
        cancionId: Int,
        nuevoOrden: Int
    ): Int

    @Query("""
        UPDATE favoritos 
        SET veces_reproducida_desde_favoritos = veces_reproducida_desde_favoritos + 1
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun incrementarReproduccionesDesdeFavoritos(
    usuarioId: Int,
    cancionId: Int
    ): Int

    // ==================== ELIMINACIÓN ====================

    @Delete
    suspend fun eliminarFavorito(favorito: FavoritoEntity): Int

    @Query("""
        DELETE FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId
    """)
    suspend fun eliminarFavoritoPorIds(usuarioId: Int, cancionId: Int): Int

    @Query("""
        DELETE FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND id_cancion IN (:cancionIds)
    """)
    suspend fun eliminarFavoritosPorIds(usuarioId: Int, cancionIds: List<Int>): Int

    @Query("DELETE FROM favoritos WHERE id_usuario = :usuarioId")
    suspend fun eliminarTodosFavoritos(usuarioId: Int): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND id_cancion = :cancionId 
        LIMIT 1
    """)
    suspend fun obtenerFavorito(usuarioId: Int, cancionId: Int): FavoritoEntity?

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_agregado DESC
    """)
    fun obtenerFavoritosDelUsuario(usuarioId: Int): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_agregado DESC
    """)
    suspend fun obtenerFavoritosDelUsuarioSync(usuarioId: Int): List<FavoritoEntity>

    @Query("SELECT COUNT(*) FROM favoritos WHERE id_usuario = :usuarioId")
    suspend fun contarFavoritos(usuarioId: Int): Int

    @Query("SELECT COUNT(*) FROM favoritos WHERE id_usuario = :usuarioId")
    fun contarFavoritosFlow(usuarioId: Int): Flow<Int>

    // ==================== VERIFICACIÓN ====================

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favoritos 
            WHERE id_usuario = :usuarioId 
              AND id_cancion = :cancionId
        )
    """)
    suspend fun esFavorita(usuarioId: Int, cancionId: Int): Boolean

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favoritos 
            WHERE id_usuario = :usuarioId 
              AND id_cancion = :cancionId
        )
    """)
    fun esFavoritaFlow(usuarioId: Int, cancionId: Int): Flow<Boolean>

    // ==================== CANCIONES CON DETALLES ====================

    @Transaction
    @Query("""
        SELECT 
            c.*,
            a.nombre AS artistaNombre,
            al.titulo AS albumNombre,
            g.nombre AS generoNombre,
            1 AS esFavorita,
            al.portada_path AS portadaPath,
            al.anio AS fechaLanzamiento
        FROM favoritos f
        INNER JOIN canciones c ON f.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        WHERE f.id_usuario = :usuarioId
        ORDER BY f.fecha_agregado DESC
    """)
    fun obtenerCancionesFavoritasConDetalles(usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    @Query("""
        SELECT 
            c.*,
            a.nombre AS artistaNombre,
            al.titulo AS albumNombre,
            g.nombre AS generoNombre,
            1 AS esFavorita,
            al.portada_path AS portadaPath,
            al.anio AS fechaLanzamiento
        FROM favoritos f
        INNER JOIN canciones c ON f.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        WHERE f.id_usuario = :usuarioId
        ORDER BY f.orden ASC, f.fecha_agregado DESC
    """)
    fun obtenerCancionesFavoritasOrdenadas(usuarioId: Int): Flow<List<CancionConArtista>>

    // ==================== ORDENAMIENTO ====================

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_agregado DESC
        LIMIT :limite
    """)
    fun obtenerFavoritosRecientes(usuarioId: Int, limite: Int = 20): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
          AND calificacion IS NOT NULL
        ORDER BY calificacion DESC, fecha_agregado DESC
        LIMIT :limite
    """)
    fun obtenerFavoritosMejorCalificados(usuarioId: Int, limite: Int = 20): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY veces_reproducida_desde_favoritos DESC
        LIMIT :limite
    """)
    fun obtenerFavoritosMasReproducidos(usuarioId: Int, limite: Int = 20): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
          AND orden > 0
        ORDER BY orden ASC
    """)
    fun obtenerFavoritosConOrdenPersonalizado(usuarioId: Int): Flow<List<FavoritoEntity>>

    // ==================== FILTROS ====================

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
          AND calificacion >= :calificacionMinima
        ORDER BY calificacion DESC
    """)
    fun obtenerFavoritosPorCalificacion(
        usuarioId: Int,
        calificacionMinima: Float
    ): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
          AND notas IS NOT NULL 
          AND LENGTH(notas) > 0
        ORDER BY fecha_agregado DESC
    """)
    fun obtenerFavoritosConNotas(usuarioId: Int): Flow<List<FavoritoEntity>>

    @Query("""
        SELECT * FROM favoritos 
        WHERE id_usuario = :usuarioId
          AND fecha_agregado BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha_agregado DESC
    """)
    fun obtenerFavoritosPorRangoDeFechas(
        usuarioId: Int,
        fechaInicio: Int,
        fechaFin: Int
    ): Flow<List<FavoritoEntity>>

    // ==================== ESTADÍSTICAS ====================

    @Query("""
        SELECT AVG(calificacion) FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND calificacion IS NOT NULL
    """)
    suspend fun obtenerCalificacionPromedio(usuarioId: Int): Float?

    @Query("""
        SELECT COUNT(*) FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND calificacion >= :calificacion
    """)
    suspend fun contarFavoritosConCalificacion(
        usuarioId: Int,
        calificacion: Float
    ): Int

    @Query("""
        SELECT SUM(veces_reproducida_desde_favoritos) FROM favoritos 
        WHERE id_usuario = :usuarioId
    """)
    suspend fun contarTotalReproduccionesDesdeFavoritos(usuarioId: Int): Int?

    @Query("""
        SELECT COUNT(DISTINCT id_cancion) FROM favoritos 
        WHERE id_usuario = :usuarioId
    """)
    suspend fun contarCancionesUnicasFavoritas(usuarioId: Int): Int

    // ==================== IDs DE CANCIONES ====================

    @Query("""
        SELECT id_cancion FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_agregado DESC
    """)
    fun obtenerIdsFavoritas(usuarioId: Int): Flow<List<Int>>

    @Query("""
        SELECT id_cancion FROM favoritos 
        WHERE id_usuario = :usuarioId
        ORDER BY fecha_agregado DESC
    """)
    suspend fun obtenerIdsFavoritasSync(usuarioId: Int): List<Int>

    // ==================== TOGGLE FAVORITO ====================

    @Transaction
    suspend fun toggleFavorito(usuarioId: Int, cancionId: Int): Boolean {
        return if (esFavorita(usuarioId, cancionId)) {
            eliminarFavoritoPorIds(usuarioId, cancionId)
            false // Se eliminó
        } else {
            insertarFavorito(FavoritoEntity.crear(usuarioId, cancionId))
            true // Se agregó
        }
    }

    @Transaction
    suspend fun toggleFavoritoConCalificacion(
        usuarioId: Int,
        cancionId: Int,
        calificacion: Float
    ): Boolean {
        return if (esFavorita(usuarioId, cancionId)) {
            eliminarFavoritoPorIds(usuarioId, cancionId)
            false
        } else {
            insertarFavorito(
                FavoritoEntity.crearConCalificacion(usuarioId, cancionId, calificacion)
            )
            true
        }
    }

    // ==================== REORDENAMIENTO ====================

    @Transaction
    suspend fun reordenarFavoritos(usuarioId: Int, nuevosOrdenes: Map<Int, Int>) {
        nuevosOrdenes.forEach { (cancionId, orden) ->
            actualizarOrden(usuarioId, cancionId, orden)
        }
    }

    @Transaction
    suspend fun resetearOrden(usuarioId: Int) {
        val favoritos = obtenerFavoritosDelUsuarioSync(usuarioId)
        favoritos.forEachIndexed { index, favorito ->
            actualizarOrden(usuarioId, favorito.idCancion, index)
        }
    }

    // ==================== BÚSQUEDA ====================

    @Query("""
        SELECT f.* FROM favoritos f
        INNER JOIN canciones c ON f.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        WHERE f.id_usuario = :usuarioId
          AND (
              c.titulo LIKE '%' || :query || '%'
              OR a.nombre LIKE '%' || :query || '%'
              OR f.notas LIKE '%' || :query || '%'
          )
        ORDER BY f.fecha_agregado DESC
        LIMIT :limite
    """)
    fun buscarEnFavoritos(
        usuarioId: Int,
        query: String,
        limite: Int = 50
    ): Flow<List<FavoritoEntity>>

    // ==================== LIMPIEZA ====================

    @Query("""
        DELETE FROM favoritos 
        WHERE id_cancion NOT IN (SELECT id_cancion FROM canciones)
    """)
    suspend fun limpiarFavoritosHuerfanos(): Int

    @Query("""
        DELETE FROM favoritos 
        WHERE id_usuario = :usuarioId 
          AND fecha_agregado < :fechaLimite
    """)
    suspend fun eliminarFavoritosAntiguos(usuarioId: Int, fechaLimite: Int): Int

    // ==================== UTILIDADES ====================

    @Query("""
        SELECT id_cancion FROM favoritos 
        WHERE id_usuario = :usuarioId 
        ORDER BY RANDOM() 
        LIMIT :cantidad
    """)
    suspend fun obtenerFavoritosAleatorios(usuarioId: Int, cantidad: Int): List<Int>

    @Transaction
    suspend fun obtenerOCrearFavorito(usuarioId: Int, cancionId: Int): FavoritoEntity {
        return obtenerFavorito(usuarioId, cancionId) ?: run {
            val nuevo = FavoritoEntity.crear(usuarioId, cancionId)
            insertarFavorito(nuevo)
            nuevo
        }
    }

    @Query("""
        SELECT fecha_agregado FROM favoritos 
        WHERE id_usuario = :usuarioId 
        ORDER BY fecha_agregado ASC 
        LIMIT 1
    """)
    suspend fun obtenerFechaPrimerFavorito(usuarioId: Int): Int?

    @Query("""
        SELECT fecha_agregado FROM favoritos 
        WHERE id_usuario = :usuarioId 
        ORDER BY fecha_agregado DESC 
        LIMIT 1
    """)
    suspend fun obtenerFechaUltimoFavorito(usuarioId: Int): Int?
}