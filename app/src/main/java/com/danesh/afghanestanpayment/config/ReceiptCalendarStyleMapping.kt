package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.ReceiptCalendarStyle

fun ActivePsp.toReceiptCalendarStyle(): ReceiptCalendarStyle = when (this) {
    ActivePsp.BP -> ReceiptCalendarStyle.IRANIAN_SHAMSI
    else -> ReceiptCalendarStyle.AFGHAN_SOLAR
}
