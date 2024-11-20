package com.baiye959.ble.data.command.impl

import com.baiye959.ble.data.command.base.BaseCommand
import com.baiye959.ble.data.model.ActionType
import com.baiye959.ble.data.model.BleMessage
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.model.MessageType

object StopCommand : BaseCommand {
    override val function = "Stop"

    data class Request(
        val arg1: String,
        val arg2: Int
    ) : BaseCommand.Request {
        override fun toMap() = mapOf(
            "arg1" to arg1,
            "arg2" to arg2
        )
    }

    data class Response(
        val result1: String,
        val result2: Int,
        override val isSuccess: Boolean = true
    ) : BaseCommand.Response

    override fun getCommand() = Command(
        name = "停止收集数据",
        description = "停止数据收集过程",
        message = BleMessage(
            type = MessageType.REQ,
            action = ActionType.Control,
            status = function,
            params = Request("default", 0).toMap()
        )
    )
}