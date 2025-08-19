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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Weekly Sleep Tracker",
            style = MaterialTheme.typography.headlineSmall,
            color = darkGreen
        )

        // Larger chart height for readability
        SleepBarChart(referenceDay = LocalDate.now(), chartHeightDp = 440.dp)

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
    var selectedHours by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Limit selection to this week
    val mondayOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sundayOfWeek = mondayOfWeek.plusDays(6)

    fun showAndroidDatePicker() {
        val dpd = DatePickerDialog(
            context,
            { _, year, monthZeroBased, dayOfMonth ->
                selectedDate = LocalDate.of(year, monthZeroBased + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        val zone = ZoneId.systemDefault()
        dpd.datePicker.minDate = mondayOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
        dpd.datePicker.maxDate = sundayOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
        dpd.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Sleep Entry", style = MaterialTheme.typography.titleLarge, color = darkGreen)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date selector
                OutlinedButton(
                    onClick = { showAndroidDatePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${selectedDate.format(DateTimeFormatter.ISO_DATE)}")
                }

                // Hours input
                OutlinedTextField(
                    value = selectedHours,
                    onValueChange = { if (it.all { c -> c.isDigit() }) selectedHours = it },
                    label = { Text("Hours Slept") },
                    placeholder = { Text("e.g. 7") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val hours = selectedHours.toIntOrNull()
                if (hours != null && hours in 0..24) {
                    SleepDataManager.addSleepEntry(selectedDate, hours)
                    onDismiss()
                }
            }) {
                Text("Save", color = darkGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun SleepBarChart(referenceDay: LocalDate, chartHeightDp: Dp) {
    val weekMap by remember { derivedStateOf { SleepDataManager.getHoursMapForWeek(referenceDay) } }

    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                setFitBars(true)
            }
        },
        update = { chart ->
            val days = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )

            val entries = days.mapIndexed { idx, day ->
                BarEntry(idx.toFloat(), (weekMap[day] ?: 0).toFloat())
            }

            val dataSet = BarDataSet(entries, "Hours Slept").apply {
                color = AndroidColor.rgb(56, 142, 60)
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 12f
                barShadowColor = AndroidColor.LTGRAY
                setDrawValues(true)
            }

            val data = BarData(dataSet).apply {
                barWidth = 0.9f // wider bars, fewer gaps
            }

            chart.data = data

            // Always show 7 day labels
            val labels = days.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.setLabelCount(7, true)

            val maxHours = (weekMap.values.maxOrNull() ?: 8)
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = (maxHours + 2).toFloat()

            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeightDp)
    )
}
