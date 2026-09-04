package com.danesh.bp.time

import com.danesh.common.RawMessage
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import com.danesh.engine.HostTimeSynchronizer
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpHostTimeSynchronizer @Inject constructor(
    private val device: Device,
) : HostTimeSynchronizer {

    override suspend fun syncFromResponse(response: RawMessage) {
        val iso = response as? IsoMessage ?: return
        val field7 = iso.transmissionDateTime.trim()
        if (field7.length != FIELD7_LENGTH || !field7.all { it.isDigit() }) {
            return
        }
        DeviceTrace.step("HostTime", "sync system time from field7=$field7")
        device.setDateTime(field7)
    }

    private companion object {
        private const val FIELD7_LENGTH = 12
    }
}
