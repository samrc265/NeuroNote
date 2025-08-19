package com.example.neuronote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Define a new data class to hold the theme information
data class ColorTheme(val lightColor: Color, val darkColor: Color, val name: String)

@Composable
fun SettingsPage(darkColor: Color, lightColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Choose Color Theme",
            color = darkColor,
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { AppThemeManager.updateTheme(theme.lightColor, theme.darkColor) },
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
                            color = theme.darkColor
                        )
                    }
                }
            }
        }
    }
}