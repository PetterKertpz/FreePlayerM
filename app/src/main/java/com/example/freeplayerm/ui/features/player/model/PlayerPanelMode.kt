package com.example.freeplayerm.ui.features.player.model

import androidx.compose.runtime.Immutable

// Modos de visualización del panel reproductor.
// NORMAL: Estado base, barra inferior (20%)
// EXPANDED: Pantalla completa con controles y tabs (100%)
// MINIMIZED: Sub-estado visual de NORMAL (automático por scroll, 10%)
@Immutable
enum class PlayerPanelMode {
   NORMAL,
   EXPANDED;
   
   val isExpanded: Boolean
      get() = this == EXPANDED
   
   val isNormal: Boolean
      get() = this == NORMAL
   
   fun toggle(): PlayerPanelMode = when (this) {
      EXPANDED -> NORMAL
      NORMAL -> EXPANDED
   }
   
   companion object {
      val DEFAULT = NORMAL
   }
}

// Modos de reproducción de la cola
@Immutable
enum class PlaybackMode {
   SEQUENTIAL,
   SHUFFLE;
   
   fun toggle(): PlaybackMode = when (this) {
      SEQUENTIAL -> SHUFFLE
      SHUFFLE -> SEQUENTIAL
   }
   
   val isShuffled: Boolean
      get() = this == SHUFFLE
   
   companion object {
      val DEFAULT = SEQUENTIAL
   }
}

// Modos de repetición
@Immutable
enum class RepeatMode {
   OFF,
   ALL,
   ONE;
   
   fun next(): RepeatMode = when (this) {
      OFF -> ALL
      ALL -> ONE
      ONE -> OFF
   }
   
   val isActive: Boolean
      get() = this != OFF
   
   val isRepeatingOne: Boolean
      get() = this == ONE
   
   companion object {
      val DEFAULT = OFF
   }
}

// Tabs disponibles en modo expandido
@Immutable
enum class ExpandedTab {
   LYRICS,
   INFO,
   LINKS;
   
   val titleKey: String
      get() = when (this) {
         LYRICS -> "tab_lyrics"
         INFO -> "tab_info"
         LINKS -> "tab_links"
      }
   
   val index: Int
      get() = ordinal
   
   companion object {
      val DEFAULT = LYRICS
      
      fun fromIndex(index: Int): ExpandedTab = entries.getOrElse(index) { DEFAULT }
   }
}