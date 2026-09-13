package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = Color.Black,
    primaryContainer = ArcCyanDark,
    onPrimaryContainer = Color.White,
    secondary = JarvisGold,
    onSecondary = Color.Black,
    secondaryContainer = JarvisGoldDark,
    onSecondaryContainer = Color.White,
    tertiary = StatusSuccess,
    background = HudBackground,
    onBackground = TextPrimary,
    surface = HudSurface,
    onSurface = TextPrimary,
    surfaceVariant = HudSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = HudBorder,
    error = StatusError
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
