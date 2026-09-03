package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CachedSongEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlayHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.scanner.LocalMusicScanner
import com.example.model.LocalAlbum
import com.example.model.LocalArtist
import com.example.model.LocalFolder
import com.example.model.LocalGenre
import com.example.model.LocalPlaylist
import com.example.model.LocalSong
import com.example.model.SongSortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val favoriteDao = database.favoriteDao()
    private val playlistDao = database.playlistDao()
    private val playHistoryDao = database.playHistoryDao()
    private val cachedSongDao = database.cachedSongDao()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _rawSongs = MutableStateFlow<List<LocalSong>>(emptyList())
    val rawSongs: StateFlow<List<LocalSong>> = _rawSongs.asStateFlow()

    val currentSongs: List<LocalSong>
        get() = _rawSongs.value

    // Favorite Song IDs
    val favoriteSongIds: Flow<List<Long>> = favoriteDao.getAllFavoriteIdsFlow()

    // Recently Played Song IDs
    val recentlyPlayedIds: Flow<List<Long>> = playHistoryDao.getRecentSongIdsFlow(50)

    // All Playlists
    val playlists: Flow<List<LocalPlaylist>> = playlistDao.getAllPlaylistsFlow().combine(_rawSongs) { entityList, allSongs ->
        entityList.map { entity ->
            val songIds = playlistDao.getSongIdsForPlaylist(entity.id)
            val songsInPlaylist = songIds.mapNotNull { id -> allSongs.find { it.id == id } }
            LocalPlaylist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                songCount = songsInPlaylist.size,
                songs = songsInPlaylist
            )
        }
    }

    // Songs combined with Favorites state
    val songsWithFavoriteState: Flow<List<LocalSong>> = combine(_rawSongs, favoriteSongIds) { songs, favIds ->
        val favSet = favIds.toSet()
        songs.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }
    }

    suspend fun loadCachedSongs() = withContext(Dispatchers.IO) {
        val cached = cachedSongDao.getAllCachedSongs()
        if (cached.isNotEmpty()) {
            val favIds = favoriteDao.getAllFavoriteIds().toSet()
            _rawSongs.value = cached.map { it.toLocalSong(isFavorite = favIds.contains(it.id)) }
        }
    }

    suspend fun scanDevice() = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val scanned = LocalMusicScanner.scanDeviceAudio(context)
            val favIds = favoriteDao.getAllFavoriteIds().toSet()
            val songsWithFav = scanned.map { it.copy(isFavorite = favIds.contains(it.id)) }
            _rawSongs.value = songsWithFav

            // Update Room database cache
            cachedSongDao.clearAll()
            cachedSongDao.insertAll(scanned.map { CachedSongEntity.fromLocalSong(it) })
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val currentFavs = favoriteDao.getAllFavoriteIds()
        if (currentFavs.contains(songId)) {
            favoriteDao.deleteFavorite(songId)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        playHistoryDao.insertPlayRecord(PlayHistoryEntity(songId = songId, playedAt = System.currentTimeMillis()))
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
    }

    fun groupSongsByAlbum(songs: List<LocalSong>): List<LocalAlbum> {
        return songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
            val first = albumSongs.first()
            LocalAlbum(
                id = albumId,
                title = first.displayAlbum,
                artist = first.displayArtist,
                albumArtUriString = first.albumArtUriString,
                songCount = albumSongs.size,
                year = first.year,
                songs = albumSongs.sortedBy { it.trackNumber }
            )
        }.sortedBy { it.title }
    }

    fun groupSongsByArtist(songs: List<LocalSong>): List<LocalArtist> {
        return songs.groupBy { it.displayArtist }.map { (artistName, artistSongs) ->
            val albumsCount = artistSongs.map { it.albumId }.distinct().size
            LocalArtist(
                name = artistName,
                songCount = artistSongs.size,
                albumCount = albumsCount,
                songs = artistSongs.sortedBy { it.displayTitle }
            )
        }.sortedBy { it.name }
    }

    fun groupSongsByGenre(songs: List<LocalSong>): List<LocalGenre> {
        return songs.groupBy { it.genre }.map { (genreName, genreSongs) ->
            LocalGenre(
                name = genreName,
                songCount = genreSongs.size,
                songs = genreSongs
            )
        }.sortedBy { it.name }
    }

    fun groupSongsByFolder(songs: List<LocalSong>): List<LocalFolder> {
        return songs.groupBy { it.folderPath }.map { (path, folderSongs) ->
            val folderName = folderSongs.first().folderName
            LocalFolder(
                name = folderName,
                path = path,
                songCount = folderSongs.size,
                songs = folderSongs.sortedBy { it.displayTitle }
            )
        }.sortedBy { it.name }
    }
}
