// app/src/main/java/com/example/freeplayerm/ui/navigation/Routes.kt

package com.example.freeplayerm.ui.features.nav

/**
 * Define las rutas únicas para cada pantalla (Composable) en la aplicación. Usamos el objeto de
 * cada ruta (ej. Routes.Login.ruta) como el identificador en el grafo de navegación.
 */
sealed class Routes(val ruta: String) {
    object Login : Routes("login")

    object Registro : Routes("registro")

    object RecuperarClave : Routes("recuperar_clave")

    object Biblioteca : Routes("biblioteca/{usuarioId}")

    fun crearRuta(usuarioId: Int) = "biblioteca/$usuarioId"
}
