package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.neuronote.datastore.SleepData
import com.example.neuronote.datastore.SleepDataSerializer
import com.example.neuronote.datastore.SleepEntry as ProtoSleepEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// Extension property for DataStore setup
private val Context.sleepDataStore: DataStore<SleepData> by dataStore(
    fileName = "sleep_tracker.pb",
    serializer = SleepDataSerializer
)

// Data class kept for compatibility with UI layers
data class SleepEntry(val date: LocalDate, val hours: Int)

object SleepDataManager {
    private lateinit var applicationContext: Context
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _sleepData = mutableStateOf<List<SleepEntry>>(emptyList())
    val sleepData: State<List<SleepEntry>> get() = _sleepData

    fun loadData(context: Context) {
        applicationContext = context.applicationContext

        applicationContext.sleepDataStore.data
            .map { sleepData ->
                sleepData.sleepEntriesList
                    .map { proto -> SleepEntry(LocalDate.parse(proto.date), proto.hours) }
                    .sortedByDescending { it.date }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
            .also { flow ->
                scope.launch { flow.collect { _sleepData.value = it } }
            }
    }

    suspend fun addSleepEntry(date: LocalDate, hours: Int) {
        val dateString = date.toString()
        val safeHours = hours.coerceIn(0, 24)

        applicationContext.sleepDataStore.updateData { currentSleepData ->
            val mutableEntries = currentSleepData.sleepEntriesList.toMutableList()
            var entryFound = false

            // Check if entry exists for the date and update it
            for (i in mutableEntries.indices) {
                if (mutableEntries[i].date == dateString) {
                    mutableEntries[i] = ProtoSleepEntry.newBuilder()
                        .setDate(dateString)
                        .setHours(safeHours)
                        .build()
                    entryFound = true
                    break
                }
            }

            // If not found, create a new entry and add it
            if (!entryFound) {
                val newEntry = ProtoSleepEntry.newBuilder()
                    .setDate(dateString)
                    .setHours(safeHours)
                    .build()
                mutableEntries.add(newEntry)
            }

            currentSleepData.toBuilder()
                .clearSleepEntries()
                .addAllSleepEntries(mutableEntries)
                .build()
        }
    }

    fun getHoursMapForWeek(referenceDay: LocalDate = LocalDate.now()): Map<DayOfWeek, Int> {
        val currentSleepData = sleepData.value
        val monday = referenceDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val days = (0L..6L).map { monday.plusDays(it) }

        val map = days.associateBy({ it.dayOfWeek }) { day ->
            currentSleepData.firstOrNull { it.date == day }?.hours ?: 0
        }
        return DayOfWeek.values().associateWith { map[it] ?: 0 }
    }

    fun getEntriesForWeek(referenceDay: LocalDate = LocalDate.now()): List<SleepEntry> {
        val currentSleepData = sleepData.value
        val monday = referenceDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        return currentSleepData.filter { !it.date.isBefore(monday) && !it.date.isAfter(sunday) }
    }
}