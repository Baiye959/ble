package com.baiye959.ble.data.command.impl

import com.baiye959.ble.data.command.base.BaseCommand
import com.baiye959.ble.data.model.ActionType
import com.baiye959.ble.data.model.BleMessage
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.model.MessageType

object UpdateSelfCalibrationCommand : BaseCommand {
    override val function = "updateSelfCalibration"

    data class Request(
        val dummy: String = ""
    ) : BaseCommand.Request {
        override fun toMap() = emptyMap<String, Any>()
    }

    data class Response(
        override val isSuccess: Boolean = true
    ) : BaseCommand.Response

    override fun getCommand() = Command(
        name = "更新自校准",
        description = "更新设备自校准参数",
        message = BleMessage(
            type = MessageType.REQ,
            action = ActionType.Control,
            status = function,
            params = Request().toMap()
        )
    )
}