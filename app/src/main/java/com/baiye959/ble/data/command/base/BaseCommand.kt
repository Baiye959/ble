package com.baiye959.ble.data.command.base

import com.baiye959.ble.data.command.Command


interface BaseCommand {
    // 命令类型
    val function: String

    // 请求参数基类
    interface Request {
        fun toMap(): Map<String, Any>
    }

    // 响应结果基类
    interface Response {
        val isSuccess: Boolean
    }

    // 获取预定义命令
    fun getCommand(): Command
}