package com.danesh.topup

import androidx.annotation.DrawableRes
import com.danesh.topup.R

enum class MobileOperator(
    val displayName: String,
    @DrawableRes val logoRes: Int,
) {
    ROSHAN("Roshan", R.drawable.ic_mobile),
    MTN("MTN", R.drawable.ic_mobile),
    AWCC("AWCC", R.drawable.ic_mobile),
    ETISALAT("Etisalat", R.drawable.ic_mobile);


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
