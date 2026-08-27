package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicObsidianColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = ObsidianWhite,
    primaryContainer = NeonRedDark,
    secondary = ObsidianGray,
    onSecondary = ObsidianWhite,
    background = ObsidianBlack,
    onBackground = ObsidianWhite,
    surface = ObsidianDark,
    onSurface = ObsidianWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = ObsidianGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force cinematic dark style by default
    dynamicColor: Boolean = false, // Preserve brand identity instead of blending with device walls
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CosmicObsidianColorScheme,
        typography = Typography,
        content = content
    )
}
