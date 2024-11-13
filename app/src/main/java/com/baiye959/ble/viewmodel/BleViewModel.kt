package com.baiye959.ble.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.baiye959.ble.data.model.DeviceInfo
import com.baiye959.ble.utils.ECBLE
import com.baiye959.ble.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _lastConnectedDevice = MutableStateFlow<DeviceInfo?>(null)
    val lastConnectedDevice = _lastConnectedDevice.asStateFlow()

    fun loadLastConnectedDevice(context: Context) {
        _lastConnectedDevice.value = PreferencesManager.getLastConnectedDevice(context)
    }

    fun connectDevice(context: Context, device: DeviceInfo) {
        ECBLE.createBLEConnection(context, device.id)
        PreferencesManager.saveLastConnectedDevice(context, device)
        _lastConnectedDevice.value = device
    }

    fun startScan(context: Context) {  // 添加 context 参数
        _isScanning.value = true
        ECBLE.onBluetoothDeviceFound { id, name, mac, rssi ->
            val deviceList = _devices.value.toMutableList()
            val existingDevice = deviceList.find { it.id == id }
            if (existingDevice == null) {
                deviceList.add(DeviceInfo(id, name, mac, rssi))
                _devices.value = deviceList
            }
        }
        ECBLE.startBluetoothDevicesDiscovery(context)  // 传入 context
    }
}