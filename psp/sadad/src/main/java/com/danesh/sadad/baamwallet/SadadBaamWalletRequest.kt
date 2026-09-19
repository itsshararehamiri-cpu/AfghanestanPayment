package com.danesh.sadad.baamwallet

import com.danesh.api.TransactionRequest

/**
 * 23-BAAM WALLET INQUIRY (استعلام QR-Code کیف پول سداد) — MTI 0100/0110، DE3 240000.
 * DE63 (Function Code 086) شامل یک «identifier» است؛ چیدمان دقیق طول‌ها در جدول
 * استخراج‌شده از PDF ناخوانا بود (نمونه‌ی سند: «01003018086012000000000001») —
 * قبل از استفاده‌ی واقعی با نمونه/تست سوئیچ صحت‌سنجی شود.
 */
data class SadadBaamWalletRequest(
    val track2: String,
    val amount: String,
    val identifier: String,
) : TransactionRequest
