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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.iot.IotDevice
import com.example.iot.SpatialRoom
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
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartHabitatScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.iotDevices.collectAsStateWithLifecycle()
    val rooms by viewModel.spatialRooms.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Matter Bridge Status Header
        item {
            HudCard(borderColor = ArcCyan) {
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
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = ArcCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "HOME ASSISTANT / MATTER 1.3 MESH",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ArcCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hub: 192.168.1.100 • Thread / Zigbee • 4 Nodes Active",
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
                            text = "MESH ONLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Spatial Awareness & Audio Routing
        item {
            HudSectionHeader(
                title = "Spatial Awareness & Audio Routing",
                subtitle = "mmWave radar & Bluetooth RSSI tracking user location"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rooms.forEach { room ->
                    SpatialRoomCard(
                        room = room,
                        onSelect = { viewModel.iotManager.selectActiveRoom(room.id) }
                    )
                }
            }
        }

        // IoT Hardware Node Controls
        item {
            HudSectionHeader(
                title = "Environment Hardware Control Plane",
                subtitle = "Lighting, climate, locks & surveillance"
            )
        }

        items(devices, key = { it.id }) { device ->
            IotDeviceCard(
                device = device,
                onToggle = { viewModel.iotManager.toggleDevice(device.id) }
            )
        }
    }
}

@Composable
private fun SpatialRoomCard(
    room: SpatialRoom,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(
                1.dp,
                if (room.isUserPresent) ArcCyan else HudBorder,
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (room.isUserPresent) HudSurfaceVariant else HudSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = if (room.isUserPresent) ArcCyan else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = room.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (room.isUserPresent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                        }
                    }
                    Text(
                        text = "Sensor: ${room.sensorType} (${room.rssiDb} dBm)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        if (room.audioRoutingActive) ArcCyan.copy(alpha = 0.2f) else HudBorder.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (room.audioRoutingActive) ArcCyan else HudBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = if (room.audioRoutingActive) ArcCyan else TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (room.audioRoutingActive) "AUDIO ROUTED" else "STANDBY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (room.audioRoutingActive) ArcCyan else TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun IotDeviceCard(
    device: IotDevice,
    onToggle: () -> Unit
) {
    val deviceIcon = when (device.type) {
        "LIGHT" -> Icons.Default.Lightbulb
        "CLIMATE" -> Icons.Default.AcUnit
        "LOCK" -> if (device.state == "LOCKED") Icons.Default.Lock else Icons.Default.LockOpen
        else -> Icons.Default.Videocam
    }

    val stateColor = when (device.state) {
        "ON", "LOCKED", "STREAMING" -> StatusSuccess
        "ECO" -> JarvisGold
        else -> StatusError
    }

    HudCard(
        borderColor = if (device.isEnabled) ArcCyanDark else HudBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = null,
                    tint = stateColor,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = device.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${device.room} • ${device.metricValue}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = device.state,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = stateColor,
                    fontFamily = FontFamily.Monospace
                )
                Switch(
                    checked = device.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyanDark,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = HudSurfaceVariant
                    )
                )
            }
        }
    }
}
