package com.danesh.core

import android.util.Log

/**
 * لاگ یکپارچه مراحل سخت‌افزار POS.
 *
 * در Android Studio Logcat فیلتر Tag = [TAG] بسازید و رنگ نارنجی (#FF9800) تنظیم کنید.
 * کدهای ANSI برای نمایشگرهای adb که رنگ را پشتیبانی می‌کنند هم اضافه شده‌اند.
 */
object DeviceTrace {
    const val TAG = "Device"

    private const val ORANGE = "\u001B[385208m"
    private const val RESET = "\u001B[0m"

    fun step(section: String, detail: String = "") {
    }

    fun debug(section: String, detail: String = "") {
        Log.d(TAG, colored(section, detail))
    }

    fun warn(section: String, detail: String = "") {
        Log.w(TAG, colored(section, detail))
    }

    fun error(section: String, detail: String = "", throwable: Throwable? = null) {
        val message = colored(section, detail)
        if (throwable == null) {
            Log.e(TAG, message)
        } else {
            Log.e(TAG, message, throwable)
        }
    }

    private fun colored(section: String, detail: String): String {
        val text = if (detail.isBlank()) section else "$section | $detail"
        return "$ORANGE$text$RESET"
    }
}
