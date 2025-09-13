// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/IconosReproductor.kt
package com.example.freeplayerm.ui.features.reproductor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Shuffle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SortNumericDown

/**
 * Objeto que centraliza todos los iconos utilizados en los controles del reproductor.
 * Esto hace que sea más fácil cambiarlos en el futuro.
 */
object IconosReproductor {
    val Reproducir: ImageVector = Icons.Default.PlayArrow
    val Pausa: ImageVector = Icons.Default.Pause
    val Siguiente: ImageVector = Icons.Default.SkipNext
    val Anterior: ImageVector = Icons.Default.SkipPrevious
    val Detener: ImageVector = Icons.Default.Stop

    val Favorito: ImageVector = Icons.Default.Favorite
    val NoFavorito: ImageVector = Icons.Default.FavoriteBorder

    // Modos de reproducción
    val Aleatorio: ImageVector = FeatherIcons.Shuffle
    val EnOrden: ImageVector = FontAwesomeIcons.Solid.SortNumericDown // Representa orden numérico

    // Modos de repetición
    val NoRepetir: ImageVector = FeatherIcons.Repeat // Se puede mostrar "apagado" con alpha reducida
    val RepetirLista: ImageVector = Icons.Default.Repeat
    val RepetirCancion: ImageVector = Icons.Default.RepeatOne
}
