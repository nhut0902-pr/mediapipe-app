package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FA9A),      // Neon Green
    secondary = Color(0xFFFFD700),    // Gold
    tertiary = Color(0xFF00BFFF),     // Deep Sky Blue
    background = Color(0xFF030610),   // Cyber Space Deep Dark
    surface = Color(0xFF0E1424),      // Midnight Blue Card
    onPrimary = Color(0xFF01040D),
    onSecondary = Color(0xFF01040D),
    onBackground = Color(0xFFF0F4FF),
    onSurface = Color(0xFFE2E8F0)
)

private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF00A36C),      // Jade Green
    secondary = Color(0xFFD4AF37),    // Dark Gold
    tertiary = Color(0xFF007FFF),     // Azure Blue
    background = Color(0xFFF4F6FC),   // Light Silver Slate
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0D111C),
    onSurface = Color(0xFF161B26)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
