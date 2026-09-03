package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.glassPanel
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.BottomTab

@Composable
fun GlassBottomNavigation(
    selectedTab: BottomTab,
    accentColor: Color = GoldPrimary,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassPanel(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0x22141208),
                borderColors = listOf(
                    GoldPrimary.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.05f)
                ),
                borderWidth = 1.dp,
                elevation = 12.dp
            )
            .height(58.dp)
            .padding(horizontal = 6.dp)
            .testTag("glass_bottom_navigation"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassNavItem(
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = selectedTab == BottomTab.HOME,
                accentColor = accentColor,
                onClick = { onTabSelected(BottomTab.HOME) },
                testTag = "nav_tab_home"
            )

            GlassNavItem(
                label = "Library",
                selectedIcon = Icons.Filled.LibraryMusic,
                unselectedIcon = Icons.Outlined.LibraryMusic,
                isSelected = selectedTab == BottomTab.LIBRARY,
                accentColor = accentColor,
                onClick = { onTabSelected(BottomTab.LIBRARY) },
                testTag = "nav_tab_library"
            )

            GlassNavItem(
                label = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                isSelected = selectedTab == BottomTab.SEARCH,
                accentColor = accentColor,
                onClick = { onTabSelected(BottomTab.SEARCH) },
                testTag = "nav_tab_search"
            )

            GlassNavItem(
                label = "Equalizer",
                selectedIcon = Icons.Filled.Equalizer,
                unselectedIcon = Icons.Outlined.Equalizer,
                isSelected = selectedTab == BottomTab.EQUALIZER,
                accentColor = accentColor,
                onClick = { onTabSelected(BottomTab.EQUALIZER) },
                testTag = "nav_tab_equalizer"
            )

            GlassNavItem(
                label = "About",
                selectedIcon = Icons.Filled.Info,
                unselectedIcon = Icons.Outlined.Info,
                isSelected = selectedTab == BottomTab.ABOUT,
                accentColor = accentColor,
                onClick = { onTabSelected(BottomTab.ABOUT) },
                testTag = "nav_tab_about"
            )
        }
    }
}

@Composable
private fun GlassNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "navScale"
    )

    val iconColor = if (isSelected) accentColor else Color.White.copy(alpha = 0.45f)
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .scale(animatedScale)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor, bounded = false, radius = 24.dp),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            } else {
                Box(modifier = Modifier.size(4.dp))
            }
        }
    }
}
