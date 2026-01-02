package com.example.freeplayerm.ui.features.library.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freeplayerm.R
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.ui.features.library.utils.GenreVisuals

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
fun ItemGenero(genero: GenreEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
    ) {
        // ✅ IMAGEN DE FONDO
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(GenreVisuals.getImageForGenre(genero.nombre))
                    .crossfade(300)
                    .memoryCacheKey("genre_${genero.nombre}")
                    .build(),
            contentDescription = "Imagen de ${genero.nombre}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_notification),
        )

        // ✅ GRADIENTE OSCURO
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
        )

        // ✅ NOMBRE DEL GÉNERO
        Text(
            text = genero.nombre,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
        )
    }
}

// ==========================================
// PREVIEWS (Componente Aislado)
// ==========================================

// --- FAKE DATA (Provider) ---
/**
 * Provee diferentes longitudes de nombres de géneros para probar el ajuste del texto y el layout.
 */
class GenreProvider : PreviewParameterProvider<GenreEntity> {
    override val values =
        sequenceOf(
            // 1. Estándar
            GenreEntity(idGenero = 1, nombre = "Rock"),
            // 2. Texto Corto
            GenreEntity(idGenero = 2, nombre = "Pop"),
            // 3. Texto Largo (Stress Test)
            GenreEntity(idGenero = 3, nombre = "Alternative & Indie"),
            // 4. Texto Muy Largo (Multiline Check)
            GenreEntity(idGenero = 4, nombre = "Rhythm and Blues / Soul"),
        )
}

/** Wrapper consistente con el tema Galaxia Oscura. */
@Composable
private fun GenreItemWrapper(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.wrapContentSize() // Se ajusta al contenido
                .background(Color(0xFF050010)) // Fondo base unificado
                .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// --- CASO 1: Iteración de Géneros (Individual) ---
// Genera automáticamente una preview por cada elemento del Provider
@Preview(name = "Variaciones de Género", group = "Estados")
@Composable
fun PreviewGenreItem(@PreviewParameter(GenreProvider::class) genero: GenreEntity) {
    GenreItemWrapper {
        // Asignamos un ancho fijo solo para la preview individual
        // para simular cómo se vería en una columna del grid
        Box(modifier = Modifier.width(180.dp)) { ItemGenero(genero = genero, onClick = {}) }
    }
}

// --- CASO 2: Grid Context (Layout) ---
// Simula 2 columnas para verificar el espaciado y aspect ratio conjunto
@Preview(name = "Contexto Grid (2 Columnas)", group = "Layout", widthDp = 400)
@Composable
fun PreviewGenreGridContext() {
    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF050010)).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Columna 1
            ItemGenero(
                genero = GenreEntity(1, "Electronic"),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            // Columna 2
            ItemGenero(
                genero = GenreEntity(2, "Classical"),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
