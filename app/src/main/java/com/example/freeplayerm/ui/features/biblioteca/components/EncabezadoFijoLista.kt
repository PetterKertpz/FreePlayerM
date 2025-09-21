package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun EncabezadoFijoLista(
    lista: ListaReproduccionEntity?,
    onVolverClick: () -> Unit,
    onEliminarListaClick: () -> Unit,
    onEditarListaClick: () -> Unit
) {
    if (lista == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón para volver a la lista de playlists
        IconButton(onClick = onVolverClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a listas")
        }

        // Portada
        AsyncImage(
            model = lista.portadaUrl,
            contentDescription = "Portada de ${lista.nombre}",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.GrisProfundo),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Título y descripción
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lista.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.basicMarquee()
            )
            if (!lista.descripcion.isNullOrBlank()) {
                Text(
                    text = lista.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.basicMarquee(),
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onEditarListaClick) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Editar lista de reproducción"
            )
        }
        // Botón para eliminar la lista completa
        IconButton(onClick = onEliminarListaClick) {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = "Eliminar lista de reproducción",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}