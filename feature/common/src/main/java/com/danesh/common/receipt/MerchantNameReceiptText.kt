package com.danesh.common.receipt

private const val PAPER_MERCHANT_NAME_MAX_LENGTH = 20
private const val ELECTRONIC_MERCHANT_NAME_MAX_LENGTH = 30
private const val ELLIPSIS = "..."

fun truncateMerchantNameForReceipt(
    name: String,
    isPaperReceipt: Boolean,
): String = truncateWithEllipsis(
    text = name.trim(),
    maxLength = if (isPaperReceipt) {
        PAPER_MERCHANT_NAME_MAX_LENGTH
    } else {
        ELECTRONIC_MERCHANT_NAME_MAX_LENGTH
    },
)

fun truncateWithEllipsis(text: String, maxLength: Int): String {
    if (text.length <= maxLength) return text
    if (maxLength <= ELLIPSIS.length) return ELLIPSIS.take(maxLength)
    return text.take(maxLength - ELLIPSIS.length) + ELLIPSIS
}
