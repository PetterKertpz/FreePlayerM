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
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.ItemArtista
import com.example.freeplayerm.ui.features.library.components.layouts.LibraryGridLayout
import com.example.freeplayerm.ui.features.library.domain.toItem
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎤 CONTENIDO DE ARTISTAS CON DISEÑO MEJORADO
 *
 * Características: ✨ Usa LibraryGridLayout genérico 🎨 Tamaño optimizado para tarjetas de
 * artistas (Galactic Style) 📱 Datos enriquecidos para visualización (verificados, populares) ⚡
 * Performance optimizado
 */
@Composable
fun ArtistsContent(
    artistas: List<ArtistEntity>,
    lazyGridState: LazyGridState,
    nivelZoom: NivelZoom = NivelZoom.NORMAL,
    onZoomChange: (NivelZoom) -> Unit = {},
    onArtistaClick: (ArtistEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ✅ Convertir entidades a items de UI (optimizado)
    val items by remember(artistas) { derivedStateOf { artistas.map { it.toItem() } } }

    // 🎨 Usar layout genérico
    LibraryGridLayout(
        items = items,
        gridState = lazyGridState,
        nivelZoom = nivelZoom,
        onZoomChange = onZoomChange,
        emptyMessage = "No hay artistas en tu biblioteca",
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalSpacing = 20.dp,
        horizontalSpacing = 12.dp,
    ) { artistaItem ->
        // 🌟 Visualización con estilo Galáctico
        ItemArtista(
            artista = artistaItem.artista,
            onClick = { onArtistaClick(artistaItem.artista) },
        )
    }
}

// ==================== PREVIEWS & FAKE DATA ====================

/**
 * 🎤 Provider de datos falsos para Previews Genera datos basados estrictamente en ArtistEntity v2.0
 */
class ArtistsProvider : PreviewParameterProvider<List<ArtistEntity>> {

    // 📭 Lista vacía
    private val emptyList = emptyList<ArtistEntity>()

    // 🎸 Lista poblada con variedad de casos
    private val populatedList =
        listOf(
            createFakeArtist(
                id = 1,
                nombre = "Daft Punk",
                tipo = ArtistEntity.TIPO_DUO,
                pais = "France",
                generos = "Electronic, House",
                popular = true,
                verificado = true,
                totalTracks = 56,
                img = "https://example.com/dp.jpg",
            ),
            createFakeArtist(
                id = 2,
                nombre = "The Weeknd",
                tipo = ArtistEntity.TIPO_SOLISTA,
                pais = "Canada",
                generos = "R&B, Pop",
                popular = true,
                verificado = true,
                totalTracks = 84,
                img = "https://example.com/tw.jpg",
            ),
            createFakeArtist(
                id = 3,
                nombre = "Arctic Monkeys",
                tipo = ArtistEntity.TIPO_BANDA,
                pais = "UK",
                generos = "Indie Rock",
                popular = true,
                verificado = false,
                totalTracks = 45,
                img = null, // Sin imagen para probar placeholder
            ),
            createFakeArtist(
                id = 4,
                nombre = "Gorillaz",
                tipo = ArtistEntity.TIPO_BANDA,
                pais = "UK",
                generos = "Alternative",
                popular = false,
                verificado = true,
                totalTracks = 32,
                img = null,
            ),
            createFakeArtist(
                id = 5,
                nombre = "Dua Lipa",
                tipo = ArtistEntity.TIPO_SOLISTA,
                pais = "UK",
                generos = "Pop, Disco",
                popular = true,
                verificado = true,
                totalTracks = 40,
                img = "https://example.com/dl.jpg",
            ),
            createFakeArtist(
                id = 6,
                nombre = "Queen",
                tipo = ArtistEntity.TIPO_BANDA,
                pais = "UK",
                generos = "Rock",
                popular = true,
                verificado = true,
                totalTracks = 120,
                img = "https://example.com/queen.jpg",
            ),
            createFakeArtist(
                id = 7,
                nombre = "Various Artists",
                tipo = ArtistEntity.TIPO_VARIOS,
                pais = null,
                generos = "Compilations",
                popular = false,
                verificado = false,
                totalTracks = 15,
                img = null,
            ),
        )

    override val values = sequenceOf(populatedList, emptyList)

    /** 🛠️ Helper para crear instancias válidas de ArtistEntity */
    private fun createFakeArtist(
        id: Int,
        nombre: String,
        tipo: String,
        pais: String?,
        generos: String,
        popular: Boolean,
        verificado: Boolean,
        totalTracks: Int,
        img: String?,
    ): ArtistEntity {
        return ArtistEntity(
            idArtista = id,
            nombre = nombre,
            nombreNormalizado = nombre.lowercase(),
            tipo = tipo,
            paisOrigen = pais,
            generos = generos,
            esPopular = popular,
            esVerificado = verificado,
            totalCanciones = totalTracks,
            imageUrl = img,
            // Valores por defecto
            fechaAgregado = System.currentTimeMillis(),
            totalReproducciones = (100..5000).random(),
            totalAlbumes = (1..15).random(),
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
private fun PreviewArtistsNormal(
    @PreviewParameter(ArtistsProvider::class) artistas: List<ArtistEntity>
) {
    val data = artistas.ifEmpty { ArtistsProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            ArtistsContent(
                artistas = data,
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onArtistaClick = {},
            )
        }
    }
}

@Preview(
    name = "🔍 Zoom Pequeño - Más items",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun PreviewArtistsSmall() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            ArtistsContent(
                artistas = ArtistsProvider().values.first(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.PEQUENO,
                onZoomChange = {},
                onArtistaClick = {},
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
private fun PreviewArtistsEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            ArtistsContent(
                artistas = emptyList(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onArtistaClick = {},
            )
        }
    }
}
