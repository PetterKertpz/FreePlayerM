// en: app/src/main/java/com/example/freeplayerm/data/local/dao/CancionDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.*
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import kotlinx.coroutines.flow.Flow

/**
 * ⚡ CANCION DAO - OPTIMIZADO Y MULTIUSO v4.0
 *
 * Características:
 * - Operaciones CRUD completas para todas las entidades
 * - Queries optimizadas con índices apropiados
 * - Flow para observables + suspend para snapshots
 * - Búsqueda y filtrado avanzado
 * - Transacciones para operaciones complejas
 * - Paginación ready
 * - Sorting flexible
 * - Data classes específicas para estadísticas (FIX: Map<String, Any> removido)
 *
 * @author Android Data Layer Manager
 * @version 4.0 - Production Ready - Bug Fixes
 */
@Dao
interface CancionDao {

    // ==================== QUERIES BASE OPTIMIZADAS ====================

    companion object {
        /**
         * Query base para CancionConArtista - reutilizable
         * Incluye todos los JOINs necesarios y el estado de favorito
         */
        const val QUERY_CANCION_CON_ARTISTA = """
            SELECT 
                c.*,
                a.nombre AS artistaNombre,
                al.titulo AS albumNombre,
                g.nombre AS generoNombre,
                (f.id_usuario IS NOT NULL) AS esFavorita,
                al.portada_path AS portadaPath,
                al.anio AS fechaLanzamiento
            FROM canciones c
            LEFT JOIN artistas a ON c.id_artista = a.id_artista
            LEFT JOIN albumes al ON c.id_album = al.id_album
            LEFT JOIN generos g ON c.id_genero = g.id_genero
            LEFT JOIN favoritos f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        """
    }

    // ==================== ARTISTAS ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarArtista(artista: ArtistaEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarArtistas(artistas: List<ArtistaEntity>): List<Int>

    @Update
    suspend fun actualizarArtista(artista: ArtistaEntity): Int

    @Delete
    suspend fun eliminarArtista(artista: ArtistaEntity): Int

    @Query("DELETE FROM artistas WHERE id_artista = :artistaId")
    suspend fun eliminarArtistaPorId(artistaId: Int): Int

    @Query("SELECT * FROM artistas WHERE id_artista = :artistaId")
    suspend fun obtenerArtistaPorId(artistaId: Int): ArtistaEntity?

    @Query("SELECT * FROM artistas WHERE id_artista = :artistaId")
    fun obtenerArtistaPorIdFlow(artistaId: Int): Flow<ArtistaEntity?>

