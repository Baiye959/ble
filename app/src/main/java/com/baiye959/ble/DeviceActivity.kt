package com.baiye959.ble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.baiye959.ble.ui.screen.DeviceScreen
import com.baiye959.ble.ui.theme.BleTheme
import com.baiye959.ble.viewmodel.DeviceViewModel

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
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理蓝牙连接
        // 使用新的公开清理方法
        viewModel.cleanup()
    }
}