package com.example.freeplayerm.ui.features.biblioteca.components.items

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import androidx.compose.foundation.background
import androidx.compose.material3.Text

/**
 * 💿 ITEM DE ÁLBUM ESTILO VINILO
 *
 * Componente visual que muestra un álbum como vinilo saliendo de su funda.
 *
 * Características:
 * - Diseño de vinilo realista
 * - Disco que asoma de la funda
 * - Etiqueta central con portada
 * - Click handler
 */
@Composable
fun ItemAlbumVinilo(
    album: AlbumEntity,
    alClick: (AlbumEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val tamanioFunda = 250.dp
    val tamanioDisco = 165.dp
    val desplazamientoDisco = 70.dp
    val rotacionGrados = 0f

    val anchoTotal = tamanioFunda + desplazamientoDisco
    val altoTotal = maxOf(tamanioFunda, tamanioDisco)

    Box(
        modifier = modifier
            .width(anchoTotal)
            .height(altoTotal)
            .rotate(rotacionGrados)
            .clickable { alClick(album) },
        contentAlignment = Alignment.CenterStart
    ) {
        // CAPA 1: DISCO (Fondo)
        Box(
            modifier = Modifier
                .padding(start = desplazamientoDisco)
                .size(tamanioDisco),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.img_disco_vinilo_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        0.dp,
                        CircleShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    ),
                contentScale = ContentScale.Fit
            )

            // Etiqueta central
            Box(
                modifier = Modifier
                    .size(tamanioDisco * 0.25f)
                    .clip(CircleShape)
            ) {
                if (!album.portadaPath.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.portadaPath)
                            .crossfade(300)
                            .memoryCacheKey(album.portadaPath)
                            .build(),
                        contentDescription = "Etiqueta ${album.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.ic_notification)
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(0xFFD500F9))
                    )
                }
            }
        }

        // CAPA 2: FUNDA (Frente)
        Box(
            modifier = Modifier.size(tamanioFunda),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.img_funda_vacia_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Portada dentro de la funda
            if (!album.portadaPath.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.portadaPath)
                        .crossfade(300)
                        .memoryCacheKey(album.portadaPath)
                        .build(),
                    contentDescription = "Portada ${album.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                // Placeholder texto si no hay imagen
                Text(
                    text = album.titulo.take(1).uppercase(),
                    fontSize = 70.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 💿 VINILO GIRATORIO ANIMADO
 *
 * Componente para el reproductor que muestra un disco girando.
 *
 * @param estaReproduciendo Si true, el disco gira infinitamente
 */
@Composable
fun ViniloGiratorio(
    cancion: CancionConArtista,
    estaReproduciendo: Boolean,
    modifier: Modifier = Modifier
) {
    // ✅ Animación solo cuando reproduce
    val rotation by animateFloatAsState(
        targetValue = if (estaReproduciendo) 360f * 1000 else 0f,
        animationSpec = if (estaReproduciendo) {
            infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(durationMillis = 500)
        },
        label = "ViniloRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Disco de vinilo girando
        Image(
            painter = painterResource(id = R.mipmap.img_disco_vinilo_foreground),
            contentDescription = "Disco de vinilo",
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation),
            contentScale = ContentScale.Fit
        )

        // Etiqueta central (portada)
        Box(
            modifier = Modifier
                .fillMaxSize(0.25f)
                .clip(CircleShape)
                .background(Color(0xFF2A0F35))
        ) {
            if (cancion.portadaPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cancion.portadaPath)
                        .crossfade(300)
                        .memoryCacheKey(cancion.portadaPath)
                        .build(),
                    contentDescription = "Portada ${cancion.cancion.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFD500F9),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

private object AlbumItemMocks {
    val albumConPortada = AlbumEntity(
        idAlbum = 1,
        idArtista = 1,
        titulo = "Random Access Memories",
        anio = 2013,
        portadaPath = "https://example.com/album.jpg"
    )

    val albumSinPortada = AlbumEntity(
        idAlbum = 2,
        idArtista = 1,
        titulo = "Discovery",
        anio = 2001,
        portadaPath = null
    )
}

@Preview(name = "Álbum con portada", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewAlbumConPortada() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ItemAlbumVinilo(
                album = AlbumItemMocks.albumConPortada,
                alClick = {}
            )
        }
    }
}

@Preview(name = "Álbum sin portada", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewAlbumSinPortada() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ItemAlbumVinilo(
                album = AlbumItemMocks.albumSinPortada,
                alClick = {}
            )
        }
    }
}

@Preview(name = "Vinilo girando", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewViniloGiratorio() {
    FreePlayerMTheme {
        val mockCancion = CancionConArtista(
            cancion = CancionEntity(
                idCancion = 1,
                idArtista = 1,
                idAlbum = 1,
                titulo = "Test",
                duracionSegundos = 240,
                origen = "LOCAL",
                archivoPath = null,
                idGenero = null
            ),
            artistaNombre = "Test Artist",
            albumNombre = null,
            generoNombre = null,
            esFavorita = false,
            portadaPath = null,
            fechaLanzamiento = null
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            ViniloGiratorio(
                cancion = mockCancion,
                estaReproduciendo = true
            )
        }
    }
}