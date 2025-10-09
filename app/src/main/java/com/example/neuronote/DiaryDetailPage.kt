package com.example.neuronote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DiaryDetailPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color,
    entry: DiaryEntry?,
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var mood by remember { mutableStateOf(entry?.mood ?: 3) } // 1..5
    var content by remember { mutableStateOf(entry?.content ?: "") }
    val context = LocalContext.current   // ✅ capture once in composable scope

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

        // ===== Mood selector: 5 emoji chips (Very Sad … Very Happy) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Composable
            fun EmojiChip(value: Int, emoji: String) {
                val selected = mood == value
                Surface(
                    onClick = { mood = value },
                    shape = MaterialTheme.shapes.large,
                    color = if (selected) darkColor else lightColor,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                    border = if (selected) null else BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (selected) Color.White else textColor
                        )
                    }
                }
            }

            // Left → right: 1 Very Sad, 2 Sad, 3 Neutral, 4 Happy, 5 Very Happy
            EmojiChip(1, "😢")
            EmojiChip(2, "😟")
            EmojiChip(3, "😐")
            EmojiChip(4, "🙂")
            EmojiChip(5, "😁")
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
                if (entry == null) {
                    DiaryDataManager.addEntry(
                        context = context,                                // ✅ use captured context
                        entry = DiaryEntry(title = safeTitle, mood = mood, content = content)
                    )
                } else {
                    DiaryDataManager.updateEntry(
                        context = context,                                // ✅ use captured context
                        updated = entry.copy(title = safeTitle, mood = mood, content = content)
                    )
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
