package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ActionTaskDao
import com.example.data.dao.CalendarEventDao
import com.example.data.dao.KnowledgeDao
import com.example.data.dao.MemoryDao
import com.example.data.dao.SystemLogDao
import com.example.data.entity.ActionTask
import com.example.data.entity.CalendarEvent
import com.example.data.entity.KnowledgeItem
import com.example.data.entity.MemoryItem
import com.example.data.entity.SystemLog

@Database(
    entities = [
        KnowledgeItem::class,
        MemoryItem::class,
        ActionTask::class,
        CalendarEvent::class,
        SystemLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun memoryDao(): MemoryDao
    abstract fun actionTaskDao(): ActionTaskDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun systemLogDao(): SystemLogDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
