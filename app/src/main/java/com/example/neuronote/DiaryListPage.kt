package com.example.neuronote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

// Helper function to map mood score to an aesthetic icon and color, mirroring MoodTrackerPage
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
fun DiaryListPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color,
    onOpenEntry: (DiaryEntry?) -> Unit
) {
    val entries = DiaryDataManager.entries
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diary entries yet", color = textColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    val (icon, tint) = getMoodIconAndColor(entry.mood)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = lightColor),
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
                                // --- New Sprite Icon Display ---
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Mood: ${entry.mood}",
                                    tint = tint,
                                    modifier = Modifier
                                        .size(28.dp) // Large enough to be clearly visible
                                        .padding(end = 12.dp)
                                )
                                // --- End New Sprite Icon Display ---

                                Column {
                                    Text(entry.title, style = MaterialTheme.typography.titleMedium, color = textColor)
                                    Text(
                                        entry.date.toLocalDate().format(DateTimeFormatter.ISO_DATE),
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            // 🗑️ Delete Button
                            IconButton(onClick = { DiaryDataManager.removeEntry(context, entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Entry",
                                    tint = darkColor
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onOpenEntry(null) },
            containerColor = darkColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        }
    }
}
