package com.baiye959.ble.data.command.impl

import com.baiye959.ble.data.command.base.BaseCommand
import com.baiye959.ble.data.model.ActionType
import com.baiye959.ble.data.model.BleMessage
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.model.MessageType

object StartCommand : BaseCommand {
    override val function = "Start"

    data class Request(
        val arg1: String
    ) : BaseCommand.Request {
        override fun toMap() = mapOf("arg1" to arg1)
    }

    data class Response(
        val result1: String,
        override val isSuccess: Boolean = true
    ) : BaseCommand.Response

    override fun getCommand() = Command(
        name = "开始收集数据",
        description = "启动数据收集过程",
        message = BleMessage(
            type = MessageType.REQ,
            action = ActionType.Control,
            status = function,
            params = Request("default").toMap()
        )
    )
}