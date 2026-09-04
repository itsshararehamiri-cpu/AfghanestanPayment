package com.danesh.knine

import android.content.Context
import android.util.Log
import com.pos.sdk.sys.SystemDevice

private const val TAG = "K9BootAutoStart"
private const val MAIN_ACTIVITY =
    "com.danesh.afghanestanpayment.MainActivity"

/**
 * ثبت اپ در boot service اختصاصی Centerm/K9 — پایدارتر از BOOT_COMPLETED معمولی.
 */
internal fun registerCentermBootService(
    context: Context,
    systemDevice: SystemDevice?,
) {
    if (systemDevice == null) {
        Log.w(TAG, "systemDevice unavailable")
        return
    }
    val packageName = context.packageName
    val addBootService = runCatching {
        systemDevice.javaClass
            .getMethod("addBootService", String::class.java, String::class.java)
            .invoke(systemDevice, packageName, MAIN_ACTIVITY)
        true
    }.getOrDefault(false)

    if (addBootService) return

    runCatching {
        systemDevice.javaClass
            .getMethod(
                "operateBootService",
                Int::class.javaPrimitiveType,
                String::class.java,
                String::class.java,
            )
            .invoke(systemDevice, 1, packageName, MAIN_ACTIVITY)
    }.onFailure { error ->
        Log.w(TAG, "Centerm boot register failed: ${error.message}")
    }
}
