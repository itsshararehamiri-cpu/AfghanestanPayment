package com.danesh.api

open class QueueItem(
    open val date: String,
    open val time: String,
    open val dateTime: String,
    open val type: Int,
    open val processingCode: String,
    open val amount: String,
    open val stan: String,
    open val merchantId: String,
    open val terminalId: String,
    open val currency: String,
    open val posConditionCode: String = "",
    open val maskedPan: String? = null,
    open val rrn: String? = null,
    open val issuer: String? = null,
    open val responseCode: Int? = null,
    open val responseMsg: String? = null,
    /** آیا رسید مشتری برای این رکورد SAF چاپ/نمایش داده شده است */
    open val customerReceiptPrinted: Boolean = false,
    open val adviceAdditionalData: String? = null,
    open val reverseField48Tag21: String? = null,
    /** PAN مبدأ برای Reverse همراه‌پی (DE2) */
    open val sourcePan: String? = null,
    /** Function Code اصلی (DE24) برای Reverse — 689 / 781 */
    open val functionCode: String? = null,
    /** تگ مقصد DE48 برای Reverse — 021 یا 045 */
    open val reverseDestTag: String? = null,
    /** مقدار مقصد DE48 برای Reverse */
    open val reverseDestValue: String? = null,
    /**
     * وضعیت چرخه عمر SAF:
     * [SafStatuses.NEEDS_REVERSE] → Reverse (HP: 1420)،
     * [SafStatuses.NEEDS_ADVICE] → Advice.
     */
    open val status: Char = SafStatuses.NEEDS_REVERSE,
    /**
     * هم‌تراز با [status] نگه داشته می‌شود برای سازگاری؛ ترجیحاً از [status] تصمیم بگیرید.
     * [QueueOperations.ADVICE] یا [QueueOperations.REVERSE]
     */
    open val queueOperation: Char = QueueOperations.REVERSE,
)
