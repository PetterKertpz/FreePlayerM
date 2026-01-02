package com.example.freeplayerm.ui.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.ui.features.library.NivelZoom
import com.example.freeplayerm.ui.features.library.components.items.ItemLista
import com.example.freeplayerm.ui.features.library.components.layouts.LibraryListLayout
import com.example.freeplayerm.ui.features.library.domain.toItem
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎵 CONTENIDO DE LISTAS DE REPRODUCCIÓN
 *
 * Características: ✨ Usa LibraryListLayout para una lista vertical fluida 🎨 Soporte para temas
 * de color por lista 📱 Datos enriquecidos (públicas, colaborativas, sistema)
 */
@Composable
fun PlaylistsContent(
    listas: List<PlaylistEntity>,
    lazyListState: LazyListState,
    nivelZoom: NivelZoom = NivelZoom.NORMAL,
    onZoomChange: (NivelZoom) -> Unit = {},
    onListaClick: (PlaylistEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ✅ Optimización de conversión
    val items by remember(listas) { derivedStateOf { listas.map { it.toItem() } } }

    // 📋 Layout de Lista (Ideal para Playlists con descripción)
    LibraryListLayout(
        items = items,
        listState = lazyListState,
        baseItemHeight = 80.dp, // 🎯 Un poco más alto para mostrar portadas y detalles
        nivelZoom = nivelZoom,
        onZoomChange = onZoomChange,
        emptyMessage = "Crea tu primera lista de reproducción",
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp), // Espacio para mini-player
        itemSpacing = 12.dp,
    ) { listaItem ->
        // 🌌 Item con diseño Galáctico
        ItemLista(lista = listaItem.lista, onClick = { onListaClick(listaItem.lista) })
    }
}

// ==================== PREVIEWS & FAKE DATA ====================

/** 🎵 Provider de datos falsos para Playlists Genera datos ricos basados en PlaylistEntity v2.0 */
class PlaylistsProvider : PreviewParameterProvider<List<PlaylistEntity>> {

    private val emptyList = emptyList<PlaylistEntity>()

    // 📋 Lista variada con casos de uso reales
    private val populatedList =
        listOf(
            // 1. Lista del Sistema (Favoritos)
            createFakePlaylist(
                id = 1,
                nombre = "Favoritos",
                desc = "Mis canciones top de siempre",
                total = 150,
                color = "#E91E63",
                esFavorita = true,
                categoria = null,
            ),
            // 2. Lista de Entrenamiento (Workout)
            createFakePlaylist(
                id = 2,
                nombre = "Gym Motivation",
                desc = "Para romperla en el gym 💪",
                total = 45,
                color = "#F44336",
                esFavorita = false,
                categoria = PlaylistEntity.CATEGORIA_WORKOUT,
            ),
            // 3. Lista para Estudiar (Focus)
            createFakePlaylist(
                id = 3,
                nombre = "Coding Mode",
                desc = "Lo-fi beats to code to ☕",
                total = 120,
                color = "#2196F3",
                esFavorita = true,
                categoria = PlaylistEntity.CATEGORIA_ESTUDIO,
            ),
            // 4. Lista de Viaje (Road Trip) - Pública y Colaborativa
            createFakePlaylist(
                id = 4,
                nombre = "Road Trip 2025",
                desc = "Clásicos del rock para la carretera",
                total = 80,
                color = "#FF9800",
                esFavorita = false,
                categoria = PlaylistEntity.CATEGORIA_VIAJE,
                esPublica = true,
                esColaborativa = true,
            ),
            // 5. Lista Chill (Sin descripción)
            createFakePlaylist(
                id = 5,
                nombre = "Sunday Chill",
                desc = null,
                total = 25,
                color = "#9C27B0",
                esFavorita = false,
                categoria = PlaylistEntity.CATEGORIA_RELAX,
            ),
            // 6. Lista Vacía (Recién creada)
            createFakePlaylist(
                id = 6,
                nombre = "Nueva Lista",
                desc = "Añade canciones...",
                total = 0,
                color = "#607D8B",
                esFavorita = false,
                categoria = PlaylistEntity.CATEGORIA_CUSTOM,
            ),
        )

    override val values = sequenceOf(populatedList, emptyList)

    /** 🛠️ Helper para crear instancias válidas de PlaylistEntity */
    private fun createFakePlaylist(
        id: Int,
        nombre: String,
        desc: String?,
        total: Int,
        color: String,
        esFavorita: Boolean,
        categoria: String?,
        esPublica: Boolean = false,
        esColaborativa: Boolean = false,
    ): PlaylistEntity {
        return PlaylistEntity(
            idLista = id,
            idUsuario = 1, // Mock user ID
            nombre = nombre,
            descripcion = desc,
            totalCanciones = total,
            // Simulación de portada solo si tiene canciones
            portadaUrl = if (total > 0) "https://fake.url/img$id.jpg" else null,
            duracionTotalSegundos = total * 210, // ~3.5 min promedio
            colorTema = color,
            esFavorita = esFavorita,
            categoria = categoria,
            esPublica = esPublica,
            esColaborativa = esColaborativa,
            fechaCreacion = System.currentTimeMillis(),
            fechaModificacion = System.currentTimeMillis(),
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(
    name = "📜 Lista Normal - Poblada",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    widthDp = 360,
    heightDp = 700,
)
@Composable
private fun PreviewPlaylistsNormal(
    @PreviewParameter(PlaylistsProvider::class) listas: List<PlaylistEntity>
) {
    val data = listas.ifEmpty { PlaylistsProvider().values.first() }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            PlaylistsContent(
                listas = data,
                lazyListState = rememberLazyListState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onListaClick = {},
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
private fun PreviewPlaylistsSmall() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            PlaylistsContent(
                listas = PlaylistsProvider().values.first(),
                lazyListState = rememberLazyListState(),
                nivelZoom = NivelZoom.PEQUENO,
                onZoomChange = {},
                onListaClick = {},
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
private fun PreviewPlaylistsEmpty() {
    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            PlaylistsContent(
                listas = emptyList(),
                lazyListState = rememberLazyListState(),
                nivelZoom = NivelZoom.NORMAL,
                onZoomChange = {},
                onListaClick = {},
            )
        }
    }
}
