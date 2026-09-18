package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skeuomorphic Light Color Scheme
private val SkeuomorphicLightColorScheme = lightColorScheme(
    primary = SkeuoCobalt,
    onPrimary = Color.White,
    primaryContainer = SkeuoWellInset,
    onPrimaryContainer = SkeuoTextPrimary,
    secondary = SkeuoPurple,
    onSecondary = Color.White,
    secondaryContainer = SkeuoSurfaceElevated,
    onSecondaryContainer = SkeuoTextPrimary,
    tertiary = SkeuoEmerald,
    onTertiary = Color.White,
    background = SkeuoCanvas,
    surface = SkeuoSurface,
    surfaceVariant = SkeuoWellInset,
    onBackground = SkeuoTextPrimary,
    onSurface = SkeuoTextPrimary,
    outline = SkeuoBorderLight,
    outlineVariant = SkeuoChromeBezel,
    error = SkeuoCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light Skeuomorphic aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SkeuomorphicLightColorScheme,
        typography = Typography,
        content = content
    )
}
