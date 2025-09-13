// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/Biblioteca.kt
package com.example.freeplayerm.ui.features.biblioteca

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado
import com.example.freeplayerm.ui.features.reproductor.PanelReproductorMinimizado
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel

@Composable
fun Biblioteca(
    usuarioId: Int,
    bibliotecaViewModel: BibliotecaViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel
) {
    val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
    val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

    LaunchedEffect(usuarioId) {
        bibliotecaViewModel.cargarDatosDeUsuario(usuarioId)
    }

    Scaffold(
        topBar = {
            SeccionEncabezado(
                usuario = estadoBiblioteca.usuarioActual,
                cuerpoActual = estadoBiblioteca.cuerpoActual, // Le pasamos el cuerpo actual
                // --- ¡AQUÍ ESTÁ LA CONEXIÓN! ---
                onMenuClick = { nuevoCuerpo ->
                    // Usamos el evento que creamos en el ViewModel
                    bibliotecaViewModel.enEvento(BibliotecaEvento.CambiarCuerpo(nuevoCuerpo))
                }
            )
        },
        bottomBar = {
            if (estadoReproductor.cancionActual != null) {
                PanelReproductorMinimizado(
                    estado = estadoReproductor,
                    enEvento = reproductorViewModel::enEvento
                )
            }
        }
    ) { paddingInterno ->
        when (estadoBiblioteca.cuerpoActual) {
            TipoDeCuerpoBiblioteca.CANCIONES -> {
                CuerpoCanciones(
                    modifier = Modifier.padding(paddingInterno),
                    estado = estadoBiblioteca,
                    onBibliotecaEvento = bibliotecaViewModel::enEvento,
                    onReproductorEvento = reproductorViewModel::enEvento
                )
            }
            // TODO: Añadir los otros cuerpos (Álbumes, Artistas, etc.)
            else -> {
                CuerpoCanciones(
                    modifier = Modifier.padding(paddingInterno),
                    estado = estadoBiblioteca,
                    onBibliotecaEvento = bibliotecaViewModel::enEvento,
                    onReproductorEvento = reproductorViewModel::enEvento
                )
            }
        }
    }
}