package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.ReceiptCalendarStyle

fun ActivePsp.toReceiptCalendarStyle(): ReceiptCalendarStyle = when (this) {
    // سداد هم مانند به‌پرداخت بازار ایران را هدف قرار می‌دهد؛ تقویم شمسی ایرانی نه افغانی.
    ActivePsp.BP, ActivePsp.SADAD -> ReceiptCalendarStyle.IRANIAN_SHAMSI
    else -> ReceiptCalendarStyle.AFGHAN_SOLAR
}
