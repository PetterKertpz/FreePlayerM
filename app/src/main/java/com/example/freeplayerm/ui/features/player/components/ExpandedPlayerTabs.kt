package com.example.freeplayerm.ui.features.player.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.model.ExpandedTab
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.formatAsTime
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== CONFIGURACIÓN ====================

@Stable
private object TabsConfig {
   // Colores
   val BackgroundColor = Color(0xFF0F0518)
   val SurfaceColor = Color.White.copy(alpha = 0.05f)
   val ContentBackground = Color.White.copy(alpha = 0.03f)
   
   // Animaciones
   const val TAB_TRANSITION_DURATION = 250
   const val TAB_FADE_DURATION = 200
   const val TAB_SLIDE_DURATION = 300
   val ScaleSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
   
   // Dimensiones
   val TabSelectorRadius = 12.dp
   val TabItemRadius = 8.dp
   val ContentRadius = 12.dp
   val ContentMinHeight = 150.dp
   val ContentMaxHeight = 300.dp
}

@Stable
private object LinkColors {
   val Genius = Color(0xFFFFFF64)
   val Youtube = Color(0xFFFF0000)
   val Google = Color(0xFF4285F4)
}

@Immutable
private data class LinkConfig(
   val icon: ImageVector,
   val title: String,
   val subtitle: String,
   val color: Color,
   val enabled: Boolean,
   val onClick: () -> Unit,
)

// ==================== MAPEOS ====================

private val tabIcons = mapOf(
   ExpandedTab.LYRICS to Icons.AutoMirrored.Filled.Article,
   ExpandedTab.INFO to Icons.Default.Info,
   ExpandedTab.LINKS to Icons.Default.Link,
)

private val tabTitles = mapOf(
   ExpandedTab.LYRICS to "Letra",
   ExpandedTab.INFO to "Info",
   ExpandedTab.LINKS to "Enlaces",
)

// ==================== COMPONENTE PRINCIPAL ====================

@Composable
fun ExpandedPlayerTabs(
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
   modifier: Modifier = Modifier,
) {
   // Callback estable
   val onTabSelected = remember(onEvent) {
      { tab: ExpandedTab -> onEvent(PlayerEvent.Panel.ChangeTab(tab)) }
   }
   
   Column(modifier = modifier.fillMaxWidth()) {
      TabSelector(
         activeTab = state.activeTab,
         onTabSelected = onTabSelected,
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      TabContent(
         activeTab = state.activeTab,
         state = state,
         onEvent = onEvent,
      )
   }
}

// ==================== TAB SELECTOR ====================

@Composable
private fun TabSelector(
   activeTab: ExpandedTab,
   onTabSelected: (ExpandedTab) -> Unit,
) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .clip(RoundedCornerShape(TabsConfig.TabSelectorRadius))
         .background(TabsConfig.SurfaceColor),
      horizontalArrangement = Arrangement.SpaceEvenly,
   ) {
      ExpandedTab.entries.forEach { tab ->
         TabItem(
            tab = tab,
            isSelected = tab == activeTab,
            onClick = { onTabSelected(tab) },
            modifier = Modifier.weight(1f),
         )
      }
   }
}

