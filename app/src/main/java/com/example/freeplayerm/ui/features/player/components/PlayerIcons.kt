package com.example.freeplayerm.com.example.freeplayerm.ui.features.player.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.freeplayerm.ui.features.player.ModoRepeticion
import com.example.freeplayerm.ui.features.player.ModoReproduccion
import com.example.freeplayerm.ui.features.player.TabExpandido
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Shuffle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SortNumericDown

/**
 * ⚡ ICONOS DEL REPRODUCTOR - v3.0
 *
 * Centralización de todos los iconos: ✅ Controles básicos ✅ Modos de reproducción ✅ Favoritos ✅
 * Navegación del panel ✅ Tabs del modo expandido ✅ Enlaces externos
 *
 * @version 3.0 - Sistema de 3 Modos
 */
object PlayerIcons {

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

  // ==================== NAVEGACIÓN DEL PANEL ====================

  val Expandir: ImageVector = Icons.Default.KeyboardArrowUp
  val Colapsar: ImageVector = Icons.Default.KeyboardArrowDown
  val Cerrar: ImageVector = Icons.Default.Close

  // ==================== TABS DEL MODO EXPANDIDO ====================

  val TabLetra: ImageVector = Icons.AutoMirrored.Filled.Article
  val TabInfo: ImageVector = Icons.Default.Info
  val TabEnlaces: ImageVector = Icons.Default.Link

  // ==================== ENLACES EXTERNOS ====================

  val Genius: ImageVector = Icons.Default.Lyrics
  val Youtube: ImageVector = Icons.Default.PlayCircle
  val Google: ImageVector = Icons.Default.Search
  val Web: ImageVector = Icons.Default.Language

  // ==================== INFORMACIÓN ====================

  val Album: ImageVector = Icons.Default.Album
  val Artista: ImageVector = Icons.Default.Person
  val Genero: ImageVector = Icons.Default.MusicNote
  val Duracion: ImageVector = Icons.Default.Timer
  val Calendario: ImageVector = Icons.Default.CalendarToday

  // ==================== FUNCIONES HELPER ====================

  fun obtenerIconoModoReproduccion(modo: ModoReproduccion): ImageVector =
      when (modo) {
        ModoReproduccion.ALEATORIO -> Aleatorio
        ModoReproduccion.EN_ORDEN -> EnOrden
      }

  fun obtenerIconoModoRepeticion(modo: ModoRepeticion): ImageVector =
      when (modo) {
        ModoRepeticion.NO_REPETIR -> NoRepetir
        ModoRepeticion.REPETIR_LISTA -> RepetirLista
        ModoRepeticion.REPETIR_CANCION -> RepetirCancion
      }

  fun obtenerIconoPlayPause(estaReproduciendo: Boolean): ImageVector =
      if (estaReproduciendo) Pausa else Reproducir

  fun obtenerIconoFavorito(esFavorita: Boolean): ImageVector =
      if (esFavorita) Favorito else NoFavorito

  fun obtenerIconoTab(tab: TabExpandido): ImageVector =
      when (tab) {
        TabExpandido.LETRA -> TabLetra
        TabExpandido.INFO -> TabInfo
        TabExpandido.ENLACES -> TabEnlaces
      }

  fun obtenerIconoNavegacion(expandir: Boolean): ImageVector = if (expandir) Expandir else Colapsar
}