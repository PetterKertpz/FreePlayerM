// app/src/main/java/com/example/freeplayerm/ui/navigation/Rutas.kt

package com.example.freeplayerm.ui.features.nav

/**
 * Define las rutas únicas para cada pantalla (Composable) en la aplicación.
 * Usamos el objeto de cada ruta (ej. Rutas.Login.ruta) como el identificador en el grafo de navegación.
 */
sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object Registro : Rutas("registro")
    object Biblioteca : Rutas("biblioteca/{usuarioId}")
        fun crearRuta(usuarioId: Int) = "biblioteca/$usuarioId"
    object PantallaDeCarga : Rutas("pantalla_de_carga")
}