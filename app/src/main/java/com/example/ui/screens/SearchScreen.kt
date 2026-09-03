package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassHeader
import com.example.ui.components.GlassPill
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun SearchScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val localExtensions = listOf("MP3", "FLAC", "WAV", "AAC", "M4A", "OGG")

    val searchResults = if (uiState.searchQuery.isBlank()) {
        uiState.allSongs
    } else {
        uiState.allSongs.filter {
            it.displayTitle.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.displayArtist.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.displayAlbum.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.folderName.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.fileExtension.contains(uiState.searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassHeader(
                title = "SEARCH LIBRARY",
                subtitle = "LOCAL STORAGE SEARCH",
                accentColor = GoldPrimary
            )
        }

        // Search Input
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glassPanel(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0x22141208),
                        borderColors = listOf(
                            GoldPrimary.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search by song, artist, album, folder, format...",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.setSearchQuery("") }
                            )
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field")
                )
            }
        }

        // Format filter chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FILTER BY FORMAT",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(localExtensions) { ext ->
                        GlassPill(
                            text = ext,
                            isSelected = uiState.searchQuery.equals(ext, ignoreCase = true),
                            accentColor = GoldPrimary,
                            onClick = {
                                viewModel.setSearchQuery(if (uiState.searchQuery.equals(ext, ignoreCase = true)) "" else ext)
                            }
                        )
                    }
                }
            }
        }

        // Results Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.searchQuery.isBlank()) "ALL LOCAL SONGS" else "MATCHING SONGS",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "${searchResults.size} FOUND",
                    color = GoldChampagne,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search Results List
        if (searchResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs found matching \"${uiState.searchQuery}\"",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(searchResults) { song ->
                val isCurrent = uiState.currentSong?.id == song.id
                SongLocalRow(
                    song = song,
                    isCurrent = isCurrent,
                    isPlaying = uiState.isPlaying && isCurrent,
                    onPlay = {
                        val idx = searchResults.indexOf(song)
                        viewModel.playSong(song, searchResults, idx)
                    },
                    onToggleFav = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                )
            }
        }
    }
}
