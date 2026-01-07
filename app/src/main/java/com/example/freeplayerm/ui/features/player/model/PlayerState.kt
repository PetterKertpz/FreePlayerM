package com.example.freeplayerm.ui.features.player.model

import androidx.compose.runtime.Immutable
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.gesture.PlayerGestureConstants

@Immutable
data class PlayerState(
   // === PLAYBACK ===
   val currentSong: SongWithArtist? = null,
   val isPlaying: Boolean = false,
   val currentPositionMs: Long = 0L,
   val scrubPositionMs: Long? = null,
   val isScrubbing: Boolean = false,
   val playbackMode: PlaybackMode = PlaybackMode.DEFAULT,
   val repeatMode: RepeatMode = RepeatMode.DEFAULT,
   val isFavorite: Boolean = false,

   // === PANEL ===
   val panelMode: PlayerPanelMode = PlayerPanelMode.DEFAULT,
   val activeTab: ExpandedTab = ExpandedTab.DEFAULT,

   // === GESTURE STATE ===
   val isDragging: Boolean = false,
   val isAnimating: Boolean = false,
   val gestureProgress: Float = 0f,
   val lastSongChangeTimestamp: Long = 0L,
   val isMinimizedByScroll: Boolean = false,

   // === EXPANDED METADATA ===
   val lyrics: String? = null,
   val artistInfo: String? = null,
   val albumDescription: String? = null,

   // === EXTERNAL LINKS ===
   val geniusUrl: String? = null,
   val youtubeUrl: String? = null,
   val googleUrl: String? = null,

   // === LOADING STATES ===
   val isLoadingLyrics: Boolean = false,
   val isLoadingInfo: Boolean = false,
) {
   // region Playback Properties

   val hasSong: Boolean
      get() = currentSong != null

   val canPlay: Boolean
      get() = currentSong?.cancion?.archivoPath != null

   val durationMs: Long
      get() = (currentSong?.cancion?.duracionSegundos?.toLong() ?: 0L) * 1000L

   val displayPositionMs: Long
      get() = (scrubPositionMs ?: currentPositionMs).coerceIn(0L, durationMs.coerceAtLeast(1L))

   val progressFraction: Float
      get() =
         if (durationMs > 0) {
            (displayPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
         } else 0f

   val remainingMs: Long
      get() = (durationMs - displayPositionMs).coerceAtLeast(0L)

   // endregion

   // region Display Properties

   val artistDisplay: String
      get() = currentSong?.artistaNombre ?: UNKNOWN_ARTIST

   val titleDisplay: String
      get() = currentSong?.cancion?.titulo ?: NO_TITLE

   val albumDisplay: String
      get() =
         buildString {
               currentSong?.albumNombre?.let { append(it) }
               currentSong?.fechaLanzamiento?.let { date ->
                  if (isNotEmpty()) append(" • ")
                  append(date.take(4))
               }
            }
            .ifEmpty { UNKNOWN_ALBUM }

   val genreDisplay: String
      get() = currentSong?.generoNombre ?: UNKNOWN_GENRE

   val coverArtUri: String?
      get() = currentSong?.cancion?.portadaPath

   // endregion

   // region Panel Properties
   
   val effectivePanelMode: PlayerPanelMode
      get() = panelMode
   
   val isExpanded: Boolean
      get() = effectivePanelMode == PlayerPanelMode.EXPANDED
   
   val isNormal: Boolean
      get() = effectivePanelMode == PlayerPanelMode.NORMAL
   
   // Altura visual efectiva considerando minimización
   val effectiveHeightFraction: Float
      get() = when {
         isExpanded -> PlayerGestureConstants.HEIGHT_FRACTION_EXPANDED
         isMinimizedByScroll -> PlayerGestureConstants.HEIGHT_FRACTION_MINIMIZED
         else -> PlayerGestureConstants.HEIGHT_FRACTION_NORMAL
      }
   
   val normalizedGestureProgress: Float
      get() = gestureProgress.coerceIn(0f, 1f)
   
   val isGesturing: Boolean
      get() = isDragging || isAnimating
   
   val canInteract: Boolean
      get() = !isAnimating && hasSong
   
   val isGestureStateSynced: Boolean
      get() = when {
         isGesturing -> true
         panelMode == PlayerPanelMode.EXPANDED -> gestureProgress >= 0.99f
         panelMode == PlayerPanelMode.NORMAL -> gestureProgress <= 0.01f
         else -> false
      }

   // endregion

   // region Metadata Properties

   val isActiveTabLoading: Boolean
      get() =
         when (activeTab) {
            ExpandedTab.LYRICS -> isLoadingLyrics
            ExpandedTab.INFO -> isLoadingInfo
            ExpandedTab.LINKS -> false
         }

   val hasActiveTabContent: Boolean
      get() =
         when (activeTab) {
            ExpandedTab.LYRICS -> !lyrics.isNullOrBlank()
            ExpandedTab.INFO -> !artistInfo.isNullOrBlank()
            ExpandedTab.LINKS -> hasLinks
         }

   val hasLinks: Boolean
      get() =
         !geniusUrl.isNullOrBlank() || !youtubeUrl.isNullOrBlank() || !googleUrl.isNullOrBlank()

   val hasLyrics: Boolean
      get() = !lyrics.isNullOrBlank()

   val hasArtistInfo: Boolean
      get() = !artistInfo.isNullOrBlank()

   val isLoading: Boolean
      get() = isLoadingLyrics || isLoadingInfo

   val lyricsDisplay: String
      get() =
         when {
            isLoadingLyrics -> "Cargando letra..."
            lyrics.isNullOrBlank() -> "Letra no disponible"
            else -> lyrics
         }

   val artistInfoDisplay: String
      get() =
         when {
            isLoadingInfo -> "Cargando información..."
            artistInfo.isNullOrBlank() -> "Información no disponible"
            else -> artistInfo
         }

   // endregion

   // region Helper Functions

   fun isValidPosition(positionMs: Long): Boolean = positionMs in 0..durationMs

   fun withUpdatedPosition(newPositionMs: Long): PlayerState =
      copy(currentPositionMs = newPositionMs.coerceIn(0L, durationMs))

   fun canMinimizeByScroll(currentTimeMs: Long): Boolean {
      if (lastSongChangeTimestamp == 0L) return true
      return currentTimeMs - lastSongChangeTimestamp > MINIMIZE_GRACE_PERIOD_MS
   }

   // endregion

   companion object {
      val EMPTY = PlayerState()
      const val MINIMIZE_GRACE_PERIOD_MS = 2000L

      // Display strings
      private const val UNKNOWN_ARTIST = "Artista desconocido"
      private const val NO_TITLE = "Sin título"
      private const val UNKNOWN_ALBUM = "Álbum desconocido"
      private const val UNKNOWN_GENRE = "Género desconocido"
      private const val LOADING_LYRICS = "Cargando letra..."
      private const val NO_LYRICS = "Letra no disponible"
      private const val LOADING_INFO = "Cargando información..."
      private const val NO_INFO = "Información no disponible"
   }
}

// region Time Formatting Extensions

fun Long.formatAsTime(): String {
   val totalSeconds = this / 1000
   val minutes = totalSeconds / 60
   val seconds = totalSeconds % 60
   return "%02d:%02d".format(minutes, seconds)
}

fun Long.formatAsExtendedTime(): String {
   val totalSeconds = this / 1000
   val hours = totalSeconds / 3600
   val minutes = (totalSeconds % 3600) / 60
   val seconds = totalSeconds % 60
   return if (hours > 0) {
      "%02d:%02d:%02d".format(hours, minutes, seconds)
   } else {
      "%02d:%02d".format(minutes, seconds)
   }
}

// endregion
