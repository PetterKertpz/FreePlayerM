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
import com.example.freeplayerm.ui.features.player.viewmodel.PlayerViewModel
import com.example.freeplayerm.ui.features.profile.EditProfileScreen
import com.example.freeplayerm.ui.features.profile.ProfileScreen
import com.example.freeplayerm.ui.features.settings.SettingsRoute

@Composable
fun NavigationGraph(
   navController: NavHostController,
   rutaDeInicio: String,
   playerViewModel: PlayerViewModel,
) {
   NavHost(navController = navController, startDestination = rutaDeInicio) {

      // Pantalla de Login
      composable(Routes.Login.ruta) { LoginScreen(navController) }

      // Pantalla de Registro
      composable(Routes.Registro.ruta) { RegisterScreen(navController) }

      // Pantalla de Recuperación
      composable(Routes.RecuperarClave.ruta) { PasswordRecoveryScreen(navController) }

      // Pantalla de Biblioteca
      composable(
         route = Routes.Biblioteca.ruta,
         arguments = listOf(navArgument("usuarioId") { type = NavType.IntType }),
      ) { backStackEntry ->
         val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: -1
         LibraryScreen(
            usuarioId = usuarioId,
            onNavigateToPerfil = { navController.navigate(Routes.Perfil.ruta) },
         )
      }

      // Pantalla de Perfil
      composable(Routes.Perfil.ruta) {
         ProfileScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSettings = { navController.navigate(Routes.Configuraciones.ruta) },
            onNavigateToEditProfile = { navController.navigate(Routes.EditarPerfil.ruta) },
         )
      }

      // Pantalla de Configuraciones
      composable(Routes.Configuraciones.ruta) {
         SettingsRoute(onNavigateBack = { navController.popBackStack() })
      }

      // Pantalla de Editar Perfil (placeholder)
      composable(Routes.EditarPerfil.ruta) {
         EditProfileScreen(
            onNavigateBack = { navController.popBackStack() },
            onSaveSuccess = { navController.popBackStack() },
         )
      }
   }
}
