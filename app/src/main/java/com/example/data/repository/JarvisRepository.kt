package com.example.data.repository

import com.example.data.database.JarvisDatabase
import com.example.data.entity.ActionTask
import com.example.data.entity.CalendarEvent
import com.example.data.entity.KnowledgeItem
import com.example.data.entity.MemoryItem
import com.example.data.entity.SystemLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class JarvisRepository(private val database: JarvisDatabase) {

    val allKnowledge: Flow<List<KnowledgeItem>> = database.knowledgeDao().getAllKnowledge()
    val allMemories: Flow<List<MemoryItem>> = database.memoryDao().getAllMemories()
    val allTasks: Flow<List<ActionTask>> = database.actionTaskDao().getAllTasks()
    val allEvents: Flow<List<CalendarEvent>> = database.calendarEventDao().getAllEvents()
    val recentLogs: Flow<List<SystemLog>> = database.systemLogDao().getRecentLogs()

    fun searchKnowledge(query: String): Flow<List<KnowledgeItem>> {
        return database.knowledgeDao().searchKnowledge(query)
    }

    suspend fun insertKnowledge(item: KnowledgeItem) = withContext(Dispatchers.IO) {
        database.knowledgeDao().insert(item)
    }

    suspend fun deleteKnowledge(id: Long) = withContext(Dispatchers.IO) {
        database.knowledgeDao().deleteById(id)
    }

    suspend fun insertMemory(item: MemoryItem) = withContext(Dispatchers.IO) {
        database.memoryDao().insert(item)
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        database.memoryDao().deleteById(id)
    }

    suspend fun insertTask(task: ActionTask): Long = withContext(Dispatchers.IO) {
        database.actionTaskDao().insert(task)
    }

    suspend fun toggleTaskCompleted(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        database.actionTaskDao().setCompleted(id, completed)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        database.actionTaskDao().deleteById(id)
    }

    suspend fun logSystemCommand(command: String, output: String, status: String = "SUCCESS") = withContext(Dispatchers.IO) {
        database.systemLogDao().insert(
            SystemLog(command = command, output = output, status = status)
        )
    }

    suspend fun exportMemoryGraphJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val knowledgeList = database.knowledgeDao().getAllKnowledge().first()
        val memoryList = database.memoryDao().getAllMemories().first()
        val taskList = database.actionTaskDao().getAllTasks().first()

        val knowledgeArr = JSONArray()
        knowledgeList.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("category", it.category)
            obj.put("content", it.content)
            obj.put("sourceUrl", it.sourceUrl)
            obj.put("tags", it.tags)
            knowledgeArr.put(obj)
        }
        root.put("knowledgeGraph", knowledgeArr)

        val memoryArr = JSONArray()
        memoryList.forEach {
            val obj = JSONObject()
            obj.put("key", it.key)
            obj.put("value", it.value)
            obj.put("category", it.category)
            obj.put("confidence", it.confidence.toDouble())
            memoryArr.put(obj)
        }
        root.put("persistentMemory", memoryArr)

        val taskArr = JSONArray()
        taskList.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("priority", it.priority)
            obj.put("deadline", it.deadline)
            obj.put("isCompleted", it.isCompleted)
            taskArr.put(obj)
        }
        root.put("actionTasks", taskArr)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportedBy", "Project J.A.R.V.I.S. v1.0")

        root.toString(2)
    }

    suspend fun wipeUserData() = withContext(Dispatchers.IO) {
        database.knowledgeDao().clearAll()
        database.memoryDao().clearAll()
        database.actionTaskDao().clearAll()
        database.calendarEventDao().clearAll()
        database.systemLogDao().clearAll()
        logSystemCommand("WIPE_DATA", "User memory graph and cache wiped successfully.", "WARNING")
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentKnowledge = database.knowledgeDao().getAllKnowledge().first()
        if (currentKnowledge.isNotEmpty()) return@withContext

        // Initial Knowledge Items (Universal ingestion: Markdown, PDF, Email, Calendar)
        val initialKnowledge = listOf(
            KnowledgeItem(
                title = "Quarterly Target Report Q3-Q4 (Confidential PDF)",
                content = "Target ARR: $14.2M (+28% YoY). Key deliverables: Finish Enterprise SSO, expand API integration tier, achieve 99.9% uptime SLA. Target launch for Sarah's enterprise account is October 15th.",
                category = "DOCS",
                sourceUrl = "file://documents/reports/q3_q4_targets_final.pdf",
                tags = "targets, metrics, quarterly, sarah, enterprise"
            ),
            KnowledgeItem(
                title = "Daily Notes (Obsidian Markdown)",
                content = "Meeting with Sarah regarding product roadmap. Sarah requested update on API rate limit controls and SSO timeline. Need to prepare 3-bullet summary before our 2:00 PM sync.",
                category = "NOTES",
                sourceUrl = "obsidian://vault/daily/2026-09-13.md",
                tags = "meeting, sarah, api, roadmap, daily"
            ),
            KnowledgeItem(
                title = "Email Thread: Enterprise Pilot Feedback from Sarah VP",
                content = "From: Sarah (VP Product) <sarah@enterprise.io>\nSubject: Re: Next Quarter Targets & Integration\n'Hi team, can you send over the updated PDF report on quarterly targets and draft the response with the timeline? Let's review before final sign-off.'",
                category = "EMAIL",
                sourceUrl = "gmail://thread/msg-49102-sarah",
                tags = "email, sarah, targets, feedback"
            ),
            KnowledgeItem(
                title = "Home Assistant & Office IoT Setup",
                content = "Primary hub on local network 192.168.1.100. Protocols: Matter over Thread and Zigbee. Automated lighting set to focus cyan mode during work hours.",
                category = "BOOKMARKS",
                sourceUrl = "http://homeassistant.local:8123",
                tags = "iot, matter, automation, office"
            )
        )
        database.knowledgeDao().insertAll(initialKnowledge)

        // Persistent Long-Term Memory
        val initialMemories = listOf(
            MemoryItem(
                key = "preferred_focus_time",
                value = "Prefers uninterrupted deep work blocks between 9:00 AM - 11:30 AM and 2:00 PM - 4:00 PM.",
                category = "HABIT",
                confidence = 0.98f
            ),
            MemoryItem(
                key = "key_stakeholder_sarah",
                value = "Sarah (VP Product) - Lead executive stakeholder. Values concise bulleted updates with clear deadline commitments.",
                category = "RELATIONSHIP",
                confidence = 0.99f
            ),
            MemoryItem(
                key = "primary_strategic_goal",
                value = "Close Q3 ARR targets of $14.2M and ship Enterprise SSO by Oct 15.",
                category = "GOAL",
                confidence = 0.96f
            ),
            MemoryItem(
                key = "assistant_voice_persona",
                value = "Polite, British butler tone (J.A.R.V.I.S.), witty, prioritize high signal-to-noise ratio, calls user 'sir'.",
                category = "PREFERENCE",
                confidence = 1.0f
            )
        )
        database.memoryDao().insertAll(initialMemories)

        // Action Tasks
        val initialTasks = listOf(
            ActionTask(
                title = "Send quarterly target report to Sarah",
                description = "Extract key metrics from Q3-Q4 report PDF and prepare response draft for Sarah's review.",
                priority = "HIGH",
                deadline = "Today, 1:45 PM",
                source = "EMAIL_SCAN",
                isCompleted = false
            ),
            ActionTask(
                title = "Audit edge-and-cloud telemetry latency",
                description = "Verify voice turn-taking latency remains sub-600ms on local WiFi network.",
                priority = "MEDIUM",
                deadline = "Tomorrow, 10:00 AM",
                source = "VOICE_MEMO",
                isCompleted = false
            ),
            ActionTask(
                title = "Configure Matter bridge for office lighting",
                description = "Sync smart switch cluster with Jarvis IoT control plane.",
                priority = "LOW",
                deadline = "Sep 18",
                source = "USER_INPUT",
                isCompleted = true
            )
        )
        database.actionTaskDao().insertAll(initialTasks)

        // Calendar Events
        val initialEvents = listOf(
            CalendarEvent(
                title = "Daily Standup & Telemetry Review",
                time = "09:30 AM - 10:00 AM",
                attendees = "Engineering Core, Systems Lead",
                location = "Hologram Room B / Video Call",
                prepNotes = "Review overnight system diagnostics and memory indexing throughput."
            ),
            CalendarEvent(
                title = "Strategic Sync with Sarah (VP Product)",
                time = "02:00 PM - 02:45 PM",
                attendees = "Sarah (VP Product), Executive Team",
                location = "Executive Boardroom",
                prepNotes = "Present quarterly target metrics and Enterprise SSO rollout timeline."
            ),
            CalendarEvent(
                title = "Deep Focus Block: Architecture Tuning",
                time = "03:00 PM - 04:30 PM",
                attendees = "Solo Focus",
                location = "Main Office",
                prepNotes = "Zero interruption block recommended by Jarvis schedule optimizer."
            )
        )
        database.calendarEventDao().insertAll(initialEvents)

        // Initial System Log
        database.systemLogDao().insert(
            SystemLog(
                command = "BOOT_SEQUENCE",
                output = "J.A.R.V.I.S. Core v2.4 initialized. All subroutines online. Voice interface ready.",
                status = "SUCCESS"
            )
        )
    }
}
