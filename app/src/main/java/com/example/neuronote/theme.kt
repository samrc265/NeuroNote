package com.example.neuronote

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF388E3C),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.Black
)

@Composable
fun NeuroNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = androidx.compose.material3.Typography(), content = content)
}
