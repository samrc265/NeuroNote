package com.example.neuronote

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun MoodPage(
    darkGreen: Color,
    lightGreen: Color,
    onDone: () -> Unit
) {
    var mood by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }

    val moodEmojis = listOf("😢", "😟", "😐", "😊", "😁")
    val moodLabels = listOf("Very Sad", "Sad", "Neutral", "Happy", "Very Happy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AnimatedContent(targetState = mood, label = "MoodEmoji") { targetMood ->
            Text(text = moodEmojis[targetMood - 1], fontSize = 80.sp)
        }
        Text(moodLabels[mood - 1], fontWeight = FontWeight.Bold, fontSize = 20.sp, color = darkGreen)

        Slider(
            value = mood.toFloat(),
            onValueChange = { mood = it.toInt().coerceIn(1, 5) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = darkGreen,
                activeTrackColor = darkGreen,
                inactiveTrackColor = lightGreen
            )
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Add a note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                MoodDataManager.addDailyMood(mood, LocalDate.now(), note.ifBlank { null })
                note = ""
                onDone()
            },
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Mood", color = Color.White, fontSize = 16.sp)
        }
    }
}
