// en: app/src/main/java/com/example/freeplayerm/ui/features/nav/Routes.kt
package com.example.freeplayerm.ui.features.nav

sealed class Routes(val ruta: String) {
   
   // Autenticación
   data object Login : Routes("login")
   data object Registro : Routes("registro")
   data object RecuperarClave : Routes("recuperar_clave")
   
   // Biblioteca
   data object Biblioteca : Routes("biblioteca/{usuarioId}") {
      fun crearRuta(usuarioId: Int): String = "biblioteca/$usuarioId"
   }
   
   // Perfil
   data object Perfil : Routes("perfil")
   data object EditarPerfil : Routes("editar_perfil")
   
   // Configuraciones
   data object Configuraciones : Routes("configuraciones")
}