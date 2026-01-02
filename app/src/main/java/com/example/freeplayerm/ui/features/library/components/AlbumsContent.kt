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
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.ItemAlbum
import com.example.freeplayerm.ui.features.library.components.layouts.LibraryGridLayout
import com.example.freeplayerm.ui.features.library.domain.toItem
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 💿 CONTENIDO DE ÁLBUMES CON DISEÑO MEJORADO
 *
 * Características: ✨ Usa LibraryGridLayout genérico con animaciones 🎯 Tamaño optimizado para
 * mostrar 3+ álbumes en pantalla 🎨 Gestos de zoom suaves (un nivel por gesto) 📱 Responsive con
 * estados vacíos integrados ⚡ Performance optimizado con derivedStateOf
 */
@Composable
fun AlbumsContent(
    albumes: List<AlbumEntity>,
    lazyGridState: LazyGridState,
    nivelZoom: NivelZoom = NivelZoom.NORMAL,
    onZoomChange: (NivelZoom) -> Unit = {},
    onAlbumClick: (AlbumEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ✅ Convertir entidades a items de UI (optimizado con derivedStateOf)
    val items by remember(albumes) { derivedStateOf { albumes.map { it.toItem() } } }

    // 🎨 Usar layout genérico mejorado con animaciones
    LibraryGridLayout(
        items = items,
        gridState = lazyGridState,
        minItemSize = 150.dp, // 🎯 Optimizado para ItemAlbumVinilo compacto (175dp)
        nivelZoom = nivelZoom,
        onZoomChange = onZoomChange,
        emptyMessage = "No hay álbumes en tu biblioteca",
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalSpacing = 20.dp,
        horizontalSpacing = 12.dp,
    ) { albumItem ->
        // 💿 Visualización de álbum con diseño vinilo compacto
        ItemAlbum(album = albumItem.album, alClick = onAlbumClick)
    }
}

// ==================== PREVIEWS & FAKE DATA ====================

/**
 * 💿 Provider de datos falsos para Previews Genera datos basados estrictamente en la estructura de
 * AlbumEntity v2.0
 */
class AlbumsProvider : PreviewParameterProvider<List<AlbumEntity>> {

    // 📭 Lista vacía
    private val emptyList = emptyList<AlbumEntity>()

    // 📚 Lista poblada con variedad de casos (Single, Live, EP, Album)
    private val populatedList =
        listOf(
            createFakeAlbum(
                id = 1,
                idArtista = 101,
                titulo = "Random Access Memories",
                anio = 2013,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Electronic",
                totalTracks = 13,
                subtitulo = "10th Anniversary Edition",
                esEdicionEspecial = true,
            ),
            createFakeAlbum(
                id = 2,
                idArtista = 102,
                titulo = "The Dark Side of the Moon",
                anio = 1973,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Progressive Rock",
                totalTracks = 10,
            ),
            createFakeAlbum(
                id = 3,
                idArtista = 103,
                titulo = "Abbey Road",
                anio = 1969,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Rock",
                totalTracks = 17,
            ),
            createFakeAlbum(
                id = 4,
                idArtista = 104,
                titulo = "AM",
                anio = 2013,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Indie Rock",
                totalTracks = 12,
            ),
            createFakeAlbum(
                id = 5,
                idArtista = 105,
                titulo = "Future Nostalgia",
                anio = 2020,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Pop",
                totalTracks = 11,
            ),
            createFakeAlbum(
                id = 6,
                idArtista = 106,
                titulo = "Thriller",
                anio = 1982,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Pop",
                totalTracks = 9,
            ),
            // 🎸 Ejemplo En Vivo
            createFakeAlbum(
                id = 7,
                idArtista = 101,
                titulo = "Alive 2007",
                anio = 2007,
                tipo = AlbumEntity.TIPO_LIVE,
                genero = "Electronic",
                totalTracks = 12,
                esEnVivo = true,
            ),
            createFakeAlbum(
                id = 8,
                idArtista = 108,
                titulo = "The Eminem Show",
                anio = 2002,
                tipo = AlbumEntity.TIPO_ALBUM,
                genero = "Hip Hop",
                totalTracks = 20,
            ),
            // 🎵 Ejemplo Single
            createFakeAlbum(
                id = 9,
                idArtista = 109,
                titulo = "Flowers",
                anio = 2023,
                tipo = AlbumEntity.TIPO_SINGLE,
                genero = "Pop",
                totalTracks = 1,
            ),
            // 💿 Ejemplo EP
            createFakeAlbum(
                id = 10,
                idArtista = 110,
                titulo = "The Velvet EP",
                anio = 2024,
                tipo = AlbumEntity.TIPO_EP,
                genero = "R&B",
                totalTracks = 5,
            ),
        )

    override val values = sequenceOf(populatedList, emptyList)

    /**
     * 🛠️ Helper para crear instancias válidas de AlbumEntity Mapea los parámetros a la estructura
     * completa de la entidad
     */
    private fun createFakeAlbum(
        id: Int,
        idArtista: Int,
        titulo: String,
        anio: Long,
        tipo: String,
        genero: String,
        totalTracks: Int,
        subtitulo: String? = null,
        esEnVivo: Boolean = false,
        esEdicionEspecial: Boolean = false,
    ): AlbumEntity {
        return AlbumEntity(
            idAlbum = id,
            idArtista = idArtista,
            titulo = titulo,
            tituloNormalizado = titulo.lowercase(),
            subtitulo = subtitulo,
            anio = anio,
            tipo = tipo,
            generoPrincipal = genero,
            totalCanciones = totalTracks,
            esEnVivo = esEnVivo,
            esEdicionEspecial = esEdicionEspecial,
            // Valores por defecto para preview
            portadaPath = null,
            fechaAgregado = System.currentTimeMillis(),
            totalReproducciones = (10..500).random(),
            calificacionPromedio = (3..5).random().toFloat(),
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(
    name = "📚 Grid Normal - Biblioteca Poblada",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun PreviewAlbumsNormal(
    @PreviewParameter(AlbumsProvider::class) albumes: List<AlbumEntity>
) {
    // Usamos el primer valor del provider (lista poblada) si la inyección falla en IDE
    val data = albumes.ifEmpty { AlbumsProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            AlbumsContent(
                albumes = data,
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onAlbumClick = {},
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
private fun PreviewAlbumsZoomPequeno() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            AlbumsContent(
                albumes = AlbumsProvider().values.first(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.PEQUENO,
                onZoomChange = {},
                onAlbumClick = {},
            )
        }
    }
}

@Preview(
    name = "🔎 Zoom Grande - Detalle",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun PreviewAlbumsZoomGrande() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            AlbumsContent(
                albumes = AlbumsProvider().values.first(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.GRANDE,
                onZoomChange = {},
                onAlbumClick = {},
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
private fun PreviewAlbumsEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            AlbumsContent(
                albumes = emptyList(),
                lazyGridState = rememberLazyGridState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onAlbumClick = {},
            )
        }
    }
}
