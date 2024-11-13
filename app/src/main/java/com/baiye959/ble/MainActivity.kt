package com.baiye959.ble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baiye959.ble.data.model.DeviceInfo
import com.baiye959.ble.ui.theme.BleTheme
import com.baiye959.ble.viewmodel.BleViewModel
import pub.devrel.easypermissions.EasyPermissions
import com.baiye959.ble.utils.ECBLE
import com.baiye959.ble.utils.PreferencesManager

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {
    private val viewModel: BleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化蓝牙
        ECBLE.openBluetoothAdapter(this)

        // 设置连接状态回调
        ECBLE.onBLEConnectionStateChange { connected, _, _ ->
            if (connected) {
                // 连接成功时保存设备信息并跳转
                viewModel.lastConnectedDevice.value?.let { device ->
                    PreferencesManager.saveLastConnectedDevice(this, device)
                    // 跳转到设备通信界面
                    startActivity(Intent(this, DeviceActivity::class.java))
                }
            }
        }

        setContent {
            BleTheme {
                BleMainScreen(viewModel = viewModel)
            }
        }
    }

    // 处理权限回调
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        ECBLE.onPermissionsGranted(this, requestCode, perms)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        ECBLE.onPermissionsDenied(requestCode, perms)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleMainScreen(
    viewModel: BleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val lastConnectedDevice by viewModel.lastConnectedDevice.collectAsState()

    // 加载上次连接的设备信息
    LaunchedEffect(Unit) {
        viewModel.loadLastConnectedDevice(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE Scanner") },
                actions = {
                    IconButton(onClick = { viewModel.startScan(context) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 上次连接的设备按钮
            lastConnectedDevice?.let { lastDevice ->
                val currentDevice = devices.find { it.id == lastDevice.id }
                if (currentDevice != null) {
                    Button(
                        onClick = { viewModel.connectDevice(context, currentDevice) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("连接上次设备: ${lastDevice.name}")
                    }
                }
            }

            // 设备列表
            LazyColumn {
                items(devices) { device ->
                    DeviceItem(
                        device = device,
                        onClick = { viewModel.connectDevice(context, device) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: DeviceInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name.ifEmpty { "Unknown Device" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MAC: ${device.mac}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "RSSI: ${device.rssi} dBm",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}