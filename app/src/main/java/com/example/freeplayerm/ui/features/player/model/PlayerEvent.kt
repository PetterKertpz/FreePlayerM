package com.example.freeplayerm.ui.features.player.model

import androidx.compose.runtime.Immutable
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist

// Eventos del reproductor organizados por categoría funcional
@Immutable
sealed interface PlayerEvent {
   
   // Eventos de control de reproducción
   @Immutable
   sealed interface Playback : PlayerEvent {
      @Immutable
      data class SetQueueAndPlay(
         val queue: List<SongWithArtist>,
         val startSong: SongWithArtist
      ) : Playback
      @Immutable
      data class PlaySingle(val song: SongWithArtist) : Playback
      
      data object PlayPause : Playback
      data object Next : Playback
      data object Previous : Playback
      data object Stop : Playback
      
      @Immutable
      data class PositionUpdate(val positionMs: Long) : Playback
      data object SongCompleted : Playback
   }
   
   // Eventos de búsqueda/scrubbing en timeline
   @Immutable
   sealed interface Seek : PlayerEvent {
      @Immutable
      data class Start(val positionMs: Long) : Seek
      @Immutable
      data class Update(val positionMs: Long) : Seek
      @Immutable
      data class Finish(val positionMs: Long) : Seek
   }
   
   // Eventos de configuración del reproductor
   sealed interface Settings : PlayerEvent {
      data object TogglePlaybackMode : Settings
      data object ToggleRepeatMode : Settings
      data object ToggleFavorite : Settings
   }
   
   // Eventos de control del panel
   @Immutable
   sealed interface Panel : PlayerEvent {
      data object Expand : Panel
      data object Collapse : Panel
      
      @Immutable
      data class SetMode(val mode: PlayerPanelMode) : Panel
      @Immutable
      data class ChangeTab(val tab: ExpandedTab) : Panel
      @Immutable
      data class NotifyScroll(val isScrolling: Boolean) : Panel
      data object AnimationCompleted : Panel
      data object SyncGestureState : Panel
      // Eventos de gestos agrupados
      @Immutable
      sealed interface Gesture : Panel {
         data object Started : Gesture
         @Immutable
         data class Update(val progress: Float) : Gesture
         @Immutable
         data class Ended(val targetMode: PlayerPanelMode) : Gesture
         data object Cancelled : Gesture
      }
   }
   
   // Eventos de swipe horizontal
   @Immutable
   sealed interface Swipe : PlayerEvent {
      @Immutable
      data class Horizontal(val direction: SwipeDirection) : Swipe
   }
   
   // Eventos de links externos
   sealed interface Links : PlayerEvent {
      data object OpenGenius : Links
      data object OpenYoutube : Links
      data object OpenGoogle : Links
   }
}

// Dirección del swipe horizontal
@Immutable
enum class SwipeDirection {
   LEFT,
   RIGHT;
   
   fun toPlaybackEvent(): PlayerEvent.Playback = when (this) {
      LEFT -> PlayerEvent.Playback.Next
      RIGHT -> PlayerEvent.Playback.Previous
   }
   
   val isNext: Boolean
      get() = this == LEFT
}

// Efectos secundarios del reproductor (side effects)
@Immutable
sealed interface PlayerEffect {
   @Immutable
   data class ShowToast(val message: String) : PlayerEffect
   
   @Immutable
   data class ShowError(val message: String) : PlayerEffect
   
   @Immutable
   data class OpenUrl(val url: String) : PlayerEffect
   
   @Immutable
   data class NavigateTo(val route: String) : PlayerEffect
   
   data object HapticClick : PlayerEffect
   data object HapticHeavy : PlayerEffect
   data object HapticSuccess : PlayerEffect
   
   data object ScrollToTop : PlayerEffect
}