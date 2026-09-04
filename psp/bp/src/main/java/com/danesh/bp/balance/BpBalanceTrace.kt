package com.danesh.bp.balance

import android.util.Log
import com.danesh.iso.IsoMessage
import com.danesh.iso.requireBp

internal object BpBalanceTrace {
    private const val TAG = "BpBalance"

    fun step(detail: String) {
        Log.i(TAG, detail)
    }

    fun verifyMacBitmap(message: IsoMessage) {
        val bp = message.requireBp()
        val macInput = bp.packForMac()
        val bitmapHex = bp.primaryBitmapHex(macInput)
        val bit64 = bp.isPrimaryBitmapBit64Set(macInput)
        check(bit64) {
            "استعلام موجودی: بیت 64 bitmap باید 1 باشد ولی روشن نیست — bitmap=$bitmapHex"
        }
    }
}
