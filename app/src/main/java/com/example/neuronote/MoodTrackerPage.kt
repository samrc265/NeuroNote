package com.example.neuronote

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons for flexibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate

// Define the Sprite/Color data structure for mapping the slider value (1-5) to UI elements
data class MoodSprite(
    val icon: ImageVector,
    val color: Color
)

// Map mood value (1-5) to a meaningful sprite and color
private fun getMoodSprite(mood: Int): MoodSprite {
    return when (mood) {
        // 1: Very Sad - Broken/Fragmented
        1 -> MoodSprite(Icons.Filled.BrokenImage, Color(0xFFD32F2F)) // Deep Red
        // 2: Sad - Heavy/Downward
        2 -> MoodSprite(Icons.Filled.Cloud, Color(0xFFFBC02D)) // Amber
        // 3: Neutral - Stillness/Balance
        3 -> MoodSprite(Icons.Filled.Balance, Color(0xFF1976D2)) // Primary Blue
        // 4: Happy - Light/Uplift
        4 -> MoodSprite(Icons.Filled.WbSunny, Color(0xFF66BB6A)) // Light Green
        // 5: Very Happy - Energy/Thriving
        5 -> MoodSprite(Icons.Filled.FlashOn, Color(0xFF388E3C)) // Dark Green
        else -> MoodSprite(Icons.Filled.Balance, Color.Gray)
    }
}

@Composable
fun MoodTrackerPage(darkColor: Color, lightColor: Color, textColor: Color, onDone: () -> Unit) {
    var mood by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val moodLabels = listOf("Very Sad", "Sad", "Neutral", "Happy", "Very Happy")
    val isDark by AppThemeManager.isDarkTheme // Check for dark mode

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedBorderColor = darkColor,
        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
        focusedLabelColor = darkColor,
        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
        cursorColor = darkColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Layered Abstract Visualization (NEW AESTHETIC) ---
        AnimatedContent(targetState = mood) { targetMood ->
            val sprite = getMoodSprite(targetMood)
            val baseSize = 120.dp
            val ringColor = sprite.color.copy(alpha = if (isDark) 0.6f else 0.2f)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(baseSize * 1.5f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 3 (Outermost)
                    Box(
                        modifier = Modifier
                            .size(baseSize * 1.5f)
                            .clip(CircleShape)
                            .background(ringColor.copy(alpha = 0.3f))
                    )
                    // Layer 2
                    Box(
                        modifier = Modifier
                            .size(baseSize * 1.2f)
                            .clip(CircleShape)
                            .background(ringColor.copy(alpha = 0.5f))
                    )
                    // Layer 1 (Core)
                    Box(
                        modifier = Modifier
                            .size(baseSize)
                            .clip(CircleShape)
                            .background(sprite.color.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icon at the Center
                        Icon(
                            imageVector = sprite.icon,
                            contentDescription = moodLabels[targetMood - 1],
                            tint = Color.White, // White icon on solid core color
                            modifier = Modifier.size(baseSize / 3)
                        )
                    }
                }

                Text(
                    moodLabels[targetMood - 1],
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = sprite.color // Keep the text color matching the mood
                )
            }
        }

        // --- Mood Selection Slider ---
        Slider(
            value = mood.toFloat(),
            onValueChange = { mood = it.toInt().coerceIn(1, 5) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = darkColor,
                activeTrackColor = darkColor,
                inactiveTrackColor = lightColor
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Note Field ---
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Add a note (optional)") },
            placeholder = { Text("What made you feel this way today?") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 6,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- Save Button ---
        Button(
            onClick = {
                scope.launch {
                    MoodDataManager.addDailyMood(mood, LocalDate.now(), note.ifBlank { null })
                    note = ""
                    onDone()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = darkColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Save Mood Entry", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
