package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.LocalSong

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_songs")
data class CachedSongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val contentUriString: String,
    val albumArtUriString: String?,
    val genre: String,
    val trackNumber: Int,
    val year: Int,
    val sizeBytes: Long,
    val dateAddedSeconds: Long,
    val dateModifiedSeconds: Long,
    val mimeType: String,
    val folderName: String,
    val folderPath: String
) {
    fun toLocalSong(isFavorite: Boolean = false): LocalSong {
        return LocalSong(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            durationMs = durationMs,
            path = path,
            contentUriString = contentUriString,
            albumArtUriString = albumArtUriString,
            genre = genre,
            trackNumber = trackNumber,
            year = year,
            sizeBytes = sizeBytes,
            dateAddedSeconds = dateAddedSeconds,
            dateModifiedSeconds = dateModifiedSeconds,
            mimeType = mimeType,
            folderName = folderName,
            folderPath = folderPath,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromLocalSong(song: LocalSong): CachedSongEntity {
            return CachedSongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                albumId = song.albumId,
                durationMs = song.durationMs,
                path = song.path,
                contentUriString = song.contentUriString,
                albumArtUriString = song.albumArtUriString,
                genre = song.genre,
                trackNumber = song.trackNumber,
                year = song.year,
                sizeBytes = song.sizeBytes,
                dateAddedSeconds = song.dateAddedSeconds,
                dateModifiedSeconds = song.dateModifiedSeconds,
                mimeType = song.mimeType,
                folderName = song.folderName,
                folderPath = song.folderPath
            )
        }
    }
}
