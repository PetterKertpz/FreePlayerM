// en: .../biblioteca/components/BarraDeAccionSeleccion.kt

package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BarraDeAccionSeleccion(
    cancionesSeleccionadas: Int,
    totalCanciones: Int,
    onSeleccionarTodo: () -> Unit,
    onCerrarModoSeleccion: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Acciones de selección a la izquierda
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSeleccionarTodo) {
                Icon(Icons.Default.Checklist, contentDescription = "Seleccionar Todo")
            }
            Text("$cancionesSeleccionadas / $totalCanciones")
        }

        // Botón para cerrar el modo selección (Cancelar Selección)
        IconButton(onClick = onCerrarModoSeleccion) {
            Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
        }
    }
}