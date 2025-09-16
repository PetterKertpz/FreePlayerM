// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/BarraDeBusquedaYFiltros.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.biblioteca.DireccionDeOrdenamiento
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

@Composable
fun BarraDeBusquedaYFiltros(
    modifier: Modifier = Modifier,
    textoDeBusqueda: String,
    criterioDeOrdenamiento: CriterioDeOrdenamiento,
    direccionDeOrdenamiento: DireccionDeOrdenamiento,
    enEvento: (BibliotecaEvento) -> Unit
) {
    // Estado para controlar si el menú desplegable está visible o no
    var menuExpandido by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textoDeBusqueda,
            onValueChange = { enEvento(BibliotecaEvento.TextoDeBusquedaCambiado(it)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Buscar en tu biblioteca...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Icono de búsqueda"
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = AppColors.GrisClaro,
                focusedContainerColor = AppColors.GrisClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        IconButton(onClick = { enEvento(BibliotecaEvento.DireccionDeOrdenamientoCambiada) }) {
            Icon(
                // El icono cambia dinámicamente según la dirección actual
                imageVector = if (direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    Icons.Default.KeyboardDoubleArrowUp
                } else {
                    Icons.Default.KeyboardDoubleArrowDown
                },
                contentDescription = "Cambiar dirección de ordenamiento",
                tint = Color.Black
            )
        }
        // --- LÓGICA DEL MENÚ DE FILTROS ---
        Box {
            IconButton(onClick = { menuExpandido = !menuExpandido }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Ordenar por",
                    tint = if (criterioDeOrdenamiento != CriterioDeOrdenamiento.NINGUNO) AppColors.PurpuraProfundo else Color.Black
                )
            }
            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = {}
            ) {
                // Iteramos sobre todos los valores del enum TipoDeFiltro
                CriterioDeOrdenamiento.entries.forEach { criterio ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = criterio.etiqueta,
                                    // Comparamos con el nuevo estado
                                    color = if (criterio == criterioDeOrdenamiento) AppColors.PurpuraProfundo else Color.Unspecified
                                )
                                if (criterio == criterioDeOrdenamiento) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Criterio seleccionado",
                                        tint = AppColors.PurpuraProfundo,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            // Enviamos el nuevo evento
                            enEvento(BibliotecaEvento.CriterioDeOrdenamientoCambiado(criterio))
                            menuExpandido = false
                        }
                    )
                }
            }
        }
    }
}
// Añadir al final de BarraDeBusquedaYFiltros.kt

@Preview(showBackground = true)
@Composable
fun PreviewBarraDeBusquedaYFiltros() {
    FreePlayerMTheme {
        // --- CAMBIO #5: ACTUALIZAMOS LA PREVISUALIZACIÓN ---
        BarraDeBusquedaYFiltros(
            textoDeBusqueda = "Mi búsqueda",
            criterioDeOrdenamiento = CriterioDeOrdenamiento.POR_ARTISTA,
            direccionDeOrdenamiento = DireccionDeOrdenamiento.DESCENDENTE,
            enEvento = {}
        )
    }
}