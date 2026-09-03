package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackDudeMusicColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldMuted.copy(alpha = 0.25f),
    onPrimaryContainer = GoldChampagne,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DeepCharcoal,
    onSecondaryContainer = GoldChampagne,
    tertiary = GoldChampagne,
    onTertiary = Color.Black,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = CharcoalGlass,
    onSurface = TextPrimary,
    surfaceVariant = DeepCharcoal,
    onSurfaceVariant = TextSecondary,
    outline = GlassWhiteBorderSubtle,
    outlineVariant = GlassWhiteBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // BD Music Player features a signature luxury Obsidian & Gold theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackDudeMusicColorScheme,
        typography = Typography,
        content = content
    )
}

