package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun EncabezadoSeleccionLista(
    lista: ListaReproduccionEntity?,
    cancionesSeleccionadas: Int,
    totalCanciones: Int,
    onSeleccionarTodo: () -> Unit,
    onQuitarSeleccion: () -> Unit,
    onEliminarLista: () -> Unit,
    onCerrarModoSeleccion: () -> Unit // <-- ✅ Nuevo parámetro
) {
    if (lista == null) return

    Box( // Usamos Box para poder posicionar el botón de cerrar en la esquina
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // --- ✅ SECCIÓN SUPERIOR: PORTADA + TÍTULO ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 40.dp) // Dejamos espacio para el botón de cerrar
            ) {
                AsyncImage(
                    model = lista.portadaUrl,
                    contentDescription = "Portada de ${lista.nombre}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.GrisProfundo),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lista.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!lista.descripcion.isNullOrBlank()) {
                        Text(
                            lista.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))

            // --- ✅ SECCIÓN INFERIOR: ACCIONES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Acciones de selección a la izquierda
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSeleccionarTodo) {
                        Icon(Icons.Default.Checklist, contentDescription = "Seleccionar Todo")
                    }
                    Text(
                        "$cancionesSeleccionadas / $totalCanciones",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = onQuitarSeleccion, enabled = cancionesSeleccionadas > 0) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar de la lista")
                    }
                }

                // Acción de eliminar lista a la derecha
                TextButton(onClick = onEliminarLista) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar Lista", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // --- ✅ BOTÓN DE CERRAR EN LA ESQUINA ---
        IconButton(
            onClick = onCerrarModoSeleccion,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar modo selección")
        }
    }
}