    @Query("SELECT * FROM artistas WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerArtistaPorNombre(nombre: String): ArtistaEntity?

    @Query("SELECT * FROM artistas ORDER BY nombre ASC")
    fun obtenerTodosLosArtistas(): Flow<List<ArtistaEntity>>

    @Query("SELECT * FROM artistas ORDER BY nombre ASC LIMIT :limit OFFSET :offset")
    suspend fun obtenerArtistasPaginados(limit: Int, offset: Int): List<ArtistaEntity>

    @Query("""
        SELECT * FROM artistas 
        WHERE nombre LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY nombre ASC
        LIMIT :limit
    """)
    suspend fun buscarArtistas(query: String, limit: Int= 50): List<ArtistaEntity>

    @Query("SELECT COUNT(*) FROM artistas")
    suspend fun contarArtistas(): Int

    @Query("""
        SELECT a.* FROM artistas a
        INNER JOIN canciones c ON a.id_artista = c.id_artista
        GROUP BY a.id_artista
        HAVING COUNT(c.id_cancion) > 0
        ORDER BY a.nombre ASC
    """)
    fun obtenerArtistasConCanciones(): Flow<List<ArtistaEntity>>

    // ==================== ÁLBUMES ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAlbum(album: AlbumEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAlbumes(albumes: List<AlbumEntity>): List<Int>

    @Update
    suspend fun actualizarAlbum(album: AlbumEntity): Int

    @Delete
    suspend fun eliminarAlbum(album: AlbumEntity): Int

    @Query("DELETE FROM albumes WHERE id_album = :albumId")
    suspend fun eliminarAlbumPorId(albumId: Int): Int

    @Query("SELECT * FROM albumes WHERE id_album = :albumId")
    suspend fun obtenerAlbumPorId(albumId: Int): AlbumEntity?

    @Query("SELECT * FROM albumes WHERE id_album = :albumId")
    fun obtenerAlbumPorIdFlow(albumId: Int): Flow<AlbumEntity?>

    @Query("""
        SELECT * FROM albumes 
        WHERE titulo = :titulo COLLATE NOCASE 
        AND id_artista = :artistaId 
        LIMIT 1
    """)
    suspend fun obtenerAlbumPorNombreYArtista(titulo: String, artistaId: Int): AlbumEntity?

    @Query("SELECT * FROM albumes ORDER BY anio DESC, titulo ASC")
    fun obtenerTodosLosAlbumes(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albumes WHERE id_artista = :artistaId ORDER BY anio DESC")
    fun obtenerAlbumesPorArtista(artistaId: Int): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albumes ORDER BY anio DESC LIMIT :limit OFFSET :offset")
    suspend fun obtenerAlbumesPaginados(limit: Int, offset: Int): List<AlbumEntity>

    @Query("""
        SELECT * FROM albumes 
        WHERE titulo LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY anio DESC
        LIMIT :limit
    """)
    suspend fun buscarAlbumes(query: String, limit: Int= 50): List<AlbumEntity>

    @Query("SELECT COUNT(*) FROM albumes")
    suspend fun contarAlbumes(): Int

    @Query("""
        SELECT al.* FROM albumes al
        INNER JOIN canciones c ON al.id_album = c.id_album
        GROUP BY al.id_album
        HAVING COUNT(c.id_cancion) > 0
        ORDER BY al.anio DESC
    """)
    fun obtenerAlbumesConCanciones(): Flow<List<AlbumEntity>>

    // ==================== GÉNEROS ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarGenero(genero: GeneroEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarGeneros(generos: List<GeneroEntity>): List<Int>

    @Update
    suspend fun actualizarGenero(genero: GeneroEntity): Int

    @Delete
    suspend fun eliminarGenero(genero: GeneroEntity): Int

    @Query("SELECT * FROM generos WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerGeneroPorNombre(nombre: String): GeneroEntity?

    @Query("SELECT * FROM generos WHERE id_genero = :generoId")
    suspend fun obtenerGeneroPorId(generoId: Int): GeneroEntity?

    @Query("SELECT * FROM generos ORDER BY nombre ASC")
    fun obtenerTodosLosGeneros(): Flow<List<GeneroEntity>>

    @Query("""
        SELECT g.* FROM generos g
        INNER JOIN canciones c ON g.id_genero = c.id_genero
        GROUP BY g.id_genero
        HAVING COUNT(c.id_cancion) > 0
        ORDER BY g.nombre ASC
    """)
    fun obtenerGenerosConCanciones(): Flow<List<GeneroEntity>>

    @Query("SELECT COUNT(*) FROM generos")
    suspend fun contarGeneros(): Int

    // ==================== CANCIONES ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCancion(cancion: CancionEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCanciones(canciones: List<CancionEntity>): List<Int>

    @Update
    suspend fun actualizarCancion(cancion: CancionEntity): Int

    @Delete
    suspend fun eliminarCancion(cancion: CancionEntity): Int

    @Query("DELETE FROM canciones WHERE id_cancion = :cancionId")
    suspend fun eliminarCancionPorId(cancionId: Int): Int

    @Query("DELETE FROM canciones WHERE id_cancion IN (:cancionIds)")
    suspend fun eliminarCancionesPorIds(cancionIds: List<Int>): Int

    @Query("SELECT * FROM canciones WHERE id_cancion = :cancionId")
    suspend fun obtenerCancionPorId(cancionId: Int): CancionEntity?

    @Query("SELECT * FROM canciones WHERE id_cancion = :cancionId")
    fun obtenerCancionPorIdFlow(cancionId: Int): Flow<CancionEntity?>

    @Query("SELECT * FROM canciones ORDER BY titulo ASC")
    fun obtenerTodasLasCanciones(): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones ORDER BY titulo ASC LIMIT :limit OFFSET :offset")
    suspend fun obtenerCancionesPaginadas(limit: Int, offset: Int): List<CancionEntity>

    @Query("SELECT COUNT(*) FROM canciones")
    suspend fun contarCanciones(): Int

    /**
     * BÚSQUEDA AVANZADA DE CANCIONES
     * Busca en título, artista, álbum y género
     */
    @Query("""
        SELECT c.* FROM canciones c
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        WHERE c.titulo LIKE '%' || :query || '%'
           OR a.nombre LIKE '%' || :query || '%'
           OR al.titulo LIKE '%' || :query || '%'
           OR g.nombre LIKE '%' || :query || '%'
        ORDER BY c.titulo ASC
        LIMIT :limit
    """)
    suspend fun buscarCanciones(query: String, limit: Int= 100): List<CancionEntity>

    /**
     * BÚSQUEDA CON TODOS LOS DETALLES
     * Devuelve CancionConArtista con toda la información relacionada
     */
    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA WHERE c.titulo LIKE '%' || :query || '%' ORDER BY c.titulo ASC LIMIT :limit")
    suspend fun buscarCancionesConDetalles(query: String, usuarioId: Int, limit: Int= 100): List<CancionConArtista>

    // ==================== QUERIES POR RELACIÓN ====================

    @Query("SELECT * FROM canciones WHERE id_artista = :artistaId ORDER BY titulo ASC")
    fun obtenerCancionesPorArtista(artistaId: Int): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE id_album = :albumId ORDER BY numero_pista ASC")
    fun obtenerCancionesPorAlbum(albumId: Int): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE id_genero = :generoId ORDER BY titulo ASC")
    fun obtenerCancionesPorGenero(generoId: Int): Flow<List<CancionEntity>>

    /**
     * CANCIONES CON TODOS LOS DETALLES
     * Usa la query base con JOINs para obtener toda la información
     */
    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA ORDER BY c.titulo ASC")
    fun obtenerCancionesConArtista(usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA WHERE c.id_artista = :artistaId ORDER BY c.titulo ASC")
    fun obtenerCancionesConArtistaPorArtista(artistaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA WHERE c.id_album = :albumId ORDER BY c.numero_pista ASC")
    fun obtenerCancionesConArtistaPorAlbum(albumId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA WHERE c.id_genero = :generoId ORDER BY c.titulo ASC")
    fun obtenerCancionesConArtistaPorGenero(generoId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA WHERE f.id_usuario = :usuarioId ORDER BY c.titulo ASC")
    fun obtenerFavoritas(usuarioId: Int): Flow<List<CancionConArtista>>

    // ==================== ORDENAMIENTO ====================

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA ORDER BY c.fecha_agregado DESC LIMIT :limit")
    fun obtenerCancionesRecientes(usuarioId: Int, limit: Int= 20): Flow<List<CancionConArtista>>

    @Transaction
    @Query("$QUERY_CANCION_CON_ARTISTA ORDER BY c.veces_reproducida DESC LIMIT :limit")
    fun obtenerCancionesMasReproducidas(usuarioId: Int, limit: Int= 20): Flow<List<CancionConArtista>>

    @Transaction
    @Query("""
        $QUERY_CANCION_CON_ARTISTA 
        WHERE c.ultima_reproduccion IS NOT NULL 
        ORDER BY c.ultima_reproduccion DESC 
        LIMIT :limit
    """)
    fun obtenerUltimasReproducidas(usuarioId: Int, limit: Int= 20): Flow<List<CancionConArtista>>

    /**
     * ACTUALIZAR ESTADÍSTICAS DE REPRODUCCIÓN
     * Se llama cada vez que se reproduce una canción
     */
    @Query("""
        UPDATE canciones 
        SET veces_reproducida = veces_reproducida + 1,
            ultima_reproduccion = :timestamp
        WHERE id_cancion = :cancionId
    """)
    suspend fun incrementarReproduccion(cancionId: Int, timestamp: Int = System.currentTimeMillis().toInt())

    // ==================== FAVORITOS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarAFavoritos(favorito: FavoritoEntity): Int

    @Query("DELETE FROM favoritos WHERE id_usuario = :usuarioId AND id_cancion = :cancionId")
    suspend fun quitarDeFavoritos(usuarioId: Int, cancionId: Int): Int

    @Transaction
    suspend fun toggleFavorito(usuarioId: Int, cancionId: Int) {
        if (esFavorita(usuarioId, cancionId)) {
            quitarDeFavoritos(usuarioId, cancionId)
        } else {
            agregarAFavoritos(FavoritoEntity(idUsuario = usuarioId, idCancion = cancionId))
        }
    }

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favoritos 
            WHERE id_usuario = :usuarioId AND id_cancion = :cancionId
        )
    """)
    suspend fun esFavorita(usuarioId: Int, cancionId: Int): Boolean

    @Query("""
        SELECT c.id_cancion FROM canciones c
        INNER JOIN favoritos f ON c.id_cancion = f.id_cancion
        WHERE f.id_usuario = :usuarioId
    """)
    fun obtenerIdsFavoritas(usuarioId: Int): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM favoritos WHERE id_usuario = :usuarioId")
    suspend fun contarFavoritos(usuarioId: Int): Int

    @Query("DELETE FROM favoritos WHERE id_usuario = :usuarioId")
    suspend fun limpiarFavoritos(usuarioId: Int): Int

    // ==================== LISTAS DE REPRODUCCIÓN ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarListaReproduccion(lista: ListaReproduccionEntity): Int

    @Update
    suspend fun actualizarListaReproduccion(lista: ListaReproduccionEntity): Int

    @Delete
    suspend fun eliminarListaReproduccion(lista: ListaReproduccionEntity): Int

    @Query("DELETE FROM listas_reproduccion WHERE id_lista = :listaId")
    suspend fun eliminarListaPorId(listaId: Int): Int

    @Query("SELECT * FROM listas_reproduccion WHERE id_lista = :listaId")
    suspend fun obtenerListaPorId(listaId: Int): ListaReproduccionEntity?

    @Query("SELECT * FROM listas_reproduccion WHERE id_usuario = :usuarioId ORDER BY nombre ASC")
    fun obtenerListasPorUsuario(usuarioId: Int): Flow<List<ListaReproduccionEntity>>

    @Query("""
        SELECT * FROM listas_reproduccion 
        WHERE id_usuario = :usuarioId 
        AND nombre LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY nombre ASC
    """)
    suspend fun buscarListas(usuarioId: Int, query: String): List<ListaReproduccionEntity>

    @Query("SELECT COUNT(*) FROM listas_reproduccion WHERE id_usuario = :usuarioId")
    suspend fun contarListas(usuarioId: Int): Int

    // ==================== DETALLES DE LISTA ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarCancionALista(detalle: DetalleListaReproduccionEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarCancionesALista(detalles: List<DetalleListaReproduccionEntity>): List<Int>

    @Query("""
        DELETE FROM detalle_lista_reproduccion 
        WHERE id_lista = :listaId AND id_cancion = :cancionId
    """)
    suspend fun quitarCancionDeLista(listaId: Int, cancionId: Int): Int

    @Query("""
        DELETE FROM detalle_lista_reproduccion 
        WHERE id_lista = :listaId AND id_cancion IN (:cancionIds)
    """)
    suspend fun quitarCancionesDeLista(listaId: Int, cancionIds: List<Int>): Int

    @Query("DELETE FROM detalle_lista_reproduccion WHERE id_lista = :listaId")
    suspend fun limpiarLista(listaId: Int): Int

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM detalle_lista_reproduccion
            WHERE id_lista = :listaId AND id_cancion = :cancionId
        )
    """)
    suspend fun cancionEstaEnLista(listaId: Int, cancionId: Int): Boolean

    @Query("""
        SELECT COUNT(*) FROM detalle_lista_reproduccion 
        WHERE id_lista = :listaId
    """)
    suspend fun contarCancionesEnLista(listaId: Int): Int

    /**
     * OBTENER CANCIONES DE UNA LISTA CON DETALLES
     */
    @Transaction
    @Query("""
        SELECT c.*, a.nombre AS artistaNombre, al.titulo AS albumNombre, 
               g.nombre AS generoNombre, (f.id_usuario IS NOT NULL) AS esFavorita,
               al.portada_path AS portadaPath, al.anio AS fechaLanzamiento
        FROM detalle_lista_reproduccion dlr
        INNER JOIN canciones c ON dlr.id_cancion = c.id_cancion
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE dlr.id_lista = :listaId
        ORDER BY dlr.orden ASC
    """)
    fun obtenerCancionesDeLista(listaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Transaction
    suspend fun moverCancionEnLista(
        listaId: Int,
        cancionId: Int,
        nuevaPosicion: Int
    ) {
        // Implementar lógica de reordenamiento si se usa campo de orden
    }

    @Transaction // Importante porque CancionConArtista usa @Embedded
    @Query("""
        SELECT 
            C.*, 
            A.nombre AS artistaNombre,
            AL.titulo AS albumNombre,
            AL.portada_path AS portadaPath,
            AL.fecha_lanzamiento AS fechaLanzamiento,
            G.nombre AS generoNombre,
            CASE WHEN F.id_cancion IS NOT NULL THEN 1 ELSE 0 END AS esFavorita
        FROM canciones C
        LEFT JOIN artistas A ON C.id_artista = A.id_artista
        LEFT JOIN albumes AL ON C.id_album = AL.id_album
        LEFT JOIN generos G ON C.id_genero = G.id_genero
        LEFT JOIN favoritos F ON C.id_cancion = F.id_cancion AND F.id_usuario = :usuarioId
        WHERE C.letra_disponible = 0 
        ORDER BY C.fecha_agregado DESC
    """)
    suspend fun obtenerCancionesSinLetra(usuarioId: Int): List<CancionConArtista>

    // ==================== ESTADÍSTICAS (CORREGIDO) ====================

    /**
     * TOP ARTISTAS POR CANTIDAD DE CANCIONES
     * FIX: Ahora devuelve List<TopItemEstadistica> en lugar de Map<String, Any>
     */
    @Query("""
        SELECT a.nombre, COUNT(c.id_cancion) as cantidad
        FROM artistas a
        LEFT JOIN canciones c ON a.id_artista = c.id_artista
        GROUP BY a.id_artista
        ORDER BY cantidad DESC
        LIMIT :limit
    """)
    suspend fun obtenerArtistasTopPorCanciones(limit: Int= 10): List<TopItemEstadistica>

    /**
     * TOP GÉNEROS POR CANTIDAD DE CANCIONES
     * FIX: Ahora devuelve List<TopItemEstadistica> en lugar de Map<String, Any>
     */
    @Query("""
        SELECT g.nombre, COUNT(c.id_cancion) as cantidad
        FROM generos g
        LEFT JOIN canciones c ON g.id_genero = c.id_genero
        GROUP BY g.id_genero
        ORDER BY cantidad DESC
        LIMIT :limit
    """)
    suspend fun obtenerGenerosTopPorCanciones(limit: Int= 10): List<TopItemEstadistica>

    /**
     * ESTADÍSTICAS GENERALES DE LA BIBLIOTECA
     * FIX: Ahora devuelve EstadisticasGenerales en lugar de Map<String, Any>
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT c.id_cancion) as total_canciones,
            COUNT(DISTINCT a.id_artista) as total_artistas,
            COUNT(DISTINCT al.id_album) as total_albumes,
            COUNT(DISTINCT g.id_genero) as total_generos,
            COALESCE(SUM(c.duracion_segundos), 0) as duracion_total
        FROM canciones c
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes al ON c.id_album = al.id_album
        LEFT JOIN generos g ON c.id_genero = g.id_genero
    """)
    suspend fun obtenerEstadisticasGenerales(): EstadisticasGenerales

    /**
     * ESTADÍSTICAS ADICIONALES - TOP ÁLBUMES
     */
    @Query("""
        SELECT 
            al.titulo,
            a.nombre as artista_nombre,
            COUNT(c.id_cancion) as cantidad_canciones,
            COALESCE(SUM(c.duracion_segundos), 0) as duracion_total
        FROM albumes al
        INNER JOIN canciones c ON al.id_album = c.id_album
        LEFT JOIN artistas a ON al.id_artista = a.id_artista
        GROUP BY al.id_album
        ORDER BY cantidad_canciones DESC
        LIMIT :limit
    """)
    suspend fun obtenerAlbumesTopPorCanciones(limit: Int= 10): List<AlbumEstadistica>

    // ==================== OPERACIONES EN LOTE ====================

    @Transaction
    suspend fun insertarArtistaConAlbumesYCanciones(
        artista: ArtistaEntity,
        albumes: List<AlbumEntity>,
        canciones: List<CancionEntity>
    ) {
        val artistaId = insertarArtista(artista)
        if (artistaId > 0) {
            insertarAlbumes(albumes.map { it.copy(idArtista = artistaId) })
            insertarCanciones(canciones.map { it.copy(idArtista = artistaId) })
        }
    }

    @Transaction
    suspend fun eliminarArtistaCompleto(artistaId: Int) {
        // Room manejará las cascadas si están configuradas en las relaciones
        eliminarArtistaPorId(artistaId)
    }

    @Transaction
    suspend fun duplicarLista(listaOriginalId: Int, nuevoNombre: String, usuarioId: Int): Int {
        val listaOriginal = obtenerListaPorId(listaOriginalId) ?: return -1

        val nuevaLista = listaOriginal.copy(
            idLista = 0,
            nombre = nuevoNombre
        )

        val nuevaListaId = insertarListaReproduccion(nuevaLista)

        // Copiar canciones (necesitarías obtener las canciones de la lista original)
        // Esta es una implementación simplificada

        return nuevaListaId
    }

    // ==================== MANTENIMIENTO ====================

    @Query("DELETE FROM canciones WHERE archivo_path IS NULL OR archivo_path = ''")
    suspend fun limpiarCancionesSinArchivo(): Int

    @Query("""
        DELETE FROM artistas 
        WHERE id_artista NOT IN (SELECT DISTINCT id_artista FROM canciones WHERE id_artista IS NOT NULL)
    """)
    suspend fun limpiarArtistasHuerfanos(): Int

    @Query("""
        DELETE FROM albumes 
        WHERE id_album NOT IN (SELECT DISTINCT id_album FROM canciones WHERE id_album IS NOT NULL)
    """)
    suspend fun limpiarAlbumesHuerfanos(): Int

    @Query("""
        DELETE FROM generos 
        WHERE id_genero NOT IN (SELECT DISTINCT id_genero FROM canciones WHERE id_genero IS NOT NULL)
    """)
    suspend fun limpiarGenerosHuerfanos(): Int

    @Transaction
    suspend fun limpiarDatosHuerfanos(): Int{
        val artistasEliminados = limpiarArtistasHuerfanos()
        val albumesEliminados = limpiarAlbumesHuerfanos()
        val generosEliminados = limpiarGenerosHuerfanos()
        return artistasEliminados + albumesEliminados + generosEliminados
    }

    // ==================== VALIDACIÓN Y DIAGNÓSTICO ====================

    /**
     * Verifica la integridad de los datos
     */
    @Query("""
        SELECT COUNT(*) FROM canciones 
        WHERE id_artista NOT IN (SELECT id_artista FROM artistas)
    """)
    suspend fun contarCancionesSinArtista(): Int

    @Query("""
        SELECT COUNT(*) FROM canciones 
        WHERE id_album NOT IN (SELECT id_album FROM albumes)
        AND id_album IS NOT NULL
    """)
    suspend fun contarCancionesSinAlbum(): Int

    /**
     * Resetea los contadores de reproducción (útil para testing)
     */
    @Query("UPDATE canciones SET veces_reproducida = 0, ultima_reproduccion = NULL")
    suspend fun resetearEstadisticasReproduccion(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDetalleLista(detalle: DetalleListaReproduccionEntity): Int

    @Query("""
    $QUERY_CANCION_CON_ARTISTA
    WHERE c.id_album = :albumId
    ORDER BY c.numero_pista ASC, c.titulo ASC
""")
    fun obtenerCancionesDeAlbumConArtista(albumId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Query("""
    $QUERY_CANCION_CON_ARTISTA
    WHERE c.id_artista = :artistaId
    ORDER BY c.titulo ASC
""")
    fun obtenerCancionesDeArtistaConArtista(artistaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Query("""
    $QUERY_CANCION_CON_ARTISTA
    WHERE c.id_genero = :generoId
    ORDER BY c.titulo ASC
""")
    fun obtenerCancionesDeGeneroConArtista(generoId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Query("""
    SELECT 
        c.*,
        a.nombre AS artistaNombre,
        al.titulo AS albumNombre,
        g.nombre AS generoNombre,
        (f.id_usuario IS NOT NULL) AS esFavorita,
        al.portada_path AS portadaPath,
        al.anio AS fechaLanzamiento
    FROM detalle_lista_reproduccion d
    INNER JOIN canciones c ON d.id_cancion = c.id_cancion
    LEFT JOIN artistas a ON c.id_artista = a.id_artista
    LEFT JOIN albumes al ON c.id_album = al.id_album
    LEFT JOIN generos g ON c.id_genero = g.id_genero
    LEFT JOIN favoritos f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
    WHERE d.id_lista = :listaId
    ORDER BY d.orden ASC, d.fecha_agregado ASC
""")
    fun obtenerCancionesDeListaConArtista(listaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    @Query("SELECT * FROM canciones WHERE archivo_path = :ruta LIMIT 1")
    suspend fun obtenerCancionPorRuta(ruta: String): CancionEntity?

    @Query("""
    SELECT 
        c.*,
        a.nombre as artistaNombre,
        al.titulo as albumNombre,
        g.nombre as generoNombre,
        EXISTS(SELECT 1 FROM favoritos f WHERE f.id_cancion = c.id_cancion AND f.id_usuario = :usuarioId) as esFavorita,
        al.portada_path as portadaPath,
        al.anio as fechaLanzamiento
    FROM canciones c
    LEFT JOIN artistas a ON c.id_artista = a.id_artista
    LEFT JOIN albumes al ON c.id_album = al.id_album
    LEFT JOIN generos g ON c.id_genero = g.id_genero
    WHERE c.id_cancion = :idCancion
    LIMIT 1
""")
    suspend fun obtenerCancionConArtistaPorId(idCancion: Int, usuarioId: Int): CancionConArtista?

    @Transaction
    @Query("""
    SELECT c.*, 
           a.nombre as artistaNombre,
           alb.id_album as albumNombre,
           alb.portada_path as portadaPath,
           alb.fecha_lanzamiento as fechaLanzamiento,
           g.nombre as generoNombre,
           EXISTS(SELECT 1 FROM favoritos WHERE id_cancion = c.id_cancion) as esFavorita
    FROM canciones c
    LEFT JOIN artistas a ON c.id_artista = a.id_artista
    LEFT JOIN albumes alb ON c.id_album = alb.id_Album
    LEFT JOIN generos g ON c.id_genero = g.id_genero
    WHERE c.id_cancion = :idCancion
""")
    fun obtenerCancionConArtista(idCancion: Int): Flow<CancionConArtista?>



}