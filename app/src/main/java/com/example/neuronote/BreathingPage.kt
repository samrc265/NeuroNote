package com.example.neuronote

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Breathing Exercise page.
 * - Slow, soothing animation (Inhale 4s, Hold 4s, Exhale 6s)
 * - Exhale circle slightly smaller for visual calm
 * - Guided countdown overlay (moves with the circle)
 * - Texts never collide with the circle (reserved lane)
 * - Extra safety around animations/coroutines to prevent crashes
 */
@Composable
fun BreathingPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    val isDark = isSystemInDarkTheme()
    val pageBg = if (isDark) Color(0xFF151515) else Color.White
    val circleColor = darkColor

    // Phase lengths (seconds)
    val inhaleSec = 4
    val holdSec = 4
    val exhaleSec = 6

    // Circle geometry
    val baseSize = 180.dp
    val maxScale = 1.24f   // Hold peak (also reached at end of Inhale)
    val minScale = 0.75f   // Exhale smallest
    val circleLaneHeight = baseSize * maxScale + 40.dp // reserved lane (prevents collision)

    // State
    var selectedMinutes by rememberSaveable { mutableIntStateOf(1) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var stage by rememberSaveable { mutableStateOf("Ready") } // Ready/Inhale/Hold/Exhale
    var motivationalText by rememberSaveable {
        mutableStateOf("Find a comfortable seat and relax your shoulders.")
    }
    var phaseCountdown by rememberSaveable { mutableIntStateOf(0) }

    // Animatable with guard
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            // Safely return to base if composable leaves composition
            scope.launch {
                runCatching { scale.animateTo(1f, tween(300)) }
            }
        }
    }

    fun setStage(newStage: String) {
        stage = newStage
        when (newStage) {
            "Inhale" -> {
                motivationalText = "Breathe in softly… fill the belly."
                phaseCountdown = inhaleSec
                scope.launch {
                    // If job is cancelled while animating, swallow the exception
                    runCatching { scale.animateTo(1.22f, tween(inhaleSec * 1000)) }
                }
            }
            "Hold" -> {
                motivationalText = "Hold gently… stay calm."
                phaseCountdown = holdSec
                scope.launch {
                    runCatching { scale.animateTo(maxScale, tween(holdSec * 1000)) }
                }
            }
            "Exhale" -> {
                motivationalText = "Slowly breathe out… let go 🌙"
                phaseCountdown = exhaleSec
                scope.launch {
                    runCatching { scale.animateTo(minScale, tween(exhaleSec * 1000)) }
                }
            }
            else -> {
                phaseCountdown = 0
                scope.launch {
                    runCatching { scale.animateTo(1f, tween(300)) }
                }
            }
        }
    }

    var runner: Job? by remember { mutableStateOf(null) }

    fun stopSession() {
        runner?.cancel()
        runner = null
        isRunning = false
        setStage("Ready")
        motivationalText = "Find a comfortable seat and relax your shoulders."
    }

    fun startSession() {
        if (isRunning) return
        isRunning = true

        val totalSeconds = (selectedMinutes * 60).coerceAtLeast(1) // guard

        runner = scope.launch {
            var elapsed = 0

            try {
                while (isActive && elapsed < totalSeconds) {
                    // INHALE
                    setStage("Inhale")
                    repeat(inhaleSec) {
                        if (!isActive) return@launch
                        delay(1000)
                        phaseCountdown = (phaseCountdown - 1).coerceAtLeast(0)
                        elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }

                    // HOLD
                    setStage("Hold")
                    repeat(holdSec) {
                        if (!isActive) return@launch
                        delay(1000)
                        phaseCountdown = (phaseCountdown - 1).coerceAtLeast(0)
                        elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }

                    // EXHALE
                    setStage("Exhale")
                    repeat(exhaleSec) {
                        if (!isActive) return@launch
                        delay(1000)
                        phaseCountdown = (phaseCountdown - 1).coerceAtLeast(0)
                        elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }
                }
            } finally {
                // Ensure UI resets gracefully whether finished or cancelled
                isRunning = false
                stage = "Ready"
                phaseCountdown = 0
                motivationalText = "Session complete ✨ Great job staying present."
                runCatching { scale.animateTo(1f, tween(400)) }
            }
        }
    }

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Breathing Exercise",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Fixed-height lane for the circle so texts never collide with it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(circleLaneHeight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Circle
                Box(
                    modifier = Modifier
                        .size(baseSize)
                        .background(circleColor, CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.2f)
                            else Color.Black.copy(alpha = 0.08f),
                            shape = CircleShape
                        )
                )

                // Overlay cue + countdown (moves with circle)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (stage) {
                            "Inhale" -> "Inhale"
                            "Hold" -> "Hold"
                            "Exhale" -> "Exhale"
                            else -> "Ready"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp),
                        textAlign = TextAlign.Center
                    )
                    if (isRunning && phaseCountdown > 0) {
                        Text(
                            text = phaseCountdown.toString(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Motivational text (outside the lane; never overlaps)
        Text(
            text = motivationalText,
            color = textColor.copy(alpha = if (isDark) 0.9f else 0.85f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Composable
            fun preset(min: Int, label: String) {
                OutlinedButton(
                    onClick = { if (!isRunning) selectedMinutes = min },
                    enabled = !isRunning,
                    modifier = Modifier
                        .width(96.dp)
                        .height(44.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedMinutes == min) darkColor else textColor.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        label,
                        color = if (selectedMinutes == min) darkColor else textColor
                    )
                }
            }
            preset(1, "1 min")
            preset(3, "3 min")
            preset(5, "5 min")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (isRunning) stopSession() else startSession() },
            colors = ButtonDefaults.buttonColors(
                containerColor = darkColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .width(140.dp)
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(if (isRunning) "Stop" else "Start")
        }

        if (!isRunning) {
            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = lightColor),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
            ) {
                Text(
                    text = "Tip: Breathe through your nose, keep your shoulders relaxed, and let the belly rise and fall.",
                    color = textColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
