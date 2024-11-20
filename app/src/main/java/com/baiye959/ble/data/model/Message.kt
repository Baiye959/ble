package com.baiye959.ble.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// 消息类型
enum class MessageType {
    REQ, RES
}

// 动作类型
enum class ActionType {
    Connect,    // 连接相关
    Check,      // 状态检查
    Control;    // 控制命令

    companion object {
        fun fromString(value: String): ActionType? =
            ActionType.entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

// 消息数据类
data class BleMessage(
    val type: MessageType,
    val action: ActionType,
    val status: String,
    val params: Map<String, Any> = emptyMap()
) {
    companion object {
        private val gson = Gson()

        fun fromMessageString(message: String): BleMessage? {
            try {
                val parts = message.split(" ", limit = 2)
                val command = parts[0].split(":")

                if (command.size != 3) return null

                val params = if (parts.size > 1) {
                    // 使用 Gson 解析 JSON 字符串为 Map
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    gson.fromJson<Map<String, Any>>(parts[1], mapType)
                } else {
                    emptyMap()
                }

                return BleMessage(
                    type = MessageType.valueOf(command[0]),
                    action = ActionType.fromString(command[1]) ?: return null,
                    status = command[2],
                    params = params
                )
            } catch (e: Exception) {
                return null
            }
        }
    }

    fun toMessageString(): String {
        val paramsJson = if (params.isEmpty()) "" else " ${gson.toJson(params)}"
        return "${type}:${action}:${status}${paramsJson}"
    }
}