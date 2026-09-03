package com.example.model

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.io.File

/**
 * Represents an authentic local audio file located on user device storage.
 */
data class LocalSong(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val contentUriString: String,
    val albumArtUriString: String? = null,
    val genre: String = "General",
    val trackNumber: Int = 0,
    val year: Int = 0,
    val sizeBytes: Long = 0L,
    val dateAddedSeconds: Long = 0L,
    val dateModifiedSeconds: Long = 0L,
    val mimeType: String = "audio/*",
    val folderName: String = "Music",
    val folderPath: String = "",
    val isFavorite: Boolean = false
) {
    val durationSeconds: Int
        get() = (durationMs / 1000).toInt()

    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remMinutes = minutes % 60
                String.format("%d:%02d:%02d", hours, remMinutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    val sizeFormatted: String
        get() {
            if (sizeBytes <= 0) return "0 MB"
            val mb = sizeBytes / (1024.0 * 1024.0)
            return String.format("%.1f MB", mb)
        }

    val fileExtension: String
        get() {
            val ext = path.substringAfterLast('.', "")
            return if (ext.isNotBlank()) ext.uppercase() else "AUDIO"
        }

    val displayArtist: String
        get() = if (artist.isBlank() || artist.equals("<unknown>", ignoreCase = true)) "Unknown Artist" else artist

    val displayAlbum: String
        get() = if (album.isBlank() || album.equals("<unknown>", ignoreCase = true)) "Unknown Album" else album

    val displayTitle: String
        get() {
            if (title.isNotBlank() && !title.equals("<unknown>", ignoreCase = true)) {
                return title
            }
            // Fallback to filename without extension
            val fileName = path.substringAfterLast(File.separator, "").substringBeforeLast('.')
            return if (fileName.isNotBlank()) fileName else "Unknown Track"
        }
}

data class LocalAlbum(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUriString: String? = null,
    val songCount: Int = 0,
    val year: Int = 0,
    val songs: List<LocalSong> = emptyList()
)

data class LocalArtist(
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val songs: List<LocalSong> = emptyList()
)

data class LocalGenre(
    val name: String,
    val songCount: Int = 0,
    val songs: List<LocalSong> = emptyList()
)

data class LocalFolder(
    val name: String,
    val path: String,
    val songCount: Int = 0,
    val songs: List<LocalSong> = emptyList()
)

data class LocalPlaylist(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val songs: List<LocalSong> = emptyList()
)

enum class SongSortOrder(val displayName: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    ARTIST_ASC("Artist (A-Z)"),
    DATE_ADDED_DESC("Recently Added"),
    DURATION_DESC("Longest First")
}
