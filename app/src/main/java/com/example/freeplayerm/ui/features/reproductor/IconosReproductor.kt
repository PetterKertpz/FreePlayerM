// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/IconosReproductor.kt
package com.example.freeplayerm.ui.features.reproductor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Shuffle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SortNumericDown

/**
 * ⚡ ICONOS DEL REPRODUCTOR - CENTRALIZADO Y TIPADO
 *
 * Ventajas:
 * - Centralización de todos los iconos
 * - Fácil mantenimiento y cambios
 * - Type-safe
 * - Reutilizable
 *
 * @version 2.0 - Optimizado
 */
object IconosReproductor {

    // ==================== CONTROLES BÁSICOS ====================

    val Reproducir: ImageVector = Icons.Default.PlayArrow
    val Pausa: ImageVector = Icons.Default.Pause
    val Siguiente: ImageVector = Icons.Default.SkipNext
    val Anterior: ImageVector = Icons.Default.SkipPrevious
    val Detener: ImageVector = Icons.Default.Stop

    // ==================== FAVORITOS ====================

    val Favorito: ImageVector = Icons.Default.Favorite
    val NoFavorito: ImageVector = Icons.Default.FavoriteBorder

    // ==================== MODOS DE REPRODUCCIÓN ====================

    val Aleatorio: ImageVector = FeatherIcons.Shuffle
    val EnOrden: ImageVector = FontAwesomeIcons.Solid.SortNumericDown

    // ==================== MODOS DE REPETICIÓN ====================

    val NoRepetir: ImageVector = FeatherIcons.Repeat
    val RepetirLista: ImageVector = Icons.Default.Repeat
    val RepetirCancion: ImageVector = Icons.Default.RepeatOne

    // ==================== NAVEGACIÓN ====================

    val Expandir: ImageVector = Icons.Default.KeyboardArrowUp
    val Colapsar: ImageVector = Icons.Default.KeyboardArrowDown

    // ==================== FUNCIONES HELPER ====================

    /**
     * Obtiene el icono correcto para el modo de reproducción
     */
    fun obtenerIconoModoReproduccion(modo: ModoReproduccion): ImageVector {
        return when (modo) {
            ModoReproduccion.ALEATORIO -> Aleatorio
            ModoReproduccion.EN_ORDEN -> EnOrden
        }
    }

    /**
     * Obtiene el icono correcto para el modo de repetición
     */
    fun obtenerIconoModoRepeticion(modo: ModoRepeticion): ImageVector {
        return when (modo) {
            ModoRepeticion.NO_REPETIR -> NoRepetir
            ModoRepeticion.REPETIR_LISTA -> RepetirLista
            ModoRepeticion.REPETIR_CANCION -> RepetirCancion
        }
    }

    /**
     * Obtiene el icono correcto para play/pause
     */
    fun obtenerIconoPlayPause(estaReproduciendo: Boolean): ImageVector {
        return if (estaReproduciendo) Pausa else Reproducir
    }

    /**
     * Obtiene el icono correcto para favorito
     */
    fun obtenerIconoFavorito(esFavorita: Boolean): ImageVector {
        return if (esFavorita) Favorito else NoFavorito
    }
}