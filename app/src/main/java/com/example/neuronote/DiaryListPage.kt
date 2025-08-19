package com.example.neuronote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun DiaryListPage(darkGreen: androidx.compose.ui.graphics.Color, lightGreen: androidx.compose.ui.graphics.Color, onOpenEntry: (DiaryEntry?) -> Unit) {
    val entries = DiaryDataManager.entries

    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diary entries yet")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry ->
                    Card(colors = CardDefaults.cardColors(containerColor = lightGreen), modifier = Modifier.fillMaxWidth().clickable { onOpenEntry(entry) }) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(when (entry.mood) {
                                1 -> "😢"; 2 -> "☹️"; 3 -> "😐"; 4 -> "🙂"; else -> "😁"
                            }, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                Text(entry.date.toLocalDate().format(DateTimeFormatter.ISO_DATE))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(onClick = { onOpenEntry(null) }, containerColor = darkGreen, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Add", tint = androidx.compose.ui.graphics.Color.White)
        }
    }
}
