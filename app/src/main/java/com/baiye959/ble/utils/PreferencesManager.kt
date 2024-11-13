package com.baiye959.ble.utils

import android.content.Context
import android.content.SharedPreferences
import com.baiye959.ble.data.model.DeviceInfo

object PreferencesManager {
    private const val PREF_NAME = "ble_preferences"
    private const val KEY_LAST_DEVICE_ID = "last_device_id"
    private const val KEY_LAST_DEVICE_NAME = "last_device_name"
    private const val KEY_LAST_DEVICE_MAC = "last_device_mac"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastConnectedDevice(context: Context, device: DeviceInfo) {
        getPreferences(context).edit().apply {
            putString(KEY_LAST_DEVICE_ID, device.id)
            putString(KEY_LAST_DEVICE_NAME, device.name)
            putString(KEY_LAST_DEVICE_MAC, device.mac)
            apply()
        }
    }

    fun getLastConnectedDevice(context: Context): DeviceInfo? {
        val prefs = getPreferences(context)
        val id = prefs.getString(KEY_LAST_DEVICE_ID, null) ?: return null
        val name = prefs.getString(KEY_LAST_DEVICE_NAME, "") ?: ""
        val mac = prefs.getString(KEY_LAST_DEVICE_MAC, "") ?: ""
        return DeviceInfo(id, name, mac, 0)
    }
}