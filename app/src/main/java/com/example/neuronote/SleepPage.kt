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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun SleepPage(
    darkGreen: Color,
    lightGreen: Color
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sleep This Week", style = MaterialTheme.typography.headlineSmall, color = darkGreen)

        SleepBarChart()

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Sleep Hours", color = Color.White)
        }
    }

    if (showDialog) {
        AddSleepDialog(
            onDismiss = { showDialog = false },
            onSave = { hours ->
                SleepDataManager.addSleep(hours, LocalDate.now())
                showDialog = false
            }
        )
    }
}

@Composable
fun SleepBarChart() {
    val entries = SleepDataManager.getThisWeekEntries()

    AndroidView(factory = { ctx ->
        BarChart(ctx).apply {
            val days = DayOfWeek.values()
            val map = mutableMapOf<DayOfWeek, Int>()
            days.forEach { map[it] = 0 }
            entries.forEach { entry ->
                map[entry.date.dayOfWeek] = (map[entry.date.dayOfWeek] ?: 0) + entry.hours
            }

            val barEntries = days.mapIndexed { index, day ->
                BarEntry(index.toFloat(), (map[day] ?: 0).toFloat())
            }

            val dataSet = BarDataSet(barEntries, "Hours Slept").apply {
                color = AndroidColor.BLUE
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 12f
            }

            this.data = BarData(dataSet)
            this.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(days.map { it.name.take(3) })
                granularity = 1f
                setDrawGridLines(false)
            }
            this.axisLeft.axisMinimum = 0f
            this.axisRight.isEnabled = false
            this.description.isEnabled = false
            this.legend.isEnabled = false
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(250.dp))
}

@Composable
fun AddSleepDialog(onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var hoursInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sleep Hours") },
        text = {
            OutlinedTextField(
                value = hoursInput,
                onValueChange = { hoursInput = it },
                label = { Text("Hours") }
            )
        },
        confirmButton = {
            Button(onClick = {
                hoursInput.toIntOrNull()?.let { onSave(it) }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
