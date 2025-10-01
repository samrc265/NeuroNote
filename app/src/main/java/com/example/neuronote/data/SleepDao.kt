package com.example.neuronote.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neuronote.data.SleepEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleep(sleep: SleepEntity)
    
    @Query("SELECT * FROM sleep_table WHERE date = :date")
    fun getSleepByDate(date: LocalDate): Flow<SleepEntity?>
    
    @Query("SELECT * FROM sleep_table ORDER BY date ASC")
    fun getAllSleep(): Flow<List<SleepEntity>>
    
    @Query("SELECT * FROM sleep_table WHERE date BETWEEN :startDate AND :endDate")
    fun getSleepBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepEntity>>
}