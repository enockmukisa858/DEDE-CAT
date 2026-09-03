package com.example.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan

/**
 * Minimized, elegant glass panel modifier.
 * Clean, subtle translucency with refined border accent.
 */
fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x18121218),
    borderColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.16f),
        Color.White.copy(alpha = 0.06f)
    ),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp
): Modifier = this
    .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false) else Modifier)
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.85f),
                backgroundColor.copy(alpha = 0.65f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = borderColors,
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
    .drawWithContent {
        drawContent()
        // Subtle clean top highlight
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height * 0.20f
            )
        )
    }

/**
 * Minimized translucent glass card modifier for song rows, album cards, and grids.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    accentColor: Color = NeonCyan,
    alphaMultiplier: Float = 1.0f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val baseModifier = this
        .clip(shape)
        .background(
            color = Color(0xFF121218).copy(alpha = 0.70f * alphaMultiplier),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f * alphaMultiplier),
                    accentColor.copy(alpha = 0.10f * alphaMultiplier),
                    Color.White.copy(alpha = 0.04f * alphaMultiplier)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )

    if (onClick != null) {
        baseModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = accentColor),
            onClick = onClick
        )
    } else {
        baseModifier
    }
}

/**
 * Minimized glass circle button modifier for playback controls.
 */
fun Modifier.glassCircleButton(
    sizeDp: Dp = 52.dp,
    accentColor: Color = NeonCyan,
    isPrimary: Boolean = false,
    glowAlpha: Float = 0.2f,
    onClick: () -> Unit
): Modifier = composed {
    val shape = CircleShape
    val bgColors = if (isPrimary) {
        listOf(
            accentColor.copy(alpha = 0.85f),
            accentColor.copy(alpha = 0.65f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.04f)
        )
    }

    this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = bgColors,
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = if (isPrimary) {
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        accentColor.copy(alpha = 0.20f)
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                }
            ),
            shape = shape
        )
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White, bounded = true),
            onClick = onClick
        )
}

/**
 * Subtle border highlight for currently active/playing item.
 */
fun Modifier.animatedGlassGlow(
    accentColor: Color,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glassGlow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnim"
    )

    this.border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = alphaAnim),
                Color.White.copy(alpha = 0.20f),
                accentColor.copy(alpha = alphaAnim * 0.4f)
            )
        ),
        shape = shape
    )
}
