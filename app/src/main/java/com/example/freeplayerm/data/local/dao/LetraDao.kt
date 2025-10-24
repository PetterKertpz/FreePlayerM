// en: app/src/main/java/com/example/freeplayerm/data/local/dao/LetraDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.LetraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLetra(letra: LetraEntity)

    @Query("SELECT * FROM letras WHERE id_cancion = :idCancion")
    fun obtenerLetraPorIdCancion(idCancion: Int): Flow<LetraEntity?>

    @Query("SELECT * FROM letras WHERE id_cancion = :idCancion")
    suspend fun obtenerLetraPorIdCancionSuspending(idCancion: Int): LetraEntity?
}