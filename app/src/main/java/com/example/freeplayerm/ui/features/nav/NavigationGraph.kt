// en: app/src/main/java/com/example/freeplayerm/ui/features/nav/NavigationGraph.kt
package com.example.freeplayerm.ui.features.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.freeplayerm.ui.features.profile.ProfileScreen
import com.example.freeplayerm.ui.features.profile.ProfileViewModel
import com.example.freeplayerm.ui.features.settings.SettingsRoute

@Composable
fun NavigationGraph(
   navController: NavHostController,
   rutaDeInicio: String,
   playerViewModel: PlayerViewModel,
) {
   NavHost(navController = navController, startDestination = rutaDeInicio) {
      
      // Pantalla de Login
      composable(Routes.Login.ruta) {
         LoginScreen(navController)
      }
      
      // Pantalla de Registro
      composable(Routes.Registro.ruta) {
         RegisterScreen(navController)
      }
      
      // Pantalla de Recuperación
      composable(Routes.RecuperarClave.ruta) {
         PasswordRecoveryScreen(navController)
      }
      
      // Pantalla de Biblioteca
      composable(
         route = Routes.Biblioteca.ruta,
         arguments = listOf(navArgument("usuarioId") { type = NavType.IntType }),
      ) { backStackEntry ->
         val usuarioId = backStackEntry.arguments?.getInt("usuarioId") ?: -1
         LibraryScreen(
            usuarioId = usuarioId,
            onNavigateToPerfil = {
               navController.navigate(Routes.Perfil.ruta)
            }
         )
      }
      
      // Pantalla de Perfil
      composable(Routes.Perfil.ruta) {
         val profileViewModel: ProfileViewModel = hiltViewModel()
         val usuario by profileViewModel.usuario.collectAsStateWithLifecycle()
         
         if (usuario != null) {
            ProfileScreen(
               usuario = usuario!!,
               onNavigateBack = {
                  navController.popBackStack()
               },
               onNavigateToSettings = {
                  navController.navigate(Routes.Configuraciones.ruta)
               },
               onEditProfile = {
                  // TODO: Implementar edición de perfil
                  navController.navigate(Routes.EditarPerfil.ruta)
               },
               onLogout = {
                  profileViewModel.cerrarSesion()
                  navController.navigate(Routes.Login.ruta) {
                     popUpTo(0) { inclusive = true }
                  }
               }
            )
         } else {
            // Pantalla de carga mientras obtiene el usuario
            Box(
               modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0xFF0F0518)),
               contentAlignment = Alignment.Center
            ) {
               CircularProgressIndicator(color = Color(0xFFD500F9))
            }
         }
      }
      
      // Pantalla de Configuraciones (placeholder)
      composable(Routes.Configuraciones.ruta) {
         SettingsRoute(
            onNavigateBack = { navController.popBackStack() }
         )
      }
      
      // Pantalla de Editar Perfil (placeholder)
      composable(Routes.EditarPerfil.ruta) {
         // TODO: Implementar EditProfileScreen
         Box(
            modifier = Modifier
               .fillMaxSize()
               .background(Color(0xFF0F0518)),
            contentAlignment = Alignment.Center
         ) {
            androidx.compose.material3.Text(
               "Editar Perfil - Próximamente",
               color = Color.White
            )
         }
      }
   }
}