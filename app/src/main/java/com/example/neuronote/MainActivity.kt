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
import java.time.LocalDate

object Pages {
    const val HOME = "Home"
    const val MOOD = "Mood Tracker"
    const val DIARY_LIST = "DiaryList"
    const val DIARY_DETAIL = "DiaryDetail"
    const val SLEEP = "Sleep"
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

    val lightGreen = Color(0xFFFFD580)
    val darkGreen = Color(0xFFFFB085) // keeping in case needed for accents
    val textColor = Color(0xFF4E342E)

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
                    color = textColor
                )
                Divider()
                val drawerItem: @Composable (String, String) -> Unit = { label, page ->
                    NavigationDrawerItem(
                        label = { Text(label, color = textColor) },
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
                drawerItem("Sleep Schedule", Pages.SLEEP)

                Spacer(Modifier.weight(1f))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Settings", color = textColor) },
                    selected = currentPage == "Settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentPage = "Settings"
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.35f)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("NeuroNote", color = textColor, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = textColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = "Info", tint = textColor) }
                        IconButton(onClick = { }) { Icon(Icons.Filled.Person, contentDescription = "Profile", tint = textColor) }
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
                        "© ${LocalDate.now().year} NeuroNote",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = textColor
                    )
                }
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentPage) {
                    Pages.HOME -> HomePage(darkGreen = darkGreen, lightGreen = lightGreen) {
                        currentPage = Pages.MOOD
                    }
                    Pages.MOOD -> MoodTrackerPage(darkGreen = darkGreen, lightGreen = lightGreen) {
                        currentPage = Pages.HOME
                    }
                    Pages.DIARY_LIST -> DiaryListPage(darkGreen = darkGreen, lightGreen = lightGreen) { entry ->
                        selectedDiary = entry
                        currentPage = Pages.DIARY_DETAIL
                    }
                    Pages.DIARY_DETAIL -> DiaryDetailPage(
                        darkGreen = darkGreen,
                        lightGreen = lightGreen,
                        entry = selectedDiary
                    ) {
                        currentPage = Pages.DIARY_LIST
                        selectedDiary = null
                    }
                    Pages.SLEEP -> SleepPage(darkGreen = darkGreen, lightGreen = lightGreen)
                    else -> Text("$currentPage", color = textColor)
                }
            }
        }
    }
}
