package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Phenomenon Studio / Phenomenon Product Theme
// Ultra-sleek, minimalist dark canvas with high-energy Electric Lime & Cyber Violet accents
private val PhenomenonDarkColorScheme = darkColorScheme(
    primary = PhenomenonElectricLime,
    onPrimary = PhenomenonCanvas,
    primaryContainer = PhenomenonSurfaceElevated,
    onPrimaryContainer = PhenomenonElectricLime,
    secondary = PhenomenonPurpleNeon,
    onSecondary = Color.White,
    secondaryContainer = PhenomenonSurfaceElevated,
    onSecondaryContainer = PhenomenonPurpleNeon,
    tertiary = PhenomenonCyanElectric,
    onTertiary = PhenomenonCanvas,
    background = PhenomenonCanvas,
    surface = PhenomenonSurface,
    surfaceVariant = PhenomenonSurfaceElevated,
    onBackground = PhenomenonTextPrimary,
    onSurface = PhenomenonTextPrimary,
    outline = PhenomenonBorder,
    outlineVariant = PhenomenonBorderActive,
    error = PhenomenonCrimson
)

private val PhenomenonLightColorScheme = lightColorScheme(
    primary = PhenomenonCanvas,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = PhenomenonCanvas,
    secondary = PhenomenonPurpleNeon,
    onSecondary = Color.White,
    tertiary = PhenomenonEmerald,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = PhenomenonCanvas,
    onSurface = PhenomenonCanvas,
    outline = Color(0xFFE2E8F0),
    error = PhenomenonCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Phenomenon signature dark aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PhenomenonDarkColorScheme else PhenomenonLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
