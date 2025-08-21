package com.example.neuronote

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime

object DiaryDataManager {
    val entries = mutableStateListOf<DiaryEntry>()
    private const val FILE_NAME = "diary_entries.json"

    fun loadEntries(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            val jsonString = file.readText()
            entries.clear()
            entries.addAll(Json.decodeFromString<List<DiaryEntry>>(jsonString))
        }
    }

    private fun saveEntries(context: Context) {
        val jsonString = Json.encodeToString(entries.toList())
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonString)
    }

    fun addEntry(context: Context, entry: DiaryEntry) {
        entries.add(0, entry)
        saveEntries(context)
    }

    fun updateEntry(context: Context, updated: DiaryEntry) {
        val idx = entries.indexOfFirst { it.id == updated.id }
        if (idx != -1) entries[idx] = updated
        saveEntries(context)
    }

    fun removeEntry(context: Context, id: Long) {
        entries.removeIf { it.id == id }
        saveEntries(context)
    }
}