package com.example.neuronote

import androidx.compose.runtime.mutableStateListOf

object DiaryDataManager {
    private val entries = mutableStateListOf<DiaryEntry>()

    fun getEntries(): List<DiaryEntry> = entries
    fun addEntry(entry: DiaryEntry) { entries.add(0, entry) } // newest at top
    fun updateEntry(updated: DiaryEntry) {
        val idx = entries.indexOfFirst { it.id == updated.id }
        if (idx != -1) entries[idx] = updated
    }
}
