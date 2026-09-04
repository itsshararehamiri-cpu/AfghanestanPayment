package com.danesh.afghanestanpayment

import android.content.Context
import androidx.lifecycle.ViewModel
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.isWifiEnabled
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LaunchConnectivityViewModel @Inject constructor(
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = networkConnectivityMonitor.isConnected

    fun refresh() {
        networkConnectivityMonitor.refresh()
    }

    fun isWifiEnabled(): Boolean = isWifiEnabled(context)
}
