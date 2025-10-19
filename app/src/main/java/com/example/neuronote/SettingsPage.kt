package com.example.neuronote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ColorTheme(val lightColor: Color, val darkColor: Color, val name: String)

@Composable
fun SettingsPage(textColor: Color, lightColor: Color) {
    val context = LocalContext.current

    // 1. Get the currently active theme colors
    val currentLightColor by AppThemeManager.lightColor
    val currentDarkColor by AppThemeManager.darkColor

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Choose Color Theme",
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val themes = listOf(
            ColorTheme(Color(0xFFC8E6C9), Color(0xFF388E3C), "Forest Green"),
            ColorTheme(Color(0xFFFFCDD2), Color(0xFFD32F2F), "Soft Red"),
            ColorTheme(Color(0xFFBBDEFB), Color(0xFF1976D2), "Ocean Blue"),
            ColorTheme(Color(0xFFFFF9C4), Color(0xFFFBC02D), "Golden Yellow"),
            ColorTheme(Color(0xFFE1BEE7), Color(0xFF6A1B9A), "Amethyst Purple"),
            ColorTheme(Color(0xFFE0E0E0), Color(0xFF424242), "Charcoal Grey")
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            themes.forEach { theme ->
                // 2. Determine if the current theme card matches the active theme
                val isSelected = theme.darkColor == currentDarkColor && theme.lightColor == currentLightColor

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            // 3. Apply a thick border using the theme's accent color if selected
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) theme.darkColor else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { AppThemeManager.updateTheme(context, theme.lightColor, theme.darkColor) },
                    colors = CardDefaults.cardColors(containerColor = theme.lightColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(theme.darkColor)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = theme.name,
                            color = theme.darkColor // Use the theme's own dark color for contrast on its light card
                        )
                    }
                }
            }
        }
    }
}
