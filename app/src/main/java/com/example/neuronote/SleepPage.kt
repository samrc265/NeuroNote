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
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun SleepPage(darkGreen: Color, lightGreen: Color) {
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    var expandedDay by remember { mutableStateOf(false) }

    var selectedHours by remember { mutableStateOf(8) }
    var expandedHours by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Weekly Sleep Tracker", style = MaterialTheme.typography.headlineSmall, color = darkGreen)

        SleepBarChart(referenceDay = LocalDate.now())

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Box {
                Button(onClick = { expandedDay = true }, colors = ButtonDefaults.buttonColors(containerColor = lightGreen)) {
                    Text(selectedDay.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()))
                }
                DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                    daysOfWeek.forEach { day ->
                        DropdownMenuItem(text = { Text(day.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())) }, onClick = {
                            selectedDay = day; expandedDay = false
                        })
                    }
                }
            }

            Box {
                Button(onClick = { expandedHours = true }, colors = ButtonDefaults.buttonColors(containerColor = lightGreen)) {
                    Text("$selectedHours hrs")
                }
                DropdownMenu(expanded = expandedHours, onDismissRequest = { expandedHours = false }) {
                    (0..24).forEach { hr ->
                        DropdownMenuItem(text = { Text("$hr hrs") }, onClick = { selectedHours = hr; expandedHours = false })
                    }
                }
            }
        }

        Button(onClick = {
            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val entryDate = monday.plusDays((selectedDay.value - 1).toLong())
            SleepDataManager.addSleepEntry(entryDate, selectedHours)
        }, colors = ButtonDefaults.buttonColors(containerColor = darkGreen), modifier = Modifier.fillMaxWidth()) {
            Text("Add / Update Sleep", color = Color.White)
        }
    }
}

@Composable
fun SleepBarChart(referenceDay: LocalDate) {
    val weekMap by remember { derivedStateOf { SleepDataManager.getHoursMapForWeek(referenceDay) } }
    AndroidView(factory = { ctx ->
        BarChart(ctx).apply {
            description.isEnabled = false; legend.isEnabled = false; axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM; xAxis.setDrawGridLines(false); setFitBars(true)
        }
    }, update = { chart ->
        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val barEntries = days.mapIndexed { idx, d -> BarEntry(idx.toFloat(), (weekMap[d] ?: 0).toFloat()) }
        val ds = BarDataSet(barEntries, "Hours slept").apply { color = AndroidColor.rgb(56,142,60); valueTextColor = AndroidColor.BLACK; valueTextSize = 12f }
        chart.data = BarData(ds)
        val labels = days.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.setLabelCount(7, true)
        chart.axisLeft.axisMinimum = 0f
        chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(260.dp))
}
