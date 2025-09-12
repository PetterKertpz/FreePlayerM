// app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/Biblioteca.kt
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

@Composable
fun Biblioteca(
    usuarioId: Int, // Recibe el ID desde el grafo de navegación
    viewModel: BibliotecaViewModel = hiltViewModel()
) {
    val estado by viewModel.estadoUi.collectAsStateWithLifecycle()

    // Usamos LaunchedEffect para cargar los datos del usuario solo cuando el ID cambie
    LaunchedEffect(usuarioId) {
        viewModel.cargarDatosDeUsuario(usuarioId)
    }

    Scaffold(
        topBar = {
            SeccionEncabezado(
                usuario = estado.usuarioActual
            )
        }
    ) { paddingInterno ->
        // SOLUCIÓN: Aplicamos el padding al contenido principal
        ListaDeCanciones(
            modifier = Modifier.padding(paddingInterno)
        )
    }
}