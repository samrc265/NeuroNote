package com.example.neuronote.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import com.example.neuronote.SleepEntry

@Entity(tableName = "sleep_table")
data class SleepEntity(
    @PrimaryKey val date: LocalDate,
    val hours: Int
)

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleep(sleep: SleepEntity)

    @Query("SELECT * FROM sleep_table ORDER BY date ASC")
    fun getAllSleep(): kotlinx.coroutines.flow.Flow<List<SleepEntity>>

    @Query("SELECT * FROM sleep_table WHERE date = :date")
    fun getSleepByDate(date: LocalDate): kotlinx.coroutines.flow.Flow<SleepEntity?>
}

object SleepDataManager {
    val sleepData = mutableStateListOf<SleepEntry>()
    private lateinit var dao: SleepDao

    fun init(context: Context) {
        dao = AppDatabase.getDatabase(context).sleepDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllSleep().collect { entries ->
                sleepData.clear()
                sleepData.addAll(entries.map { SleepEntry(it.date, it.hours) })
            }
        }
    }

    fun addSleepEntry(context: Context, date: LocalDate, hours: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertSleep(SleepEntity(date, hours.coerceIn(0, 24)))
        }
    }

    fun getSleepByDate(date: LocalDate) = dao.getSleepByDate(date)

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
