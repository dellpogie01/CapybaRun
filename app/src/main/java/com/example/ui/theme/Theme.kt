package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CapyPrimary,
    onPrimary = Color.White,
    secondary = CapyGold,
    onSecondary = CapyTextDark,
    background = CapyBackground,
    onBackground = CapyTextDark,
    surface = CapySurface,
    onSurface = CapyTextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = CapyPrimary,
    onPrimary = Color.White,
    secondary = CapyGold,
    onSecondary = CapyTextDark,
    background = Color(0xFF1E1611),
    onBackground = CapyBackground,
    surface = Color(0xFF2A201A),
    onSurface = CapyBackground
)

@Composable
fun CapybarunTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CapybarunTheme(darkTheme = darkTheme, content = content)
}
