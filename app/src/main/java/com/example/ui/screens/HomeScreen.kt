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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalAlbum
import com.example.model.LocalSong
import com.example.ui.components.GlassHeader
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassPill
import com.example.ui.components.LocalArtworkImage
import com.example.ui.components.MiniWaveformIcon
import com.example.ui.glass.animatedGlassGlow
import com.example.ui.glass.glassCard
import com.example.ui.glass.glassCircleButton
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.BottomTab
import com.example.ui.viewmodel.LibrarySubTab
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val songs = uiState.allSongs
    val filterTabs = listOf("All Songs", "Favorites", "Recent", "FLAC / Hi-Res")
    var selectedFilter by remember { mutableStateOf("All Songs") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header
        item {
            GlassHeader(
                title = "BD MUSIC PLAYER",
                subtitle = "LOCAL AUDIO VAULT • ${songs.size} TRACKS",
                accentColor = GoldPrimary,
                isScanning = uiState.isScanning,
                onRescanClick = { viewModel.scanLocalMusic() },
                onVisualizerClick = { viewModel.selectTab(BottomTab.EQUALIZER) }
            )
        }

        if (uiState.isScanning) {
            item {
                ScanningBanner(accentColor = GoldPrimary)
            }
        }

        if (songs.isEmpty() && !uiState.isScanning) {
            item {
                EmptyStorageNotice(
                    permissionGranted = uiState.hasStoragePermission,
                    onScanClick = { viewModel.scanLocalMusic() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                )
            }
        } else if (songs.isNotEmpty()) {
            // 2. Featured / Current Hero Card
            val heroSong = uiState.currentSong ?: songs.first()
            item {
                FeaturedLocalHero(
                    song = heroSong,
                    isPlaying = uiState.isPlaying && uiState.currentSong?.id == heroSong.id,
                    onPlayClick = {
                        if (uiState.currentSong?.id == heroSong.id) {
                            viewModel.togglePlayPause()
                        } else {
                            viewModel.playSong(heroSong, songs, songs.indexOf(heroSong).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                )
            }

            // 3. Filter Pills
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterTabs) { tab ->
                        val isSelected = selectedFilter == tab
                        GlassPill(
                            text = tab,
                            isSelected = isSelected,
                            accentColor = GoldPrimary,
                            onClick = { selectedFilter = tab }
                        )
                    }
                }
            }

            // 4. Albums / Folders Quick Horizontal Bar
            if (uiState.albums.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(
                            title = "LOCAL ALBUMS",
                            badge = "${uiState.albums.size} ALBUMS",
                            accentColor = GoldSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.albums) { album ->
                                AlbumLocalCard(
                                    album = album,
                                    onClick = {
                                        viewModel.selectAlbumDetail(album)
                                        viewModel.selectLibrarySubTab(LibrarySubTab.ALBUMS)
                                        viewModel.selectTab(BottomTab.LIBRARY)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Song Catalog List
            val displaySongs = when (selectedFilter) {
                "Favorites" -> uiState.favoriteSongs
                "FLAC / Hi-Res" -> songs.filter { it.fileExtension.equals("FLAC", ignoreCase = true) || it.fileExtension.equals("WAV", ignoreCase = true) }
                "Recent" -> uiState.recentlyPlayedSongs.ifEmpty { songs.sortedByDescending { it.dateAddedSeconds } }
                else -> songs
            }

            item {
                SectionHeader(
                    title = "MUSIC DIRECTORY",
                    badge = "${displaySongs.size} TRACKS",
                    accentColor = GoldPrimary
                )
            }

            if (displaySongs.isEmpty()) {
                item {
                    Text(
                        text = "No songs matching this category.",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(displaySongs) { song ->
                    val isCurrent = uiState.currentSong?.id == song.id
                    SongLocalRow(
                        song = song,
                        isCurrent = isCurrent,
                        isPlaying = uiState.isPlaying && isCurrent,
                        onPlay = {
                            val idx = displaySongs.indexOf(song)
                            viewModel.playSong(song, displaySongs, idx)
                        },
                        onToggleFav = { viewModel.toggleFavorite(song) },
                        onMore = { viewModel.showSongDetails(song) },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    badge: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.SansSerif
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(0.8.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ScanningBanner(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .glassPanel(
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color(0x18D4AF37)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            color = accentColor,
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
        Column {
            Text(
                text = "Scanning Device Storage...",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Detecting MP3, FLAC, WAV, AAC, M4A & OGG files",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyStorageNotice(
    permissionGranted: Boolean,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassPanel(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0x1E141208)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No Local Music Found",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (permissionGranted) {
                    "No audio files found in device storage. Transfer your music files (MP3, FLAC, WAV, M4A) to your device or SD card and tap scan."
                } else {
                    "Storage permission is needed to scan your device's audio library."
                },
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Scan Device Storage",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FeaturedLocalHero(
    song: LocalSong,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassPanel(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0x281A1608),
                borderColors = listOf(
                    GoldPrimary.copy(alpha = 0.40f),
                    GoldMuted.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.05f)
                ),
                borderWidth = 1.dp
            )
            .height(160.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalArtworkImage(
                albumArtUri = song.albumArtUriString,
                title = song.displayTitle,
                modifier = Modifier
                    .size(110.dp),
                shape = RoundedCornerShape(14.dp),
                fallbackTint = GoldPrimary
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldPrimary.copy(alpha = 0.20f))
                        .border(0.8.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LOCAL AUDIO • ${song.fileExtension}",
                        color = GoldChampagne,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = song.displayTitle,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = song.displayArtist,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${song.sizeFormatted} • ${song.durationFormatted}",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .glassCircleButton(
                        sizeDp = 48.dp,
                        accentColor = GoldPrimary,
                        isPrimary = true,
                        glowAlpha = 0.3f,
                        onClick = onPlayClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumLocalCard(
    album: LocalAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(130.dp)
            .glassCard(
                shape = RoundedCornerShape(14.dp),
                accentColor = GoldPrimary,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Column {
            LocalArtworkImage(
                albumArtUri = album.albumArtUriString,
                title = album.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(10.dp),
                fallbackIcon = Icons.Default.Album,
                fallbackTint = GoldSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = album.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${album.songCount} tracks",
                color = TextTertiary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongLocalRow(
    song: LocalSong,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFav: () -> Unit,
    onMore: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val cardMod = if (isCurrent) {
        Modifier.animatedGlassGlow(accentColor = GoldPrimary, shape = shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(cardMod)
            .glassCard(
                shape = shape,
                accentColor = GoldPrimary,
                onClick = onPlay
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(42.dp)) {
                    LocalArtworkImage(
                        albumArtUri = song.albumArtUriString,
                        title = song.displayTitle,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        fallbackTint = GoldPrimary
                    )

                    if (isCurrent && isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x80000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniWaveformIcon(accentColor = GoldPrimary)
                        }
                    }
                }

                Column {
                    Text(
                        text = song.displayTitle,
                        color = if (isCurrent) GoldChampagne else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = song.displayArtist,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "•",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = song.durationFormatted,
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GlassIconButton(
                    size = 32.dp,
                    accentColor = GoldSecondary,
                    backgroundColor = if (song.isFavorite) GoldPrimary.copy(alpha = 0.2f) else Color(0x08FFFFFF),
                    onClick = onToggleFav
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) GoldPrimary else TextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (onMore != null) {
                    GlassIconButton(
                        size = 32.dp,
                        accentColor = GoldPrimary,
                        backgroundColor = Color(0x08FFFFFF),
                        onClick = onMore
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextTertiary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
