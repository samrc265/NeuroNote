package com.example.neuronote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "sleep_table")
data class SleepEntity(
    @PrimaryKey val date: LocalDate,
    val hours: Int
)