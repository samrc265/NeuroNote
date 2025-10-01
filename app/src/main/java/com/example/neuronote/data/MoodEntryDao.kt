package com.example.neuronote.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.neuronote.data.MoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodEntryDao {
    @Insert
    suspend fun insert(entry: MoodEntryEntity)

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY id ASC")
    fun getEntriesByDate(date: LocalDate): Flow<List<MoodEntryEntity>>

    @Query("SELECT AVG(mood) FROM mood_entries WHERE date = :date")
    fun getAverageMood(date: LocalDate): Flow<Double?>

    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getEntriesAfterDate(startDate: LocalDate): Flow<List<MoodEntryEntity>>
}