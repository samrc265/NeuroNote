package com.example.neuronote

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    ),
    GameItem(
        id = "HoldGame",
        title = "The Hold Game",
        description = "Test your patience by holding for a random, undisclosed amount of time.",
        icon = { tint -> Icon(Icons.Default.LockClock, contentDescription = null, tint = tint) }
    ),
    GameItem(
        id = "MemoryGame",
        title = "Sequence Recall",
        description = "Watch a sequence of lights and tap them back in the correct order.",
        icon = { tint -> Icon(Icons.Default.Memory, contentDescription = null, tint = tint) }
    )
)

// This composable manages the internal navigation between the list and the selected game.
@Composable
fun RecreationalsPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color,
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
        "HoldGame" -> HoldGame( // Game function is in TapperGame.kt
            darkColor = darkColor,
            textColor = textColor,
            onFinish = { currentPage = "GameList" }
        )
        "MemoryGame" -> MemoryGame( // Game function is defined below
            darkColor = darkColor,
            textColor = textColor,
            onFinish = { currentPage = "GameList" }
        )
        else -> Text("Game Not Found: $currentPage", color = textColor)
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

// =========================================================================
// SEQUENCE RECALL / MEMORY GAME (Complete Logic)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGame(darkColor: Color, textColor: Color, onFinish: () -> Unit) {
    val maxDots = 3
    val dotColors = remember { listOf(Color.Red, Color.Blue, Color.Green) }
    val dotSize = 80.dp

    var currentRound by remember { mutableStateOf(1) }
    var gameStage by remember { mutableStateOf("Ready") } // Ready, Watch, Repeat, Fail
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var message by remember { mutableStateOf("Focus to begin...") }

    val dotPulseScales = List(maxDots) { remember { Animatable(1f) } }
    val scope = rememberCoroutineScope()

    // Function to generate and display the sequence
    fun startNextRound() {
        scope.launch {
            gameStage = "Watch"
            message = "Watch Carefully..."
            playerInput = emptyList()

            // 1. Generate new sequence (add one more dot)
            val newDot = Random.nextInt(0, maxDots)
            sequence = sequence + newDot

            delay(1500) // Pause before showing sequence

            // 2. Play the sequence
            for (dotIndex in sequence) {
                scope.launch {
                    dotPulseScales[dotIndex].animateTo(1.2f, tween(150))
                    delay(500)
                    dotPulseScales[dotIndex].animateTo(1f, tween(150))
                }
                delay(700) // Time per flash
            }

            // 3. Transition to player turn
            gameStage = "Repeat"
            message = "Your Turn: Tap in order."
        }
    }

    // Handles the player tapping a dot
    fun handleTap(index: Int) {
        if (gameStage != "Repeat") return

        playerInput = playerInput + index

        // 1. Give visual feedback
        scope.launch {
            dotPulseScales[index].animateTo(1.2f, tween(100))
            dotPulseScales[index].animateTo(1f, tween(100))
        }

        // 2. Check if the tap is incorrect
        if (playerInput.last() != sequence[playerInput.lastIndex]) {
            message = "❌ Incorrect! Try Again."
            gameStage = "Fail"
            return
        }

        // 3. Check if the sequence is complete and correct
        if (playerInput.size == sequence.size) {
            currentRound++
            message = "✅ Success! Round $currentRound."
            scope.launch {
                delay(1000)
                startNextRound()
            }
        }
    }

    // Start/Reset Logic
    val resetGame = {
        currentRound = 1
        sequence = emptyList()
        gameStage = "Ready"
        message = "Focus to begin..."
        playerInput = emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sequence Recall", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {

            Text(
                message,
                color = textColor,
                style = MaterialTheme.typography.titleLarge,
                minLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(60.dp)
            )
            Text(
                "Round: $currentRound | Length: ${sequence.size}",
                color = textColor.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge
            )

            // --- The Tappable Dot Area ---
            Row(
                modifier = Modifier.fillMaxWidth().height(dotSize * 1.5f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until maxDots).forEach { index ->
                    BaseMemoryDot(
                        color = dotColors[index],
                        scale = dotPulseScales[index].value,
                        onTap = { handleTap(index) }
                    )
                }
            }

            // --- Control Buttons ---
            if (gameStage == "Ready") {
                Button(
                    // Calls the local function
                    onClick = { startNextRound() },
                    colors = ButtonDefaults.buttonColors(containerColor = darkColor)
                ) {
                    Text("Start Focus", fontSize = 18.sp)
                }
            } else if (gameStage == "Fail") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You lost focus.", color = darkColor, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = resetGame,
                        colors = ButtonDefaults.buttonColors(containerColor = darkColor)
                    ) {
                        Text("Try Again", fontSize = 18.sp)
                    }
                }
            } else if (gameStage == "Repeat") {
                Text(
                    "Taps remaining: ${sequence.size - playerInput.size}",
                    color = darkColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// Reusable composable for the simplified tappable dot
@Composable
fun BaseMemoryDot(color: Color, scale: Float, onTap: () -> Unit) {
    val dotSize = 80.dp
    Box(
        modifier = Modifier
            .size(dotSize)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.8f))
            .clickable(
                onClick = onTap,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // FIX: Add an empty content lambda {} to resolve the Box overload ambiguity
    }
}
