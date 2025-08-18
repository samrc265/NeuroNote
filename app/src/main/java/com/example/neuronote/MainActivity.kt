package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
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
import kotlinx.coroutines.launch

object Pages {
    const val HOME = "Home"
    const val MOOD = "Mood Tracker"
    const val DIARY_LIST = "DiaryList"
    const val DIARY_DETAIL = "DiaryDetail"
}

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
    var currentPage by remember { mutableStateOf(Pages.HOME) }
    var selectedDiary by remember { mutableStateOf<DiaryEntry?>(null) }

    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF388E3C)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = lightGreen) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "NeuroNote",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = darkGreen
                )
                Divider(color = Color.DarkGray)

                // Drawer items
                val drawerItem: @Composable (String, String) -> Unit = { label, page ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = currentPage == page ||
                                (label == "Diary" && (currentPage == Pages.DIARY_LIST || currentPage == Pages.DIARY_DETAIL)),
                        onClick = {
                            scope.launch { drawerState.close() }
                            currentPage = page
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                drawerItem("Home", Pages.HOME)
                drawerItem("Mood Tracker", Pages.MOOD)
                drawerItem("Diary", Pages.DIARY_LIST)
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.35f)
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
                BottomAppBar(
                    containerColor = lightGreen,
                    tonalElevation = 2.dp,
                    modifier = Modifier.height(24.dp) // smaller footer
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
                    Pages.HOME -> HomePage(
                        darkGreen = darkGreen,
                        lightGreen = lightGreen,
                        onUpdateMoodClick = { currentPage = Pages.MOOD }
                    )

                    Pages.MOOD -> MoodTrackerPage(
                        darkGreen = darkGreen,
                        lightGreen = lightGreen,
                        onDone = { currentPage = Pages.HOME } // ⬅️ no back button; just finish & return
                    )

                    Pages.DIARY_LIST -> DiaryListPage(darkGreen, lightGreen) { entry ->
                        selectedDiary = entry
                        currentPage = Pages.DIARY_DETAIL
                    }

                    Pages.DIARY_DETAIL -> DiaryDetailPage(darkGreen, lightGreen, selectedDiary) {
                        currentPage = Pages.DIARY_LIST
                        selectedDiary = null
                    }
                }
            }
        }
    }
}
