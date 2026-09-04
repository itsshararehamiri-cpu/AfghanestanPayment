package com.danesh.common.receipt

import com.danesh.api.TransactionType

enum class ReceiptType {
    MERCHANT_RECEIPT,
    CUSTOMER_RECEIPT,
    DUPLICATE_RECEIPT,
    UNSETTLED_TRANSACTION,
}

/** موجودی: رسید پذیرنده تحت هیچ شرایطی چاپ/نمایش داده نمی‌شود. */
fun ReceiptType.forTransaction(transactionType: TransactionType): ReceiptType =
    if (transactionType == TransactionType.BALANCE && this == ReceiptType.MERCHANT_RECEIPT) {
        ReceiptType.CUSTOMER_RECEIPT
    } else {
        this
    }
