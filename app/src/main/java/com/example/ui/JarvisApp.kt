package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ExecutiveBriefingScreen
import com.example.ui.screens.HudCopilotScreen
import com.example.ui.screens.KnowledgeGraphScreen
import com.example.ui.screens.SmartHabitatScreen
import com.example.ui.screens.SystemDiagnosticsScreen
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp(
    viewModel: JarvisViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val emailReviewDraft by viewModel.emailReviewDraft.collectAsStateWithLifecycle()
    val exportedMemory by viewModel.exportedMemory.collectAsStateWithLifecycle()
    val feedbackNotice by viewModel.feedbackNotice.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()

    var editableDraftText by remember(emailReviewDraft) {
        mutableStateOf(emailReviewDraft ?: "")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ArcCyan)
                        )
                        Text(
                            text = "J.A.R.V.I.S.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ArcCyan
                        )
                        Box(
                            modifier = Modifier
                                .background(HudSurfaceVariant, RoundedCornerShape(4.dp))
                                .border(0.5.dp, HudBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EDGE+CLOUD AI",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = HudSurface,
                    titleContentColor = ArcCyan
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = HudSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, HudBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "HUD Voice Copilot") },
                    label = { Text("HUD", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArcCyan,
                        indicatorColor = ArcCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_hud")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.DashboardCustomize, contentDescription = "Executive Tasks & Agenda") },
                    label = { Text("Executive", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArcCyan,
                        indicatorColor = ArcCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_executive")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Knowledge Graph & RAG") },
                    label = { Text("RAG Index", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArcCyan,
                        indicatorColor = ArcCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_knowledge")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Smart Habitat IoT") },
                    label = { Text("IoT Mesh", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArcCyan,
                        indicatorColor = ArcCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_iot")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "System Diagnostics") },
                    label = { Text("Terminal", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArcCyan,
                        indicatorColor = ArcCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_terminal")
                )
            }
        },
        containerColor = HudBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Feedback Notice Banner if present
            AnimatedVisibility(visible = feedbackNotice != null) {
                feedbackNotice?.let { notice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ArcCyanDark)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notice,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { viewModel.clearFeedbackNotice() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Main Tab Screen Routing
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> HudCopilotScreen(viewModel = viewModel)
                    1 -> ExecutiveBriefingScreen(viewModel = viewModel)
                    2 -> KnowledgeGraphScreen(viewModel = viewModel)
                    3 -> SmartHabitatScreen(viewModel = viewModel)
                    4 -> SystemDiagnosticsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Key Flow 2 Dialog: Email Review Prompt Before Sending to Sarah
    if (emailReviewDraft != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEmailDraft() },
            containerColor = HudSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = JarvisGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "AUTONOMOUS ACTION: REVIEW DRAFT",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Prompt: 'Find that PDF report on quarterly targets and draft a response to Sarah.'",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "J.A.R.V.I.S.: 'Draft ready, sir. Would you like to review before sending?'",
                        fontSize = 12.sp,
                        color = ArcCyan,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = editableDraftText,
                        onValueChange = { editableDraftText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcCyan,
                            unfocusedBorderColor = HudBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmSendEmailDraft() },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Authorize & Send", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissEmailDraft() }) {
                    Text("Discard", color = TextMuted, fontSize = 11.sp)
                }
            }
        )
    }

    // 1-Click Memory Graph Export Dialog
    if (exportedMemory != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportDialog() },
            containerColor = HudSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(20.dp))
                    Text("MEMORY GRAPH EXPORT (JSON)", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = ArcCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    Text("Full local memory graph, RAG documents, and tasks exported:", fontSize = 11.sp, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF030712), RoundedCornerShape(6.dp))
                            .border(1.dp, HudBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportedMemory ?: "",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusSuccess
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("JARVIS_MEMORY_EXPORT", exportedMemory)
                        clipboard?.setPrimaryClip(clip)
                        viewModel.dismissExportDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy JSON", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExportDialog() }) {
                    Text("Close", color = TextMuted, fontSize = 11.sp)
                }
            }
        )
    }
}
