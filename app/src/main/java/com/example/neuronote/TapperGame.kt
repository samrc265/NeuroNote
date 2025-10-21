package com.example.neuronote

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi // New: Required for combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable // New: Used in TheHoldGame
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState // New: Used in TheHoldGame
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack  // New: Used in TheHoldGame
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs // New: Used in TheHoldGame
import kotlin.random.Random // New: Used in TheHoldGame

// Helper extension function for formatting decimals (Must be outside any Composable)
fun Double.format(digits: Int) = "%.${digits}f".format(this)

// =========================================================================
// 1. MINDFUL TAPPER GAME (Original)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapperGame(darkColor: Color, textColor: Color, onFinish: () -> Unit) {
    val targetTaps = 30
    var tapsRemaining by remember { mutableStateOf(targetTaps) }
    var gameActive by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 800)
        )
        while (gameActive) {
            pulseScale.animateTo(1f, tween(300))
            pulseScale.animateTo(1.1f, tween(800))
        }
    }

    fun handleTap() {
        if (!gameActive) return
        tapsRemaining--
        scope.launch {
            pulseScale.snapTo(1.2f)
            pulseScale.animateTo(1.1f, tween(150))
        }
        if (tapsRemaining <= 0) {
            gameActive = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mindful Tapper", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (gameActive) {
                Text(
                    "Taps Remaining:",
                    color = textColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    tapsRemaining.toString(),
                    color = darkColor,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(32.dp))
                // FIX: Replaced Box with Surface for elevation and modern look
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(scaleX = pulseScale.value, scaleY = pulseScale.value),
                    shape = CircleShape,
                    color = darkColor,
                    shadowElevation = 8.dp, // Added shadow
                    tonalElevation = 4.dp // Added tonal elevation
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                onClick = ::handleTap,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    "Focus on the rhythm and sensation of each tap.",
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    "🧘 Focus Achieved!",
                    color = darkColor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "You completed the mindful tapping exercise.",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = darkColor)
                ) {
                    Text("Back to Recreations")
                }
            }
        }
    }
}

// =========================================================================
// 2. THE HOLD GAME (Complete Logic)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HoldGame(darkColor: Color, textColor: Color, onFinish: () -> Unit) {
    // Game states
    var targetHoldTime by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var resultText by remember { mutableStateOf("Press and HOLD the circle.") }
    var gameActive by remember { mutableStateOf(true) }
    var isHolding by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (!gameActive) return@LaunchedEffect
        if (isPressed) {
            isHolding = true
            resultText = "HOLDING..."
            startTime = System.currentTimeMillis()
            pulseScale.animateTo(1.15f, tween(300))
        } else if (isHolding) {
            isHolding = false
            pulseScale.animateTo(1f, tween(300))
            val releaseTime = System.currentTimeMillis()
            val actualHoldTime = releaseTime - startTime
            val difference = abs(actualHoldTime - targetHoldTime)
            val actualHoldSeconds = actualHoldTime / 1000.0
            val message = when {
                difference <= 300 -> " 🌟  Perfect!\n(${actualHoldSeconds.format(2)}s)"
                difference <= 700 -> " ✨  Great focus!\n(${actualHoldSeconds.format(2)}s)"
                difference <= 1500 -> "🧘 Good attempt.\n(${actualHoldSeconds.format(2)}s)"
                else -> " 😴  Lost focus.\n(${actualHoldSeconds.format(2)}s)"
            }
            resultText = message
            gameActive = false
        }
    }

    // Initialize the target time when the game starts
    LaunchedEffect(Unit) {
        targetHoldTime = Random.nextLong(4000L, 8000L) // Random time between 4 and 8 seconds
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Hold Game", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                "Objective: Hold the circle for the perfect, unknown amount of time.",
                color = textColor.copy(alpha = 0.8f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // --- Game Message ---
            Text(
                resultText,
                color = if (!gameActive) darkColor else textColor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.heightIn(min = 60.dp)
            )
            // --- The Tappable Circle (The Hold Button) ---
            // FIX: Replaced Box with Surface for elevation and modern look
            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(scaleX = pulseScale.value, scaleY = pulseScale.value),
                shape = CircleShape,
                color = darkColor,
                shadowElevation = 12.dp, // Added strong shadow
                tonalElevation = 6.dp // Added tonal elevation
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { /* No action on simple click */ },
                            onLongClick = { /* No action on long click */ }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHolding) "HOLD!" else if (gameActive) "START" else "DONE",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            // --- Bottom Instructions / Retry Button ---
            if (gameActive) {
                Text(
                    "Focus entirely on your internal clock. Do not count.",
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = darkColor)
                ) {
                    // FIX: Changed button text to clearly indicate navigating back to the list
                    Text("Back to Recreations")
                }
            }
        }
    }
}