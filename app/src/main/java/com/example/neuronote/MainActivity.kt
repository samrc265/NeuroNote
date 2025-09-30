package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.neuronote.data.MoodDataManager
import com.example.neuronote.data.SleepDataManager
import com.example.neuronote.ui.CalendarPage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Load theme
            AppThemeManager.loadTheme(this)

            // Init Room managers
            SleepDataManager.init(this)
            MoodDataManager.init(this)
            DiaryDataManager.loadEntries(this) // keep diary as JSON if desired

            // Diary still uses JSON
            DiaryDataManager.loadEntries(this)

            NeuroNoteTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf("Home") }
    var diaryEntryToEdit by remember { mutableStateOf<DiaryEntry?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Observe theme
    val lightColorTheme by AppThemeManager.lightColor
    val darkColorTheme by AppThemeManager.darkColor
    val isDark by AppThemeManager.isDarkTheme

    val backgroundColor = if (isDark) Color(0xFF121212) else lightColorTheme
    val primaryColor = darkColorTheme
    val cardColor = if (isDark) Color(0xFF1E1E1E) else lightColorTheme
    val textColor = if (isDark) Color.White.copy(alpha = 0.9f) else darkColorTheme
    val topBarColor = if (isDark) Color(0xFF222222) else Color.White.copy(alpha = 0.95f)

    val infoContent = remember(currentPage) {
        when (currentPage) {
            "Home" -> InfoContent(
                title = "Home Page",
                text = "Dashboard overview of your mood and emotional distribution."
            )
            "Mood Tracker" -> InfoContent(
                title = "Mood Tracker",
                text = "Track your daily mood with a rating and optional note."
            )
            "Sleep Tracker" -> InfoContent(
                title = "Sleep Tracker",
                text = "Log your sleep hours for each day of the week."
            )
            "Diary" -> InfoContent(
                title = "Diary",
                text = "Write private entries tagged with your mood."
            )
            "Calendar" -> InfoContent(
                title = "Calendar",
                text = "View your sleep hours and average mood on a monthly calendar."
            )
            "Settings" -> InfoContent(
                title = "Settings",
                text = "Customize colors and theme."
            )
            else -> InfoContent("", "")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = backgroundColor) {
                Text(
                    "NeuroNote",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
                val pages = listOf("Home", "Mood Tracker", "Sleep Tracker", "Diary", "Calendar", "Settings")
                pages.forEach { page ->
                    NavigationDrawerItem(
                        label = { Text(page, color = textColor) },
                        selected = currentPage == page,
                        onClick = {
                            currentPage = page
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentPage, color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = textColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { AppThemeManager.toggleTheme(context) }) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = textColor
                            )
                        }
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    "Home" -> HomePage(primaryColor, cardColor, textColor) { currentPage = "Mood Tracker" }
                    "Mood Tracker" -> MoodTrackerPage(primaryColor, cardColor, textColor) { currentPage = "Home" }
                    "Sleep Tracker" -> SleepPage(primaryColor, cardColor, textColor)
                    "Diary" -> DiaryListPage(primaryColor, cardColor, textColor) {
                        diaryEntryToEdit = it
                        currentPage = "DiaryDetail"
                    }
                    "DiaryDetail" -> DiaryDetailPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor,
                        entry = diaryEntryToEdit,
                        onSave = {
                            currentPage = "Diary"
                            diaryEntryToEdit = null
                        }
                    )
                    "Calendar" -> CalendarPage(primaryColor, cardColor, textColor)
                    "Settings" -> SettingsPage(textColor, cardColor)
                }
            }

            if (showInfoDialog) {
                InfoDialog(infoContent, cardColor, textColor) { showInfoDialog = false }
            }
        }
    }
}
