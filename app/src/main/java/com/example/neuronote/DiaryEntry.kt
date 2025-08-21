package com.example.neuronote

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class DiaryEntry(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val mood: Int,
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime = LocalDateTime.now()
)