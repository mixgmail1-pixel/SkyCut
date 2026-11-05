package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color(0xFF090E17),
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = Color.White,
    secondary = SkyBlueLight,
    onSecondary = Color(0xFF090E17),
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder,
    error = AccentRedCut
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlueDark,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = SkyBluePrimary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightBorder,
    error = AccentRedCut
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to CapCut-style sleek dark studio theme, with light mode supported
    dynamicColor: Boolean = false, // Keep signature SkyCut Sky Blue branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
