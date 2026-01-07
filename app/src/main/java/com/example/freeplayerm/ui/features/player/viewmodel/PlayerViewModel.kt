package com.example.freeplayerm.ui.features.player.viewmodel

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.freeplayerm.data.local.dao.LyricsDao
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.entity.FavoriteEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.data.repository.GeniusRepository
import com.example.freeplayerm.ui.features.player.model.PlaybackMode
import com.example.freeplayerm.ui.features.player.model.PlayerEffect
import com.example.freeplayerm.ui.features.player.model.PlayerEvent
import com.example.freeplayerm.ui.features.player.model.PlayerPanelMode
import com.example.freeplayerm.ui.features.player.model.PlayerState
import com.example.freeplayerm.ui.features.player.model.RepeatMode
import com.example.freeplayerm.utils.MediaItemHelper
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicLong

// ViewModel unificado del reproductor
// Gestiona estado UI, MediaController, eventos y efectos one-shot
@HiltViewModel
class PlayerViewModel
@Inject
constructor(
   @ApplicationContext private val context: Context,
   private val serviceComponentName: ComponentName,
   private val geniusRepository: GeniusRepository,
   private val lyricsDao: LyricsDao,
   private val songDao: SongDao,
   private val mediaItemHelper: MediaItemHelper,
) : ViewModel() {
   
   companion object {
      private const val TAG = "PlayerViewModel"
      private const val PROGRESS_UPDATE_MS = 250L
      private const val PROGRESS_EMIT_THRESHOLD_MS = 1000L
      private const val TIMEOUT_DATA_MS = 5000L
      private const val DEFAULT_USER_ID = 1
      private const val MINIMIZE_DELAY_MS = 300L
      private const val RESTORE_DELAY_MS = 400L
      private const val RESTART_THRESHOLD_MS = 3000L
      
      // Messages
      private const val MSG_PLAYER_UNAVAILABLE = "Reproductor no disponible"
      private const val MSG_PLAYER_ERROR = "Error conectando al reproductor"
      private const val MSG_CRITICAL_ERROR = "Error crítico en reproductor"
      private const val MSG_NO_MORE_SONGS = "No hay más canciones"
      private const val MSG_NO_PLAYABLE_SONGS = "No hay canciones reproducibles"
      private const val MSG_PROCESS_ERROR = "Error al procesar las canciones"
      private const val MSG_PREPARE_ERROR = "Error al preparar reproducción"
      private const val MSG_LOAD_ERROR = "Error al cargar canciones"
      private const val MSG_SONG_ERROR = "Error al cargar canción"
      private const val MSG_SONGS_SKIPPED = "canciones omitidas"
      private const val MSG_FAVORITE_ADD = "Agregado a favoritos ❤️"
      private const val MSG_FAVORITE_REMOVE = "Quitado de favoritos"
      private const val MSG_FAVORITE_ERROR = "Error al actualizar favorito"
      private const val MSG_LINK_UNAVAILABLE = "Enlace no disponible"
      private const val MSG_SHUFFLE_ON = "Modo aleatorio activado"
      private const val MSG_SHUFFLE_OFF = "Modo aleatorio desactivado"
      private const val MSG_REPEAT_OFF = "Repetición desactivada"
      private const val MSG_REPEAT_ALL = "Repetir lista"
      private const val MSG_REPEAT_ONE = "Repetir canción"
   }

   // region State

   private val _state = MutableStateFlow(PlayerState())
   val state: StateFlow<PlayerState> = _state.asStateFlow()

   // endregion

   // region Effects (One-Shot)

   private val _effects = Channel<PlayerEffect>(Channel.BUFFERED)
   val effects = _effects.receiveAsFlow()

   // endregion

   // region Media Controller

   private var mediaController: MediaController? = null
   private lateinit var controllerFuture: ListenableFuture<MediaController>
   private var playerListener: Player.Listener? = null

   private val player: Player?
      get() = mediaController

   // endregion

   // region Internal State
   
   // Cola de canciones thread-safe con StateFlow inmutable
   private val _songQueue = MutableStateFlow<Map<String, SongWithArtist>>(emptyMap())
   
   private fun updateSongQueue(transform: (Map<String, SongWithArtist>) -> Map<String, SongWithArtist>) {
      _songQueue.update(transform)
   }
   
   private fun getSongFromQueue(key: String): SongWithArtist? {
      return _songQueue.value[key]
   }
   
   private fun clearSongQueue() {
      _songQueue.value = emptyMap()
   }
   
   private fun addToSongQueue(key: String, song: SongWithArtist) {
      updateSongQueue { current -> current + (key to song) }
   }

   private var progressUpdaterJob: Job? = null
   private var additionalDataJob: Job? = null
   private var scrollJob: Job? = null
   private val _lastEmittedSecond = AtomicLong(-1L)
   private var lastEmittedSecond: Long
      get() = _lastEmittedSecond.get()
      set(value) { _lastEmittedSecond.set(value) }

   // endregion

   // region Initialization

   init {
      connectToService()
      observeFavoriteState()
   }

   private fun connectToService() {
      try {
         val sessionToken = SessionToken(context, serviceComponentName)
         controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

         controllerFuture.addListener(
            {
               try {
                  mediaController = controllerFuture.get()
                  viewModelScope.launch(Dispatchers.Main.immediate) {
                     setupPlayerListeners()
                     syncInitialState()
                  }
               } catch (e: Exception) {
                  Log.e(TAG, "Error connecting to player", e)
                  sendEffect(PlayerEffect.ShowError("Error conectando al reproductor"))
               }
            },
            ContextCompat.getMainExecutor(context),
         )
      } catch (e: Exception) {
         Log.e(TAG, "Critical error in player", e)
         sendEffect(PlayerEffect.ShowError("Error crítico en reproductor"))
      }
   }

   private fun setupPlayerListeners() {
      playerListener?.let { player?.removeListener(it) }

      playerListener =
         object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
               _state.update { it.copy(isPlaying = isPlaying) }
               if (isPlaying) startProgressUpdater() else stopProgressUpdater()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
               updateCurrentSongFromPlayer(mediaItem)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
               val newMode =
                  when (repeatMode) {
                     Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                     Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                     Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                     else -> RepeatMode.OFF
                  }
               _state.update { it.copy(repeatMode = newMode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
               val newMode =
                  if (shuffleModeEnabled) PlaybackMode.SHUFFLE else PlaybackMode.SEQUENTIAL
               _state.update { it.copy(playbackMode = newMode) }
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
               error?.let {
                  Log.e(TAG, "Player error: ${it.message}", it)
                  sendEffect(PlayerEffect.ShowError("Error: ${it.message}"))
               }
            }
         }

      player?.addListener(playerListener!!)
   }

   private fun syncInitialState() {
      player?.let { p ->
         _state.update {
            it.copy(
               isPlaying = p.isPlaying,
               playbackMode =
                  if (p.shuffleModeEnabled) PlaybackMode.SHUFFLE else PlaybackMode.SEQUENTIAL,
               repeatMode =
                  when (p.repeatMode) {
                     Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                     Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                     Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                     else -> RepeatMode.OFF
                  },
            )
         }
         p.currentMediaItem?.let { updateCurrentSongFromPlayer(it)
         }
      }
   }

   // endregion

   // region Event Handling

   fun onEvent(event: PlayerEvent) {
      when (event) {
         is PlayerEvent.Playback -> handlePlaybackEvent(event)
         is PlayerEvent.Seek -> handleSeekEvent(event)
         is PlayerEvent.Settings -> handleSettingsEvent(event)
         is PlayerEvent.Panel -> handlePanelEvent(event)
         is PlayerEvent.Swipe -> handleSwipeEvent(event)
         is PlayerEvent.Links -> handleLinksEvent(event)
      }
   }

   // endregion

   // region Playback Events

   private fun handlePlaybackEvent(event: PlayerEvent.Playback) {
      val p =
         player
            ?: run {
               sendEffect(PlayerEffect.ShowToast("Reproductor no disponible"))
               return
            }

      when (event) {
         is PlayerEvent.Playback.SetQueueAndPlay -> {
            setQueueAndPlay(event.queue, event.startSong)
         }
         is PlayerEvent.Playback.PlaySingle -> {
            setQueueAndPlay(listOf(event.song), event.song)
         }
         is PlayerEvent.Playback.PlayPause -> togglePlayPause()
         is PlayerEvent.Playback.Next -> {
            if (p.hasNextMediaItem() || p.repeatMode != Player.REPEAT_MODE_OFF) {
               p.seekToNextMediaItem()
            } else {
               sendEffect(PlayerEffect.ShowToast("No hay más canciones"))
            }
         }
         is PlayerEvent.Playback.Previous -> {
            when {
               p.currentPosition > RESTART_THRESHOLD_MS -> p.seekTo(0)
               p.hasPreviousMediaItem() -> p.seekToPreviousMediaItem()
            }
         }
         is PlayerEvent.Playback.Stop -> stopPlayback()
         is PlayerEvent.Playback.PositionUpdate -> {
            if (!_state.value.isScrubbing) {
               _state.update { it.copy(currentPositionMs = event.positionMs) }
            }
         }
         is PlayerEvent.Playback.SongCompleted -> {
            Log.d(TAG, "Song completed naturally")
         }
      }
   }

   // endregion

   // region Seek Events

   private fun handleSeekEvent(event: PlayerEvent.Seek) {
      when (event) {
         is PlayerEvent.Seek.Start -> {
            _state.update {
               it.copy(isScrubbing = true, scrubPositionMs = event.positionMs.coerceAtLeast(0))
            }
         }
         is PlayerEvent.Seek.Update -> {
            _state.update { it.copy(scrubPositionMs = event.positionMs.coerceAtLeast(0)) }
         }
         is PlayerEvent.Seek.Finish -> {
            val duration = _state.value.durationMs
            val position = event.positionMs.coerceIn(0L, duration)
            player?.seekTo(position)
            _state.update {
               it.copy(isScrubbing = false, scrubPositionMs = null, currentPositionMs = position)
            }
            lastEmittedSecond = -1L
         }
      }
   }

   // endregion

   // region Settings Events

   private fun handleSettingsEvent(event: PlayerEvent.Settings) {
      when (event) {
         is PlayerEvent.Settings.TogglePlaybackMode -> togglePlaybackMode()
         is PlayerEvent.Settings.ToggleRepeatMode -> toggleRepeatMode()
         is PlayerEvent.Settings.ToggleFavorite -> toggleFavorite()
      }
   }

   // endregion

   // region Panel Events
   
   private fun handlePanelEvent(event: PlayerEvent.Panel) {
      when (event) {
         is PlayerEvent.Panel.SyncGestureState -> {
            // Sincronización explícita solo cuando sea necesario
            val targetProgress = when (_state.value.panelMode) {
               PlayerPanelMode.EXPANDED -> 1f
               PlayerPanelMode.NORMAL-> 0f
            }
            _state.update { it.copy(gestureProgress = targetProgress) }
         }
         
         is PlayerEvent.Panel.Expand -> {
            _state.update {
               it.copy(
                  panelMode = PlayerPanelMode.EXPANDED,
                  isAnimating = false,
               )
            }
            loadExpandedDataIfNeeded()
         }
         
         is PlayerEvent.Panel.Collapse -> {
            _state.update {
               it.copy(
                  panelMode = PlayerPanelMode.NORMAL,
                  isAnimating = false,
               )
            }
         }
         
         is PlayerEvent.Panel.SetMode -> {
            _state.update {
               it.copy(
                  panelMode = event.mode,
                  isAnimating = false,
               )
            }
            if (event.mode == PlayerPanelMode.EXPANDED) {
               loadExpandedDataIfNeeded()
            }
         }
         
         is PlayerEvent.Panel.ChangeTab -> {
            _state.update { it.copy(activeTab = event.tab) }
         }
         
         is PlayerEvent.Panel.NotifyScroll -> handleScrollNotification(event.isScrolling)
         
         is PlayerEvent.Panel.AnimationCompleted -> {
            _state.update { it.copy(isAnimating = false) }
         }
         
         is PlayerEvent.Panel.Gesture -> handleGestureEvent(event)
      }
   }
   
   private fun handleGestureEvent(event: PlayerEvent.Panel.Gesture) {
      when (event) {
         is PlayerEvent.Panel.Gesture.Started -> {
            _state.update { it.copy(isDragging = true) }
         }
         
         is PlayerEvent.Panel.Gesture.Update -> {
            _state.update { it.copy(gestureProgress = event.progress) }
         }
         
         is PlayerEvent.Panel.Gesture.Ended -> {
            _state.update {
               it.copy(
                  isDragging = false,
                  isAnimating = true,
                  panelMode = event.targetMode
               )
            }
            if (event.targetMode == PlayerPanelMode.EXPANDED) {
               loadExpandedDataIfNeeded()
            }
         }
         
         is PlayerEvent.Panel.Gesture.Cancelled -> {
            _state.update { it.copy(isDragging = false, isAnimating = false) }
         }
      }
   }

   private fun handleScrollNotification(isScrolling: Boolean) {
      val currentState = _state.value

      // No minimizar si está expandido o no hay canción
      if (currentState.panelMode == PlayerPanelMode.EXPANDED) return
      if (!currentState.hasSong) return

      // No minimizar durante gesture period después de cambio de canción
      val currentTimeMs = System.currentTimeMillis()
      if (!currentState.canMinimizeByScroll(currentTimeMs)) return

      // Cancelar job anterior para evitar race conditions
      scrollJob?.cancel()

      scrollJob =
         viewModelScope.launch {
            try {
               when {
                  isScrolling && currentState.panelMode == PlayerPanelMode.NORMAL && !currentState.isMinimizedByScroll -> {
                     delay(MINIMIZE_DELAY_MS)
                     _state.update { state ->
                        if (
                           state.panelMode == PlayerPanelMode.NORMAL &&
                           !state.isMinimizedByScroll &&
                           !state.isGesturing &&
                           !state.isScrubbing &&
                           state.hasSong
                        ) {
                           state.copy(isMinimizedByScroll = true)
                        } else state
                     }
                  }
                  !isScrolling && currentState.isMinimizedByScroll -> {
                     delay(RESTORE_DELAY_MS)
                     _state.update { state ->
                        if (
                           state.isMinimizedByScroll &&
                           !state.isScrubbing &&
                           !state.isGesturing &&
                           state.hasSong
                        ) {
                           state.copy(isMinimizedByScroll = false)
                        } else state
                     }
                  }
               }
            } catch (e: CancellationException) {
               // Job cancelado correctamente
            }
         }
   }

   // endregion

   // region Swipe Events

   private fun handleSwipeEvent(event: PlayerEvent.Swipe) {
      when (event) {
         is PlayerEvent.Swipe.Horizontal -> {
            handlePlaybackEvent(event.direction.toPlaybackEvent())
            sendEffect(PlayerEffect.HapticClick)
         }
      }
   }

   // endregion

   // region Links Events

   private fun handleLinksEvent(event: PlayerEvent.Links) {
      val currentState = _state.value
      val url =
         when (event) {
            is PlayerEvent.Links.OpenGenius -> currentState.geniusUrl
            is PlayerEvent.Links.OpenYoutube -> currentState.youtubeUrl
            is PlayerEvent.Links.OpenGoogle -> currentState.googleUrl
         }

      if (url.isNullOrBlank()) {
         sendEffect(PlayerEffect.ShowToast("Enlace no disponible"))
      } else {
         sendEffect(PlayerEffect.OpenUrl(url))
      }
   }

   // endregion

   // region Playback Logic
   
   private fun setQueueAndPlay(queue: List<SongWithArtist>, startSong: SongWithArtist) {
      val p = player ?: run {
         sendEffect(PlayerEffect.ShowToast(MSG_PLAYER_UNAVAILABLE))
         return
      }
      
      viewModelScope.launch {
         try {
            // Limpiar la cola al inicio
            clearSongQueue()
            
            val (validSongs, invalidSongs) = queue.partition {
               !it.cancion.archivoPath.isNullOrBlank()
            }
            
            if (invalidSongs.isNotEmpty()) {
               Log.w(TAG, "${invalidSongs.size} canciones sin archivo válido")
            }
            
            if (validSongs.isEmpty()) {
               sendEffect(PlayerEffect.ShowError(MSG_NO_PLAYABLE_SONGS))
               return@launch
            }
            
            val mediaItems = validSongs.mapNotNull { song ->
               try {
                  mediaItemHelper.crearMediaItem(song).also { mediaItem ->
                     // Usar addToSongQueue en lugar de updateSongQueue directamente
                     addToSongQueue(mediaItem.mediaId, song)
                  }
               } catch (e: Exception) {
                  Log.e(TAG, "Error creando MediaItem para '${song.cancion.titulo}'", e)
                  null
               }
            }
            
            if (mediaItems.isEmpty()) {
               sendEffect(PlayerEffect.ShowError(MSG_PROCESS_ERROR))
               return@launch
            }
            
            val startIndex = mediaItems
               .indexOfFirst { it.mediaId == startSong.cancion.idCancion.toString() }
               .let { if (it >= 0) it else 0 }
            
            withContext(Dispatchers.Main) {
               try {
                  p.setMediaItems(mediaItems, startIndex, 0L)
                  p.prepare()
                  p.playWhenReady = true
               } catch (e: Exception) {
                  Log.e(TAG, "Error preparing player", e)
                  sendEffect(PlayerEffect.ShowError(MSG_PREPARE_ERROR))
               }
            }
            
            if (invalidSongs.isNotEmpty()) {
               sendEffect(PlayerEffect.ShowToast("${invalidSongs.size} $MSG_SONGS_SKIPPED"))
            }
         } catch (e: Exception) {
            Log.e(TAG, "Error setting queue", e)
            sendEffect(PlayerEffect.ShowError(MSG_LOAD_ERROR))
         }
      }
   }

   private fun togglePlayPause() {
      player?.let { if (it.isPlaying) it.pause() else it.play() }
   }
   
   private fun stopPlayback() {
      player?.let { p ->
         p.stop()
         p.clearMediaItems()
         clearSongQueue() // Usar el método clearSongQueue()
         _state.update { PlayerState() }
      }
   }

   // endregion

   // region Song Update
   
   private fun updateCurrentSongFromPlayer(mediaItem: MediaItem?) {
      if (mediaItem == null) {
         clearSongState()
         return
      }
      
      additionalDataJob?.cancel()
      
      viewModelScope.launch {
         try {
            var song = getSongFromQueue(mediaItem.mediaId)
            
            if (song == null) {
               song = withContext(Dispatchers.IO) {
                  mediaItemHelper.mediaItemACancionConArtista(mediaItem, DEFAULT_USER_ID)
               }
               song?.let {
                  addToSongQueue(mediaItem.mediaId, it) // Usar addToSongQueue
               }
            }
            
            if (song != null) {
               _state.update {
                  it.copy(
                     currentSong = song,
                     lastSongChangeTimestamp = System.currentTimeMillis(),
                     currentPositionMs = 0L,
                     scrubPositionMs = null,
                     isScrubbing = false,
                     isDragging = false,
                     isMinimizedByScroll = false, // Restaurar al cambiar canción
                     lyrics = null,
                     artistInfo = null,
                     geniusUrl = null,
                     youtubeUrl = null,
                     googleUrl = null,
                     isLoadingLyrics = false,
                     isLoadingInfo = false,
                  )
               }
               
               buildLinks(song)
               
               if (_state.value.panelMode == PlayerPanelMode.EXPANDED) {
                  loadExpandedData(song)
               }
            } else {
               clearSongState()
            }
         } catch (e: CancellationException) {
            throw e
         } catch (e: Exception) {
            Log.e(TAG, "Error updating song", e)
            clearSongState()
            sendEffect(PlayerEffect.ShowError(MSG_SONG_ERROR))
         }
      }
   }

   private fun clearSongState() {
      _state.update {
         it.copy(
            currentSong = null,
            currentPositionMs = 0L,
            scrubPositionMs = null,
            lyrics = null,
            artistInfo = null,
            geniusUrl = null,
            youtubeUrl = null,
            googleUrl = null,
         )
      }
   }

   // endregion

   // region Expanded Data Loading

   private fun loadExpandedDataIfNeeded() {
      _state.value.currentSong?.let { song ->
         if (_state.value.lyrics == null && !_state.value.isLoadingLyrics) {
            loadExpandedData(song)
         }
      }
   }

   private fun loadExpandedData(song: SongWithArtist) {
      // Cancelar job anterior
      additionalDataJob?.cancel()

      additionalDataJob =
         viewModelScope.launch {
            val songId = song.cancion.idCancion

            // Verificar que sigue siendo la canción actual
            if (!isCurrentSong(songId)) return@launch

            _state.update { it.copy(isLoadingLyrics = true, isLoadingInfo = true) }

            try {
               coroutineScope {
                  // Sincronización con Genius en background
                  launch(Dispatchers.IO) {
                     runCatching { geniusRepository.sincronizarCancionAlReproducir(song.cancion) }
                        .onFailure { e -> Log.w(TAG, "Error syncing with Genius", e) }
                  }

                  // Cargar lyrics
                  launch(Dispatchers.IO) { loadLyrics(songId) }

                  // Cargar info del artista
                  launch(Dispatchers.IO) { loadArtistInfo(song) }
               }
            } catch (e: CancellationException) {
               // Job cancelado, limpiar loading states
               if (isCurrentSong(songId)) {
                  _state.update { it.copy(isLoadingLyrics = false, isLoadingInfo = false) }
               }
            }
         }
   }

   private suspend fun loadLyrics(songId: Int) {
      try {
         withTimeout(TIMEOUT_DATA_MS) {
            lyricsDao.obtenerLetraPorIdCancion(songId).firstOrNull()?.let { lyricsEntity ->
               if (isCurrentSong(songId)) {
                  _state.update {
                     it.copy(
                        lyrics = lyricsEntity.textoLetra.trim().ifBlank { null },
                        isLoadingLyrics = false,
                     )
                  }
               }
            } ?: run { _state.update { it.copy(lyrics = null, isLoadingLyrics = false) } }
         }
      } catch (e: TimeoutCancellationException) {
         _state.update { it.copy(lyrics = null, isLoadingLyrics = false) }
      } catch (e: CancellationException) {
         throw e
      } catch (e: Exception) {
         Log.e(TAG, "Error loading lyrics", e)
         _state.update { it.copy(lyrics = null, isLoadingLyrics = false) }
      }
   }

   private suspend fun loadArtistInfo(song: SongWithArtist) {
      val artistId = song.cancion.idArtista?.toLong()
      if (artistId == null) {
         _state.update { it.copy(artistInfo = null, isLoadingInfo = false) }
         return
      }

      try {
         withTimeout(TIMEOUT_DATA_MS) {
            songDao.obtenerArtistaPorIdFlow(artistId.toInt()).firstOrNull()?.let { artistEntity ->
               if (isCurrentSong(song.cancion.idCancion)) {
                  val info =
                     buildString {
                           appendLine("🎤 ${artistEntity.nombre}")
                           artistEntity.paisOrigen?.let { appendLine("📍 $it") }
                           artistEntity.descripcion
                              ?.trim()
                              ?.takeIf { it.isNotBlank() }
                              ?.let {
                                 appendLine()
                                 append(it)
                              }
                        }
                        .trim()
                        .ifBlank { null }

                  _state.update { it.copy(artistInfo = info, isLoadingInfo = false) }
               }
            } ?: run { _state.update { it.copy(artistInfo = null, isLoadingInfo = false) } }
         }
      } catch (e: TimeoutCancellationException) {
         _state.update { it.copy(artistInfo = null, isLoadingInfo = false) }
      } catch (e: CancellationException) {
         throw e
      } catch (e: Exception) {
         Log.e(TAG, "Error loading artist info", e)
         _state.update { it.copy(artistInfo = null, isLoadingInfo = false) }
      }
   }

   private fun buildLinks(song: SongWithArtist) {
      val title = song.cancion.titulo
      val artist = song.artistaNombre ?: "Unknown"

      try {
         val query = URLEncoder.encode("$title $artist", "UTF-8")
         val artistQuery = URLEncoder.encode("$artist artist", "UTF-8")

         _state.update {
            it.copy(
               geniusUrl = "https://genius.com/search?q=$query",
               youtubeUrl = "https://www.youtube.com/results?search_query=$query",
               googleUrl = "https://www.google.com/search?q=$artistQuery",
            )
         }
      } catch (e: Exception) {
         Log.e(TAG, "Error building links", e)
      }
   }

   private fun isCurrentSong(songId: Int): Boolean {
      return _state.value.currentSong?.cancion?.idCancion == songId
   }

   // endregion

   // region Progress Updater

   private fun startProgressUpdater() {
      stopProgressUpdater()
      lastEmittedSecond = -1L

      progressUpdaterJob =
         viewModelScope.launch {
            while (isActive) {
               updateProgress()
               delay(PROGRESS_UPDATE_MS)
            }
         }
   }
   
   private fun updateProgress() {
      if (_state.value.isScrubbing) return
      
      player?.let { p ->
         val currentPosition = p.currentPosition
         val currentSecond = currentPosition / PROGRESS_EMIT_THRESHOLD_MS
         
         if (currentSecond != lastEmittedSecond) {
            _state.update { it.copy(currentPositionMs = currentPosition) }
            lastEmittedSecond = currentSecond
         }
      }
   }

   private fun stopProgressUpdater() {
      progressUpdaterJob?.cancel()
      progressUpdaterJob = null
   }

   // endregion

   // region Playback Mode / Repeat Mode

   private fun togglePlaybackMode() {
      player?.let { p ->
         val newMode = _state.value.playbackMode.toggle()
         p.shuffleModeEnabled = newMode == PlaybackMode.SHUFFLE
         _state.update { it.copy(playbackMode = newMode) }

         val message =
            if (newMode == PlaybackMode.SHUFFLE) {
               "Modo aleatorio activado"
            } else {
               "Modo aleatorio desactivado"
            }
         sendEffect(PlayerEffect.ShowToast(message))
      }
   }

   private fun toggleRepeatMode() {
      player?.let { p ->
         val newMode = _state.value.repeatMode.next()

         p.repeatMode =
            when (newMode) {
               RepeatMode.OFF -> Player.REPEAT_MODE_OFF
               RepeatMode.ALL -> Player.REPEAT_MODE_ALL
               RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }

         _state.update { it.copy(repeatMode = newMode) }

         val message =
            when (newMode) {
               RepeatMode.OFF -> "Repetición desactivada"
               RepeatMode.ALL -> "Repetir lista"
               RepeatMode.ONE -> "Repetir canción"
            }
         sendEffect(PlayerEffect.ShowToast(message))
      }
   }

   // endregion

   // region Favorites

   private fun observeFavoriteState() {
      viewModelScope.launch {
         _state
            .map { it.currentSong?.cancion?.idCancion }
            .distinctUntilChanged()
            .collect { songId -> songId?.let { updateFavoriteState(it.toLong()) } }
      }
   }

   private suspend fun updateFavoriteState(songId: Long) {
      runCatching {
         songDao.obtenerCancionConArtistaPorId(songId.toInt(), DEFAULT_USER_ID)?.let { song ->
            _state.update { it.copy(isFavorite = song.esFavorita) }
         }
      }
   }

   private fun toggleFavorite() {
      viewModelScope.launch {
         val song = _state.value.currentSong?.cancion ?: return@launch
         val newState = !_state.value.isFavorite

         try {
            if (newState) {
               songDao.agregarAFavoritos(FavoriteEntity(DEFAULT_USER_ID, song.idCancion))
               sendEffect(PlayerEffect.ShowToast("Agregado a favoritos ❤️"))
            } else {
               songDao.quitarDeFavoritos(DEFAULT_USER_ID, song.idCancion)
               sendEffect(PlayerEffect.ShowToast("Quitado de favoritos"))
            }
            _state.update { it.copy(isFavorite = newState) }
         } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite", e)
            sendEffect(PlayerEffect.ShowError("Error al actualizar favorito"))
         }
      }
   }

   // endregion

   // region Public Utilities

   fun playSong(song: SongWithArtist) {
      setQueueAndPlay(listOf(song), song)
   }

   fun isConnected(): Boolean = mediaController != null

   fun hasSong(): Boolean = _state.value.hasSong

   // endregion

   // region Effect Helper

   private fun sendEffect(effect: PlayerEffect) {
      viewModelScope.launch { _effects.send(effect) }
   }

   // endregion

   // region Cleanup
   
   override fun onCleared() {
      additionalDataJob?.cancel()
      progressUpdaterJob?.cancel()
      scrollJob?.cancel()
      
      playerListener?.let { player?.removeListener(it) }
      playerListener = null
      
      runCatching { MediaController.releaseFuture(controllerFuture) }
      
      mediaController = null
      
      clearSongQueue()
      
      super.onCleared()
   }

   // endregion
}
