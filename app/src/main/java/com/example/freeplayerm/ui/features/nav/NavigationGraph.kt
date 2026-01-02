// en: app/src/main/java/com/example/freeplayerm/ui/features/nav/NavigationGraph.kt
package com.example.freeplayerm.ui.features.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.freeplayerm.ui.features.auth.login.LoginScreen
import com.example.freeplayerm.ui.features.auth.recovery.PasswordRecoveryScreen
import com.example.freeplayerm.ui.features.auth.register.RegisterScreen
import com.example.freeplayerm.ui.features.library.LibraryScreen
import com.example.freeplayerm.ui.features.player.ReproductorViewModel

// --- CAMBIO CLAVE AQUÍ ---
@Composable
fun NavigationGraph(
    navController: NavHostController,
    rutaDeInicio: String,
    reproductorViewModel: ReproductorViewModel, // <-- Aceptamos el ViewModel compartido
) {
    NavHost(navController = navController, startDestination = rutaDeInicio) {
        composable(Routes.Login.ruta) { LoginScreen(navController) }
        composable(Routes.Registro.ruta) { RegisterScreen(navController) }
        composable(Routes.RecuperarClave.ruta) { PasswordRecoveryScreen(navController) }
        composable(
            route = Routes.Biblioteca.ruta,
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: -1
            // Se lo pasamos a la pantalla Biblioteca
            LibraryScreen(
                usuarioId = usuarioId,
                reproductorViewModel = reproductorViewModel, // <-- ¡NUEVO!
            )
        }
    }
}
