package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuronote.ui.theme.NeuroNoteTheme
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf("Home") }

    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF388E3C)

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.3f),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = lightGreen
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "NeuroNote",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = darkGreen
                )
                Divider()

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = currentPage == "Home",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentPage = "Home"
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Mood Tracker") },
                    selected = currentPage == "Mood",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentPage = "Mood"
                    }
                )
                // you can add Calendar / Notes / etc here
            }
        }
    ) {
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
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = darkGreen)
                        }
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
                        onDone = { currentPage = "Home" } // after saving return Home
                    )
                }
            }
        }
    }
}