@Composable
private fun TabItem(
   tab: ExpandedTab,
   isSelected: Boolean,
   onClick: () -> Unit,
   modifier: Modifier = Modifier,
) {
   val interactionSource = remember { MutableInteractionSource() }
   val isPressed by interactionSource.collectIsPressedAsState()
   
   val backgroundColor by animateColorAsState(
      targetValue = if (isSelected) AppColors.ElectricViolet.v6.copy(alpha = 0.3f) else Color.Transparent,
      animationSpec = tween(TabsConfig.TAB_TRANSITION_DURATION),
      label = "tabBg",
   )
   
   val contentColor by animateColorAsState(
      targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
      animationSpec = tween(TabsConfig.TAB_TRANSITION_DURATION),
      label = "tabContent",
   )
   
   val iconTint by animateColorAsState(
      targetValue = if (isSelected) AppColors.ElectricViolet.v6 else Color.White.copy(alpha = 0.5f),
      animationSpec = tween(TabsConfig.TAB_TRANSITION_DURATION),
      label = "tabIcon",
   )
   
   val scale by animateFloatAsState(
      targetValue = when {
         isPressed -> 0.95f
         isSelected -> 1.02f
         else -> 1f
      },
      animationSpec = TabsConfig.ScaleSpring,
      label = "tabScale",
   )
   
   Box(
      modifier = modifier
         .scale(scale)
         .clip(RoundedCornerShape(TabsConfig.TabItemRadius))
         .background(backgroundColor)
         .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
         )
         .padding(vertical = 10.dp, horizontal = 12.dp),
      contentAlignment = Alignment.Center,
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.Center,
      ) {
         Icon(
            imageVector = tabIcons[tab] ?: Icons.Default.Info,
            contentDescription = tabTitles[tab],
            tint = iconTint,
            modifier = Modifier.size(18.dp),
         )
         Spacer(modifier = Modifier.width(6.dp))
         Text(
            text = tabTitles[tab] ?: "",
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
         )
      }
   }
}

// ==================== CONTENIDO ANIMADO ====================

@Composable
private fun TabContent(
   activeTab: ExpandedTab,
   state: PlayerState,
   onEvent: (PlayerEvent) -> Unit,
) {
   AnimatedContent(
      targetState = activeTab,
      transitionSpec = {
         val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
         
         (fadeIn(tween(TabsConfig.TAB_TRANSITION_DURATION)) +
               slideInHorizontally(
                  initialOffsetX = { direction * it / 4 },
                  animationSpec = tween(TabsConfig.TAB_SLIDE_DURATION),
               )) togetherWith
               (fadeOut(tween(TabsConfig.TAB_FADE_DURATION)) +
                     slideOutHorizontally(
                        targetOffsetX = { -direction * it / 4 },
                        animationSpec = tween(TabsConfig.TAB_TRANSITION_DURATION),
                     ))
      },
      label = "tabContent",
   ) { tab ->
      when (tab) {
         ExpandedTab.LYRICS -> LyricsTabContent(
            lyrics = state.lyricsDisplay,
            isLoading = state.isLoadingLyrics,
         )
         ExpandedTab.INFO -> InfoTabContent(
            artistInfo = state.artistInfoDisplay,
            albumDisplay = state.albumDisplay,
            genreDisplay = state.genreDisplay,
            releaseDate = state.currentSong?.fechaLanzamiento,
            durationMs = state.durationMs,
            isLoading = state.isLoadingInfo,
         )
         ExpandedTab.LINKS -> LinksTabContent(
            hasGenius = !state.geniusUrl.isNullOrBlank(),
            hasYoutube = !state.youtubeUrl.isNullOrBlank(),
            hasGoogle = !state.googleUrl.isNullOrBlank(),
            onEvent = onEvent,
         )
      }
   }
}

// ==================== CONTENIDO DE TABS ====================

@Composable
private fun LyricsTabContent(
   lyrics: String,
   isLoading: Boolean,
) {
   ContentContainer {
      if (isLoading) {
         LoadingContent(message = "Cargando letra...")
      } else {
         val scrollState = rememberScrollState()
         Text(
            text = lyrics,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier
               .fillMaxSize()
               .verticalScroll(scrollState)
               .padding(16.dp),
         )
      }
   }
}

