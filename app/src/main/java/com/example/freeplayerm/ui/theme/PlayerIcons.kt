package com.example.freeplayerm.com.example.freeplayerm.ui.theme

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
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.freeplayerm.ui.features.player.model.RepeatMode  // ✅ Corregido
import com.example.freeplayerm.ui.features.player.model.PlaybackMode  // ✅ Corregido
import com.example.freeplayerm.ui.features.player.model.ExpandedTab  // ✅ Corregido
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Shuffle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SortNumericDown

/**
 * ⚡ PLAYER ICONS - v4.0
 *
 * Centralized icon system:
 * ✅ Basic playback controls
 * ✅ Playback & repeat modes
 * ✅ Favorites
 * ✅ Panel navigation
 * ✅ Expanded mode tabs
 * ✅ External links
 * ✅ Metadata icons
 *
 * @version 4.0 - Aligned with 3-Mode System (MINIMIZED/NORMAL/EXPANDED)
 */
object PlayerIcons {
   
   // ==================== BASIC PLAYBACK CONTROLS ====================
   
   val Play: ImageVector = Icons.Default.PlayArrow
   val Pause: ImageVector = Icons.Default.Pause
   val Next: ImageVector = Icons.Default.SkipNext
   val Previous: ImageVector = Icons.Default.SkipPrevious
   val Stop: ImageVector = Icons.Default.Stop
   
   // ==================== FAVORITES ====================
   
   val Favorite: ImageVector = Icons.Default.Favorite
   val NotFavorite: ImageVector = Icons.Default.FavoriteBorder
   
   // ==================== PLAYBACK MODES ====================
   
   val Shuffle: ImageVector = FeatherIcons.Shuffle
   val Sequential: ImageVector = FontAwesomeIcons.Solid.SortNumericDown
   
   // ==================== REPEAT MODES ====================
   
   val RepeatOff: ImageVector = FeatherIcons.Repeat
   val RepeatAll: ImageVector = Icons.Default.Repeat
   val RepeatOne: ImageVector = Icons.Default.RepeatOne
   
   // ==================== PANEL NAVIGATION ====================
   
   val Expand: ImageVector = Icons.Default.KeyboardArrowUp
   val Collapse: ImageVector = Icons.Default.KeyboardArrowDown
   val Close: ImageVector = Icons.Default.Close
   
   // ==================== EXPANDED MODE TABS ====================
   
   val TabLyrics: ImageVector = Icons.AutoMirrored.Filled.Article
   val TabInfo: ImageVector = Icons.Default.Info
   val TabCredits: ImageVector = Icons.Default.Workspaces
   val TabLinks: ImageVector = Icons.Default.Link
   
   // ==================== EXTERNAL LINKS ====================
   
   val Genius: ImageVector = Icons.Default.Lyrics
   val Youtube: ImageVector = Icons.Default.PlayCircle
   val Google: ImageVector = Icons.Default.Search
   val Web: ImageVector = Icons.Default.Language
   
   // ==================== METADATA INFORMATION ====================
   
   val Album: ImageVector = Icons.Default.Album
   val Artist: ImageVector = Icons.Default.Person
   val Genre: ImageVector = Icons.Default.MusicNote
   val Duration: ImageVector = Icons.Default.Timer
   val Calendar: ImageVector = Icons.Default.CalendarToday
   
   // ==================== HELPER FUNCTIONS ====================
   
   /**
    * Returns the appropriate icon for the given playback mode.
    *
    * @param mode Current playback mode (SHUFFLE or SEQUENTIAL)
    * @return ImageVector icon representing the mode
    */
   fun getPlaybackModeIcon(mode: PlaybackMode): ImageVector =
      when (mode) {
         PlaybackMode.SHUFFLE -> Shuffle
         PlaybackMode.SEQUENTIAL -> Sequential
      }
   
   /**
    * Returns the appropriate icon for the given repeat mode.
    *
    * @param mode Current repeat mode (OFF, ALL, or ONE)
    * @return ImageVector icon representing the mode
    */
   fun getRepeatModeIcon(mode: RepeatMode): ImageVector =
      when (mode) {
         RepeatMode.OFF -> RepeatOff
         RepeatMode.ALL -> RepeatAll
         RepeatMode.ONE -> RepeatOne
      }
   
   /**
    * Returns Play or Pause icon based on playback state.
    *
    * @param isPlaying True if currently playing, false if paused
    * @return ImageVector icon (Play or Pause)
    */
   fun getPlayPauseIcon(isPlaying: Boolean): ImageVector =
      if (isPlaying) Pause else Play
   
   /**
    * Returns Favorite or NotFavorite icon based on favorite state.
    *
    * @param isFavorite True if marked as favorite, false otherwise
    * @return ImageVector icon (Favorite or NotFavorite)
    */
   fun getFavoriteIcon(isFavorite: Boolean): ImageVector =
      if (isFavorite) Favorite else NotFavorite
   
   /**
    * Returns the appropriate icon for an expanded mode tab.
    *
    * @param tab The tab type (LYRICS, INFO, or LINKS)
    * @return ImageVector icon representing the tab
    */
   fun getTabIcon(tab: ExpandedTab): ImageVector =
      when (tab) {
         ExpandedTab.LYRICS -> TabLyrics
         ExpandedTab.INFO -> TabInfo
         ExpandedTab.CREDITS -> TabCredits
         ExpandedTab.LINKS -> TabLinks
      }
   
   /**
    * Returns Expand or Collapse icon for panel navigation.
    *
    * @param shouldExpand True to show expand arrow, false for collapse
    * @return ImageVector icon (Expand or Collapse)
    */
   fun getNavigationIcon(shouldExpand: Boolean): ImageVector =
      if (shouldExpand) Expand else Collapse
}