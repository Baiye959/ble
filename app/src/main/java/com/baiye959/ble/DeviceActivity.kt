package com.baiye959.ble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.baiye959.ble.ui.screen.DeviceScreen
import com.baiye959.ble.ui.theme.BleTheme
import com.baiye959.ble.viewmodel.DeviceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceActivity : ComponentActivity() {
    private val viewModel: DeviceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BleTheme {
                DeviceScreen(
                    viewModel = viewModel,
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }

        // 延迟500ms后发送上线请求，确保蓝牙连接已经建立
        lifecycleScope.launch {
            delay(500)
            viewModel.sendOnlineRequest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }
}