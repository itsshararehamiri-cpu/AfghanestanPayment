package com.danesh.common.receipt

enum class MerchantReceiptPrintMode {
    OPTIONAL,
    MANDATORY,
    DISABLED;


    companion object {
        fun fromStored(value: String?): MerchantReceiptPrintMode =
            entries.find { it.name == value } ?: OPTIONAL
    }
}
