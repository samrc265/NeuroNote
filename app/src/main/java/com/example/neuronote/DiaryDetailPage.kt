package com.example.neuronote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Helper function to map mood score to an aesthetic icon and color
private fun getMoodIconAndColor(mood: Int): Pair<ImageVector, Color> {
    return when (mood) {
        1 -> Icons.Filled.BrokenImage to Color(0xFFD32F2F) // Very Sad (Deep Red)
        2 -> Icons.Filled.Cloud to Color(0xFFF57C00)        // Sad (Amber)
        3 -> Icons.Filled.Balance to Color(0xFF1976D2)       // Neutral (Primary Blue)
        4 -> Icons.Filled.WbSunny to Color(0xFF66BB6A)      // Happy (Light Green)
        else -> Icons.Filled.FlashOn to Color(0xFF388E3C)   // Very Happy (Dark Green)
    }
}

@Composable
fun DiaryDetailPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color,
    entry: DiaryEntry?,
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var mood by remember { mutableIntStateOf(entry?.mood ?: 3) } // 1..5
    var content by remember { mutableStateOf(entry?.content ?: "") }
    val context = LocalContext.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedBorderColor = darkColor,
        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
        focusedLabelColor = darkColor,
        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
        cursorColor = darkColor,
        focusedContainerColor = lightColor,
        unfocusedContainerColor = lightColor
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        // ===== Mood selector: 5 Sprite Chips =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Composable
            fun MoodSpriteChip(value: Int) {
                val selected = mood == value
                val (icon, tint) = getMoodIconAndColor(value)

                Surface(
                    onClick = { mood = value },
                    shape = MaterialTheme.shapes.large,
                    color = if (selected) darkColor else lightColor,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                    border = if (selected) null else BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .clickable(enabled = !selected) {} // Disable click if already selected for visual emphasis
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Mood: $value",
                            tint = if (selected) Color.White else tint,
                            modifier = Modifier.size(36.dp) // Large and clear icon
                        )
                    }
                }
            }

            // Left → right: 1 Very Sad, 2 Sad, 3 Neutral, 4 Happy, 5 Very Happy
            MoodSpriteChip(1)
            MoodSpriteChip(2)
            MoodSpriteChip(3)
            MoodSpriteChip(4)
            MoodSpriteChip(5)
        }

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Diary Text") },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            maxLines = Int.MAX_VALUE,
            colors = textFieldColors
        )

        Button(
            onClick = {
                val safeTitle = title.ifBlank { "Untitled" }
                val newEntry = DiaryEntry(
                    id = entry?.id ?: System.currentTimeMillis(), // Use existing ID if editing
                    title = safeTitle,
                    mood = mood,
                    content = content
                )

                if (entry == null) {
                    DiaryDataManager.addEntry(context = context, entry = newEntry)
                } else {
                    DiaryDataManager.updateEntry(context = context, updated = newEntry)
                }
                onSave()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = darkColor)
        ) {
            Text("Save", color = Color.White)
        }
    }
}
