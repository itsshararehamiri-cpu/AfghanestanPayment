package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.danesh.api.TransactionType
import com.danesh.common.merchant.LocalMicroPaymentIndexConfig

@Stable
data class SuccessReceiptScreenActions(
    val homeEnabled: Boolean,
    val onHomeClick: () -> Unit,
    val onPrintClick: () -> Unit,
)

@Composable
fun rememberSuccessReceiptScreenActions(
    printFlow: SuccessReceiptPrintFlow,
    onHomeClick: () -> Unit,
): SuccessReceiptScreenActions {
    printFlow.merchantPhase
    printFlow.customerReceiptHandled
    printFlow.pendingPrintType
    printFlow.merchantReceiptPrinted
    printFlow.activeReceiptType
    printFlow.receiptContentVersion

    val homeEnabled = printFlow.canNavigateHome()
    return SuccessReceiptScreenActions(
        homeEnabled = homeEnabled,
        onHomeClick = {
            if (printFlow.canNavigateHome()) {
                onHomeClick()
            }
        },
        onPrintClick = {
            printFlow.requestPrint(printFlow.onPrintButtonClick())
        },
    )
}

@Composable
fun rememberSuccessReceiptPrintFlow(
    transactionType: TransactionType? = null,
    merchantReceiptEnabled: Boolean = transactionType != TransactionType.BALANCE,
    transactionAmountRials: Long? = null,
): SuccessReceiptPrintFlow {
    val printMode = LocalMerchantReceiptPrintMode.current
    val microPaymentConfig = LocalMicroPaymentIndexConfig.current
    val effectivePrintMode = remember(printMode, microPaymentConfig, transactionAmountRials) {
        com.danesh.common.receipt.MicroPaymentIndexReceiptRules.resolveEffectivePrintMode(
            baseMode = printMode,
            microPaymentIndexEnabled = microPaymentConfig.enabled,
            thresholdRials = microPaymentConfig.thresholdRials,
            transactionAmountRials = transactionAmountRials,
        )
    }
    return remember(effectivePrintMode, merchantReceiptEnabled) {
        SuccessReceiptPrintFlow(effectivePrintMode, merchantReceiptEnabled)
    }
}
