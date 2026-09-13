package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_items")
data class KnowledgeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "NOTES", "EMAIL", "CALENDAR", "DOCS", "BOOKMARKS"
    val sourceUrl: String,
    val tags: String, // comma-separated
    val timestamp: Long = System.currentTimeMillis(),
    val isIndexed: Boolean = true
)

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String, // "PREFERENCE", "HABIT", "GOAL", "RELATIONSHIP"
    val confidence: Float = 0.95f,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "action_tasks")
data class ActionTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val deadline: String,
    val source: String, // "VOICE_MEMO", "EMAIL_SCAN", "MEETING_BRIEF", "USER_INPUT"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val output: String,
    val status: String, // "SUCCESS", "WARNING", "ABORTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val time: String,
    val attendees: String,
    val location: String,
    val prepNotes: String,
    val hasConflict: Boolean = false
)
