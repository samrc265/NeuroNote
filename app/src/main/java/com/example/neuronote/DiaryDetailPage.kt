package com.example.neuronote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun DiaryDetailPage(darkColor: Color, lightColor: Color, textColor: Color, entry: DiaryEntry?, onSave: () -> Unit) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var mood by remember { mutableStateOf(entry?.mood ?: 3) }
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1 to "😢", 2 to "☹️", 3 to "😐", 4 to "🙂", 5 to "😁").forEach { (value, emoji) ->
                Button(
                    onClick = { mood = value },
                    colors = ButtonDefaults.buttonColors(containerColor = if (mood == value) darkColor else lightColor)
                ) {
                    Text(emoji, fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                }
            }
        }

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Diary Text") },
            modifier = Modifier.fillMaxSize().weight(1f),
            maxLines = Int.MAX_VALUE,
            colors = textFieldColors
        )

        Button(onClick = {
            if (entry == null) {
                DiaryDataManager.addEntry(context, DiaryEntry(title = title.ifBlank { "Untitled" }, mood = mood, content = content))
            } else {
                DiaryDataManager.updateEntry(context, entry.copy(title = title.ifBlank { "Untitled" }, mood = mood, content = content))
            }
            onSave()
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = darkColor)) {
            Text("Save", color = Color.White)
        }
    }
}
