package com.danesh.bp.field63

import com.danesh.api.PspDeviceMetadata


object BpField63Formatter {

    fun format(metadata: PspDeviceMetadata): String {
        val serial = metadata.serial.trim()
        val appVersion = metadata.appVersion.trim()
        val imei = metadata.imei.trim()
        val simSerial = metadata.simSerial.trim()
        check(serial.isNotEmpty()) { "BP field 63: DeviceSerial is required" }
        check(appVersion.isNotEmpty()) { "BP field 63: AppVersion is required" }
        return "$serial@$appVersion@$imei@$simSerial"
    }
}

fun PspDeviceMetadata.toBpField63(): String = BpField63Formatter.format(this)
