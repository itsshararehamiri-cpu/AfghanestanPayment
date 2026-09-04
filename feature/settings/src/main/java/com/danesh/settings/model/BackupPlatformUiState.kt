package com.danesh.settings.model

import com.danesh.common.connection.ConnectionChannel
import com.danesh.common.network.NetworkConnectionType

data class BackupPlatformUiState(
    val connectionType: NetworkConnectionType = NetworkConnectionType.NONE,
    val dualChannelConfigured: Boolean = false,
    val alternateBackupChannel: ConnectionChannel? = null,
    val backupEnabled: Boolean = false,
)
