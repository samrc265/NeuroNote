package com.example.neuronote

import androidx.compose.runtime.mutableStateListOf

object DiaryDataManager {
    val entries = mutableStateListOf<DiaryEntry>()

    fun addEntry(entry: DiaryEntry) { entries.add(0, entry) } // newest first
    fun updateEntry(updated: DiaryEntry) {
        val idx = entries.indexOfFirst { it.id == updated.id }
        if (idx != -1) entries[idx] = updated
    }
    fun removeEntry(id: Long) { entries.removeIf { it.id == id } }
}
