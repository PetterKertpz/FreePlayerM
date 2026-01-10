package com.example.freeplayerm.data.repository

import com.example.freeplayerm.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {

   suspend fun insertarUsuario(usuario: UserEntity)

   suspend fun obtenerUsuarioPorCorreo(correo: String): UserEntity?

   suspend fun actualizarUsuario(usuario: UserEntity)

   suspend fun eliminarUsuario(usuario: UserEntity)

   suspend fun obtenerUsuarioPorId(id: Int): UserEntity?

   suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit>

   fun obtenerUsuarioPorIdFlow(id: Int): Flow<UserEntity?>
   
   suspend fun sincronizarEstadisticas(userId: Int)
   suspend fun incrementarReproducciones(userId: Int)
   suspend fun actualizarUltimaSesion(userId: Int)
   suspend fun actualizarFotoPerfil(userId: Int, nuevaFotoUrl: String)
   suspend fun actualizarInformacion(
      userId: Int,
      nombreCompleto: String? = null,
      biografia: String? = null
   )
   
   suspend fun registrarUsuarioLocal(
      nombreUsuario: String,
      correo: String,
      contrasena: String,
   ): Result<UserEntity>

   suspend fun iniciarSesionLocal(identificador: String, contrasena: String): Result<UserEntity>

   suspend fun buscarOCrearUsuarioGoogle(
      correo: String,
      nombreUsuario: String,
      fotoUrl: String?,
   ): Result<UserEntity>
}
