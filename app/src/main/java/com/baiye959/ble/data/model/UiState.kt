package com.baiye959.ble.data.model

import com.baiye959.ble.data.command.Command

enum class DeviceScreenMode {
    User,   // 用户界面
    Debug   // 调试界面
}

data class DeviceUiState(
    val deviceStatus: DeviceStatus = DeviceStatus.Offline,
    val availableCommands: List<Command> = emptyList()
)

