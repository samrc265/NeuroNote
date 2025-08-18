package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.neuronote.ui.theme.NeuroNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroNoteTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentPage by remember { mutableStateOf("Home") }

    val lightGreen = androidx.compose.ui.graphics.Color(0xFF90EE90)
    val darkGreen = androidx.compose.ui.graphics.Color(0xFF388E3C)

    when (currentPage) {
        "Home" -> HomePage(
            darkGreen = darkGreen,
            lightGreen = lightGreen,
            onUpdateMoodClick = { currentPage = "Mood" }
        )
        "Mood" -> MoodPage(
            darkGreen = darkGreen,
            lightGreen = lightGreen,
            onBackClick = { currentPage = "Home" }
        )
    }
}
