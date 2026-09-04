package com.danesh.settings.model

import com.danesh.common.connection.ConnectionChannel

data class MainServerSettingsUiState(
    val connectionType: ConnectionChannel = ConnectionChannel.WIFI,
    val serverIp: String = "",
    val serverPort: String = "",
    val tmsIp: String = "",
    val tmsPort: String = "",
    val idleTimeMinutes: String = "",
    val serverIpError: String? = null,
    val serverPortError: String? = null,
    val tmsIpError: String? = null,
    val tmsPortError: String? = null,
    val idleTimeError: String? = null,
    val savedSuccessfully: Boolean = false,
    val validationSummary: String? = null,
    val navigateToWifiSelection: Boolean = false,
)
