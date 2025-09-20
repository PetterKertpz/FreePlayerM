// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/VentanaListasReproduccion.kt
package com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity

@Composable
fun VentanaListasReproduccion(
    listasExistentes: List<ListaReproduccionEntity>,
    onDismiss: () -> Unit,
    onCrearLista: (nombre: String, descripcion: String?) -> Unit,
    onAnadirAListas: (idListas: List<Int>) -> Unit
) {
    var mostrarDialogoCrearLista by remember { mutableStateOf(false) }
    val listasSeleccionadas = remember { mutableStateListOf<Int>() }

    if (mostrarDialogoCrearLista) {
        DialogoCrearLista(
            onDismiss = { mostrarDialogoCrearLista = false },
            onCrear = { nombre, desc ->
                onCrearLista(nombre, desc)
                mostrarDialogoCrearLista = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp) // Limita la altura máxima del diálogo
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Añadir a...",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedButton(
                    onClick = { mostrarDialogoCrearLista = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear lista")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear nueva lista")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listasExistentes, key = { it.idLista }) { lista ->
                        FilaListaSeleccionable(
                            nombreLista = lista.nombre,
                            estaSeleccionada = lista.idLista in listasSeleccionadas,
                            onToggle = {
                                if (lista.idLista in listasSeleccionadas) {
                                    listasSeleccionadas.remove(lista.idLista)
                                } else {
                                    listasSeleccionadas.add(lista.idLista)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAnadirAListas(listasSeleccionadas.toList()) },
                    enabled = listasSeleccionadas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir")
                }
            }
        }
    }
}

@Composable
private fun FilaListaSeleccionable(
    nombreLista: String,
    estaSeleccionada: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = estaSeleccionada,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = nombreLista, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DialogoCrearLista(
    onDismiss: () -> Unit,
    onCrear: (nombre: String, descripcion: String?) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    val nombreValido = nombre.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Lista de Reproducción") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    isError = !nombreValido
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") }
                )
                // TODO: Añadir aquí un botón o área para seleccionar una imagen de portada
            }
        },
        confirmButton = {
            Button(
                onClick = { onCrear(nombre, descripcion.ifBlank { null }) },
                enabled = nombreValido
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}