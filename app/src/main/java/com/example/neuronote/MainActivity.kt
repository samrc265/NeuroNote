package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.neuronote.ui.theme.NeuroNoteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize the global music player so it survives page changes
        MusicPlayerManager.init(
            applicationContext,
            listOf(
                Track("1", "Anger Calming Music", "NeuroNote", R.raw.angercalmingmusic),
                Track("2", "Sleep", "NeuroNote", R.raw.sleepmusic),
                Track("3", "Flowing River ASMR", "NeuroNote", R.raw.riverasmr),
                Track("4", "Music For ADHD Users", "NeuroNote", R.raw.adhdcalmingmusic),
                Track("5", "Metronome", "NeuroNote", R.raw.metronome)
            )
        )

        setContent {
            // Load persisted theme/data (your existing managers)
            AppThemeManager.loadTheme(this)
            DiaryDataManager.loadEntries(this)
            MoodDataManager.loadData(this)
            SleepDataManager.loadData(this)

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

    // Observe theme states from the manager
    val lightColorTheme by AppThemeManager.lightColor
    val darkColorTheme by AppThemeManager.darkColor
    val isDark by AppThemeManager.isDarkTheme

    // Define colors based on the current theme mode
    val backgroundColor = if (isDark) Color(0xFF121212) else lightColorTheme
    val primaryColor = darkColorTheme
    val cardColor = if (isDark) Color(0xFF1E1E1E) else lightColorTheme
    val textColor = if (isDark) Color.White.copy(alpha = 0.9f) else darkColorTheme
    val topBarColor = if (isDark) Color(0xFF222222) else Color.White.copy(alpha = 0.95f)

    // INFO CONTENT definitions
    val infoContent = remember(currentPage) {
        when (currentPage) {
            "Home" -> InfoContent(
                title = "Home Page",
                text = "This is your dashboard. It provides an overview of your mood and emotional distribution over time. You can also quickly add your current mood from here."
            )
            "Mood Tracker" -> InfoContent(
                title = "Mood Tracker",
                text = "Track your daily mood with a simple rating and a note. Saving your mood helps you monitor your emotional trends and patterns."
            )
            "Sleep Tracker" -> InfoContent(
                title = "Sleep Tracker",
                text = "Log your sleep hours for each day of the week. This tracker helps you visualize your sleep patterns and maintain a healthy sleep schedule."
            )
            "Diary" -> InfoContent(
                title = "Diary",
                text = "A private space for your thoughts. You can add new entries, edit existing ones, or delete them. Each entry is tagged with your mood for that day."
            )
            "Chatbot" -> InfoContent(
                title = "Chatbot",
                text = "Ask questions and get AI-powered answers using Gemini. Great for mood tips, journaling prompts, and general queries."
            )
            "Recreationals" -> InfoContent(
                title = "Recreationals",
                text = "A place for quick, mindful games designed to help you focus, relax, and take a non-heavy break."
            )
            "Focus Mode" -> InfoContent(
                title = "Focus Mode",
                text = "Enter a distraction-free session. Pick a preset or custom timer. When enabled, the app sets your phone to Do Not Disturb (silent) and restores it when you finish."
            )
            // NEW page info
            "Music" -> InfoContent(
                title = "Music",
                text = "Play calming tracks while you journal or focus. The playlist uses free ambient tracks."
            )
            "Settings" -> InfoContent(
                title = "Settings",
                text = "Customize your app experience. Choose a color theme that suits your mood and preferences."
            )
            "DiaryDetail" -> InfoContent(title = "Edit Entry", text = "Update your diary entry and save.")
            else -> InfoContent(title = "", text = "")
        }
    }

    // High-contrast drawer item colors
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = primaryColor.copy(alpha = 0.15f),
        selectedTextColor = primaryColor,
        selectedIconColor = primaryColor,
        unselectedTextColor = textColor,
        unselectedContainerColor = Color.Transparent
    )

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
                @Composable
                fun drawerItem(label: String) {
                    NavigationDrawerItem(
                        label = { Text(label, color = textColor) },
                        selected = currentPage == label,
                        onClick = {
                            currentPage = label
                            scope.launch { drawerState.close() }
                        },
                        colors = drawerItemColors
                    )
                }
                drawerItem("Home")
                drawerItem("Mood Tracker")
                drawerItem("Sleep Tracker")
                drawerItem("Diary")
                drawerItem("Chatbot")
                drawerItem("Recreationals")
                drawerItem("Focus Mode")
                drawerItem("Music")      // NEW entry
                drawerItem("Settings")
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentPage, color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = textColor)
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
                            Icon(Icons.Filled.Info, contentDescription = "Info", tint = textColor)
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
                    "Home" -> HomePage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor,
                        onUpdateMoodClick = { currentPage = "Mood Tracker" }
                    )
                    "Mood Tracker" -> MoodTrackerPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor,
                        onDone = { currentPage = "Home" }
                    )
                    "Sleep Tracker" -> SleepPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor
                    )
                    "Diary" -> DiaryListPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor,
                        onOpenEntry = {
                            diaryEntryToEdit = it
                            currentPage = "DiaryDetail"
                        }
                    )
                    "Settings" -> SettingsPage(
                        textColor = textColor,
                        lightColor = cardColor
                    )
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
                    "Chatbot" -> ChatbotPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor
                    )
                    "Focus Mode" -> FocusModePage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor
                    )
                    // Recreationals hub manages inner screens
                    "Recreationals", "TapperGame", "HoldGame", "MemoryGame" -> RecreationalsPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor,
                        onNavigateToGame = { gameId -> currentPage = gameId },
                        onNavigateBack = { currentPage = "Recreationals" }
                    )
                    // NEW Music page route
                    "Music" -> MusicPage(
                        darkColor = primaryColor,
                        lightColor = cardColor,
                        textColor = textColor
                    )
                    else -> Text("Page Not Found: $currentPage", color = textColor)
                }
            }

            if (showInfoDialog) {
                InfoDialog(
                    info = infoContent,
                    containerColor = cardColor,
                    textColor = textColor,
                    onDismiss = { showInfoDialog = false }
                )
            }
        }
    }
}
