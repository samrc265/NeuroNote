package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.neuronote.datastore.DailyMoodEntry
import com.example.neuronote.datastore.HistoryMoodEntry
import com.example.neuronote.datastore.MoodData
import com.example.neuronote.datastore.MoodDataSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// Extension property for DataStore setup
private val Context.moodDataStore: DataStore<MoodData> by dataStore(
    fileName = "mood_tracker.pb",
    serializer = MoodDataSerializer
)

// Data classes for external use
data class DailyMood(val mood: Int, val date: LocalDate, val note: String? = null)
data class HistoryMood(val date: LocalDate, val averageMood: Double, val notes: List<String> = emptyList())

object MoodDataManager {
    private lateinit var applicationContext: Context
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _dailyMoods = mutableStateOf<List<DailyMood>>(emptyList())
    val dailyMoods: State<List<DailyMood>> get() = _dailyMoods

    private val _historyMoods = mutableStateOf<List<HistoryMood>>(emptyList())
    val historyMoods: State<List<HistoryMood>> get() = _historyMoods

    fun loadData(context: Context) {
        applicationContext = context.applicationContext

        // Observe and map Daily Moods
        applicationContext.moodDataStore.data
            .map { moodData ->
                moodData.dailyMoodsList
                    .map { proto -> DailyMood(proto.mood, LocalDate.parse(proto.date), proto.note.ifBlank { null }) }
                    .sortedByDescending { it.date }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
            .also { flow ->
                scope.launch { flow.collect { _dailyMoods.value = it } }
            }

        // Observe and map History Moods
        applicationContext.moodDataStore.data
            .map { moodData ->
                moodData.historyMoodsList
                    .map { proto -> HistoryMood(LocalDate.parse(proto.date), proto.averageMood, proto.notesList) }
                    .sortedByDescending { it.date }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
            .also { flow ->
                scope.launch { flow.collect { _historyMoods.value = it } }
            }

        // Run archival check immediately
        scope.launch { checkAndArchiveOldMoods() }
    }

    suspend fun addDailyMood(mood: Int, date: LocalDate = LocalDate.now(), note: String? = null) {
        val newEntry = DailyMoodEntry.newBuilder()
            .setMood(mood.coerceIn(1, 5))
            .setDate(date.toString())
            .apply { if (!note.isNullOrBlank()) setNote(note) }
            .build()

        applicationContext.moodDataStore.updateData { currentMoodData ->
            val mutableDailyMoods = currentMoodData.dailyMoodsList.toMutableList()

            val latestDate = mutableDailyMoods.firstOrNull()?.date?.let { LocalDate.parse(it) }
            if (latestDate != null && date > latestDate) {
                // Delegate archiving and get the new state
                val archivedData = archiveOldData(currentMoodData, latestDate)

                // Add the new mood to the newly archived data's daily list
                archivedData.toBuilder()
                    .addDailyMoods(0, newEntry)
                    .build()
            } else {
                // Just add the new mood to the current list
                mutableDailyMoods.add(0, newEntry)
                currentMoodData.toBuilder()
                    .clearDailyMoods()
                    .addAllDailyMoods(mutableDailyMoods)
                    .build()
            }
        }
    }

    // Logic extracted to be callable inside updateData (returns the new MoodData)
    private fun archiveOldData(currentMoodData: MoodData, dateToArchive: LocalDate): MoodData {
        val dailyMoodsList = currentMoodData.dailyMoodsList
        val historyMoodsList = currentMoodData.historyMoodsList.toMutableList()
        val dateString = dateToArchive.toString()

        val moodsToArchive = dailyMoodsList.filter { it.date == dateString }

        if (moodsToArchive.isNotEmpty()) {
            val avg = moodsToArchive.map { it.mood.toDouble() }.average()
            val notes = moodsToArchive.mapNotNull { it.note.ifBlank { null } }

            val newHistoryEntry = HistoryMoodEntry.newBuilder()
                .setDate(dateString)
                .setAverageMood(avg)
                .addAllNotes(notes)
                .build()

            // Add new history entry
            historyMoodsList.add(0, newHistoryEntry)
        }

        // Filter daily moods, keeping only those for today or later
        val newDailyMoods = dailyMoodsList.filter { LocalDate.parse(it.date) >= LocalDate.now() }

        return currentMoodData.toBuilder()
            .clearDailyMoods()
            .addAllDailyMoods(newDailyMoods)
            .clearHistoryMoods()
            .addAllHistoryMoods(historyMoodsList)
            .build()
    }

    private suspend fun checkAndArchiveOldMoods() {
        applicationContext.moodDataStore.data.collect { currentMoodData ->
            val latestDailyMood = currentMoodData.dailyMoodsList.firstOrNull()
            if (latestDailyMood != null) {
                val latestDate = LocalDate.parse(latestDailyMood.date)
                if (latestDate < LocalDate.now()) {
                    applicationContext.moodDataStore.updateData {
                        archiveOldData(it, latestDate)
                    }
                }
            }
        }
    }

    suspend fun forceArchiveToday() {
        applicationContext.moodDataStore.updateData {
            archiveOldData(it, LocalDate.now())
        }
    }
}