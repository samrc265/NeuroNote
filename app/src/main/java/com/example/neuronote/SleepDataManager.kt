package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Serializable
data class SleepEntry(
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val hours: Int
)

object SleepDataManager {
    val sleepData = mutableStateListOf<SleepEntry>()
    private const val FILE_NAME = "sleep_data.json"

    fun loadData(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val jsonString = file.readText()
            sleepData.clear()
            sleepData.addAll(Json.decodeFromString<List<SleepEntry>>(jsonString))
        }
    }

    private fun saveData(context: Context) {
        val jsonString = Json.encodeToString(sleepData.toList())
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonString)
    }

    fun addSleepEntry(context: Context, date: LocalDate, hours: Int) {
        val safe = hours.coerceIn(0, 24)
        val idx = sleepData.indexOfFirst { it.date == date }
        if (idx != -1) sleepData[idx] = SleepEntry(date, safe) else sleepData.add(SleepEntry(date, safe))
        saveData(context)
    }

    fun getHoursMapForWeek(referenceDay: LocalDate = LocalDate.now()): Map<DayOfWeek, Int> {
        val monday = referenceDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val days = (0L..6L).map { monday.plusDays(it) }
        val map = days.associateBy({ it.dayOfWeek }) { day -> sleepData.firstOrNull { it.date == day }?.hours ?: 0 }
        return DayOfWeek.values().associateWith { map[it] ?: 0 }
    }

    fun getEntriesForWeek(referenceDay: LocalDate = LocalDate.now()): List<SleepEntry> {
        val monday = referenceDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        return sleepData.filter { !it.date.isBefore(monday) && !it.date.isAfter(sunday) }
    }
}