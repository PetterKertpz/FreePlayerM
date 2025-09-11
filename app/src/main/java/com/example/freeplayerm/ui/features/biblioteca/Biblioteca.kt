package com.example.freeplayerm.ui.features.biblioteca

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.freeplayerm.ui.features.biblioteca.components.BarraDeReproduccion
import com.example.freeplayerm.ui.features.biblioteca.components.ListaDeCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado

@Composable
fun Biblioteca() {
    // Aquí irá la implementación de la pantalla de Biblioteca
    Scaffold(
        topBar = {
            // Aquí irá nuestro Header
            SeccionEncabezado(

            )
        },
        bottomBar = {
            // Aquí irá nuestro Footer
            BarraDeReproduccion()
        }
    ) { paddingInterno ->
        // Este es el contenido principal (Main).
        // `paddingInterno` contiene el espacio necesario para que nuestra lista
        // no quede oculta detrás del Header y el Footer. Es crucial aplicarlo.
        ListaDeCanciones(
        )
    }
}


@Composable
@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440",
    showSystemUi = true,)
fun BibliotecaPreview() {
    Biblioteca()
}