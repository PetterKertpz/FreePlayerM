// en: app/src/main/java/com/example/freeplayerm/data/local/dao/UsuarioDao.kt
package com.example.freeplayerm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeplayerm.data.local.entity.UsuarioEntity
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
interface UsuarioDao {

    // ==================== OPERACIONES BÁSICAS ====================

    /**
     * Inserta un nuevo usuario
     * Falla si ya existe un usuario con el mismo correo o nombre de usuario
     * @return ID del usuario insertado, o -1 si falla
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    /**
     * Inserta múltiples usuarios
     * Útil para importación de datos o testing
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>): List<Long>

    /**
     * Actualiza los datos de un usuario existente
     */
    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity): Int

    /**
     * Elimina un usuario
     * ⚠️ Considera las implicaciones en favoritos, listas, etc.
     */
    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity): Int

    /**
     * Elimina un usuario por ID
     */
    @Query("DELETE FROM usuarios WHERE id_usuario = :id")
    suspend fun eliminarUsuarioPorId(id: Int): Int

    // ==================== OBTENER USUARIOS ====================

    /**
     * Obtiene todos los usuarios (suspending)
     * Útil para administración
     */
    @Query("SELECT * FROM usuarios ORDER BY nombre_usuario ASC")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>

    /**
     * Obtiene todos los usuarios (Flow reactivo)
     */
    @Query("SELECT * FROM usuarios ORDER BY nombre_usuario ASC")
    fun obtenerTodosLosUsuariosFlow(): Flow<List<UsuarioEntity>>

    /**
     * Obtiene un usuario por correo electrónico
     * Útil para login y validación
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    /**
     * Obtiene un usuario por nombre de usuario
     */
    @Query("SELECT * FROM usuarios WHERE nombre_usuario = :nombreUsuario COLLATE NOCASE LIMIT 1")
    suspend fun obtenerUsuarioPorNombreUsuario(nombreUsuario: String): UsuarioEntity?

    /**
     * Obtiene un usuario por ID (suspending)
     */
    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    /**
     * Obtiene un usuario por ID (Flow reactivo)
     * Permite observar cambios en tiempo real
     */
    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun obtenerUsuarioPorIdFlow(id: Int): Flow<UsuarioEntity?>

    // ==================== AUTENTICACIÓN Y VALIDACIÓN ====================

    /**
     * Verifica las credenciales de un usuario
     * @return UsuarioEntity si las credenciales son correctas, null si no
     */
    @Query("""
        SELECT * FROM usuarios 
        WHERE (correo = :identificador OR nombre_usuario = :identificador) 
        AND contrasenia = :contrasenia 
        COLLATE NOCASE 
        LIMIT 1
    """)
    suspend fun autenticarUsuario(identificador: String, contrasenia: String): UsuarioEntity?

    /**
     * Verifica si existe un usuario con el correo especificado
     */
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE correo = :correo COLLATE NOCASE)")
    suspend fun existeCorreo(correo: String): Boolean

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     */
    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE nombre_usuario = :nombreUsuario COLLATE NOCASE)")
    suspend fun existeNombreUsuario(nombreUsuario: String): Boolean

    /**
     * Verifica si un usuario está activo
     */
    @Query("SELECT activo FROM usuarios WHERE id_usuario = :id")
    suspend fun usuarioEstaActivo(id: Int): Boolean?

    // ==================== GESTIÓN DE SESIÓN ====================

    /**
     * Actualiza la fecha de última sesión de un usuario
     */
    @Query("""
        UPDATE usuarios 
        SET ultima_sesion = :timestamp 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarUltimaSesion(id: Int, timestamp: Long = System.currentTimeMillis()): Int

    /**
     * Obtiene usuarios activos recientemente
     */
    @Query("""
        SELECT * FROM usuarios 
        WHERE ultima_sesion > :timestamp 
        ORDER BY ultima_sesion DESC
    """)
    suspend fun obtenerUsuariosActivosRecientes(
        timestamp: Long = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) // 7 días
    ): List<UsuarioEntity>

    // ==================== GESTIÓN DE CONTRASEÑA ====================

    /**
     * Actualiza la contraseña de un usuario
     */
    @Query("""
        UPDATE usuarios 
        SET contrasenia = :nuevaContrasenia 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarContrasenia(id: Int, nuevaContrasenia: String): Int

    /**
     * Valida la contraseña actual de un usuario
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM usuarios 
            WHERE id_usuario = :id AND contrasenia = :contrasenia
        )
    """)
    suspend fun validarContrasenia(id: Int, contrasenia: String): Boolean

    // ==================== GESTIÓN DE PERFIL ====================

    /**
     * Actualiza solo el nombre de usuario
     */
    @Query("""
        UPDATE usuarios 
        SET nombre_usuario = :nuevoNombre 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarNombreUsuario(id: Int, nuevoNombre: String): Int

    /**
     * Actualiza solo el correo electrónico
     */
    @Query("""
        UPDATE usuarios 
        SET correo = :nuevoCorreo 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarCorreo(id: Int, nuevoCorreo: String): Int

    /**
     * Actualiza la foto de perfil
     */
    @Query("""
        UPDATE usuarios 
        SET foto_perfil = :rutaFoto 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarFotoPerfil(id: Int, rutaFoto: String?): Int

    /**
     * Actualiza el estado activo del usuario
     */
    @Query("""
        UPDATE usuarios 
        SET activo = :activo 
        WHERE id_usuario = :id
    """)
    suspend fun actualizarEstadoActivo(id: Int, activo: Boolean): Int

    // ==================== BÚSQUEDA Y FILTROS ====================

    /**
     * Busca usuarios por nombre o correo
     */
    @Query("""
        SELECT * FROM usuarios 
        WHERE nombre_usuario LIKE '%' || :query || '%' 
           OR correo LIKE '%' || :query || '%' 
        COLLATE NOCASE
        ORDER BY nombre_usuario ASC
        LIMIT :limit
    """)
    suspend fun buscarUsuarios(query: String, limit: Int = 50): List<UsuarioEntity>

    /**
     * Obtiene solo usuarios activos
     */
    @Query("SELECT * FROM usuarios WHERE activo = 1 ORDER BY nombre_usuario ASC")
    fun obtenerUsuariosActivos(): Flow<List<UsuarioEntity>>

    /**
     * Obtiene solo usuarios inactivos
     */
    @Query("SELECT * FROM usuarios WHERE activo = 0 ORDER BY nombre_usuario ASC")
    fun obtenerUsuariosInactivos(): Flow<List<UsuarioEntity>>

    // ==================== ESTADÍSTICAS ====================

    /**
     * Cuenta el total de usuarios
     */
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    /**
     * Cuenta usuarios activos
     */
    @Query("SELECT COUNT(*) FROM usuarios WHERE activo = 1")
    suspend fun contarUsuariosActivos(): Int

    /**
     * Cuenta usuarios inactivos
     */
    @Query("SELECT COUNT(*) FROM usuarios WHERE activo = 0")
    suspend fun contarUsuariosInactivos(): Int

    /**
     * Obtiene la fecha de creación del usuario más antiguo
     */
    @Query("SELECT MIN(fecha_creacion) FROM usuarios")
    suspend fun obtenerFechaUsuarioMasAntiguo(): Long?

    /**
     * Obtiene la fecha de creación del usuario más reciente
     */
    @Query("SELECT MAX(fecha_creacion) FROM usuarios")
    suspend fun obtenerFechaUsuarioMasReciente(): Long?

    // ==================== OPERACIONES AVANZADAS ====================

    /**
     * Registra un nuevo usuario con validación
     * Verifica que no existan duplicados antes de insertar
     */
    @Transaction
    suspend fun registrarUsuario(usuario: UsuarioEntity): Long {
        // Verificar si el correo ya existe
        if (existeCorreo(usuario.correo)) {
            return -1L // Error: correo ya registrado
        }

        // Verificar si el nombre de usuario ya existe
        if (existeNombreUsuario(usuario.nombreUsuario)) {
            return -2L // Error: nombre de usuario ya registrado
        }

        // Insertar el usuario
        return insertarUsuario(usuario)
    }

    /**
     * Login con actualización de última sesión
     */
    @Transaction
    suspend fun loginUsuario(identificador: String, contrasenia: String): UsuarioEntity? {
        val usuario = autenticarUsuario(identificador, contrasenia)

        if (usuario != null && usuario.activo) {
            actualizarUltimaSesion(usuario.idUsuario)
        }

        return usuario?.takeIf { it.activo }
    }

    /**
     * Cambiar contraseña con validación
     */
    @Transaction
    suspend fun cambiarContrasenia(
        id: Int,
        contraseniaActual: String,
        nuevaContrasenia: String
    ): Boolean {
        // Validar contraseña actual
        if (!validarContrasenia(id, contraseniaActual)) {
            return false
        }

        // Actualizar a nueva contraseña
        return actualizarContrasenia(id, nuevaContrasenia) > 0
    }

    /**
     * Desactivar usuario (soft delete)
     * Mejor que eliminar para mantener integridad referencial
     */
    @Transaction
    suspend fun desactivarUsuario(id: Int): Boolean {
        return actualizarEstadoActivo(id, activo = false) > 0
    }

    /**
     * Reactivar usuario
     */
    @Transaction
    suspend fun reactivarUsuario(id: Int): Boolean {
        return actualizarEstadoActivo(id, activo = true) > 0
    }

    // ==================== LIMPIEZA Y MANTENIMIENTO ====================

    /**
     * Elimina usuarios inactivos por más de X días
     */
    @Query("""
        DELETE FROM usuarios 
        WHERE activo = 0 
        AND ultima_sesion < :timestamp
    """)
    suspend fun limpiarUsuariosInactivosAntiguos(
        timestamp: Long = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000) // 90 días
    ): Int

    /**
     * Resetea las sesiones de todos los usuarios
     * Útil para mantenimiento o seguridad
     */
    @Query("UPDATE usuarios SET ultima_sesion = NULL")
    suspend fun resetearTodasLasSesiones(): Int

    /**
     * Elimina TODOS los usuarios
     * ⚠️ USAR CON EXTREMA PRECAUCIÓN - Solo para testing/reset
     */
    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodosLosUsuarios(): Int
}