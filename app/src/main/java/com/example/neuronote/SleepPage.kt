package com.example.neuronote

import android.app.DatePickerDialog
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun SleepPage(darkColor: Color, lightColor: Color, textColor: Color) {
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
            color = textColor
        )
        SleepBarChart(darkColor = darkColor, lightColor = lightColor, referenceDay = LocalDate.now(), chartHeightDp = 440.dp, textColor = textColor)
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = darkColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add / Update Sleep", color = Color.White)
        }
    }

    if (showDialog) {
        AddSleepDialog(
            darkColor = darkColor,
            lightColor = lightColor,
            textColor = textColor,
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepDialog(darkColor: Color, lightColor: Color, textColor: Color, onDismiss: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedHours by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val hoursOptions = (0..12).toList()
    val mondayOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sundayOfWeek = mondayOfWeek.plusDays(6)
    val scope = rememberCoroutineScope()

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
        containerColor = lightColor,
        title = {
            Text("Add Sleep Entry", style = MaterialTheme.typography.titleLarge, color = textColor)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showAndroidDatePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${selectedDate.format(DateTimeFormatter.ISO_DATE)}", color = textColor)
                }

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
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = darkColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            focusedLabelColor = darkColor,
                            unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                            cursorColor = darkColor
                        )
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
                    scope.launch {
                        SleepDataManager.addSleepEntry(selectedDate, selectedHours!!)
                        onDismiss()
                    }
                }
            }) {
                Text("Save", color = darkColor)
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
fun SleepBarChart(darkColor: Color, lightColor: Color, referenceDay: LocalDate, chartHeightDp: Dp, textColor: Color) {
    val weekMap by remember { derivedStateOf { SleepDataManager.getHoursMapForWeek(referenceDay) } }
    // FIX: Determine today's DayOfWeek for highlighting
    val today = LocalDate.now().dayOfWeek

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
            val days = DayOfWeek.values().toList()
            val entries = days.mapIndexed { idx, day ->
                BarEntry(idx.toFloat(), weekMap[day]?.toFloat() ?: 0f)
            }

            // FIX: Create a list of colors, highlighting the current day with a contrasting light color
            val barColors = days.map { day ->
                if (day == today) {
                    lightColor.toArgb() // Use the card/light background color for emphasis
                } else {
                    darkColor.toArgb()
                }
            }

            val dataSet = BarDataSet(entries, "Hours Slept").apply {
                // FIX: Set colors using the barColors list
                setColors(barColors)
                valueTextColor = textColor.toArgb()
                valueTextSize = 10f
                isHighlightEnabled = false
            }
            val data = BarData(dataSet).apply {
                barWidth = 0.8f
            }
            chart.data = data
            chart.xAxis.textColor = textColor.toArgb()
            chart.axisLeft.textColor = textColor.toArgb()
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