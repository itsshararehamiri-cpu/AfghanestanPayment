package com.danesh.sadad.device

import com.danesh.core.DeviceKeyIndexes
import com.danesh.core.PspDeviceSettingsOverride
import com.danesh.sadad.key.SadadWorkingMacState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * سداد اندیس کلید را از کاربر (بارگذاری کارت کلید) می‌گیرد: کلیدهای کارت در اسلات C و
 * کلیدهای کاری LOGON در C+1؛ رمز کارت با کلید PIN کاری (C+1) گرفته می‌شود.
 */
@Singleton
class SadadDeviceSettingsOverride @Inject constructor(
    private val workingMacState: SadadWorkingMacState,
) : PspDeviceSettingsOverride {

    override fun keyIndexes(defaults: DeviceKeyIndexes): DeviceKeyIndexes {
        val cardIndex = workingMacState.keyIndex()
        return defaults.copy(
            masterKey = cardIndex,
            macKey = cardIndex,
            pinKey = cardIndex,
            dataKey = cardIndex,
            bootstrapMasterKey = cardIndex,
            bootstrapMacKey = cardIndex,
            transportKey = cardIndex,
            pinEntryKey = workingMacState.workingKeyIndex(),
        )
    }
}
