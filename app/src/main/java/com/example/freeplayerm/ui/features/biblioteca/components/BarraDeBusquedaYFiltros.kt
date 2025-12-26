// ui/features/biblioteca/components/BarraDeBusquedaYFiltros.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.biblioteca.DireccionDeOrdenamiento

private val ColorAcento = Color(0xFFD500F9)
private val ColorSecundario = Color(0xFF00E5FF)
private val ColorFondo = Color(0xFF1E1E1E)

@Composable
fun BarraDeBusquedaYFiltros(
    textoDeBusqueda: String,
    criterioDeOrdenamiento: CriterioDeOrdenamiento,
    direccionDeOrdenamiento: DireccionDeOrdenamiento,
    enEvento: (BibliotecaEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpandido by remember { mutableStateOf(false) }

    // Animación de rotación para el icono de dirección
    val rotacionIcono by animateFloatAsState(
        targetValue = if (direccionDeOrdenamiento == DireccionDeOrdenamiento.DESCENDENTE) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "rotacion_direccion"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = ColorFondo.copy(alpha = 0.6f),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ColorAcento.copy(alpha = 0.5f),
                        ColorSecundario.copy(alpha = 0.5f)
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
            placeholder = {
                Text(
                    text = "Buscar en tu universo...",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = ColorAcento,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Botón Invertir Dirección (ABC ↔ CBA)
        IconButton(
            onClick = { enEvento(BibliotecaEvento.DireccionDeOrdenamientoCambiada) }
        ) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = if (direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    "Orden ascendente (A-Z)"
                } else {
                    "Orden descendente (Z-A)"
                },
                tint = ColorAcento,
                modifier = Modifier.rotate(rotacionIcono)
            )
        }

        // Botón Criterio de Ordenamiento
        IconButton(onClick = { menuExpandido = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Ordenar por",
                tint = if (criterioDeOrdenamiento != CriterioDeOrdenamiento.NINGUNO) {
                    ColorAcento
                } else {
                    Color.White
                }
            )
        }

        // Dropdown de criterios
        DropdownMenu(
            expanded = menuExpandido,
            onDismissRequest = { menuExpandido = false },
            modifier = Modifier.background(ColorFondo)
        ) {
            CriterioDeOrdenamiento.entries.forEach { criterio ->
                val estaSeleccionado = criterio == criterioDeOrdenamiento
                DropdownMenuItem(
                    text = {
                        Text(
                            text = criterio.etiqueta,
                            color = if (estaSeleccionado) ColorAcento else Color.White
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