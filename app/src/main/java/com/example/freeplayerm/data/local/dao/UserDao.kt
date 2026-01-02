// en: app/src/main/java/com/example/freeplayerm/data/local/dao/UserDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * ⚡ USUARIO DAO - MEJORADO Y SEGURO v2.0
 *
 * Manejo completo de usuarios con:
 * - Autenticación y validación
 * - Gestión de sesiones
 * - Búsqueda y filtros
 * - Queries optimizadas
 * - Operaciones de seguridad
 *
 * @author Android Data Layer Manager
 * @version 2.0 - Enhanced & Secure
 */
@Dao
interface UserDao {

    // ==================== OPERACIONES BÁSICAS ====================

    /**
     * Inserta un nuevo usuario Falla si ya existe un usuario con el mismo correo o nombre de
     * usuario
     *
     * @return ID del usuario insertado, o -1 si falla
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UserEntity): Long

    /** Inserta múltiples usuarios Útil para importación de datos o testing */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuarios(usuarios: List<UserEntity>): List<Long>

    /** Actualiza los datos de un usuario existente */
    @Update suspend fun actualizarUsuario(usuario: UserEntity): Int

    /** Elimina un usuario ⚠️ Considera las implicaciones en favoritos, listas, etc. */
    @Delete suspend fun eliminarUsuario(usuario: UserEntity): Int

    /** Elimina un usuario por ID */
    @Query("DELETE FROM usuarios WHERE id_usuario = :id")
    suspend fun eliminarUsuarioPorId(id: Int): Int

    // ==================== OBTENER USUARIOS ====================

    /** Obtiene todos los usuarios (suspending) Útil para administración */
    @Query("SELECT * FROM usuarios ORDER BY nombre_usuario ASC")
    suspend fun obtenerTodosLosUsuarios(): List<UserEntity>

    /** Obtiene todos los usuarios (Flow reactivo) */
    @Query("SELECT * FROM usuarios ORDER BY nombre_usuario ASC")
    fun obtenerTodosLosUsuariosFlow(): Flow<List<UserEntity>>

    /** Obtiene un usuario por correo electrónico Útil para login y validación */
    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UserEntity?

    /** Obtiene un usuario por nombre de usuario */
    @Query("SELECT * FROM usuarios WHERE nombre_usuario = :nombreUsuario COLLATE NOCASE LIMIT 1")
    suspend fun obtenerUsuarioPorNombreUsuario(nombreUsuario: String): UserEntity?

