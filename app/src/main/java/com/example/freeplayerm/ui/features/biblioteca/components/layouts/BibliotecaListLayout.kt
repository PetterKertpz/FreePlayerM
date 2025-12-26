package com.example.freeplayerm.ui.features.biblioteca.components.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.biblioteca.domain.BibliotecaItem

/**
 * 📋 LAYOUT GENÉRICO PARA LISTAS VERTICALES
 *
 * Componente reutilizable para todas las vistas de lista:
 * - Canciones
 * - Listas de reproducción
 * - Cualquier contenido en lista vertical
 *
 * Maneja automáticamente:
 * - Estados vacíos
 * - Búsqueda sin resultados
 * - Scroll state persistence
 * - Keys estables para performance
 */
@Composable
fun <T : BibliotecaItem> BibliotecaListLayout(
    items: List<T>,
    listState: LazyListState,
    searchQuery: String = "",
    emptyMessage: String = "No hay elementos disponibles",
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 100.dp),
    itemContent: @Composable (T) -> Unit
) {
    when {
        // Estado: Búsqueda sin resultados
        items.isEmpty() && searchQuery.isNotBlank() -> {
            EmptySearchState(
                query = searchQuery,
                modifier = modifier
            )
        }

        // Estado: Lista vacía (sin búsqueda activa)
        items.isEmpty() -> {
            EmptyListState(
                message = emptyMessage,
                modifier = modifier
            )
        }

        // Estado: Contenido disponible
        else -> {
            LazyColumn(
                state = listState,
                contentPadding = contentPadding,
                modifier = modifier.fillMaxSize()
            ) {
                items(
                    items = items,
                    key = { it.id }, // ✅ KEY ESTABLE - Crucial para performance
                    contentType = { it::class } // ✅ RECICLAJE OPTIMIZADO
                ) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

// ==================== ESTADOS VACÍOS ====================

/**
 * Estado mostrado cuando la búsqueda no devuelve resultados
 */
@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White.copy(alpha = 0.3f)
            )
            Text(
                text = "No se encontraron resultados",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "para \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Estado mostrado cuando la lista está vacía (sin filtros)
 */
@Composable
private fun EmptyListState(
    message: String,
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
                text = "📭",
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