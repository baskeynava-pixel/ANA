package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.JarvisDatabase
import com.example.data.entity.ActionTask
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JarvisRepositoryTest {

    private lateinit var database: JarvisDatabase
    private lateinit var repository: JarvisRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, JarvisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = JarvisRepository(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSeedInitialData() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val knowledge = repository.allKnowledge.first()
        val memories = repository.allMemories.first()
        val tasks = repository.allTasks.first()
        val events = repository.allEvents.first()

        assertTrue(knowledge.isNotEmpty())
        assertTrue(memories.isNotEmpty())
        assertTrue(tasks.isNotEmpty())
        assertTrue(events.isNotEmpty())

        // Verify Sarah Flow task exists
        val sarahTask = tasks.find { it.title.contains("Sarah", ignoreCase = true) }
        assertNotNull(sarahTask)
        assertEquals("HIGH", sarahTask?.priority)
    }

    @Test
    fun testInsertAndToggleTask() = runBlocking {
        val taskId = repository.insertTask(
            ActionTask(
                title = "Calibrate mmWave sensor array",
                description = "Office spatial presence testing",
                priority = "MEDIUM",
                deadline = "Tomorrow",
                source = "TEST"
            )
        )

        val tasksBefore = repository.allTasks.first()
        val inserted = tasksBefore.find { it.id == taskId }
        assertNotNull(inserted)
        assertEquals(false, inserted?.isCompleted)

        repository.toggleTaskCompleted(taskId, true)
        val tasksAfter = repository.allTasks.first()
        val updated = tasksAfter.find { it.id == taskId }
        assertEquals(true, updated?.isCompleted)
    }

    @Test
    fun testExportMemoryGraphJson() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val jsonStr = repository.exportMemoryGraphJson()
        assertTrue(jsonStr.contains("knowledgeGraph"))
        assertTrue(jsonStr.contains("persistentMemory"))
        assertTrue(jsonStr.contains("actionTasks"))
    }
}
