package com.danesh.payapp.config

import com.danesh.core.DeviceKeyIndexes
import com.danesh.core.DeviceSettings
import com.danesh.core.DeviceTimeouts
import com.danesh.core.PinEntryPolicy

/**
 * مقادیر پیش‌فرض دستگاه برای هر PSP (برنامه‌نویس تعیین می‌کند).
 * PSPهایی که اندیس را از کاربر می‌گیرند (مثل سداد هنگام بارگذاری کارت کلید) آن را از طریق
 * [com.danesh.core.PspDeviceSettingsOverride] بازنویسی می‌کنند.
 */
object DeviceSettingsDefaults {

    private const val DEFAULT_KEY_INDEX = 16

    /** ANSI X9.19 با zero-pad (همان رفتار قبلی K9). */
    private const val DEFAULT_MAC_ALGORITHM = "TYPE_X919_00"

    private val defaultTimeouts = DeviceTimeouts(
        cardReadMs = 20_000,
        scanMs = 60_000,
        pinpadReadyMs = 30_000L,
    )

    private val defaultPinEntry = PinEntryPolicy(minLength = 4, maxLength = 4)

    fun forPsp(psp: ActivePsp): DeviceSettings = when (psp) {
        ActivePsp.SADAD -> DeviceSettings(
            // سداد: کلیدهای کارت در اسلات C و کلیدهای کاری LOGON در اسلات C+1.
            keyIndexes = DeviceKeyIndexes(
                masterKey = DEFAULT_KEY_INDEX,
                macKey = DEFAULT_KEY_INDEX,
                pinKey = DEFAULT_KEY_INDEX,
                dataKey = DEFAULT_KEY_INDEX,
                pinEntryKey = DEFAULT_KEY_INDEX + 1,
            ),
            timeouts = defaultTimeouts,
            pinEntry = defaultPinEntry,
            macAlgorithm = DEFAULT_MAC_ALGORITHM,
        )

        ActivePsp.BP,
        ActivePsp.HP,
        ActivePsp.FANAVA,
        ActivePsp.AP,
        ActivePsp.PN,
        -> DeviceSettings(
            keyIndexes = DeviceKeyIndexes(
                masterKey = DEFAULT_KEY_INDEX,
                macKey = DEFAULT_KEY_INDEX,
                pinKey = DEFAULT_KEY_INDEX,
                dataKey = DEFAULT_KEY_INDEX,
            ),
            timeouts = defaultTimeouts,
            pinEntry = defaultPinEntry,
            macAlgorithm = DEFAULT_MAC_ALGORITHM,
        )
    }
}
