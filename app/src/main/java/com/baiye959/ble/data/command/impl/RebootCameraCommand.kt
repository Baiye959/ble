package com.baiye959.ble.data.command.impl

import com.baiye959.ble.data.command.base.BaseCommand
import com.baiye959.ble.data.model.ActionType
import com.baiye959.ble.data.model.BleMessage
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.model.MessageType

object RebootCameraCommand : BaseCommand {
    override val function = "rebootCamera"

    data class Request(
        val dummy: String = ""
    ) : BaseCommand.Request {
        override fun toMap() = emptyMap<String, Any>()
    }

    data class Response(
        override val isSuccess: Boolean = true
    ) : BaseCommand.Response

    override fun getCommand() = Command(
        name = "重启相机",
        description = "重新启动相机设备",
        message = BleMessage(
            type = MessageType.REQ,
            action = ActionType.Control,
            status = function,
            params = Request().toMap()
        )
    )
}