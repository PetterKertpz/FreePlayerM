
package com.example.freeplayerm.core.security

import at.favre.lib.crypto.bcrypt.BCrypt


object SeguridadHelper {

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
}