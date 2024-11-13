package com.baiye959.ble.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.baiye959.ble.utils.ECBLE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class DeviceViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isHexMode = MutableStateFlow(false)
    val isHexMode = _isHexMode.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll = _autoScroll.asStateFlow()

    init {
        ECBLE.onBLECharacteristicValueChange { str, strHex ->
            val timeStr = SimpleDateFormat("[HH:mm:ss,SSS]", Locale.getDefault())
                .format(Date())
            val message = if (_isHexMode.value) {
                "$timeStr ${strHex.replace("(.{2})".toRegex(), "$1 ")}"
            } else {
                "$timeStr $str"
            }
            _messages.value = _messages.value + message
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
            if (data.length > 484) {  // 488 - 4 (为结束符预留空间)
                showToast(context, "最多只能发送242字节")
                return
            }
            if (!Pattern.compile("^[0-9a-fA-F]+$").matcher(data).matches()) {
                showToast(context, "格式错误，只能是0-9、a-f、A-F")
                return
            }

            try {
                // 在HEX数据后添加0D0A（\r\n的十六进制）
                val dataWithCRLF = "{$data}0D0A"
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
            // 确保消息以\r\n结尾
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

    // 添加一个公开的清理方法
    fun cleanup() {
        ECBLE.onBLECharacteristicValueChange { _, _ -> }
        ECBLE.onBLEConnectionStateChange { _, _, _ -> }
        ECBLE.closeBLEConnection()
    }

    override fun onCleared() {
        super.onCleared()
        ECBLE.onBLECharacteristicValueChange { _, _ -> }
        ECBLE.onBLEConnectionStateChange { _, _, _ -> }
        ECBLE.closeBLEConnection()
    }

    private val _isDebugMode = MutableStateFlow(true)
    val isDebugMode = _isDebugMode.asStateFlow()

    fun setDebugMode(enabled: Boolean) {
        _isDebugMode.value = enabled
    }

    // 添加预设命令发送功能
    fun sendPresetCommand(context: Context, command: String) {
        sendMessage(context, command)
    }
}