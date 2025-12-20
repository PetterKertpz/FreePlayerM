// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoCanciones.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.TipoDeCuerpoBiblioteca
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * ✅ ULTRA-OPTIMIZADO: Lista de canciones con performance mejorado
 *
 * Mejoras críticas:
 * 1. ✅ Uso de key estable (idCancion) para evitar recomposiciones
 * 2. ✅ contentType para reciclaje optimizado
 * 3. ✅ derivedStateOf para cálculos eficientes
 * 4. ✅ Modifier.animateItem() para animaciones suaves
 * 5. ✅ Estados de carga claramente definidos
 */
@Composable
fun CuerpoCanciones(
    canciones: List<CancionConArtista>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit
) {
    // ✅ OPTIMIZACIÓN: Detectar estado vacío sin recomponer innecesariamente
    val esListaVacia by remember(canciones.size) {
        derivedStateOf { canciones.isEmpty() }
    }

    if (esListaVacia) {
        // Estados vacíos optimizados
        EstadoVacio(
            textoDeBusqueda = estado.textoDeBusqueda,
            modifier = modifier
        )
    } else {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(bottom = 100.dp), // Espacio para el player
            modifier = modifier.fillMaxSize()
        ) {
            items(
                items = canciones,
                key = { it.cancion.idCancion }, // ⚡ KEY ESTABLE - Crucial para performance
                contentType = { "CancionItem" } // ⚡ RECICLAJE OPTIMIZADO
            ) { item ->
                ItemCancionGalacticoWrapper(
                    item = item,
                    estado = estado,
                    onBibliotecaEvento = onBibliotecaEvento,
                    onReproductorEvento = onReproductorEvento,
                    modifier = Modifier.animateItem() // ⚡ ANIMACIONES EFICIENTES
                )
            }
        }
    }
}

/**
 * ✅ Estado vacío optimizado con mensajes contextuales
 */
@Composable
private fun EstadoVacio(
    textoDeBusqueda: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = if (textoDeBusqueda.isNotBlank()) {
                "No hay resultados para \"$textoDeBusqueda\""
            } else {
                "No hay canciones disponibles"
            },
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * ✅ Wrapper con lógica de interacción separada para mejor performance
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemCancionGalacticoWrapper(
    item: CancionConArtista,
    estado: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ OPTIMIZACIÓN: Calcular solo cuando cambia la selección
    val isSelected by remember(estado.cancionesSeleccionadas, item.cancion.idCancion) {
        derivedStateOf {
            item.cancion.idCancion in estado.cancionesSeleccionadas
        }
    }

    Box(
        modifier = modifier.combinedClickable(
            onClick = {
                if (estado.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.AlternarSeleccionCancion(item.cancion.idCancion))
                } else {
                    onReproductorEvento(ReproductorEvento.Reproduccion.EstablecerColaYReproducir(estado.canciones, item))
                    onBibliotecaEvento(BibliotecaEvento.LimpiarBusqueda)
                }
            },
            onLongClick = {
                if (!estado.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.ActivarModoSeleccion(item))
                }
            }
        )
    ) {
        ItemCancionGalactico(
            cancion = item,
            esSeleccionado = isSelected,
            alClick = { /* Manejado por el Box wrapper */ },
            alClickMasOpciones = {
                onBibliotecaEvento(BibliotecaEvento.EditarCancion(item))
            }
        )
    }
}

// ==========================================
// ✅ PREVIEWS COMPLETAS
// ==========================================

// Datos mock optimizados
private object MockDataCanciones {
    fun generarCanciones(count: Int): List<CancionConArtista> {
        return (1..count).map { i ->
            CancionConArtista(
                cancion = CancionEntity(
                    idCancion = i,
                    titulo = "Canción de Prueba #$i",
                    idArtista = (i % 5) + 1,
                    idAlbum = (i % 3) + 1,
                    duracionSegundos = 210,
                    archivoPath = "/path/to/song$i.mp3",
                    origen = "LOCAL",
                    idGenero = null
                ),
                artistaNombre = "Artista ${(i % 5) + 1}",
                albumNombre = "Álbum ${(i % 3) + 1}",
                generoNombre = "Rock",
                esFavorita = i % 3 == 0,
                portadaPath = null,
                fechaLanzamiento = null
            )
        }
    }

    val estadoConCanciones = BibliotecaEstado(
        canciones = generarCanciones(20),
        textoDeBusqueda = "",
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES
    )

    val estadoVacio = BibliotecaEstado(
        canciones = emptyList(),
        textoDeBusqueda = "",
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES
    )

    val estadoBusquedaSinResultados = BibliotecaEstado(
        canciones = emptyList(),
        textoDeBusqueda = "canción que no existe",
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES
    )

    val estadoModoSeleccion = BibliotecaEstado(
        canciones = generarCanciones(10),
        esModoSeleccion = true,
        cancionesSeleccionadas = setOf(1, 3, 5),
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES
    )
}

@Preview(name = "Light - Lista Normal", showBackground = true)
@Preview(name = "Dark - Lista Normal", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCuerpoCancionesNormal() {
    FreePlayerMTheme {
        CuerpoCanciones(
            estado = MockDataCanciones.estadoConCanciones,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = MockDataCanciones.estadoConCanciones.canciones
        )
    }
}

@Preview(name = "Estado Vacío", showBackground = true)
@Composable
private fun PreviewCuerpoCancionesVacio() {
    FreePlayerMTheme {
        CuerpoCanciones(
            estado = MockDataCanciones.estadoVacio,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = MockDataCanciones.estadoVacio.canciones
        )
    }
}


@Preview(name = "Modo Selección", showBackground = true)
@Composable
private fun PreviewCuerpoCancionesModoSeleccion() {
    FreePlayerMTheme {
        CuerpoCanciones(
            estado = MockDataCanciones.estadoModoSeleccion,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = MockDataCanciones.estadoModoSeleccion.canciones
        )
    }
}

@Preview(name = "Lista Larga (Performance Test)", showBackground = true)
@Composable
private fun PreviewCuerpoCancionesListaLarga() {
    FreePlayerMTheme {
        val estadoLargo = BibliotecaEstado(
            canciones = MockDataCanciones.generarCanciones(100),
            cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES
        )

        CuerpoCanciones(
            estado = estadoLargo,
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            lazyListState = LazyListState(0, 0),
            canciones = estadoLargo.canciones
        )
    }
}