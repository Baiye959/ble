package com.baiye959.ble.data.command

import com.baiye959.ble.data.model.BleMessage

data class Command(
    val name: String,
    val description: String,
    val message: BleMessage
)