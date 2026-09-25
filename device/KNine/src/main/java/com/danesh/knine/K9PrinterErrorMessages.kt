package com.danesh.knine

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate


object K9PrinterErrorMessages {

    object DeviceErrorCode {
        const val DEVICE_OK = 0
        const val DEVICE_OPEN_OK = 4097
        const val DEVICE_NOT_OPEN = 4098
        const val DEVICE_BUSY = 4099
        const val DEVICE_NORESPONSE = 4100
        const val DEVICE_PRINT_DATA_ERROR = 4101
        const val DEVICE_PRINTER_OVER_HEATER = 4102
        const val DEVICE_PRINTER_HEAD_OVER_HEIGH = 4103
        const val DEVICE_PRINTER_OUT_OF_PAPER = 4104
        const val DEVICE_PRINTER_CMD_ERROR = 4105
        const val DEVICE_PRINTER_LOWMEMORY = 4112
        const val DEVICE_PRINTER_UNKNOW_EXCEPTION = 4113
        const val DEVICE_PRINTER_LISTENER_NULL = 4114
        const val DEVICE_PRINTER_LOW_POWER = 4115
        const val DEVICE_PIN_ENTRY_TIMEOUT=8200
    }

    /**
     * پیام کاربرپسند برای خطای چاپ — بدون کد/متن فنی SDK (آن‌ها فقط در لاگ می‌مانند)
     * و به زبانی که کاربر در اپ انتخاب کرده است.
     */
    fun message(context: Context, code: Int): String =
        knownMessage(context, code) ?: generic(context)

    /** پیام مشخص برای کدهای شناخته‌شده؛ برای بقیه null. */
    fun knownMessage(context: Context, code: Int): String? {
        val messageRes = messageResForCode(code) ?: return null
        return localized(context).getString(messageRes)
    }

    /** پیام عمومی «چاپ نشد، دوباره امتحان کنید». */
    fun generic(context: Context): String =
        localized(context).getString(R.string.error_printer)

    fun deviceUnavailable(context: Context): String =
        localized(context).getString(R.string.error_print_device)

    fun noPaper(context: Context): String =
        localized(context).getString(R.string.error_printer_no_paper)

    /**
     * کانتکست با زبان انتخاب‌شده در اپ؛ کانتکست‌های Application/Service زیر اندروید ۱۳
     * ممکن است هنوز زبان سیستم را داشته باشند.
     */
    private fun localized(context: Context): Context {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (appLocales.isEmpty()) return context
        val wanted = appLocales[0] ?: return context
        val current = context.resources.configuration.locales[0]
        if (current == wanted) return context
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList.forLanguageTags(appLocales.toLanguageTags()))
        return runCatching { context.createConfigurationContext(config) }.getOrDefault(context)
    }

    @StringRes
    internal fun messageResForCode(code: Int): Int? = when (code) {
        DeviceErrorCode.DEVICE_NOT_OPEN -> R.string.error_printer_not_open
        DeviceErrorCode.DEVICE_BUSY -> R.string.error_printer_busy
        DeviceErrorCode.DEVICE_NORESPONSE -> R.string.error_printer_no_response
        DeviceErrorCode.DEVICE_PRINT_DATA_ERROR -> R.string.error_printer_data_error
        DeviceErrorCode.DEVICE_PRINTER_OVER_HEATER -> R.string.error_printer_over_heater
        DeviceErrorCode.DEVICE_PRINTER_HEAD_OVER_HEIGH -> R.string.error_printer_head_over_height
        DeviceErrorCode.DEVICE_PRINTER_OUT_OF_PAPER -> R.string.error_printer_no_paper
        DeviceErrorCode.DEVICE_PRINTER_CMD_ERROR -> R.string.error_printer_cmd_error
        DeviceErrorCode.DEVICE_PRINTER_LOWMEMORY -> R.string.error_printer_low_memory
        DeviceErrorCode.DEVICE_PRINTER_UNKNOW_EXCEPTION -> R.string.error_printer_unknown_exception
        DeviceErrorCode.DEVICE_PRINTER_LISTENER_NULL -> R.string.error_printer_listener_null
        DeviceErrorCode.DEVICE_PRINTER_LOW_POWER -> R.string.error_printer_low_power
        DeviceErrorCode.DEVICE_PIN_ENTRY_TIMEOUT -> R.string.pin_entry_timeout
        else -> null
    }
}
