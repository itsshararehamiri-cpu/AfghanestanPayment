package com.danesh.sadad.device

import com.danesh.api.PspDeviceMetadata
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.core.Device
import com.danesh.core.DeviceDefaults
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadDeviceMetadataProvider @Inject constructor(
    private val device: Device,
    private val appVersionProvider: AppVersionProvider,
    private val contextProvider: TransactionContextProvider,
) : PspDeviceMetadataProvider {

    override suspend fun metadata(): PspDeviceMetadata {
        return PspDeviceMetadata(
            serial = deviceSerial(),
            appVersion = appVersionProvider.versionName(),
        )
    }

    override suspend fun deviceSerial(): String {
        return contextProvider.getTerminalConfig().deviceSerial
            .ifBlank { device.getSerial() }
            .ifBlank { DeviceDefaults.SERIAL }
    }
}
