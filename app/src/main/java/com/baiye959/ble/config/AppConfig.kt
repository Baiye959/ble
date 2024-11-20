package com.baiye959.ble.config

object AppConfig {
    // 上线消息
    const val CONNECTION_TIMEOUT_MS = 5000L  // 连接超时时间（毫秒）
    const val SKIP_ONLINE_CHECK = true  // 是否跳过上线检查（方便测试，无实际应用场景）

    // 心跳检测
    const val ENABLE_HEARTBEAT = false  // 是否启用心跳检测（此处代码逻辑有问题，只有启动上线检查才能启用心跳检测）
    const val HEARTBEAT_INTERVAL_MS = 30000L  // 心跳发送间隔（毫秒）
    const val HEARTBEAT_TIMEOUT_MS = 5000L  // 心跳超时时间（毫秒）

    // 控制协议
    const val COMMAND_TIMEOUT_MS = 5000L  // 命令响应超时时间（毫秒）
}