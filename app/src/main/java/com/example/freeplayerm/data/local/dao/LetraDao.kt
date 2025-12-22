// en: app/src/main/java/com/example/freeplayerm/data/local/dao/LetraDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.LetraEntity
import com.example.freeplayerm.data.local.entity.TopItemEstadistica
import kotlinx.coroutines.flow.Flow

/**
 * ⚡ LETRA DAO - MEJORADO Y OPTIMIZADO v2.0
 *
 * Manejo completo de letras de canciones con:
 * - Operaciones CRUD completas
 * - Búsqueda y caché de letras
 * - Sincronización con APIs externas
 * - Manejo de estado de carga
 * - Queries optimizadas
 *
 * @author Android Data Layer Manager
 * @version 2.0 - Enhanced
 */
@Dao
interface LetraDao {

    // ==================== OPERACIONES BÁSICAS ====================

    /**
     * Inserta o actualiza una letra
     * Si ya existe para esa canción, la reemplaza
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLetra(letra: LetraEntity): Int

    /**
     * Inserta múltiples letras en una sola transacción
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLetras(letras: List<LetraEntity>): List<Int>

    /**
     * Actualiza una letra existente
     */
    @Update
    suspend fun actualizarLetra(letra: LetraEntity): Int

    /**
     * Elimina una letra
     */
    @Delete
    suspend fun eliminarLetra(letra: LetraEntity): Int

    /**
     * Elimina una letra por ID de canción
     */
    @Query("DELETE FROM letras WHERE id_cancion = :idCancion")
    suspend fun eliminarLetraPorIdCancion(idCancion: Int): Int

    // ==================== OBTENER LETRAS ====================

    /**
     * Obtiene la letra de una canción específica (Flow reactivo)
     * Se actualiza automáticamente cuando cambia en la BD
     */
    @Query("SELECT * FROM letras WHERE id_cancion = :idCancion")
    fun obtenerLetraPorIdCancion(idCancion: Int): Flow<LetraEntity?>

    /**
     * Obtiene la letra de una canción específica (suspending)
     * Para operaciones puntuales sin reactividad
     */
    @Query("SELECT * FROM letras WHERE id_cancion = :idCancion")
    suspend fun obtenerLetraPorIdCancionSuspending(idCancion: Int): LetraEntity?

    /**
     * Obtiene todas las letras almacenadas
     */
    @Query("SELECT * FROM letras ORDER BY id_cancion ASC")
    fun obtenerTodasLasLetras(): Flow<List<LetraEntity>>

    /**
     * Obtiene todas las letras (suspending)
     */
    @Query("SELECT * FROM letras ORDER BY id_cancion ASC")
    suspend fun obtenerTodasLasLetrasSuspending(): List<LetraEntity>

    // ==================== BÚSQUEDA Y FILTROS ====================

    /**
     * Busca letras por contenido de texto
     * Útil para encontrar canciones por fragmentos de letra
     */
    @Query("""
        SELECT * FROM letras 
        WHERE texto_letra LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY id_cancion ASC
        LIMIT :limit
    """)
    suspend fun buscarLetrasPorContenido(query: String, limit: Int = 50): List<LetraEntity>

    /**
     * Busca letras por fuente (por ejemplo: "genius", "musixmatch", "manual")
     */
    @Query("""
        SELECT * FROM letras 
        WHERE fuente = :fuente COLLATE NOCASE
        ORDER BY id_cancion ASC
    """)
    fun obtenerLetrasPorFuente(fuente: String): Flow<List<LetraEntity>>

    /**
     * Obtiene letras agregadas recientemente
     */
    @Query("""
        SELECT * FROM letras 
        ORDER BY fecha_agregado DESC
        LIMIT :limit
    """)
    fun obtenerLetrasRecientes(limit: Int = 20): Flow<List<LetraEntity>>

    // ==================== ESTADÍSTICAS Y CONTADORES ====================

    /**
     * Cuenta cuántas letras están almacenadas
     */
    @Query("SELECT COUNT(*) FROM letras")
    suspend fun contarLetras(): Int

