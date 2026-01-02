package com.example.freeplayerm.ui.features.library.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.PlaylistEntity

// ==================== COLORES Y ESTILOS LOCALES ====================

private object DialogColors {
    val NeonPrimary = Color(0xFFD500F9)
    val DarkBackgroundStart = Color(0xFF2A0F35)
    val DarkBackgroundEnd = Color(0xFF0F0518)

    val BorderGradient =
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.2f), NeonPrimary.copy(alpha = 0.5f))
        )

    val BackgroundGradient =
        Brush.verticalGradient(colors = listOf(DarkBackgroundStart, DarkBackgroundEnd))
}

// ==================== COMPONENTES PRINCIPALES ====================

@Composable
fun PlaylistDialog(
    listasExistentes: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onCrearLista: (nombre: String, descripcion: String?, portadaUri: String?) -> Unit,
    onAnadirAListas: (idListas: List<Int>) -> Unit,
) {
    var mostrarDialogoCrearLista by remember { mutableStateOf(false) }
    val listasSeleccionadas = remember { mutableStateListOf<Int>() }

    // Diálogo anidado para crear lista
    if (mostrarDialogoCrearLista) {
        DialogoCrearLista(
            onDismiss = { mostrarDialogoCrearLista = false },
            onCrear = { nombre, desc, portadaUri ->
                onCrearLista(nombre, desc, portadaUri)
                mostrarDialogoCrearLista = false
            },
        )
    }

    // Diálogo Principal "Añadir a..."
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Contenedor Galáctico
        Surface(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, DialogColors.BorderGradient, RoundedCornerShape(24.dp)),
            color = Color.Transparent, // Usamos Box con gradiente dentro
        ) {
            Box(modifier = Modifier.background(DialogColors.BackgroundGradient).padding(24.dp)) {
                Column {
                    // Encabezado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Añadir a Playlist",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White.copy(alpha = 0.6f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Crear Nueva
                    OutlinedButton(
                        onClick = { mostrarDialogoCrearLista = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = DialogColors.NeonPrimary,
                                containerColor = Color.White.copy(alpha = 0.05f),
                            ),
                        border = BorderStroke(1.dp, DialogColors.NeonPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear nueva lista", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de Playlists
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Acción Principal
                    Button(
                        onClick = { onAnadirAListas(listasSeleccionadas.toList()) },
                        enabled = listasSeleccionadas.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DialogColors.NeonPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.3f),
                            ),
                        shape = RoundedCornerShape(12.dp),
                        elevation =
                            ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 2.dp,
                            ),
                    ) {
                        Text("AÑADIR (${listasSeleccionadas.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCrearLista(
    listaAEditar: PlaylistEntity? = null,
    onDismiss: () -> Unit,
    onCrear: (nombre: String, descripcion: String?, portadaUri: String?) -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf(listaAEditar?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(listaAEditar?.descripcion ?: "") }
    val nombreValido = nombre.isNotBlank()

    var imagenSeleccionadaUri by
        remember(listaAEditar?.idLista) { mutableStateOf(listaAEditar?.portadaUrl?.toUri()) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> if (uri != null) imagenSeleccionadaUri = uri },
        )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            modifier =
                Modifier.fillMaxWidth()
                    .border(1.dp, DialogColors.BorderGradient, RoundedCornerShape(24.dp)),
        ) {
            Box(modifier = Modifier.background(DialogColors.BackgroundGradient).padding(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (listaAEditar != null) "Editar Lista" else "Nueva Lista",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Selector de Imagen
                    Box(
                        modifier =
                            Modifier.size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(
                                    1.dp,
                                    if (imagenSeleccionadaUri != null) DialogColors.NeonPrimary
                                    else Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp),
                                )
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest.Builder()
                                            .setMediaType(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                            .build()
                                    )
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (imagenSeleccionadaUri != null) {
                            AsyncImage(
                                model = imagenSeleccionadaUri,
                                contentDescription = "Portada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            // Overlay sutil para indicar que se puede cambiar
                            Box(
                                modifier =
                                    Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f))
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = DialogColors.NeonPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Añadir foto", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Inputs Estilizados
                    GalacticTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = "Nombre de la lista",
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GalacticTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = "Descripción (opcional)",
                        singleLine = false,
                        maxLines = 3,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botones de Acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                onCrear(
                                    nombre,
                                    descripcion.ifBlank { null },
                                    imagenSeleccionadaUri?.toString(),
                                )
                            },
                            enabled = nombreValido,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = DialogColors.NeonPrimary,
                                    disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                ),
                        ) {
                            Text(if (listaAEditar != null) "Guardar" else "Crear")
                        }
                    }
                }
            }
        }
    }
}

