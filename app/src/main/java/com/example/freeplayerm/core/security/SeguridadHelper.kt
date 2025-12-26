// en: app/src/main/java/com/example/freeplayerm/core/security/SeguridadHelper.kt
package com.example.freeplayerm.core.security

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

/**
 * 🔐 SEGURIDAD HELPER - MEJORADO v2.0
 *
 * Utilidades de seguridad para:
 * - Hashing de contraseñas con BCrypt
 * - Generación de tokens de sesión
 * - Validación de tokens
 * - Generación de salts y UUIDs
 *
 * @version 2.0 - Enhanced with Token Management
 */
object SeguridadHelper {

    // ==================== CONTRASEÑAS ====================

    /**
     * Toma una contraseña en texto plano y la convierte en un hash seguro.
     * @param contrasenaPlana La contraseña que el usuario ingresó.
     * @return Un String que representa el hash de la contraseña. Este es el valor que guardaremos en la base de datos.
     */
    fun hashContrasena(contrasenaPlana: String): String {
        // BCrypt.withDefaults() utiliza la configuración estándar y recomendada.
        // hashToString() genera el hash. El "costo" (12) es un factor de trabajo que
        // determina qué tan difícil (y lento) es calcular el hash. Un valor más alto
        // es más seguro pero más lento. 12 es un excelente punto de partida.
        return BCrypt.withDefaults().hashToString(12, contrasenaPlana.toCharArray())
    }

    /**
     * Compara una contraseña en texto plano con un hash guardado.
     * @param contrasenaPlana La contraseña que el usuario ingresa en el login.
     * @param contrasenaHasheada El hash que recuperamos de la base de datos.
     * @return `true` si la contraseña coincide con el hash, `false` en caso contrario.
     */
    fun verificarContrasena(contrasenaPlana: String, contrasenaHasheada: String): Boolean {
        // BCrypt se encarga de extraer la "sal" (salt) y los parámetros del hash
        // y realizar la comparación de forma segura.
        val resultado = BCrypt.verifyer().verify(contrasenaPlana.toCharArray(), contrasenaHasheada)
        return resultado.verified
    }

    // ==================== TOKENS DE SESIÓN ====================

    /**
     * Genera un token de sesión único
     *
     * Formato: session_[usuarioId]_[timestamp]_[uuid]
     *
     * En producción, considera usar JWT (JSON Web Tokens) para tokens más seguros
     * que pueden contener información firmada.
     *
     * @param usuarioId ID del usuario
     * @return Token único de sesión
     */
    fun generarTokenSesion(usuarioId: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(48) // ✅ 384 bits de entropía
        random.nextBytes(bytes)
        val tokenBase = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        // ✅ Hash rápido del userId (no BCrypt) solo para ofuscación
        val userSalt = (usuarioId * 31 + System.currentTimeMillis()).toString()
            .hashCode().toString(16).padStart(8, '0')

        return "session_${userSalt}_${tokenBase}"
    }

