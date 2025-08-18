package com.example.neuronote

import java.time.LocalDateTime

data class DiaryEntry(
    val id: Long = System.currentTimeMillis(),
    var title: String,
    var mood: Int, // 1–5 scale
    var content: String,
    val date: LocalDateTime = LocalDateTime.now()
)
