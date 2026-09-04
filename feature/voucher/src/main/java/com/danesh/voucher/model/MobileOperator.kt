package com.danesh.voucher.model

import androidx.annotation.DrawableRes
import com.danesh.common.R

enum class MobileOperator(
    val displayName: String,
    @DrawableRes val logoRes: Int,
) {
    ROSHAN("Roshan", com.danesh.voucher.R.drawable.ic_irancel),
    MTN("MTN", com.danesh.voucher.R.drawable.ic_irancel),
    AWCC("AWCC", com.danesh.voucher.R.drawable.ic_irancel),
    ETISALAT("Etisalat", com.danesh.voucher.R.drawable.ic_irancel);


    companion object {
        val default: MobileOperator get() = ROSHAN
    }
}

fun MobileOperator.operatorCode(): String = when (this) {
    MobileOperator.ROSHAN -> "1"
    MobileOperator.MTN -> "2"
    MobileOperator.AWCC -> "3"
    MobileOperator.ETISALAT -> "4"
}
