
package com.example.scrollstop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SoftBlack,
    onPrimary = Paper,
    secondary = MutedGray,
    onSecondary = Paper,
    tertiary = WarmBeige,
    background = Cream,
    onBackground = SoftBlack,
    surface = Paper,
    onSurface = SoftBlack,
    surfaceVariant = HeroCream,
    onSurfaceVariant = MutedGray,
    outline = WarmBeige
)

private val DarkColorScheme = darkColorScheme(
    primary = HeroCream,
    onPrimary = SoftBlack,
    secondary = WarmBeige,
    onSecondary = SoftBlack,
    tertiary = MutedGray,
    background = SoftBlack,
    onBackground = Cream,
    surface = Color(0xFF24231F),
    onSurface = Cream,
    surfaceVariant = SoftBlack,
    onSurfaceVariant = WarmBeige,
    outline = MutedGray
)

@Composable
fun ScrollStopTheme(
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
