// ui/features/biblioteca/components/SearchBarWithFilters.kt
package com.example.freeplayerm.ui.features.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.library.BibliotecaEvento
import com.example.freeplayerm.ui.features.library.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.library.DireccionDeOrdenamiento

// ==================== COLORES LOCALES ====================

private object SearchBarColors {
    val NeonPrimary = Color(0xFFD500F9)
    val NeonSecondary = Color(0xFF00E5FF)
    val BackgroundDark = Color(0xFF1E1E1E).copy(alpha = 0.8f)
    val TextHint = Color.White.copy(alpha = 0.4f)
    val TextPrimary = Color.White

    // Gradiente normal: Sutil y elegante
    val BorderGradientIdle =
        Brush.horizontalGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
        )

    // Gradiente enfocado: Neón vibrante
    val BorderGradientFocused =
        Brush.horizontalGradient(colors = listOf(NeonPrimary, NeonSecondary))
}

// ==================== COMPONENTE PRINCIPAL ====================

@Composable
fun SearchBarWithFilters(
    textoDeBusqueda: String,
    criterioDeOrdenamiento: CriterioDeOrdenamiento,
    direccionDeOrdenamiento: DireccionDeOrdenamiento,
    enEvento: (BibliotecaEvento) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpandido by remember { mutableStateOf(false) }
    var estaEnFoco by remember { mutableStateOf(false) }

    // Animación de rotación para dirección
    val rotacionIcono by
        animateFloatAsState(
            targetValue =
                if (direccionDeOrdenamiento == DireccionDeOrdenamiento.DESCENDENTE) 180f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "rotacion_direccion",
        )

    // Animación del color del icono de búsqueda
    val colorIconoBusqueda by
        animateColorAsState(
            targetValue =
                if (estaEnFoco) SearchBarColors.NeonPrimary else Color.White.copy(alpha = 0.5f),
            label = "color_icono",
        )

    // Contenedor tipo "Píldora"
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(50)) // Píldora completa
                .background(SearchBarColors.BackgroundDark)
                .border(
                    width = if (estaEnFoco) 1.5.dp else 1.dp,
                    brush =
                        if (estaEnFoco) SearchBarColors.BorderGradientFocused
                        else SearchBarColors.BorderGradientIdle,
                    shape = RoundedCornerShape(50),
                )
                .padding(horizontal = 20.dp), // Padding interno cómodo
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 1. Icono de Búsqueda
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colorIconoBusqueda,
            modifier = Modifier.size(24.dp),
        )

        Spacer(Modifier.width(12.dp))

        // 2. Campo de Texto (Usamos BasicTextField para control total del estilo)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (textoDeBusqueda.isEmpty() && !estaEnFoco) {
                Text(
                    text = "Buscar en tu universo...",
                    style = TextStyle(color = SearchBarColors.TextHint, fontSize = 16.sp),
                )
            }

            BasicTextField(
                value = textoDeBusqueda,
                onValueChange = { enEvento(BibliotecaEvento.TextoDeBusquedaCambiado(it)) },
                modifier = Modifier.fillMaxWidth().onFocusChanged { estaEnFoco = it.isFocused },
                textStyle = TextStyle(color = SearchBarColors.TextPrimary, fontSize = 16.sp),
                cursorBrush = SolidColor(SearchBarColors.NeonPrimary),
                singleLine = true,
            )
        }

        // Botón limpiar (solo si hay texto)
        if (textoDeBusqueda.isNotEmpty()) {
            IconButton(
                onClick = { enEvento(BibliotecaEvento.TextoDeBusquedaCambiado("")) },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpiar",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        // Separador vertical sutil
        Box(
            modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f))
        )

        Spacer(Modifier.width(4.dp))

        // 3. Botón Dirección (ABC ↔ CBA)
        IconButton(onClick = { enEvento(BibliotecaEvento.DireccionDeOrdenamientoCambiada) }) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Cambiar dirección",
                tint =
                    if (direccionDeOrdenamiento == DireccionDeOrdenamiento.DESCENDENTE)
                        SearchBarColors.NeonPrimary
                    else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.rotate(rotacionIcono),
            )
        }

        // 4. Botón Filtros/Ordenar
        Box {
            IconButton(onClick = { menuExpandido = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Ordenar por",
                    tint =
                        if (criterioDeOrdenamiento != CriterioDeOrdenamiento.NINGUNO) {
                            SearchBarColors.NeonSecondary
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        },
                )
            }

            // Dropdown Personalizado
            MaterialTheme(
                colorScheme =
                    MaterialTheme.colorScheme.copy(
                        surface = Color(0xFF2D2D2D) // Fondo oscuro del menú
                    )
            ) {
                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false },
                    modifier =
                        Modifier.background(Color(0xFF252525))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                ) {
                    CriterioDeOrdenamiento.entries.forEach { criterio ->
                        val estaSeleccionado = criterio == criterioDeOrdenamiento
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = criterio.etiqueta,
                                    color =
                                        if (estaSeleccionado) SearchBarColors.NeonSecondary
                                        else Color.White,
                                    fontWeight =
                                        if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = {
                                enEvento(BibliotecaEvento.CriterioDeOrdenamientoCambiado(criterio))
                                menuExpandido = false
                            },
                            modifier =
                                Modifier.background(
                                    if (estaSeleccionado) Color.White.copy(alpha = 0.05f)
                                    else Color.Transparent
                                ),
                        )
                    }
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(name = "Barra de Búsqueda - Default", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewSearchBarDefault() {
    Box(Modifier.padding(16.dp)) {
        SearchBarWithFilters(
            textoDeBusqueda = "",
            criterioDeOrdenamiento =
                CriterioDeOrdenamiento.NINGUNO, // Asumiendo que existe en tu enum
            direccionDeOrdenamiento = DireccionDeOrdenamiento.ASCENDENTE,
            enEvento = {},
        )
    }
}

@Preview(
    name = "Barra de Búsqueda - Con Texto y Filtro",
    showBackground = true,
    backgroundColor = 0xFF121212,
)
@Composable
private fun PreviewSearchBarActive() {
    Box(Modifier.padding(16.dp)) {
        SearchBarWithFilters(
            textoDeBusqueda = "Daft Punk",
            criterioDeOrdenamiento = CriterioDeOrdenamiento.NINGUNO, // Asumiendo que existe
            direccionDeOrdenamiento = DireccionDeOrdenamiento.DESCENDENTE,
            enEvento = {},
        )
    }
}
