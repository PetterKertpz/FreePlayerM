package com.example.freeplayerm.ui.features.biblioteca.components

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.ui.features.shared.MarqueeTextConDesvanecido
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * ✅ OPTIMIZADO: Encabezado fijo para vista de lista de reproducción
 *
 * Mejoras:
 * - Optimización de AsyncImage con cache y crossfade
 * - Mejor accesibilidad con semantics
 * - Manejo de estados null
 * - Previews completas
 */
@Composable
fun EncabezadoFijoLista(
    lista: ListaReproduccionEntity?,
    onVolverClick: () -> Unit,
    onEliminarListaClick: () -> Unit,
    onEditarListaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Early return si lista es null
    if (lista == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón para volver a la lista de playlists
        IconButton(
            onClick = onVolverClick
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver a listas de reproducción"
            )
        }

        // ✅ OPTIMIZADO: Portada con placeholder y cache
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(lista.portadaUrl)
                .crossfade(300)
                .memoryCacheKey(lista.portadaUrl)
                .diskCacheKey(lista.portadaUrl)
                .build(),
            contentDescription = "Portada de ${lista.nombre}",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.GrisProfundo),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_notification),
            error = painterResource(id = R.drawable.ic_notification)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Título y descripción
        Column(modifier = Modifier.weight(1f)) {
            MarqueeTextConDesvanecido(
                text = lista.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (!lista.descripcion.isNullOrBlank()) {
                MarqueeTextConDesvanecido(
                    text = lista.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón editar
        IconButton(
            onClick = onEditarListaClick
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Editar lista ${lista.nombre}",
                tint = Color(0xFFD500F9)
            )
        }

        // Botón eliminar
        IconButton(
            onClick = onEliminarListaClick
        ) {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = "Eliminar lista ${lista.nombre}",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ==========================================
// ✅ PREVIEWS COMPLETAS
// ==========================================

@Preview(name = "Light - Con descripción", showBackground = true)
@Preview(name = "Dark - Con descripción", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEncabezadoConDescripcion() {
    FreePlayerMTheme {
        EncabezadoFijoLista(
            lista = ListaReproduccionEntity(
                idLista = 1,
                idUsuario = 1,
                nombre = "Workout Mix 2024",
                descripcion = "Las mejores canciones para entrenar",
                portadaUrl = null
            ),
            onVolverClick = {},
            onEliminarListaClick = {},
            onEditarListaClick = {}
        )
    }
}

@Preview(name = "Sin descripción", showBackground = true)
@Composable
private fun PreviewEncabezadoSinDescripcion() {
    FreePlayerMTheme {
        EncabezadoFijoLista(
            lista = ListaReproduccionEntity(
                idLista = 2,
                idUsuario = 1,
                nombre = "Favoritos",
                descripcion = null,
                portadaUrl = null
            ),
            onVolverClick = {},
            onEliminarListaClick = {},
            onEditarListaClick = {}
        )
    }
}

@Preview(name = "Nombre largo", showBackground = true)
@Composable
private fun PreviewEncabezadoNombreLargo() {
    FreePlayerMTheme {
        EncabezadoFijoLista(
            lista = ListaReproduccionEntity(
                idLista = 3,
                idUsuario = 1,
                nombre = "Mi Lista Super Larga Con Muchos Caracteres Para Probar El Marquee",
                descripcion = "Descripción también muy larga para probar el comportamiento del texto cuando se desborda",
                portadaUrl = null
            ),
            onVolverClick = {},
            onEliminarListaClick = {},
            onEditarListaClick = {}
        )
    }
}

@Preview(name = "Estados múltiples", showBackground = true, heightDp = 400)
@Composable
private fun PreviewEncabezadoEstadosMultiples() {
    FreePlayerMTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            EncabezadoFijoLista(
                lista = ListaReproduccionEntity(
                    idLista = 1,
                    idUsuario = 1,
                    nombre = "Corta",
                    descripcion = "Breve",
                    portadaUrl = null
                ),
                onVolverClick = {},
                onEliminarListaClick = {},
                onEditarListaClick = {}
            )

            Spacer(modifier = Modifier.size(16.dp))

            EncabezadoFijoLista(
                lista = ListaReproduccionEntity(
                    idLista = 2,
                    idUsuario = 1,
                    nombre = "Sin descripción",
                    descripcion = null,
                    portadaUrl = null
                ),
                onVolverClick = {},
                onEliminarListaClick = {},
                onEditarListaClick = {}
            )

            Spacer(modifier = Modifier.size(16.dp))

            EncabezadoFijoLista(
                lista = ListaReproduccionEntity(
                    idLista = 3,
                    idUsuario = 1,
                    nombre = "Texto muy largo que debería activar marquee",
                    descripcion = "Descripción también larga para ver el comportamiento",
                    portadaUrl = null
                ),
                onVolverClick = {},
                onEliminarListaClick = {},
                onEditarListaClick = {}
            )
        }
    }
}