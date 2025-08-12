package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neuronote.ui.theme.NeuroNoteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroNoteTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedItem = remember { mutableStateOf("Home") }

    // Define the new colors
    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF388E3C) // A darker shade of green for contrast

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = lightGreen
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "NeuroNote",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = darkGreen
                )
                Divider(color = Color.DarkGray)
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = selectedItem.value == "Home",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Home"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Calendar") },
                    selected = selectedItem.value == "Calendar",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Calendar"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Mood Tracker") },
                    selected = selectedItem.value == "Mood Tracker",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Mood Tracker"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Sleep Schedule") },
                    selected = selectedItem.value == "Sleep Schedule",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Sleep Schedule"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Notes") },
                    selected = selectedItem.value == "Notes",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Notes"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Diary") },
                    selected = selectedItem.value == "Diary",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Diary"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f)) // Pushes settings to bottom

                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = selectedItem.value == "Settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = "Settings"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.4f)
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
                        IconButton(onClick = { /* Info icon click */ }) {
                            Icon(Icons.Filled.Info, contentDescription = "Info", tint = darkGreen)
                        }
                        IconButton(onClick = { /* Profile click */ }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = darkGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = lightGreen
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = lightGreen,
                    tonalElevation = 2.dp,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "© 2023 NeuroNote",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        fontSize = 12.sp,
                        color = darkGreen
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                when (selectedItem.value) {
                    "Home" -> Text("Welcome to NeuroNote!", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Calendar" -> Text("Calendar View", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Mood Tracker" -> Text("Mood Tracker Interface", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Sleep Schedule" -> Text("Sleep Schedule Details", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Notes" -> Text("Notes Editor", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Diary" -> Text("Diary Entries", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                    "Settings" -> Text("App Settings", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                }
            }
        }
    }
}