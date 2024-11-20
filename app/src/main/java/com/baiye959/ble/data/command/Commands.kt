package com.baiye959.ble.data.command

import com.baiye959.ble.data.command.impl.GetDeviceListCommand
import com.baiye959.ble.data.command.impl.RebootCameraCommand
import com.baiye959.ble.data.command.impl.StartCommand
import com.baiye959.ble.data.command.impl.StopCommand
import com.baiye959.ble.data.command.impl.UpdateSelfCalibrationCommand

object Commands {
    private val commandMap = mapOf(
        StartCommand.function to StartCommand,
        StopCommand.function to StopCommand,
        GetDeviceListCommand.function to GetDeviceListCommand,
        UpdateSelfCalibrationCommand.function to UpdateSelfCalibrationCommand,
        RebootCameraCommand.function to RebootCameraCommand
    )

    val all = commandMap.values.map { it.getCommand() }

    fun fromString(function: String) = commandMap[function]
}