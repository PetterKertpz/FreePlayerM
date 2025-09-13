// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.CancionEntity
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Composable
fun CuerpoCanciones(
    modifier: Modifier = Modifier,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    Column(modifier = modifier) {
        // 1. Barra de Búsqueda y Filtros
        BarraDeBusquedaYFiltros(
            textoDeBusqueda = estado.textoDeBusqueda,
            filtroActual = estado.filtroActual,
            enEvento = onBibliotecaEvento
        )

        // 2. Lista de Canciones
        ListaDeCanciones(
            canciones = estado.canciones,
            onCancionClick = { cancionSeleccionada ->
                // Cuando se hace clic en una canción, se lo decimos al reproductor
                onReproductorEvento(ReproductorEvento.SeleccionarCancion(cancionSeleccionada))
            }
        )
    }
}
// Añadir al final de CuerpoCanciones.kt

@Preview(showBackground = true)
@Composable
fun PreviewCuerpoCanciones() {
    val listaDePrueba = listOf(
        CancionEntity(1, 1, 1, 1, "El Sol no Regresa", 227, "", "LOCAL", null),
        CancionEntity(2, 2, 2, 2, "La Playa", 247, "", "LOCAL", null)
    )
    val estadoDePrueba = BibliotecaEstado(
        canciones = listaDePrueba,
        textoDeBusqueda = "La"
    )
    FreePlayerMTheme {
        CuerpoCanciones(
            estado = estadoDePrueba,
            onBibliotecaEvento = {},
            onReproductorEvento = {}
        )
    }
}