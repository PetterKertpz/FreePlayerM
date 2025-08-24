package com.example.freeplayerm.data.repository

import com.example.freeplayerm.data.local.entity.UsuarioEntity

/**
 * Esta interfaz define el "contrato" para nuestro Repositorio de Usuarios.
 * Describe las operaciones de datos que nuestra aplicación puede realizar relacionadas con los usuarios,
 * abstrayendo los detalles de la fuente de datos (en este caso, Room).
 */
interface UsuarioRepository {

    suspend fun insertarUsuario(usuario: UsuarioEntity)

    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    suspend fun eliminarUsuario(usuario: UsuarioEntity)
}