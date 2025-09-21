// en: app/src/main/java/com/example/freeplayerm/data/local/dao/CancionDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.DetalleListaReproduccionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.FavoritoEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import kotlinx.coroutines.flow.Flow

@Dao
interface CancionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarArtista(artista: ArtistaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAlbum(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCancion(cancion: CancionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarGenero(genero: GeneroEntity): Long

    @Update
    suspend fun actualizarLista(lista: ListaReproduccionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun anadirAFavoritos(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE id_usuario = :usuarioId AND id_cancion = :cancionId")
    suspend fun quitarDeFavoritos(usuarioId: Int, cancionId: Int)
//
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDetalleLista(detalle: DetalleListaReproduccionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarListaReproduccion(lista: ListaReproduccionEntity): Long
//
    @Query("DELETE FROM detalle_lista_reproduccion WHERE id_lista = :listaId AND id_cancion IN (:cancionIds)")
    suspend fun quitarCancionesDeLista(listaId: Int, cancionIds: List<Int>)

    @Query("DELETE FROM listas_reproduccion WHERE id_lista = :listaId")
    suspend fun eliminarListaPorId(listaId: Int)

    @Query("SELECT * FROM artistas WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerArtistaPorNombre(nombre: String): ArtistaEntity?

    @Query("SELECT * FROM albumes WHERE titulo = :titulo COLLATE NOCASE AND id_artista = :artistaId LIMIT 1")
    suspend fun obtenerAlbumPorNombreYArtista(titulo: String, artistaId: Int): AlbumEntity?

    @Query("SELECT * FROM generos WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun obtenerGeneroPorNombre(nombre: String): GeneroEntity?

    @Query("SELECT * FROM canciones WHERE archivo_path = :path LIMIT 1")
    suspend fun obtenerCancionPorRuta(path: String): CancionEntity?


    @Query("""
        SELECT c.*, a.nombre AS artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones AS c
        LEFT JOIN artistas AS a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos AS f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
    """)
    fun obtenerCancionesConArtista(usuarioId: Int): Flow<List<CancionConArtista>>

    @Query("SELECT * FROM canciones WHERE id_cancion = :id")
    fun obtenerCancionPorId(id: Int): Flow<CancionEntity?>

    @Query("SELECT * FROM albumes ORDER BY anio DESC")
    fun obtenerTodosLosAlbumes(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM canciones WHERE id_album = :albumId ORDER BY titulo ASC")
    fun obtenerCancionesPorAlbumId(albumId: Int): Flow<List<CancionEntity>>

    @Query("SELECT * FROM artistas ORDER BY nombre ASC")
    fun obtenerTodosLosArtistas(): Flow<List<ArtistaEntity>>

    @Query("SELECT * FROM canciones WHERE id_artista = :artistaId ORDER BY titulo ASC")
    fun obtenerCancionesPorArtistaId(artistaId: Int): Flow<List<CancionEntity>>

    @Query("SELECT * FROM generos ORDER BY nombre ASC")
    fun obtenerTodosLosGeneros(): Flow<List<GeneroEntity>>

    @Query("SELECT * FROM canciones WHERE id_genero = :generoId ORDER BY titulo ASC")
    fun obtenerCancionesPorGeneroId(generoId: Int): Flow<List<CancionEntity>>

    @Query("SELECT * FROM listas_reproduccion WHERE id_usuario = :usuarioId ORDER BY nombre ASC")
    fun obtenerListasPorUsuarioId(usuarioId: Int): Flow<List<ListaReproduccionEntity>>

    @Query("""
    SELECT c.* FROM canciones c
    INNER JOIN detalle_lista_reproduccion d ON c.id_cancion = d.id_cancion
    WHERE d.id_lista = :listaId
    ORDER BY c.titulo ASC
    """)
    fun obtenerCancionesPorListaId(listaId: Int): Flow<List<CancionEntity>>

    @Query("""
    SELECT c.* FROM canciones c
    INNER JOIN favoritos f ON c.id_cancion = f.id_cancion
    WHERE f.id_usuario = :usuarioId
    ORDER BY c.titulo ASC
    """)
    fun obtenerCancionesFavoritas(usuarioId: Int): Flow<List<CancionEntity>>

    @Query("""
        SELECT c.*, a.nombre AS artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones AS c
        LEFT JOIN artistas AS a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos AS f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE c.id_album = :albumId
    """)
    fun obtenerCancionesDeAlbumConArtista(albumId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    // 3. NUEVA: Para las canciones de un artista
    @Query("""
        SELECT c.*, a.nombre AS artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones AS c
        LEFT JOIN artistas AS a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos AS f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE c.id_artista = :artistaId
    """)
    fun obtenerCancionesDeArtistaConArtista(artistaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    // 4. NUEVA: Para las canciones de un género
    @Query("""
        SELECT c.*, a.nombre AS artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones AS c
        LEFT JOIN artistas AS a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos AS f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        WHERE c.id_genero = :generoId
    """)
    fun obtenerCancionesDeGeneroConArtista(generoId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    // 5. NUEVA: Para las canciones de una lista de reproducción
    @Query("""
        SELECT c.*, a.nombre as artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones c
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        LEFT JOIN favoritos AS f ON c.id_cancion = f.id_cancion AND f.id_usuario = :usuarioId
        INNER JOIN detalle_lista_reproduccion d ON c.id_cancion = d.id_cancion
        WHERE d.id_lista = :listaId
    """)
    fun obtenerCancionesDeListaConArtista(listaId: Int, usuarioId: Int): Flow<List<CancionConArtista>>

    // 6. NUEVA: Para las canciones favoritas de un usuario
    @Query("""
        SELECT c.*, a.nombre as artistaNombre, al.titulo AS albumNombre, g.nombre AS generoNombre,
               (f.id_usuario IS NOT NULL) as esFavorita,
               al.portada_path AS portadaPath
        FROM canciones c
        LEFT JOIN artistas a ON c.id_artista = a.id_artista
        LEFT JOIN albumes AS al ON c.id_album = al.id_album
        LEFT JOIN generos AS g ON c.id_genero = g.id_genero
        INNER JOIN favoritos f ON c.id_cancion = f.id_cancion
        WHERE f.id_usuario = :usuarioId
    """)
    fun obtenerCancionesFavoritasConArtista(usuarioId: Int): Flow<List<CancionConArtista>>

}