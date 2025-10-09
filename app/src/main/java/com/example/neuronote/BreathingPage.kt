package com.example.neuronote

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun BreathingPage(
    darkColor: Color,   // theme primary/accent (use in dark mode body)
    lightColor: Color,  // card color (unchanged)
    textColor: Color    // normal text color for light mode
) {
    // Use the app's own dark-mode toggle so colors follow your switch
    val isDark by AppThemeManager.isDarkTheme

    // Page background stays as you liked earlier
    val pageBg = if (isDark) Color(0xFF101214) else Color.White
    val circleColor = darkColor

    // Colors
    val headingColor = if (isDark) darkColor else textColor
    val affirmationColor = if (isDark) darkColor else textColor.copy(alpha = 0.9f)

    // Smooth, soothing timing
    val inhaleSec = 4
    val holdSec = 4
    val exhaleSec = 6

    // Keep text clear of the circle area
    val baseSize = 180.dp
    val maxScale = 1.24f
    val minScale = 0.75f
    val circleLaneHeight = baseSize * maxScale + 40.dp

    var selectedMinutes by rememberSaveable { mutableIntStateOf(1) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var stage by rememberSaveable { mutableStateOf("Ready") }
    var motivationalText by rememberSaveable {
        mutableStateOf("Find a comfortable seat and relax your shoulders.")
    }
    var phaseCountdown by rememberSaveable { mutableIntStateOf(0) }

    // 🔁 Rotating motivational messages per phase
    val inhaleMsgs = remember {
        listOf(
            "Breathe in softly… fill the belly.",
            "Slow, deep inhale… let the chest rise.",
            "Inhale gently through the nose.",
            "Draw in calm with every breath."
        )
    }
    val holdMsgs = remember {
        listOf(
            "Hold gently… stay calm.",
            "Softly hold… feel the stillness.",
            "Pause here… steady and relaxed.",
            "Quiet pause… you’re doing great."
        )
    }
    val exhaleMsgs = remember {
        listOf(
            "Slowly breathe out… let go 🌙",
            "Exhale… release any tension.",
            "Long exhale… soften the shoulders.",
            "Breathe out… relax the jaw."
        )
    }
    var inhaleIdx by rememberSaveable { mutableIntStateOf(0) }
    var holdIdx by rememberSaveable { mutableIntStateOf(0) }
    var exhaleIdx by rememberSaveable { mutableIntStateOf(0) }

    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose { scope.launch { runCatching { scale.animateTo(1f, tween(300)) } } }
    }

    fun setStage(newStage: String) {
        stage = newStage
        when (newStage) {
            "Inhale" -> {
                motivationalText = inhaleMsgs[inhaleIdx % inhaleMsgs.size]
                inhaleIdx++
                phaseCountdown = inhaleSec
                scope.launch { runCatching { scale.animateTo(1.22f, tween(inhaleSec * 1000)) } }
            }
            "Hold" -> {
                motivationalText = holdMsgs[holdIdx % holdMsgs.size]
                holdIdx++
                phaseCountdown = holdSec
                scope.launch { runCatching { scale.animateTo(maxScale, tween(holdSec * 1000)) } }
            }
            "Exhale" -> {
                motivationalText = exhaleMsgs[exhaleIdx % exhaleMsgs.size]
                exhaleIdx++
                phaseCountdown = exhaleSec
                scope.launch { runCatching { scale.animateTo(minScale, tween(exhaleSec * 1000)) } }
            }
            else -> {
                phaseCountdown = 0
                scope.launch { runCatching { scale.animateTo(1f, tween(300)) } }
            }
        }
    }

    var runner: Job? by remember { mutableStateOf(null) }

    fun stopSession() {
        runner?.cancel(); runner = null
        isRunning = false
        setStage("Ready")
        motivationalText = "Find a comfortable seat and relax your shoulders."
    }

    fun startSession() {
        if (isRunning) return
        isRunning = true
        val totalSeconds = (selectedMinutes * 60).coerceAtLeast(1)

        runner = scope.launch {
            var elapsed = 0
            try {
                while (isActive && elapsed < totalSeconds) {
                    setStage("Inhale")
                    repeat(inhaleSec) {
                        if (!isActive) return@launch
                        delay(1000); phaseCountdown--; elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }
                    setStage("Hold")
                    repeat(holdSec) {
                        if (!isActive) return@launch
                        delay(1000); phaseCountdown--; elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }
                    setStage("Exhale")
                    repeat(exhaleSec) {
                        if (!isActive) return@launch
                        delay(1000); phaseCountdown--; elapsed++
                        if (elapsed >= totalSeconds) return@launch
                    }
                }
            } finally {
                isRunning = false
                stage = "Ready"
                phaseCountdown = 0
                motivationalText = "Session complete ✨ Great job staying present."
                runCatching { scale.animateTo(1f, tween(400)) }
            }
        }
    }

    // --------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        // Heading
        Text(
            text = "Breathing Exercise",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp, fontWeight = FontWeight.SemiBold
            ),
            color = headingColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Circle lane
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
                            color = if (isDark) Color.White.copy(alpha = 0.18f)
                            else Color.Black.copy(alpha = 0.08f),
                            shape = CircleShape
                        )
                )
                // Label + countdown (on circle, keep white)
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

        // Rotating affirmation
        Text(
            text = motivationalText,
            color = affirmationColor,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Preset buttons (selected = bold text + thicker border)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Composable
            fun preset(min: Int, label: String) {
                val contentCol =
                    (if (isDark) darkColor else textColor)
                        .copy(alpha = if (isRunning) 0.65f else 1f)
                val borderCol =
                    (if (isDark) darkColor else textColor.copy(alpha = 0.55f))
                        .copy(alpha = if (isRunning) 0.65f else 1f)

                OutlinedButton(
                    onClick = { if (!isRunning) selectedMinutes = min },
                    enabled = true,
                    modifier = Modifier
                        .width(96.dp)
                        .height(44.dp),
                    border = BorderStroke(
                        width = if (selectedMinutes == min) 2.dp else 1.dp,
                        color = if (selectedMinutes == min) darkColor else borderCol
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = contentCol
                    )
                ) {
                    Text(
                        text = label,
                        color = if (selectedMinutes == min) darkColor else contentCol,
                        fontWeight = if (selectedMinutes == min) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            preset(1, "1 min")
            preset(3, "3 min")
            preset(5, "5 min")
        }

        Spacer(Modifier.height(24.dp))

        // Start/Stop button
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
        ) { Text(if (isRunning) "Stop" else "Start") }
    }
}
