// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/VentanaListasReproduccion.kt
package com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity

@Composable
fun VentanaListasReproduccion(
    listasExistentes: List<ListaReproduccionEntity>,
    onDismiss: () -> Unit,
    onCrearLista: (nombre: String, descripcion: String?, portadaUri: String?) -> Unit,
    onAnadirAListas: (idListas: List<Int>) -> Unit
) {
    var mostrarDialogoCrearLista by remember { mutableStateOf(false) }
    val listasSeleccionadas = remember { mutableStateListOf<Int>() }

    if (mostrarDialogoCrearLista) {
        DialogoCrearLista(
            onDismiss = { mostrarDialogoCrearLista = false },
            onCrear = { nombre, desc, portadaUri -> // Recibimos los 3 parámetros
                onCrearLista(nombre, desc, portadaUri) // Y los pasamos hacia arriba
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
    onCrear: (nombre: String, descripcion: String?, portadaUri: String?) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    val nombreValido = nombre.isNotBlank()

    // --- ✅ LÓGICA DEL PHOTO PICKER ---
    // 1. Estado para guardar el Uri de la imagen seleccionada
    var imagenSeleccionadaUri by remember { mutableStateOf<Uri?>(null) }
    // 2. Creamos el lanzador que abrirá la galería
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            imagenSeleccionadaUri = uri
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Lista de Reproducción") },
        text = {
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            // 3. Lanzamos el Photo Picker al hacer clic
                            photoPickerLauncher.launch(
                                // La llamada correcta usa el Builder de la clase "PickVisualMediaRequest"
                                PickVisualMediaRequest.Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    .build()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenSeleccionadaUri != null) {
                        AsyncImage(
                            model = imagenSeleccionadaUri,
                            contentDescription = "Portada seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Añadir portada",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

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
                onClick = {
                    // 4. Pasamos el Uri como String al crear la lista
                    onCrear(nombre, descripcion.ifBlank { null }, imagenSeleccionadaUri.toString())
                },
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