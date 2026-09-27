package com.danesh.common.card

/**
 * تنظیمات کهربا (پرداخت بدون تماس EMV) — از طریق flavor تعیین می‌شود (فعلاً فقط سداد).
 * کارت مغناطیسی همیشه فعال است؛ کهربا یک روش اختیاری در کنار آن است.
 */
interface KahrobaPolicy {
    /** کهربا برای این PSP فعال است. */
    val isEnabled: Boolean

    /**
     * سقف مبلغ (ریال) خرید کهربا بدون PIN. مبلغ بیشتر از این همیشه PIN می‌خواهد.
     * صفر یعنی همیشه PIN گرفته شود.
     */
    val noPinAmountLimit: Long

    companion object {
        val Disabled: KahrobaPolicy = object : KahrobaPolicy {
            override val isEnabled: Boolean = false
            override val noPinAmountLimit: Long = 0
        }
    }
}

/** درخواست خواندن کهربا از صفحه‌ی کارت؛ null یعنی فقط کارت مغناطیسی. */
sealed interface ContactlessReadRequest {
    data class Purchase(val amount: String) : ContactlessReadRequest
    data object Balance : ContactlessReadRequest
}

/**
 * قانون PIN کهربا مطابق KahrobaPurchase/KahrobaBalance در transaction_process:
 * مانده همیشه PIN می‌خواهد؛ خرید اگر کارت خواسته یا مبلغ از سقف بیشتر باشد.
 */
fun kahrobaPinRequired(
    request: ContactlessReadRequest,
    cardRequestsPin: Boolean,
    noPinAmountLimit: Long,
): Boolean = when (request) {
    ContactlessReadRequest.Balance -> true
    is ContactlessReadRequest.Purchase -> {
        val amount = request.amount.filter(Char::isDigit).toLongOrNull() ?: 0L
        cardRequestsPin || amount > noPinAmountLimit
    }
}

/** کد پاسخ سوئیچ برای «PIN لازم است» در خرید کهربا بدون PIN (معادل result == 76 در C). */
const val KAHROBA_PIN_REQUIRED_RESPONSE_CODE = "76"
