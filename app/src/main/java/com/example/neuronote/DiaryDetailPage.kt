package com.example.neuronote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiaryDetailPage(darkGreen: androidx.compose.ui.graphics.Color, lightGreen: androidx.compose.ui.graphics.Color, entry: DiaryEntry?, onSave: () -> Unit) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var mood by remember { mutableStateOf(entry?.mood ?: 3) }
    var content by remember { mutableStateOf(entry?.content ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1 to "😢", 2 to "☹️", 3 to "😐", 4 to "🙂", 5 to "😁").forEach { (value, emoji) ->
                TextButton(onClick = { mood = value }, colors = ButtonDefaults.buttonColors(containerColor = if (mood == value) darkGreen else lightGreen)) {
                    Text(emoji, fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                }
            }
        }

        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Diary Text") }, modifier = Modifier.fillMaxSize().weight(1f), maxLines = Int.MAX_VALUE)

        Button(onClick = {
            if (entry == null) {
                DiaryDataManager.addEntry(DiaryEntry(title = title.ifBlank { "Untitled" }, mood = mood, content = content))
            } else {
                DiaryDataManager.updateEntry(entry.copy(title = title.ifBlank { "Untitled" }, mood = mood, content = content))
            }
            onSave()
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = darkGreen)) {
            Text("Save", color = androidx.compose.ui.graphics.Color.White)
        }
    }
}
