package com.example.neuronote

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

object AppThemeManager {
    private val _lightColor = mutableStateOf(Color(0xFFC8E6C9)) // default light
    private val _darkColor = mutableStateOf(Color(0xFF388E3C))  // default dark

    val lightColor: State<Color> get() = _lightColor
    val darkColor: State<Color> get() = _darkColor

    fun updateTheme(light: Color, dark: Color) {
        _lightColor.value = light
        _darkColor.value = dark
    }
}
