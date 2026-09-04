package com.danesh.common.receipt

import com.danesh.api.TransactionResultDetail
import com.danesh.api.maskPanForDisplay

fun String.maskPanForReceipt(): String = maskPanForDisplay()

fun TransactionResultDetail.receiptPan(): String =
    maskedPan.ifBlank { pan }.maskPanForReceipt()
