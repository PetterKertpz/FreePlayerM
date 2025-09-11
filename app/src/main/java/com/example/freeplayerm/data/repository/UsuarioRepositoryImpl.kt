// Movido al paquete 'repository'
package com.example.freeplayerm.data.repository

// Añadimos los imports necesarios para no usar nombres largos
import com.example.freeplayerm.core.security.SeguridadHelper
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import java.util.Date

// Renombramos la clase a 'UsuarioRepositoryImpl'
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository { // Asegúrate de que implementa 'UsuarioRepository'

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

    override suspend fun registrarUsuarioLocal(
        nombreUsuario: String,
        correo: String,
        contrasena: String
    ): Result<Unit> {
        return try {
            // 1. Verificar si el correo ya está en uso.
            if (usuarioDao.obtenerUsuarioPorCorreo(correo) != null) {
                return Result.failure(Exception("El correo electrónico ya está registrado."))
            }

            // 2. Hashear la contraseña. Es la única responsabilidad de seguridad aquí.
            val contrasenaHasheada = SeguridadHelper.hashContrasena(contrasena)

            // 3. Crear la entidad COMPLETA Y CONSISTENTE aquí mismo.
            val nuevoUsuario = UsuarioEntity(
                nombreUsuario = nombreUsuario,
                correo = correo,
                contrasenaHash = contrasenaHasheada,
                fechaRegistro = Date(), // La fecha actual
                fotoPerfilPathLocal = null, // Nulo por defecto al registrarse
                tipoAutenticacion = "LOCAL" // Definimos el tipo de autenticación
            )

            // 4. Insertar el usuario en la base de datos.
            usuarioDao.insertarUsuario(nuevoUsuario)
            Result.success(Unit)
        } catch (_: Exception) {
            // Capturamos cualquier error, como una violación de la constraint 'unique'.
            Result.failure(Exception("No se pudo completar el registro. Inténtalo de nuevo."))
        }
    }

    override suspend fun iniciarSesionLocal(identificador: String, contrasena: String): Result<UsuarioEntity> {
        return try {
            // 1. Intentamos encontrar al usuario, primero por correo.
            //    Usamos una variable 'var' porque su valor podría cambiar.
            var usuario = usuarioDao.obtenerUsuarioPorCorreo(identificador)

            // 2. Si no se encontró por correo, intentamos por nombre de usuario.
            if (usuario == null) {
                usuario = usuarioDao.obtenerUsuarioPorNombreUsuario(identificador)
            }

            // 3. Si después de ambas búsquedas el usuario sigue siendo nulo,
            //    entonces no existe y devolvemos un error.
            if (usuario == null) {
                return Result.failure(Exception("Usuario o contraseña incorrectos."))
            }

            // A partir de aquí, la lógica de verificación de la contraseña es exactamente la misma.
            val contrasenaHasheada = usuario.contrasenaHash
                ?: return Result.failure(Exception("Este usuario se registró con un método social."))

            val contrasenaEsValida = SeguridadHelper.verificarContrasena(
                contrasenaPlana = contrasena,
                contrasenaHasheada = contrasenaHasheada
            )

            if (contrasenaEsValida) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos."))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Ocurrió un error inesperado."))
        }
    }

    override suspend fun buscarOCrearUsuarioGoogle(
        correo: String,
        nombreUsuario: String
    ): Result<UsuarioEntity> {
        return try {
            // 1. Buscamos si el usuario ya existe por su correo.
            var usuario = usuarioDao.obtenerUsuarioPorCorreo(correo)

            // 2. Si no existe, creamos una nueva entidad para él.
            if (usuario == null) {
                val nuevoUsuario = UsuarioEntity(
                    nombreUsuario = nombreUsuario,
                    correo = correo,
                    contrasenaHash = null, // Nulo porque es un inicio de sesión social
                    fechaRegistro = Date(), // La fecha actual
                    fotoPerfilPathLocal = null,
                    tipoAutenticacion = "GOOGLE" // Marcamos que es un usuario de Google
                )
                usuarioDao.insertarUsuario(nuevoUsuario)
                // Lo leemos de nuevo para obtener el ID que la base de datos le asignó
                usuario = usuarioDao.obtenerUsuarioPorCorreo(correo)!!
            }

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}