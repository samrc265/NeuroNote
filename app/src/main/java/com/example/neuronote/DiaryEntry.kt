package com.example.neuronote

import java.time.LocalDateTime

data class DiaryEntry(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val mood: Int,
    val content: String,
    val date: LocalDateTime = LocalDateTime.now()
)
