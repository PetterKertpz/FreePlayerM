// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/Biblioteca.kt
package com.example.freeplayerm.ui.features.biblioteca

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoAlbumes
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado
import com.example.freeplayerm.ui.features.reproductor.PanelReproductorMinimizado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import java.util.Date

/**
 * =================================================================
 * 1. El "Composable Inteligente"
 * =================================================================
 */
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

    CuerpoBiblioteca(
        estadoBiblioteca = estadoBiblioteca,
        estadoReproductor = estadoReproductor,
        onBibliotecaEvento = bibliotecaViewModel::enEvento,
        onReproductorEvento = reproductorViewModel::enEvento,
        onAlbumClick = { album ->
            bibliotecaViewModel.enEvento(BibliotecaEvento.AlbumSeleccionado(album))
        }
    )
}

/**
 * =================================================================
 * 2. El "Composable Tonto"
 * =================================================================
 */
@Composable
fun CuerpoBiblioteca(
    estadoBiblioteca: BibliotecaEstado,
    estadoReproductor: ReproductorEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit,
    onAlbumClick: (AlbumEntity) -> Unit
) {
    Scaffold(
        topBar = {
            SeccionEncabezado(
                usuario = estadoBiblioteca.usuarioActual,
                cuerpoActual = estadoBiblioteca.cuerpoActual,
                onMenuClick = { nuevoCuerpo ->
                    onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(nuevoCuerpo))
                }
            )
        },
        bottomBar = {
            if (estadoReproductor.cancionActual != null) {
                PanelReproductorMinimizado(
                    estado = estadoReproductor,
                    enEvento = onReproductorEvento
                )
            }
        }
    ) { paddingInterno ->
        when (estadoBiblioteca.cuerpoActual) {
            TipoDeCuerpoBiblioteca.CANCIONES,
            TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM -> { // Ambos casos usan el mismo cuerpo
                CuerpoCanciones(
                    modifier = Modifier.padding(paddingInterno),
                    estado = estadoBiblioteca,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento
                )
            }
            TipoDeCuerpoBiblioteca.ALBUMES -> {
                CuerpoAlbumes(
                    modifier = Modifier.padding(paddingInterno),
                    albumes = estadoBiblioteca.albumes,
                    onAlbumClick = onAlbumClick // La lógica ya está en el Composable "inteligente"
                )
            }
            else -> {
                CuerpoCanciones(
                    modifier = Modifier.padding(paddingInterno),
                    estado = estadoBiblioteca,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento
                )
            }
        }
    }
}

/**
 * =================================================================
 * 3. Previsualización
 * =================================================================
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewBiblioteca() {
    val usuarioDePrueba = UsuarioEntity(1, "Preview User", "a@a.com", "", Date(), "", "LOCAL")
    val cancionDePrueba = CancionEntity(1, 1, 1, 1, "El Sol no Regresa", 227, "", "LOCAL", null)
    val albumesDePrueba = listOf(
        AlbumEntity(1, 1, "Viaje de Lujo", 2023, ""),
        AlbumEntity(2, 2, "Noches de Verano", 2022, "")
    )

    FreePlayerMTheme {
        CuerpoBiblioteca(
            estadoBiblioteca = BibliotecaEstado(
                usuarioActual = usuarioDePrueba,
                cuerpoActual = TipoDeCuerpoBiblioteca.ALBUMES,
                albumes = albumesDePrueba
            ),
            estadoReproductor = ReproductorEstado(
                cancionActual = cancionDePrueba,
                estaReproduciendo = true
            ),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            onAlbumClick = {}
        )
    }
}