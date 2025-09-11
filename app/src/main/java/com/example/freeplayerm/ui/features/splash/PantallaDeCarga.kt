// app/src/main/java/com/example/freeplayerm/ui/features/splash/PantallaDeCarga.kt
package com.example.freeplayerm.ui.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.freeplayerm.ui.features.nav.Rutas
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaDeCarga(navController: NavController) {

    // LaunchedEffect es una función de Compose que ejecuta una corrutina
    // cuando el composable entra en la composición. Es ideal para tareas
    // asíncronas que solo deben ejecutarse una vez, como verificar la sesión.
    LaunchedEffect(key1 = true) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        val rutaDestino = if (usuarioActual != null) {
            // Si hay un usuario, vamos a la biblioteca
            Rutas.Biblioteca.ruta
        } else {
            // Si no, vamos al login
            Rutas.Login.ruta
        }

        // Navegamos a la ruta destino y limpiamos la pila de navegación
        // para que el usuario no pueda volver a esta pantalla de carga.
        navController.navigate(rutaDestino) {
            popUpTo(Rutas.PantallaDeCarga.ruta) {
                inclusive = true
            }
        }
    }

    // Mientras se verifica, mostramos un indicador de carga.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaDeCarga() {
    val navControllerFalso = rememberNavController()
    PantallaDeCarga(navController = navControllerFalso)
}
