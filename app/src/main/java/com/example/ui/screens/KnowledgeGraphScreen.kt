package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.KnowledgeItem
import com.example.data.entity.MemoryItem
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.HudSectionHeader
import com.example.ui.components.HudStatusBadge
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KnowledgeGraphScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val filteredKnowledge by viewModel.filteredKnowledge.collectAsStateWithLifecycle()
    val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.ragSearchQuery.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Indexed RAG Vault, 1 = Long-Term Memory, 2 = Data Governance
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(16.dp)
    ) {
        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = HudSurface,
            contentColor = ArcCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ArcCyan,
                    height = 2.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("RAG Knowledge Index", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Long-Term Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Data Control", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Search Bar with RAG label
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setRagSearchQuery(it) },
                    placeholder = { Text("Semantic RAG Search (e.g. 'Sarah', 'targets', 'Obsidian')...", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ArcCyan)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rag_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Ingestion categories filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("ALL", "DOCS", "NOTES", "EMAIL", "BOOKMARKS")
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArcCyanDark,
                                selectedLabelColor = Color.White,
                                containerColor = HudSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val displayedItems = if (selectedCategoryFilter == "ALL") {
                    filteredKnowledge
                } else {
                    filteredKnowledge.filter { it.category == selectedCategoryFilter }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        KnowledgeItemCard(
                            item = item,
                            onAskJarvis = {
                                viewModel.submitPrompt("From context in '${item.title}', summarize the key points and implications.")
                                viewModel.setTab(0) // Switch to HUD
                            }
                        )
                    }
                }
            }

            1 -> {
                // Long-Term Memory View
                Column(modifier = Modifier.fillMaxSize()) {
                    HudSectionHeader(
                        title = "Persistent Long-Term Memory",
                        subtitle = "Autonomous memory of preferences, habits, goals & relationships"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allMemories, key = { it.id }) { mem ->
                            MemoryItemCard(memory = mem)
                        }
                    }
                }
            }

            2 -> {
                // Data Governance View (1-click export & instant wipe)
                DataGovernanceView(
                    onExport = { viewModel.exportMemoryGraph() },
                    onWipe = { showWipeConfirmDialog = true },
                    onRestore = { viewModel.restoreSeedData() }
                )
            }
        }
    }

    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            containerColor = HudSurface,
            title = {
                Text(
                    "INSTANT DATA WIPE CONFIRMATION",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = StatusError,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This action permanently erases all locally indexed documents, vector context, persistent memories, and action tasks. Do you authorize this purge?",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Purge All Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun KnowledgeItemCard(
    item: KnowledgeItem,
    onAskJarvis: () -> Unit
) {
    val categoryIcon = when (item.category) {
        "DOCS" -> Icons.Default.Description
        "NOTES" -> Icons.Default.Notes
        "EMAIL" -> Icons.Default.Email
        else -> Icons.Default.Bookmark
    }

    HudCard(borderColor = HudBorder) {
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
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .background(HudSurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Vector Indexed",
                        fontSize = 9.sp,
                        color = StatusSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = item.content,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp,
                maxLines = 3
            )

            // Source Citation Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(HudBackground, RoundedCornerShape(4.dp))
                        .border(0.5.dp, HudBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Source: ${item.sourceUrl}",
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onAskJarvis,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Query Context", fontSize = 9.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MemoryItemCard(memory: MemoryItem) {
    val categoryColor = when (memory.category) {
        "PREFERENCE" -> ArcCyan
        "HABIT" -> StatusSuccess
        "GOAL" -> JarvisGold
        else -> Color(0xFFE056FD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HudBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = HudSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = memory.category,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                Text(
                    text = "Confidence: ${(memory.confidence * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = memory.key,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArcCyan,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = memory.value,
                fontSize = 12.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DataGovernanceView(
    onExport: () -> Unit,
    onWipe: () -> Unit,
    onRestore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HudSectionHeader(
                title = "Data Control & Privacy Plane",
                subtitle = "Local-first vector storage and user data sovereignty"
            )
        }

        item {
            HudCard(borderColor = ArcCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1-CLICK MEMORY GRAPH EXPORT",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyan
                    )
                    Text(
                        text = "Export complete local knowledge graph, long-term memory graph, action tasks, and embeddings into a standardized JSON format.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = onExport,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Memory Graph (JSON)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            HudCard(borderColor = StatusSuccess) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RESTORE SEED KNOWLEDGE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )
                    Text(
                        text = "Re-populate sample confidential Q3 report, Obsidian notes, Sarah email threads, and daily calendar for testing.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = HudSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Default Data", color = StatusSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            HudCard(borderColor = StatusError) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INSTANT ZERO-TRACE DATA WIPE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = StatusError
                    )
                    Text(
                        text = "Instant 1-click irreversible data purge across all Room database entities and caches.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = onWipe,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wipe All Local Data", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
