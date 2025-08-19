package com.example.neuronote

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate

data class DailyMood(val mood: Int, val date: LocalDate, val note: String? = null)
data class HistoryMood(val date: LocalDate, val averageMood: Double, val notes: List<String> = emptyList())

object MoodDataManager {
    // State-backed lists -> UI recomposes automatically
    val dailyMoods = mutableStateListOf<DailyMood>()
    val historyMoods = mutableStateListOf<HistoryMood>()

    private var lastSavedDate: LocalDate? = null

    fun addDailyMood(mood: Int, date: LocalDate = LocalDate.now(), note: String? = null) {
        // If date advanced, archive previous day's dailyMoods
        if (lastSavedDate != null && date.isAfter(lastSavedDate)) {
            archiveDailyMoods()
            dailyMoods.clear()
        }
        dailyMoods.add(DailyMood(mood.coerceIn(1, 5), date, note))
        lastSavedDate = date
    }

    private fun archiveDailyMoods() {
        if (dailyMoods.isNotEmpty()) {
            val avg = dailyMoods.map { it.mood }.average()
            val notes = dailyMoods.mapNotNull { it.note }
            historyMoods.add(HistoryMood(dailyMoods.first().date, avg, notes))
        }
    }

    // Manual archive (useful for testing)
    fun forceArchiveToday() {
        archiveDailyMoods()
        dailyMoods.clear()
        lastSavedDate = LocalDate.now()
    }
}
