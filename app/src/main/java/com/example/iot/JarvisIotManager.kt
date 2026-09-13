package com.example.iot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IotDevice(
    val id: String,
    val name: String,
    val type: String, // "LIGHT", "CLIMATE", "LOCK", "CAMERA"
    val room: String,
    val state: String, // "ON", "OFF", "LOCKED", "UNLOCKED", "STREAMING"
    val metricValue: String, // e.g., "75%", "21.5°C", "Motion: Clear", "Matter v1.3"
    val isEnabled: Boolean
)

data class SpatialRoom(
    val id: String,
    val name: String,
    val sensorType: String, // "mmWave Radar", "Bluetooth RSSI", "Vision Array"
    val isUserPresent: Boolean,
    val rssiDb: Int,
    val audioRoutingActive: Boolean
)

class JarvisIotManager {

    private val _devices = MutableStateFlow(
        listOf(
            IotDevice(
                id = "light_office",
                name = "Office Arc Halo Light",
                type = "LIGHT",
                room = "Main Office",
                state = "ON",
                metricValue = "85% | 4000K Focus",
                isEnabled = true
            ),
            IotDevice(
                id = "climate_main",
                name = "Smart HVAC Climate",
                type = "CLIMATE",
                room = "Office & Lab",
                state = "ON",
                metricValue = "21.5°C (Target 22°C)",
                isEnabled = true
            ),
            IotDevice(
                id = "lock_perimeter",
                name = "Workshop Biometric Lock",
                type = "LOCK",
                room = "Workshop",
                state = "LOCKED",
                metricValue = "Secured (Matter)",
                isEnabled = true
            ),
            IotDevice(
                id = "camera_lab",
                name = "Security Vision Feed",
                type = "CAMERA",
                room = "Outer Lab",
                state = "STREAMING",
                metricValue = "1080p60 • Motion: Clear",
                isEnabled = true
            )
        )
    )
    val devices: StateFlow<List<IotDevice>> = _devices.asStateFlow()

    private val _rooms = MutableStateFlow(
        listOf(
            SpatialRoom(
                id = "room_office",
                name = "Primary Command Office",
                sensorType = "mmWave Radar",
                isUserPresent = true,
                rssiDb = -42,
                audioRoutingActive = true
            ),
            SpatialRoom(
                id = "room_lab",
                name = "Hardware Workshop / Lab",
                sensorType = "Bluetooth RSSI",
                isUserPresent = false,
                rssiDb = -79,
                audioRoutingActive = false
            ),
            SpatialRoom(
                id = "room_living",
                name = "Living Quarters",
                sensorType = "Matter Sensor",
                isUserPresent = false,
                rssiDb = -88,
                audioRoutingActive = false
            )
        )
    )
    val rooms: StateFlow<List<SpatialRoom>> = _rooms.asStateFlow()

    fun toggleDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val newState = when (dev.type) {
                    "LIGHT" -> if (dev.state == "ON") "OFF" else "ON"
                    "LOCK" -> if (dev.state == "LOCKED") "UNLOCKED" else "LOCKED"
                    "CLIMATE" -> if (dev.state == "ON") "ECO" else "ON"
                    else -> if (dev.state == "STREAMING") "STANDBY" else "STREAMING"
                }
                dev.copy(
                    state = newState,
                    isEnabled = newState != "OFF" && newState != "STANDBY"
                )
            } else dev
        }
    }

    fun selectActiveRoom(roomId: String) {
        _rooms.value = _rooms.value.map { room ->
            room.copy(
                isUserPresent = (room.id == roomId),
                audioRoutingActive = (room.id == roomId)
            )
        }
    }
}
