package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalAlbum
import com.example.model.LocalArtist
import com.example.model.LocalFolder
import com.example.model.LocalPlaylist
import com.example.model.LocalSong
import com.example.ui.components.GlassHeader
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPill
import com.example.ui.components.LocalArtworkImage
import com.example.ui.glass.glassCard
import com.example.ui.glass.glassCircleButton
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun LibraryScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Playlists") }
    val categories = listOf("Playlists", "Albums", "Artists", "Folders", "Favorites")

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // If viewing an album, artist, or playlist detail drill-down
    var viewingAlbum by remember { mutableStateOf<LocalAlbum?>(null) }
    var viewingArtist by remember { mutableStateOf<LocalArtist?>(null) }
    var viewingFolder by remember { mutableStateOf<LocalFolder?>(null) }
    var viewingPlaylist by remember { mutableStateOf<LocalPlaylist?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassHeader(
                title = "MUSIC LIBRARY",
                subtitle = "LOCAL STORAGE VAULT",
                accentColor = GoldPrimary
            )
        }

        // Subcategory Pills (when not drilled down into detail view)
        if (viewingAlbum == null && viewingArtist == null && viewingFolder == null && viewingPlaylist == null) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        GlassPill(
                            text = category,
                            isSelected = isSelected,
                            accentColor = GoldPrimary,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        // 1. DETAIL DRILL DOWN VIEW
        if (viewingAlbum != null) {
            val alb = viewingAlbum!!
            val albumSongs = uiState.allSongs.filter { it.album == alb.title }
            item {
                DetailHeaderCard(
                    title = alb.title,
                    subtitle = "${alb.artist} • ${alb.songCount} songs",
                    icon = Icons.Default.Album,
                    iconTint = GoldSecondary,
                    onBack = { viewingAlbum = null },
                    onPlayAll = {
                        if (albumSongs.isNotEmpty()) viewModel.playSong(albumSongs.first(), albumSongs, 0)
                    }
                )
            }
            items(albumSongs) { song ->
                val isCurrent = uiState.currentSong?.id == song.id
                SongLocalRow(
                    song = song,
                    isCurrent = isCurrent,
                    isPlaying = uiState.isPlaying && isCurrent,
                    onPlay = { viewModel.playSong(song, albumSongs, albumSongs.indexOf(song)) },
                    onToggleFav = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                )
            }
        } else if (viewingArtist != null) {
            val art = viewingArtist!!
            val artistSongs = uiState.allSongs.filter { it.artist == art.name }
            item {
                DetailHeaderCard(
                    title = art.name,
                    subtitle = "${art.songCount} tracks in local storage",
                    icon = Icons.Default.Person,
                    iconTint = GoldPrimary,
                    onBack = { viewingArtist = null },
                    onPlayAll = {
                        if (artistSongs.isNotEmpty()) viewModel.playSong(artistSongs.first(), artistSongs, 0)
                    }
                )
            }
            items(artistSongs) { song ->
                val isCurrent = uiState.currentSong?.id == song.id
                SongLocalRow(
                    song = song,
                    isCurrent = isCurrent,
                    isPlaying = uiState.isPlaying && isCurrent,
                    onPlay = { viewModel.playSong(song, artistSongs, artistSongs.indexOf(song)) },
                    onToggleFav = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                )
            }
        } else if (viewingFolder != null) {
            val fld = viewingFolder!!
            val folderSongs = uiState.allSongs.filter { it.folderName == fld.name }
            item {
                DetailHeaderCard(
                    title = fld.name,
                    subtitle = "${fld.path} • ${fld.songCount} songs",
                    icon = Icons.Default.Folder,
                    iconTint = GoldSecondary,
                    onBack = { viewingFolder = null },
                    onPlayAll = {
                        if (folderSongs.isNotEmpty()) viewModel.playSong(folderSongs.first(), folderSongs, 0)
                    }
                )
            }
            items(folderSongs) { song ->
                val isCurrent = uiState.currentSong?.id == song.id
                SongLocalRow(
                    song = song,
                    isCurrent = isCurrent,
                    isPlaying = uiState.isPlaying && isCurrent,
                    onPlay = { viewModel.playSong(song, folderSongs, folderSongs.indexOf(song)) },
                    onToggleFav = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                )
            }
        } else if (viewingPlaylist != null) {
            val pl = viewingPlaylist!!
            val playlistSongs = pl.songs
            item {
                DetailHeaderCard(
                    title = pl.name,
                    subtitle = "${playlistSongs.size} tracks",
                    icon = Icons.Default.PlaylistPlay,
                    iconTint = GoldPrimary,
                    onBack = { viewingPlaylist = null },
                    onPlayAll = {
                        if (playlistSongs.isNotEmpty()) viewModel.playSong(playlistSongs.first(), playlistSongs, 0)
                    }
                )
            }
            if (playlistSongs.isEmpty()) {
                item {
                    Text(
                        text = "This playlist is empty. Add songs from your song library or Now Playing screen.",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(playlistSongs) { song ->
                    val isCurrent = uiState.currentSong?.id == song.id
                    SongLocalRow(
                        song = song,
                        isCurrent = isCurrent,
                        isPlaying = uiState.isPlaying && isCurrent,
                        onPlay = { viewModel.playSong(song, playlistSongs, playlistSongs.indexOf(song)) },
                        onToggleFav = { viewModel.toggleFavorite(song) },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            // 2. MAIN CATEGORY VIEWS
            when (selectedCategory) {
                "Playlists" -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOCAL PLAYLISTS (${uiState.playlists.size})",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            GlassIconButton(
                                size = 36.dp,
                                accentColor = GoldPrimary,
                                onClick = { showCreatePlaylistDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (uiState.playlists.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                                    .glassPanel(shape = RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("No Playlists Yet", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Create custom playlists from your local music.", color = TextTertiary, fontSize = 12.sp)
                                    Button(
                                        onClick = { showCreatePlaylistDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("+ Create Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(uiState.playlists) { pl ->
                            PlaylistRowItem(
                                playlist = pl,
                                onClick = { viewingPlaylist = pl },
                                onDelete = { viewModel.deletePlaylist(pl.id) },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                "Albums" -> {
                    items(uiState.albums) { alb ->
                        AlbumRowItem(
                            album = alb,
                            onClick = { viewingAlbum = alb },
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
                        )
                    }
                }

                "Artists" -> {
                    items(uiState.artists) { art ->
                        ArtistRowItem(
                            artist = art,
                            onClick = { viewingArtist = art },
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
                        )
                    }
                }

                "Folders" -> {
                    items(uiState.folders) { fld ->
                        FolderRowItem(
                            folder = fld,
                            onClick = { viewingFolder = fld },
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
                        )
                    }
                }

                "Favorites" -> {
                    val favorites = uiState.allSongs.filter { it.isFavorite }
                    if (favorites.isEmpty()) {
                        item {
                            Text(
                                text = "No favorite songs yet. Tap the heart icon on any song to add it here.",
                                color = TextTertiary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                            )
                        }
                    } else {
                        items(favorites) { song ->
                            val isCurrent = uiState.currentSong?.id == song.id
                            SongLocalRow(
                                song = song,
                                isCurrent = isCurrent,
                                isPlaying = uiState.isPlaying && isCurrent,
                                onPlay = { viewModel.playSong(song, favorites, favorites.indexOf(song)) },
                                onToggleFav = { viewModel.toggleFavorite(song) },
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text("Create Playlist", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name", color = TextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x0CFFFFFF),
                        focusedIndicatorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color(0xFF141208),
            modifier = Modifier.border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        )
    }
}

@Composable
private fun DetailHeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onBack: () -> Unit,
    onPlayAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .glassPanel(shape = RoundedCornerShape(18.dp), backgroundColor = Color(0x281A1608), borderColors = listOf(GoldPrimary.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f)))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                GlassIconButton(size = 38.dp, accentColor = GoldPrimary, onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .glassCircleButton(
                        sizeDp = 44.dp,
                        accentColor = GoldPrimary,
                        isPrimary = true,
                        glowAlpha = 0.3f,
                        onClick = onPlayAll
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun PlaylistRowItem(
    playlist: LocalPlaylist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(14.dp), accentColor = GoldPrimary, onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                }

                Column {
                    Text(text = playlist.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${playlist.songCount} songs", color = TextTertiary, fontSize = 11.sp)
                }
            }

            GlassIconButton(
                size = 34.dp,
                accentColor = Color(0xFFFF5252),
                backgroundColor = Color(0x0CFFFFFF),
                onClick = onDelete
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = TextTertiary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AlbumRowItem(
    album: LocalAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(14.dp), accentColor = GoldPrimary, onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocalArtworkImage(
                albumArtUri = album.albumArtUriString,
                title = album.title,
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(8.dp),
                fallbackIcon = Icons.Default.Album,
                fallbackTint = GoldSecondary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = album.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${album.artist} • ${album.songCount} tracks", color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ArtistRowItem(
    artist: LocalArtist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(14.dp), accentColor = GoldPrimary, onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = artist.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${artist.songCount} songs", color = TextTertiary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FolderRowItem(
    folder: LocalFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(14.dp), accentColor = GoldPrimary, onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = folder.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${folder.path} • ${folder.songCount} songs", color = TextTertiary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
