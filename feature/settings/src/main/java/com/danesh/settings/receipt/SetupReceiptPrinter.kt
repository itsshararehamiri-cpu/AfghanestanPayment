package com.danesh.settings.receipt

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.settings.R
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.KeyLoadingKcvSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * چاپ رسیدهای راه‌اندازی پایانه: KCV کلیدها و مشخصات پایانه/پذیرنده.
 * متدهای print در صورت خطای چاپ (مثلاً نبود کاغذ) پیام خطا را برمی‌گردانند، در غیر این صورت null.
 */
@Singleton
class SetupReceiptPrinter @Inject constructor(
    private val device: Device,
    private val contextProvider: TransactionContextProvider,
    private val appVersionProvider: AppVersionProvider,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val receiptPspBrandProvider: ReceiptPspBrandProvider,
    private val localePreferences: LocalePreferences,
    @ApplicationContext private val appContext: Context,
) {

    fun buildConfigurationSummary(): InitialConfigurationSummary {
        val serial = runCatching { device.getSerial() }.getOrElse { "" }
        val terminalConfig = runCatching { contextProvider.getTerminalConfig() }.getOrNull()
        return InitialConfigurationSummary(
            hardwareSerial = serial.ifBlank { "-" },
            terminalId = terminalConfig?.terminalId.orEmpty().ifBlank { "-" },
            appVersion = appVersionProvider.versionName().ifBlank { "-" },
            programDate = ReceiptNowFormatter.format(localePreferences),
            merchantId = terminalConfig?.merchantId.orEmpty().ifBlank { "-" },
        )
    }

    suspend fun printConfigurationReceipt(summary: InitialConfigurationSummary): String? {
        val bitmap = runCatching {
            InitialConfigurationReceiptBitmapFactory.create(
                title = appContext.getString(R.string.settings_initial_configuration_receipt_title),
                hardwareSerialLabel = appContext.getString(R.string.settings_initial_configuration_hardware_serial),
                hardwareSerial = summary.hardwareSerial,
                terminalIdLabel = appContext.getString(R.string.settings_initial_configuration_terminal_id),
                terminalId = summary.terminalId,
                merchantIdLabel = appContext.getString(R.string.settings_initial_configuration_merchant_id),
                merchantId = summary.merchantId,
                appVersionLabel = appContext.getString(R.string.settings_initial_configuration_app_version),
                appVersion = summary.appVersion,
                programDateLabel = appContext.getString(R.string.settings_initial_configuration_program_date),
                programDate = summary.programDate,
                titleTypeface = paperReceiptTypefaceResolver.titleTypeface(appContext),
                lineTypeface = paperReceiptTypefaceResolver.bodyTypeface(appContext),
                lineTextAlign = paperReceiptTypefaceResolver.lineTextAlign(),
                lineX = paperReceiptTypefaceResolver.lineX(
                    width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                    horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
                ),
            )
        }.getOrElse { error ->
            Log.e(TAG, "build configuration receipt failed", error)
            return error.message.orEmpty()
        }
        return printBitmap(bitmap)
    }

    /** KCV کلیدهای تزریق‌شده (TMK/MAC/DATA/PIN). */
    suspend fun printKcvReceipt(kcv: KeyLoadingKcvSummary): String? {
        val bitmap = runCatching {
            KcvReceiptBitmapFactory.create(
                context = appContext,
                title = appContext.getString(R.string.settings_kcv_receipt_title),
                masterKeyLabel = appContext.getString(R.string.settings_sadad_kcv_master_key),
                masterKeyValue = kcv.master,
                macKeyLabel = appContext.getString(R.string.settings_kcv_mac_key),
                macKeyValue = kcv.mac,
                dataKeyLabel = appContext.getString(R.string.settings_kcv_data_key),
                dataKeyValue = kcv.data,
                pinKeyLabel = appContext.getString(R.string.settings_kcv_pin_key),
                pinKeyValue = kcv.pin,
                fonts = paperReceiptTypefaceResolver.bitmapFonts(
                    context = appContext,
                    width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                    horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
                ),
                pspBrand = receiptPspBrandProvider.current(),
            )
        }.getOrElse { error ->
            Log.e(TAG, "build KCV receipt failed", error)
            return error.message.orEmpty()
        }
        return printBitmap(bitmap)
    }

    private suspend fun printBitmap(bitmap: Bitmap): String? =
        suspendCancellableCoroutine { continuation ->
            val job = CoroutineScope(continuation.context).launch {
                runCatching {
                    device.print(
                        bitmap = bitmap,
                        context = appContext,
                        onSuccess = {
                            if (continuation.isActive) continuation.resume(null)
                        },
                        onFailed = { message ->
                            Log.w(TAG, "print failed: $message")
                            if (continuation.isActive) continuation.resume(message.ifBlank { "-" })
                        },
                    )
                }.onFailure { error ->
                    if (continuation.isActive) continuation.resume(error.message.orEmpty().ifBlank { "-" })
                }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }

    private companion object {
        const val TAG = "SetupReceipt"
    }
}
