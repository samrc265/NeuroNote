package com.example.neuronote

import androidx.compose.runtime.mutableStateListOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class SleepEntry(val date: LocalDate, val hours: Int)

object SleepDataManager {
    val sleepData = mutableStateListOf<SleepEntry>()

    fun addSleepEntry(date: LocalDate, hours: Int) {
        val safe = hours.coerceIn(0, 24)
        val idx = sleepData.indexOfFirst { it.date == date }
        if (idx != -1) sleepData[idx] = SleepEntry(date, safe) else sleepData.add(SleepEntry(date, safe))
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
