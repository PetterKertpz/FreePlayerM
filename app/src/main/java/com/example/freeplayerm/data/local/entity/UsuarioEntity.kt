// en: app/src/main/java/com/example/freeplayerm/data/local/entity/UsuarioEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 👤 USUARIO ENTITY - OPTIMIZADA Y SEGURA v2.0
 *
 * Entidad que representa un usuario en el sistema
 * Incluye autenticación, perfil y gestión de sesiones
 *
 * Características:
 * - Índices únicos para correo y nombre de usuario
 * - Soporte para múltiples tipos de autenticación
 * - Soft delete mediante campo "activo"
 * - Gestión de sesiones con última sesión
 * - Foto de perfil opcional
 *
 * @version 2.0 - Enhanced & Fixed
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["nombre_usuario"], unique = true),
        Index(value = ["correo"], unique = true),
        Index(value = ["ultima_sesion"]),
        Index(value = ["activo"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int = 0,

    // ==================== INFORMACIÓN DE CUENTA ====================

    @ColumnInfo(name = "nombre_usuario")
    val nombreUsuario: String,

    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "contrasenia_hash")
    val contraseniaHash: String, // Hash de la contraseña (usar BCrypt o similar)

    // ==================== PERFIL ====================

    @ColumnInfo(name = "nombre_completo")
    val nombreCompleto: String? = null,

    @ColumnInfo(name = "foto_perfil")
    val fotoPerfil: String? = null, // Path o URL de la foto de perfil

    @ColumnInfo(name = "biografia")
    val biografia: String? = null,

    @ColumnInfo(name = "fecha_nacimiento")
    val fechaNacimiento: Long? = null, // Timestamp de fecha de nacimiento

    // ==================== AUTENTICACIÓN ====================

    @ColumnInfo(name = "tipo_autenticacion")
    val tipoAutenticacion: String = TIPO_LOCAL, // LOCAL, GOOGLE, FACEBOOK, etc.

    @ColumnInfo(name = "provider_id")
    val providerId: String? = null, // ID del proveedor externo (Google, Facebook, etc.)

    // ==================== ESTADO Y SESIONES ====================

    @ColumnInfo(name = "activo")
    val activo: Boolean = true, // Si el usuario está activo (soft delete)

    @ColumnInfo(name = "ultima_sesion")
    val ultimaSesion: Long? = null, // Timestamp de última sesión

    // ==================== TOKENS DE SESIÓN ====================

    @ColumnInfo(name = "token_sesion")
    val tokenSesion: String? = null,

    @ColumnInfo(name = "refresh_token")
    val refreshToken: String? = null,

    @ColumnInfo(name = "token_expiracion")
    val tokenExpiracion: Long? = null,

    @ColumnInfo(name = "salt")
    val salt: String? = null, // Salt adicional si se usa otro algoritmo

    @ColumnInfo(name = "ultimo_cambio_contrasena")
    val ultimoCambioContrasena: Long? = null,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(), // Timestamp de cuándo se creó

    // ==================== PREFERENCIAS ====================

    @ColumnInfo(name = "tema_oscuro")
    val temaOscuro: Boolean = false,

    @ColumnInfo(name = "notificaciones_habilitadas")
    val notificacionesHabilitadas: Boolean = true,

    @ColumnInfo(name = "reproduccion_automatica")
    val reproduccionAutomatica: Boolean = true,

    @ColumnInfo(name = "calidad_preferida")
    val calidadPreferida: String = CALIDAD_ALTA, // LOW, MEDIUM, HIGH

    @ColumnInfo(name = "idioma_preferido")
    val idiomaPreferido: String = "es", // es, en, fr, etc.

    // ==================== ESTADÍSTICAS ====================

    @ColumnInfo(name = "total_reproducciones")
    val totalReproducciones: Int = 0,

    @ColumnInfo(name = "total_favoritos")
    val totalFavoritos: Int = 0,

    @ColumnInfo(name = "total_listas")
    val totalListas: Int = 0
) {
    /**
     * Verifica si el usuario está activo
     */
    fun estaActivo(): Boolean = activo

    /**
     * Verifica si usa autenticación local
     */
    fun esLocal(): Boolean = tipoAutenticacion == TIPO_LOCAL

    /**
     * Verifica si usa autenticación de terceros
     */
    fun esOAuth(): Boolean = tipoAutenticacion != TIPO_LOCAL

    /**
     * Obtiene el nombre para mostrar (prioriza nombre completo, si no usa nombre de usuario)
     */
    fun nombreParaMostrar(): String = nombreCompleto?.takeIf { it.isNotBlank() } ?: nombreUsuario

    /**
     * Verifica si tiene foto de perfil
     */
    fun tieneFotoPerfil(): Boolean = !fotoPerfil.isNullOrBlank()

    /**
     * Calcula tiempo desde última sesión en días
     */
    fun diasDesdeUltimaSesion(): Int? {
        return ultimaSesion?.let {
            val diff = System.currentTimeMillis() - it
            (diff / (1000 * 60 * 60 * 24)).toInt()
        }
    }

    companion object {
        // Tipos de autenticación
        const val TIPO_LOCAL = "LOCAL"
        const val TIPO_GOOGLE = "GOOGLE"
        const val TIPO_FACEBOOK = "FACEBOOK"
        const val TIPO_TWITTER = "TWITTER"
        const val TIPO_APPLE = "APPLE"

        // Calidades de audio preferidas
        const val CALIDAD_BAJA = "LOW"
        const val CALIDAD_MEDIA = "MEDIUM"
        const val CALIDAD_ALTA = "HIGH"
        const val CALIDAD_LOSSLESS = "LOSSLESS"

    }
}