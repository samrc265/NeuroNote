package com.example.neuronote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neuronote.data.MoodDataManager
import com.example.neuronote.data.SleepDataManager
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.catch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
        firstVisibleMonth = currentMonth
    )

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            "Mood & Sleep Calendar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCell(
                    date = day.date,
                    isSelected = day.date == selectedDate,
                    onClick = { selectedDate = day.date },
                    darkColor = darkColor,
                    lightColor = lightColor,
                    textColor = textColor
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        DayDetailCard(
            date = selectedDate,
            darkColor = darkColor,
            lightColor = lightColor,
            textColor = textColor
        )
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    val bg = if (isSelected) darkColor else lightColor
    val fg = if (isSelected) Color.White else textColor

    // use clickable with explicit interaction source (safe inside composable)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(2.dp)
            .background(bg, RoundedCornerShape(6.dp))
            .wrapContentSize(Alignment.Center)
            .clickable(indication = null, interactionSource = interactionSource) { onClick() }
    ) {
        Text(text = date.dayOfMonth.toString(), color = fg)
    }
}

@Composable
fun DayDetailCard(
    date: LocalDate,
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    var sleepHours by remember { mutableStateOf<Int?>(null) }
    var moodAvg by remember { mutableStateOf<Double?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Collect Sleep data (Flow<SleepEntity?> -> we only need hours)
    LaunchedEffect(date) {
        loadError = null
        try {
            SleepDataManager.getSleepByDate(date)
                .catch { e -> loadError = "Sleep load error: ${e.message}" }
                .collectLatest { sleepEntity ->
                    // safe assignment; if DB returns null, set null
                    sleepHours = try {
                        sleepEntity?.hours
                    } catch (_: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            loadError = "Sleep collection failed: ${e.message}"
            sleepHours = null
        }
    }

    // Collect Mood average data (Flow<Double?>)
    LaunchedEffect(date) {
        loadError = loadError // preserve previous error if any
        try {
            MoodDataManager.getAverageMoodFlow(date)
                .catch { e -> loadError = "Mood load error: ${e.message}" }
                .collectLatest { avg ->
                    moodAvg = try {
                        avg
                    } catch (_: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            loadError = (loadError?.plus("\n") ?: "") + "Mood collection failed: ${e.message}"
            moodAvg = null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Details for $date", fontWeight = FontWeight.Bold, color = textColor)

            Text("🛏️ Sleep Hours: ${sleepHours?.toString() ?: "No data"}", color = textColor)

            val moodEmoji = when (moodAvg?.toInt() ?: 0) {
                1 -> "😢"
                2 -> "☹️"
                3 -> "😐"
                4 -> "🙂"
                5 -> "😁"
                else -> "—"
            }
            Text(
                "😊 Average Mood: ${moodAvg?.let { String.format("%.2f", it) } ?: "No data"} $moodEmoji",
                color = textColor
            )

            if (!loadError.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("⚠️ ${loadError}", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
