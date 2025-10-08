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
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun SleepPage(darkColor: Color, lightColor: Color, textColor: Color) {
    var showDialog by remember { mutableStateOf(false) }

    // Observe all sleep data so the UI recomposes whenever entries change
    val allSleep by SleepDataManager.sleepData

    val today = LocalDate.now()
    val mondayOfWeek = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val sundayOfWeek = remember(mondayOfWeek) { mondayOfWeek.plusDays(6) }

    // Filter snapshot for the current week (reacts to allSleep changes)
    val weekEntries = remember(allSleep, today) {
        allSleep.filter { !it.date.isBefore(mondayOfWeek) && !it.date.isAfter(sundayOfWeek) }
    }
    val totalHours = remember(weekEntries) { weekEntries.sumOf { it.hours } }

    // Days elapsed (Mon..today inclusive)
    val daysElapsed = remember(today) {
        val end = if (today.isAfter(sundayOfWeek)) sundayOfWeek else today
        (ChronoUnit.DAYS.between(mondayOfWeek, end).toInt() + 1).coerceIn(1, 7)
    }

    // Build feedback message & color
    val (feedbackText, feedbackTint) = remember(totalHours, daysElapsed) {
        buildWeeklySleepFeedback(totalHours, daysElapsed)
    }

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

        SleepBarChart(
            darkColor = darkColor,
            lightColor = lightColor,
            referenceDay = today,
            chartHeightDp = 440.dp,
            textColor = textColor,
            isDarkMode = AppThemeManager.isDarkTheme.value,
            sleepDataSnapshot = allSleep
        )

        // Weekly feedback card
        Card(
            colors = CardDefaults.cardColors(containerColor = lightColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "This Week So Far",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = feedbackText,
                    style = MaterialTheme.typography.bodyMedium,   // ✅ fixed typo
                    color = feedbackTint
                )
            }
        }

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
                        // No manual refresh needed; the page observes SleepDataManager.sleepData.
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
fun SleepBarChart(
    darkColor: Color,
    lightColor: Color,
    referenceDay: LocalDate,
    chartHeightDp: Dp,
    textColor: Color,
    isDarkMode: Boolean,
    sleepDataSnapshot: List<SleepEntry>
) {
    // Recompute the map whenever the snapshot changes
    val weekMap = remember(sleepDataSnapshot, referenceDay) {
        computeWeekMapFromSnapshot(sleepDataSnapshot, referenceDay)
    }

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
                BarEntry(idx.toFloat(), (weekMap[day] ?: 0).toFloat())
            }

            // Light: white bars; Dark: accent bars
            val barColors = if (!isDarkMode) {
                List(days.size) { AndroidColor.WHITE }
            } else {
                List(days.size) { darkColor.toArgb() }
            }

            val dataSet = BarDataSet(entries, "Hours Slept").apply {
                setColors(barColors)
                valueTextColor = textColor.toArgb()
                valueTextSize = 10f
                isHighlightEnabled = false
            }
            val data = BarData(dataSet).apply { barWidth = 0.8f }
            chart.data = data

            chart.xAxis.textColor = textColor.toArgb()
            chart.axisLeft.textColor = textColor.toArgb()
            val labels = days.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.setLabelCount(7, true)

            // ✅ Safe bounds so min < max even if all values are 0
            val maxHours = (weekMap.values.maxOrNull() ?: 0)
            val axisMax = (maxHours + 2).coerceAtLeast(6).toFloat()
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = axisMax

            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeightDp)
    )
}

/** Build (recommended) feedback for adults: 7–9 h/night scaled to days elapsed. */
private fun buildWeeklySleepFeedback(totalHours: Int, daysElapsed: Int): Pair<String, Color> {
    val safeDays = daysElapsed.coerceIn(1, 7)
    val minTarget = 7 * safeDays
    val maxTarget = 9 * safeDays

    val message = when {
        totalHours < minTarget -> {
            "You’ve slept ${totalHours}h over $safeDays day${if (safeDays == 1) "" else "s"}. " +
                    "That’s below the recommended ${minTarget}–${maxTarget}h for this point in the week. " +
                    "Try to catch a bit more rest tonight."
        }
        totalHours > maxTarget -> {
            "You’re at ${totalHours}h over $safeDays day${if (safeDays == 1) "" else "s"}, " +
                    "which is above the recommended ${minTarget}–${maxTarget}h. " +
                    "If you still feel tired, it’s okay to keep the extra rest—otherwise aim for about 7–9h per night."
        }
        else -> {
            "Nice work—${totalHours}h over $safeDays day${if (safeDays == 1) "" else "s"} " +
                    "is right on track (recommended ${minTarget}–${maxTarget}h). Keep it up!"
        }
    }

    val tint = when {
        totalHours < minTarget -> Color(0xFFD32F2F) // red-ish
        totalHours > maxTarget -> Color(0xFFF57C00) // amber-ish
        else -> Color(0xFF2E7D32) // green-ish
    }
    return message to tint
}

/** Compute the week map from a snapshot so Compose observes changes. */
private fun computeWeekMapFromSnapshot(
    snapshot: List<SleepEntry>,
    referenceDay: LocalDate
): Map<DayOfWeek, Int> {
    val monday = referenceDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)
    val week = snapshot.filter { !it.date.isBefore(monday) && !it.date.isAfter(sunday) }

    val base = DayOfWeek.values().associateWith { 0 }.toMutableMap()
    week.forEach { entry ->
        base[entry.date.dayOfWeek] = entry.hours
    }
    return base
}
