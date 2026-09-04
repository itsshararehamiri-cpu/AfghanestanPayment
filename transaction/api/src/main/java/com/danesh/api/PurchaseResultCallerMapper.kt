package com.danesh.api

import org.json.JSONObject

/**
 * Converts an internal purchase result JSON payload to the format expected by caller apps.
 */
fun convertPurchaseResultToJsonObject(response: String): String {
    val result = parseTransactionResultDetail(response)
        ?: return JSONObject().toString()

    val responseForCallerApp = JSONObject()
    responseForCallerApp.put("amount", result.amount)
    responseForCallerApp.put("rrn", result.rrn.orEmpty())
    responseForCallerApp.put("trace", result.trace)
    responseForCallerApp.put("issuerName", result.issuerName)
    responseForCallerApp.put("responseCode", result.responseCode)
    responseForCallerApp.put("posCode", result.posCode)
    responseForCallerApp.put("maskedPan", result.maskedPan.ifBlank { result.pan })
    responseForCallerApp.put("pan", result.maskedPan.ifBlank { result.pan })
    responseForCallerApp.put("responseMessage", result.responseMessage)
    responseForCallerApp.put("merchantId", result.merchantId)
    responseForCallerApp.put("terminalID", result.terminalId)
    responseForCallerApp.put("merchantPhone", result.merchantPhone)
    responseForCallerApp.put("time", result.time)
    responseForCallerApp.put("date", result.date)
    responseForCallerApp.put("merchantName", result.merchantName)
    return responseForCallerApp.toString()
}
