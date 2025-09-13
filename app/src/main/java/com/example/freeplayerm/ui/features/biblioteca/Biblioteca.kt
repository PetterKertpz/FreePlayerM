// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/Biblioteca.kt
package com.example.freeplayerm.ui.features.biblioteca

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.ui.features.biblioteca.components.ListaDeCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado
import com.example.freeplayerm.ui.features.reproductor.PanelReproductorMinimizado // <-- Importamos el nuevo panel
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel

// --- CAMBIO CLAVE AQUÍ ---
@Composable
fun Biblioteca(
    usuarioId: Int,
    bibliotecaViewModel: BibliotecaViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel // <-- Recibimos el ViewModel compartido
) {
    val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
    val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle() // Observamos su estado

    LaunchedEffect(usuarioId) {
        bibliotecaViewModel.cargarDatosDeUsuario(usuarioId)
    }

    Scaffold(
        topBar = {
            SeccionEncabezado(
                usuario = estadoBiblioteca.usuarioActual
            )
        },
        // --- LÓGICA DEL PANEL INFERIOR ---
        bottomBar = {
            // Solo mostramos el panel si hay una canción cargada en el reproductor
            if (estadoReproductor.cancionActual != null) {
                PanelReproductorMinimizado(
                    estado = estadoReproductor,
                    enEvento = reproductorViewModel::enEvento
                )
            }
        }
    ) { paddingInterno ->
        ListaDeCanciones(
            modifier = Modifier.padding(paddingInterno)
        )
    }
}