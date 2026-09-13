package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ActionTask
import com.example.data.entity.CalendarEvent
import com.example.data.entity.KnowledgeItem
import com.example.data.entity.MemoryItem
import com.example.data.entity.SystemLog
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items ORDER BY timestamp DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeItem>>

    @Query("SELECT * FROM knowledge_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchKnowledge(query: String): Flow<List<KnowledgeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: KnowledgeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<KnowledgeItem>)

    @Query("DELETE FROM knowledge_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM knowledge_items")
    suspend fun clearAll()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY category, key ASC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MemoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MemoryItem>)

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memory_items")
    suspend fun clearAll()
}

@Dao
interface ActionTaskDao {
    @Query("SELECT * FROM action_tasks ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, createdAt DESC")
    fun getAllTasks(): Flow<List<ActionTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ActionTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<ActionTask>)

    @Update
    suspend fun update(task: ActionTask)

    @Query("UPDATE action_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM action_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM action_tasks")
    suspend fun clearAll()
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEvent>)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<SystemLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SystemLog): Long

    @Query("DELETE FROM system_logs")
    suspend fun clearAll()
}
