package com.danesh.common.receipt

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun SuccessReceiptPrintEffects(
    printFlow: SuccessReceiptPrintFlow,
    receiptBitmap: Bitmap?,
    context: Context,
    autoFinishDelayMs: Int = RESULT_AUTO_HOME_DELAY_MS,
    onHomeClick: () -> Unit,
    onClearReceiptBitmap: () -> Unit = {},
    onCustomerReceiptHandled: () -> Unit = {},
    onPrint: (
        bitmap: Bitmap,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) -> Unit,
) {
    var homeNavigationTriggered by remember { mutableStateOf(false) }

    fun navigateHomeOnce() {
        if (!homeNavigationTriggered) {
            homeNavigationTriggered = true
            onHomeClick()
        }
    }

    fun acceptMerchantPrint() {
        onClearReceiptBitmap()
        printFlow.onMerchantPrintAccepted()
    }

    LaunchedEffect(receiptBitmap) {
        if (receiptBitmap != null && printFlow.shouldAutoPrintCustomer()) {
            printFlow.triggerAutoPrintCustomer()
        }
    }

    LaunchedEffect(receiptBitmap, printFlow.pendingPrintType, printFlow.activeReceiptType) {
        val pendingType = printFlow.pendingPrintType ?: return@LaunchedEffect
        if (pendingType != printFlow.activeReceiptType) return@LaunchedEffect
        if (pendingType == ReceiptType.MERCHANT_RECEIPT) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect
        printFlow.onPrintStarted()
        onPrint(
            bitmap,
            { printFlow.onPrintCompleted(pendingType) },
            { printFlow.onPrintFailed(pendingType) },
        )
    }

    LaunchedEffect(
        printFlow.receiptContentVersion,
        receiptBitmap,
        printFlow.pendingPrintType,
        printFlow.activeReceiptType,
    ) {
        if (printFlow.pendingPrintType != ReceiptType.MERCHANT_RECEIPT) return@LaunchedEffect
        if (printFlow.activeReceiptType != ReceiptType.MERCHANT_RECEIPT) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect

        printFlow.onPrintStarted()
        onPrint(
            bitmap,
            { printFlow.onPrintCompleted(ReceiptType.MERCHANT_RECEIPT) },
            { printFlow.onPrintFailed(ReceiptType.MERCHANT_RECEIPT) },
        )
    }

    LaunchedEffect(printFlow.customerReceiptHandled) {
        if (printFlow.customerReceiptHandled) {
            onCustomerReceiptHandled()
        }
    }

    LaunchedEffect(printFlow.merchantPhase) {
        if (printFlow.merchantPhase == MerchantReceiptPhase.MANDATORY_PROMPT) {
            delay(autoFinishDelayMs.toLong())
            if (printFlow.merchantPhase == MerchantReceiptPhase.MANDATORY_PROMPT) {
                acceptMerchantPrint()
            }
        }
    }

    LaunchedEffect(
        printFlow.customerReceiptHandled,
        printFlow.pendingPrintType,
        printFlow.merchantPhase,
        printFlow.merchantReceiptPrinted,
    ) {
        if (printFlow.canNavigateHome()) {
            delay(autoFinishDelayMs.toLong())
            if (printFlow.canNavigateHome()) {
                navigateHomeOnce()
            }
        }
    }

    if (printFlow.merchantPhase == MerchantReceiptPhase.OPTIONAL_DIALOG) {
        MerchantReceiptOptionalDialog(
            onConfirm = { acceptMerchantPrint() },
            onDecline = {
                printFlow.onMerchantPrintDeclined()
                navigateHomeOnce()
            },
        )
    }

    if (printFlow.merchantPhase == MerchantReceiptPhase.MANDATORY_PROMPT) {
        MerchantReceiptMandatoryDialog(
            onContinue = { acceptMerchantPrint() },
        )
    }
}
