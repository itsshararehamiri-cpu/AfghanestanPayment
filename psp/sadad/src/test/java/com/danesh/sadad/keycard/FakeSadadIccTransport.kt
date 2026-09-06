package com.danesh.sadad.keycard

/**
 * شبیه‌ساز کارت کلید برای تست‌های واحد سرویس/ریدر، بدون نیاز به سخت‌افزار واقعی.
 * رفتار کارت واقعی (انتخاب اپلت → نیاز به تایید PIN → خواندن رکورد) را با یک state
 * machine ساده شبیه‌سازی می‌کند.
 */
class FakeSadadIccTransport(
    private val correctPin: String = "1234",
    private val maxPinTries: Int = 3,
    private val publicModulus: ByteArray = ByteArray(128) { 0x11 },
    private val privateExponent: ByteArray = ByteArray(128) { 0x22 },
    private val encryptedKeysByNumber: Map<Int, ByteArray> = emptyMap(),
) : SadadIccTransport {

    var poweredOn = false
        private set
    var cardPresent = true
    private var selectedApplet: SadadKeyCard? = null
    private var pinVerified = false
    private var remainingTries = maxPinTries

    override fun powerOn(): Boolean {
        poweredOn = true
        return true
    }

    override fun powerOff() {
        poweredOn = false
        selectedApplet = null
        pinVerified = false
    }

    override fun isCardPresent(): Boolean = cardPresent

    override suspend fun exchange(command: ByteArray): ByteArray {
        val cla = command[0].toInt() and 0xFF
        val ins = command[1].toInt() and 0xFF
        return when {
            cla == 0x00 && ins == 0xA4 -> handleSelect(command)
            cla == 0xA8 && ins == 0x20 -> handleVerifyPin(command)
            cla == 0xA8 && ins == 0xB3 -> requirePinVerified { ok(publicModulus) }
            cla == 0xA8 && ins == 0xB4 -> requirePinVerified { ok(privateExponent) }
            cla == 0xA8 && ins == 0xB0 -> handleReadEncryptedKey(command)
            else -> sw(0x6D00)
        }
    }

    private fun handleSelect(command: ByteArray): ByteArray {
        val appletIdHex = SadadHex.encode(command.copyOfRange(5, command.size))
        selectedApplet = SadadKeyCard.entries.find { it.appletIdHex == appletIdHex }
            ?: return sw(0x6A82)
        pinVerified = false
        return sw(SadadKeyCardApdu.SW_OK)
    }

    private fun handleVerifyPin(command: ByteArray): ByteArray {
        if (selectedApplet == null) return sw(SadadKeyCardError.PIN_VERIFICATION_NEEDED.sw)
        val lc = command[4].toInt() and 0xFF
        val pin = String(command.copyOfRange(5, 5 + lc), Charsets.US_ASCII)
        return if (pin == correctPin) {
            pinVerified = true
            remainingTries = maxPinTries
            byteArrayOf(0x01, remainingTries.toByte()) + sw(SadadKeyCardApdu.SW_OK)
        } else {
            remainingTries = (remainingTries - 1).coerceAtLeast(0)
            byteArrayOf(0x00, remainingTries.toByte()) + sw(SadadKeyCardApdu.SW_OK)
        }
    }

    private fun handleReadEncryptedKey(command: ByteArray): ByteArray = requirePinVerified {
        val keyNumber = command[3].toInt() and 0xFF
        val value = encryptedKeysByNumber[keyNumber] ?: return@requirePinVerified sw(
            SadadKeyCardError.KEY_INDEX_NOT_AVAILABLE.sw,
        )
        ok(value)
    }

    private inline fun requirePinVerified(block: () -> ByteArray): ByteArray =
        if (pinVerified) block() else sw(SadadKeyCardError.PIN_VERIFICATION_NEEDED.sw)

    private fun ok(body: ByteArray): ByteArray = body + sw(SadadKeyCardApdu.SW_OK)

    private fun sw(value: Int): ByteArray =
        byteArrayOf((value shr 8).toByte(), value.toByte())
}
