package com.example.neuronote.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    val mood: Int,
    val note: String? = null
)

@Dao
interface MoodEntryDao {
    @Insert
    suspend fun insert(entry: MoodEntryEntity)

    @Query("SELECT * FROM mood_entries ORDER BY date ASC, id ASC")
    fun getAllEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY id ASC")
    fun getEntriesByDate(date: LocalDate): Flow<List<MoodEntryEntity>>
}

data class DailyMood(val mood: Int, val date: LocalDate, val note: String? = null)
data class HistoryMood(val date: LocalDate, val averageMood: Double, val notes: List<String> = emptyList())

object MoodDataManager {
    val dailyMoods = mutableStateListOf<DailyMood>()
    val historyMoods = mutableStateListOf<HistoryMood>()

    private lateinit var dao: MoodEntryDao

    fun init(context: Context) {
        dao = AppDatabase.getDatabase(context).moodEntryDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllEntries().collect { entries -> rebuildListsFromEntries(entries) }
        }
    }

    private fun rebuildListsFromEntries(entries: List<MoodEntryEntity>) {
        val grouped = entries.groupBy { it.date }.toSortedMap()
        if (grouped.isEmpty()) {
            dailyMoods.clear(); historyMoods.clear(); return
        }
        val sortedDates = grouped.keys.sorted()
        val latestDate = sortedDates.maxOrNull()!!

        dailyMoods.clear()
        grouped[latestDate]?.let { listForLatest ->
            dailyMoods.addAll(listForLatest.map { DailyMood(it.mood, it.date, it.note) })
        }

        historyMoods.clear()
        sortedDates.filter { it != latestDate }.forEach { d ->
            val listForDate = grouped[d] ?: emptyList()
            val avg = if (listForDate.isEmpty()) 0.0 else listForDate.map { it.mood }.average()
            val notes = listForDate.mapNotNull { it.note }
            historyMoods.add(HistoryMood(d, avg, notes))
        }
    }

    fun addDailyMood(context: Context, mood: Int, date: LocalDate = LocalDate.now(), note: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(MoodEntryEntity(date = date, mood = mood.coerceIn(1, 5), note = note))
        }
    }

    fun getAverageMoodFlow(date: LocalDate): Flow<Double?> {
        return dao.getEntriesByDate(date).map { list ->
            if (list.isEmpty()) null else list.map { it.mood }.average()
        }
    }
}
