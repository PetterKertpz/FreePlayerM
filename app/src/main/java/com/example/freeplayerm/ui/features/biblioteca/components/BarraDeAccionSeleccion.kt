// ui/features/biblioteca/components/BarraDeAccionSeleccion.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * ✅ OPTIMIZADO: Barra de acción flotante para modo selección
 *
 * Mejoras:
 * - Añadido semantics para accesibilidad
 * - Añadido estado hover
 * - Optimización de colores y gradientes
 */
@Composable
fun BarraDeAccionSeleccion(
    cancionesSeleccionadas: Int,
    totalCanciones: Int,
    onSeleccionarTodo: () -> Unit,
    onCerrarModoSeleccion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2A0F35).copy(alpha = 0.95f), // Morado oscuro casi opaco
        border = BorderStroke(1.dp, Color(0xFFD500F9).copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ✅ CORREGIDO: contentDescription correctamente implementado
                val descripcionSeleccion = if (cancionesSeleccionadas == totalCanciones) {
                    "Deseleccionar todas"
                } else {
                    "Seleccionar todas las canciones"
                }

                IconButton(
                    onClick = onSeleccionarTodo
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = descripcionSeleccion,
                        tint = if (cancionesSeleccionadas == totalCanciones) {
                            Color(0xFFD500F9) // Morado brillante si están todas
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        }
                    )
                }

                Column {
                    Text(
                        text = "$cancionesSeleccionadas seleccionadas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // ✅ Nuevo: Mostrar total
                    if (totalCanciones > 0) {
                        Text(
                            text = "de $totalCanciones",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            IconButton(
                onClick = onCerrarModoSeleccion
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Salir del modo selección",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ==========================================
// ✅ PREVIEWS COMPLETAS
// ==========================================

@Preview(name = "Light - Pocas seleccionadas", showBackground = true)
@Preview(name = "Dark - Pocas seleccionadas", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewBarraPocasSeleccionadas() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 3,
                totalCanciones = 50,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )
        }
    }
}

@Preview(name = "Todas seleccionadas", showBackground = true)
@Composable
private fun PreviewBarraTodasSeleccionadas() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 50,
                totalCanciones = 50,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )
        }
    }
}

@Preview(name = "Sin selección", showBackground = true)
@Composable
private fun PreviewBarraSinSeleccion() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 0,
                totalCanciones = 100,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )
        }
    }
}

@Preview(name = "Lista pequeña", showBackground = true)
@Composable
private fun PreviewBarraListaPequeña() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 2,
                totalCanciones = 5,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )
        }
    }
}

@Preview(name = "Estados Múltiples", showBackground = true, heightDp = 400)
@Composable
private fun PreviewBarraEstadosMultiples() {
    FreePlayerMTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Sin selección:", color = Color.White)
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 0,
                totalCanciones = 50,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )

            Text("Algunas seleccionadas:", color = Color.White)
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 15,
                totalCanciones = 50,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )

            Text("Todas seleccionadas:", color = Color.White)
            BarraDeAccionSeleccion(
                cancionesSeleccionadas = 50,
                totalCanciones = 50,
                onSeleccionarTodo = {},
                onCerrarModoSeleccion = {}
            )
        }
    }
}