package org.opennur.quran.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF155B4A)
private val LightBackground = Color(0xFFF8F5EF)
private val LightSurface = Color(0xFFFFFBF5)
private val Gold = Color(0xFFB78C3D)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    background = LightBackground,
    surface = LightSurface,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF83D0B2),
    onPrimary = Color(0xFF00382A),
    secondary = Color(0xFFE6C37A),
    background = Color(0xFF101815),
    surface = Color(0xFF17221D),
)

@Composable
fun QuranTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkMode) DarkColors else LightColors,
        content = content,
    )
}
