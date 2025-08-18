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
    var currentPage by remember { mutableStateOf("Home") }
    var selectedDiary by remember { mutableStateOf<DiaryEntry?>(null) }

    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF388E3C)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = lightGreen) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "NeuroNote",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = darkGreen
                )
                Divider(color = Color.DarkGray)
                listOf("Home", "Calendar", "Mood Tracker", "Sleep Schedule", "Notes", "Diary", "Settings").forEach { page ->
                    NavigationDrawerItem(
                        label = { Text(page) },
                        selected = currentPage == page,
                        onClick = {
                            scope.launch { drawerState.close() }
                            currentPage = if (page == "Diary") "DiaryList" else page
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
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
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Info, contentDescription = "Info", tint = darkGreen)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = darkGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = lightGreen)
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = lightGreen,
                    tonalElevation = 2.dp,
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        "© 2025 NeuroNote",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
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
                when (currentPage) {
                    "Home" -> HomePage(darkGreen, lightGreen) { currentPage = "Mood Tracker" }
                    "DiaryList" -> DiaryListPage(darkGreen, lightGreen) { entry ->
                        selectedDiary = entry
                        currentPage = "DiaryDetail"
                    }
                    "DiaryDetail" -> DiaryDetailPage(darkGreen, lightGreen, selectedDiary) {
                        currentPage = "DiaryList"
                    }
                    else -> Text("$currentPage Page", style = MaterialTheme.typography.titleLarge, color = darkGreen)
                }
            }
        }
    }
}
