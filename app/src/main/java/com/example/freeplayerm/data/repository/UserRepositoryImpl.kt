// app/src/main/java/com/example/freeplayerm/data/repository/UsuarioRepositoryImpl.kt
package com.example.freeplayerm.data.repository

import com.example.freeplayerm.core.security.SecurityHelper
import com.example.freeplayerm.data.local.dao.UserDao
import com.example.freeplayerm.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 👤 USUARIO REPOSITORY IMPLEMENTATION
 *
 * Implementación del repositorio de usuarios Maneja autenticación local y con Firebase/Google
 *
 * Características:
 * - ✅ Registro local con hash de contraseñas
 * - ✅ Login local con validación
 * - ✅ Integración con Google Sign-In
 * - ✅ Recuperación de contraseña vía email
 * - ✅ Validaciones de negocio
 * - ✅ Generación de avatares por defecto
 */
@Singleton
class UserRepositoryImpl
@Inject
constructor(
    private val userDao: UserDao,
    private val sessionRepository: SessionRepository,
    private val firebaseAuth: FirebaseAuth,
) : UserRepository {

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    override suspend fun insertarUsuario(usuario: UserEntity) {
        userDao.insertarUsuario(usuario)
    }

    override suspend fun obtenerUsuarioPorCorreo(correo: String): UserEntity? {
        return userDao.obtenerUsuarioPorCorreo(correo)
    }

    override suspend fun actualizarUsuario(usuario: UserEntity) {
        userDao.actualizarUsuario(usuario)
    }

    override suspend fun eliminarUsuario(usuario: UserEntity) {
        userDao.eliminarUsuario(usuario)
    }

    override suspend fun obtenerUsuarioPorId(id: Int): UserEntity? {
        return userDao.obtenerUsuarioPorId(id)
    }

    override fun obtenerUsuarioPorIdFlow(id: Int): Flow<UserEntity?> {
        return userDao.obtenerUsuarioPorIdFlow(id)
    }

    // ==================== AUTENTICACIÓN LOCAL ====================

    override suspend fun registrarUsuarioLocal(
        nombreUsuario: String,
        correo: String,
        contrasena: String,
    ): Result<UserEntity> {
        return try {
            // Validación: Correo duplicado
            if (userDao.existeCorreo(correo)) {
                return Result.failure(Exception("El correo electrónico ya está registrado."))
            }

            // Validación: Nombre de usuario duplicado
            if (userDao.existeNombreUsuario(nombreUsuario)) {
                return Result.failure(Exception("El nombre de usuario ya está en uso."))
            }

            // Validación: Contraseña débil
            val validacionContrasena = validarContrasena(contrasena)
            if (!validacionContrasena.first) {
                return Result.failure(Exception(validacionContrasena.second))
            }

            // Hash de contraseña
            val contrasenaHasheada = SecurityHelper.hashContrasena(contrasena)

            // Generar avatar por defecto
            val fotoUrlPredeterminada = generarAvatarUrl(nombreUsuario)

            // Crear usuario
            val nuevoUsuario =
                UserEntity(
                    nombreUsuario = nombreUsuario,
                    correo = correo,
                    contraseniaHash = contrasenaHasheada,
                    fechaCreacion = System.currentTimeMillis(),
                    fotoPerfil = fotoUrlPredeterminada,
                    tipoAutenticacion = UserEntity.TIPO_LOCAL,
                    activo = true,
                )

            val nuevoId = userDao.insertarUsuario(nuevoUsuario)
            val usuarioCreado = userDao.obtenerUsuarioPorId(nuevoId.toInt())!!

            Result.success(usuarioCreado)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo completar el registro. ${e.message}", e))
        }
    }

    override suspend fun iniciarSesionLocal(
        identificador: String,
        contrasena: String,
    ): Result<UserEntity> {
        return try {
            // Buscar usuario por correo o nombre de usuario
            var usuario = userDao.obtenerUsuarioPorCorreo(identificador)
            if (usuario == null) {
                usuario = userDao.obtenerUsuarioPorNombreUsuario(identificador)
            }

            // Validación: Usuario no existe
            if (usuario == null) {
                return Result.failure(Exception("Usuario o contraseña incorrectos."))
            }

            // Validación: Cuenta desactivada
            if (!usuario.activo) {
                return Result.failure(Exception("Esta cuenta ha sido desactivada."))
            }

            // Validación: Usuario social (no tiene contraseña local)
            if (usuario.tipoAutenticacion != UserEntity.TIPO_LOCAL) {
                return Result.failure(
                    Exception(
                        "Este usuario se registró usando ${usuario.tipoAutenticacion}. " +
                            "Por favor, inicia sesión con ese método."
                    )
                )
            }

            // Verificar contraseña
            val contrasenaEsValida =
                SecurityHelper.verificarContrasena(
                    contrasenaPlana = contrasena,
                    contrasenaHasheada = usuario.contraseniaHash,
                )

            if (contrasenaEsValida) {
                // Generar tokens seguros
                val token = SecurityHelper.generarTokenSesion(usuario.idUsuario)
                val refreshToken = SecurityHelper.generarRefreshToken(usuario.idUsuario)
                val expiracion = SecurityHelper.calcularExpiracion(24)

                userDao.actualizarTokens(usuario.idUsuario, token, refreshToken, expiracion)
                Result.success(usuario.copy(tokenSesion = token))
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ocurrió un error inesperado: ${e.message}", e))
        }
    }

    // ==================== RECUPERACIÓN DE CONTRASEÑA ====================

    override suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> {
        return try {
            // Validar que el usuario existe
            val usuario =
                userDao.obtenerUsuarioPorCorreo(correo)
                    ?: return Result.failure(
                        Exception("No existe una cuenta registrada con ese correo electrónico.")
                    )

            // Validar que es una cuenta local (tiene contraseña)
            if (usuario.tipoAutenticacion != UserEntity.TIPO_LOCAL) {
                return Result.failure(
                    Exception(
                        "Esta cuenta fue registrada usando ${usuario.tipoAutenticacion} " +
                            "y no tiene contraseña local."
                    )
                )
            }

            // Enviar email de recuperación vía Firebase
            firebaseAuth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                Exception("No se pudo enviar el correo de recuperación: ${e.message}", e)
            )
        }
    }

    // ==================== AUTENTICACIÓN SOCIAL (GOOGLE) ====================

    override suspend fun buscarOCrearUsuarioGoogle(
        correo: String,
        nombreUsuario: String,
        fotoUrl: String?,
    ): Result<UserEntity> {
        return try {
            // Generar URL de foto por defecto si no se proporciona
            val urlFinalParaGuardar =
                fotoUrl?.takeIf { it.isNotBlank() } ?: generarAvatarUrl(nombreUsuario)

            val usuarioExistente = userDao.obtenerUsuarioPorCorreo(correo)

            if (usuarioExistente == null) {
                // Crear nuevo usuario de Google
                val nuevoUsuario =
                    UserEntity(
                        nombreUsuario = nombreUsuario,
                        correo = correo,
                        contraseniaHash = "", // Sin contraseña local para OAuth
                        fechaCreacion = System.currentTimeMillis(),
                        fotoPerfil = urlFinalParaGuardar,
                        tipoAutenticacion = UserEntity.TIPO_GOOGLE,
                        providerId = correo, // Guardar el correo como provider ID
                        activo = true,
                    )

                val nuevoId = userDao.insertarUsuario(nuevoUsuario)
                val usuarioCreado =
                    userDao.obtenerUsuarioPorId(nuevoId.toInt())
                        ?: throw Exception("Usuario creado pero no se pudo recuperar de la BD")

                // Generar tokens de sesión
                val token = SecurityHelper.generarTokenSesion(usuarioCreado.idUsuario)
                val refreshToken = SecurityHelper.generarRefreshToken(usuarioCreado.idUsuario)
                val expiracion = SecurityHelper.calcularExpiracion(24)

                android.util.Log.d(
                    "UserRepository",
                    "Generando tokens para nuevo usuario Google ID=${usuarioCreado.idUsuario}, correo=$correo",
                )

                val filasActualizadas =
                    userDao.actualizarTokens(
                        usuarioCreado.idUsuario,
                        token,
                        refreshToken,
                        expiracion,
                    )
                if (filasActualizadas == 0) {
                    throw Exception("No se pudieron actualizar los tokens en la base de datos")
                }
                userDao.actualizarUltimaSesion(usuarioCreado.idUsuario)

                // ✅ Devolver usuario CON tokens actualizados
                val usuarioConTokens =
                    usuarioCreado.copy(
                        tokenSesion = token,
                        refreshToken = refreshToken,
                        tokenExpiracion = expiracion,
                    )
                Result.success(usuarioConTokens)
            } else {
                // Usuario existente: actualizar datos de Google Y tokens
                val usuarioActualizado =
                    usuarioExistente.copy(
                        nombreUsuario = nombreUsuario,
                        fotoPerfil = urlFinalParaGuardar,
                        tipoAutenticacion = UserEntity.TIPO_GOOGLE,
                    )

                userDao.actualizarUsuario(usuarioActualizado)

                android.util.Log.d(
                    "UserRepository",
                    "Actualizando usuario existente Google ID=${usuarioActualizado.idUsuario}, correo=$correo",
                )

                // ✅ Generar nuevos tokens para la sesión actual
                val token = SecurityHelper.generarTokenSesion(usuarioActualizado.idUsuario)
                val refreshToken = SecurityHelper.generarRefreshToken(usuarioActualizado.idUsuario)
                val expiracion = SecurityHelper.calcularExpiracion(24)

                val filasActualizadas =
                    userDao.actualizarTokens(
                        usuarioActualizado.idUsuario,
                        token,
                        refreshToken,
                        expiracion,
                    )
                if (filasActualizadas == 0) {
                    throw Exception("No se pudieron actualizar los tokens para usuario existente")
                }
                userDao.actualizarUltimaSesion(usuarioActualizado.idUsuario)

                // ✅ Devolver con tokens
                Result.success(
                    usuarioActualizado.copy(
                        tokenSesion = token,
                        refreshToken = refreshToken,
                        tokenExpiracion = expiracion,
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al sincronizar el usuario de Google: ${e.message}", e))
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida que una contraseña cumpla con los requisitos mínimos
     *
     * @return Par de (esValida, mensajeError)
     */
    private fun validarContrasena(contrasena: String): Pair<Boolean, String> {
        return when {
            contrasena.length < 8 -> false to "La contraseña debe tener al menos 8 caracteres."
            !contrasena.any { it.isDigit() } ->
                false to "La contraseña debe contener al menos un número."
            !contrasena.any { it.isUpperCase() } ->
                false to "La contraseña debe contener al menos una mayúscula."
            !contrasena.any { it.isLowerCase() } ->
                false to "La contraseña debe contener al menos una minúscula."
            else -> true to ""
        }
    }

    /**
     * Genera una URL de avatar usando UI Avatars
     *
     * @param nombreUsuario Nombre del usuario para generar iniciales
     * @return URL del avatar generado
     */
    private fun generarAvatarUrl(nombreUsuario: String): String {
        val iniciales =
            nombreUsuario
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .take(2)
                .joinToString("")

        return "https://ui-avatars.com/api/" +
            "?name=$iniciales" +
            "&background=random" +
            "&color=fff" +
            "&size=256" +
            "&bold=true"
    }
}