@Composable
private fun InfoTabContent(
   artistInfo: String,
   albumDisplay: String,
   genreDisplay: String,
   releaseDate: String?,
   durationMs: Long,
   isLoading: Boolean,
) {
   ContentContainer {
      if (isLoading) {
         LoadingContent(message = "Cargando información...")
      } else {
         val scrollState = rememberScrollState()
         Column(
            modifier = Modifier
               .fillMaxSize()
               .verticalScroll(scrollState)
               .padding(16.dp)
         ) {
            Text(
               text = artistInfo,
               color = Color.White.copy(alpha = 0.85f),
               fontSize = 14.sp,
               lineHeight = 22.sp,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(Icons.Default.Album, "Álbum", albumDisplay)
            InfoRow(Icons.Default.MusicNote, "Género", genreDisplay)
            releaseDate?.let { InfoRow(Icons.Default.CalendarToday, "Año", it.take(4)) }
            InfoRow(Icons.Default.Timer, "Duración", durationMs.formatAsTime())
         }
      }
   }
}

@Composable
private fun LinksTabContent(
   hasGenius: Boolean,
   hasYoutube: Boolean,
   hasGoogle: Boolean,
   onEvent: (PlayerEvent) -> Unit,
) {
   // Callbacks estables
   val onGeniusClick = remember(onEvent) { { onEvent(PlayerEvent.Links.OpenGenius) } }
   val onYoutubeClick = remember(onEvent) { { onEvent(PlayerEvent.Links.OpenYoutube) } }
   val onGoogleClick = remember(onEvent) { { onEvent(PlayerEvent.Links.OpenGoogle) } }
   
   val links = remember(hasGenius, hasYoutube, hasGoogle) {
      listOf(
         LinkConfig(
            icon = Icons.Default.Lyrics,
            title = "Ver en Genius",
            subtitle = "Letra y anotaciones",
            color = LinkColors.Genius,
            enabled = hasGenius,
            onClick = onGeniusClick,
         ),
         LinkConfig(
            icon = Icons.Default.PlayCircle,
            title = "Buscar en YouTube",
            subtitle = "Videos y conciertos",
            color = LinkColors.Youtube,
            enabled = hasYoutube,
            onClick = onYoutubeClick,
         ),
         LinkConfig(
            icon = Icons.Default.Search,
            title = "Buscar artista",
            subtitle = "Google Search",
            color = LinkColors.Google,
            enabled = hasGoogle,
            onClick = onGoogleClick,
         ),
      )
   }
   
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .clip(RoundedCornerShape(TabsConfig.ContentRadius))
         .background(TabsConfig.ContentBackground)
         .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
   ) {
      Text(
         text = "Más sobre esta canción",
         color = Color.White.copy(alpha = 0.7f),
         fontSize = 12.sp,
         fontWeight = FontWeight.Medium,
      )
      
      links.forEach { link ->
         LinkButton(config = link)
      }
   }
}

// ==================== COMPONENTES REUTILIZABLES ====================

@Composable
private fun ContentContainer(content: @Composable () -> Unit) {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .heightIn(min = TabsConfig.ContentMinHeight, max = TabsConfig.ContentMaxHeight)
         .clip(RoundedCornerShape(TabsConfig.ContentRadius))
         .background(TabsConfig.ContentBackground)
   ) {
      content()
   }
}

@Composable
private fun LoadingContent(message: String) {
   Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
   ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
         CircularProgressIndicator(
            color = AppColors.ElectricViolet.v6,
            modifier = Modifier.size(32.dp),
            strokeWidth = 2.dp,
         )
         Spacer(modifier = Modifier.height(8.dp))
         Text(
            text = message,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
         )
      }
   }
}

@Composable
private fun InfoRow(
   icon: ImageVector,
   label: String,
   value: String,
) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .padding(vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = AppColors.ElectricViolet.v6.copy(alpha = 0.7f),
         modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
         text = label,
         color = Color.White.copy(alpha = 0.5f),
         fontSize = 12.sp,
         modifier = Modifier.width(70.dp),
      )
      Text(
         text = value,
         color = Color.White,
         fontSize = 14.sp,
         fontWeight = FontWeight.Medium,
      )
   }
}

@Composable
private fun LinkButton(config: LinkConfig) {
   val interactionSource = remember { MutableInteractionSource() }
   val isPressed by interactionSource.collectIsPressedAsState()
   
   val scale by animateFloatAsState(
      targetValue = if (isPressed && config.enabled) 0.97f else 1f,
      animationSpec = TabsConfig.ScaleSpring,
      label = "linkScale",
   )
   
   val backgroundColor by animateColorAsState(
      targetValue = when {
         !config.enabled -> Color.White.copy(alpha = 0.02f)
         isPressed -> config.color.copy(alpha = 0.2f)
         else -> config.color.copy(alpha = 0.1f)
      },
      animationSpec = tween(150),
      label = "linkBg",
   )
   
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .scale(scale)
         .clip(RoundedCornerShape(12.dp))
         .background(backgroundColor)
         .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = config.enabled,
            onClick = config.onClick,
         )
         .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
   ) {
      LinkIcon(
         icon = config.icon,
         color = config.color,
         enabled = config.enabled,
      )
      
      Spacer(modifier = Modifier.width(16.dp))
      
      LinkText(
         title = config.title,
         subtitle = config.subtitle,
         enabled = config.enabled,
         modifier = Modifier.weight(1f),
      )
      
      if (config.enabled) {
         LinkChevron(isPressed = isPressed)
      }
   }
}