    /**
     * Genera un refresh token único
     *
     * Los refresh tokens suelen ser más largos y tienen vida más larga
     * Se usan para obtener nuevos access tokens sin re-autenticación
     *
     * @param usuarioId ID del usuario
     * @return Refresh token único
     */
    fun generarRefreshToken(usuarioId: Int): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        return "refresh_${usuarioId}_${timestamp}_${uuid}"
    }

    /**
     * Valida un refresh token y extrae el ID del usuario
     *
     * @param refreshToken Refresh token a validar
     * @return ID del usuario si es válido, null si no
     */
    fun validarRefreshToken(refreshToken: String): Int? {
        return try {
            val parts = refreshToken.split("_")
            if (parts.size >= 2 && parts[0] == "refresh") {
                parts[1].toIntOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica si un token ha expirado
     *
     * @param tokenExpiracion Timestamp de expiración (milisegundos)
     * @return true si el token expiró
     */
    fun tokenExpirado(tokenExpiracion: Long): Boolean {
        return System.currentTimeMillis() > tokenExpiracion
    }

    /**
     * Calcula el timestamp de expiración para un token
     *
     * @param duracionHoras Duración en horas (default: 24)
     * @return Timestamp de expiración
     */
    fun calcularExpiracion(duracionHoras: Int = 24): Long {
        val duracionMs = duracionHoras * 60 * 60 * 1000L
        return System.currentTimeMillis() + duracionMs
    }

    // ==================== SALTS Y UUIDS ====================

    /**
     * Genera un salt único
     *
     * Útil si decides usar un algoritmo diferente a BCrypt
     * (BCrypt genera salt automáticamente)
     *
     * @return Salt único en formato UUID
     */
    fun generarSalt(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Genera un UUID único
     *
     * Útil para IDs de sincronización, dispositivos, etc.
     *
     * @return UUID único
     */
    fun generarUUID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Genera un UUID corto (sin guiones)
     *
     * @return UUID corto
     */
    fun generarUUIDCorto(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida la fortaleza de una contraseña
     *
     * Criterios:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     * - Al menos un carácter especial (opcional)
     *
     * @param contrasena Contraseña a validar
     * @param requerirEspeciales Si requiere caracteres especiales
     * @return Resultado de validación con mensaje
     */
    fun validarFortalezaContrasena(
        contrasena: String,
        requerirEspeciales: Boolean = false
    ): ResultadoValidacion {
        if (contrasena.length < 8) {
            return ResultadoValidacion(false, "La contraseña debe tener al menos 8 caracteres")
        }

        if (!contrasena.any { it.isUpperCase() }) {
            return ResultadoValidacion(false, "La contraseña debe contener al menos una mayúscula")
        }

        if (!contrasena.any { it.isLowerCase() }) {
            return ResultadoValidacion(false, "La contraseña debe contener al menos una minúscula")
        }

        if (!contrasena.any { it.isDigit() }) {
            return ResultadoValidacion(false, "La contraseña debe contener al menos un número")
        }

        if (requerirEspeciales) {
            val caracteresEspeciales = "!@#$%^&*()_+-=[]{}|;:,.<>?"
            if (!contrasena.any { it in caracteresEspeciales }) {
                return ResultadoValidacion(
                    false,
                    "La contraseña debe contener al menos un carácter especial"
                )
            }
        }

        return ResultadoValidacion(true, "Contraseña válida")
    }

    /**
     * Calcula el nivel de fortaleza de una contraseña (0-100)
     *
     * @param contrasena Contraseña a evaluar
     * @return Score de 0 a 100
     */
    fun calcularFortalezaContrasena(contrasena: String): Int {
        var score = 0

        // Longitud
        score += when {
            contrasena.length >= 12 -> 30
            contrasena.length >= 10 -> 20
            contrasena.length >= 8 -> 10
            else -> 0
        }

        // Mayúsculas
        if (contrasena.any { it.isUpperCase() }) score += 15

        // Minúsculas
        if (contrasena.any { it.isLowerCase() }) score += 15

        // Números
        if (contrasena.any { it.isDigit() }) score += 15

        // Caracteres especiales
        val caracteresEspeciales = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (contrasena.any { it in caracteresEspeciales }) score += 25

        return score.coerceIn(0, 100)
    }

    /**
     * Obtiene el texto descriptivo de fortaleza
     *
     * @param score Score de fortaleza (0-100)
     * @return Texto descriptivo
     */
    fun obtenerTextoFortaleza(score: Int): String {
        return when {
            score >= 80 -> "Muy fuerte"
            score >= 60 -> "Fuerte"
            score >= 40 -> "Media"
            score >= 20 -> "Débil"
            else -> "Muy débil"
        }
    }

    // ==================== SANITIZACIÓN ====================

    /**
     * Sanitiza un string para prevenir inyección SQL
     * (Room ya protege contra esto, pero útil para casos especiales)
     *
     * @param input String a sanitizar
     * @return String sanitizado
     */
    fun sanitizarInput(input: String): String {
        return input
            .replace("'", "''")
            .replace("--", "")
            .replace(";", "")
            .replace("/*", "")
            .replace("*/", "")
            .trim()
    }

    /**
     * Valida que un email tenga formato correcto
     *
     * @param email Email a validar
     * @return true si es válido
     */
    fun validarEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    // ==================== DATA CLASSES ====================

    /**
     * Resultado de validación con mensaje
     */
    data class ResultadoValidacion(
        val valido: Boolean,
        val mensaje: String
    )

    // ==================== CONSTANTES ====================

    object Constantes {
        const val DURACION_TOKEN_HORAS = 24
        const val DURACION_REFRESH_TOKEN_DIAS = 30
        const val LONGITUD_MINIMA_CONTRASENA = 8
        const val COSTO_BCRYPT = 12
    }
}