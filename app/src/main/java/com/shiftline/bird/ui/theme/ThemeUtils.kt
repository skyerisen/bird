package com.shiftline.bird.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Provides theme-related colors based on launcher settings
 */
data class LauncherColors(
    val promptColor: Color,
    val outputColor: Color,
    val overlayColor: Color,
    val navBarColor: Color
)

/**
 * Creates launcher colors from the Material 3 color scheme and overlay alpha
 */
@Composable
fun getLauncherColors(
    colorScheme: ColorScheme,
    overlayAlpha: Float
): LauncherColors {
    return LauncherColors(
        promptColor = colorScheme.primary,
        outputColor = colorScheme.secondary,
        overlayColor = colorScheme.surface.copy(alpha = overlayAlpha),
        navBarColor = colorScheme.surfaceVariant.copy(alpha = 0.8f)
    )
}
