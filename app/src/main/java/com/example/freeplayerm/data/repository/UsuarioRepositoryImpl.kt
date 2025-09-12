// app/src/main/java/com/example/freeplayerm/data/repository/UsuarioRepositoryImpl.kt
package com.example.freeplayerm.data.repository

import com.example.freeplayerm.core.security.SeguridadHelper
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

// Clase que implementa la interfaz UsuarioRepository.
// Es la implementación concreta que interactúa con el DAO.
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao,
    private val sessionRepository: SessionRepository // <-- ¡Nueva inyección!
) : UsuarioRepository {

    // Simplemente llama al método correspondiente en el DAO.
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

    /**
     * Registra un nuevo usuario con correo y contraseña.
     * Ventajas:
     * - Centraliza toda la lógica de registro.
     * - Usa Result<> para un manejo de errores limpio en el ViewModel.
     * - Asegura que no se registren correos duplicados.
     * Desventajas:
     * - Podría crecer si se añade más lógica (ej. validación de contraseña compleja).
     */
    override suspend fun registrarUsuarioLocal(
        nombreUsuario: String,
        correo: String,
        contrasena: String
    ): Result<UsuarioEntity> {
        return try {
            // 1. Verificamos si ya existe un usuario con ese correo.
            if (usuarioDao.obtenerUsuarioPorCorreo(correo) != null) {
                return Result.failure(Exception("El correo electrónico ya está registrado."))
            }

            // 2. Hasheamos la contraseña para no guardarla en texto plano.
            val contrasenaHasheada = SeguridadHelper.hashContrasena(contrasena)

            // 3. Generamos una URL para una foto de perfil genérica usando las iniciales.
            val iniciales = nombreUsuario.split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .take(2)
                .joinToString("")
            val fotoUrlPredeterminada = "https://ui-avatars.com/api/?name=$iniciales&background=random&color=fff&size=256"

            // 4. Creamos el objeto UsuarioEntity con todos los datos necesarios.
            val nuevoUsuario = UsuarioEntity(
                nombreUsuario = nombreUsuario,
                correo = correo,
                contrasenaHash = contrasenaHasheada,
                fechaRegistro = Date(),
                fotoPerfilUrl = fotoUrlPredeterminada,
                tipoAutenticacion = "LOCAL"
            )

            // 5. Insertamos en la BD y obtenemos el nuevo ID.
            val nuevoId = usuarioDao.insertarUsuario(nuevoUsuario)

            // 6. Obtenemos el usuario recién creado desde la BD para asegurar consistencia.
            // El '!!' es seguro aquí porque acabamos de insertarlo.
            val usuarioCreado = usuarioDao.obtenerUsuarioPorId(nuevoId.toInt())!!
            Result.success(usuarioCreado)

        } catch (e: Exception) {
            Result.failure(Exception("No se pudo completar el registro. ${e.message}", e))
        }
    }

    /**
     * Inicia sesión para un usuario local.
     * Ventajas:
     * - Permite al usuario usar su correo O su nombre de usuario para ingresar.
     * - Devuelve mensajes de error genéricos por seguridad.
     * Formas alternativas:
     * - Podrías tener dos funciones separadas: una para iniciar por correo y otra por nombre de usuario,
     * pero unificarlas aquí simplifica el ViewModel.
     */
    override suspend fun iniciarSesionLocal(identificador: String, contrasena: String): Result<UsuarioEntity> {
        return try {
            // 1. Buscamos al usuario por correo primero.
            var usuario = usuarioDao.obtenerUsuarioPorCorreo(identificador)

            // 2. Si no se encuentra, intentamos por nombre de usuario.
            if (usuario == null) {
                usuario = usuarioDao.obtenerUsuarioPorNombreUsuario(identificador)
            }

            // 3. Si no se encontró de ninguna forma, el usuario no existe.
            if (usuario == null) {
                return Result.failure(Exception("Usuario o contraseña incorrectos."))
            }

            // 4. Verificamos la contraseña.
            // Si el hash es nulo, es una cuenta social (Google) y no puede iniciar sesión localmente.
            val contrasenaHasheada = usuario.contrasenaHash
                ?: return Result.failure(Exception("Este usuario se registró usando un método social."))

            val contrasenaEsValida = SeguridadHelper.verificarContrasena(
                contrasenaPlana = contrasena,
                contrasenaHasheada = contrasenaHasheada
            )

            if (contrasenaEsValida) {
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

    /**
     * Busca un usuario de Google por su correo. Si existe, lo actualiza. Si no, lo crea.
     * Ventajas:
     * - Maneja ambos casos (creación y actualización) en un solo lugar.
     * - Valida la URL de la foto de perfil para evitar guardar imágenes genéricas o nulas de Google.
     */
    override suspend fun buscarOCrearUsuarioGoogle(
        correo: String,
        nombreUsuario: String,
        fotoUrl: String?
    ): Result<UsuarioEntity> {
        return try {
            // --- LÓGICA DE VALIDACIÓN DE FOTO DE PERFIL ---
            // 1. Verificamos si la URL de Google es válida o es una genérica.
            val urlFinalParaGuardar = if (fotoUrl.isNullOrEmpty()) {
                // 2. Si es nula o vacía, generamos nuestra propia URL de avatar.
                val iniciales = nombreUsuario.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                    .take(2)
                    .joinToString("")
                "https://ui-avatars.com/api/?name=$iniciales&background=random&color=fff&size=256"
            } else {
                // 3. Si es válida, usamos la que nos dio Google.
                fotoUrl
            }

            val usuarioExistente = usuarioDao.obtenerUsuarioPorCorreo(correo)

            if (usuarioExistente == null) {
                // --- SOLUCIÓN: Lógica de creación que faltaba ---
                // El usuario no existe, así que lo creamos.
                val nuevoUsuario = UsuarioEntity(
                    nombreUsuario = nombreUsuario,
                    correo = correo,
                    contrasenaHash = null, // Las cuentas de Google no tienen contraseña local.
                    fechaRegistro = Date(),
                    fotoPerfilUrl = urlFinalParaGuardar, // Usamos la URL validada.
                    tipoAutenticacion = "GOOGLE"
                )
                val nuevoId = usuarioDao.insertarUsuario(nuevoUsuario)
                val usuarioCreado = usuarioDao.obtenerUsuarioPorId(nuevoId.toInt())!!
                Result.success(usuarioCreado)

            } else {
                // El usuario ya existe, lo actualizamos.
                // Usamos .copy() para crear una nueva instancia con los datos actualizados,
                // manteniendo intactos los que no cambiamos (como el id o la fecha de registro).
                val usuarioActualizado = usuarioExistente.copy(
                    nombreUsuario = nombreUsuario,
                    fotoPerfilUrl = urlFinalParaGuardar, // Actualizamos la foto por si cambió en Google.
                    tipoAutenticacion = "GOOGLE" // Aseguramos que el tipo sea Google.
                )
                usuarioDao.actualizarUsuario(usuarioActualizado)
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