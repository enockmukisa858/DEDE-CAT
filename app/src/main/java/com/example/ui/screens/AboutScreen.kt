package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassHeader
import com.example.ui.glass.glassCard
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
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun AboutScreen(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
        contentPadding = PaddingValues(bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassHeader(
                title = "BD MUSIC PLAYER",
                subtitle = "CREATOR SPOTLIGHT & APP HERITAGE",
                accentColor = GoldPrimary
            )
        }

        // 1. Creator Hero Card: Enock Mukisa
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glassPanel(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0x281A1608),
                        borderColors = listOf(
                            GoldPrimary.copy(alpha = 0.50f),
                            GoldMuted.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        ),
                        borderWidth = 1.2.dp,
                        elevation = 12.dp
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Creator Portrait with Gold Glow Ring
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                            .border(2.dp, GoldPrimary, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.creator_enock_1788197453039),
                            contentDescription = "Enock Mukisa - Creator",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Verified Creator Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GoldPrimary.copy(alpha = 0.18f))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Creator",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "APP CREATOR & LEAD ARCHITECT",
                            color = GoldChampagne,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Creator Name & Kazinga Heritage Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Enock Mukisa",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Origin",
                                tint = GoldSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Born & Raised in KAZINGA",
                                color = GoldChampagne,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Biography & Vision Statement
                    Text(
                        text = "Enock Mukisa, creator of the app BLACK DUDE MUSIC (BD MUSIC PLAYER), is a young creative IT expert born and raised in KAZINGA. Driven by a deep passion for digital media, high-fidelity audio engineering, and modern Android design, Enock crafted this application to deliver an authentic, high-performance music experience built exclusively for local music enthusiasts.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Connect with Enock Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:enockmukisa858@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "BD Music Player Feedback")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect with Enock Mukisa",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. Core Pillars of BD MUSIC PLAYER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CORE PILLARS & FEATURES",
                    color = GoldChampagne,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                FeatureCard(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = GoldPrimary,
                    title = "100% Local Device Music Only",
                    description = "Absolute privacy. Scans your device storage directly for MP3, FLAC, WAV, AAC, M4A, OGG & OPUS files. No cloud uploads, no ads, and zero sample tracks."
                )

                FeatureCard(
                    icon = Icons.Default.GraphicEq,
                    iconTint = GoldSecondary,
                    title = "DSP Audio & Soundstage Equalizer",
                    description = "Hardware-accelerated sound enhancement featuring live spectrum visualization, dynamic bass boost, 3D spatial virtualizer, and custom acoustic presets."
                )

                FeatureCard(
                    icon = Icons.Default.MusicNote,
                    iconTint = GoldChampagne,
                    title = "Artwork & Metadata Extraction",
                    description = "Automatic local MediaStore thumbnail scanning, ID3 album art extraction, and dynamic blurred canvas backgrounds for every song."
                )

                FeatureCard(
                    icon = Icons.Default.Security,
                    iconTint = GoldPrimary,
                    title = "Continuous Audiophile Playback",
                    description = "Background playback service with lock-screen notifications, repeat & shuffle modes, audio ducking, and auto-queue advance."
                )
            }
        }

        // 3. Technical Specifications
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glassPanel(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x1E121008)
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TECHNICAL SPECIFICATIONS",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    SpecRow(label = "Application", value = "BD MUSIC PLAYER (Black Dude Music)")
                    SpecRow(label = "Creator", value = "Enock Mukisa (IT Specialist)")
                    SpecRow(label = "Origin", value = "Kazinga, Uganda")
                    SpecRow(label = "Theme", value = "Obsidian Black & Imperial Gold")
                    SpecRow(label = "Supported Formats", value = "MP3, FLAC, WAV, AAC, M4A, OGG, OPUS")
                    SpecRow(label = "Architecture", value = "Kotlin • Jetpack Compose • Room • Coroutines")
                    SpecRow(label = "Version", value = "2.0 (Gold Master Edition)")
                }
            }
        }

        // 4. Kazinga Proud Footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Crafted with passion in Kazinga by Enock Mukisa • Black Dude Music © 2026",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(
                shape = RoundedCornerShape(16.dp),
                accentColor = iconTint
            )
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
