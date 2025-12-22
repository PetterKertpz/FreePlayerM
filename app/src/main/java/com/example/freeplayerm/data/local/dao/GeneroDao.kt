package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.GeneroEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de géneros musicales en la base de datos.
 * ✅ CORREGIDO - Campos sincronizados con GeneroEntity
 */
@Dao
interface GeneroDao {

    // ==================== INSERCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGenero(genero: GeneroEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGeneros(generos: List<GeneroEntity>): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarGeneroSiNoExiste(genero: GeneroEntity): Int

    // ==================== ACTUALIZACIÓN ====================

    @Update
    suspend fun actualizarGenero(genero: GeneroEntity): Int

    @Query("""
        UPDATE generos 
        SET nombre = :nombre,
            nombre_normalizado = :nombreNormalizado,
            descripcion = :descripcion,
            color = :color,
            emoji = :emoji,
            ultima_actualizacion = :timestamp
        WHERE id_genero = :id
    """)
    suspend fun actualizarInformacionBasica(
        id: Int,
        nombre: String,
        nombreNormalizado: String,
        descripcion: String?,
        color: String?,
        emoji: String?,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE generos 
        SET total_canciones = total_canciones + :incremento,
            ultima_actualizacion = :timestamp
        WHERE id_genero = :id
    """)
    suspend fun incrementarContadorCanciones(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE generos 
        SET total_reproducciones = total_reproducciones + :incremento,
            ultima_actualizacion = :timestamp
        WHERE id_genero = :id
    """)
    suspend fun incrementarReproduccionesGenero(
        id: Int,
        incremento: Int = 1,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ELIMINACIÓN ====================

    @Delete
    suspend fun eliminarGenero(genero: GeneroEntity): Int

    @Query("DELETE FROM generos WHERE id_genero = :id")
    suspend fun eliminarGeneroPorId(id: Int): Int

    @Query("DELETE FROM generos WHERE total_canciones = 0 AND genero_padre_id IS NOT NULL")
    suspend fun eliminarSubgenerosSinCanciones(): Int

    // ==================== CONSULTAS BÁSICAS ====================

    @Query("SELECT * FROM generos WHERE id_genero = :id LIMIT 1")
    suspend fun obtenerGeneroPorId(id: Int): GeneroEntity?

    @Query("SELECT * FROM generos WHERE id_genero = :id LIMIT 1")
    fun obtenerGeneroPorIdFlow(id: Int): Flow<GeneroEntity?>

    @Query("SELECT * FROM generos ORDER BY nombre ASC")
    fun obtenerTodosLosGeneros(): Flow<List<GeneroEntity>>

    @Query("SELECT * FROM generos ORDER BY nombre ASC")
    suspend fun obtenerTodosLosGenerosSync(): List<GeneroEntity>

    @Query("SELECT COUNT(*) FROM generos")
    suspend fun contarGeneros(): Int

    @Query("SELECT COUNT(*) FROM generos")
    fun contarGenerosFlow(): Flow<Int>

    // ==================== BÚSQUEDA ====================

    @Query("""
        SELECT * FROM generos 
        WHERE nombre LIKE '%' || :query || '%'
           OR nombre_normalizado LIKE '%' || :query || '%'
           OR descripcion LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN nombre LIKE :query || '%' THEN 1 ELSE 2 END,
            nombre ASC
        LIMIT :limite
    """)
    fun buscarGeneros(query: String, limite: Int = 50): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        WHERE nombre = :nombre COLLATE NOCASE
        LIMIT 1
    """)
    suspend fun buscarGeneroPorNombreExacto(nombre: String): GeneroEntity?

    @Query("""
        SELECT * FROM generos 
        WHERE nombre_normalizado = :nombreNormalizado
        LIMIT 1
    """)
    suspend fun buscarGeneroPorNombreNormalizado(nombreNormalizado: String): GeneroEntity?

    // ==================== JERARQUÍA DE GÉNEROS ====================

    @Query("""
        SELECT * FROM generos 
        WHERE genero_padre_id IS NULL
        ORDER BY nombre ASC
    """)
    fun obtenerGenerosPrincipales(): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        WHERE genero_padre_id = :padreId
        ORDER BY nombre ASC
    """)
    fun obtenerSubgeneros(padreId: Int): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        WHERE genero_padre_id = :padreId
        ORDER BY nombre ASC
    """)
    suspend fun obtenerSubgenerosSync(padreId: Int): List<GeneroEntity>

    @Query("""
        SELECT COUNT(*) FROM generos 
        WHERE genero_padre_id = :padreId
    """)
    suspend fun contarSubgeneros(padreId: Int): Int

    @Transaction
    suspend fun obtenerJerarquiaCompleta(generoId: Int): List<GeneroEntity> {
        val resultado = mutableListOf<GeneroEntity>()
        val cola = mutableListOf(generoId)

        while (cola.isNotEmpty()) {
            val actualId = cola.removeAt(0)
            val genero = obtenerGeneroPorId(actualId)

            if (genero != null) {
                resultado.add(genero)
                val hijos = obtenerSubgenerosSync(actualId)
                cola.addAll(hijos.map { it.idGenero })
            }
        }

        return resultado
    }

    @Query("""
        SELECT * FROM generos 
        WHERE id_genero = (
            SELECT genero_padre_id FROM generos WHERE id_genero = :generoId
        )
        LIMIT 1
    """)
    suspend fun obtenerGeneroPadre(generoId: Int): GeneroEntity?

    // ==================== POPULARIDAD Y FILTROS ====================

    @Query("""
        SELECT * FROM generos 
        WHERE es_popular = 1
        ORDER BY total_canciones DESC
        LIMIT :limite
    """)
    fun obtenerGenerosPopulares(limite: Int = 20): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        ORDER BY total_canciones DESC
        LIMIT :limite
    """)
    fun obtenerGenerosMasCanciones(limite: Int = 20): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        ORDER BY total_reproducciones DESC
        LIMIT :limite
    """)
    fun obtenerGenerosMasEscuchados(limite: Int = 20): Flow<List<GeneroEntity>>

    @Query("""
        SELECT * FROM generos 
        ORDER BY fecha_agregado DESC
        LIMIT :limite
    """)
    fun obtenerGenerosRecientes(limite: Int = 20): Flow<List<GeneroEntity>>

    // ==================== ACTUALIZACIÓN DE POPULARIDAD ====================

    @Query("""
        UPDATE generos 
        SET es_popular = CASE 
            WHEN total_canciones >= :minimoParaPopular THEN 1 
            ELSE 0 
        END,
        ultima_actualizacion = :timestamp
    """)
    suspend fun actualizarPopularidadAutomatica(
        minimoParaPopular: Int = 50,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE generos 
        SET es_popular = :esPopular,
            ultima_actualizacion = :timestamp
        WHERE id_genero = :id
    """)
    suspend fun marcarComoPopular(
        id: Int,
        esPopular: Boolean,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ESTADÍSTICAS ====================

    @Query("""
        UPDATE generos 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_genero = generos.id_genero
        ),
        total_artistas = (
            SELECT COUNT(DISTINCT id_artista) 
            FROM canciones 
            WHERE canciones.id_genero = generos.id_genero
              AND id_artista IS NOT NULL
        ),
        ultima_actualizacion = :timestamp
        WHERE id_genero = :id
    """)
    suspend fun recalcularEstadisticas(
        id: Int,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    @Query("""
        UPDATE generos 
        SET total_canciones = (
            SELECT COUNT(*) 
            FROM canciones 
            WHERE canciones.id_genero = generos.id_genero
        ),
        total_artistas = (
            SELECT COUNT(DISTINCT id_artista) 
            FROM canciones 
            WHERE canciones.id_genero = generos.id_genero
              AND id_artista IS NOT NULL
        ),
        ultima_actualizacion = :timestamp
    """)
    suspend fun recalcularTodasLasEstadisticas(
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== ANÁLISIS ====================

    @Query("""
        SELECT AVG(total_canciones) FROM generos 
        WHERE total_canciones > 0
    """)
    suspend fun obtenerPromedioCancionesPorGenero(): Float?

    @Query("""
        SELECT COUNT(*) FROM generos 
        WHERE total_canciones >= :minimo
    """)
    suspend fun contarGenerosConMinimoCanciones(minimo: Int): Int

    @Query("""
        SELECT * FROM generos 
        WHERE total_canciones > 0
        ORDER BY (total_reproducciones * 1.0 / total_canciones) DESC
        LIMIT :limite
    """)
    fun obtenerGenerosMayorPromedioReproduccionesPorCancion(limite: Int = 20): Flow<List<GeneroEntity>>

    // ==================== LIMPIEZA ====================

    @Query("""
        DELETE FROM generos 
        WHERE total_canciones = 0 
          AND genero_padre_id IS NOT NULL
          AND fecha_agregado < :timestampLimite
    """)
    suspend fun eliminarSubgenerosVaciosAntiguos(timestampLimite: Int): Int

    @Query("""
        SELECT * FROM generos 
        WHERE nombre_normalizado IS NULL 
           OR LENGTH(nombre_normalizado) = 0
    """)
    suspend fun obtenerGenerosConNombresNoNormalizados(): List<GeneroEntity>

    // ==================== UTILIDADES ====================

    @Query("SELECT EXISTS(SELECT 1 FROM generos WHERE id_genero = :id)")
    suspend fun existeGenero(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM generos WHERE nombre = :nombre COLLATE NOCASE)")
    suspend fun existeGeneroPorNombre(nombre: String): Boolean

    @Query("SELECT id_genero FROM generos WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerIdGeneroPorNombre(nombre: String): Int?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM generos g1 
            WHERE g1.id_genero = :hijoId 
              AND g1.genero_padre_id = :padreId
        )
    """)
    suspend fun esSubgeneroDe(hijoId: Int, padreId: Int): Boolean

    @Transaction
    suspend fun insertarOActualizarGenero(genero: GeneroEntity): Int {
        val existente = buscarGeneroPorNombreNormalizado(genero.nombreNormalizado)
        return if (existente != null) {
            actualizarGenero(genero.copy(idGenero = existente.idGenero))
            existente.idGenero
        } else {
            insertarGenero(genero)
        }
    }

    @Transaction
    suspend fun obtenerOCrearGenero(nombre: String, padreId: Int? = null): GeneroEntity {
        val nombreNormalizado = GeneroEntity.normalizar(nombre)
        val existente = buscarGeneroPorNombreNormalizado(nombreNormalizado)

        return existente ?: run {
            val nuevoGenero = GeneroEntity(
                nombre = nombre,
                nombreNormalizado = nombreNormalizado,
                generoPadreId = padreId,
                color = GeneroEntity.obtenerColorSugerido(nombre),
                emoji = GeneroEntity.obtenerEmojiSugerido(nombre)
            )
            val id = insertarGenero(nuevoGenero)
            nuevoGenero.copy(idGenero = id.toInt())
        }
    }

    @Transaction
    suspend fun verificarCicloEnJerarquia(generoId: Int, nuevoPadreId: Int): Boolean {
        var actualId: Int? = nuevoPadreId
        var iteraciones = 0
        val maxIteraciones = 10

        while (actualId != null && iteraciones < maxIteraciones) {
            if (actualId == generoId) return true
            val genero = obtenerGeneroPorId(actualId)
            actualId = genero?.generoPadreId
            iteraciones++
        }

        return false
    }

    @Transaction
    suspend fun obtenerRutaHastaRaiz(generoId: Int): List<GeneroEntity> {
        val ruta = mutableListOf<GeneroEntity>()
        var actualId: Int? = generoId
        var iteraciones = 0
        val maxIteraciones = 10

        while (actualId != null && iteraciones < maxIteraciones) {
            val genero = obtenerGeneroPorId(actualId) ?: break
            ruta.add(0, genero)
            actualId = genero.generoPadreId
            iteraciones++
        }

        return ruta
    }
}