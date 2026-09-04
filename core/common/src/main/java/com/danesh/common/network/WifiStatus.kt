package com.danesh.common.network

import android.content.Context
import android.net.wifi.WifiManager

fun isWifiEnabled(context: Context): Boolean {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        ?: return false
    return wifiManager.isWifiEnabled
}
