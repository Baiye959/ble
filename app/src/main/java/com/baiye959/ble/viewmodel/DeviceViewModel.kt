package com.baiye959.ble.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baiye959.ble.config.AppConfig
import com.baiye959.ble.data.command.Command
import com.baiye959.ble.data.command.Commands
import com.baiye959.ble.data.model.*
import com.baiye959.ble.utils.ECBLE
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class DeviceViewModel : ViewModel() {
    private val _currentMode = MutableStateFlow(DeviceScreenMode.User)
    val currentMode = _currentMode.asStateFlow()

    private val _deviceUiState = MutableStateFlow(DeviceUiState())
    val deviceUiState = _deviceUiState.asStateFlow()
    val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isHexMode = MutableStateFlow(false)
    val isHexMode = _isHexMode.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll = _autoScroll.asStateFlow()

    private var connectionTimeoutJob: Job? = null
    private var currentConnectionId: String? = null
    private var commandTimeoutJob: Job? = null
    private var currentCommandFunction: String? = null

    init {
        // 初始化可用命令列表
        _deviceUiState.value = _deviceUiState.value.copy(
            availableCommands = Commands.all
        )

        ECBLE.onBLECharacteristicValueChange { str, strHex ->
            val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                .format(Date())
            val message = if (_isHexMode.value) {
                "$timeStr ${strHex.replace("(.{2})".toRegex(), "$1 ")}"
            } else {
                "$timeStr $str"
            }
            _messages.value = _messages.value + message

            processMessage(str)
        }
    }

    fun sendOnlineRequest() {
        if (AppConfig.SKIP_ONLINE_CHECK) {
            _deviceUiState.value = _deviceUiState.value.copy(
                deviceStatus = DeviceStatus.Online
            )
            connectionTimeoutJob?.cancel()
        } else viewModelScope.launch {
            currentConnectionId = Random().nextInt(100000).toString()

            _deviceUiState.value = _deviceUiState.value.copy(
                deviceStatus = DeviceStatus.Connecting
            )

            val message = BleMessage(
                type = MessageType.REQ,
                action = ActionType.Connect,
                status = "Wait",
                params = mapOf("id" to currentConnectionId!!)
            )

            val messageStr = message.toMessageString().let {
                if (!it.endsWith("\r\n")) it + "\r\n" else it
            }

            sendMessage(messageStr)

            connectionTimeoutJob?.cancel()
            connectionTimeoutJob = launch {
                delay(AppConfig.CONNECTION_TIMEOUT_MS)
                if (_deviceUiState.value.deviceStatus == DeviceStatus.Connecting) {
                    _deviceUiState.value = _deviceUiState.value.copy(
                        deviceStatus = DeviceStatus.Offline
                    )
                }
            }
        }
    }

    fun sendMessage(context: Context, text: String) {
        if (_isHexMode.value) {
            val data = text.replace(" ", "").replace("\r", "").replace("\n", "")

            if (data.isEmpty()) {
                showToast(context, "请输入要发送的数据")
                return
            }
            if (data.length % 2 != 0) {
                showToast(context, "长度错误，长度只能是双数")
                return
            }
            if (data.length > 484) {
                showToast(context, "最多只能发送242字节")
                return
            }
            if (!Pattern.compile("^[0-9a-fA-F]+$").matcher(data).matches()) {
                showToast(context, "格式错误，只能是0-9、a-f、A-F")
                return
            }

            try {
                val dataWithCRLF = "${data}0D0A"
                ECBLE.writeBLECharacteristicValue(dataWithCRLF, true)
                val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                    .format(Date())
                _messages.value = _messages.value + "$timeStr [发送] $dataWithCRLF"
            } catch (e: Exception) {
                showToast(context, "发送失败: ${e.message}")
            }
        } else {
            if (text.isEmpty()) {
                showToast(context, "请输入要发送的数据")
                return
            }
            val tempSendData = text.replace("\n", "\r\n").let {
                if (!it.endsWith("\r\n")) it + "\r\n" else it
            }

            if (tempSendData.length > 244) {
                showToast(context, "最多只能发送244字节")
                return
            }

            try {
                ECBLE.writeBLECharacteristicValue(tempSendData, false)
                val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                    .format(Date())
                _messages.value = _messages.value + "$timeStr [发送] $tempSendData"
            } catch (e: Exception) {
                showToast(context, "发送失败: ${e.message}")
            }
        }
    }

    private fun sendMessage(text: String) {
        try {
            val tempSendData = text.replace("\n", "\r\n").let {
                if (!it.endsWith("\r\n")) it + "\r\n" else it
            }

            val bytes = tempSendData.toByteArray()
            android.util.Log.d("ble-send", "Sending message: $tempSendData (${bytes.size} bytes)")
            android.util.Log.d("ble-send", "Hex: ${bytes.joinToString("") { "%02X".format(it) }}")

            ECBLE.writeBLECharacteristicValue(tempSendData, false)
            val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                .format(Date())
            _messages.value = _messages.value + "$timeStr [发送] $tempSendData"
        } catch (e: Exception) {
            android.util.Log.e("ble-send", "Send failed", e)
        }
    }

    fun setScreenMode(mode: DeviceScreenMode) {
        _currentMode.value = mode
    }

    fun processMessage(message: String) {
        val bleMessage = BleMessage.fromMessageString(message)
        bleMessage?.let {
            when (it.action) {
                ActionType.Connect -> handleConnectMessage(it)
                ActionType.Check -> handleCheckMessage(it)
                ActionType.Control -> handleControlMessage(it)
                ActionType.Status -> {
                    Log.d("ble-receive", "这是一条状态信息")
                    handleStatusMessage(it)
                }
            }
        }
    }

    private fun handleConnectMessage(message: BleMessage) {
        if (message.type == MessageType.RES &&
            message.params["id"] == currentConnectionId
        ) {
            connectionTimeoutJob?.cancel()

            val status = when (message.status) {
                "1" -> {
                    startHeartbeat()  // 设备在线时启动心跳检测
                    DeviceStatus.Online
                }
                "0" -> DeviceStatus.Rejected
                else -> DeviceStatus.Offline
            }

            _deviceUiState.value = _deviceUiState.value.copy(
                deviceStatus = status
            )
        }
    }

    private fun handleControlMessage(message: BleMessage) {
        if (message.type == MessageType.RES) {
            if (currentCommandFunction != null && message.status == currentCommandFunction) {
                commandTimeoutJob?.cancel()

                if (_currentMode.value == DeviceScreenMode.User) {
                    val responseText = when (message.status) {
                        "START" -> "开始收集数据: ${message.params["result1"]}"
                        "STOP" -> "停止收集数据: ${message.params["result1"]}, ${message.params["result2"]}"
                        else -> "命令执行完成: ${message.params}"
                    }
                    _toastMessage.value = responseText
                }

                currentCommandFunction = null
                _context = null
            }
        }
    }

    private fun handleStatusMessage(message: BleMessage) {
        when (message.status) {
            "get_current_fps" -> {
                try {
                    val fps = message.params["value"]?.toString() ?: "0"
                    _deviceUiState.value = _deviceUiState.value.copy(
                        getCurrentFps = fps
                    )
                    Log.i("ble-receive", "更新getCurrentFps为$fps")
                } catch (e: Exception) {
                    Log.e("DeviceViewModel", "Parse fps failed", e)
                }
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun setHexMode(enabled: Boolean) {
        _isHexMode.value = enabled
    }

    fun setAutoScroll(enabled: Boolean) {
        _autoScroll.value = enabled
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun sendCommand(context: Context, command: Command) {
        if (_deviceUiState.value.deviceStatus != DeviceStatus.Online) {
            showToast(context, "设备未连接")
            return
        }

        _context = context  // 保存 context 用于显示响应结果
        currentCommandFunction = command.message.status
        sendMessage(context, command.message.toMessageString())

        commandTimeoutJob?.cancel()
        commandTimeoutJob = viewModelScope.launch {
            delay(AppConfig.COMMAND_TIMEOUT_MS)
            if (currentCommandFunction != null) {
                if (_currentMode.value == DeviceScreenMode.User) {
                    showToast(context, "命令响应超时")
                }
                currentCommandFunction = null
                _context = null
            }
        }
    }

    // 添加 context 变量用于显示 Toast
    private var _context: Context? = null

    fun cleanup() {
        connectionTimeoutJob?.cancel()
        commandTimeoutJob?.cancel()
        currentConnectionId = null
        currentCommandFunction = null
        _context = null
        ECBLE.onBLECharacteristicValueChange { _, _ -> }
        ECBLE.onBLEConnectionStateChange { _, _, _ -> }
        ECBLE.closeBLEConnection()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    private var heartbeatJob: Job? = null
    private var heartbeatTimeoutJob: Job? = null
    private var lastHeartbeatTimestamp: Long = 0

    private fun startHeartbeat() {
        if (!AppConfig.ENABLE_HEARTBEAT) return

        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                if (_deviceUiState.value.deviceStatus == DeviceStatus.Online) {
                    lastHeartbeatTimestamp = System.currentTimeMillis() * 1000 // 转换为微秒
                    val message = BleMessage(
                        type = MessageType.REQ,
                        action = ActionType.Check,
                        status = "Wait",
                        params = mapOf("timestamp" to lastHeartbeatTimestamp.toString())
                    )

                    val messageStr = message.toMessageString() + "\r\n"
                    try {
                        ECBLE.writeBLECharacteristicValue(messageStr, false)
                        val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                            .format(Date())
                        _messages.value = _messages.value + "$timeStr [发送] $messageStr"
                    } catch (e: Exception) {
                        android.util.Log.e("heartbeat", "Send failed", e)
                    }

                    startHeartbeatTimeout()
                }
                delay(AppConfig.HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun startHeartbeatTimeout() {
        heartbeatTimeoutJob?.cancel()
        heartbeatTimeoutJob = viewModelScope.launch {
            delay(AppConfig.HEARTBEAT_TIMEOUT_MS)
            if (_deviceUiState.value.deviceStatus == DeviceStatus.Online) {
                _deviceUiState.value = _deviceUiState.value.copy(
                    deviceStatus = DeviceStatus.Offline
                )
                heartbeatJob?.cancel()
            }
        }
    }

    private fun handleCheckMessage(message: BleMessage) {
        if (message.type == MessageType.RES) {
            val timestamp = try {
                message.params["timestamp"]?.let { java.lang.Long.parseLong(it as String) }
            } catch (e: NumberFormatException) {
                null
            }

            if (timestamp == lastHeartbeatTimestamp) {
                heartbeatTimeoutJob?.cancel()
                when (message.status) {
                    "1" -> { /* 保持在线状态 */ }
                    "0" -> {
                        _deviceUiState.value = _deviceUiState.value.copy(
                            deviceStatus = DeviceStatus.Rejected
                        )
                        heartbeatJob?.cancel()
                    }
                }
            }
        }
    }
}