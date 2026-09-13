package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ActionTask
import com.example.data.entity.CalendarEvent
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.HudSectionHeader
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudBorderBright
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExecutiveBriefingScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val events by viewModel.allEvents.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Morning Agenda, 1 = Action Tasks, 2 = Calendar Schedule
    var showExtractMemoDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var rawMemoText by remember { mutableStateOf("") }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("HIGH") }
    var newTaskDeadline by remember { mutableStateOf("Today, 5:00 PM") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(16.dp)
    ) {
        // Top Sub-Tabs
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = HudSurface,
            contentColor = ArcCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = ArcCyan,
                    height = 2.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Daily Briefing", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Task Board (${tasks.count { !it.isCompleted }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Schedule (${events.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeSubTab) {
            0 -> MorningBriefingView(
                events = events,
                tasks = tasks,
                onSynthesize = { viewModel.synthesizeMorningBriefing() },
                onExtractMemo = { showExtractMemoDialog = true },
                onTriggerSarah = { viewModel.triggerSarahFlow() }
            )
            1 -> ActionTasksBoardView(
                tasks = tasks,
                onToggle = { id, comp -> viewModel.toggleTask(id, comp) },
                onDelete = { id -> viewModel.deleteTask(id) },
                onAddTask = { showAddTaskDialog = true },
                onExtractMemo = { showExtractMemoDialog = true }
            )
            2 -> ScheduleOptimizationView(
                events = events,
                onPrepareBriefing = { event ->
                    viewModel.submitPrompt("Generate 3-bullet meeting prep briefing for '${event.title}' with attendees: ${event.attendees}.")
                }
            )
        }
    }

    // Dialog: Action Extraction from Voice Memo / Incoming Message
    if (showExtractMemoDialog) {
        AlertDialog(
            onDismissRequest = { showExtractMemoDialog = false },
            containerColor = HudSurface,
            title = {
                Text(
                    "AUTONOMOUS ACTION EXTRACTION",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ArcCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste an incoming email, slack message, or post-meeting transcript. Jarvis will extract structured action items with priority and deadlines.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = rawMemoText,
                        onValueChange = { rawMemoText = it },
                        placeholder = {
                            Text("e.g. 'Met with Sarah. Need to finalize the quarterly ARR slide by 3pm and test the Matter IoT light switch by tomorrow.'", fontSize = 11.sp, color = TextMuted)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcCyan,
                            unfocusedBorderColor = HudBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rawMemoText.isNotBlank()) {
                            viewModel.extractActionItemsFromMemo(rawMemoText)
                            rawMemoText = ""
                            showExtractMemoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
                ) {
                    Text("Extract Actions", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtractMemoDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Dialog: Add Custom Action Task
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = HudSurface,
            title = {
                Text("NEW ACTION TASK", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = ArcCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = HudBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Description / Details") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = HudBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskDeadline,
                        onValueChange = { newTaskDeadline = it },
                        label = { Text("Deadline") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = HudBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { prio ->
                            Button(
                                onClick = { newTaskPriority = prio },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (newTaskPriority == prio) ArcCyanDark else HudSurfaceVariant
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(prio, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addNewTask(newTaskTitle, newTaskDesc, newTaskPriority, newTaskDeadline)
                            newTaskTitle = ""
                            newTaskDesc = ""
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
                ) {
                    Text("Save Task", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun MorningBriefingView(
    events: List<CalendarEvent>,
    tasks: List<ActionTask>,
    onSynthesize: () -> Unit,
    onExtractMemo: () -> Unit,
    onTriggerSarah: () -> Unit
) {
    val pendingHighTasks = tasks.filter { !it.isCompleted && it.priority == "HIGH" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Briefing Header
        item {
            HudCard(borderColor = ArcCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = JarvisGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "EXECUTIVE MORNING BRIEFING",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = JarvisGold
                            )
                        }
                        Text(
                            text = "22°C Clear • 09:30 AM",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "Good morning, sir. You have ${events.size} scheduled commitments today, including the Strategic Sync with Sarah at 2:00 PM. ${pendingHighTasks.size} high-priority action items require attention. A recommended 90-minute deep focus block has been reserved.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSynthesize,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Re-Synthesize Agenda", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onExtractMemo,
                            colors = ButtonDefaults.buttonColors(containerColor = HudSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan Memo", color = ArcCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Key Needs & Proactive Shortcuts
        item {
            HudSectionHeader(title = "Proactive Action Triggers", subtitle = "Automated executive workflows")
        }

        item {
            HudCard(
                borderColor = JarvisGold,
                onClick = onTriggerSarah
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusWarning)
                            )
                            Text(
                                text = "EXECUTE FLOW 2: QUARTERLY TARGET DRAFT",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Query local vector index for Q3 target PDF -> Draft email to Sarah with pre-send review prompt.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = onTriggerSarah,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisGold),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Draft", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Focus Block Recommendation Card
        item {
            HudCard(borderColor = StatusSuccess) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "CALENDAR OPTIMIZATION: FOCUS BLOCK",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                        Text(
                            text = "03:00 PM - 04:30 PM: Conflict-free 90 min deep work window detected. Automated DND and focus cyan lighting ready.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTasksBoardView(
    tasks: List<ActionTask>,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onAddTask: () -> Unit,
    onExtractMemo: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudSectionHeader(title = "Task Management", subtitle = "Auto-extracted from voice & emails")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onExtractMemo,
                    colors = ButtonDefaults.buttonColors(containerColor = HudSurfaceVariant),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Auto-Extract", fontSize = 10.sp, color = ArcCyan)
                }
                Button(
                    onClick = onAddTask,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Text("Add", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending action tasks.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onToggle = { onToggle(task.id, task.isCompleted) },
                        onDelete = { onDelete(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    task: ActionTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> StatusError
        "MEDIUM" -> JarvisGold
        else -> StatusSuccess
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isCompleted) HudBorder else priorityColor.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) HudSurfaceVariant.copy(alpha = 0.5f) else HudSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (task.isCompleted) StatusSuccess else ArcCyan
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, priorityColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(HudBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.source,
                            fontSize = 9.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "• ${task.deadline}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ScheduleOptimizationView(
    events: List<CalendarEvent>,
    onPrepareBriefing: (CalendarEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            HudSectionHeader(title = "Schedule Timeline", subtitle = "Real-time conflict detection active")
        }

        items(events, key = { it.id }) { event ->
            HudCard(
                borderColor = if (event.hasConflict) StatusError else ArcCyanDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ArcCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = event.time,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ArcCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onPrepareBriefing(event) },
                            colors = ButtonDefaults.buttonColors(containerColor = HudSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Prep Briefing", fontSize = 9.sp, color = ArcCyan)
                        }
                    }

                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Attendees: ${event.attendees} • ${event.location}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HudSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Jarvis Notes: ${event.prepNotes}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
