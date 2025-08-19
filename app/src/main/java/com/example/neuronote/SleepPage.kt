package com.example.neuronote

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

@Composable
fun SleepPage(darkGreen: Color, lightGreen: Color) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Weekly Sleep Tracker", style = MaterialTheme.typography.headlineSmall, color = darkGreen)

        SleepBarChart(referenceDay = LocalDate.now())

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add / Update Sleep", color = Color.White)
        }
    }

    if (showDialog) {
        AddSleepDialog(
            darkGreen = darkGreen,
            lightGreen = lightGreen,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun AddSleepDialog(darkGreen: Color, lightGreen: Color, onDismiss: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedHours by remember { mutableStateOf(8) }
    var expandedHours by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ),
                showModeToggle = false,
                title = { Text("Select Date") },
                headline = null
            ) { state ->
                val millis = state.selectedDateMillis
                if (millis != null) {
                    selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sleep Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
                ) {
                    Text("Date: ${selectedDate.format(DateTimeFormatter.ISO_DATE)}")
                }

                Box {
                    Button(
                        onClick = { expandedHours = true },
                        colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
                    ) {
                        Text("$selectedHours hrs")
                    }
                    DropdownMenu(expanded = expandedHours, onDismissRequest = { expandedHours = false }) {
                        (0..24).forEach { hr ->
                            DropdownMenuItem(
                                text = { Text("$hr hrs") },
                                onClick = {
                                    selectedHours = hr
                                    expandedHours = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                SleepDataManager.addSleepEntry(selectedDate, selectedHours)
                onDismiss()
            }) { Text("Save", color = darkGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun SleepBarChart(referenceDay: LocalDate) {
    val weekMap by remember { derivedStateOf { SleepDataManager.getHoursMapForWeek(referenceDay) } }

    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                setFitBars(true)
            }
        },
        update = { chart ->
            val days = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )
            val barEntries = days.mapIndexed { idx, d -> BarEntry(idx.toFloat(), (weekMap[d] ?: 0).toFloat()) }
            val ds = BarDataSet(barEntries, "Hours slept").apply {
                color = AndroidColor.rgb(56, 142, 60)
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 14f
            }
            chart.data = BarData(ds)
            val labels = days.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.granularity = 1f
            chart.xAxis.setLabelCount(7, true)
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = 12f // optional cap
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp) // 🔼 increased size
    )
}
