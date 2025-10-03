package com.example.neuronote

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapperGame(darkColor: Color, textColor: Color, onFinish: () -> Unit) {
    val targetTaps = 30
    var tapsRemaining by remember { mutableStateOf(targetTaps) }
    var gameActive by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    // Animatable for the pulsing and feedback effect on the circle
    val pulseScale = remember { Animatable(1f) }

    // Logic to run the visual pulse effect
    LaunchedEffect(Unit) {
        // Initial large pulse
        pulseScale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Continuous gentle pulse loop while active
        while (gameActive) {
            pulseScale.animateTo(1f, tween(300))
            pulseScale.animateTo(1.1f, tween(800))
        }
    }

    // Function to handle a user tap on the circle
    fun handleTap() {
        if (!gameActive) return

        tapsRemaining--
        scope.launch {
            // Quick feedback animation on tap
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
                    // Uses the onFinish callback to navigate back to the list
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

                // The large, tappable circle with animation
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(scaleX = pulseScale.value, scaleY = pulseScale.value)
                        .clip(CircleShape)
                        .background(darkColor)
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
                Spacer(Modifier.height(32.dp))
                Text(
                    "Focus on the rhythm and sensation of each tap.",
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )

            } else {
                // Game Finished Screen
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