    /**
     * Cuenta letras por fuente específica
     */
    @Query("SELECT COUNT(*) FROM letras WHERE fuente = :fuente COLLATE NOCASE")
    suspend fun contarLetrasPorFuente(fuente: String): Int

    /**
     * Verifica si existe una letra para una canción
     */
    @Query("SELECT EXISTS(SELECT 1 FROM letras WHERE id_cancion = :idCancion)")
    suspend fun existeLetraParaCancion(idCancion: Int): Boolean

    // ==================== CACHÉ Y VALIDACIÓN ====================

    /**
     * Obtiene letras que necesitan actualización
     * (por ejemplo, más antiguas que cierta fecha)
     */
    @Query("""
        SELECT * FROM letras 
        WHERE fecha_agregado < :timestamp
        ORDER BY fecha_agregado ASC
        LIMIT :limit
    """)
    suspend fun obtenerLetrasParaActualizar(
        timestamp: Int = System.currentTimeMillis().toInt() - (30 * 24 * 60 * 60 * 1000), // 30 días
        limit: Int = 50
    ): List<LetraEntity>

    /**
     * Actualiza la fuente y fecha de una letra existente
     */
    @Query("""
        UPDATE letras 
        SET fuente = :fuente, fecha_agregado = :timestamp
        WHERE id_cancion = :idCancion
    """)
    suspend fun actualizarFuenteLetra(
        idCancion: Int,
        fuente: String,
        timestamp: Int = System.currentTimeMillis().toInt()
    ): Int

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    /**
     * Elimina letras de canciones que ya no existen en la base de datos
     */
    @Query("""
        DELETE FROM letras 
        WHERE id_cancion NOT IN (SELECT id_cancion FROM canciones)
    """)
    suspend fun limpiarLetrasHuerfanas(): Int

    /**
     * Elimina letras vacías o inválidas
     */
    @Query("""
        DELETE FROM letras 
        WHERE texto_letra IS NULL OR texto_letra = '' OR LENGTH(texto_letra) < 10
    """)
    suspend fun limpiarLetrasInvalidas(): Int

    /**
     * Elimina todas las letras
     * ⚠️ Usar con precaución
     */
    @Query("DELETE FROM letras")
    suspend fun eliminarTodasLasLetras(): Int

    // ==================== OPERACIONES AVANZADAS ====================

    /**
     * Actualiza o inserta una letra con validación
     */
    @Transaction
    suspend fun upsertLetra(idCancion: Int, textoLetra: String, fuente: String = "manual"): Int {
        val letraExistente = obtenerLetraPorIdCancionSuspending(idCancion)

        return if (letraExistente != null) {
            val letraActualizada = letraExistente.copy(
                textoLetra = textoLetra,
                fuente = fuente,
                fechaAgregado = System.currentTimeMillis()
            )
            actualizarLetra(letraActualizada)
        } else {
            val nuevaLetra = LetraEntity(
                idCancion = idCancion,
                textoLetra = textoLetra,  // ✅ Este es el nombre correcto
                fuente = fuente
                // ❌ NO incluir 'letra = TODO()' - ese parámetro no existe
            )
            insertarLetra(nuevaLetra)
        }
    }

    /**
     * Obtiene estadísticas de letras por fuente
     */
    @Query("""
    SELECT fuente as nombre, COUNT(*) as cantidad 
    FROM letras 
    GROUP BY fuente 
    ORDER BY cantidad DESC
""")
    suspend fun obtenerEstadisticasPorFuente(): List<TopItemEstadistica>

    /**
     * Busca canciones sin letra
     * (canciones que existen pero no tienen letra asociada)
     */
    @Query("""
        SELECT c.id_cancion 
        FROM canciones c 
        LEFT JOIN letras l ON c.id_cancion = l.id_cancion 
        WHERE l.id_letra IS NULL
        LIMIT :limit
    """)
    suspend fun obtenerCancionesSinLetra(limit: Int = 100): List<Int>

    /**
     * Cuenta canciones sin letra
     */
    @Query("""
        SELECT COUNT(*) 
        FROM canciones c 
        LEFT JOIN letras l ON c.id_cancion = l.id_cancion 
        WHERE l.id_letra IS NULL
    """)
    suspend fun contarCancionesSinLetra(): Int
}