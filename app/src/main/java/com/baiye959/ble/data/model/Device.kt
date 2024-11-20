package com.baiye959.ble.data.model

data class DeviceInfo(
    val id: String,
    val name: String,
    val mac: String,
    val rssi: Int
)

// 设备状态
enum class DeviceStatus {
    Connecting,     // 连接中
    Rejected,       // 连接被拒绝
    Online,         // 在线
    Offline;        // 离线

    companion object {
        fun fromString(value: String): DeviceStatus? =
            DeviceStatus.entries.find { it.name.equals(value, ignoreCase = true) }
    }
}