package com.danesh.api

data class DeviceConfigurationSummary(
    val hardwareSerial: String,
    val terminalId: String,
    val appVersion: String,
    val programDate: String,
    val merchantId: String,
)
