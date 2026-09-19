package com.danesh.sadad.keycard

import android.util.Log
import com.danesh.core.Device
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * لایه‌ی نازک روی [Device] برای تبادل APDU با کارت‌خوان دستگاه.
 * جداسازی این واسط، تست واحد منطق کارت کلید سداد را بدون نیاز به پیاده‌سازی کامل [Device] ممکن می‌کند.
 */
interface SadadIccTransport {
    fun powerOn(): Boolean
    fun powerOff()
    fun isCardPresent(): Boolean
    suspend fun exchange(command: ByteArray): ByteArray
}

@Singleton
class SadadDeviceIccTransport @Inject constructor(
    private val device: Device,
) : SadadIccTransport {

    override fun powerOn(): Boolean {
        Log.d("TAG", "powerOn: SadadDeviceIccTransport")
        return device.powerOnIcCard()
    }

    override fun powerOff() {
        Log.d("TAG", "powerOff: SadadDeviceIccTransport")

        return device.powerOffIcCard()
    }

    override fun isCardPresent(): Boolean {
        Log.d("TAG", "isCardPresent: SadadDeviceIccTransport")

        return device.isIcCardDetect()
    }

    override suspend fun exchange(command: ByteArray): ByteArray {
        Log.d("TAG", "exchange: SadadDeviceIccTransport${ISOUtil.hexString(command)}")

        var errorMessage: String? = null
        val response = device.sendApdu(command) { message -> errorMessage = message }
        return response ?: throw SadadKeyCardException(
            errorMessage ?: "پاسخی از کارت‌خوان دریافت نشد",
        )// TODO:  
    }
}
