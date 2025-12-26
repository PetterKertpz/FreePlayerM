package com.example.freeplayerm.ui.features.biblioteca.components.items

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.ui.features.biblioteca.utils.GeneroVisuals
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎸 ITEM DE GÉNERO PARA GRID
 *
 * Componente que muestra géneros con imagen de fondo temática.
 *
 * Características:
 * - Imagen de fondo según el género
 * - Gradiente oscuro para legibilidad
 * - Nombre del género destacado
 * - Aspect ratio 1.5:1 (ancho:alto)
 */
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
        // ✅ IMAGEN DE FONDO
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

        // ✅ GRADIENTE OSCURO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // ✅ NOMBRE DEL GÉNERO
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

// ==================== PREVIEWS ====================

private object GeneroItemMocks {
    val generosComunes = listOf(
        GeneroEntity(idGenero = 1, nombre = "Rock"),
        GeneroEntity(idGenero = 2, nombre = "Pop"),
        GeneroEntity(idGenero = 3, nombre = "Jazz"),
        GeneroEntity(idGenero = 4, nombre = "Electronic"),
        GeneroEntity(idGenero = 5, nombre = "Hip Hop"),
        GeneroEntity(idGenero = 6, nombre = "Reggae")
    )
}

@Preview(name = "Light - Rock", showBackground = true)
@Preview(name = "Dark - Rock", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGeneroRock() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .width(200.dp)
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            ItemGeneroGalactico(
                genero = GeneroItemMocks.generosComunes[0],
                onClick = {}
            )
        }
    }
}

@Preview(name = "Grid de géneros", showBackground = true, widthDp = 400, heightDp = 500)
@Composable
private fun PreviewGridGeneros() {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0518))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GeneroItemMocks.generosComunes.take(2).forEach { genero ->
                        ItemGeneroGalactico(
                            genero = genero,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GeneroItemMocks.generosComunes.drop(2).take(2).forEach { genero ->
                        ItemGeneroGalactico(
                            genero = genero,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}