    /** Obtiene un usuario por ID (suspending) */
    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: Int): UserEntity?

    /** Obtiene un usuario por ID (Flow reactivo) Permite observar cambios en tiempo real */
    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun obtenerUsuarioPorIdFlow(id: Int): Flow<UserEntity?>

    // ==================== AUTENTICACIÓN Y VALIDACIÓN ====================

    /**
     * Verifica las credenciales de un usuario
     *
     * @return UserEntity si las credenciales son correctas, null si no
     */
    @Deprecated("SEGURIDAD: No comparar hashes en SQL. Usar obtenerUsuarioParaAutenticar()")
    suspend fun autenticarUsuario(identificador: String, contrasenia: String): UserEntity? = null

    /** Verifica si existe un usuario con el correo especificado */
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE correo = :correo COLLATE NOCASE)")
    suspend fun existeCorreo(correo: String): Boolean

    /** Verifica si existe un usuario con el nombre de usuario especificado */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM usuarios WHERE nombre_usuario = :nombreUsuario COLLATE NOCASE)"
    )
    suspend fun existeNombreUsuario(nombreUsuario: String): Boolean

    /** Verifica si un usuario está activo */
    @Query("SELECT activo FROM usuarios WHERE id_usuario = :id")
    suspend fun usuarioEstaActivo(id: Int): Boolean?

    // ==================== GESTIÓN DE SESIÓN ====================

    /** Actualiza la fecha de última sesión de un usuario */
    @Query(
        """
    UPDATE usuarios 
    SET ultima_sesion = :timestamp 
    WHERE id_usuario = :id
    """
    )
    suspend fun actualizarUltimaSesion(id: Int, timestamp: Long = System.currentTimeMillis()): Int

    /** Obtiene usuarios activos recientemente */
    @Query(
        """
        SELECT * FROM usuarios 
        WHERE ultima_sesion > :timestamp 
        ORDER BY ultima_sesion DESC
    """
    )
    suspend fun obtenerUsuariosActivosRecientes(
        timestamp: Int = System.currentTimeMillis().toInt() - (7 * 24 * 60 * 60 * 1000) // 7 días
    ): List<UserEntity>

    // ==================== GESTIÓN DE CONTRASEÑA ====================

    /** Actualiza la contraseña de un usuario */
    @Query(
        """
        UPDATE usuarios 
        SET contrasenia_hash = :nuevaContrasenia 
        WHERE id_usuario = :id
    """
    )
    suspend fun actualizarContrasenia(id: Int, nuevaContrasenia: String): Int

    /** Valida la contraseña actual de un usuario */
    /** ⚠️ DEPRECADO: No comparar hashes en SQL. Usar UserRepository para verificar con BCrypt. */
    @Deprecated(
        message = "BCrypt hashes no se pueden comparar en SQL. Usar UserRepository.",
        level = DeprecationLevel.ERROR,
    )
    suspend fun validarContrasenia(id: Int, contrasenia: String): Boolean = false

    // ==================== GESTIÓN DE PERFIL ====================

    /** Actualiza solo el nombre de usuario */
    @Query(
        """
        UPDATE usuarios 
        SET nombre_usuario = :nuevoNombre 
        WHERE id_usuario = :id
    """
    )
    suspend fun actualizarNombreUsuario(id: Int, nuevoNombre: String): Int

    /** Actualiza solo el correo electrónico */
    @Query(
        """
        UPDATE usuarios 
        SET correo = :nuevoCorreo 
        WHERE id_usuario = :id
    """
    )
    suspend fun actualizarCorreo(id: Int, nuevoCorreo: String): Int

    /** Actualiza la foto de perfil */
    @Query(
        """
        UPDATE usuarios 
        SET foto_perfil = :rutaFoto 
        WHERE id_usuario = :id
    """
    )
    suspend fun actualizarFotoPerfil(id: Int, rutaFoto: String?): Int

    /** Actualiza el estado activo del usuario */
    @Query(
        """
        UPDATE usuarios 
        SET activo = :activo 
        WHERE id_usuario = :id
    """
    )
    suspend fun actualizarEstadoActivo(id: Int, activo: Boolean): Int

    // ==================== BÚSQUEDA Y FILTROS ====================

    /** Busca usuarios por nombre o correo */
    @Query(
        """
        SELECT * FROM usuarios 
        WHERE nombre_usuario LIKE '%' || :query || '%' 
           OR correo LIKE '%' || :query || '%' 
        COLLATE NOCASE
        ORDER BY nombre_usuario ASC
        LIMIT :limit
    """
    )
    suspend fun buscarUsuarios(query: String, limit: Int = 50): List<UserEntity>

    /** Obtiene solo usuarios activos */
    @Query("SELECT * FROM usuarios WHERE activo = 1 ORDER BY nombre_usuario ASC")
    fun obtenerUsuariosActivos(): Flow<List<UserEntity>>

    /** Obtiene solo usuarios inactivos */
    @Query("SELECT * FROM usuarios WHERE activo = 0 ORDER BY nombre_usuario ASC")
    fun obtenerUsuariosInactivos(): Flow<List<UserEntity>>

    // ==================== ESTADÍSTICAS ====================

    /** Cuenta el total de usuarios */
    @Query("SELECT COUNT(*) FROM usuarios") suspend fun contarUsuarios(): Int

    /** Cuenta usuarios activos */
    @Query("SELECT COUNT(*) FROM usuarios WHERE activo = 1")
    suspend fun contarUsuariosActivos(): Int

    /** Cuenta usuarios inactivos */
    @Query("SELECT COUNT(*) FROM usuarios WHERE activo = 0")
    suspend fun contarUsuariosInactivos(): Int

    /** Obtiene la fecha de creación del usuario más antiguo */
    @Query("SELECT MIN(fecha_creacion) FROM usuarios")
    suspend fun obtenerFechaUsuarioMasAntiguo(): Int?

    /** Obtiene la fecha de creación del usuario más reciente */
    @Query("SELECT MAX(fecha_creacion) FROM usuarios")
    suspend fun obtenerFechaUsuarioMasReciente(): Int?

    // ==================== OPERACIONES AVANZADAS ====================

    /**
     * Registra un nuevo usuario con validación Verifica que no existan duplicados antes de insertar
     */
    @Transaction
    suspend fun registrarUsuario(usuario: UserEntity): Long { // Cambiar a Long
        // Verificar si el correo ya existe
        if (existeCorreo(usuario.correo)) {
            return -1 // Error: correo ya registrado
        }

        // Verificar si el nombre de usuario ya existe
        if (existeNombreUsuario(usuario.nombreUsuario)) {
            return -2 // Error: nombre de usuario ya registrado
        }

        // Insertar el usuario
        return insertarUsuario(usuario)
    }

    /** Login con actualización de última sesión */
    /**
     * ⚠️ DEPRECADO: Usar UserRepository.login() en su lugar. La verificación BCrypt no puede
     * hacerse en SQL/DAO.
     */
    @Deprecated(
        message = "Usar UserRepository.login() que verifica BCrypt correctamente",
        level = DeprecationLevel.WARNING,
    )
    @Transaction
    suspend fun loginUsuario(identificador: String, contrasenia: String): UserEntity? {
        // Esta función ya no debe usarse - la autenticación BCrypt
        // debe hacerse en UserRepository, no en el DAO
        return null
    }

    /** Cambiar contraseña con validación */
    /** ⚠️ DEPRECADO: Usar UserRepository.cambiarContrasenia() en su lugar. */
    @Deprecated(
        message = "Usar UserRepository.cambiarContrasenia() que verifica BCrypt correctamente",
        level = DeprecationLevel.WARNING,
    )
    @Transaction
    suspend fun cambiarContrasenia(
        id: Int,
        contraseniaActual: String,
        nuevaContrasenia: String,
    ): Boolean = false

    /** Desactivar usuario (soft delete) Mejor que eliminar para mantener integridad referencial */
    @Transaction
    suspend fun desactivarUsuario(id: Int): Boolean {
        return actualizarEstadoActivo(id, activo = false) > 0
    }

    /** Reactivar usuario */
    @Transaction
    suspend fun reactivarUsuario(id: Int): Boolean {
        return actualizarEstadoActivo(id, activo = true) > 0
    }

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    /** Elimina usuarios inactivos por más de X días */
    @Query(
        """
        DELETE FROM usuarios 
        WHERE activo = 0 
        AND ultima_sesion < :timestamp
    """
    )
    suspend fun limpiarUsuariosInactivosAntiguos(
        timestamp: Int = System.currentTimeMillis().toInt() - (90 * 24 * 60 * 60 * 1000) // 90 días
    ): Int

    /** Resetea las sesiones de todos los usuarios Útil para mantenimiento o seguridad */
    @Query("UPDATE usuarios SET ultima_sesion = NULL") suspend fun resetearTodasLasSesiones(): Int

    /** Elimina TODOS los usuarios ⚠️ USAR CON EXTREMA PRECAUCIÓN - Solo para testing/reset */
    @Query("DELETE FROM usuarios") suspend fun eliminarTodosLosUsuarios(): Int

    // ==================== GESTIÓN DE TOKENS (NUEVO) ====================

    /** Actualiza los tokens de sesión de un usuario */
    @Query(
        """
    UPDATE usuarios 
    SET token_sesion = :token,
        refresh_token = :refreshToken,
        token_expiracion = :expiracion,
        ultima_sesion = :timestamp
    WHERE id_usuario = :id
    """
    )
    suspend fun actualizarTokens(
        id: Int,
        token: String,
        refreshToken: String,
        expiracion: Long,
        timestamp: Long = System.currentTimeMillis(),
    ): Int

    /** Obtiene un usuario por token de sesión */
    @Query("SELECT * FROM usuarios WHERE token_sesion = :token LIMIT 1")
    suspend fun obtenerUsuarioPorToken(token: String): UserEntity?

    /** Invalida el token de sesión de un usuario (logout) */
    @Query(
        """
        UPDATE usuarios 
        SET token_sesion = NULL,
            refresh_token = NULL,
            token_expiracion = NULL
        WHERE id_usuario = :id
    """
    )
    suspend fun invalidarTokens(id: Int): Int

    /** Verifica si un token es válido */
    @Query(
        """
    SELECT EXISTS(
        SELECT 1 FROM usuarios 
        WHERE token_sesion = :token 
        AND token_expiracion > :ahora
        AND activo = 1
        )
    """
    )
    suspend fun tokenEsValido(token: String, ahora: Long = System.currentTimeMillis()): Boolean

    /** Actualiza el timestamp de último cambio de contraseña */
    @Query(
        """
    UPDATE usuarios 
    SET ultimo_cambio_contrasena = :timestamp 
    WHERE id_usuario = :id
    """
    )
    suspend fun actualizarUltimoCambioContrasena(
        id: Int,
        timestamp: Long = System.currentTimeMillis(),
    ): Int

    /**
     * Obtiene usuario para autenticar (sin comparar hash en SQL) REEMPLAZA al método
     * autenticarUsuario anterior
     */
    @Query(
        """
        SELECT * FROM usuarios 
        WHERE (correo = :identificador OR nombre_usuario = :identificador)
        AND activo = 1
        COLLATE NOCASE 
        LIMIT 1
    """
    )
    suspend fun obtenerUsuarioParaAutenticar(identificador: String): UserEntity?

    /**
     * Invalida todos los tokens de un usuario Útil para "cerrar sesión en todos los dispositivos"
     */
    @Query(
        """
        UPDATE usuarios 
        SET token_sesion = NULL,
            refresh_token = NULL,
            token_expiracion = NULL
        WHERE id_usuario = :id
    """
    )
    suspend fun cerrarTodasLasSesiones(id: Int): Int

    /** Obtiene usuarios con tokens expirados */
    @Query(
        """
    SELECT * FROM usuarios 
    WHERE token_expiracion < :ahora
    AND token_expiracion IS NOT NULL
    """
    )
    suspend fun obtenerUsuariosConTokensExpirados(
        ahora: Long = System.currentTimeMillis()
    ): List<UserEntity>

    /** Limpia tokens expirados de todos los usuarios */
    @Query(
        """
    UPDATE usuarios 
    SET token_sesion = NULL,
        refresh_token = NULL,
        token_expiracion = NULL
    WHERE token_expiracion < :ahora
    AND token_expiracion IS NOT NULL
    """
    )
    suspend fun limpiarTokensExpirados(ahora: Long = System.currentTimeMillis()): Int
}
