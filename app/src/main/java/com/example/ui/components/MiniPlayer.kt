package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun GlassMiniPlayer(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val song = uiState.currentSong ?: return
    val progress = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .glassPanel(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x281A1608),
                    borderColors = listOf(
                        GoldPrimary.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f)
                    ),
                    borderWidth = 1.dp,
                    elevation = 8.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = GoldPrimary),
                    onClick = { viewModel.setNowPlayingExpanded(true) }
                )
                .testTag("mini_player")
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Album Art + Title / Artist
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        LocalArtworkImage(
                            albumArtUri = song.albumArtUriString,
                            title = song.displayTitle,
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            fallbackTint = GoldPrimary
                        )

                        Column {
                            Text(
                                text = song.displayTitle,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = song.displayArtist,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (uiState.isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary)
                                    )
                                }
                            }
                        }
                    }

                    // Right: Actions (Favorite, Play/Pause, Next)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlassIconButton(
                            size = 36.dp,
                            accentColor = GoldPrimary,
                            backgroundColor = if (song.isFavorite) GoldPrimary.copy(alpha = 0.2f) else Color(0x0CFFFFFF),
                            onClick = { viewModel.toggleFavorite(song) }
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (song.isFavorite) GoldPrimary else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Play/Pause
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .glassCircleButton(
                                    sizeDp = 38.dp,
                                    accentColor = GoldPrimary,
                                    isPrimary = true,
                                    glowAlpha = 0.3f,
                                    onClick = { viewModel.togglePlayPause() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Next
                        GlassIconButton(
                            size = 36.dp,
                            accentColor = GoldPrimary,
                            onClick = { viewModel.next() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom progress line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0x14FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GoldPrimary, GoldChampagne)
                                )
                            )
                    )
                }
            }
        }
    }
}
