package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassAudioVisualizer
import com.example.ui.components.GlassHeader
import com.example.ui.components.GlassPill
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
fun EqualizerScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val presets = listOf("Normal", "Bass Boost", "Vocal Boost", "Electronic", "Acoustic", "Rock")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassHeader(
                title = "AUDIO EQUALIZER",
                subtitle = "DSP SOUND ENHANCEMENT",
                accentColor = GoldPrimary
            )
        }

        // Live Spectrum Analyzer Display
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glassPanel(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x281A1608),
                        borderColors = listOf(
                            GoldPrimary.copy(alpha = 0.40f),
                            GoldMuted.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.05f)
                        ),
                        borderWidth = 1.dp
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "REAL-TIME SPECTRUM",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Text(
                            text = if (uiState.isPlaying) "PLAYING" else "PAUSED",
                            color = if (uiState.isPlaying) GoldChampagne else TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    GlassAudioVisualizer(
                        isPlaying = uiState.isPlaying,
                        accentColor = GoldPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sound Presets Pills
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACOUSTIC PRESETS",
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
                    items(presets) { preset ->
                        val isSelected = uiState.equalizerPreset == preset
                        GlassPill(
                            text = preset,
                            isSelected = isSelected,
                            accentColor = GoldPrimary,
                            onClick = { viewModel.setEqualizerPreset(preset) }
                        )
                    }
                }
            }
        }

        // Bass Boost, Virtualizer & Treble Controls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glassPanel(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x22141208),
                        borderColors = listOf(
                            GoldPrimary.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bass Boost
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BASS BOOST",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.bassBoost.toInt()}%",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = uiState.bassBoost,
                            onValueChange = { viewModel.setBassBoost(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldChampagne,
                                activeTrackColor = GoldPrimary,
                                inactiveTrackColor = Color(0x20FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )
                    }

                    // Virtualizer 3D Soundstage
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SurroundSound,
                                    contentDescription = null,
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "3D VIRTUALIZER",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${uiState.virtualizer.toInt()}%",
                                color = GoldSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = uiState.virtualizer,
                            onValueChange = { viewModel.setVirtualizer(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldChampagne,
                                activeTrackColor = GoldSecondary,
                                inactiveTrackColor = Color(0x20FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )
                    }

                    // Treble Clarity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TREBLE CLARITY",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.treble.toInt()}%",
                                color = GoldChampagne,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = uiState.treble,
                            onValueChange = { viewModel.setTreble(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldChampagne,
                                activeTrackColor = GoldChampagne,
                                inactiveTrackColor = Color(0x20FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )
                    }
                }
            }
        }
    }
}
