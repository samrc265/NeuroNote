package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuronote.ui.theme.NeuroNoteTheme

@ExperimentalMaterial3Api
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

@ExperimentalMaterial3Api
@Composable
fun AppNavigation() {
    var currentPage by remember { mutableStateOf("Home") }

    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF388E3C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NeuroNote",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                actions = {
                    IconButton(onClick = { /* Info */ }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = darkGreen)
                    }
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = darkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lightGreen)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = lightGreen, tonalElevation = 2.dp) {
                Text(
                    "© 2025 NeuroNote",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = darkGreen
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (currentPage) {
                "Home" -> HomePage(
                    darkGreen = darkGreen,
                    lightGreen = lightGreen,
                    onUpdateMoodClick = { currentPage = "Mood" }
                )
                "Mood" -> MoodPage(
                    darkGreen = darkGreen,
                    lightGreen = lightGreen,
                    onDone = { currentPage = "Home" } // ✅ returns to home after save
                )
            }
        }
    }
}
