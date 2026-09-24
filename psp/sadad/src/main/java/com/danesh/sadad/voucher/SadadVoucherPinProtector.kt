package com.danesh.sadad.voucher

import android.util.Log
import com.danesh.core.Device
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * رمز پین شارژ سداد با کلید دیتای دستگاه (DEK)، معادل `securityService.encryptData`.
 */
@Singleton
class SadadVoucherPinProtector @Inject constructor(
    private val device: Device,
) {

    fun encryptToHex(plainPin: String): String {
        val pin = plainPin.trim()
        if (pin.isEmpty()) return ""

        val input = padToDesBlock(pin.toByteArray(Charsets.US_ASCII))

        Log.d(TAG, "PIN length = ${pin.length}")
        Log.d(TAG, "INPUT HEX = ${ISOUtil.hexString(input)}")
        Log.d("VOUCHER_DEBUG", "RAW FIELD62 PIN = [$pin]")
        val encrypted = device.decrypt(input)
        Log.d("VOUCHER_DEBUG", "ENCRYPTED PIN = [${encrypted?.let { ISOUtil.hexString(it) }}]")

        Log.d(TAG, "ENCRYPTED BYTE LENGTH = ${encrypted?.size}")
        Log.d(TAG, "ENCRYPTED HEX = ${encrypted?.let { ISOUtil.hexString(it) }}")

        if (encrypted == null || encrypted.isEmpty()) {
            Log.w(TAG, "encryptDataByDek failed for voucher PIN")
            return ""
        }

        return ISOUtil.hexString(encrypted).uppercase()
    }

    private fun padToDesBlock(data: ByteArray): ByteArray {
        if (data.isEmpty()) return ByteArray(8)
        if (data.size % 8 == 0) return data
        val padded = ByteArray(((data.size / 8) + 1) * 8)
        data.copyInto(padded)
        return padded
    }

    private companion object {
        const val TAG = "SadadVoucherPin"
    }
}
