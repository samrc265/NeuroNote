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
import java.time.LocalDate

private val moodLabels = mapOf(
    1 to "Very Sad",
    2 to "Sad",
    3 to "Neutral",
    4 to "Happy",
    5 to "Very Happy"
)

@Composable
fun HomePage(darkColor: Color, lightColor: Color, textColor: Color, onUpdateMoodClick: () -> Unit) {
    var graphFilter by remember { mutableStateOf("Month") }
    var pieChartRange by remember { mutableStateOf("Today") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mood Over Time", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Month", "Year", "All Time").forEach { filter ->
                Button(
                    onClick = { graphFilter = filter },
                    colors = ButtonDefaults.buttonColors(containerColor = if (graphFilter == filter) darkColor else lightColor)
                ) { Text(filter, color = if (graphFilter == filter) Color.White else darkColor) }
            }
        }
        MoodLineChart(filter = graphFilter)
        Divider()
        Text("Emotional Distribution", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Today", "Last 7 Days").forEach { range ->
                Button(
                    onClick = { pieChartRange = range },
                    colors = ButtonDefaults.buttonColors(containerColor = if (pieChartRange == range) darkColor else lightColor)
                ) { Text(range, color = if (pieChartRange == range) Color.White else darkColor) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                MoodPieChart(range = pieChartRange, darkColor = darkColor, lightColor = lightColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("Mood Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                MoodTextBreakdown(range = pieChartRange, textColor = textColor)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onUpdateMoodClick, colors = ButtonDefaults.buttonColors(containerColor = darkColor),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Update Current Mood", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun MoodLineChart(filter: String) {
    val history by MoodDataManager.historyMoods
    val filtered = when (filter) {
        "Month" -> history.filter { it.date.isAfter(LocalDate.now().minusMonths(1)) }
        "Year" -> history.filter { it.date.isAfter(LocalDate.now().minusYears(1)) }
        else -> history
    }
    // FIX: Explicitly name the chart parameter in the update lambda to resolve scope issue
    AndroidView(factory = { ctx -> LineChart(ctx) }, update = { chartInstance ->
        val entries = filtered.map { Entry(it.date.dayOfYear.toFloat(), it.averageMood.toFloat()) }
        val ds = LineDataSet(entries, "Avg Mood").apply {
            color = AndroidColor.BLUE; valueTextColor = AndroidColor.BLACK; lineWidth = 2f
            setCircleColor(AndroidColor.BLUE); circleRadius = 4f
        }
        chartInstance.data = LineData(ds)
        chartInstance.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartInstance.axisLeft.apply {
            axisMinimum = 1f; axisMaximum = 5f; granularity = 1f; setLabelCount(5, true)
        }
        chartInstance.axisRight.isEnabled = false
        chartInstance.description.isEnabled = false; chartInstance.legend.isEnabled = false
        chartInstance.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(200.dp))
}

@Composable
fun MoodPieChart(range: String, darkColor: Color, lightColor: Color) {
    val dailyMoods by MoodDataManager.dailyMoods
    val historyMoods by MoodDataManager.historyMoods

    val moods: List<DailyMood> = if (range == "Today") {
        dailyMoods
    } else {
        historyMoods.takeLast(7).map { DailyMood(it.averageMood.toInt().coerceIn(1, 5), it.date) }
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
                val colors = entries.map {
                    when (it.label) {
                        "Very Sad" -> Color(0xFFE57373) // Light Red
                        "Sad" -> Color(0xFFF44336)    // Red
                        "Neutral" -> Color(0xFFFFCA28) // Yellow
                        "Happy" -> Color(0xFF66BB6A)   // Light Green
                        "Very Happy" -> Color(0xFF133D14)
                        else -> lightColor
                    }.hashCode()
                }
                val ds = PieDataSet(entries, "").apply {
                    setColors(colors)
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
fun MoodTextBreakdown(range: String, textColor: Color) {
    val dailyMoods by MoodDataManager.dailyMoods
    val historyMoods by MoodDataManager.historyMoods

    val moods = if (range == "Today") dailyMoods
    else historyMoods.takeLast(7).map { DailyMood(it.averageMood.toInt().coerceIn(1, 5), it.date) }

    val counts = moods.groupingBy { it.mood }.eachCount()
    val total = counts.values.sum().takeIf { it > 0 } ?: 1
    if (counts.isEmpty()) {
        Text("No data available", fontSize = 14.sp, color = textColor)
    } else {
        (1..5).forEach { m ->
            val c = counts[m] ?: 0
            if (c > 0) {
                val pct = (c.toFloat() / total) * 100
                Text("${moodLabels[m]}: ${"%.1f".format(pct)}%", fontSize = 14.sp, color = textColor)
            }
        }
    }
}