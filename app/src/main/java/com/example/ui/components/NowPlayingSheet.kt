package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LocalSong
import com.example.service.ServiceRepeatMode
import com.example.ui.glass.glassCircleButton
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun NowPlayingScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val song = uiState.currentSong ?: return

    var showInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var isQueueViewActive by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        SongInfoDialog(
            song = song,
            onDismiss = { showInfoDialog = false }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            song = song,
            playlists = uiState.playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onAddToPlaylist = { plId ->
                viewModel.addSongToPlaylist(plId, song.id)
            },
            onCreateAndAdd = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Dynamic blurred album art background
        if (!song.albumArtUriString.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(song.albumArtUriString),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(70.dp)
                    .alpha(0.25f)
            )
        }

        // Overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xD008080C),
                                Color(0xA008080C),
                                Color(0xF508080C)
                            )
                        )
                    )
                }
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    size = 40.dp,
                    accentColor = GoldPrimary,
                    onClick = { viewModel.setNowPlayingExpanded(false) }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "BD MUSIC PLAYER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                GlassIconButton(
                    size = 40.dp,
                    accentColor = GoldPrimary,
                    onClick = { showInfoDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Song Info",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Artwork Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141208))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!song.albumArtUriString.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(song.albumArtUriString),
                        contentDescription = "${song.displayTitle} Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .testTag("now_playing_artwork")
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GoldPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = song.fileExtension,
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Artist & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.displayTitle,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.testTag("track_title_text")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.displayArtist} • ${song.displayAlbum}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        size = 38.dp,
                        accentColor = GoldPrimary,
                        onClick = { showAddToPlaylistDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to playlist",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    GlassIconButton(
                        size = 38.dp,
                        accentColor = GoldPrimary,
                        backgroundColor = if (song.isFavorite) GoldPrimary.copy(alpha = 0.2f) else Color(0x0CFFFFFF),
                        onClick = { viewModel.toggleFavorite(song) }
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite track",
                            tint = if (song.isFavorite) GoldPrimary else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider
            GlassProgressSlider(
                positionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                accentColor = GoldPrimary,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                GlassIconButton(
                    size = 44.dp,
                    accentColor = GoldPrimary,
                    backgroundColor = if (uiState.isShuffle) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                    borderColor = if (uiState.isShuffle) GoldPrimary.copy(alpha = 0.6f) else Color.Transparent,
                    onClick = { viewModel.toggleShuffle() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (uiState.isShuffle) GoldPrimary else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous
                GlassIconButton(
                    size = 46.dp,
                    accentColor = GoldPrimary,
                    onClick = { viewModel.previous() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .glassCircleButton(
                            sizeDp = 68.dp,
                            accentColor = GoldPrimary,
                            isPrimary = true,
                            glowAlpha = 0.3f,
                            onClick = { viewModel.togglePlayPause() }
                        )
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next
                GlassIconButton(
                    size = 46.dp,
                    accentColor = GoldPrimary,
                    onClick = { viewModel.next() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Repeat Mode Button
                val repeatIcon = when (uiState.repeatMode) {
                    ServiceRepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val isRepeatActive = uiState.repeatMode != ServiceRepeatMode.OFF

                GlassIconButton(
                    size = 44.dp,
                    accentColor = GoldPrimary,
                    backgroundColor = if (isRepeatActive) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                    borderColor = if (isRepeatActive) GoldPrimary.copy(alpha = 0.6f) else Color.Transparent,
                    onClick = { viewModel.toggleRepeat() }
                ) {
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) GoldPrimary else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Format Info & Queue Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio format & size tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = song.fileExtension,
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = song.sizeFormatted,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                // Queue Sheet Toggle
                GlassIconButton(
                    size = 38.dp,
                    accentColor = GoldPrimary,
                    backgroundColor = if (isQueueViewActive) GoldPrimary.copy(alpha = 0.25f) else Color(0x0CFFFFFF),
                    onClick = { isQueueViewActive = !isQueueViewActive }
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue (${uiState.queue.size})",
                        tint = if (isQueueViewActive) GoldPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Queue List if active
            AnimatedVisibility(
                visible = isQueueViewActive,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                QueueLocalSongList(
                    queue = uiState.queue,
                    currentIndex = uiState.queueIndex,
                    onSongClick = { qSong, index ->
                        viewModel.playSong(qSong, uiState.queue, index)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun QueueLocalSongList(
    queue: List<LocalSong>,
    currentIndex: Int,
    onSongClick: (LocalSong, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassPanel(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0x22141208),
                borderColors = listOf(
                    GoldPrimary.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "UP NEXT IN QUEUE (${queue.size})",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            queue.take(15).forEachIndexed { index, qSong ->
                val isPlaying = index == currentIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPlaying) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onSongClick(qSong, index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        LocalArtworkImage(
                            albumArtUri = qSong.albumArtUriString,
                            title = qSong.displayTitle,
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        )
                        Column {
                            Text(
                                text = qSong.displayTitle,
                                color = if (isPlaying) GoldPrimary else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = qSong.displayArtist,
                                color = TextTertiary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = qSong.durationFormatted,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
