package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.HudCard
import com.example.ui.components.HudStatusBadge
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudBorderBright
import com.example.ui.theme.HudSurface
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HudCopilotScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val lastResponse by viewModel.lastAiResponse.collectAsStateWithLifecycle()
    val inputQuery by viewModel.inputQuery.collectAsStateWithLifecycle()
    val isTtsMuted by viewModel.isTtsMuted.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.voiceEngine.isListening.collectAsStateWithLifecycle()
    val waveLevels by viewModel.voiceEngine.audioWaveLevels.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()

    var attachedContextName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Quick Telemetry Status Ribbon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudStatusBadge(
                label = "LATENCY",
                value = "${telemetry.latencyMs}ms",
                isPositive = telemetry.latencyMs < 600
            )
            HudStatusBadge(
                label = "RAM",
                value = "${telemetry.ramPercentUsed}%"
            )
            HudStatusBadge(
                label = "POWER",
                value = "${telemetry.batteryPercent}%",
                isPositive = telemetry.batteryPercent > 20
            )
            IconButton(
                onClick = { viewModel.toggleMuteTts() },
                modifier = Modifier.size(36.dp).testTag("mute_button")
            ) {
                Icon(
                    imageVector = if (isTtsMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Toggle TTS Audio",
                    tint = if (isTtsMuted) TextMuted else ArcCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Arc Reactor & Waveform Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(HudSurface)
                .border(1.dp, HudBorder, RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ArcReactorCore(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isGenerating = isAiGenerating,
                    onClick = { viewModel.startVoiceInput() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Waveform
                AudioWaveformVisualizer(
                    levels = waveLevels,
                    isActive = isListening || isSpeaking || isAiGenerating
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isAiGenerating) "PROCESSING COGNITIVE STREAM..."
                    else if (isListening) "SPEECH-TO-TEXT STREAMING..."
                    else if (isSpeaking) "J.A.R.V.I.S. DUPLEX SPEECH ACTIVE"
                    else "TAP CORE OR MIC FOR DUPLEX VOICE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isListening) JarvisGold else ArcCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Flow Quick Triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.triggerSarahFlow() },
                colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("sarah_flow_button")
            ) {
                Text("Draft Sarah Q3", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.synthesizeMorningBriefing() },
                colors = ButtonDefaults.buttonColors(containerColor = HudSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("morning_briefing_button")
            ) {
                Text("Briefing", fontSize = 11.sp, color = ArcCyan, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.abortSequence() },
                colors = ButtonDefaults.buttonColors(containerColor = StatusError.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("abort_button")
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = "Abort Sequence",
                    tint = StatusError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ABORT", fontSize = 11.sp, color = StatusError, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Response Stream Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            HudCard(
                modifier = Modifier.fillMaxSize(),
                borderColor = if (isSpeaking) ArcCyan else HudBorder
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "J.A.R.V.I.S. INTELLIGENCE DISPATCH",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ArcCyan
                            )
                            if (isSpeaking) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StatusSuccess)
                                    )
                                    Text("TTS Voicing", fontSize = 10.sp, color = StatusSuccess)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = lastResponse,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextPrimary
                        )
                    }

                    if (isAiGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = ArcCyan
                                )
                                Text(
                                    text = "RAG context query in progress...",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Optional attached multimodal context tag
        AnimatedVisibility(visible = attachedContextName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attached Multimodal Context: $attachedContextName",
                    fontSize = 11.sp,
                    color = JarvisGold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Remove",
                    fontSize = 11.sp,
                    color = StatusError,
                    modifier = Modifier.clickable { attachedContextName = null }
                )
            }
        }

        // Voice & Text Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HudSurface, RoundedCornerShape(12.dp))
                .border(1.dp, HudBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Multimodal Document / Image attachment toggle
            IconButton(
                onClick = {
                    attachedContextName = if (attachedContextName == null) "quarterly_targets_q3.pdf (Vector Embedded)" else null
                },
                modifier = Modifier.size(40.dp).testTag("attach_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach Document Context",
                    tint = if (attachedContextName != null) JarvisGold else TextSecondary
                )
            }

            // Input TextField
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { viewModel.setInputQuery(it) },
                placeholder = {
                    Text(
                        "Command Jarvis or ask about context...",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Voice mic button
            IconButton(
                onClick = { viewModel.startVoiceInput() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isListening) JarvisGold else HudBorder)
                    .testTag("voice_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) Color.Black else ArcCyan
                )
            }

            // Send button
            IconButton(
                onClick = {
                    val prompt = if (attachedContextName != null) {
                        "$inputQuery\n[Context: Using $attachedContextName]"
                    } else inputQuery
                    viewModel.submitPrompt(prompt)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ArcCyan)
                    .testTag("send_command_button"),
                enabled = inputQuery.isNotBlank() || attachedContextName != null
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = Color.Black
                )
            }
        }
    }
}
