// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/VentanaListasReproduccion.kt
package com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.theme.AppColors

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
            onCrear = { nombre, desc, portadaUri ->
                onCrearLista(nombre, desc, portadaUri)
                mostrarDialogoCrearLista = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        PlaylistItem(
                            lista = lista,
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
private fun PlaylistItem(
    lista: ListaReproduccionEntity,
    estaSeleccionada: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        border = if (estaSeleccionada) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = lista.portadaUrl,
                contentDescription = "Portada de ${lista.nombre}",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.GrisProfundo),
                contentScale = ContentScale.Crop,
                // Un fallback por si no hay portada
                error = painterResource(id = R.drawable.ic_notification)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = lista.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            SelectorAnimado(seleccionado = estaSeleccionada)
        }
    }
}

@Composable
private fun SelectorAnimado(seleccionado: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (seleccionado) 1f else 0.8f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = seleccionado,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
fun DialogoCrearLista(
    listaAEditar: ListaReproduccionEntity? = null,
    onDismiss: () -> Unit,
    onCrear: (nombre: String, descripcion: String?, portadaUri: String?) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(listaAEditar?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(listaAEditar?.descripcion ?: "") }
    val nombreValido = nombre.isNotBlank()

    // --- ✅ LÓGICA DEL PHOTO PICKER ---
    // 1. Estado para guardar el Uri de la imagen seleccionada
    var imagenSeleccionadaUri by remember(listaAEditar?.idLista) {
        mutableStateOf<Uri?>(listaAEditar?.portadaUrl?.toUri())
    }
    // 2. Creamos el lanzador que abrirá la galería
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // Solo actualizamos la imagen si el usuario realmente seleccionó una nueva.
            // Si cancela (uri es null), no hacemos nada y la portada original se mantiene.
            if (uri != null) {
                imagenSeleccionadaUri = uri
            }
        }
    )


    AlertDialog(
        onDismissRequest = onDismiss,
        // El título cambia dependiendo del modo
        title = { Text(if (listaAEditar != null) "Editar Lista" else "Nueva Lista") },
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
            }

        },
        confirmButton = {
            Button(
                onClick = {
                    onCrear(nombre, descripcion.ifBlank { null }, imagenSeleccionadaUri?.toString())
                },
                enabled = nombreValido
            ) {
                // El texto del botón cambia dependiendo del modo
                Text(if (listaAEditar != null) "Guardar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


