package com.danesh.common.receipt

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.danesh.api.QueueCustomerReceiptPrinter
import com.danesh.api.QueueItem
import com.danesh.api.TransactionContextProvider
import com.danesh.api.toTransactionResultDetail
import com.danesh.common.locale.LocalReceiptCalendarStyle
import com.danesh.common.locale.LocalReceiptLocale
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptCalendarStyleProvider
import com.danesh.common.locale.toAppLocale
import com.danesh.core.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class ComposeQueueCustomerReceiptPrinter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val device: Device,
    private val contextProvider: TransactionContextProvider,
    private val receiptCalendarStyleProvider: ReceiptCalendarStyleProvider,
    private val localePreferences: LocalePreferences,
) : QueueCustomerReceiptPrinter {

    override suspend fun printCustomerReceipt(item: QueueItem): Boolean =
        withContext(Dispatchers.Main) {
            val detail = item.toTransactionResultDetail(contextProvider.getTerminalConfig())
            val bitmap = renderReceiptBitmap(detail) ?: return@withContext false
            printBitmap(bitmap)
        }

    private suspend fun renderReceiptBitmap(
        detail: com.danesh.api.TransactionResultDetail,
    ): Bitmap? = suspendCancellableCoroutine { continuation ->
        val hostContext = context.applicationContext
        val root = FrameLayout(hostContext)
        val composeView = ComposeView(hostContext)
        root.addView(
            composeView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        composeView.setContent {
            CompositionLocalProvider(
                LocalContext provides hostContext,
                LocalReceiptLocale provides localePreferences.getLanguage().toAppLocale(),
                LocalReceiptCalendarStyle provides receiptCalendarStyleProvider.getCalendarStyle(),
            ) {
                ReceiptUi(
                    content = {
                        TransactionPaperReceipt(
                            result = detail,
                            receiptType = ReceiptType.CUSTOMER_RECEIPT,
                            isPaperReceipt = true,
                        )
                    },
                    receiptKey = detail.dateTime.ifBlank { detail.trace },
                ) { bitmap ->
                    if (continuation.isActive) {
                        continuation.resume(bitmap)
                    }
                }
            }
        }
        composeView.post {
            measureAndLayout(root, hostContext)
        }
    }

    private suspend fun printBitmap(bitmap: Bitmap): Boolean = coroutineScope {
        suspendCancellableCoroutine { continuation ->
            launch {
                device.print(
                    bitmap = bitmap,
                    context = context,
                    onSuccess = {
                        if (continuation.isActive) continuation.resume(true)
                    },
                    onFailed = {
                        if (continuation.isActive) continuation.resume(false)
                    },
                )
            }
        }
    }

    private fun measureAndLayout(root: View, context: Context) {
        val width = context.resources.displayMetrics.widthPixels
        val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        root.measure(widthSpec, heightSpec)
        root.layout(0, 0, root.measuredWidth, root.measuredHeight)
    }
}
