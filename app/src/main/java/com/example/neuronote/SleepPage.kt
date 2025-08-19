@file:OptIn(ExperimentalMaterial3Api::class)

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

@ExperimentalMaterial3Api
@Composable
fun AddSleepDialog(darkGreen: Color, lightGreen: Color, onDismiss: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedHours by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val hoursOptions = (0..12).toList() // dropdown 0–12 hours

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

                // Dropdown for hours
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedHours?.toString() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hours Slept") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        hoursOptions.forEach { hr ->
                            DropdownMenuItem(
                                text = { Text("$hr hours") },
                                onClick = {
                                    selectedHours = hr
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedHours != null) {
                    SleepDataManager.addSleepEntry(selectedDate, selectedHours!!)
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
                val hours = weekMap[day] ?: 0
                // simulate blocky look: stack 1f for each hour
                val stack = FloatArray(hours) { 1f }
                BarEntry(idx.toFloat(), stack)
            }

            val dataSet = BarDataSet(entries, "Hours Slept").apply {
                setColors(
                    IntArray(12) { AndroidColor.rgb(56, 142, 60) }.toList()
                )
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 10f
                isHighlightEnabled = false
                setDrawValues(false) // no clutter
            }

            val data = BarData(dataSet).apply {
                barWidth = 0.8f
            }

            chart.data = data

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
