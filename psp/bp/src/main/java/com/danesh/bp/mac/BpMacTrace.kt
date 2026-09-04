package com.danesh.bp.mac

import android.util.Log

internal object BpMacTrace {
    private const val TAG = "BpMac"
    private const val HEX_CHUNK_SIZE = 800

    fun step(section: String, detail: String = "") {
        val message = if (detail.isBlank()) section else "$section | $detail"
        Log.i(TAG, message)
    }

    fun logHex(section: String, label: String, hex: String) {
        step(section, "$label len=${hex.length / 2} bytes")
        if (hex.length <= HEX_CHUNK_SIZE) {
            step(section, "$label hex=$hex")
            return
        }
        var offset = 0
        var part = 1
        while (offset < hex.length) {
            val end = minOf(offset + HEX_CHUNK_SIZE, hex.length)
            step(section, "$label hex part $part=${hex.substring(offset, end)}")
            offset = end
            part++
        }
    }

    fun error(section: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.e(TAG, section)
        } else {
            Log.e(TAG, section, throwable)
        }
    }
}
