package com.example.freeplayerm.data.repository

import com.example.freeplayerm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow // <-- Importante añadir

interface UsuarioRepository {

    suspend fun insertarUsuario(usuario: UsuarioEntity)
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?
    suspend fun actualizarUsuario(usuario: UsuarioEntity)
    suspend fun eliminarUsuario(usuario: UsuarioEntity)
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    // --- NUEVA FUNCIÓN AÑADIDA A LA INTERFAZ ---
    fun obtenerUsuarioPorIdFlow(id: Int): Flow<UsuarioEntity?>

    suspend fun registrarUsuarioLocal(
        nombreUsuario: String,
        correo: String,
        contrasena: String
    ): Result<UsuarioEntity>

    suspend fun iniciarSesionLocal(
        identificador:String,
        contrasena: String
    ): Result<UsuarioEntity>

    suspend fun buscarOCrearUsuarioGoogle(
        correo: String,
        nombreUsuario: String,
        fotoUrl: String?
    ): Result<UsuarioEntity>
}