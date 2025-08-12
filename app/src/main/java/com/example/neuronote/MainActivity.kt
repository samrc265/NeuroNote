package com.example.neuronote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NeuroNoteApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroNoteApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentPageTitle by remember { mutableStateOf("Home") }
    var showInfoDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onPageSelected = { pageTitle ->
                    currentPageTitle = pageTitle
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HeaderBar(
                    title = currentPageTitle,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onInfoClick = { showInfoDialog = true }
                )
            },
            bottomBar = { FooterBar() }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(navController = navController, startDestination = "page1") {
                    composable("page1") { PageContent("Home Page") }
                    composable("page2") { PageContent("Mood Tracker") }
                    composable("page3") { PageContent("Mood Teacher") }
                    composable("page4") { PageContent("Sleep Chart") }
                    composable("page5") { PageContent("Notes") }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("OK") }
            },
            title = { Text("Page Info") },
            text = { Text("This page will display its respective feature information.") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(title: String, onMenuClick: () -> Unit, onInfoClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun FooterBar() {
    Column {
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("footer", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(navController: NavHostController, onPageSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = {
                navController.navigate("page1")
                onPageSelected("Home")
            }
        )
        NavigationDrawerItem(
            label = { Text("Mood Tracker") },
            selected = false,
            onClick = {
                navController.navigate("page2")
                onPageSelected("Mood Tracker")
            }
        )
        NavigationDrawerItem(
            label = { Text("Mood Teacher") },
            selected = false,
            onClick = {
                navController.navigate("page3")
                onPageSelected("Mood Teacher")
            }
        )
        NavigationDrawerItem(
            label = { Text("Sleep Chart") },
            selected = false,
            onClick = {
                navController.navigate("page4")
                onPageSelected("Sleep Chart")
            }
        )
        NavigationDrawerItem(
            label = { Text("Notes") },
            selected = false,
            onClick = {
                navController.navigate("page5")
                onPageSelected("Notes")
            }
        )
    }
}

@Composable
fun PageContent(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
    }
}
