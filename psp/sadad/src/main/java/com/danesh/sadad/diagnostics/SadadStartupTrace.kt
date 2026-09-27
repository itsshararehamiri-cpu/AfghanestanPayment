package com.danesh.sadad.diagnostics

import com.danesh.common.diagnostics.StartupTraceFile
import com.danesh.iso.IsoMessage

private val REDACTED_FIELDS = setOf(2, 35, 48, 52, 55, 62, 64, 128)

internal fun traceIso(stage: String, message: IsoMessage?) {
    if (message == null) {
        StartupTraceFile.line(stage, "message=null")
        return
    }
    val iso = runCatching { message.getIsoMessage() }.getOrNull()
    val fields = if (iso == null) {
        "mti=${message.mti} stan=${message.stan} rc=${message.responseCode} tid=${message.terminalId}"
    } else {
        buildString {
            for (field in 0..iso.maxField) {
                if (!iso.hasField(field)) continue
                append("F$field=")
                if (field in REDACTED_FIELDS) {
                    val len = iso.getBytes(field)?.size ?: iso.getString(field)?.length ?: 0
                    append("len=$len")
                } else {
                    val text = iso.getString(field)?.replace('\n', ' ')?.take(120)
                    append(text ?: "bin")
                }
                append(' ')
            }
        }.trim()
    }
    StartupTraceFile.line(stage, fields)
}
