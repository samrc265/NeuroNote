package com.example.neuronote

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate

private val moodLabels = mapOf(
    1 to "Very Sad",
    2 to "Sad",
    3 to "Neutral",
    4 to "Happy",
    5 to "Very Happy"
)

@Composable
fun HomePage(darkGreen: Color, lightGreen: Color, onUpdateMoodClick: () -> Unit) {
    var graphFilter by remember { mutableStateOf("Month") }
    var pieChartRange by remember { mutableStateOf("Today") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mood Over Time", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = darkGreen)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Month", "Year", "All Time").forEach { filter ->
                Button(
                    onClick = { graphFilter = filter },
                    colors = ButtonDefaults.buttonColors(containerColor = if (graphFilter == filter) darkGreen else lightGreen)
                ) { Text(filter, color = if (graphFilter == filter) Color.White else darkGreen) }
            }
        }
        MoodLineChart(filter = graphFilter)

        Divider()

        Text("Emotional Distribution", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = darkGreen)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Today", "Last 7 Days").forEach { range ->
                Button(
                    onClick = { pieChartRange = range },
                    colors = ButtonDefaults.buttonColors(containerColor = if (pieChartRange == range) darkGreen else lightGreen)
                ) { Text(range, color = if (pieChartRange == range) Color.White else darkGreen) }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MoodPieChart(range = pieChartRange)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("Mood Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                MoodTextBreakdown(range = pieChartRange)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onUpdateMoodClick, colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Update Current Mood", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun MoodLineChart(filter: String) {
    // observe historyMoods (state list)
    val history = MoodDataManager.historyMoods
    val filtered = when (filter) {
        "Month" -> history.filter { it.date.isAfter(LocalDate.now().minusMonths(1)) }
        "Year" -> history.filter { it.date.isAfter(LocalDate.now().minusYears(1)) }
        else -> history
    }

    AndroidView(factory = { ctx -> LineChart(ctx) }, update = { chart ->
        val entries = filtered.map { Entry(it.date.dayOfYear.toFloat(), it.averageMood.toFloat()) }
        val ds = LineDataSet(entries, "Avg Mood").apply {
            color = AndroidColor.BLUE; valueTextColor = AndroidColor.BLACK; lineWidth = 2f
            setCircleColor(AndroidColor.BLUE); circleRadius = 4f
        }
        chart.data = LineData(ds)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisLeft.apply {
            axisMinimum = 1f; axisMaximum = 5f; granularity = 1f; setLabelCount(5, true)
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(200.dp))
}

@Composable
fun MoodPieChart(range: String) {
    val moods: List<DailyMood> = if (range == "Today") {
        MoodDataManager.dailyMoods
    } else {
        // last 7 days, expand history to daily-like entries (approx)
        MoodDataManager.historyMoods.takeLast(7).map { DailyMood(it.averageMood.toInt().coerceIn(1,5), it.date) }
    }

    if (moods.isEmpty()) {
        AndroidView(factory = { ctx ->
            PieChart(ctx).apply {
                val ds = PieDataSet(listOf(PieEntry(1f, "No Data")), "")
                ds.colors = listOf(AndroidColor.LTGRAY)
                data = PieData(ds)
                description.isEnabled = false; legend.isEnabled = false
                invalidate()
            }
        }, modifier = Modifier.fillMaxWidth().height(180.dp))
    } else {
        AndroidView(factory = { ctx ->
            PieChart(ctx).apply {
                val counts = moods.groupingBy { it.mood }.eachCount()
                val entries = counts.map { PieEntry(it.value.toFloat(), moodLabels[it.key]) }
                val ds = PieDataSet(entries, "").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextColor = AndroidColor.BLACK; valueTextSize = 14f
                }
                data = PieData(ds)
                description.isEnabled = false; legend.isEnabled = false
                invalidate()
            }
        }, modifier = Modifier.fillMaxWidth().height(180.dp))
    }
}

@Composable
fun MoodTextBreakdown(range: String) {
    val moods = if (range == "Today") MoodDataManager.dailyMoods
    else MoodDataManager.historyMoods.takeLast(7).map { DailyMood(it.averageMood.toInt().coerceIn(1,5), it.date) }

    val counts = moods.groupingBy { it.mood }.eachCount()
    val total = counts.values.sum().takeIf { it > 0 } ?: 1

    if (counts.isEmpty()) {
        Text("No data available", fontSize = 14.sp)
    } else {
        (1..5).forEach { m ->
            val c = counts[m] ?: 0
            if (c > 0) {
                val pct = (c.toFloat() / total) * 100
                Text("${moodLabels[m]}: ${"%.1f".format(pct)}%", fontSize = 14.sp)
            }
        }
    }
}
