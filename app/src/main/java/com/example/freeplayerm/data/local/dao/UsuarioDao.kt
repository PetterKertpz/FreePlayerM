package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow // <-- Importante añadir

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE nombre_usuario = :nombreUsuario LIMIT 1")
    suspend fun obtenerUsuarioPorNombreUsuario(nombreUsuario: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    // --- NUEVA FUNCIÓN AÑADIDA ---
    // Esta función devuelve un Flow, que permite observar cambios en el usuario en tiempo real.
    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun obtenerUsuarioPorIdFlow(id: Int): Flow<UsuarioEntity?>

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity)
}