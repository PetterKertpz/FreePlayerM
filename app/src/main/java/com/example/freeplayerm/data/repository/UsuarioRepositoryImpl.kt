// app/src/main/java/com/example/freeplayerm/data/repository/UsuarioRepositoryImpl.kt
package com.example.freeplayerm.data.repository

import com.example.freeplayerm.core.security.SeguridadHelper
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao,
    private val sessionRepository: SessionRepository
) : UsuarioRepository {
    private val auth = Firebase.auth

    override suspend fun insertarUsuario(usuario: UsuarioEntity) {
        usuarioDao.insertarUsuario(usuario)
    }

    override suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? {
        return usuarioDao.obtenerUsuarioPorCorreo(correo)
    }

    override suspend fun actualizarUsuario(usuario: UsuarioEntity) {
        usuarioDao.actualizarUsuario(usuario)
    }

    override suspend fun eliminarUsuario(usuario: UsuarioEntity) {
        usuarioDao.eliminarUsuario(usuario)
    }

    override suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> {
        return try {
            val usuario = usuarioDao.obtenerUsuarioPorCorreo(correo)

            if (usuario == null) {
                return Result.failure(Exception("No existe una cuenta registrada con ese correo electrónico."))
            }
            if (usuario.tipoAutenticacion != UsuarioEntity.TIPO_LOCAL) {
                return Result.failure(Exception("Esa cuenta fue registrada usando un método social y no tiene contraseña local."))
            }

            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun registrarUsuarioLocal(
        nombreUsuario: String,
        correo: String,
        contrasena: String
    ): Result<UsuarioEntity> {
        return try {
            if (usuarioDao.existeCorreo(correo)) {
                return Result.failure(Exception("El correo electrónico ya está registrado."))
            }

            if (usuarioDao.existeNombreUsuario(nombreUsuario)) {
                return Result.failure(Exception("El nombre de usuario ya está en uso."))
            }

            val contrasenaHasheada = SeguridadHelper.hashContrasena(contrasena)

            val iniciales = nombreUsuario.split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .take(2)
                .joinToString("")
            val fotoUrlPredeterminada = "https://ui-avatars.com/api/?name=$iniciales&background=random&color=fff&size=256"

            val nuevoUsuario = UsuarioEntity(
                nombreUsuario = nombreUsuario,
                correo = correo,
                contrasenia = contrasenaHasheada,
                fechaCreacion = System.currentTimeMillis(),
                fotoPerfil = fotoUrlPredeterminada,
                tipoAutenticacion = UsuarioEntity.TIPO_LOCAL,
                activo = true
            )

            val nuevoId = usuarioDao.insertarUsuario(nuevoUsuario)
            val usuarioCreado = usuarioDao.obtenerUsuarioPorId(nuevoId.toInt())!!

            Result.success(usuarioCreado)

        } catch (e: Exception) {
            Result.failure(Exception("No se pudo completar el registro. ${e.message}", e))
        }
    }

    override suspend fun iniciarSesionLocal(identificador: String, contrasena: String): Result<UsuarioEntity> {
        return try {
            var usuario = usuarioDao.obtenerUsuarioPorCorreo(identificador)

            if (usuario == null) {
                usuario = usuarioDao.obtenerUsuarioPorNombreUsuario(identificador)
            }

            if (usuario == null) {
                return Result.failure(Exception("Usuario o contraseña incorrectos."))
            }

            if (!usuario.activo) {
                return Result.failure(Exception("Esta cuenta ha sido desactivada."))
            }

            if (usuario.tipoAutenticacion != UsuarioEntity.TIPO_LOCAL) {
                return Result.failure(Exception("Este usuario se registró usando un método social."))
            }

            val contrasenaEsValida = SeguridadHelper.verificarContrasena(
                contrasenaPlana = contrasena,
                contrasenaHasheada = usuario.contrasenia
            )

            if (contrasenaEsValida) {
                usuarioDao.actualizarUltimaSesion(usuario.idUsuario)
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ocurrió un error inesperado.", e))
        }
    }

    override suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity? {
        return usuarioDao.obtenerUsuarioPorId(id)
    }

    override suspend fun buscarOCrearUsuarioGoogle(
        correo: String,
        nombreUsuario: String,
        fotoUrl: String?
    ): Result<UsuarioEntity> {
        return try {
            val urlFinalParaGuardar = if (fotoUrl.isNullOrEmpty()) {
                val iniciales = nombreUsuario.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                    .take(2)
                    .joinToString("")
                "https://ui-avatars.com/api/?name=$iniciales&background=random&color=fff&size=256"
            } else {
                fotoUrl
            }

            val usuarioExistente = usuarioDao.obtenerUsuarioPorCorreo(correo)

            if (usuarioExistente == null) {
                val nuevoUsuario = UsuarioEntity(
                    nombreUsuario = nombreUsuario,
                    correo = correo,
                    contrasenia = "",
                    fechaCreacion = System.currentTimeMillis(),
                    fotoPerfil = urlFinalParaGuardar,
                    tipoAutenticacion = UsuarioEntity.TIPO_GOOGLE,
                    activo = true
                )
                val nuevoId = usuarioDao.insertarUsuario(nuevoUsuario)
                val usuarioCreado = usuarioDao.obtenerUsuarioPorId(nuevoId.toInt())!!

                usuarioDao.actualizarUltimaSesion(usuarioCreado.idUsuario)
                Result.success(usuarioCreado)

            } else {
                val usuarioActualizado = usuarioExistente.copy(
                    nombreUsuario = nombreUsuario,
                    fotoPerfil = urlFinalParaGuardar,
                    tipoAutenticacion = UsuarioEntity.TIPO_GOOGLE
                )
                usuarioDao.actualizarUsuario(usuarioActualizado)
                usuarioDao.actualizarUltimaSesion(usuarioActualizado.idUsuario)
                Result.success(usuarioActualizado)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al sincronizar el usuario de Google.", e))
        }
    }

    override fun obtenerUsuarioPorIdFlow(id: Int): Flow<UsuarioEntity?> {
        return usuarioDao.obtenerUsuarioPorIdFlow(id)
    }
}