// ui/features/biblioteca/components/VentanaListasReproduccion.kt
package com.example.freeplayerm.ui.features.biblioteca.components

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
            // Fondo oscuro para el diálogo principal
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Añadir a...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedButton(
                    onClick = { mostrarDialogoCrearLista = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD500F9)),
                    border = BorderStroke(1.dp, Color(0xFFD500F9))
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD500F9),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray
                    )
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
        // Borde morado si está seleccionado
        border = if (estaSeleccionada) BorderStroke(2.dp, Color(0xFFD500F9)) else null,
        colors = CardDefaults.cardColors(
            // Fondo ligeramente más claro que el diálogo para destacar
            containerColor = Color(0xFF2D2D2D)
        ),
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
                // Fallback si no hay portada (asegúrate de tener este icono o usa otro)
                error = painterResource(id = R.drawable.ic_notification)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = lista.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
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
                if (seleccionado) Color(0xFFD500F9) else Color.White.copy(alpha = 0.1f)
            )
            .border(2.dp, if (seleccionado) Color(0xFFD500F9) else Color.Gray, CircleShape),
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
                tint = Color.White,
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

    // 1. Estado para guardar el Uri de la imagen seleccionada
    var imagenSeleccionadaUri by remember(listaAEditar?.idLista) {
        mutableStateOf<Uri?>(listaAEditar?.portadaUrl?.toUri())
    }

    // 2. Lanzador del Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imagenSeleccionadaUri = uri
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        // Configuración de colores oscuros para el AlertDialog
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),

        title = { Text(if (listaAEditar != null) "Editar Lista" else "Nueva Lista") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2D2D2D))
                        .clickable {
                            photoPickerLauncher.launch(
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
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Input Nombre estilizado para Dark Mode
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    isError = !nombreValido,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFD500F9),
                        focusedLabelColor = Color(0xFFD500F9),
                        unfocusedLabelColor = Color.Gray,
                        focusedIndicatorColor = Color(0xFFD500F9),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Input Descripción estilizado
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFD500F9),
                        focusedLabelColor = Color(0xFFD500F9),
                        unfocusedLabelColor = Color.Gray,
                        focusedIndicatorColor = Color(0xFFD500F9),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCrear(nombre, descripcion.ifBlank { null }, imagenSeleccionadaUri?.toString())
                },
                enabled = nombreValido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD500F9),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(if (listaAEditar != null) "Guardar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text("Cancelar")
            }
        }
    )
}