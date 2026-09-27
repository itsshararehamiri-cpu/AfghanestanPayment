package com.danesh.sadad.kahroba

import com.danesh.api.TransactionRequest

/**
 * 21.1-KAHROBA SALE (NFC) — MTI 0200/0210، DE3 000000، DE22 071.
 * iccData: خروجی EMV کارت (فیلد ۵۵) به‌صورت hex — از KahrobaEmvKernel تأمین می‌شود.
 * pinBlock: خالی یعنی خرید بدون PIN (کارت PIN نخواسته و مبلغ زیر سقف است).
 */
data class SadadKahrobaSaleRequest(
    val track2: String,
    val pinBlock: String,
    val amount: String,
    val iccData: String,
) : TransactionRequest

/** 21.2-KAHROBA BALANCE (NFC) — MTI 0100/0110، DE3 310000، DE22 071. */
data class SadadKahrobaBalanceRequest(
    val track2: String,
    val pinBlock: String,
    val iccData: String,
) : TransactionRequest
