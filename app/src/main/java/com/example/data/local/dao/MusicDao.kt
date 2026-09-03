package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedSongEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlayHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites")
    fun getAllFavoriteIdsFlow(): Flow<List<Long>>

    @Query("SELECT songId FROM favorites")
    suspend fun getAllFavoriteIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavoriteFlow(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC, addedAt ASC")
    fun getSongIdsForPlaylistFlow(playlistId: Long): Flow<List<Long>>

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC, addedAt ASC")
    suspend fun getSongIdsForPlaylist(playlistId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCountForPlaylistFlow(playlistId: Long): Flow<Int>
}

@Dao
interface PlayHistoryDao {
    @Query("SELECT songId FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentSongIdsFlow(limit: Int = 50): Flow<List<Long>>

    @Query("SELECT songId FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentSongIds(limit: Int = 50): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayRecord(record: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE songId = :songId")
    suspend fun removeSongFromHistory(songId: Long)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()
}

@Dao
interface CachedSongDao {
    @Query("SELECT * FROM cached_songs ORDER BY title ASC")
    fun getAllCachedSongsFlow(): Flow<List<CachedSongEntity>>

    @Query("SELECT * FROM cached_songs")
    suspend fun getAllCachedSongs(): List<CachedSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<CachedSongEntity>)

    @Query("DELETE FROM cached_songs WHERE id NOT IN (:validIds)")
    suspend fun removeDeletedSongs(validIds: List<Long>)

    @Query("DELETE FROM cached_songs")
    suspend fun clearAll()
}
