package com.danesh.bp.logon

import com.danesh.api.PspLogonWorkingKeys


internal object BpLogonField62Parser {

    fun parse(field62: ByteArray): PspLogonWorkingKeys {
        require(field62.isNotEmpty()) { "فیلد 62 خالی است" }
        val blockSize = when (field62.size) {
            48 -> 16
            72 -> 24
            else -> error(
                "فیلد 62 logon باید 48 یا 72 بایت باشد (MAC|PIN|DATA)، دریافت: ${field62.size}",
            )
        }
        return PspLogonWorkingKeys(
            encryptedMacKey = field62.copyOfRange(0, blockSize),
            encryptedPinKey = field62.copyOfRange(blockSize, blockSize * 2),
            encryptedDataKey = field62.copyOfRange(blockSize * 2, blockSize * 3))
    }
}
