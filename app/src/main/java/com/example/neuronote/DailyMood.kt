package com.example.neuronote

import java.time.LocalDate

data class DailyMood(
    val mood: Int,
    val date: LocalDate
)

data class HistoryMood(
    val date: LocalDate,
    val averageMood: Double
)

object MoodDataManager {
    private val dailyMoods = mutableListOf<DailyMood>()
    private val historyMoods = mutableListOf<HistoryMood>()

    fun addDailyMood(mood: Int) {
        val today = LocalDate.now()
        dailyMoods.add(DailyMood(mood, today))
    }

    fun getDailyMoods(): List<DailyMood> = dailyMoods
    fun getHistoryMoods(): List<HistoryMood> = historyMoods

    fun checkAndResetDaily() {
        if (dailyMoods.isNotEmpty() && dailyMoods.first().date != LocalDate.now()) {
            saveDailyAverageToHistory()
            dailyMoods.clear()
        }
    }

    private fun saveDailyAverageToHistory() {
        if (dailyMoods.isEmpty()) return
        val avgMood = dailyMoods.map { it.mood }.average()
        val date = dailyMoods.first().date
        historyMoods.add(HistoryMood(date, avgMood))
    }
}
