package com.example.freeplayerm.ui.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.ItemGenero
import com.example.freeplayerm.ui.features.library.components.layouts.LibraryGridLayout
import com.example.freeplayerm.ui.features.library.domain.toItem
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎸 CONTENIDO DE GÉNEROS CON DISEÑO MEJORADO
 *
 * Características: ✨ Usa LibraryGridLayout con espaciado adaptado para tarjetas anchas 🎨 Datos
 * de prueba con emojis y colores reales 📱 Soporte para jerarquías visuales (Principal vs
 * Subgénero)
 */
@Composable
fun GenresContent(
    generos: List<GenreEntity>,
    lazyGridState: LazyGridState,
    nivelZoom: NivelZoom = NivelZoom.NORMAL,
    onZoomChange: (NivelZoom) -> Unit = {},
    onGeneroClick: (GenreEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ✅ Convertir entidades a items de UI
    val items by remember(generos) { derivedStateOf { generos.map { it.toItem() } } }

    // 🎨 Layout para Géneros (Suelen ser tarjetas más anchas o llamativas)
    LibraryGridLayout(
        items = items,
        gridState = lazyGridState,
        nivelZoom = nivelZoom,
        onZoomChange = onZoomChange,
        emptyMessage = "No hay géneros disponibles",
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalSpacing = 12.dp, // Menos espacio vertical para agrupar mejor
        horizontalSpacing = 12.dp,
    ) { generoItem ->
        // 🌈 Visualización Galáctica del Género
        ItemGenero(
            genero = generoItem.genero,
            onClick = { onGeneroClick(generoItem.genero) },
        )
    }
}

// ==================== PREVIEWS & FAKE DATA ====================

/** 🎸 Provider de datos falsos para Previews de Géneros */
class GenresProvider : PreviewParameterProvider<List<GenreEntity>> {

    private val emptyList = emptyList<GenreEntity>()

    // 🌈 Lista poblada con datos realistas y jerarquía
    private val populatedList =
        listOf(
            createFakeGenre(1, "Rock", "🎸", "#E74C3C", "Alternative, Indie, Classic", true),
            createFakeGenre(2, "Pop", "🎤", "#3498DB", "Dance Pop, Synth", true),
            createFakeGenre(3, "Hip Hop", "🎧", "#9B59B6", "Trap, Old School", true),
            createFakeGenre(4, "Jazz", "🎷", "#F39C12", "Smooth, Bebop", false),
            createFakeGenre(5, "Electronic", "🎹", "#1ABC9C", "House, Techno, Trance", true),
            createFakeGenre(6, "Metal", "🤘", "#2C3E50", "Heavy, Thrash, Doom", true),
            // Subgénero ejemplo
            createFakeGenre(7, "Heavy Metal", "💀", "#2C3E50", null, false, padreId = 6),
            createFakeGenre(8, "Indie", "🎨", "#7F8C8D", "Lo-Fi, Bedroom Pop", false),
        )

    override val values = sequenceOf(populatedList, emptyList)

    /** 🛠️ Helper para crear instancias válidas de GenreEntity */
    private fun createFakeGenre(
        id: Int,
        nombre: String,
        emoji: String,
        color: String,
        subgeneros: String?,
        popular: Boolean,
        padreId: Int? = null,
    ): GenreEntity {
        return GenreEntity(
            idGenero = id,
            nombre = nombre,
            emoji = emoji,
            color = color,
            subgeneros = subgeneros,
            esPopular = popular,
            generoPadreId = padreId,
            totalCanciones = (20..500).random(),
            totalArtistas = (5..50).random(),
            descripcion = "Descripción generada para el género $nombre...",
            fechaAgregado = System.currentTimeMillis(),
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(
    name = "🎸 Grid Normal - Biblioteca Poblada",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun PreviewGenerosNormal(
    @PreviewParameter(GenresProvider::class) generos: List<GenreEntity>
) {
    val data = generos.ifEmpty { GenresProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            GenresContent(
                generos = data,
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onGeneroClick = {},
            )
        }
    }
}

@Preview(
    name = "🔍 Zoom Pequeño - Compacto",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun PreviewGenerosSmall() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            GenresContent(
                generos = GenresProvider().values.first(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.PEQUENO,
                onZoomChange = {},
                onGeneroClick = {},
            )
        }
    }
}

@Preview(
    name = "📭 Estado Vacío",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun PreviewGenerosEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            GenresContent(
                generos = emptyList(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onGeneroClick = {},
            )
        }
    }
}
