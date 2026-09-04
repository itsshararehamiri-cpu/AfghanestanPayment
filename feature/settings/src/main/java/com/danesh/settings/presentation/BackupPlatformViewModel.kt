package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.common.connection.ConnectionChannel
import com.danesh.common.connection.ConnectionEndpointResolver
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.NetworkConnectionType
import com.danesh.settings.model.BackupPlatformUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class BackupPlatformViewModel @Inject constructor(
    private val endpointResolver: ConnectionEndpointResolver,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<BackupPlatformUiState> = _uiState.asStateFlow()

    fun refresh() {
        networkConnectivityMonitor.refresh()
        _uiState.value = loadState()
    }

    fun setBackupEnabled(enabled: Boolean) {
        val channel = _uiState.value.alternateBackupChannel ?: return
        endpointResolver.setBackupEnabledFor(channel, enabled)
        _uiState.update { it.copy(backupEnabled = enabled) }
    }

    private fun loadState(): BackupPlatformUiState {
        networkConnectivityMonitor.refresh()
        val alternateChannel = endpointResolver.alternateBackupChannelForUi()
        val backupEnabled = alternateChannel?.let(endpointResolver::isBackupEnabledFor) ?: false
        return BackupPlatformUiState(
            connectionType = toNetworkType(endpointResolver.primaryChannelForTransaction()),
            dualChannelConfigured = endpointResolver.isDualChannelConfigured(),
            alternateBackupChannel = alternateChannel,
            backupEnabled = backupEnabled,
        )
    }

    private fun toNetworkType(channel: ConnectionChannel): NetworkConnectionType = when (channel) {
        ConnectionChannel.WIFI -> NetworkConnectionType.WIFI
        ConnectionChannel.GPRS -> NetworkConnectionType.GPRS
    }
}
