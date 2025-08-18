package com.example.neuronote

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MoodPage(
    darkGreen: Color,
    lightGreen: Color,
    onBackClick: () -> Unit
) {
    var mood by remember { mutableStateOf(3) } // Default mood = Neutral
    var note by remember { mutableStateOf("") }

    val moodEmojis = listOf("😢", "😟", "😐", "😊", "😁")
    val moodLabels = listOf("Very Sad", "Sad", "Neutral", "Happy", "Very Happy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Track Your Mood",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = darkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lightGreen)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = lightGreen,
                tonalElevation = 2.dp,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    "© 2023 NeuroNote",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = darkGreen
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Emoji
            AnimatedContent(targetState = mood, label = "MoodEmoji") { targetMood ->
                Text(
                    text = moodEmojis[targetMood - 1],
                    fontSize = 80.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Label
            Text(
                text = moodLabels[mood - 1],
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = darkGreen
            )

            // Slider
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

            // Notes
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    MoodDataManager.addDailyMood(mood, LocalDate.now(), note.ifBlank { null })
                    note = "" // clear input
                },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Mood", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
