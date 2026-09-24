package com.danesh.sadad.queue

/**
 * پاسخ advice/reverse سداد: 0، 2، 5 و 25 موفق هستند.
 */
object SadadSafResponseCodes {
    fun isSuccess(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        val normalized = code.trim().trimStart('0').ifEmpty { "0" }
        return normalized == "0" || normalized == "2" || normalized == "5" || normalized == "25"
    }

    fun isFailure(code: String?): Boolean = !isSuccess(code)
}
