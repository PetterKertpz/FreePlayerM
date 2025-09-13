// en: app/src/main/java/com/example/freeplayerm/MainActivity.kt

package com.example.freeplayerm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.freeplayerm.ui.features.nav.GrafoDeNavegacion
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inyectamos el MainViewModel para la lógica de sesión
    private val mainViewModel: MainViewModel by viewModels()
    // --- CAMBIO CLAVE ---
    // Inyectamos el ReproductorViewModel a nivel de Actividad.
    // Su estado persistirá mientras la actividad esté viva.
    private val reproductorViewModel: ReproductorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreePlayerMTheme {
                val usuario by mainViewModel.usuarioActual.collectAsStateWithLifecycle()

                val rutaDeInicio = if (usuario != null) {
                    Rutas.Biblioteca.crearRuta(usuario!!.id)
                } else {
                    Rutas.Login.ruta
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Pasamos la instancia del ReproductorViewModel a nuestro grafo de navegación
                    GrafoDeNavegacion(
                        navController = navController,
                        rutaDeInicio = rutaDeInicio,
                        reproductorViewModel = reproductorViewModel // <-- ¡NUEVO!
                    )
                }
            }
        }
    }
}