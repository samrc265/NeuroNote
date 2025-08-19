package com.example.neuronote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun DiaryListPage(
    darkGreen: androidx.compose.ui.graphics.Color,
    lightGreen: androidx.compose.ui.graphics.Color,
    onOpenEntry: (DiaryEntry?) -> Unit
) {
    val entries = DiaryDataManager.entries

    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diary entries yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = lightGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onOpenEntry(entry) }
                                    .weight(1f)
                            ) {
                                Text(
                                    when (entry.mood) {
                                        1 -> "😢"
                                        2 -> "☹️"
                                        3 -> "😐"
                                        4 -> "🙂"
                                        else -> "😁"
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                    Text(entry.date.toLocalDate().format(DateTimeFormatter.ISO_DATE))
                                }
                            }

                            // 🗑️ Delete Button
                            IconButton(onClick = { DiaryDataManager.removeEntry(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Entry",
                                    tint = darkGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onOpenEntry(null) },
            containerColor = darkGreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
