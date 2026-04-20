package com.example.calorietracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = SurfaceWhite,
    secondary = RoseSecondary,
    onSecondary = TextPrimary,
    tertiary = WarmPeach,
    background = SurfaceSoft,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = BorderSoft
)

@Composable
fun CalorieTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
