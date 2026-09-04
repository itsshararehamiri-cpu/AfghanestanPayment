package com.danesh.common.receipt

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * ReceiptUi + [SuccessReceiptPrintEffects] در یک scope تا بعد از تایید دیالوگ،
 * bitmap رسید پذیرنده حتماً ساخته و چاپ شود.
 */
@Composable
fun SuccessReceiptPrintHost(
    printFlow: SuccessReceiptPrintFlow,
    context: Context,
    autoFinishDelayMs: Int = RESULT_AUTO_HOME_DELAY_MS,
    onHomeClick: () -> Unit,
    onCustomerReceiptHandled: () -> Unit = {},
    onPrint: (
        bitmap: Bitmap,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) -> Unit,
    receiptContent: @Composable (ReceiptType) -> Unit,
) {
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val receiptType = printFlow.activeReceiptType
    val receiptUiKey = printFlow.receiptUiKey()

    ReceiptUi(
        content = { receiptContent(receiptType) },
        receiptKey = receiptUiKey,
        onGenerateReceipt = { receiptBitmap = it },
    )

    SuccessReceiptPrintEffects(
        printFlow = printFlow,
        receiptBitmap = receiptBitmap,
        context = context,
        autoFinishDelayMs = autoFinishDelayMs,
        onHomeClick = onHomeClick,
        onClearReceiptBitmap = { receiptBitmap = null },
        onCustomerReceiptHandled = onCustomerReceiptHandled,
        onPrint = onPrint,
    )
}
