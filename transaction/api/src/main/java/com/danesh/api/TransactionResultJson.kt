package com.danesh.api

import android.net.Uri
import com.google.gson.Gson

private val transactionResultGson = Gson()

fun parseTransactionResultDetail(response: String): TransactionResultDetail? {
    if (response.isBlank()) return null
    val payload = runCatching { Uri.decode(response) }.getOrDefault(response)
    val parsed = runCatching {
        transactionResultGson.fromJson(payload, TransactionResultDetail::class.java)
    }.getOrNull() ?: return null
    return parsed.copy(isSuccess = parsed.resolvedSuccess())
}

fun TransactionResultDetail.toJson(): String = transactionResultGson.toJson(this)
