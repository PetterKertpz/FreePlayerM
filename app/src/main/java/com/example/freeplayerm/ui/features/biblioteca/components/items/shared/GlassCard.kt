package com.example.freeplayerm.ui.features.biblioteca.components.items.shared

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 🎴 TARJETA BASE CON EFECTO CRISTAL
 *
 * Componente compartido por todos los items de lista.
 * Provee el efecto visual glassmorphism y manejo de gestos.
 *
 * Características:
 * - Gradiente de fondo semi-transparente
 * - Soporte para modo selección
 * - Click y long press unificados
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaCristal(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seleccionado: Boolean = false,
    onLongClick: (() -> Unit)? = null,
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}