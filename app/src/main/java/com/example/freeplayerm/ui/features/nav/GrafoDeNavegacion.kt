// app/src/main/java/com/example/freeplayerm/ui/features/nav/GrafoDeNavegacion.kt
package com.example.freeplayerm.ui.features.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.freeplayerm.ui.features.biblioteca.Biblioteca
import com.example.freeplayerm.ui.features.login.PantallaLogin
import com.example.freeplayerm.ui.features.login.PantallaRegistro
import com.example.freeplayerm.ui.features.splash.PantallaDeCarga

@Composable
fun GrafoDeNavegacion(
    navController: NavHostController,
    rutaDeInicio: String
) {
    NavHost(
        navController = navController,
        startDestination = rutaDeInicio // Usamos la ruta de inicio que nos pasan
    ) {
        // La pantalla de carga ya no está en el grafo principal si se decide en MainActivity
        // La dejamos por si la necesitas para otra cosa, pero la ruta de inicio ya la controla.
        composable(Rutas.PantallaDeCarga.ruta) {
            PantallaDeCarga(navController)
        }
        composable(Rutas.Login.ruta) {
            PantallaLogin(navController)
        }
        composable(Rutas.Registro.ruta) {
            PantallaRegistro(navController)
        }
        composable(
            route = Rutas.Biblioteca.ruta,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: -1
            Biblioteca(usuarioId = usuarioId)
        }
    }
}