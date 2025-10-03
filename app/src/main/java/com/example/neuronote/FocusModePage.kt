package com.example.neuronote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

@Composable
fun FocusModePage(
    darkColor: Color,   // your app’s primary/accent color
    lightColor: Color,  // your app’s card/container color
    textColor: Color,   // your app’s text color based on theme
    vm: FocusModeViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with date & live time
        Card(
            colors = CardDefaults.cardColors(containerColor = lightColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = java.time.LocalDate.now().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = state.nowTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a")),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = if (state.isFocusing) "Focus Mode is ON" else "Focus Mode is OFF",
                    color = if (state.isFocusing) Color(0xFF2E7D32) else textColor.copy(alpha = 0.7f)
                )
            }
        }

        // Preset timers
        Text("Presets", color = textColor, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(15, 25, 45, 60).forEach { minutes ->
                val selected = state.selectedPresetMin == minutes
                OutlinedButton(
                    onClick = { vm.setPreset(minutes) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selected) Color.White else darkColor
                    ),
                    border = if (selected)
                        ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                        )
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) darkColor else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${minutes}m")
                    }
                }
            }
        }

        // Custom timer (Slider now themed with your colors)
        Card(
            colors = CardDefaults.cardColors(containerColor = lightColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Custom (minutes): ${state.customMin}", color = textColor)
                Slider(
                    value = state.customMin.toFloat(),
                    onValueChange = { vm.setCustom(it.toInt()) },
                    valueRange = 5f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = darkColor,
                        activeTrackColor = darkColor,
                        inactiveTrackColor = textColor.copy(alpha = 0.2f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                Text(
                    "Tip: choose a time that matches your task scope.",
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }

        // Big Focus Button (highlight when ON)
        Button(
            onClick = {
                if (state.isFocusing) {
                    vm.stopFocus(context)
                } else {
                    vm.startWithPreset(context)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (state.isFocusing) 3.dp else 0.dp,
                    color = if (state.isFocusing) darkColor else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = darkColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = if (state.isFocusing) Icons.Filled.NotificationsOff else Icons.Filled.Timer,
                    contentDescription = null
                )
                Text(if (state.isFocusing) "Stop Focus" else "Start Focus (${state.selectedPresetMin}m)")
            }
        }

        // Secondary row: Start with Custom, Reset, DND Settings
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { if (!state.isFocusing) vm.startWithCustom(context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = darkColor)
            ) {
                Text("Start Custom")
            }

            OutlinedButton(
                onClick = { vm.reset(context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = darkColor)
            ) {
                Icon(Icons.Filled.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Reset")
            }

            IconButton(onClick = { vm.openDndSettings(context) }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "DND Settings",
                    tint = darkColor
                )
            }
        }

        // Remaining time visual
        AnimatedVisibility(visible = state.isFocusing) {
            Card(
                colors = CardDefaults.cardColors(containerColor = lightColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Time Remaining", color = textColor)
                    Text(
                        formatHms(state.remainingSec),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        "Phone set to silent (DND) during focus.",
                        color = textColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun formatHms(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
