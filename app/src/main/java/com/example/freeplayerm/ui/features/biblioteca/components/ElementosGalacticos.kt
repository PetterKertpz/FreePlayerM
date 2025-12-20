package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.*
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.utils.GeneroVisuals
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==========================================
// 1. BASE: TARJETA DE CRISTAL
// ==========================================
@Composable
fun TarjetaCristal(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seleccionado: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundBrush = if (seleccionado) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFD500F9).copy(alpha = 0.3f),
                Color(0xFFD500F9).copy(alpha = 0.1f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF1E1E1E).copy(alpha = 0.4f),
                Color(0xFF0F0518).copy(alpha = 0.2f)
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// ==========================================
// 2. ITEM DE CANCIÓN (Lista) - OPTIMIZADO
// ==========================================
@Composable
fun ItemCancionGalactico(
    cancion: CancionConArtista,
    esSeleccionado: Boolean,
    alClick: () -> Unit,
    alClickMasOpciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaCristal(
        onClick = alClick,
        seleccionado = esSeleccionado,
        modifier = modifier
    ) {
        // ✅ OPTIMIZACIÓN: Portada con placeholder y error handling
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center
        ) {
            if (cancion.portadaPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cancion.portadaPath)
                        .crossfade(300)
                        .memoryCacheKey(cancion.portadaPath)
                        .build(),
                    contentDescription = "Portada de ${cancion.cancion.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.ic_notification),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cancion.cancion.titulo,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cancion.artistaNombre ?: "Desconocido",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = alClickMasOpciones) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

// ==========================================
// 3. ITEM ÁLBUM VINILO (Grid) - ⚡ CORREGIDO
// ==========================================
@Composable
fun ItemAlbumVinilo(
    album: AlbumEntity, // ✅ CORRECCIÓN: Parámetro agregado
    alClick: (AlbumEntity) -> Unit, // ✅ CORRECCIÓN: Click con parámetro
    modifier: Modifier = Modifier
) {
    val tamañoFunda = 250.dp
    val tamañoDisco = 165.dp
    val desplazamientoDisco = 70.dp
    val rotacionGrados = 0f

    val anchoTotal = tamañoFunda + desplazamientoDisco
    val altoTotal = maxOf(tamañoFunda, tamañoDisco)

    Box(
        modifier = modifier
            .width(anchoTotal)
            .height(altoTotal)
            .rotate(rotacionGrados)
            .clickable { alClick(album) }, // ✅ CORRECCIÓN: Pasar album
        contentAlignment = Alignment.CenterStart
    ) {
        // CAPA 1: DISCO (Fondo)
        Box(
            modifier = Modifier
                .padding(start = desplazamientoDisco)
                .size(tamañoDisco),
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
                    .size(tamañoDisco * 0.25f)
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
            modifier = Modifier.size(tamañoFunda),
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

// ==========================================
// 4. VINILO GIRATORIO ANIMADO - ⚡ OPTIMIZADO
// ==========================================
@Composable
fun ViniloGiratorio(
    cancion: CancionConArtista,
    estaReproduciendo: Boolean,
    modifier: Modifier = Modifier
) {
    // ✅ OPTIMIZACIÓN: Animación solo cuando reproduce
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
                .fillMaxSize(0.35f)
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

// ==========================================
// 5. ITEM ARTISTA (Grid) - OPTIMIZADO
// ==========================================
@Composable
fun ItemArtistaGalactico(
    artista: ArtistaEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD500F9),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            // ✅ OPTIMIZACIÓN: Avatar optimizado
            val urlFoto =
                "https://ui-avatars.com/api/?name=${artista.nombre}&background=random&color=fff&size=256"
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urlFoto)
                    .crossfade(300)
                    .memoryCacheKey(artista.nombre)
                    .build(),
                contentDescription = "Foto de ${artista.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = artista.nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================
// 6. ITEM GÉNERO (Grid) - OPTIMIZADO
// ==========================================
@Composable
fun ItemGeneroGalactico(
    genero: GeneroEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(GeneroVisuals.getImageForGenre(genero.nombre))
                .crossfade(300)
                .memoryCacheKey("genre_${genero.nombre}")
                .build(),
            contentDescription = "Imagen de ${genero.nombre}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_notification)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Text(
            text = genero.nombre,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

// ==========================================
// 7. ITEM LISTA DE REPRODUCCIÓN - OPTIMIZADO
// ==========================================
@Composable
fun ItemListaGalactico(
    lista: ListaReproduccionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaCristal(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A0F35)),
            contentAlignment = Alignment.Center
        ) {
            if (!lista.portadaUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(lista.portadaUrl)
                        .crossfade(300)
                        .memoryCacheKey(lista.portadaUrl)
                        .build(),
                    contentDescription = "Portada de ${lista.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_notification)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = Color(0xFFD500F9).copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = lista.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (!lista.descripcion.isNullOrBlank()) {
                Text(
                    text = lista.descripcion,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Lista local",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==========================================
// ✅ PREVIEWS COMPLETAS
// ==========================================

// Datos mock para previews
private object MocksVisuales {
    val cancionBase = CancionEntity(
        idCancion = 1,
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        titulo = "Midnight City",
        duracionSegundos = 240,
        origen = "LOCAL",
        archivoPath = null
    )
    val cancionConArtista = CancionConArtista(
        cancion = cancionBase,
        artistaNombre = "M83",
        albumNombre = "Hurry Up, We're Dreaming",
        generoNombre = "Synthpop",
        esFavorita = true,
        portadaPath = null,
        fechaLanzamiento = null
    )
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
    val artista = ArtistaEntity(
        idArtista = 1,
        nombre = "Daft Punk",
        paisOrigen = "France",
        descripcion = "Legendary Duo"
    )
    val genero = GeneroEntity(idGenero = 1, nombre = "Synthwave")
    val lista = ListaReproduccionEntity(
        idLista = 1,
        idUsuario = 1,
        nombre = "Viaje Espacial",
        descripcion = "Música para programar",
        portadaUrl = null
    )
}

@Composable
private fun PreviewGalaxiaContext(content: @Composable () -> Unit) {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0518))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

// ==========================================
// ✅ PREVIEWS - LIGHT & DARK MODE
// ==========================================

@Preview(name = "Canción - Light", showBackground = true)
@Preview(name = "Canción - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewItemCancion() {
    PreviewGalaxiaContext {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ItemCancionGalactico(
                cancion = MocksVisuales.cancionConArtista,
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {}
            )
            ItemCancionGalactico(
                cancion = MocksVisuales.cancionConArtista.copy(
                    cancion = MocksVisuales.cancionBase.copy(titulo = "Canción Seleccionada")
                ),
                esSeleccionado = true,
                alClick = {},
                alClickMasOpciones = {}
            )
        }
    }
}

@Preview(name = "Álbum Vinilo - Con Portada")
@Preview(name = "Álbum Vinilo - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAlbumVinilo() {
    PreviewGalaxiaContext {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemAlbumVinilo(
                album = MocksVisuales.albumConPortada,
                alClick = {}
            )
        }
    }
}

@Preview(name = "Vinilo Giratorio - Reproduciendo")
@Composable
fun PreviewViniloGiratorio() {
    PreviewGalaxiaContext {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ViniloGiratorio(
                cancion = MocksVisuales.cancionConArtista,
                estaReproduciendo = true,
                modifier = Modifier.size(120.dp)
            )
            ViniloGiratorio(
                cancion = MocksVisuales.cancionConArtista,
                estaReproduciendo = false,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}

@Preview(name = "Artista")
@Preview(name = "Artista - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewItemArtista() {
    PreviewGalaxiaContext {
        ItemArtistaGalactico(
            artista = MocksVisuales.artista,
            onClick = {}
        )
    }
}

@Preview(name = "Género")
@Preview(name = "Género - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewItemGenero() {
    PreviewGalaxiaContext {
        Box(Modifier.width(200.dp)) {
            ItemGeneroGalactico(
                genero = MocksVisuales.genero,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Lista de Reproducción")
@Preview(name = "Lista - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewItemLista() {
    PreviewGalaxiaContext {
        ItemListaGalactico(
            lista = MocksVisuales.lista,
            onClick = {}
        )
    }
}

@Preview(name = "Todos los Items", showBackground = true)
@Composable
fun PreviewTodosLosItems() {
    FreePlayerMTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFF0F0518))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("CANCIÓN:", color = Color.White, fontWeight = FontWeight.Bold)
            ItemCancionGalactico(
                cancion = MocksVisuales.cancionConArtista,
                esSeleccionado = false,
                alClick = {},
                alClickMasOpciones = {}
            )

            Spacer(Modifier.height(8.dp))
            Text("LISTA:", color = Color.White, fontWeight = FontWeight.Bold)
            ItemListaGalactico(
                lista = MocksVisuales.lista,
                onClick = {}
            )

            Spacer(Modifier.height(8.dp))
            Text("ARTISTA:", color = Color.White, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ItemArtistaGalactico(
                    artista = MocksVisuales.artista,
                    onClick = {}
                )
            }
        }
    }
}