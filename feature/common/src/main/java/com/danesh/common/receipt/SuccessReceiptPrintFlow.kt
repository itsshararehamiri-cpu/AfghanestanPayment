package com.danesh.common.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.danesh.common.receipt.MerchantReceiptPrintMode.DISABLED
import com.danesh.common.receipt.MerchantReceiptPrintMode.MANDATORY
import com.danesh.common.receipt.MerchantReceiptPrintMode.OPTIONAL

enum class MerchantReceiptPhase {
    NOT_STARTED,
    OPTIONAL_DIALOG,
    MANDATORY_PROMPT,
    MERCHANT_PRINTING,
    DONE,
}

class SuccessReceiptPrintFlow(
    private val printMode: MerchantReceiptPrintMode,
    private val merchantReceiptEnabled: Boolean = true,
) {
    private val effectivePrintMode: MerchantReceiptPrintMode
        get() = if (merchantReceiptEnabled) printMode else DISABLED

    var activeReceiptType by mutableStateOf(ReceiptType.CUSTOMER_RECEIPT)
        private set

    var customerReceiptHandled by mutableStateOf(false)
        private set

    var merchantReceiptPrinted by mutableStateOf(false)
        private set

    var pendingPrintType by mutableStateOf<ReceiptType?>(null)
        private set

    var merchantPhase by mutableStateOf(MerchantReceiptPhase.NOT_STARTED)
        private set

    var receiptContentVersion by mutableStateOf(0)
        private set

    private var autoPrintCustomerTriggered = false

    fun receiptUiKey(): Any = activeReceiptType to receiptContentVersion

    fun canNavigateHome(): Boolean = isFlowComplete()

    fun isFlowComplete(): Boolean =
        customerReceiptHandled &&
            pendingPrintType == null &&
            when (effectivePrintMode) {
                DISABLED -> merchantPhase == MerchantReceiptPhase.DONE
                OPTIONAL -> merchantPhase == MerchantReceiptPhase.DONE
                MANDATORY -> merchantReceiptPrinted
            }

    fun shouldAutoPrintCustomer(): Boolean =
        !autoPrintCustomerTriggered && !customerReceiptHandled

    fun triggerAutoPrintCustomer() {
        if (shouldAutoPrintCustomer()) {
            autoPrintCustomerTriggered = true
            requestPrint(ReceiptType.CUSTOMER_RECEIPT)
        }
    }

    fun onPrintButtonClick(): ReceiptType {
        return when {
            !customerReceiptHandled -> ReceiptType.CUSTOMER_RECEIPT
            effectivePrintMode != DISABLED && !merchantReceiptPrinted -> ReceiptType.MERCHANT_RECEIPT
            else -> ReceiptType.CUSTOMER_RECEIPT
        }
    }

    fun requestPrint(type: ReceiptType) {
        activeReceiptType = type
        pendingPrintType = type
    }

    fun onPrintStarted() {
        pendingPrintType = null
    }

    fun onPrintCompleted(type: ReceiptType) {
        when (type) {
            ReceiptType.CUSTOMER_RECEIPT -> onCustomerReceiptHandled()
            ReceiptType.MERCHANT_RECEIPT -> {
                merchantReceiptPrinted = true
                merchantPhase = MerchantReceiptPhase.DONE
            }
            ReceiptType.DUPLICATE_RECEIPT,
            ReceiptType.UNSETTLED_TRANSACTION,
            -> Unit
        }
    }

    fun onPrintFailed(type: ReceiptType) {
        pendingPrintType = null
        when (type) {
            ReceiptType.CUSTOMER_RECEIPT -> onCustomerReceiptHandled()
            ReceiptType.MERCHANT_RECEIPT -> {
                merchantReceiptPrinted = true
                merchantPhase = MerchantReceiptPhase.DONE
            }
            ReceiptType.DUPLICATE_RECEIPT,
            ReceiptType.UNSETTLED_TRANSACTION,
            -> Unit
        }
    }

    private fun onCustomerReceiptHandled() {
        if (customerReceiptHandled) return
        customerReceiptHandled = true
        merchantPhase = when (effectivePrintMode) {
            DISABLED -> MerchantReceiptPhase.DONE
            OPTIONAL -> MerchantReceiptPhase.OPTIONAL_DIALOG
            MANDATORY -> MerchantReceiptPhase.MANDATORY_PROMPT
        }
    }

    fun onMerchantPrintAccepted() {
        merchantPhase = MerchantReceiptPhase.MERCHANT_PRINTING
        receiptContentVersion++
        requestPrint(ReceiptType.MERCHANT_RECEIPT)
    }

    fun onMerchantPrintDeclined() {
        merchantPhase = MerchantReceiptPhase.DONE
    }

    fun onMandatoryPromptDismissed() {
        if (merchantPhase == MerchantReceiptPhase.MANDATORY_PROMPT && !merchantReceiptPrinted) {
            onMerchantPrintAccepted()
        }
    }

    fun isMerchantReceiptEnabled(): Boolean =
        merchantReceiptEnabled && printMode != DISABLED
}
