package com.danesh.settings.domain

import com.danesh.api.DeviceConfigurationSummary
import com.danesh.settings.model.InitialConfigurationSummary

fun InitialConfigurationSummary.toDeviceConfigurationSummary(): DeviceConfigurationSummary =
    DeviceConfigurationSummary(
        hardwareSerial = hardwareSerial,
        terminalId = terminalId,
        appVersion = appVersion,
        programDate = programDate,
        merchantId = merchantId,
    )

fun DeviceConfigurationSummary.toInitialConfigurationSummary(): InitialConfigurationSummary =
    InitialConfigurationSummary(
        hardwareSerial = hardwareSerial,
        terminalId = terminalId,
        appVersion = appVersion,
        programDate = programDate,
        merchantId = merchantId,
    )
