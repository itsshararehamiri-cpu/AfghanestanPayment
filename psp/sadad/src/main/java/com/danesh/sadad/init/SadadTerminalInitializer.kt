package com.danesh.sadad.init

import com.danesh.sadad.util.ByteCursor
import com.danesh.sadad.util.IranSystemEncoding
import com.danesh.sadad.util.SadadField63Wire

/**
 * Host Function Code 013 — Terminal initializer.
 *
 * Terminal Id n8، Card Acq Id n15،
 * سپس Len n3 + ans برای نام/آدرس فارسی و انگلیسی و تلفن،
 * Postal Code n10، Lines Count n2،
 * سپس Len n2 + ans برای Headline / Tax Memory Unique Code /
 * Sales Fund Device Serial / Sales Fund Memory Serial.
 */
data class TerminalInitializer(
    val terminalId: String,
    val acqId: String,
    val acqNameFa: String,
    val acqNameEn: String,
    val acqAddressFa: String,
    val acqAddressEn: String,
    val tel: String,
    val postalCode: String,
    val linesCount: String,
    val headlineNo: String = "",
    val taxMemoryUniqueCode: String = "",
    val salesFundDeviceSerial: String = "",
    val salesFundMemorySerial: String = "",
)

object SadadTerminalInitializerCodec {
    private const val LEN_TERMINAL_ID = 8
    private const val LEN_MERCHANT_ID = 15
    private const val LEN_POSTAL_CODE = 10
    private const val MAX_MERCHANT_NAME_LEN = 40

    fun parse(data: String): TerminalInitializer? {
        if (data.isBlank()) return null
        return parse(data.toByteArray(SadadField63Wire.CHARSET))
    }

    fun parse(raw: ByteArray): TerminalInitializer? {
        if (raw.size < LEN_TERMINAL_ID + LEN_MERCHANT_ID) return null
        return runCatching { parseOrThrow(raw) }.getOrNull()
    }

    private fun parseOrThrow(raw: ByteArray): TerminalInitializer {
        val cursor = ByteCursor(raw)
        val terminalId = cursor.readAscii(LEN_TERMINAL_ID).trim()
        val merchantId = cursor.readAscii(LEN_MERCHANT_ID).trim()

        val nameFa = readLengthPrefixed(cursor, 3) { bytes, fullLen ->
            val (offset, copyLen) = clipMerchantName(fullLen)
            val slice = bytes.copyOfRange(offset, (offset + copyLen).coerceAtMost(bytes.size))
            IranSystemEncoding.fieldToUtf8(slice)
        }
        val nameEn = readLengthPrefixed(cursor, 3) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val addrFa = readLengthPrefixed(cursor, 3) { bytes, _ ->
            IranSystemEncoding.fieldToUtf8(bytes)
        }
        val addrEn = readLengthPrefixed(cursor, 3) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val tel = readLengthPrefixed(cursor, 3) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val postalCode = cursor.readAscii(LEN_POSTAL_CODE)
        val linesCount = if (cursor.has(2)) cursor.readAscii(2) else "00"

        val headlineNo = readLengthPrefixed(cursor, 2) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val uniqueCode = readLengthPrefixed(cursor, 2) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val deviceSerial = readLengthPrefixed(cursor, 2) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }
        val memorySerial = readLengthPrefixed(cursor, 2) { bytes, _ ->
            String(bytes, SadadField63Wire.CHARSET).trim()
        }

        return TerminalInitializer(
            terminalId = terminalId,
            acqId = merchantId,
            acqNameFa = nameFa,
            acqNameEn = nameEn,
            acqAddressFa = addrFa,
            acqAddressEn = addrEn,
            tel = tel,
            postalCode = postalCode,
            linesCount = linesCount,
            headlineNo = headlineNo,
            taxMemoryUniqueCode = uniqueCode,
            salesFundDeviceSerial = deviceSerial,
            salesFundMemorySerial = memorySerial,
        )
    }

    private fun clipMerchantName(fullLen: Int): Pair<Int, Int> {
        if (fullLen <= MAX_MERCHANT_NAME_LEN + 1) return 0 to fullLen
        var skip = fullLen - MAX_MERCHANT_NAME_LEN
        if (skip % 2 != 0) skip++
        return skip to MAX_MERCHANT_NAME_LEN
    }

    private fun readLengthPrefixed(
        cursor: ByteCursor,
        lengthDigits: Int,
        decode: (ByteArray, Int) -> String,
    ): String {
        if (!cursor.has(lengthDigits)) return ""
        val length = cursor.readDigits(lengthDigits)
        if (length <= 0) return ""
        if (!cursor.has(length)) return ""
        return decode(cursor.readBytes(length), length)
    }
}
