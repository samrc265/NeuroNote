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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            // Load all data when the app starts
            AppThemeManager.loadTheme(context)
            DiaryDataManager.loadEntries(context)
            MoodDataManager.loadData(context)
            SleepDataManager.loadData(context)

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

    val context = LocalContext.current // Get context once

    val lightColor by AppThemeManager.lightColor
    val darkColor by AppThemeManager.darkColor

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
            "Settings" -> InfoContent(
                title = "Settings",
                text = "Customize your app experience. Choose a color theme that suits your mood and preferences."
            )
            else -> InfoContent(title = "", text = "")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "NeuroNote",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = darkColor
                )
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = currentPage == "Home",
                    onClick = {
                        currentPage = "Home"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Mood Tracker") },
                    selected = currentPage == "Mood Tracker",
                    onClick = {
                        currentPage = "Mood Tracker"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Sleep Tracker") },
                    selected = currentPage == "Sleep Tracker",
                    onClick = {
                        currentPage = "Sleep Tracker"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Diary") },
                    selected = currentPage == "Diary",
                    onClick = {
                        currentPage = "Diary"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentPage == "Settings",
                    onClick = {
                        currentPage = "Settings"
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentPage, color = darkColor) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = darkColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Profile Action */ }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = darkColor)
                        }
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = darkColor)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(lightColor),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    "Home" -> HomePage(
                        darkColor = darkColor,
                        lightColor = lightColor,
                        onUpdateMoodClick = { currentPage = "Mood Tracker" }
                    )
                    "Mood Tracker" -> MoodTrackerPage(
                        darkColor = darkColor,
                        lightColor = lightColor,
                        onDone = { currentPage = "Home" }
                    )
                    "Sleep Tracker" -> SleepPage(
                        darkColor = darkColor,
                        lightColor = lightColor
                    )
                    "Diary" -> DiaryListPage(
                        darkColor = darkColor,
                        lightColor = lightColor,
                        onOpenEntry = {
                            diaryEntryToEdit = it
                            currentPage = "DiaryDetail"
                        }
                    )
                    "Settings" -> SettingsPage(
                        darkColor = darkColor,
                        lightColor = lightColor
                    )
                    "DiaryDetail" -> DiaryDetailPage(
                        darkColor = darkColor,
                        lightColor = lightColor,
                        entry = diaryEntryToEdit,
                        onSave = {
                            currentPage = "Diary"
                            diaryEntryToEdit = null
                        }
                    )
                }
            }

            if (showInfoDialog) {
                InfoDialog(
                    info = infoContent,
                    darkColor = darkColor,
                    onDismiss = { showInfoDialog = false }
                )
            }
        }
    }
}