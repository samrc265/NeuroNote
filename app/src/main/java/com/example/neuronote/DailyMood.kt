package com.example.neuronote

import java.time.LocalDate

// Stores moods entered in a single day (before averaging into history)
data class DailyMood(
    val mood: Int,            // 1–5 scale
    val date: LocalDate,
    val note: String? = null  // Optional note for this entry
)

// Stores the average mood of a past day
data class HistoryMood(
    val date: LocalDate,
    val averageMood: Double,
    val notes: List<String> = emptyList() // All notes from that day
)

object MoodDataManager {
    private val dailyMoods = mutableListOf<DailyMood>()
    private val historyMoods = mutableListOf<HistoryMood>()
    private var lastSavedDate: LocalDate? = null

    fun addDailyMood(mood: Int, date: LocalDate, note: String? = null) {
        // If date has changed, reset daily and archive yesterday
        if (lastSavedDate != null && date.isAfter(lastSavedDate)) {
            archiveDailyMoods()
            dailyMoods.clear()
        }

        dailyMoods.add(DailyMood(mood, date, note))
        lastSavedDate = date
    }

    private fun archiveDailyMoods() {
        if (dailyMoods.isNotEmpty()) {
            val avg = dailyMoods.map { it.mood }.average()
            val notes = dailyMoods.mapNotNull { it.note }
            historyMoods.add(HistoryMood(dailyMoods.first().date, avg, notes))
        }
    }

    fun getDailyMoods(): List<DailyMood> = dailyMoods
    fun getHistoryMoods(): List<HistoryMood> = historyMoods
}
