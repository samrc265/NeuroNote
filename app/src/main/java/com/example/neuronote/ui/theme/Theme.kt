package com.example.neuronote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ModernLight = lightColorScheme(
    primary = SoftBlueLight,
    secondary = SoftGreenLight,
    surface = WarmGreyLight,
    background = WarmGreyLight,
    onPrimary = DeepNavyLight,
    onSecondary = DeepNavyLight,
    onBackground = DeepNavyLight,
    onSurface = DeepNavyLight,
)

private val ModernDark = darkColorScheme(
    primary = SoftBlueDark,
    secondary = SoftGreenDark,
    surface = WarmGreyDark,
    background = DeepNavyDark,
    onPrimary = WarmGreyDark,
    onSecondary = WarmGreyDark,
    onBackground = WarmGreyDark,
    onSurface = WarmGreyDark,
)

@Composable
fun NeuroNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ModernDark else ModernLight
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
