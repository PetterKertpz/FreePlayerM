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
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreePlayerMTheme {
                val usuario by viewModel.usuarioActual.collectAsStateWithLifecycle()

                // La ruta de inicio ahora puede ser Login o Biblioteca
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
                    // Esta llamada ahora es correcta porque coincide con la nueva firma de GrafoDeNavegacion
                    GrafoDeNavegacion(navController = navController, rutaDeInicio = rutaDeInicio)
                }
            }
        }
    }
}