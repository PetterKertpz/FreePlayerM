// app/src/main/java/com/example/freeplayerm/ui/navigation/GrafoDeNavegacion.kt

package com.example.freeplayerm.ui.features.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freeplayerm.ui.features.biblioteca.Biblioteca
import com.example.freeplayerm.ui.features.login.PantallaLogin
import com.example.freeplayerm.ui.features.login.PantallaRegistro
import com.example.freeplayerm.ui.features.splash.PantallaDeCarga

@Composable
fun GrafoDeNavegacion() {
    // NavController: Es el cerebro de la navegación. Se encarga de gestionar
    // la pila de pantallas (back stack) y nos permite navegar entre ellas.
    val controladorDeNavegacion = rememberNavController()

    // NavHost: Es el contenedor que alojará todas las pantallas navegables.
    // Define el punto de partida de la navegación con `startDestination`.
    NavHost(
        navController = controladorDeNavegacion,
        startDestination = Rutas.PantallaDeCarga.ruta // Empezamos en la pantalla de carga
    ) {
        // composable(ruta) { ... }: Define una pantalla para una ruta específica.
        composable(Rutas.PantallaDeCarga.ruta) {
            PantallaDeCarga(controladorDeNavegacion)
        }
        composable(Rutas.Login.ruta) {
            PantallaLogin(controladorDeNavegacion)
        }
        composable(Rutas.Registro.ruta) {
            PantallaRegistro(controladorDeNavegacion)
        }
        composable(Rutas.Biblioteca.ruta) {
            Biblioteca() // La biblioteca no necesita el controlador por ahora
        }
    }
}