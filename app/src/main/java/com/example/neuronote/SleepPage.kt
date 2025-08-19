package com.example.neuronote

import android.app.DatePickerDialog
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
import java.util.Locale

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

        // increased size
        SleepBarChart(referenceDay = LocalDate.now(), chartHeightDp = 420.dp)

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

    val context = LocalContext.current

    // Restrict date selection to the current week (Monday..Sunday)
    val mondayOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sundayOfWeek = mondayOfWeek.plusDays(6)

    // helper to open Android DatePickerDialog
    fun showAndroidDatePicker() {
        val dpd = DatePickerDialog(
            context,
            { _, year, monthZeroBased, dayOfMonth ->
                // month is 0-based from Android DatePicker -> convert to LocalDate (1-based)
                selectedDate = LocalDate.of(year, monthZeroBased + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )

        // restrict selectable range to current week
        val zone = ZoneId.systemDefault()
        dpd.datePicker.minDate = mondayOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
        dpd.datePicker.maxDate = sundayOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()

        dpd.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sleep Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Date selector (opens Android DatePicker)
                Button(
                    onClick = { showAndroidDatePicker() },
                    colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
                ) {
                    Text("Date: ${selectedDate.format(DateTimeFormatter.ISO_DATE)}")
                }

                // Hours selector (dropdown)
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
                // Save to SleepDataManager (will update UI reactively)
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
fun SleepBarChart(referenceDay: LocalDate, chartHeightDp: Dp) {
    // derivedStateOf will recompute when SleepDataManager.sleepData (the state list) changes
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

            val barEntries = days.mapIndexed { idx, d ->
                BarEntry(idx.toFloat(), (weekMap[d] ?: 0).toFloat())
            }

            val ds = BarDataSet(barEntries, "Hours slept").apply {
                color = AndroidColor.rgb(56, 142, 60)
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 12f
            }

            chart.data = BarData(ds)

            // labels Mon Tue ...
            val labels = days.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.granularity = 1f
            chart.xAxis.setLabelCount(7, true)

            // set Y-axis max dynamically (at least 12)
            val maxHours = (weekMap.values.maxOrNull() ?: 12)
            val yMax = maxOf(12, maxHours + 2)
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = yMax.toFloat()

            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeightDp)
    )
}
