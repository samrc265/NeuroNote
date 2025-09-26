package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object AppThemeManager {
    private const val PREFS_NAME = "AppThemePrefs"
    private const val KEY_LIGHT_COLOR = "light_color"
    private const val KEY_DARK_COLOR = "dark_color"
    private const val KEY_IS_DARK_THEME = "is_dark_theme"

    private val _lightColor = mutableStateOf(Color(0xFFC8E6C9)) // default light
    private val _darkColor = mutableStateOf(Color(0xFF388E3C))  // default dark
    private val _isDarkTheme = mutableStateOf(false)

    val lightColor: State<Color> get() = _lightColor
    val darkColor: State<Color> get() = _darkColor
    val isDarkTheme: State<Boolean> get() = _isDarkTheme

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLightColor = prefs.getInt(KEY_LIGHT_COLOR, Color(0xFFC8E6C9).toArgb())
        val savedDarkColor = prefs.getInt(KEY_DARK_COLOR, Color(0xFF388E3C).toArgb())
        _lightColor.value = Color(savedLightColor)
        _darkColor.value = Color(savedDarkColor)
        _isDarkTheme.value = prefs.getBoolean(KEY_IS_DARK_THEME, false)
    }

    fun updateTheme(context: Context, light: Color, dark: Color) {
        _lightColor.value = light
        _darkColor.value = dark
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_LIGHT_COLOR, light.toArgb())
            .putInt(KEY_DARK_COLOR, dark.toArgb())
            .apply()
    }

    fun toggleTheme(context: Context) {
        _isDarkTheme.value = !_isDarkTheme.value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_DARK_THEME, _isDarkTheme.value)
            .apply()
    }
}