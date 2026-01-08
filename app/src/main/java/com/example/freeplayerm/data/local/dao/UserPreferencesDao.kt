package com.example.freeplayerm.data.local.dao

import androidx.room.*
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * ⚙️ USER PREFERENCES DAO
 *
 * Data Access Object para UserPreferencesEntity
 * Maneja todas las operaciones de BD relacionadas con preferencias de usuario
 */
@Dao
interface UserPreferencesDao {
   
   @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertarPreferencias(preferencias: UserPreferencesEntity): Long
   
   @Update
   suspend fun actualizarPreferencias(preferencias: UserPreferencesEntity)
   
   @Delete
   suspend fun eliminarPreferencias(preferencias: UserPreferencesEntity)
   
   @Query("SELECT * FROM preferencias_usuario WHERE id_usuario = :idUsuario")
   suspend fun obtenerPreferenciasPorId(idUsuario: Int): UserPreferencesEntity?
   
   @Query("SELECT * FROM preferencias_usuario WHERE id_usuario = :idUsuario")
   fun obtenerPreferenciasPorIdFlow(idUsuario: Int): Flow<UserPreferencesEntity?>
   
   @Query("DELETE FROM preferencias_usuario WHERE id_usuario = :idUsuario")
   suspend fun eliminarPreferenciasPorUsuario(idUsuario: Int)
   
   @Query("SELECT EXISTS(SELECT 1 FROM preferencias_usuario WHERE id_usuario = :idUsuario)")
   suspend fun existenPreferencias(idUsuario: Int): Boolean
}