package com.danesh.common.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkConnectivityMonitor {
    val isConnected: StateFlow<Boolean>
    val connectionType: StateFlow<NetworkConnectionType>
    fun refresh()
}
