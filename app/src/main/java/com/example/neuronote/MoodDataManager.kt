package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

@Serializable
data class DailyMood(
    val mood: Int,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val note: String? = null
)

@Serializable
data class HistoryMood(
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val averageMood: Double,
    val notes: List<String> = emptyList()
)

object MoodDataManager {
    val dailyMoods = mutableStateListOf<DailyMood>()
    val historyMoods = mutableStateListOf<HistoryMood>()
    private var lastSavedDate: LocalDate? = null
    private const val HISTORY_FILE_NAME = "mood_history.json"
    private const val DAILY_FILE_NAME = "daily_moods.json"

    fun loadData(context: Context) {
        val historyFile = File(context.filesDir, HISTORY_FILE_NAME)
        if (historyFile.exists()) {
            val jsonString = historyFile.readText()
            historyMoods.clear()
            historyMoods.addAll(Json.decodeFromString<List<HistoryMood>>(jsonString))
        }
        val dailyFile = File(context.filesDir, DAILY_FILE_NAME)
        if (dailyFile.exists()) {
            val jsonString = dailyFile.readText()
            dailyMoods.clear()
            dailyMoods.addAll(Json.decodeFromString<List<DailyMood>>(jsonString))
        }
    }

    private fun saveData(context: Context) {
        val historyFile = File(context.filesDir, HISTORY_FILE_NAME)
        historyFile.writeText(Json.encodeToString(historyMoods.toList()))

        val dailyFile = File(context.filesDir, DAILY_FILE_NAME)
        dailyFile.writeText(Json.encodeToString(dailyMoods.toList()))
    }

    fun addDailyMood(context: Context, mood: Int, date: LocalDate = LocalDate.now(), note: String? = null) {
        if (lastSavedDate != null && date.isAfter(lastSavedDate)) {
            archiveDailyMoods(context)
            dailyMoods.clear()
        }
        dailyMoods.add(DailyMood(mood.coerceIn(1, 5), date, note))
        lastSavedDate = date
        saveData(context)
    }

    private fun archiveDailyMoods(context: Context) {
        if (dailyMoods.isNotEmpty()) {
            val avg = dailyMoods.map { it.mood }.average()
            val notes = dailyMoods.mapNotNull { it.note }
            historyMoods.add(HistoryMood(dailyMoods.first().date, avg, notes))
            saveData(context)
        }
    }

    fun forceArchiveToday(context: Context) {
        archiveDailyMoods(context)
        dailyMoods.clear()
        lastSavedDate = LocalDate.now()
        saveData(context)
    }
}