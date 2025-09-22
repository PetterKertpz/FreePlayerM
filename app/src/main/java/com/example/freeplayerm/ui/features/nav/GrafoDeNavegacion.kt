// en: app/src/main/java/com/example/freeplayerm/ui/features/nav/GrafoDeNavegacion.kt
package com.example.freeplayerm.ui.features.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.freeplayerm.ui.features.biblioteca.Biblioteca
import com.example.freeplayerm.ui.features.login.PantallaLogin
import com.example.freeplayerm.ui.features.login.PantallaRecuperarClave
import com.example.freeplayerm.ui.features.login.PantallaRegistro
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel


// --- CAMBIO CLAVE AQUÍ ---
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GrafoDeNavegacion(
    navController: NavHostController,
    rutaDeInicio: String,
    reproductorViewModel: ReproductorViewModel // <-- Aceptamos el ViewModel compartido
) {
    NavHost(
        navController = navController,
        startDestination = rutaDeInicio
    ) {
        composable(Rutas.Login.ruta) {
            PantallaLogin(navController)
        }
        composable(Rutas.Registro.ruta) {
            PantallaRegistro(navController)
        }
        composable(Rutas.RecuperarClave.ruta) {
            PantallaRecuperarClave(navController)
        }
        composable(
            route = Rutas.Biblioteca.ruta,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: -1
            // Se lo pasamos a la pantalla Biblioteca
            Biblioteca(
                usuarioId = usuarioId,
                reproductorViewModel = reproductorViewModel // <-- ¡NUEVO!
            )
        }
    }
}