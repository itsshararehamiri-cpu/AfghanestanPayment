package com.danesh.sadad.keycard

import com.danesh.core.Device
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

    override fun powerOn(): Boolean = device.powerOnIcCard()

    override fun powerOff() = device.powerOffIcCard()

    override fun isCardPresent(): Boolean = device.isIcCardDetect()

    override suspend fun exchange(command: ByteArray): ByteArray {
        var errorMessage: String? = null
        val response = device.sendApdu(command) { message -> errorMessage = message }
        return response ?: throw SadadKeyCardException(
            errorMessage ?: "پاسخی از کارت‌خوان دریافت نشد",
        )
    }
}
