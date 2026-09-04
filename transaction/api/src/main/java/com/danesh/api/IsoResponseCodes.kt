package com.danesh.api

object IsoResponseCodes {
    fun isApproved(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        val normalized = code.trim()
        return normalized.all { it == '0' }
    }

    fun isFailure(code: String?): Boolean = !isApproved(code)
}

fun TransactionResultDetail.resolvedSuccess(): Boolean =
    isSuccess || IsoResponseCodes.isApproved(responseCode)
