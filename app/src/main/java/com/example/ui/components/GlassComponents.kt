package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LocalPlaylist
import com.example.model.LocalSong
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Minimized Clean Dynamic Background.
 */
@Composable
fun DynamicGlassBackground(
    currentSong: LocalSong?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        if (currentSong?.albumArtUriString != null) {
            AsyncImage(
                model = Uri.parse(currentSong.albumArtUriString),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .alpha(0.20f)
            )
        }

        // Deep gradient tint with subtle gold sheen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x330A0A0E),
                                Color(0xB308080C),
                                Color(0xFF060608)
                            )
                        )
                    )
                }
        )

        content()
    }
}

/**
 * Album Art Thumbnail with fallback artwork icon
 */
@Composable
fun LocalArtworkImage(
    albumArtUri: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    fallbackIcon: ImageVector = Icons.Default.MusicNote,
    fallbackTint: Color = GoldPrimary
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF221E14),
                        Color(0xFF141208)
                    )
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.20f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!albumArtUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(albumArtUri),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = fallbackTint.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Header with "BD MUSIC PLAYER" branding and scanning indicator
 */
@Composable
fun GlassHeader(
    title: String = "BD MUSIC PLAYER",
    subtitle: String? = null,
    accentColor: Color = GoldPrimary,
    isScanning: Boolean = false,
    onRescanClick: (() -> Unit)? = null,
    onVisualizerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onRescanClick != null) {
                GlassIconButton(
                    accentColor = accentColor,
                    size = 38.dp,
                    onClick = onRescanClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan Music",
                        tint = if (isScanning) accentColor else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onVisualizerClick != null) {
                GlassIconButton(
                    accentColor = accentColor,
                    size = 38.dp,
                    onClick = onVisualizerClick
                ) {
                    MiniWaveformIcon(accentColor = accentColor)
                }
            }
        }
    }
}

@Composable
fun MiniWaveformIcon(accentColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(14.dp)
    ) {
        val heights = listOf(0.4f, 0.85f, 0.6f, 1.0f, 0.5f)
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxHeight(h)
                    .clip(RoundedCornerShape(1.dp))
                    .background(accentColor)
            )
        }
    }
}

/**
 * Minimized glass icon button
 */
@Composable
fun GlassIconButton(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = CircleShape,
    accentColor: Color = GoldPrimary,
    backgroundColor: Color = Color(0x10FFFFFF),
    borderColor: Color = Color(0x18FFFFFF),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White, bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Minimized clean seek progress slider
 */
@Composable
fun GlassProgressSlider(
    positionMs: Long,
    durationMs: Long,
    accentColor: Color = GoldPrimary,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFormatted = remember(positionMs) {
        val sec = (positionMs / 1000).coerceAtLeast(0)
        String.format("%d:%02d", sec / 60, sec % 60)
    }

    val totalFormatted = remember(durationMs) {
        val sec = (durationMs / 1000).coerceAtLeast(0)
        String.format("%d:%02d", sec / 60, sec % 60)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = positionMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = GoldChampagne,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0x22FFFFFF),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .testTag("progress_slider")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentFormatted,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = totalFormatted,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Minimized Chip Pill for filter tabs
 */
@Composable
fun GlassPill(
    text: String,
    isSelected: Boolean,
    accentColor: Color = GoldPrimary,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.20f) else Color(0x10FFFFFF)
    val borderColor = if (isSelected) accentColor.copy(alpha = 0.70f) else Color(0x18FFFFFF)

    Row(
        modifier = modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Song Metadata Details Dialog
 */
@Composable
fun SongInfoDialog(
    song: LocalSong,
    onDismiss: () -> Unit
) {
    val dateFormatted = remember(song.dateModifiedSeconds) {
        if (song.dateModifiedSeconds > 0) {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(song.dateModifiedSeconds * 1000))
        } else "Unknown"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                Text("Song Details", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "Title", value = song.displayTitle)
                InfoRow(label = "Artist", value = song.displayArtist)
                InfoRow(label = "Album", value = song.displayAlbum)
                InfoRow(label = "Duration", value = song.durationFormatted)
                InfoRow(label = "Format", value = "${song.fileExtension} (${song.mimeType})")
                InfoRow(label = "File Size", value = song.sizeFormatted)
                InfoRow(label = "Folder", value = song.folderName)
                InfoRow(label = "Location", value = song.path)
                InfoRow(label = "Modified", value = dateFormatted)
            }
        },
        containerColor = Color(0xFF141208),
        modifier = Modifier.border(1.dp, GoldPrimary.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Add Song to Playlist Dialog
 */
@Composable
fun AddToPlaylistDialog(
    song: LocalSong,
    playlists: List<LocalPlaylist>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (playlistId: Long) -> Unit,
    onCreateAndAdd: (name: String) -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (isCreatingNew) {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreateAndAdd(newPlaylistName.trim())
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save & Add", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        },
        dismissButton = {
            if (isCreatingNew) {
                TextButton(onClick = { isCreatingNew = false }) {
                    Text("Back", color = TextSecondary)
                }
            }
        },
        title = {
            Text(
                text = if (isCreatingNew) "New Playlist" else "Add to Playlist",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (isCreatingNew) {
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .clickable { isCreatingNew = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary)
                            Text("+ Create New Playlist", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    if (playlists.isEmpty()) {
                        item {
                            Text(
                                text = "No existing playlists. Tap above to create one!",
                                color = TextTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(playlists) { pl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0CFFFFFF))
                                    .clickable {
                                        onAddToPlaylist(pl.id)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = pl.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(text = "${pl.songCount} songs", color = TextTertiary, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF141208),
        modifier = Modifier.border(1.dp, GoldPrimary.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
    )
}

/**
 * Animated Spectrum Audio Visualizer Bars
 */
@Composable
fun GlassAudioVisualizer(
    isPlaying: Boolean,
    accentColor: Color = GoldPrimary,
    modifier: Modifier = Modifier
) {
    val barCount = 28
    val transition = rememberInfiniteTransition(label = "visualizerAnim")

    val animProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animProgress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0CFFFFFF))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val phase = (i.toFloat() / barCount) * 3.14159f * 2
            val wave = kotlin.math.sin(phase + animProgress * 6.28f).toFloat()
            val normalizedWave = ((wave + 1f) / 2f).coerceIn(0.12f, 1f)
            val barHeightFraction = if (isPlaying) {
                val factor = (0.25f + 0.75f * ((i % 5 + 1) / 5f)) * normalizedWave
                factor.coerceIn(0.1f, 1f)
            } else {
                0.08f
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(barHeightFraction)
                    .padding(horizontal = 1.5.dp)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }
}
