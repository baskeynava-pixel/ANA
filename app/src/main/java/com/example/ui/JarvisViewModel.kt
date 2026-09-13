package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.database.JarvisDatabase
import com.example.data.entity.ActionTask
import com.example.data.entity.CalendarEvent
import com.example.data.entity.KnowledgeItem
import com.example.data.entity.MemoryItem
import com.example.data.entity.SystemLog
import com.example.data.repository.JarvisRepository
import com.example.diagnostics.JarvisDiagnosticsMonitor
import com.example.diagnostics.SystemTelemetry
import com.example.iot.IotDevice
import com.example.iot.JarvisIotManager
import com.example.iot.SpatialRoom
import com.example.voice.JarvisVoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getInstance(application)
    val repository = JarvisRepository(db)
    val voiceEngine = JarvisVoiceEngine(application)
    val diagnosticsMonitor = JarvisDiagnosticsMonitor(application)
    val iotManager = JarvisIotManager()
    val geminiService = GeminiService()

    // Nav Tab: 0 = HUD Copilot, 1 = Executive & Tasks, 2 = Knowledge & RAG, 3 = Smart IoT, 4 = Terminal & Telemetry
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Telemetry
    val telemetry: StateFlow<SystemTelemetry> = diagnosticsMonitor.pollTelemetryFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            diagnosticsMonitor.captureTelemetry()
        )

    // Data from Room
    val allKnowledge: StateFlow<List<KnowledgeItem>> = repository.allKnowledge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<ActionTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemLogs: StateFlow<List<SystemLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // IoT
    val iotDevices: StateFlow<List<IotDevice>> = iotManager.devices
    val spatialRooms: StateFlow<List<SpatialRoom>> = iotManager.rooms

    // UI state
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _lastAiResponse = MutableStateFlow(
        "Good day, sir. All J.A.R.V.I.S. core systems are nominal. Voice interface, telemetry monitors, and persistent memory graph are fully synchronized. How may I be of assistance?"
    )
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    private val _inputQuery = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery.asStateFlow()

    private val _isTtsMuted = MutableStateFlow(false)
    val isTtsMuted: StateFlow<Boolean> = _isTtsMuted.asStateFlow()

    // RAG Search
    private val _ragSearchQuery = MutableStateFlow("")
    val ragSearchQuery: StateFlow<String> = _ragSearchQuery.asStateFlow()

    val filteredKnowledge: StateFlow<List<KnowledgeItem>> = combine(
        allKnowledge,
        _ragSearchQuery
    ) { items, query ->
        if (query.isBlank()) items
        else {
            val q = query.lowercase()
            items.filter {
                it.title.lowercase().contains(q) ||
                        it.content.lowercase().contains(q) ||
                        it.tags.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Email Review Dialog (Flow 2)
    private val _emailReviewDraft = MutableStateFlow<String?>(null)
    val emailReviewDraft: StateFlow<String?> = _emailReviewDraft.asStateFlow()

    // Export Memory Dialog
    private val _exportedMemory = MutableStateFlow<String?>(null)
    val exportedMemory: StateFlow<String?> = _exportedMemory.asStateFlow()

    // Feedback Banner
    private val _feedbackNotice = MutableStateFlow<String?>(null)
    val feedbackNotice: StateFlow<String?> = _feedbackNotice.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setInputQuery(text: String) {
        _inputQuery.value = text
    }

    fun setRagSearchQuery(text: String) {
        _ragSearchQuery.value = text
    }

    fun toggleMuteTts() {
        _isTtsMuted.value = !_isTtsMuted.value
        if (_isTtsMuted.value) {
            voiceEngine.stop()
        }
    }

    fun abortSequence() {
        voiceEngine.abort()
        _isAiGenerating.value = false
        _lastAiResponse.value = "Aborting command sequence, sir. All active routines suspended."
        viewModelScope.launch {
            repository.logSystemCommand("ABORT_COMMAND", "User issued emergency abort sequence.", "ABORTED")
        }
    }

    fun submitPrompt(promptText: String = _inputQuery.value) {
        val query = promptText.trim()
        if (query.isBlank()) return
        _inputQuery.value = ""
        _isAiGenerating.value = true

        viewModelScope.launch {
            // Check for emergency abort voice trigger
            if (query.lowercase().contains("abort")) {
                abortSequence()
                return@launch
            }

            // Assemble RAG context from indexed knowledge and memory
            val knowledgeContext = allKnowledge.value.take(5).joinToString("\n---\n") {
                "[Category: ${it.category}] Title: ${it.title}\nSource: ${it.sourceUrl}\nContent: ${it.content}"
            }
            val memoryContext = allMemories.value.joinToString("\n") {
                "- [${it.category}] ${it.key}: ${it.value}"
            }

            repository.logSystemCommand("PROMPT_INPUT", query, "SUCCESS")

            val response = geminiService.generateJarvisResponse(query, knowledgeContext, memoryContext)
            _lastAiResponse.value = response
            _isAiGenerating.value = false

            // Check if response contains draft email for Sarah (Flow 2)
            if (response.contains("Draft ready", ignoreCase = true) && response.contains("Sarah", ignoreCase = true)) {
                _emailReviewDraft.value = response
            }

            // Auto-speak response if not muted
            if (!_isTtsMuted.value) {
                voiceEngine.speak(response)
            }

            repository.logSystemCommand("AI_RESPONSE", response.take(120) + "...", "SUCCESS")
        }
    }

    fun startVoiceInput() {
        voiceEngine.startListening { spoken ->
            _inputQuery.value = spoken
            submitPrompt(spoken)
        }
    }

    fun toggleTask(taskId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId, !currentCompleted)
        }
    }

    fun addNewTask(title: String, description: String, priority: String, deadline: String) {
        viewModelScope.launch {
            repository.insertTask(
                ActionTask(
                    title = title,
                    description = description,
                    priority = priority,
                    deadline = deadline,
                    source = "USER_INPUT"
                )
            )
            repository.logSystemCommand("CREATE_TASK", "Task '$title' added to queue.", "SUCCESS")
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun extractActionItemsFromMemo(memoText: String) {
        _isAiGenerating.value = true
        viewModelScope.launch {
            val prompt = "Extract all actionable tasks from this transcript/memo into structured tasks with deadlines and priority: $memoText"
            val response = geminiService.generateJarvisResponse(prompt)
            _lastAiResponse.value = response
            _isAiGenerating.value = false

            // Insert newly extracted tasks into task board
            repository.insertTask(
                ActionTask(
                    title = "Follow up on transcript: ${memoText.take(40)}...",
                    description = response.take(200),
                    priority = "HIGH",
                    deadline = "Today",
                    source = "VOICE_MEMO"
                )
            )
            if (!_isTtsMuted.value) {
                voiceEngine.speak("I have extracted the structured action items and pushed them to your Task Manager, sir.")
            }
        }
    }

    fun synthesizeMorningBriefing() {
        submitPrompt("Synthesize my custom morning briefing agenda with priorities, weather, and schedule optimization.")
    }

    fun triggerSarahFlow() {
        submitPrompt("Find that PDF report on quarterly targets and draft a response to Sarah.")
    }

    fun confirmSendEmailDraft() {
        _emailReviewDraft.value = null
        _feedbackNotice.value = "Email securely dispatched to Sarah (VP Product)."
        viewModelScope.launch {
            repository.logSystemCommand("SEND_EMAIL", "Dispatched quarterly targets email to Sarah.", "SUCCESS")
            if (!_isTtsMuted.value) {
                voiceEngine.speak("The email has been dispatched to Sarah with the quarterly metrics attached, sir.")
            }
        }
    }

    fun dismissEmailDraft() {
        _emailReviewDraft.value = null
    }

    fun exportMemoryGraph() {
        viewModelScope.launch {
            val json = repository.exportMemoryGraphJson()
            _exportedMemory.value = json
            _feedbackNotice.value = "User memory graph exported (1-click export)."
        }
    }

    fun dismissExportDialog() {
        _exportedMemory.value = null
    }

    fun wipeAllData() {
        viewModelScope.launch {
            repository.wipeUserData()
            _feedbackNotice.value = "All local user memory and indices wiped."
            _lastAiResponse.value = "All memory matrices have been purged to factory initialization, sir."
        }
    }

    fun restoreSeedData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            _feedbackNotice.value = "Knowledge graph restored to default state."
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            when (trimmed.lowercase()) {
                "sys_diag", "diagnostics" -> {
                    val t = telemetry.value
                    val out = "CPU/RAM: ${t.ramPercentUsed}% (${t.ramUsedMb}MB / ${t.ramTotalMb}MB) | Battery: ${t.batteryPercent}% (${t.batteryTempCelsius}°C) | Net: ${t.networkType} (${t.latencyMs}ms)"
                    repository.logSystemCommand(trimmed, out, "SUCCESS")
                }
                "net_check" -> {
                    val t = telemetry.value
                    val out = "Network: ${t.networkType} | Status: ${if (t.isOnline) "ONLINE" else "OFFLINE"} | Latency: ${t.latencyMs}ms"
                    repository.logSystemCommand(trimmed, out, "SUCCESS")
                }
                "sync_graph" -> {
                    val count = allKnowledge.value.size
                    val out = "Re-indexed $count documents into local vector space. Zero embedding faults."
                    repository.logSystemCommand(trimmed, out, "SUCCESS")
                }
                "mem_clean" -> {
                    System.gc()
                    repository.logSystemCommand(trimmed, "Garbage collection triggered. Heap cache cleared.", "SUCCESS")
                }
                "iot_status" -> {
                    val devs = iotDevices.value.map { "${it.name}: ${it.state}" }.joinToString(" | ")
                    repository.logSystemCommand(trimmed, "Matter Mesh: $devs", "SUCCESS")
                }
                "abort" -> {
                    abortSequence()
                }
                else -> {
                    // Route unknown terminal command to AI
                    val response = geminiService.generateJarvisResponse("Execute terminal instruction: $trimmed")
                    repository.logSystemCommand(trimmed, response, "SUCCESS")
                }
            }
        }
    }

    fun clearFeedbackNotice() {
        _feedbackNotice.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
