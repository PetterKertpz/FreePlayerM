// en: app/src/main/java/com/example/freeplayerm/data/local/dao/CancionDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CancionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCancion(cancion: CancionEntity)

    @Query("SELECT * FROM canciones")
    fun obtenerTodasLasCanciones(): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE id_cancion = :id")
    fun obtenerCancionPorId(id: Int): Flow<CancionEntity?>

    @Query("SELECT * FROM albumes ORDER BY anio DESC")
    fun obtenerTodosLosAlbumes(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM canciones WHERE id_album = :albumId ORDER BY titulo ASC")
    fun obtenerCancionesPorAlbumId(albumId: Int): Flow<List<CancionEntity>>
    // Aquí podemos añadir más consultas en el futuro, como:
    // - Obtener canciones por artista
    // - Obtener canciones de un álbum
    // - Obtener canciones favoritas de un usuario
}