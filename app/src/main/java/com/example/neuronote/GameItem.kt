package com.example.neuronote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Define all available games and their titles/descriptions here
data class GameItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: @Composable (Color) -> Unit
)

// List of all offline games available in the app
val gameList = listOf(
    GameItem(
        id = "TapperGame",
        title = "Mindful Tapper",
        description = "A simple tapping game to anchor your focus in the present moment.",
        icon = { tint -> Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = tint) }
    )
    // Future games will be added to this list
)

// This composable manages the internal navigation between the list and the selected game.
@Composable
fun RecreationalsPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color,
    // The following callbacks are provided by MainActivity but are managed internally here.
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Current state can be "GameList" or a specific game ID like "TapperGame"
    var currentPage by remember { mutableStateOf("GameList") }

    when (currentPage) {
        "GameList" -> GameListPage(
            darkColor = darkColor,
            textColor = textColor,
            onGameSelect = { id -> currentPage = id } // Internal switch to the game screen
        )
        "TapperGame" -> TapperGame(
            darkColor = darkColor,
            textColor = textColor,
            onFinish = { currentPage = "GameList" } // Navigates back to the list
        )
        // Add more 'when' branches for future games here
        else -> Text("Game Not Found", color = textColor)
    }
}

@Composable
fun GameListPage(
    darkColor: Color,
    textColor: Color,
    onGameSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Simple, Offline Focus Games",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(gameList) { game ->
            Card(
                colors = CardDefaults.cardColors(containerColor = darkColor.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGameSelect(game.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            game.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            game.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                    game.icon(darkColor)
                }
            }
        }
    }
}