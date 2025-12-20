// ui/features/biblioteca/components/BarraDeBusquedaYFiltros.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.biblioteca.DireccionDeOrdenamiento

@Composable
fun BarraDeBusquedaYFiltros(
    textoDeBusqueda: String,
    criterioDeOrdenamiento: CriterioDeOrdenamiento,
    direccionDeOrdenamiento: DireccionDeOrdenamiento,
    enEvento: (BibliotecaEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    // Contenedor Flotante "Capsula"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFD500F9).copy(alpha = 0.5f),
                        Color(0xFF00E5FF).copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono Search
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f)
        )

        Spacer(Modifier.width(8.dp))

        // Input Transparente
        TextField(
            value = textoDeBusqueda,
            onValueChange = { enEvento(BibliotecaEvento.TextoDeBusquedaCambiado(it)) },
            placeholder = { Text("Buscar en tu universo...", color = Color.White.copy(alpha = 0.4f)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFD500F9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Botón Filtros
        var menuExpandido by remember { mutableStateOf(false) }

        IconButton(onClick = { menuExpandido = true }) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Ordenar",
                tint = if (criterioDeOrdenamiento != CriterioDeOrdenamiento.NINGUNO) Color(0xFFD500F9) else Color.White
            )
        }

        // Dropdown Oscuro
        DropdownMenu(
            expanded = menuExpandido,
            onDismissRequest = { menuExpandido = false },
            modifier = Modifier.background(Color(0xFF1E1E1E))
        ) {
            CriterioDeOrdenamiento.entries.forEach { criterio ->
                DropdownMenuItem(
                    text = {
                        Text(
                            criterio.etiqueta,
                            color = if(criterio == criterioDeOrdenamiento) Color(0xFFD500F9) else Color.White
                        )
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