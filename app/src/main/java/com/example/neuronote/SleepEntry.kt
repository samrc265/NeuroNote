package com.example.neuronote

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

data class SleepEntry(val date: LocalDate, val hours: Int)

object SleepDataManager {
    private val sleepEntries = mutableListOf<SleepEntry>()

    fun addSleep(hours: Int, date: LocalDate = LocalDate.now()) {
        sleepEntries.add(SleepEntry(date, hours))
    }

    fun getThisWeekEntries(): List<SleepEntry> {
        val currentWeek = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        return sleepEntries.filter {
            it.date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) == currentWeek
        }
    }
}
