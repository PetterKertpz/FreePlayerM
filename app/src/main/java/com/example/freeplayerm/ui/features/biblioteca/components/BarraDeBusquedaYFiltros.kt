// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/BarraDeBusquedaYFiltros.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    var menuExpandido by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.1f to Color.Black,
                        0.4f to Color.Black,
                        0.8f to AppColors.GrisProfundo
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Añade espacio entre elementos
    ) {
        // Usamos OutlinedTextField para un mejor control del estilo
        OutlinedTextField(
            value = textoDeBusqueda,
            onValueChange = { enEvento(BibliotecaEvento.TextoDeBusquedaCambiado(it)) },
            modifier = Modifier.weight(1f)
                .border(
                width = 2.dp,
                color = AppColors.PurpuraProfundo,
                shape = RoundedCornerShape(30.dp)
            ),
            placeholder = { Text("Buscar en tu biblioteca...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Icono de búsqueda"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(30.dp), // Bordes completamente redondeados
            colors = TextFieldDefaults.colors(
                // --- ✅ COLORES CORREGIDOS PARA VISIBILIDAD ---
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AppColors.PurpuraProfundo,
                focusedPlaceholderColor = Color.LightGray,
                unfocusedPlaceholderColor = Color.LightGray,
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.White,
                focusedContainerColor = AppColors.Negro,
                unfocusedContainerColor = AppColors.Negro,
                focusedIndicatorColor = Color.Transparent, // Sin línea debajo
                unfocusedIndicatorColor = Color.Transparent // Sin línea debajo
            )
        )
        IconButton(onClick = { enEvento(BibliotecaEvento.DireccionDeOrdenamientoCambiada) }) {
            Icon(
                imageVector = if (direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    Icons.Default.KeyboardDoubleArrowUp
                } else {
                    Icons.Default.KeyboardDoubleArrowDown
                },
                contentDescription = "Cambiar dirección de ordenamiento",
                // --- ✅ ICONO EN BLANCO PARA SER VISIBLE ---
                tint = Color.White
            )
        }
        Box {
            IconButton(onClick = { menuExpandido = !menuExpandido }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Ordenar por",
                    // --- ✅ ICONO EN BLANCO (O PÚRPURA SI ESTÁ ACTIVO) ---
                    tint = if (criterioDeOrdenamiento != CriterioDeOrdenamiento.NINGUNO) AppColors.PurpuraProfundo else Color.White
                )
            }
            DropdownMenu(
                expanded = menuExpandido,
                // --- ✅ CORREGIDO PARA PODER CERRAR EL MENÚ ---
                onDismissRequest = { menuExpandido = false }
            ) {
                CriterioDeOrdenamiento.entries.forEach { criterio ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = criterio.etiqueta,
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
            criterioDeOrdenamiento = CriterioDeOrdenamiento.NINGUNO,
            direccionDeOrdenamiento = DireccionDeOrdenamiento.DESCENDENTE,
            enEvento = {}
        )
    }
}