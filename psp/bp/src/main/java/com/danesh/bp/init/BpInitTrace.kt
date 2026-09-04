package com.danesh.bp.init

import android.util.Log

internal object BpInitTrace {
    private const val TAG = "BpInit"

    fun step(section: String, detail: String = "") {
        val message = if (detail.isBlank()) section else "$section | $detail"
        Log.i(TAG, message)
    }

    fun error(section: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.e(TAG, section)
        } else {
            Log.e(TAG, section, throwable)
        }
    }
}
