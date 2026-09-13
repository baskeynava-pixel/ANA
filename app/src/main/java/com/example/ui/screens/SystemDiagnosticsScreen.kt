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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.entity.SystemLog
import com.example.diagnostics.SystemTelemetry
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.HudSectionHeader
import com.example.ui.components.HudStatusBadge
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SystemDiagnosticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val logs by viewModel.systemLogs.collectAsStateWithLifecycle()

    var terminalCmd by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Health Banner
        item {
            HudCard(
                borderColor = if (telemetry.systemStatus.contains("NOMINAL")) ArcCyan else StatusWarning
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ArcCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SYSTEM INTEGRITY: ${telemetry.systemStatus}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ArcCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Thermal: ${telemetry.thermalStatus} (${telemetry.batteryTempCelsius}°C) • Network: ${telemetry.networkType} (${telemetry.latencyMs}ms)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(StatusSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, StatusSuccess, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SECURE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Hardware Gauges (RAM, Battery, Storage)
        item {
            HudSectionHeader(title = "Hardware Telemetry (Real-Time)", subtitle = "Android Hardware & Kernel Monitors")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RAM Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HudBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HudSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                            Text("RAM USAGE", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "${telemetry.ramPercentUsed}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${telemetry.ramUsedMb}MB / ${telemetry.ramTotalMb}MB",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        LinearProgressIndicator(
                            progress = { telemetry.ramPercentUsed / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = ArcCyan,
                            trackColor = HudSurfaceVariant
                        )
                    }
                }

                // Battery Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HudBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HudSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(16.dp))
                            Text("BATTERY", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "${telemetry.batteryPercent}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (telemetry.isCharging) "Charging • ${telemetry.batteryTempCelsius}°C" else "Discharging • ${telemetry.batteryTempCelsius}°C",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        LinearProgressIndicator(
                            progress = { telemetry.batteryPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = JarvisGold,
                            trackColor = HudSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            // Storage & Network Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Storage Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HudBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HudSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                            Text("STORAGE", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = String.format(Locale.US, "%.1f GB Free", telemetry.storageFreeGb),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = String.format(Locale.US, "Total: %.1f GB", telemetry.storageTotalGb),
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Network Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HudBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HudSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                            Text("NETWORK", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = telemetry.networkType,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ping Latency: ${telemetry.latencyMs}ms",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Terminal Console & Automation Command Runner
        item {
            HudSectionHeader(
                title = "Automation Terminal & Shell",
                subtitle = "Subroutines: sys_diag, net_check, sync_graph, mem_clean, iot_status, abort"
            )
        }

        item {
            // Quick Command Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val commands = listOf("sys_diag", "net_check", "sync_graph", "mem_clean", "iot_status", "abort")
                items(commands) { cmd ->
                    Button(
                        onClick = {
                            terminalCmd = cmd
                            viewModel.executeTerminalCommand(cmd)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cmd == "abort") StatusError.copy(alpha = 0.2f) else HudSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (cmd == "abort") StatusError else HudBorder
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            cmd,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (cmd == "abort") StatusError else ArcCyan
                        )
                    }
                }
            }
        }

        item {
            // Command Input Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HudSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = ArcCyan,
                    modifier = Modifier.size(18.dp)
                )

                OutlinedTextField(
                    value = terminalCmd,
                    onValueChange = { terminalCmd = it },
                    placeholder = { Text("jarvis@edge:~$ enter command...", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = ArcCyan,
                        unfocusedTextColor = ArcCyan
                    ),
                    modifier = Modifier.weight(1f).testTag("terminal_input_field"),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        viewModel.executeTerminalCommand(terminalCmd)
                        terminalCmd = ""
                    },
                    modifier = Modifier.size(36.dp).testTag("run_terminal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Execute Command",
                        tint = ArcCyan
                    )
                }
            }
        }

        // Execution Logs Feed
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(1.dp, HudBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text(
                            text = "[J.A.R.V.I.S. TERMINAL OUTPUT - KERNEL LOG STREAM]",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                    }

                    items(logs, key = { it.id }) { log ->
                        TerminalLogLine(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalLogLine(log: SystemLog) {
    val timeStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.format(Date(log.timestamp))
    }

    val statusColor = when (log.status) {
        "SUCCESS" -> StatusSuccess
        "WARNING" -> JarvisGold
        else -> StatusError
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[$timeStr]",
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "> ${log.command}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ArcCyan,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "[${log.status}]",
                fontSize = 9.sp,
                color = statusColor,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = log.output,
            fontSize = 11.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