// ==================== SUB-COMPONENTES ====================

@Composable
private fun PlaylistItem(lista: PlaylistEntity, estaSeleccionada: Boolean, onToggle: () -> Unit) {
    val backgroundColor =
        if (estaSeleccionada) DialogColors.NeonPrimary.copy(alpha = 0.15f) else Color.Transparent

    val borderColor =
        if (estaSeleccionada) DialogColors.NeonPrimary.copy(alpha = 0.5f)
        else Color.White.copy(alpha = 0.1f)

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(onClick = onToggle)
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Portada
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black,
            modifier = Modifier.size(48.dp),
        ) {
            if (lista.portadaUrl != null) {
                AsyncImage(
                    model = lista.portadaUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lista.nombre,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${lista.totalCanciones} canciones",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // Checkbox con Animación Manual (Fix para RowScope.AnimatedVisibility error)
        SelectorAnimado(estaSeleccionada)
    }
}

/** 🎯 Selector Animado corregido (Sin AnimatedVisibility para evitar errores de Scope) */
@Composable
private fun SelectorAnimado(seleccionado: Boolean) {
    // Animación de escala con efecto rebote
    val scale by
        animateFloatAsState(
            targetValue = if (seleccionado) 1f else 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "scale",
        )

    // Animación de opacidad
    val alpha by
        animateFloatAsState(
            targetValue = if (seleccionado) 1f else 0f,
            animationSpec = tween(200),
            label = "alpha",
        )

    Box(
        modifier =
            Modifier.size(24.dp)
                .clip(CircleShape)
                .background(if (seleccionado) DialogColors.NeonPrimary else Color.Transparent)
                .border(
                    2.dp,
                    if (seleccionado) DialogColors.NeonPrimary else Color.Gray,
                    CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (scale > 0f) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp).scale(scale).graphicsLayer { this.alpha = alpha },
            )
        }
    }
}

@Composable
private fun GalacticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    maxLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                cursorColor = DialogColors.NeonPrimary,
                focusedLabelColor = DialogColors.NeonPrimary,
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = DialogColors.NeonPrimary,
                unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f),
            ),
    )
}

// ==================== PREVIEWS ====================

@Preview(name = "1. Selector de Listas")
@Composable
private fun PreviewVentanaListas() {
    val listasMock =
        listOf(
            PlaylistEntity(idLista = 1, idUsuario = 1, nombre = "Favoritos", totalCanciones = 120),
            PlaylistEntity(
                idLista = 2,
                idUsuario = 1,
                nombre = "Gym Motivation",
                totalCanciones = 45,
            ),
            PlaylistEntity(idLista = 3, idUsuario = 1, nombre = "Chill Vibes", totalCanciones = 20),
        )

    // Necesitamos un Surface oscuro detrás para ver el diálogo correctamente en preview
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        PlaylistDialog(
            listasExistentes = listasMock,
            onDismiss = {},
            onCrearLista = { _, _, _ -> },
            onAnadirAListas = {},
        )
    }
}

@Preview(name = "2. Crear Lista")
@Composable
private fun PreviewCrearLista() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        DialogoCrearLista(onDismiss = {}, onCrear = { _, _, _ -> })
    }
}
