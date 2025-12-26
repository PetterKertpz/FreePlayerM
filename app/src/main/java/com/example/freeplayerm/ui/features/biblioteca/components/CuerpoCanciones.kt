package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.components.items.ItemCancionGalactico
import com.example.freeplayerm.ui.features.biblioteca.components.layouts.BibliotecaListLayout
import com.example.freeplayerm.ui.features.biblioteca.domain.toItems
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento

/**
 * ✅ REFACTORIZADO - Usa BibliotecaListLayout genérico
 *
 * Antes: 80+ líneas con lógica duplicada
 * Ahora: 40 líneas enfocadas en lógica de negocio
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
    // ✅ Convertir entidades a items de UI con estado de selección
    val items by remember(canciones, estado.cancionesSeleccionadas) {
        derivedStateOf {
            canciones.toItems(estado.cancionesSeleccionadas)
        }
    }

    // ✅ Usar layout genérico
    BibliotecaListLayout(
        items = items,
        listState = lazyListState,
        searchQuery = estado.textoDeBusqueda,
        emptyMessage = "No hay canciones en tu biblioteca",
        modifier = modifier
    ) { cancionItem ->
        // Lógica específica de interacción con canciones
        ItemCancionGalactico(
            cancion = cancionItem.cancionConArtista,
            esSeleccionado = cancionItem.isSelected,
            alClick = {
                if (estado.esModoSeleccion) {
                    onBibliotecaEvento(
                        BibliotecaEvento.AlternarSeleccionCancion(cancionItem.id)
                    )
                } else {
                    onReproductorEvento(
                        ReproductorEvento.Reproduccion.EstablecerColaYReproducir(
                            estado.canciones,
                            cancionItem.cancionConArtista
                        )
                    )
                    onBibliotecaEvento(BibliotecaEvento.LimpiarBusqueda)
                }
            },
            alLongClick = {
                if (!estado.esModoSeleccion) {
                    onBibliotecaEvento(
                        BibliotecaEvento.ActivarModoSeleccion(cancionItem.cancionConArtista)
                    )
                }
            },
            alClickMasOpciones = {
                onBibliotecaEvento(
                    BibliotecaEvento.EditarCancion(cancionItem.cancionConArtista)
                )
            }
        )
    }
}