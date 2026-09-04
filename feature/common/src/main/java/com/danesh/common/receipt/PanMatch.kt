package com.danesh.common.receipt

import android.util.Log

/** تطبیق PAN کشیده‌شده با PAN ماسک‌شده ذخیره‌شده (۶ رقم اول + ۴ رقم آخر). */
fun pansMatchForReprint(swipedPan: String, storedMaskedPan: String): Boolean {
    val swipedDigits = swipedPan.filter(Char::isDigit)
    val storedDigits = storedMaskedPan.filter(Char::isDigit)
    if (swipedDigits.length < 10 || storedDigits.length < 10) return false
    val t= swipedDigits.take(6) == storedDigits.take(6) &&
        swipedDigits.takeLast(4) == storedDigits.takeLast(4)
    return t
}
