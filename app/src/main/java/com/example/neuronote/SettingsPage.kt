package com.example.neuronote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
fun SettingsPage(textColor: Color, lightColor: Color, darkColor: Color) { // Added darkColor
    val context = LocalContext.current
    val isAppLockEnabled by AppLockManager.isLockEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween // Push the lock button to the bottom
    ) {
        // --- Top Section: Theme Selection ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Color Theme",
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val themes = listOf(
                ColorTheme(Color(0xFFC8E6C9), Color(0xFF388E3C), "Forest Green"),
                ColorTheme(Color(0xFFFFCDD2), Color(0xFFD32F2F), "Soft Red"),
                ColorTheme(Color(0xFFBBDEFB), Color(0xFF1976D2), "Ocean Blue"),
                ColorTheme(Color(0xFFFFF9C4), Color(0xFFFBC02D), "Golden Yellow"),
                ColorTheme(Color(0xFFE1BEE7), Color(0xFF6A1B9A), "Amethyst Purple"),
                ColorTheme(Color(0xFFE0E0E0), Color(0xFF424242), "Charcoal Grey")
            )

            themes.forEach { theme ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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

        // --- Bottom Section: App Lock Button (Reacts to theme colors) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to push it down
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isAppLockEnabled) {
                        // If enabled, prompt to disable or change (for now, let's toggle)
                        AppLockManager.disableAppLock(context)
                    } else {
                        // If disabled, start the setup process (will set to enabled on success)
                        AppLockManager.showSetupBiometricPrompt(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkColor, // Use darkColor for the button background
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "App Lock",
                        tint = Color.White
                    )
                    Text(
                        text = if (isAppLockEnabled) "App Lock: Enabled (Tap to Disable)" else "Secure App with Biometrics",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isAppLockEnabled) "App will require Biometric/PIN/Pattern on open." else "Add a security layer using device lock.",
                color = textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
