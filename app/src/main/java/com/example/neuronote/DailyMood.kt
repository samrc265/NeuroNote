package com.example.neuronote

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate

data class DailyMood(
    val mood: Int,
    val date: LocalDate,
    val note: String? = null
)

data class HistoryMood(
    val date: LocalDate,
    val averageMood: Double,
    val notes: List<String> = emptyList()
)

object MoodDataManager {
    // State-backed lists so UI recomposes automatically
    val dailyMoods = mutableStateListOf<DailyMood>()
    val historyMoods = mutableStateListOf<HistoryMood>()

    private var lastDate: LocalDate? = null

    fun addDailyMood(mood: Int, date: LocalDate, note: String? = null) {
        // If a new date arrives, archive previous day's moods
        if (lastDate != null && date.isAfter(lastDate)) {
            archiveCurrentDay()
            dailyMoods.clear()
        }
        dailyMoods.add(DailyMood(mood.coerceIn(1,5), date, note))
        lastDate = date
    }

    private fun archiveCurrentDay() {
        if (dailyMoods.isNotEmpty()) {
            val avg = dailyMoods.map { it.mood }.average()
            val notes = dailyMoods.mapNotNull { it.note }
            historyMoods.add(HistoryMood(dailyMoods.first().date, avg, notes))
        }
    }

    /**
     * Call this at app start (optional) or by a background worker at 00:00 to roll the day.
     * If today's daily entry exists and you want to force archive, pass true.
     */
    fun endOfDayArchive(force: Boolean = false) {
        if (force || (lastDate != null && LocalDate.now().isAfter(lastDate))) {
            archiveCurrentDay()
            dailyMoods.clear()
            lastDate = LocalDate.now()
        }
    }
}