@Composable
private fun LinkIcon(
   icon: ImageVector,
   color: Color,
   enabled: Boolean,
) {
   val backgroundBrush = remember(color, enabled) {
      if (enabled) {
         Brush.linearGradient(listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.15f)))
      } else {
         Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
      }
   }
   
   Box(
      modifier = Modifier
         .size(44.dp)
         .clip(RoundedCornerShape(10.dp))
         .background(backgroundBrush),
      contentAlignment = Alignment.Center,
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = if (enabled) color else Color.White.copy(alpha = 0.3f),
         modifier = Modifier.size(24.dp),
      )
   }
}

@Composable
private fun LinkText(
   title: String,
   subtitle: String,
   enabled: Boolean,
   modifier: Modifier = Modifier,
) {
   Column(modifier = modifier) {
      Text(
         text = title,
         color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
         fontSize = 15.sp,
         fontWeight = FontWeight.SemiBold,
      )
      Text(
         text = subtitle,
         color = if (enabled) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
         fontSize = 12.sp,
      )
   }
}

@Composable
private fun LinkChevron(isPressed: Boolean) {
   val alpha by animateFloatAsState(
      targetValue = if (isPressed) 0.7f else 0.4f,
      animationSpec = tween(100),
      label = "chevronAlpha",
   )
   
   Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = Color.White.copy(alpha = alpha),
      modifier = Modifier.size(20.dp),
   )
}

// ==================== PREVIEWS ====================

@Composable
fun PreviewContainer(content: @Composable () -> Unit) {
   FreePlayerMTheme(darkTheme = true) {
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(TabsConfig.BackgroundColor)
            .padding(16.dp),
         contentAlignment = Alignment.Center,
      ) {
         content()
      }
   }
}

private class TabStateProvider : PreviewParameterProvider<PlayerState> {
   private val demoSong = SongWithArtist.preview(
      titulo = "Bohemian Rhapsody",
      artista = "Queen",
      album = "A Night at the Opera",
      genero = "Rock",
      duracionSegundos = 354,
      esFavorita = true,
   ).copy(fechaLanzamiento = "1975-10-31")
   
   override val values = sequenceOf(
      PlayerState(
         currentSong = demoSong,
         activeTab = ExpandedTab.LYRICS,
         lyrics = "Is this the real life?\nIs this just fantasy?",
      ),
      PlayerState(
         currentSong = demoSong,
         activeTab = ExpandedTab.INFO,
         artistInfo = "🎤 Queen\n📍 Londres, Reino Unido",
      ),
      PlayerState(
         currentSong = demoSong,
         activeTab = ExpandedTab.LINKS,
         geniusUrl = "https://genius.com",
         youtubeUrl = "https://youtube.com",
         googleUrl = "https://google.com",
      ),
   )
}

@Preview(name = "Tabs - Estados", showBackground = true, backgroundColor = 0xFF0F0518, heightDp = 450)
@Composable
private fun PreviewTabsStates(
   @PreviewParameter(TabStateProvider::class) state: PlayerState
) {
   PreviewContainer {
      ExpandedPlayerTabs(state = state, onEvent = {})
   }
}

@Preview(name = "Tabs - Loading", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewTabsLoading() {
   PreviewContainer {
      ExpandedPlayerTabs(
         state = PlayerState(
            currentSong = SongWithArtist.preview(),
            activeTab = ExpandedTab.LYRICS,
            isLoadingLyrics = true,
         ),
         onEvent = {},
      )
   }
}