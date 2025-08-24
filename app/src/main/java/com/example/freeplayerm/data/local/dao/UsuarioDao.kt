package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    // Usamos ABORT para que el registro falle si el usuario ya existe.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>

    // La consulta SQL ha sido corregida para buscar en la columna 'correo'.
    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity)
}