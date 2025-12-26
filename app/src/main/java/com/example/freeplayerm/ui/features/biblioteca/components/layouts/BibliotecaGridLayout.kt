package com.example.freeplayerm.ui.features.biblioteca.components.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.biblioteca.domain.BibliotecaItem

/**
 * 📐 LAYOUT GENÉRICO PARA GRIDS
 *
 * Componente reutilizable para todas las vistas de grid:
 * - Álbumes
 * - Artistas
 * - Géneros
 *
 * Características:
 * - Columnas adaptativas al tamaño de pantalla
 * - Estados vacíos integrados
 * - Performance optimizado
 */
@Composable
fun <T : BibliotecaItem> BibliotecaGridLayout(
    items: List<T>,
    gridState: LazyGridState,
    minItemSize: Dp = 160.dp,
    searchQuery: String = "",
    emptyMessage: String = "No hay elementos disponibles",
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 24.dp,
    horizontalSpacing: Dp = 16.dp,
    itemContent: @Composable (T) -> Unit
) {
    when {
        // Estado: Búsqueda sin resultados
        items.isEmpty() && searchQuery.isNotBlank() -> {
            EmptySearchStateGrid(
                query = searchQuery,
                modifier = modifier
            )
        }

        // Estado: Grid vacío
        items.isEmpty() -> {
            EmptyGridState(
                message = emptyMessage,
                modifier = modifier
            )
        }

        // Estado: Contenido disponible
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minItemSize),
                state = gridState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                modifier = modifier.fillMaxSize()
            ) {
                items(
                    items = items,
                    key = { it.id }, // ✅ KEY ESTABLE
                    contentType = { it::class } // ✅ RECICLAJE OPTIMIZADO
                ) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

// ==================== ESTADOS VACÍOS ====================

@Composable
private fun EmptySearchStateGrid(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White.copy(alpha = 0.3f)
            )
            Text(
                text = "Sin resultados para",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "\"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyGridState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            Text(
                text = "📂",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = 0.3f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}