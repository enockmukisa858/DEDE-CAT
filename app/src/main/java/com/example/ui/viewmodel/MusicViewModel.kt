package com.example.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BlackDudeMusicApplication
import com.example.model.LocalAlbum
import com.example.model.LocalArtist
import com.example.model.LocalFolder
import com.example.model.LocalGenre
import com.example.model.LocalPlaylist
import com.example.model.LocalSong
import com.example.model.SongSortOrder
import com.example.service.MusicPlaybackService
import com.example.service.ServiceRepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BottomTab(val title: String) {
    HOME("Home"),
    LIBRARY("Library"),
    SEARCH("Search"),
    EQUALIZER("Equalizer"),
    ABOUT("About")
}

enum class LibrarySubTab(val title: String) {
    SONGS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    FOLDERS("Folders"),
    PLAYLISTS("Playlists"),
    FAVORITES("Favorites"),
    GENRES("Genres")
}

data class MusicUiState(
    // Library Data
    val allSongs: List<LocalSong> = emptyList(),
    val favoriteSongs: List<LocalSong> = emptyList(),
    val recentlyPlayedSongs: List<LocalSong> = emptyList(),
    val recentlyAddedSongs: List<LocalSong> = emptyList(),
    val albums: List<LocalAlbum> = emptyList(),
    val artists: List<LocalArtist> = emptyList(),
    val genres: List<LocalGenre> = emptyList(),
    val folders: List<LocalFolder> = emptyList(),
    val playlists: List<LocalPlaylist> = emptyList(),

    // Storage and Scanning
    val hasStoragePermission: Boolean = false,
    val isScanning: Boolean = false,

    // Playback State
    val currentSong: LocalSong? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<LocalSong> = emptyList(),
    val queueIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: ServiceRepeatMode = ServiceRepeatMode.ALL,
    val isBuffering: Boolean = false,

    // UI & Navigation
    val selectedTab: BottomTab = BottomTab.HOME,
    val selectedLibrarySubTab: LibrarySubTab = LibrarySubTab.SONGS,
    val isNowPlayingExpanded: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SongSortOrder = SongSortOrder.TITLE_ASC,

    // Detail Views
    val selectedAlbumDetail: LocalAlbum? = null,
    val selectedArtistDetail: LocalArtist? = null,
    val selectedFolderDetail: LocalFolder? = null,
    val selectedPlaylistDetail: LocalPlaylist? = null,

    // Dialogs
    val songDetailDialogTrack: LocalSong? = null,
    val addToPlaylistDialogTrack: LocalSong? = null,

    // Audio Equalizer settings
    val bassBoost: Float = 40f,
    val virtualizer: Float = 30f,
    val treble: Float = 20f,
    val equalizerPreset: String = "Normal"
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BlackDudeMusicApplication).musicRepository
    private var playbackService: MusicPlaybackService? = null
    private var isBound = false

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicPlaybackService.LocalBinder
            playbackService = binder?.getService()
            isBound = true

            // Observe playback state from Service
            playbackService?.let { s ->
                viewModelScope.launch {
                    s.playbackState.collectLatest { state ->
                        _uiState.update { current ->
                            current.copy(
                                currentSong = state.currentSong,
                                isPlaying = state.isPlaying,
                                currentPositionMs = state.currentPositionMs,
                                durationMs = state.durationMs,
                                queue = state.queue,
                                queueIndex = state.queueIndex,
                                isShuffle = state.isShuffle,
                                repeatMode = state.repeatMode,
                                isBuffering = state.isBuffering
                            )
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isBound = false
        }
    }

    init {
        bindPlaybackService()
        loadCachedAndObserve()
    }

    private fun bindPlaybackService() {
        val context = getApplication<Application>()
        val intent = Intent(context, MusicPlaybackService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun loadCachedAndObserve() {
        viewModelScope.launch {
            // Load cache immediately
            repository.loadCachedSongs()
        }

        // Observe scanning state
        viewModelScope.launch {
            repository.isScanning.collectLatest { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }

        // Combine songs with favorites, history, playlists
        viewModelScope.launch {
            combine(
                repository.songsWithFavoriteState,
                repository.recentlyPlayedIds,
                repository.playlists
            ) { songs, recentIds, playlistsList ->
                Triple(songs, recentIds, playlistsList)
            }.collectLatest { (songs, recentIds, playlistsList) ->
                val sortedSongs = sortSongs(songs, _uiState.value.sortOrder)
                val favs = songs.filter { it.isFavorite }
                val recents = recentIds.mapNotNull { id -> songs.find { it.id == id } }.distinctBy { it.id }
                val recentlyAdded = songs.sortedByDescending { it.dateAddedSeconds }
                val albums = repository.groupSongsByAlbum(songs)
                val artists = repository.groupSongsByArtist(songs)
                val genres = repository.groupSongsByGenre(songs)
                val folders = repository.groupSongsByFolder(songs)

                _uiState.update { current ->
                    current.copy(
                        allSongs = sortedSongs,
                        favoriteSongs = favs,
                        recentlyPlayedSongs = recents,
                        recentlyAddedSongs = recentlyAdded,
                        albums = albums,
                        artists = artists,
                        genres = genres,
                        folders = folders,
                        playlists = playlistsList
                    )
                }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = isGranted) }
        if (isGranted) {
            scanLocalMusic()
        }
    }

    fun scanLocalMusic() {
        viewModelScope.launch {
            repository.scanDevice()
        }
    }

    // Playback Controls
    private fun getActivePlaybackService(): MusicPlaybackService? = playbackService ?: MusicPlaybackService.instance

    fun playSong(song: LocalSong, queue: List<LocalSong> = emptyList(), index: Int = -1) {
        val finalQueue = when {
            queue.size > 1 -> queue
            _uiState.value.allSongs.size > 1 -> _uiState.value.allSongs
            queue.isNotEmpty() -> queue
            _uiState.value.allSongs.isNotEmpty() -> _uiState.value.allSongs
            else -> listOf(song)
        }
        val finalIndex = if (index in finalQueue.indices && finalQueue[index].id == song.id) {
            index
        } else {
            val found = finalQueue.indexOfFirst { it.id == song.id }
            if (found >= 0) found else 0
        }
        getActivePlaybackService()?.playSong(song, finalQueue, finalIndex)
    }

    fun togglePlayPause() {
        val service = getActivePlaybackService()
        if (_uiState.value.currentSong == null && _uiState.value.allSongs.isNotEmpty()) {
            playSong(_uiState.value.allSongs.first(), _uiState.value.allSongs, 0)
        } else {
            service?.togglePlayPause()
        }
    }

    fun next() {
        getActivePlaybackService()?.next()
    }

    fun previous() {
        getActivePlaybackService()?.previous()
    }

    fun seekTo(positionMs: Long) {
        getActivePlaybackService()?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        getActivePlaybackService()?.toggleShuffle()
    }

    fun toggleRepeat() {
        getActivePlaybackService()?.toggleRepeat()
    }

    fun toggleFavorite(song: LocalSong) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id)
        }
    }

    // Playlists
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_uiState.value.selectedPlaylistDetail?.id == playlistId) {
                _uiState.update { it.copy(selectedPlaylistDetail = null) }
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _uiState.update { it.copy(addToPlaylistDialogTrack = null) }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // Navigation & View Modes
    fun selectTab(tab: BottomTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectLibrarySubTab(subTab: LibrarySubTab) {
        _uiState.update {
            it.copy(
                selectedLibrarySubTab = subTab,
                selectedAlbumDetail = null,
                selectedArtistDetail = null,
                selectedFolderDetail = null,
                selectedPlaylistDetail = null
            )
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isNowPlayingExpanded = expanded) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(order: SongSortOrder) {
        _uiState.update { current ->
            current.copy(
                sortOrder = order,
                allSongs = sortSongs(current.allSongs, order)
            )
        }
    }

    fun selectAlbumDetail(album: LocalAlbum?) {
        _uiState.update { it.copy(selectedAlbumDetail = album) }
    }

    fun selectArtistDetail(artist: LocalArtist?) {
        _uiState.update { it.copy(selectedArtistDetail = artist) }
    }

    fun selectFolderDetail(folder: LocalFolder?) {
        _uiState.update { it.copy(selectedFolderDetail = folder) }
    }

    fun selectPlaylistDetail(playlist: LocalPlaylist?) {
        _uiState.update { it.copy(selectedPlaylistDetail = playlist) }
    }

    fun showSongDetails(song: LocalSong?) {
        _uiState.update { it.copy(songDetailDialogTrack = song) }
    }

    fun showAddToPlaylist(song: LocalSong?) {
        _uiState.update { it.copy(addToPlaylistDialogTrack = song) }
    }

    fun setBassBoost(value: Float) {
        _uiState.update { it.copy(bassBoost = value) }
    }

    fun setVirtualizer(value: Float) {
        _uiState.update { it.copy(virtualizer = value) }
    }

    fun setTreble(value: Float) {
        _uiState.update { it.copy(treble = value) }
    }

    fun setEqualizerPreset(preset: String) {
        val (b, v, t) = when (preset) {
            "Bass Boost" -> Triple(85f, 40f, 20f)
            "Vocal Boost" -> Triple(20f, 60f, 75f)
            "Electronic" -> Triple(75f, 70f, 60f)
            "Acoustic" -> Triple(35f, 50f, 50f)
            "Rock" -> Triple(65f, 45f, 65f)
            else -> Triple(40f, 30f, 20f)
        }
        _uiState.update {
            it.copy(
                equalizerPreset = preset,
                bassBoost = b,
                virtualizer = v,
                treble = t
            )
        }
    }

    private fun sortSongs(songs: List<LocalSong>, order: SongSortOrder): List<LocalSong> {
        return when (order) {
            SongSortOrder.TITLE_ASC -> songs.sortedBy { it.displayTitle.lowercase() }
            SongSortOrder.TITLE_DESC -> songs.sortedByDescending { it.displayTitle.lowercase() }
            SongSortOrder.ARTIST_ASC -> songs.sortedBy { it.displayArtist.lowercase() }
            SongSortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAddedSeconds }
            SongSortOrder.DURATION_DESC -> songs.sortedByDescending { it.durationMs }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
}
